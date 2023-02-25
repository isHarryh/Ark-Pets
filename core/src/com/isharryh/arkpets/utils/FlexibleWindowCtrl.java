/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;


public class FlexibleWindowCtrl {
    public Vector2 origin;
    public Insert maxInsert;
    public Insert curInsert;

    /** Initialize a Flexible Window-size Controller instance.
     * @param originSize The original size of the window.
     * @param maxInsertLength The max insert length of each side.
     */
    public FlexibleWindowCtrl(Vector2 originSize, int maxInsertLength) {
        origin = originSize;
        maxInsert = new Insert(
                maxInsertLength, maxInsertLength,
                maxInsertLength, maxInsertLength
        );
        curInsert = new Insert();
    }

    /** Fix the window's size to the best cropped size using the given parameters.
     * Note that {@code offset} should be set greater than {@code extensionLength}, or it may cause shaking.
     * @param pixmap The pixmap got from the current rendered frame of libGDX.
     * @param extensionLength The extension length to be added to each overflowed side (px), no negative. 2~6 recommended.
     * @param offset The offset to apply to the white space (px). 4~8 recommended.
     * @param moderateThreshold The threshold to moderate the variation (px), no negative. 2~4 recommended.
     * @param flipX Flip the pixmap along the x-axis.
     * @param flipY Flip the pixmap along the y-axis.
     * @return False if the size didn't change, true otherwise.
     */
    public boolean fixToBestCroppedSize(Pixmap pixmap, int extensionLength, int offset, int moderateThreshold, boolean flipX, boolean flipY) {
        Insert insert = curInsert.clone();
        final int alphaThreshold = 128;
        final int edgeWidth = pixmap.getWidth() - 1;
        final int edgeHeight = pixmap.getHeight() - 1;

        // TOP
        for (int y = 0; y <= edgeHeight; y++)
            for (int x = 0; x <= edgeWidth; x++)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThreshold) {
                    insert.top += (y == 0 ? extensionLength : - y);
                    if (insert.top < (flipY ? curInsert.bottom : curInsert.top))
                        insert.top = (insert.top < -offset ? insert.top + 1 : (flipY ? curInsert.bottom : curInsert.top));
                    x = Integer.MAX_VALUE - 1;
                    y = Integer.MAX_VALUE - 1;
                }
        // BOTTOM
        for (int y = edgeHeight; y >= 0; y--)
            for (int x = 0; x <= edgeWidth; x++)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThreshold) {
                    insert.bottom += (y == edgeHeight ? extensionLength : y - edgeHeight);
                    if (insert.bottom < (flipY ? curInsert.top : curInsert.bottom))
                        insert.bottom = (insert.bottom < -offset ? insert.bottom + 1 : (flipY ? curInsert.top : curInsert.bottom));
                    x = Integer.MAX_VALUE - 1;
                    y = Integer.MIN_VALUE + 1;
                }
        // LEFT
        for (int x = 0; x <= edgeWidth; x++)
            for (int y = 0; y <= edgeHeight; y++)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThreshold) {
                    insert.left += (x == 0 ? extensionLength : - x);
                    if (insert.left < (flipX ? curInsert.right : curInsert.left))
                        insert.left = (insert.left < -offset ? insert.left + 1 : (flipX ? curInsert.right : curInsert.left));
                    x = Integer.MAX_VALUE - 1;
                    y = Integer.MAX_VALUE - 1;
                }
        // RIGHT
        for (int x = edgeWidth; x >= 0; x--)
            for (int y = 0; y <= edgeHeight; y++)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThreshold) {
                    insert.right += (x == edgeWidth ? extensionLength : x - edgeWidth);
                    if (insert.right < (flipX ? curInsert.left : curInsert.right))
                        insert.right = (insert.right < -offset ? insert.right + 1 : (flipX ? curInsert.left : curInsert.right));
                    x = Integer.MIN_VALUE + 1;
                    y = Integer.MAX_VALUE - 1;
                }

        if (flipX)
            insert.swapHorizontal();
        if (flipY)
            insert.swapVertical();

        insert.bottom = 0; // Temporarily set bottom insert value to 0
        insert.limitMaxNoNegative(maxInsert);
        return curInsert.moderatelyModify(insert, moderateThreshold);
    }

    /** Get the total width.
     * @return The total width.
     */
    public int getWidth() {
        return (int)(curInsert.left + curInsert.right + origin.x);
    }

    /** Get the total height.
     * @return The total height.
     */
    public int getHeight() {
        return (int)(curInsert.top + curInsert.bottom + origin.y);
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

        public void limitMaxNoNegative(Insert maxInsert) {
            top = Math.max(Math.min(top, maxInsert.top), 0);
            bottom = Math.max(Math.min(bottom, maxInsert.bottom), 0);
            left = Math.max(Math.min(left, maxInsert.left), 0);
            right = Math.max(Math.min(right, maxInsert.right), 0);
        }

        public void limitMinNoNegative(Insert minAbsInsert) {
            top = Math.min(Math.max(top, 0), minAbsInsert.top);
            bottom = Math.min(Math.max(bottom, 0), minAbsInsert.bottom);
            left = Math.min(Math.max(left, 0), minAbsInsert.left);
            right = Math.min(Math.max(right, 0), minAbsInsert.right);
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

        public Insert clone() {
            return new Insert(top, bottom, left, right);
        }
    }
}
