package com.protolambda.blocktopograph.map.marker;


import com.protolambda.blocktopograph.util.NamedBitmapProvider;
import java.awt.image.BufferedImage;

public class CustomNamedBitmapProvider implements NamedBitmapProvider {

    private final NamedBitmapProvider inner;

    private final String displayName;

    public CustomNamedBitmapProvider(NamedBitmapProvider inner, String displayName){
        this.inner = inner;
        this.displayName = displayName;
    }

    @Override
    public BufferedImage getBitmap() {
        return inner.getBitmap();
    }

    @Override
    public String getBitmapDisplayName() {
        return displayName;
    }
    
    @Override
    public String getBitmapDataName() {
        return inner.getBitmapDataName();
    }
}
