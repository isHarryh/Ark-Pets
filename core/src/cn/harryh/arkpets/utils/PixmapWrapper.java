/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.Deflater;


public class PixmapWrapper {
    protected Pixmap pixmap;

    protected static HashMap<String, Color[]> cmapCache = new HashMap<>(4);

    public PixmapWrapper(Pixmap pixmap) {
        this.pixmap = pixmap;
    }

    /** Gets the internal pixmap.
     * @return The internal pixmap itself.
     */
    public Pixmap getPixmap() {
        if (pixmap == null) throw new IllegalStateException("Pixmap is null or has been disposed");
        return pixmap;
    }

    /** Saves the internal pixmap to a PNG file.
     * @param file The target file handle.
     * @param flipY Whether to flip the pixmap vertically when saving.
     */
    public void savePixmap(FileHandle file, boolean flipY) {
        if (pixmap == null) throw new IllegalStateException("Pixmap is null or has been disposed");
        PixmapIO.writePNG(file, pixmap, Deflater.DEFAULT_COMPRESSION, flipY);
    }

    /** Draws an unfilled rectangle on the pixmap.
     * @param color The color of the rectangle's border.
     * @param x The x coordinate of the rectangle's top-left corner.
     * @param y The y coordinate of the rectangle's top-left corner.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     * @param thickness The thickness of the rectangle's border.
     */
    public void drawUnfilledRectangle(Color color, int x, int y, int width, int height, int thickness) {
        if (pixmap == null) throw new IllegalStateException("Pixmap is null or has been disposed");
        if (thickness < 1) throw new IllegalArgumentException("Thickness must be at least 1");
        if (width < 1) throw new IllegalArgumentException("Width must be at least 1");
        if (height < 1) throw new IllegalArgumentException("Height must be at least 1");
        thickness = Math.min(thickness, Math.min(width / 2, height / 2));

        pixmap.setColor(color);
        if (thickness == 1) {
            pixmap.drawRectangle(x, y, width, height);
        } else {
            // Top
            pixmap.fillRectangle(x, y, width, thickness);
            // Bottom
            pixmap.fillRectangle(x, y + height - thickness, width, thickness);
            // Left
            pixmap.fillRectangle(x, y + thickness, thickness, height - 2 * thickness);
            // Right
            pixmap.fillRectangle(x + width - thickness, y + thickness, thickness, height - 2 * thickness);
        }
    }

    /** Draws the color map on the pixmap by the given style and channel.
     * @param style The style name of the color map. See {@link #getCmap(String)} for supported style names.
     * @param channel The channel to be mapped. Supported values: "r", "g", "b", "a" or their upper-cased one.
     */
    public void drawCmap(String style, String channel) {
        if (pixmap == null) throw new IllegalStateException("Pixmap is null or has been disposed");

        Color[] cmap = getCmap(style);
        int[] cmapInts = Arrays.stream(cmap).mapToInt(Color::rgba8888).toArray();

        int shift;
        int mask;
        switch (channel) {
            case "r", "R" -> { mask = 0xFF000000; shift = 24; }
            case "g", "G" -> { mask = 0x00FF0000; shift = 16; }
            case "b", "B" -> { mask = 0x0000FF00; shift = 8; }
            case "a", "A" -> { mask = 0x000000FF; shift = 0; }
            default -> throw new IllegalArgumentException("Unsupported channel: " + channel);
        }

        int width = pixmap.getWidth();
        int height = pixmap.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixmap.getPixel(x, y);
                int index = (pixel & mask) >>> shift;
                pixmap.drawPixel(x, y, cmapInts[index]);
            }
        }
    }

    /** Disposes the internal pixmap.
     * After disposing, the PixmapWrapper object cannot be used anymore.
     */
    public void dispose() {
        if (pixmap != null && !pixmap.isDisposed()) {
            pixmap.dispose();
            pixmap = null;
        }
    }

    /** Creates a PixmapWrapper object from the given DynamicOrthographicCamara's framebuffer.
     * @param camara The given DynamicOrthographicCamara.
     * @return The created PixmapWrapper object.
     */
    public static PixmapWrapper fromCamera(DynamicOrthographicCamara camara) {
        return new PixmapWrapper(
                Pixmap.createFromFrameBuffer(0, 0, camara.getWidth(), camara.getHeight())
        );
    }

    /** Gets the color map by the given style name.
     * @param style The style name. Currently supported styles: "tab16", "tab16t".
     * @return The color mapping array (length 256). Note that the returned array is cached.
     */
    public static Color[] getCmap(String style) {
        if (cmapCache.containsKey(style)) {
            return cmapCache.get(style);
        } else {
            // Build camp
            if (style.equals("tab16") || style.equals("tab16t")) {
                int[] tab16Hex = {
                        0xFFFFFF, 0xD3D3D3, 0xA8A8A8, 0x5FA0D0,
                        0xAFD2E1, 0xC3EDAF, 0xF5E66E, 0xF8B85E,
                        0xFA8A4E, 0xFD5B3D, 0xFF2D2D, 0xEBA9D8,
                        0xF5CEEF, 0xF0BCC8, 0xDC7264, 0xC82800
                };
                Color[] tab16Colors = new Color[256];
                for (int i = 0; i < 16; i++) {
                    Color c = new Color(tab16Hex[i] << 8 | 0xFF);
                    Arrays.fill(tab16Colors, i * 16, (i + 1) * 16, c);
                }
                cmapCache.put(style, tab16Colors);
                if (style.equals("tab16t")) {
                    tab16Colors[0] = new Color(0, 0, 0, 0);
                }
                return tab16Colors;
            } else {
                throw new IllegalArgumentException("Unsupported cmap style: " + style);
            }
        }
    }
}
