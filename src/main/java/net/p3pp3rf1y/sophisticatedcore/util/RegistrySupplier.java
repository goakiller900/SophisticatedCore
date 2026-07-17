package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.resources.ResourceKey;

import java.util.Objects;
import java.util.function.Supplier;

public final class RegistrySupplier<T> implements Supplier<T> {
	private final ResourceKey<T> key;
	private final Supplier<? extends T> factory;
	private T value;

	RegistrySupplier(ResourceKey<T> key, Supplier<? extends T> factory) {
		this.key = key;
		this.factory = factory;
	}

	T create() {
		value = factory.get();
		return value;
	}

	public ResourceKey<T> getKey() {
		return key;
	}

	@Override
	public T get() {
		return Objects.requireNonNull(value, () -> "Registry entry " + key.identifier() + " was accessed before registration");
	}
}
