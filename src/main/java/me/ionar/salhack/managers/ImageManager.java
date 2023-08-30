package me.ionar.salhack.managers;

import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.util.imgs.SalDynamicTexture;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ImageManager {
    public NavigableMap<String, SalDynamicTexture> Pictures = new TreeMap<String, SalDynamicTexture>();

    public ImageManager() {
        //LoadImages();

    }

    public static ImageManager Get() {
        return SalHack.GetImageManager();
    }

    public void Load() {
        LoadImage("OutlinedEllipse");
        LoadImage("Arrow");
        LoadImage("blockimg");
        LoadImage("BlueBlur");
        LoadImage("Eye");
        LoadImage("mouse");
        LoadImage("questionmark");
        LoadImage("robotimg");
        LoadImage("SalHackWatermark");
        LoadImage("Shield");
        LoadImage("skull");
        LoadImage("particles");
        LoadImage("Title");
    }

    public void LoadImage(String img) {
        BufferedImage image = null;

        InputStream stream = ImageManager.class.getResourceAsStream("/assets/salhack/imgs/" + img + ".png");

        try {
            image = ImageIO.read(stream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (image == null) {
            System.out.println("Couldn't load image: " + img);
            return;
        }

        int height = image.getHeight();
        int width = image.getWidth();

        final SalDynamicTexture texture = new SalDynamicTexture(image, height, width);
        if (texture != null) {
            texture.SetResourceLocation(Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation("salhack/textures", texture));

            Pictures.put(img, texture);

            System.out.println("Loaded Img: " + img);
        }
    }

    public SalDynamicTexture GetDynamicTexture(String image) {
        if (Pictures.containsKey(image))
            return Pictures.get(image);

        return null;
    }

    public String GetNextImage(String value, boolean recursive) {
        String string = null;

        for (Map.Entry<String, SalDynamicTexture> itr : Pictures.entrySet()) {
            if (!itr.getKey().equalsIgnoreCase(value))
                continue;

            if (recursive) {
                string = Pictures.lowerKey(itr.getKey());

                if (string == null)
                    return Pictures.lastKey();
            } else {
                string = Pictures.higherKey(itr.getKey());

                if (string == null)
                    return Pictures.firstKey();
            }

            return string;
        }

        return string;
    }

}
