package me.ionar.salhack.util.imgs;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

public class SalDynamicTexture extends DynamicTexture {
    private final int Height;
    private final int Width;
    private final BufferedImage m_BufferedImage;
    private ResourceLocation m_TexturedLocation;
    private ImageFrame m_Frame;

    public SalDynamicTexture(BufferedImage bufferedImage, int height1, int width1) {
        super(bufferedImage);

        m_Frame = null;
        m_BufferedImage = bufferedImage;

        Height = height1;
        Width = width1;
    }

    public int GetHeight() {
        return Height;
    }

    public int GetWidth() {
        return Width;
    }

    public final DynamicTexture GetDynamicTexture() {
        return this;
    }

    public final BufferedImage GetBufferedImage() {
        return m_BufferedImage;
    }

    public void SetResourceLocation(ResourceLocation dynamicTextureLocation) {
        m_TexturedLocation = dynamicTextureLocation;
    }

    public final ResourceLocation GetResourceLocation() {
        return m_TexturedLocation;
    }

    public void SetImageFrame(final ImageFrame frame) {
        m_Frame = frame;
    }

    /// used for gifs
    public final ImageFrame GetFrame() {
        return m_Frame;
    }
}
