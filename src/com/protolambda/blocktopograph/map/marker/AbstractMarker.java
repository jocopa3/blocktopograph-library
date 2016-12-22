package com.protolambda.blocktopograph.map.marker;

import com.protolambda.blocktopograph.map.Dimension;
import com.protolambda.blocktopograph.util.NamedBitmapProvider;
import com.protolambda.blocktopograph.util.NamedBitmapProviderHandle;
import com.protolambda.blocktopograph.util.math.Vector3;
import java.awt.image.BufferedImage;

public class AbstractMarker implements NamedBitmapProviderHandle {

    public final int x, y, z;
    public final Dimension dimension;

    public final NamedBitmapProvider namedBitmapProvider;

    public final boolean isCustom;

    public AbstractMarker(int x, int y, int z, Dimension dimension, NamedBitmapProvider namedBitmapProvider, boolean isCustom) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.namedBitmapProvider = namedBitmapProvider;
        this.isCustom = isCustom;
    }

    public int getChunkX(){
        return x >> 4;
    }

    public int getChunkZ(){
        return z >> 4;
    }

    /**
     * Loads the provided bitmap into the image view.
     * @param iconView The view to load the icon into.
     */
    //public void loadIcon(BufferedImage iconView) {
    //    iconView = this.getNamedBitmapProvider().getBitmap();
    //}

    @Override
    public NamedBitmapProvider getNamedBitmapProvider() {
        return namedBitmapProvider;
    }

    public AbstractMarker copy(int x, int y, int z, Dimension dimension) {
        return new AbstractMarker(x, y, z, dimension, this.namedBitmapProvider, this.isCustom);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractMarker that = (AbstractMarker) o;

        return x == that.x
            && y == that.y
            && z == that.z
            && dimension == that.dimension
            && (namedBitmapProvider != null
                ? namedBitmapProvider.equals(that.namedBitmapProvider)
                : that.namedBitmapProvider == null);

    }

    @Override
    public int hashCode() {
        int result = Vector3.intHash(x, y, z);
        result = 31 * result + (dimension != null ? dimension.hashCode() : 0);
        result = 31 * result + (namedBitmapProvider != null ? namedBitmapProvider.hashCode() : 0);
        return result;
    }
}
