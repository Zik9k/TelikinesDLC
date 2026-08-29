package com.zik9k.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Base64;

public final class AvatarTextures {
    private static final String[] NAMES = {"Dog", "Green", "Squirrel", "Chef"};
    private static final String[] DATA = {
        "REPLACE_DOG",
        "REPLACE_GREEN",
        "REPLACE_SQUIRREL",
        "REPLACE_CHEF"
    };

    private static final boolean[] LOADED = new boolean[DATA.length];

    private AvatarTextures() {
    }

    public static String[] getNames() {
        return NAMES.clone();
    }

    public static void draw(DrawContext context, MinecraftClient client, int index, int x, int y, int size) {
        int safeIndex = Math.floorMod(index, DATA.length);
        Identifier textureId = Identifier.of("zik9k-client", "avatar_" + safeIndex);
        TextureManager textureManager = client.getTextureManager();

        if (!LOADED[safeIndex]) {
            register(textureManager, textureId, safeIndex);
        }

        context.drawTexture(RenderPipelines.GUI_TEXTURED, textureId, x, y, 0.0f, 0.0f, size, size, 64, 64);
    }

    private static void register(TextureManager textureManager, Identifier id, int index) {
        try {
            byte[] bytes = Base64.getDecoder().decode(DATA[index]);
            NativeImage image = NativeImage.read(bytes);
            final int safeIndex = index;
            textureManager.registerTexture(id, new NativeImageBackedTexture(
                    () -> "TelikinesDLC avatar " + safeIndex,
                    image
            ));
            LOADED[safeIndex] = true;
        } catch (IOException | IllegalArgumentException exception) {
            // Keep the GUI usable even if an embedded avatar cannot be decoded.
        }
    }
}
