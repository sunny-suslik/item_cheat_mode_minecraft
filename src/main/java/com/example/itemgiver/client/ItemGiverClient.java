package com.example.itemgiver.client;

import com.example.itemgiver.ItemGiverMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemGiverClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("itemgiver-client");

    public static final net.minecraft.client.option.KeyBinding OPEN_GUI_KEY = KeyBindingHelper.registerKeyBinding(
            new net.minecraft.client.option.KeyBinding(
                    "key.itemgiver.open_gui",
                    GLFW.GLFW_KEY_G,
                    "category.itemgiver"
            )
    );

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playC2S().register(ItemGiverMod.GiveItemPayload.PACKET_ID, ItemGiverMod.GIVE_ITEM_CODEC);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_GUI_KEY.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new ItemGiveScreen());
            }
        });
    }
}
