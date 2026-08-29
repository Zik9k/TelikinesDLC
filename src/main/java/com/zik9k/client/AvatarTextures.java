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
        "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"
    };

    private static final boolean[] LOADED = new boolean[DATA.length];

    private AvatarTextures() {
    }

    public static int count() {
        return DATA.length;
    }

    public static String getName(int index) {
        return NAMES[Math.floorMod(index, NAMES.length)];
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
        }
    }
}
