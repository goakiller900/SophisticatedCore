package net.p3pp3rf1y.sophisticatedcore.event.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Environment(EnvType.CLIENT)
public interface ClientRecipesUpdated {
	Event<ClientRecipesUpdated> EVENT = EventFactory.createArrayBacked(ClientRecipesUpdated.class, callbacks -> () -> {
		for(ClientRecipesUpdated event : callbacks)
			event.onRecipesUpdated();
	});

	void onRecipesUpdated();
}
