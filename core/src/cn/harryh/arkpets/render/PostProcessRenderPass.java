/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.render;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.transitions.EasingFunction;
import cn.harryh.arkpets.transitions.TransitionFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ScreenUtils;

import static cn.harryh.arkpets.Const.canvasMaxSize;


/** The class represents the render pass which draws the camera's frame buffer
 * to the screen along with additional effects such as outline and shadow.
 */
public class PostProcessRenderPass {
    protected final DynamicOrthographicCamara camera;

    private final SpriteBatch batch;
    private final EffectShader shader;
    private Texture bgTexture;
    private final float outlineWidth;
    private final Color outlineColor;
    private final Color shadowColor;
    private final TransitionFloat outlineAlpha;
    private final TransitionFloat alpha;

    /** Initializes a post-process render pass bound to the given camera.
     * @param camera The camera whose frame buffer is used as the input texture.
     * @param config The ArkPets Config instance which contains the essential settings.
     */
    public PostProcessRenderPass(DynamicOrthographicCamara camera, ArkConfig config) {
        this.camera = camera;
        batch = new SpriteBatch();
        /* Shader pedantic should be disabled to avoid uniform not-found error. */
        ShaderProgram.pedantic = false;
        shader = new EffectShader(config.render_shader_high_quality);
        batch.setShader(shader);
        EasingFunction easingFunction = ArkConfig.getEasingFunctionFrom(config.transition_type);
        float easingDuration = Math.max(0, config.transition_duration);
        outlineAlpha = new TransitionFloat(easingFunction, easingDuration);
        alpha = new TransitionFloat(easingFunction, easingDuration);
        outlineWidth = config.render_outline_width;
        outlineColor = new Color(Color.CLEAR);
        shadowColor = ArkConfig.getGdxColorFrom(config.render_shadow_color);
    }

    /** Sets the canvas with the specified background color.
     * @param bgColor The background color which can include alpha value.
     */
    public void setCanvas(Color bgColor) {
        Pixmap pixmap = new Pixmap(canvasMaxSize, canvasMaxSize, Format.RGBA8888);
        pixmap.setColor(bgColor);
        pixmap.fill();
        bgTexture = new Texture(pixmap);
    }

    /** Requests to set the outline's alpha value.
     * @param newAlpha The new alpha value ranging in [0,1].
     */
    public void setOutlineAlpha(float newAlpha) {
        outlineAlpha.reset(Math.max(0f, Math.min(1f, newAlpha)));
    }

    /** Requests to set the outline's color.
     * @param color The color to be applied as the outline.
     */
    public void setOutlineColor(Color color) {
        outlineColor.set(color);
    }

    /** Requests to set the alpha value of the ultimate rendering process.
     * @param newAlpha The new alpha value ranging in [0,1].
     */
    public void setAlpha(float newAlpha) {
        alpha.reset(Math.max(0f, Math.min(1f, newAlpha)));
    }

    /** Renders the camera's frame buffer to the screen along with the additional effects.
     * The transitions will be updated according to {@code Gdx.graphics.getDeltaTime()}.
     */
    public void render() {
        outlineAlpha.addProgress(Gdx.graphics.getDeltaTime());
        alpha.addProgress(Gdx.graphics.getDeltaTime());
        batch.getProjectionMatrix().set(camera.combined);
        Texture passedTexture = camera.getFBO().getColorBufferTexture();
        shader.bind();
        shader.setOutlineColor(outlineColor);
        shader.setOutlineWidth(outlineWidth);
        shader.setOutlineAlpha(outlineAlpha.now());
        shader.setShadowColor(shadowColor);
        shader.setTextureSize(passedTexture);
        shader.setAlpha(alpha.now());
        ScreenUtils.clear(0, 0, 0, 0, true);
        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.draw(bgTexture, 0, 0);
        batch.draw(passedTexture,
                0, 0, 0, 0, camera.getWidth(), camera.getHeight(),
                1, 1, 0,
                0, 0, passedTexture.getWidth(), passedTexture.getHeight(),
                false, true);
        batch.end();
    }
}
