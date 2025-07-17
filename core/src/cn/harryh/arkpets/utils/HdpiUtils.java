package cn.harryh.arkpets.utils;

import com.badlogic.gdx.Gdx;


/**
 * A wrapper of GDX HdpiUtils.
 * @see com.badlogic.gdx.graphics.glutils.HdpiUtils
 */
public class HdpiUtils {
    public static int toBackBufferX(int logical) {
        if (Gdx.graphics == null) {
            return logical;
        } else {
            return com.badlogic.gdx.graphics.glutils.HdpiUtils.toBackBufferX(logical);
        }
    }

    public static int toBackBufferY(int logical) {
        if (Gdx.graphics == null) {
            return logical;
        } else {
            return com.badlogic.gdx.graphics.glutils.HdpiUtils.toBackBufferY(logical);
        }
    }

    public static int toLogicalX(int backBufferX) {
        if (Gdx.graphics == null) {
            return backBufferX;
        } else {
            return com.badlogic.gdx.graphics.glutils.HdpiUtils.toLogicalX(backBufferX);
        }
    }

    public static int toLogicalY(int backBufferY) {
        if (Gdx.graphics == null) {
            return backBufferY;
        } else {
            return com.badlogic.gdx.graphics.glutils.HdpiUtils.toLogicalY(backBufferY);
        }
    }
}
