// Adapted from Gimm1q by Mim1q, MIT License.
// https://github.com/mim1q/Gimm1q/tree/1.21.1
package dev.mim1q.gimm1q.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.network.s2c.ValueCalculatorSyncS2CPacket;
import dev.mim1q.gimm1q.valuecalculators.ValueCalculatorsReloadedCallback;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.neoforged.fml.loading.FMLPaths;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;
import redempt.crunch.functional.ExpressionEnv;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/** The subset of Gimm1q's calculator format used by Mine Cells. */
public final class ValueCalculatorResourceReloader implements SynchronousResourceReloader {
    public static final ValueCalculatorResourceReloader INSTANCE = new ValueCalculatorResourceReloader();
    private static final String PREFIX = "value_calculators/";
    private static final Pattern VARIABLE = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*(?![a-zA-Z_0-9])\\(?");
    private volatile Map<Identifier, Calculator> calculators = Map.of();
    private volatile Map<Identifier, JsonObject> sourceJson = Map.of();

    private ValueCalculatorResourceReloader() {}

    @Override
    public void reload(ResourceManager manager) {
        var loaded = new HashMap<Identifier, Calculator>();
        var source = new HashMap<Identifier, JsonObject>();
        manager.findResources("value_calculators", id ->
            id.getPath().startsWith(PREFIX) && id.getPath().endsWith(".json")
        ).forEach((path, resource) -> {
            var relative = path.getPath().substring(PREFIX.length(), path.getPath().length() - 5);
            var id = Identifier.of(path.getNamespace(), relative);
            try (var reader = resource.getReader()) {
                var json = JsonParser.parseReader(reader).getAsJsonObject();
                loaded.put(id, Calculator.parse(json));
                source.put(id, json);
            } catch (Exception e) {
                Gimm1q.LOGGER.error("Failed to load value calculator {}", id, e);
            }
        });
        applyConfigOverrides(loaded, source);
        calculators = Map.copyOf(loaded);
        sourceJson = Map.copyOf(source);
        ValueCalculatorsReloadedCallback.EVENT.invoker().onValueCalculatorsReloaded(calculators.keySet());
        Gimm1q.LOGGER.info("Loaded {} Mine Cells value calculators", calculators.size());
    }

    private static void applyConfigOverrides(Map<Identifier, Calculator> loaded,
                                             Map<Identifier, JsonObject> source) {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        for (var entry : Map.copyOf(source).entrySet()) {
            var id = entry.getKey();
            var directory = FMLPaths.CONFIGDIR.get().resolve(id.getNamespace()).resolve("value_calculators");
            var template = directory.resolve(id.getPath() + ".template.json");
            var override = directory.resolve(id.getPath() + ".json");
            try {
                Files.createDirectories(template.getParent());
                Files.writeString(template, gson.toJson(entry.getValue()), StandardCharsets.UTF_8);
                if (!Files.isRegularFile(override)) continue;
                try (var reader = Files.newBufferedReader(override, StandardCharsets.UTF_8)) {
                    var merged = merge(entry.getValue(), JsonParser.parseReader(reader).getAsJsonObject());
                    source.put(id, merged);
                    loaded.put(id, Calculator.parse(merged));
                }
            } catch (Exception e) {
                Gimm1q.LOGGER.error("Failed to load value calculator config {}", override, e);
            }
        }
    }

    private static JsonObject merge(JsonObject base, JsonObject override) {
        var merged = base.deepCopy();
        for (var entry : override.entrySet()) {
            if ((entry.getKey().equals("variables") || entry.getKey().equals("equations"))
                && entry.getValue().isJsonObject() && merged.has(entry.getKey())) {
                var section = merged.getAsJsonObject(entry.getKey());
                entry.getValue().getAsJsonObject().entrySet().forEach(value ->
                    section.add(value.getKey(), value.getValue()));
            } else {
                merged.add(entry.getKey(), entry.getValue());
            }
        }
        return merged;
    }

    public Optional<Double> calculateExpression(Identifier id, String name, ValueCalculatorContext context) {
        var calculator = calculators.get(id);
        return calculator == null ? Optional.empty() : calculator.calculate(name, context);
    }

    public static Set<Identifier> getAllIds() {
        return INSTANCE.calculators.keySet();
    }

    public ValueCalculatorSyncS2CPacket createPacket() {
        var root = new JsonObject();
        sourceJson.forEach((id, json) -> root.add(id.toString(), json));
        return new ValueCalculatorSyncS2CPacket(root.toString());
    }

    public void replaceWith(ValueCalculatorSyncS2CPacket packet) {
        try {
            var root = JsonParser.parseString(packet.json()).getAsJsonObject();
            var loaded = new HashMap<Identifier, Calculator>();
            root.entrySet().forEach(entry -> {
                var id = Identifier.tryParse(entry.getKey());
                if (id != null && entry.getValue().isJsonObject()) {
                    loaded.put(id, Calculator.parse(entry.getValue().getAsJsonObject()));
                }
            });
            calculators = Map.copyOf(loaded);
            ValueCalculatorsReloadedCallback.EVENT.invoker().onValueCalculatorsReloaded(calculators.keySet());
        } catch (Exception e) {
            Gimm1q.LOGGER.error("Failed to apply synchronized value calculators", e);
        }
    }

    private record Calculator(Map<String, JsonObject> variables, Map<String, Expression> equations) {
        static Calculator parse(JsonObject json) {
            var variables = new HashMap<String, JsonObject>();
            if (json.has("variables")) json.getAsJsonObject("variables").entrySet().forEach(entry -> {
                if (entry.getValue().isJsonObject()) variables.put(entry.getKey(), entry.getValue().getAsJsonObject());
            });
            var equations = new HashMap<String, Expression>();
            if (json.has("equations")) json.getAsJsonObject("equations").entrySet().forEach(entry -> {
                try {
                    equations.put(entry.getKey(), Expression.parse(entry.getValue()));
                } catch (Exception e) {
                    Gimm1q.LOGGER.error("Failed to parse value calculator equation {}", entry.getKey(), e);
                }
            });
            return new Calculator(Map.copyOf(variables), Map.copyOf(equations));
        }

        Optional<Double> calculate(String name, ValueCalculatorContext context) {
            var equation = equations.get(name);
            if (equation == null) return Optional.empty();
            try {
                var values = new double[equation.variableNames.length];
                for (int i = 0; i < values.length; i++) {
                    var variableName = equation.variableNames[i];
                    values[i] = context.getVariableOverride(variableName)
                        .orElseGet(() -> resolveVariable(variables.get(variableName), context));
                }
                return Optional.of(equation.compiled.evaluate(values));
            } catch (Exception e) {
                Gimm1q.LOGGER.error("Failed to evaluate value calculator equation {}", name, e);
                return Optional.empty();
            }
        }

        private static double resolveVariable(JsonObject source, ValueCalculatorContext context) {
            if (source == null) return 0.0;
            var type = source.has("type") ? source.get("type").getAsString() : "";
            if (type.equals("gimm1q:enchantment")) {
                var holder = context.get(ValueCalculatorParameter.HOLDER);
                var stack = context.get(ValueCalculatorParameter.HOLDER_STACK);
                if (stack == null && holder != null) stack = holder.getStackInHand(holder.getActiveHand());
                if (holder == null || stack == null || stack == ItemStack.EMPTY) return 0.0;
                var id = Identifier.tryParse(source.get("enchantment").getAsString());
                if (id == null) return 0.0;
                var entry = holder.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(id);
                return entry.isPresent() ? EnchantmentHelper.getLevel(entry.get(), stack) : 0.0;
            }
            if (type.equals("gimm1q:attribute")) {
                var holder = context.get(ValueCalculatorParameter.HOLDER);
                if (holder == null || !source.has("attribute")) return 0.0;
                var id = Identifier.tryParse(source.get("attribute").getAsString().trim());
                if (id == null) return 0.0;
                EntityAttribute attribute = Registries.ATTRIBUTE.get(id);
                if (attribute == null) return source.has("fallback") ? source.get("fallback").getAsDouble() : 0.0;
                var entry = Registries.ATTRIBUTE.getEntry(attribute);
                return holder.getAttributes().hasAttribute(entry)
                    ? holder.getAttributeValue(entry)
                    : source.has("fallback") ? source.get("fallback").getAsDouble() : 0.0;
            }
            return source.has("fallback") ? source.get("fallback").getAsDouble() : 0.0;
        }
    }

    private record Expression(String[] variableNames, CompiledExpression compiled) {
        static Expression parse(JsonElement json) {
            var text = json.getAsString();
            var names = new LinkedHashSet<String>();
            var matcher = VARIABLE.matcher(text);
            while (matcher.find()) {
                var token = matcher.group();
                if (!token.endsWith("(")) names.add(token);
            }
            var variables = names.toArray(String[]::new);
            ExpressionEnv env = new ExpressionEnv().setVariableNames(variables);
            env.addFunction("if", 3, args -> args[0] != 0 ? args[1] : args[2]);
            return new Expression(variables, Crunch.compileExpression(text, env));
        }
    }
}
