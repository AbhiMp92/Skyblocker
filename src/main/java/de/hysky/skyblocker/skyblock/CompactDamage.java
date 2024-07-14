package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.CustomArmorAnimatedDyes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.regex.Pattern;

public class CompactDamage {
	private static final Pattern DAMAGE_PATTERN = Pattern.compile("[✧✯]?[\\d,]+[✧✯]?❤?");

	private CompactDamage() {
	}

	public static void compactDamage(ArmorStandEntity entity) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.compactDamage.enabled) return;
		if (!isValidArmorStand(entity)) return;

		Text customName = entity.getCustomName();
		String customNameStringified = customName.getString();

		if (!DAMAGE_PATTERN.matcher(customNameStringified).matches()) return;

		List<Text> siblings = customName.getSiblings();
		if (siblings.isEmpty()) return;

		MutableText prettierCustomName = prettifyCustomName(customName, customNameStringified, siblings);

		entity.setCustomName(prettierCustomName);
	}

	private static boolean isValidArmorStand(ArmorStandEntity entity) {
		return entity.isInvisible() && entity.hasCustomName() && entity.isCustomNameVisible();
	}

	private static MutableText prettifyCustomName(Text customName, String customNameStringified, List<Text> siblings) {
		if (siblings.size() == 1) {
			return prettifyNormalDamage(customName);
		} else {
			return prettifyCritDamage(customName, customNameStringified, siblings);
		}
	}

	private static MutableText prettifyNormalDamage(Text customName) {
		Text text = customName.getSiblings().get(0);
		String dmg = text.getString().replace(",", "");
		if (!NumberUtils.isParsable(dmg)) return null;

		String prettifiedDmg = prettifyDamageNumber(Long.parseLong(dmg));
		int color = getColorFromText(text);

		return Text.literal("").append(Text.literal(prettifiedDmg).setStyle(customName.getStyle()).withColor(color));
	}

	private static MutableText prettifyCritDamage(Text customName, String customNameStringified, List<Text> siblings) {
		boolean wasDoubled = customNameStringified.contains("❤");
		int entriesToRemove = wasDoubled ? 2 : 1;

		String dmg = siblings.subList(1, siblings.size() - entriesToRemove)
				.stream()
				.map(Text::getString)
				.reduce("", String::concat)
				.replace(",", "");

		if (!NumberUtils.isParsable(dmg)) return null;

		String dmgSymbol = customNameStringified.charAt(0) != '✯' ? "✧" : "✯";
		String prettifiedDmg = dmgSymbol + prettifyDamageNumber(Long.parseLong(dmg)) + dmgSymbol;

		MutableText prettierCustomName = Text.literal("");
		int length = prettifiedDmg.length();

		for (int i = 0; i < length; i++) {
			prettierCustomName.append(Text.literal(prettifiedDmg.substring(i, i + 1)).withColor(
					CustomArmorAnimatedDyes.interpolate(
							SkyblockerConfigManager.get().uiAndVisuals.compactDamage.critDamageGradientStart.getRGB() & 0x00FFFFFF,
							SkyblockerConfigManager.get().uiAndVisuals.compactDamage.critDamageGradientEnd.getRGB() & 0x00FFFFFF,
							i / (length - 1.0)
					)
			));
		}

		if (wasDoubled) {
			prettierCustomName.append(Text.literal("❤").formatted(Formatting.LIGHT_PURPLE));
		}

		prettierCustomName.setStyle(customName.getStyle());
		return prettierCustomName;
	}

	private static int getColorFromText(Text text) {
		if (text.getStyle().getColor() != null) {
			if (text.getStyle().getColor() == TextColor.fromFormatting(Formatting.GRAY)) {
				return SkyblockerConfigManager.get().uiAndVisuals.compactDamage.normalDamageColor.getRGB() & 0x00FFFFFF;
			} else {
				return text.getStyle().getColor().getRgb();
			}
		} else {
			return SkyblockerConfigManager.get().uiAndVisuals.compactDamage.normalDamageColor.getRGB() & 0x00FFFFFF;
		}
	}

	private static String prettifyDamageNumber(long damage) {
		if (damage < 1_000) return String.valueOf(damage);
		if (damage < 1_000_000) return format(damage / 1_000.0) + "k";
		if (damage < 1_000_000_000) return format(damage / 1_000_000.0) + "M";
		if (damage < 1_000_000_000_000L) return format(damage / 1_000_000_000.0) + "B";
		return format(damage / 1_000_000_000_000.0) + "T"; //This will probably never be reached
	}

	private static String format(double number) {
		return ("%." + SkyblockerConfigManager.get().uiAndVisuals.compactDamage.precision + "f").formatted(number);
	}
}
