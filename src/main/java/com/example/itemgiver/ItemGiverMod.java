package com.example.itemgiver;

import com.example.itemgiver.client.ItemGiveScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemGiverMod implements ModInitializer {
    public static final String MOD_ID = "itemgiver";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier GIVE_ITEM_PACKET_ID = Identifier.of(MOD_ID, "give_item");
    public static final PacketCodec<RegistryByteBuf, GiveItemPayload> GIVE_ITEM_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, GiveItemPayload::itemId,
            PacketCodecs.INTEGER, GiveItemPayload::count,
            PacketCodecs.STRING, GiveItemPayload::nbtJson,
            GiveItemPayload::new
    );

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(GiveItemPayload.PACKET_ID, GIVE_ITEM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(GiveItemPayload.PACKET_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            String itemId = payload.itemId();
            int count = payload.count();
            String nbtJson = payload.nbtJson();

            Identifier itemIdentifier = Identifier.tryParse(itemId);
            if (itemIdentifier == null) {
                // Try prefixing with minecraft:
                itemIdentifier = Identifier.tryParse("minecraft:" + itemId);
            }

            if (itemIdentifier == null || !Registries.ITEM.containsId(itemIdentifier)) {
                LOGGER.warn("Invalid item ID received: {}", itemId);
                return;
            }

            ItemStack stack = new ItemStack(Registries.ITEM.get(itemIdentifier), count);

            if (!nbtJson.equals("{}") && !nbtJson.isEmpty()) {
                try {
                    var nbt = StringNbtReader.parse(nbtJson);
                    stack.setNbt(nbt);
                } catch (Exception e) {
                    LOGGER.warn("Failed to parse NBT JSON: {}", nbtJson, e);
                }
            }

            if (!player.giveItemStack(stack)) {
                player.dropItem(stack, false);
            }
        });
    }

    public record GiveItemPayload(String itemId, int count, String nbtJson) implements CustomPayload {
        public static final CustomPayload.Id<GiveItemPayload> PACKET_ID = new CustomPayload.Id<>(GIVE_ITEM_PACKET_ID);

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }
}
