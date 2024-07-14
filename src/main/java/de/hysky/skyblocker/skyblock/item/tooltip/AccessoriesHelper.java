package de.hysky.skyblocker.skyblock.item.tooltip;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.util.UndashedUuid;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AccessoriesHelper {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("collected_accessories.json");
	private static final Pattern BAG_TITLE = Pattern.compile("Accessory Bag(?: \\((?<page>\\d+)\\/\\d+\\))?");
	//UUID -> Profile Id & Data
	private static final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, ProfileAccessoryData>> COLL_ACCESSORIES = new Object2ObjectOpenHashMap<>();
	private static final Predicate<String> NON_EMPTY = s -> !s.isEmpty();
	private static final Predicate<Accessory> HAS_FAM = Accessory::hasFamily;
	private static final ToIntFunction<Accessory> ACC_TIER = Accessory::tier;

	private static Map<String, Accessory> ACC_DATA = new Object2ObjectOpenHashMap<>();
	//remove??
	private static CompletableFuture<Void> loaded;

	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register((_client) -> load());
		ClientLifecycleEvents.CLIENT_STOPPING.register((_client) -> save());
		ScreenEvents.BEFORE_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			if (Utils.isOnSkyblock() && TooltipInfoType.ACCESSORIES.isTooltipEnabled() && !Utils.getProfileId().isEmpty() && screen instanceof GenericContainerScreen genericContainerScreen) {
				Matcher matcher = BAG_TITLE.matcher(genericContainerScreen.getTitle().getString());

				if (matcher.matches()) {
					ScreenEvents.afterTick(screen).register(_screen -> {
						GenericContainerScreenHandler handler = genericContainerScreen.getScreenHandler();
						int page = matcher.group("page") != null ? Integer.parseInt(matcher.group("page")) : 1;

						collectAccessories(handler.slots.subList(0, handler.getRows() * 9), page);
					});
				}
			}
		});
	}

	//Note: JsonOps.COMPRESSED must be used if you're using maps with non-string keys
	private static void load() {
		loaded = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(FILE)) {
				COLL_ACCESSORIES.putAll(ProfileAccessoryData.SERIALIZATION_CODEC.parse(JsonOps.COMPRESSED, JsonParser.parseReader(reader)).getOrThrow());
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Accessory Helper] Failed to load accessory file!", e);
			}
		});
	}

	private static void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
			SkyblockerMod.GSON.toJson(ProfileAccessoryData.SERIALIZATION_CODEC.encodeStart(JsonOps.COMPRESSED, COLL_ACCESSORIES).getOrThrow(), writer);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Accessory Helper] Failed to save accessory file!", e);
		}
	}

	private static void collectAccessories(List<Slot> slots, int page) {
		//Is this even needed?
		if (!loaded.isDone()) return;

		List<String> accessoryIds = slots.stream()
				.map(Slot::getStack)
				.map(ItemUtils::getItemId)
				.filter(NON_EMPTY)
				.toList();

		String uuid = UndashedUuid.toString(MinecraftClient.getInstance().getSession().getUuidOrNull());

		COLL_ACCESSORIES.computeIfAbsent(uuid, _uuid -> new Object2ObjectOpenHashMap<>()).computeIfAbsent(Utils.getProfileId(), profileId -> ProfileAccessoryData.createDefault()).pages()
				.put(page, new ObjectOpenHashSet<>(accessoryIds));
	}
	public static Pair<AccessoryReport, String> getReport(String accId) {
		if (!ACC_DATA.containsKey(accId) || Utils.getProfileId().isEmpty()) return Pair.of(AccessoryReport.INELIGIBLE, null);

		Accessory acc = ACC_DATA.get(accId);
		String uuid = UndashedUuid.toString(MinecraftClient.getInstance().getSession().getUuidOrNull());
		Set<Accessory> collAcc = COLL_ACCESSORIES.computeIfAbsent(uuid, _uuid -> new Object2ObjectOpenHashMap<>()).computeIfAbsent(Utils.getProfileId(), profileId -> ProfileAccessoryData.createDefault()).pages().values().stream()
				.flatMap(ObjectOpenHashSet::stream)
				.filter(ACC_DATA::containsKey)
				.map(ACC_DATA::get)
				.collect(Collectors.toSet());

		// If the accessory doesn't belong to a family
		if (acc.family().isEmpty()) {
			//If the player has this accessory or player doesn't have this accessory
			return collAcc.contains(acc) ? Pair.of(AccessoryReport.HAS_HIGHEST_TIER, null) : Pair.of(AccessoryReport.MISSING, "");
		}

		Predicate<Accessory> HAS_SAME_FAM = acc::hasSameFamily;
		Set<Accessory> collSameFam = collAcc.stream()
				.filter(HAS_FAM)
				.filter(HAS_SAME_FAM)
				.collect(Collectors.toSet());

		Set<Accessory> sameFam = ACC_DATA.values().stream()
				.filter(HAS_FAM)
				.filter(HAS_SAME_FAM)
				.collect(Collectors.toSet());

		int highestTierFam = sameFam.stream()
				.mapToInt(ACC_TIER)
				.max()
				.orElse(0);

		//If the player hasn't collected any accessory in same family
		if (collSameFam.isEmpty()) return Pair.of(AccessoryReport.MISSING, String.format("(%d/%d)", acc.tier(), highestTierFam));

		int highestTierColl = collSameFam.stream()
				.mapToInt(ACC_TIER)
				.max()
				.getAsInt();

		//If this accessory is the highest tier, and the player has the highest tier accessory in this family
		//This accounts for multiple accessories with the highest tier
		if (acc.tier() == highestTierFam && highestTierColl == highestTierFam) return Pair.of(AccessoryReport.HAS_HIGHEST_TIER, null);

		//If this accessory is a higher tier than all the other collected accessories in the same family
		if (acc.tier() > highestTierColl) return Pair.of(AccessoryReport.IS_GREATER_TIER, String.format("(%d→%d/%d)", highestTierColl, acc.tier(), highestTierFam));

		//If this accessory is a lower tier than one already obtained from same family
		if (acc.tier() < highestTierColl) return Pair.of(AccessoryReport.OWNS_BETTER_TIER, String.format("(%d→%d/%d)", highestTierColl, acc.tier(), highestTierFam));

		//If there is an accessory in the same family that has a higher tier
		if (acc.tier() < highestTierFam) return Pair.of(AccessoryReport.HAS_GREATER_TIER, String.format("(%d/%d)", highestTierColl, highestTierFam));

		return Pair.of(AccessoryReport.MISSING, String.format("(%d/%d)", acc.tier(), highestTierFam));
	}


	static void refreshData(JsonObject data) {
		try {
			ACC_DATA = Accessory.MAP_CODEC.parse(JsonOps.INSTANCE, data).getOrThrow();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Accessory Helper] Failed to parse data!", e);
		}
	}

	private record ProfileAccessoryData(Int2ObjectOpenHashMap<ObjectOpenHashSet<String>> pages) {
		private static final Codec<ProfileAccessoryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Codec.INT, Codec.STRING.listOf().xmap(ObjectOpenHashSet::new, ObjectArrayList::new))
						.xmap(Int2ObjectOpenHashMap::new, Int2ObjectOpenHashMap::new).fieldOf("pages").forGetter(ProfileAccessoryData::pages)
		).apply(instance, ProfileAccessoryData::new));
		private static final Codec<Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, ProfileAccessoryData>>> SERIALIZATION_CODEC = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, CODEC)
				.xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new)
		).xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new);

		private static ProfileAccessoryData createDefault() {
			return new ProfileAccessoryData(new Int2ObjectOpenHashMap<>());
		}
	}

	/**
	 * @author AzureAaron
	 * @implSpec <a href="https://github.com/AzureAaron/aaron-mod/blob/1.20/src/main/java/net/azureaaron/mod/commands/MagicalPowerCommand.java#L475">Aaron's Mod</a>
	 */
	private record Accessory(String id, Optional<String> family, int tier) {
		private static final Codec<Accessory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(Accessory::id),
				Codec.STRING.optionalFieldOf("family").forGetter(Accessory::family),
				Codec.INT.optionalFieldOf("tier", 0).forGetter(Accessory::tier)
		).apply(instance, Accessory::new));
		private static final Codec<Map<String, Accessory>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);

		private boolean hasFamily() {
			return family.isPresent();
		}

		private boolean hasSameFamily(Accessory other) {
			return other.family().equals(this.family);
		}
	}

	public enum AccessoryReport {
		HAS_HIGHEST_TIER, //You've collected the highest tier - Collected
		IS_GREATER_TIER, //This accessory is an upgrade from the one in the same family that you already have - Upgrade -- Shows you what tier this accessory is in its family
		HAS_GREATER_TIER, //This accessory has a higher tier upgrade - Upgradable -- Shows you the highest tier accessory you've collected in that family
		OWNS_BETTER_TIER, //You've collected an accessory in this family with a higher tier - Downgrade -- Shows you the highest tier accessory you've collected in that family
		MISSING, //You don't have any accessories in this family - Missing
		INELIGIBLE
	}
}
