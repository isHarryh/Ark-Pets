package cn.harryh.arkpets.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import static cn.harryh.arkpets.Const.*;


public class ComplexShader extends BaseShader {
    public ComplexShader(boolean gles30, boolean high) {
        super(String.format(pass2VShader, gles30 ? "gles30" : "gl21"),
                String.format(high ? pass2FShader : pass2FShaderLow, gles30 ? "gles30" : "gl21"));
    }

    public void setAlpha(float alpha) {
        setUniformf("u_alpha", alpha);
    }

    public void setOutlineColor(Color outlineColor) {
        setUniformf("u_outlineColor", outlineColor.r, outlineColor.g, outlineColor.b, outlineColor.a);
    }

    public void setOutlineWidth(float outlineWidth) {
        setUniformf("u_outlineWidth", outlineWidth);
    }

    public void setOutlineAlpha(float alpha) {
        setUniformf("u_outlineAlpha", alpha);
    }

    public void setShadowColor(Color shadowColor) {
        setUniformf("u_shadowColor", shadowColor.r, shadowColor.g, shadowColor.b, shadowColor.a);
    }

    public void setTextureSize(Texture texture) {
        setUniformi("u_textureSize", texture.getWidth(), texture.getHeight());
    }
}
