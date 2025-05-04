/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import java.util.Objects;


public class DynamicOrthographicCamara extends OrthographicCamera {
    protected final int initWidth;
    protected final int initHeight;
    protected final int paddingLength;
    protected final Insert curInsert = new Insert();
    protected final Insert maxInsert = new Insert();
    protected final Insert minInsert = new Insert();
    protected FrameBuffer fbo;

    protected static final int stepLength = 2;
    protected static final boolean cameraYDown = false;

    /** Initializes a Dynamic Orthographic Camera instance.
     * @param paddingLength The padding length of each side.
     */
    public DynamicOrthographicCamara(int initWidth, int initHeight, int paddingLength) {
        this.initWidth = initWidth;
        this.initHeight = initHeight;
        this.paddingLength = paddingLength;
        setInsert(new Insert());
    }

    public void setInsert(Insert insert) {
        // Update insert
        insert.limitMax(maxInsert);
        insert.limitMin(minInsert);
        curInsert.set(insert);
        // Update camera geometry
        setToOrtho(cameraYDown, getWidth(), getHeight());
    }

    public void setMaxInsert(int length) {
        maxInsert.set(length);
        setInsert(curInsert);
    }

    public void setMinInsert(int length) {
        minInsert.set(length);
        setInsert(curInsert);
    }

    public Insert getFittedInsert(Pixmap pixmap, float alphaThreshold, boolean flippedX, boolean flippedY) {
        final Insert insert = curInsert.clone();
        final int edgeWidth = pixmap.getWidth() - 1;
        final int edgeHeight = pixmap.getHeight() - 1;
        final int extendedX = paddingLength;
        final int extendedY = paddingLength;
        final int reservedX = paddingLength;
        final int reservedY = paddingLength;
        final int alphaThresholdInt = Math.max(1, Math.min((int) Math.floor(alphaThreshold * 255f), 255));

        if (flippedX)
            insert.swapHorizontal();
        if (flippedY)
            insert.swapVertical();

        // TOP
        for (int y = 0; y <= edgeHeight; y += stepLength)
            for (int x = 0; x <= edgeWidth; x += stepLength)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThresholdInt) {
                    if (y == 0)
                        insert.top += extendedY;
                    else
                        insert.top -= y - reservedY;
                    x = Integer.MAX_VALUE - stepLength;
                    y = Integer.MAX_VALUE - stepLength;
                }
        // BOTTOM
        for (int y = edgeHeight; y >= 0; y -= stepLength)
            for (int x = 0; x <= edgeWidth; x += stepLength)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThresholdInt) {
                    if (y == edgeHeight)
                        insert.bottom += extendedY;
                    else
                        insert.bottom -= edgeHeight - y - reservedY;
                    x = Integer.MAX_VALUE - stepLength;
                    y = Integer.MIN_VALUE + stepLength;
                }
        // LEFT
        for (int x = 0; x <= edgeWidth; x += stepLength)
            for (int y = 0; y <= edgeHeight; y += stepLength)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThresholdInt) {
                    if (x == 0)
                        insert.left += extendedX;
                    else
                        insert.left -= x - reservedX;
                    x = Integer.MAX_VALUE - stepLength;
                    y = Integer.MAX_VALUE - stepLength;
                }
        // RIGHT
        for (int x = edgeWidth; x >= 0; x -= stepLength)
            for (int y = 0; y <= edgeHeight; y += stepLength)
                if ((pixmap.getPixel(x, y) & 0x000000FF) >= alphaThresholdInt) {
                    if (x == edgeWidth)
                        insert.right += extendedX;
                    else
                        insert.right -= edgeWidth - x - reservedX;
                    x = Integer.MIN_VALUE + stepLength;
                    y = Integer.MAX_VALUE - stepLength;
                }

        if (flippedX)
            insert.swapHorizontal();
        if (flippedY)
            insert.swapVertical();

        return insert;
    }

    /** Gets the total width.
     * @return The total width.
     */
    public int getWidth() {
        return curInsert.left + curInsert.right + initWidth;
    }

    /** Gets the total height.
     * @return The total height.
     */
    public int getHeight() {
        return curInsert.top + curInsert.bottom + initHeight;
    }

    /** Gets the FrameBuffer Object that has the same width and height with this camera.
     * Note that the returned FrameBuffer Object may be a cached one.
     * @return The FrameBuffer Object.
     */
    public FrameBuffer getFBO() {
        if (fbo == null) {
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, getWidth(), getHeight(), false);
        } else if (fbo.getWidth() != getWidth() || fbo.getHeight() != getHeight()) {
            fbo.dispose();
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, getWidth(), getHeight(), false);
        }
        return fbo;
    }

    /** Gets the current insert.
     * @return The insert object;
     */
    public Insert getInsert() {
        return curInsert;
    }

    public boolean isInsertMaxed() {
        return curInsert.equals(maxInsert);
    }

    public void setInsertMaxed() {
        setInsert(maxInsert);
    }

    @Override
    public String toString() {
        return "DynamicOrthographicCamara " + getWidth() + "*" + getHeight() + " {Insert: " + curInsert + "}";
    }


    public static class Insert {
        public int top;
        public int bottom;
        public int left;
        public int right;

        public Insert(int top, int bottom, int left, int right) {
            this.top = top;
            this.bottom = bottom;
            this.left = left;
            this.right = right;
        }

        public Insert(int length) {
            set(length, length, length, length);
        }

        public Insert() {
            set(0);
        }

        public void limitMax(Insert maxInsert) {
            if (maxInsert == null)
                return;
            top = Math.min(top, maxInsert.top);
            bottom = Math.min(bottom, maxInsert.bottom);
            left = Math.min(left, maxInsert.left);
            right = Math.min(right, maxInsert.right);
        }

        public void limitMin(Insert minInsert) {
            if (minInsert == null)
                return;
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

        public void set(int top, int bottom, int left, int right) {
            this.top = top;
            this.bottom = bottom;
            this.left = left;
            this.right = right;
        }

        public void set(int length) {
            set(length, length, length, length);
        }

        public void set(Insert insert) {
            set(insert.top, insert.bottom, insert.left, insert.right);
        }

        @Override
        @SuppressWarnings("MethodDoesntCallSuperMethod")
        public Insert clone() {
            return new Insert(top, bottom, left, right);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Insert insert = (Insert) o;
            return top == insert.top && bottom == insert.bottom && left == insert.left && right == insert.right;
        }

        @Override
        public int hashCode() {
            return Objects.hash(top, bottom, left, right);
        }

        @Override
        public String toString() {
            return "^" + top + " v" + bottom + " <" + left + " >" + right;
        }
    }
}
