/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.render;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ScreenUtils;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;


/** The class represents the render pass which draws the spine skeleton into the camera's frame buffer.
 */
public class SpineRenderPass {
    protected final DynamicOrthographicCamara camera;

    private final TwoColorPolygonBatch batch;
    private final RawShader shader;
    private final SkeletonRenderer renderer;

    /** Initializes a spine render pass bound to the given camera.
     * @param camera The camera whose frame buffer is used as the render target.
     */
    public SpineRenderPass(DynamicOrthographicCamara camera) {
        this.camera = camera;
        batch = new TwoColorPolygonBatch();
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
        /* Shader pedantic should be disabled to avoid uniform not-found error. */
        ShaderProgram.pedantic = false;
        shader = new RawShader();
        batch.setShader(shader);
    }

    /** Renders the given skeleton into the camera's frame buffer.
     * @param skeleton The skeleton to be rendered.
     */
    public void render(Skeleton skeleton) {
        batch.getProjectionMatrix().set(camera.combined);
        camera.getFBO().begin();
        shader.bind();
        shader.setAlpha(1.0f);
        ScreenUtils.clear(0, 0, 0, 0, true);
        batch.begin();
        renderer.draw(batch, skeleton);
        batch.end();
        camera.getFBO().end();
    }

    /** Renders the given skeleton additively onto the currently bound frame buffer.
     * Note that the caller is responsible for binding the frame buffer and clearing it.
     * @param skeleton The skeleton to be rendered.
     * @param alpha The additive rendering alpha.
     */
    public void renderAdditive(Skeleton skeleton, float alpha) {
        batch.getProjectionMatrix().set(camera.combined);
        shader.bind();
        shader.setAlpha(alpha);
        batch.begin();
        renderer.draw(batch, skeleton);
        batch.end();
    }
}
