package com.example.itemgiver.client;

import com.example.itemgiver.ItemGiverMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ItemGiveScreen extends Screen {
    private TextFieldWidget itemIdField;
    private TextFieldWidget countField;
    private TextFieldWidget nbtField;
    private ButtonWidget giveButton;

    public ItemGiveScreen() {
        super(Text.translatable("screen.itemgiver.title"));
    }

    @Override
    protected void init() {
        int textFieldWidth = 200;
        int centerX = width / 2 - textFieldWidth / 2;

        // Item ID field
        itemIdField = new TextFieldWidget(textRenderer, centerX, height / 2 - 60, textFieldWidth, 20, Text.literal("Item ID"));
        itemIdField.setMaxLength(256);
        itemIdField.setText("minecraft:stone");
        addDrawableChild(itemIdField);

        // Count field
        countField = new TextFieldWidget(textRenderer, centerX, height / 2 - 30, textFieldWidth, 20, Text.literal("Count"));
        countField.setMaxLength(3);
        countField.setText("1");
        countField.setPredicate(s -> {
            if (s.isEmpty()) return true;
            try {
                int count = Integer.parseInt(s);
                return count >= 1 && count <= 64;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        addDrawableChild(countField);

        // NBT field
        nbtField = new TextFieldWidget(textRenderer, centerX, height / 2, textFieldWidth, 20, Text.literal("NBT"));
        nbtField.setMaxLength(4096);
        nbtField.setText("{}");
        addDrawableChild(nbtField);

        // Give button
        giveButton = ButtonWidget.builder(Text.translatable("button.itemgiver.give"), button -> sendGivePacket())
                .dimensions(centerX, height / 2 + 30, textFieldWidth, 20)
                .build();
        addDrawableChild(giveButton);

        setInitialFocus(itemIdField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(textRenderer, title.getString(), width / 2 - textRenderer.getWidth(title.getString()) / 2, height / 2 - 90, 0xFFFFFF);
        
        context.drawTextWithShadow(textRenderer, Text.translatable("label.itemgiver.item_id"), itemIdField.getX(), itemIdField.getY() - 10, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.translatable("label.itemgiver.count"), countField.getX(), countField.getY() - 10, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.translatable("label.itemgiver.nbt"), nbtField.getX(), nbtField.getY() - 10, 0xFFFFFF);
    }

    private void sendGivePacket() {
        String itemId = itemIdField.getText().trim();
        if (itemId.isEmpty()) {
            itemId = "minecraft:stone";
        } else if (!itemId.contains(":")) {
            itemId = "minecraft:" + itemId;
        }

        int count;
        try {
            count = Integer.parseInt(countField.getText());
            if (count < 1) count = 1;
            if (count > 64) count = 64;
        } catch (NumberFormatException e) {
            count = 1;
        }

        String nbtJson = nbtField.getText().trim();
        if (nbtJson.isEmpty()) {
            nbtJson = "{}";
        }

        ClientPlayNetworking.send(new ItemGiverMod.GiveItemPayload(itemId, count, nbtJson));
        close();
    }
}
