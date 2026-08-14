/** Copyright (c) 2022-2026, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import static cn.harryh.arkpets.Const.*;


public class EffectShader extends BaseShader {
    public EffectShader(boolean high) {
        super(pass2VShader, high ? pass2FShader : pass2FShaderLow);
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
