package com.zik9k.client;

public final class BlockOverlayModule extends Module {
    private int alpha = 70;
    private int outlineAlpha = 190;

    public BlockOverlayModule() {
        super("Block Overlay", "Highlights the block under your crosshair", ModuleCategory.RENDER);
        alpha = ClientConfig.blockOverlayAlpha();
        outlineAlpha = ClientConfig.blockOverlayOutlineAlpha();
    }

    public int alpha() { return alpha; }
    public int outlineAlpha() { return outlineAlpha; }

    public void setAlpha(int value) {
        alpha = Math.max(10, Math.min(180, value));
        ClientConfig.setBlockOverlayAlpha(alpha);
    }

    public void setOutlineAlpha(int value) {
        outlineAlpha = Math.max(40, Math.min(255, value));
        ClientConfig.setBlockOverlayOutlineAlpha(outlineAlpha);
    }
}
