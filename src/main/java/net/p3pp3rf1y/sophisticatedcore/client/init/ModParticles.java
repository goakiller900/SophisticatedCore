package net.p3pp3rf1y.sophisticatedcore.client.init;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.JukeboxUpgradeNoteParticle;

public class ModParticles {
	private ModParticles() {}

	public static void registerFactories() {
		ParticleProviderRegistry.getInstance().register(net.p3pp3rf1y.sophisticatedcore.init.ModParticles.JUKEBOX_NOTE.get(), JukeboxUpgradeNoteParticle.Factory::new);
	}
}
