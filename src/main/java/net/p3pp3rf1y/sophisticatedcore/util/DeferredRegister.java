package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Collects Fabric registry entries until the mod initializer registers them. */
public final class DeferredRegister<T> {
	private final Registry<T> registry;
	private final String namespace;
	private final List<RegistrySupplier<? extends T>> entries = new ArrayList<>();
	private boolean registered;

	private DeferredRegister(Registry<T> registry, String namespace) {
		this.registry = registry;
		this.namespace = namespace;
	}

	public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
		return new DeferredRegister<>(registry, namespace);
	}

	public <I extends T> RegistrySupplier<I> register(String name, Supplier<? extends I> factory) {
		@SuppressWarnings({"unchecked", "rawtypes"})
		ResourceKey<I> key = (ResourceKey) ResourceKey.create(registry.key(), Identifier.fromNamespaceAndPath(namespace, name));
		RegistrySupplier<I> entry = new RegistrySupplier<>(key, factory);
		entries.add(entry);
		return entry;
	}

	public void register() {
		if (registered) {
			return;
		}
		registered = true;
		for (RegistrySupplier<? extends T> entry : entries) {
			register(entry);
		}
	}

	private <I extends T> void register(RegistrySupplier<I> entry) {
		Registry.register(registry, entry.getKey().identifier(), entry.create());
	}
}
