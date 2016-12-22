package com.protolambda.blocktopograph.map.marker;

/**
 * TODO docs
 */
public class MarkerImageView {

    private final AbstractMarker markerHook;

    public MarkerImageView(AbstractMarker markerHook) {
        this.markerHook = markerHook;
    }

    public AbstractMarker getMarkerHook(){
        return markerHook;
    }

}
