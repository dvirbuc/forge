package forge.item;

import forge.util.MyRandom;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class BoosterSlot {
    private final String slotName;
    private String baseRarity;
    private float startRange = 0.0f;
    private final TreeMap<Float, String> slotPercentages = new TreeMap<>();
    private final List<Pair<String, String>> pairedSheets = new ArrayList<>();

    public BoosterSlot(final String slotName, final List<String> contents) {
        this.slotName = slotName;
        this.baseRarity = null;
        parseContents(contents);
    }

    public final String getSlotName() {
        return slotName;
    }

    public static BoosterSlot parseSlot(final String slotName, final List<String> contents) {
        return new BoosterSlot(slotName, contents);
    }

    private void parseContents(List<String> contents) {
        for (String content : contents) {
            if (content.startsWith("#")) {
                continue;
            }
            String[] parts = content.split("=", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid booster slot entry: " + content);
            }

            String key = parts[0].trim();
            String value = parts[1].trim();

            if (key.equalsIgnoreCase("Base")) {
                baseRarity = value;
            } else if (key.equalsIgnoreCase("Replace")) {
                String[] replaceParts = value.split(" ", 2);
                if (replaceParts.length != 2) {
                    throw new IllegalArgumentException("Invalid Replace entry: " + content);
                }
                float pct = Float.parseFloat(replaceParts[0]);
                startRange += pct;
                slotPercentages.put(startRange, replaceParts[1]);
            } else if (key.equalsIgnoreCase("Pair")) {
                String[] pairParts = value.split("\\|", 2);
                if (pairParts.length != 2 || pairParts[0].trim().isEmpty() || pairParts[1].trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Pair entries must contain exactly two sheet expressions separated by '|': " + content);
                }
                pairedSheets.add(Pair.of(pairParts[0].trim(), pairParts[1].trim()));
            }
        }
    }

    public boolean hasPairs() {
        return !pairedSheets.isEmpty();
    }

    public Pair<String, String> getRandomPair() {
        if (pairedSheets.isEmpty()) {
            throw new IllegalStateException("Booster slot '" + slotName + "' has no configured pairs");
        }
        return pairedSheets.get(MyRandom.getRandom().nextInt(pairedSheets.size()));
    }

    public String replaceSlot() {
        float rand = MyRandom.getRandom().nextFloat();
        for (Float key : slotPercentages.keySet()) {
            if (rand < key) {
                System.out.println("Replaced a base slot! " + slotName + " -> " + slotPercentages.get(key));
                return slotPercentages.get(key);
            }
        }

        return baseRarity;
    }
}
