package com.clovercard.cloversellitemtoserver;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CloverSellItemToServer.MODID)
@Mod.EventBusSubscriber(modid = CloverSellItemToServer.MODID)
public class CloverSellItemToServer {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "cloversellitemtoserver";

    public CloverSellItemToServer() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        new SellToServerCommand(event.getDispatcher());
        new GetItemValueCommand(event.getDispatcher());
    }
}
