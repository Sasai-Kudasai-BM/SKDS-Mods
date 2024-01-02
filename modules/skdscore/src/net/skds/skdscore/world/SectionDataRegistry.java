package net.skds.skdscore.world;

import lombok.Getter;
import net.skds.skdscore.utils.WorldSide;

import java.util.*;
import java.util.function.Function;

public class SectionDataRegistry {

	@Getter
	private static final List<SectionDataRegistryEntry<?>> dataRegistryEntries = new ArrayList<>();
	@Getter
	private static final Map<String, SectionDataRegistryEntry<?>> dataRegistryEntryMap = new HashMap<>();

	private static boolean finished = false;

	public static <T extends ChunkSectionData> SectionDataRegistryEntry<T> regSectionData(Function<SectionDataHolder, T> constructor, WorldSide side, String name) {
		if (finished) {
			throw new IllegalStateException("Registration must be done before FMLCommonSetupEvent");
		}
		SectionDataRegistryEntry<T> entry = new SectionDataRegistryEntry<>(constructor, side, name, dataRegistryEntries.size());
		dataRegistryEntries.add(entry);
		dataRegistryEntryMap.put(name, entry);

		return entry;
	}
	
	public static void finish() {
		finished = true;
		dataRegistryEntries.sort(Comparator.comparing(c -> c.id));
	}

}
