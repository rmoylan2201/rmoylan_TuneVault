package com.example.tunevaultfx.profile.media;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Swing-backed 90° rotation for crop dialogs. */
public final class ImageRotationUtil {

    private ImageRotationUtil() {}

    /** One clockwise quarter-turn. */
    public static Image rotate90Clockwise(Image fxImage) {
        if (fxImage == null) {
            return null;
        }
        BufferedImage src = SwingFXUtils.fromFXImage(fxImage, null);
        if (src == null) {
            return fxImage;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int type =
                src.getTransparency() == BufferedImage.OPAQUE
                        ? BufferedImage.TYPE_INT_RGB
                        : BufferedImage.TYPE_INT_ARGB;
        BufferedImage dest = new BufferedImage(h, w, type);
        Graphics2D g = dest.createGraphics();
        try {
            g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.translate(h, 0);
            g.rotate(Math.PI / 2);
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return SwingFXUtils.toFXImage(dest, null);
    }
}
