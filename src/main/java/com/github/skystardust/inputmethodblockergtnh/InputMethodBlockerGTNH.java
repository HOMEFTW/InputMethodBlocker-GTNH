package com.github.skystardust.inputmethodblockergtnh;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = InputMethodBlockerGTNH.MODID,
    version = InputMethodBlockerGTNH.VERSION,
    name = InputMethodBlockerGTNH.MOD_NAME,
    acceptedMinecraftVersions = "[1.7.10]")
public class InputMethodBlockerGTNH {

    public static final String MODID = "inputmethodblockergtnh";
    public static final String MOD_NAME = "InputMethodBlocker-GTNH";
    public static final String VERSION = Tags.VERSION;

    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.github.skystardust.inputmethodblockergtnh.ClientProxy",
        serverSide = "com.github.skystardust.inputmethodblockergtnh.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
