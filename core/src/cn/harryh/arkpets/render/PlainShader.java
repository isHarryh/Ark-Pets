package cn.harryh.arkpets.render;


import static cn.harryh.arkpets.Const.pass1FShader;
import static cn.harryh.arkpets.Const.pass1VShader;


public class PlainShader extends BaseShader {
    public PlainShader() {
        super(pass1VShader, pass1FShader);
    }

    public void setAlpha(float alpha) {
        setUniformf("u_alpha", alpha);
    }
}
