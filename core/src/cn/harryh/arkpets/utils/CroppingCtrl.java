/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;


public class CroppingCtrl {
    private final Vector2 origin;
    private final Insert maxInsert;
    public final Insert curInsert;

    /** Initializes a Cropping Controller instance.
     * @param originSize      The original size of the cropper.
     * @param maxInsertLength The max insert length of each side.
     */
    public CroppingCtrl(Vector2 originSize, int maxInsertLength) {
        origin = originSize;
        maxInsert = new Insert(
                maxInsertLength, maxInsertLength,
                maxInsertLength, maxInsertLength
        );
        curInsert = new Insert();
    }

    /** Fits the size to the best cropped size using the given parameters.
     * @param pixmap     The pixmap got from the current rendered frame of libGDX.
     * @param extended   The extension length to be added to each overflowed side (px).
     * @param reserved   The reservation length of the white space (px).
     * @param flipX      Flip the pixmap along the x-axis.
     * @param flipY      Flip the pixmap along the y-axis.
     * @param initialize Allow to decrease the size.
     */
    public void fitToBestCroppedSize(Pixmap pixmap, int extended, int reserved, boolean flipX, boolean flipY, boolean initialize) {
        Insert insert = curInsert.clone();
        final int alphaThreshold = 255;
        final int edgeWidth = pixmap.getWidth() - 1;
        final int edgeHeight = pixmap.getHeight() - 1;
        //PixmapIO.writePNG(new FileHandle("temp.png"), pixmap);

        if (flipX)
            insert.swapHorizontal();
        if (flipY)
            insert.swapVertical();

        // TOP
        for (int y = 0; y <= edgeHeight; y++)
            for (int x = 0; x <= edgeWidth; x++)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThreshold) {
                    if (y == 0)
                        insert.top += extended;
                    else
                        insert.top -= y - reserved;
                    x = Integer.MAX_VALUE - 1;
                    y = Integer.MAX_VALUE - 1;
                }
        // BOTTOM
        for (int y = edgeHeight; y >= 0; y--)
            for (int x = 0; x <= edgeWidth; x++)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThreshold) {
                    if (y == edgeHeight)
                        insert.bottom += extended;
                    else
                        insert.bottom -= edgeHeight - y - reserved;
                    x = Integer.MAX_VALUE - 1;
                    y = Integer.MIN_VALUE + 1;
                }
        // LEFT
        for (int x = 0; x <= edgeWidth; x++)
            for (int y = 0; y <= edgeHeight; y++)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThreshold) {
                    if (x == 0)
                        insert.left += extended;
                    else
                        insert.left -= x - reserved;
                    x = Integer.MAX_VALUE - 1;
                    y = Integer.MAX_VALUE - 1;
                }
        // RIGHT
        for (int x = edgeWidth; x >= 0; x--)
            for (int y = 0; y <= edgeHeight; y++)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThreshold) {
                    if (x == edgeWidth)
                        insert.right += extended;
                    else
                        insert.right -= edgeWidth - x - reserved;
                    x = Integer.MIN_VALUE + 1;
                    y = Integer.MAX_VALUE - 1;
                }

        if (flipX)
            insert.swapHorizontal();
        if (flipY)
            insert.swapVertical();

        insert.limitMax(maxInsert);
        if (!initialize)
            insert.limitMin(curInsert);
        curInsert.moderatelyModify(insert, 0);
    }

    /** Gets the total width.
     * @return The total width.
     */
    public int getWidth() {
        return (int) (getLeft() + getRight() + origin.x);
    }

    /** Gets the total height.
     * @return The total height.
     */
    public int getHeight() {
        return (int) (getTop() + getBottom() + origin.y);
    }

    public int getTop() {
        return curInsert.top;
    }

    public int getBottom() {
        return curInsert.bottom;
    }

    public int getLeft() {
        return curInsert.left;
    }

    public int getRight() {
        return curInsert.right;
    }

    public static class Insert {
        public int top;
        public int bottom;
        public int left;
        public int right;

        public Insert() {
            top = 0;
            bottom = 0;
            left = 0;
            right = 0;
        }

        public Insert(int top, int bottom, int left, int right) {
            this.top = top;
            this.bottom = bottom;
            this.left = left;
            this.right = right;
        }

        public void limitMax(Insert maxInsert) {
            top = Math.min(top, maxInsert.top);
            bottom = Math.min(bottom, maxInsert.bottom);
            left = Math.min(left, maxInsert.left);
            right = Math.min(right, maxInsert.right);
        }

        public void limitMin(Insert minInsert) {
            top = Math.max(top, minInsert.top);
            bottom = Math.max(bottom, minInsert.bottom);
            left = Math.max(left, minInsert.left);
            right = Math.max(right, minInsert.right);
        }

        public void swapHorizontal() {
            left += right;
            right = left - right;
            left -= right;
        }

        public void swapVertical() {
            bottom += top;
            top = bottom - top;
            bottom -= top;
        }

        public boolean moderatelyModify(Insert changeTo, int threshold) {
            int top_ = Math.abs(changeTo.top - top) > threshold ? changeTo.top : top;
            int bottom_ = Math.abs(changeTo.bottom - bottom) > threshold ? changeTo.bottom : bottom;
            int left_ = Math.abs(changeTo.left - left) > threshold ? changeTo.left : left;
            int right_ = Math.abs(changeTo.right - right) > threshold ? changeTo.right : right;

            if (top_ == top && bottom_ == bottom && left_ == left && right_ == right) {
                return false;
            } else {
                top = top_;
                bottom = bottom_;
                left = left_;
                right = right_;
                return true;
            }
        }

        @Override
        public Insert clone() {
            try {
                return (Insert)super.clone();
            } catch (CloneNotSupportedException e) {
                return new Insert(top, bottom, left, right);
            }
        }

        @Override
        public String toString() {
            return top + "\t" + bottom + "\t" + left + "\t" + right;
        }
    }
}
