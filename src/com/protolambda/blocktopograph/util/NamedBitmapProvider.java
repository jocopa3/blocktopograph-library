package com.protolambda.blocktopograph.util;

import java.awt.image.BufferedImage;

public interface NamedBitmapProvider {

    BufferedImage getBitmap();

    String getBitmapDisplayName();

    String getBitmapDataName();

}
