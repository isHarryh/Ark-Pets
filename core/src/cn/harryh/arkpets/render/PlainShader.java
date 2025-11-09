package cn.harryh.arkpets.render;


import static cn.harryh.arkpets.Const.pass1FShader;
import static cn.harryh.arkpets.Const.pass1VShader;


public class PlainShader extends BaseShader {
    public PlainShader(boolean gles30) {
        super(String.format(pass1VShader, gles30 ? "gles30" : "gl21"), String.format(pass1FShader, gles30 ? "gles30" : "gl21"));
    }

    public void setAlpha(float alpha) {
        setUniformf("u_alpha", alpha);
    }
}
