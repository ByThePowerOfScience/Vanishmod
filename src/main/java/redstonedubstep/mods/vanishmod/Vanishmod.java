package redstonedubstep.mods.vanishmod;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Vanishmod.MODID)
public class Vanishmod {
	public static final String MODID = "vmod"; //This is Vanishmod v1.1.17.1 for 1.20.4!
	public static boolean mc2discordDetected = false;

	public Vanishmod() {
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC, "vanishmod-server.toml");
	}

	public void registerCommands(RegisterCommandsEvent event) {
		VanishCommand.register(event.getDispatcher());
	}
}
