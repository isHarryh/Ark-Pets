/** Copyright (c) 2022-2026, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;


public class BaseShader extends ShaderProgram {
    public BaseShader(String vertexShader, String fragmentShader) {
        super(Gdx.files.internal(vertexShader), Gdx.files.internal(fragmentShader));
        if (!isCompiled()) {
            throw new RuntimeException(
                    "Failed to compile shaders, source: " + vertexShader + " & " + fragmentShader + ", log: " + getLog()
            );
        }
    }
}
