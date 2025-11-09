package cn.harryh.arkpets.render;

import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;


public class BaseShader extends ShaderProgram {
    public BaseShader(String vertexShader, String fragmentShader) {
        super(Gdx.files.internal(vertexShader), Gdx.files.internal(fragmentShader));
        if (!isCompiled()) {
            Logger.error("Shader", "Shader program failed to compile.");
            Logger.error("Shader", "Shader source: " + vertexShader + " & " + fragmentShader);
            Logger.error("Shader", "Shader log: " + getLog());
            throw new RuntimeException("Launch ArkPets failed, failed to compile shaders.");
        }
    }
}
