/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protolambda.blocktopograph.util.io;

import com.protolambda.blocktopograph.Log;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Matt
 */
public class ImageUtil {

    private static final ImageUtil SINGLETON = new ImageUtil();

    private BufferedImage readImageInternal(String path) {
        BufferedImage image = null;

        try {
            System.out.println(getClass().getResource(path));
            image = ImageIO.read(getClass().getResourceAsStream(path));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return image;
    }

    public static BufferedImage readImage(String path) {
        // All paths should point to the assets folder
        if (!path.startsWith("/assets/")) {
            path = "/assets/" + path;
        }

        return SINGLETON.readImageInternal(path);
    }

    public static BufferedImage scaleImage(BufferedImage srcImg, int w, int h) {
        BufferedImage scaledImage = new BufferedImage(w, h, srcImg.getType());
        Graphics2D g2d = scaledImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(srcImg, 0, 0, w, h, null);

        g2d.dispose();

        return scaledImage;
    }
    
    public static byte[] asBytes(BufferedImage srcImg) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(srcImg, "png", baos);
            baos.flush();
            byte[] imgArray = baos.toByteArray();
            baos.close();
            return imgArray;
        } catch (IOException e) {
            return null;
        }
    }
}
