/** Copyright (c) 2022-2026, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.render;

import static cn.harryh.arkpets.Const.pass1FShader;
import static cn.harryh.arkpets.Const.pass1VShader;


public class RawShader extends BaseShader {
    public RawShader() {
        super(pass1VShader, pass1FShader);
    }

    public void setAlpha(float alpha) {
        setUniformf("u_alpha", alpha);
    }
}
