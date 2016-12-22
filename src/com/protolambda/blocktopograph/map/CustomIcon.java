package com.protolambda.blocktopograph.map;

import com.protolambda.blocktopograph.util.NamedBitmapProvider;
import com.protolambda.blocktopograph.util.NamedBitmapProviderHandle;
import com.protolambda.blocktopograph.util.UV;
import com.protolambda.blocktopograph.util.io.ImageUtil;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.util.HashMap;

/**
 * CustomIcon provides an easy collection of special icons to use for markers.
 */
public enum CustomIcon implements NamedBitmapProviderHandle, NamedBitmapProvider {

    DEFAULT_MARKER("default_marker", UV.ab(0, 0, 32, 32)),
    BLUE_MARKER("blue_marker", UV.ab(0, 32, 32, 64)),
    GREEN_MARKER("green_marker", UV.ab(32, 0, 64, 32)),
    RED_MARKER("red_marker", UV.ab(32, 32, 64, 64)),
    AQUA_MARKER("aqua_marker", UV.ab(64, 0, 96, 32)),
    ORANGE_MARKER("orange_marker", UV.ab(64, 32, 96, 64)),
    YELLOW_MARKER("yellow_marker", UV.ab(96, 0, 128, 32)),
    PURPLE_MARKER("purple_marker", UV.ab(96, 32, 128, 64)),
    SPAWN_MARKER("spawn_marker", UV.ab(64, 64, 128, 128));

    public final String iconName;
    public final UV uv;

    public BufferedImage bitmap;

    CustomIcon(String iconName, UV uv) {
        this.iconName = iconName;
        this.uv = uv;
    }

    @Override
    public BufferedImage getBitmap() {
        return this.bitmap;
    }

    @Override
    public NamedBitmapProvider getNamedBitmapProvider() {
        return this;
    }

    @Override
    public String getBitmapDisplayName() {
        return this.iconName;
    }

    @Override
    public String getBitmapDataName() {
        return this.iconName;
    }

    public static void loadCustomBitmaps() throws IOException {
        //Bitmap sheet = BitmapFactory.decodeStream(assetManager.open("custom_icons.png"));
        BufferedImage sheet = ImageUtil.readImage("custom_icons.png");

        for (CustomIcon icon : CustomIcon.values()) {
            if (icon.bitmap == null && icon.uv != null) {
                icon.bitmap = sheet.getSubimage(
                        icon.uv.uX, icon.uv.uY,
                        icon.uv.vX - icon.uv.uX, icon.uv.vY - icon.uv.uY
                );
                //Bitmap.createBitmap(sheet,
                //        icon.uv.uX, icon.uv.uY,
                //        icon.uv.vX - icon.uv.uX, icon.uv.vY - icon.uv.uY,
                //        null, false);
            }
        }
    }

    private static HashMap<String, CustomIcon> iconsByName;

    static {
        iconsByName = new HashMap<>();

        for (CustomIcon icon : CustomIcon.values()) {
            iconsByName.put(icon.iconName, icon);
        }
    }

    public static CustomIcon getCustomIcon(String iconName) {
        return iconsByName.get(iconName);
    }

}
