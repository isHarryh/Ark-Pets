/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.animations.AnimClip;
import cn.harryh.arkpets.animations.AnimClip.AnimStage;
import cn.harryh.arkpets.animations.AnimClipGroup;
import cn.harryh.arkpets.animations.AnimComposer;
import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.assets.AssetItem.AssetAccessor;
import cn.harryh.arkpets.transitions.TernaryFunction;
import cn.harryh.arkpets.transitions.TransitionFloat;
import cn.harryh.arkpets.transitions.TransitionVector3;
import cn.harryh.arkpets.utils.DynamicOrthographicCamara;
import cn.harryh.arkpets.utils.DynamicOrthographicCamara.Insert;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.SerializationException;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;

import java.util.HashMap;

import static cn.harryh.arkpets.Const.*;
import static java.io.File.separator;


public class ArkChar {
    protected final DynamicOrthographicCamara camera;
    protected final TransitionVector3 position;

    private final TwoColorPolygonBatch batch;
    private Texture bgTexture;
    private final TransitionFloat offsetY;
    private final TransitionFloat outlineWidth;

    private final ShaderProgram shader1;
    private final ShaderProgram shader2;
    private final Skeleton skeleton;
    private final SkeletonRenderer renderer;

    private final AnimComposer composer;
    private final AnimationState animationState;
    protected final AnimClipGroup animList;
    protected final HashMap<AnimStage, Insert> stageInsertMap;

    protected float alpha;

    /** Initializes an ArkPets character.
     * @param config The ArkPets Config instance which contains the asset's information and other essential settings.
     * @param scale The scale of the skeleton.
     */
    public ArkChar(ArkConfig config, float scale) {
        // 1.Graphics setup
        camera = new DynamicOrthographicCamara(canvasMaxSize, canvasMaxSize, Math.round(canvasReserveLength * scale));
        camera.setMaxInsert(0);
        camera.setMinInsert(canvasReserveLength - canvasMaxSize);
        batch = new TwoColorPolygonBatch();
        renderer = new SkeletonRenderer();
        Color backgroundColor = config.getBackgroundColor();
        /* Pre-multiplied alpha shouldn't be applied to models released in Arknights 2.1.41 or later,
        otherwise you may get a corrupted rendering result. */
        renderer.setPremultipliedAlpha(false);
        /* Shader pedantic should be disabled to avoid uniform not-found error. */
        ShaderProgram.pedantic = false;
        shader1 = getShader(pass1VShader, pass1FShader);
        shader2 = getShader(pass2VShader, pass2FShader);
        Logger.debug("Shader", "Shader program compiled");
        // 2.Geometry setup
        position = new TransitionVector3(TernaryFunction.EASE_OUT_CUBIC, (float)durationNormal.toSeconds());
        offsetY = new TransitionFloat(TernaryFunction.EASE_OUT_CUBIC, (float)durationNormal.toSeconds());
        outlineWidth = new TransitionFloat(TernaryFunction.EASE_OUT_CUBIC, (float)durationFast.toSeconds());
        // 3.Skeleton setup
        SkeletonData skeletonData;
        try {
            String assetLocation = config.character_asset;
            AssetAccessor assetAccessor = new AssetAccessor(config.character_files);
            String path2atlas = assetLocation + separator + assetAccessor.getFirstFileOf(".atlas");
            String path2skel = assetLocation + separator + assetAccessor.getFirstFileOf(".skel");
            // Load atlas
            TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(path2atlas));
            // Load skel (use SkeletonJson instead of SkeletonBinary if the file type is JSON)
            try {
                SkeletonBinary binary = new SkeletonBinary(atlas);
                binary.setScale(scale * skelBaseScale);
                skeletonData = binary.readSkeletonData(Gdx.files.internal(path2skel));
            } catch (Exception e) {
                Logger.warn("Character", "Failed to load skeleton, trying load as json");
                SkeletonJson json = new SkeletonJson(atlas);
                json.setScale(scale * skelBaseScale);
                skeletonData = json.readSkeletonData(Gdx.files.internal(path2skel));
            }
        } catch (SerializationException | GdxRuntimeException e) {
            Logger.error("Character", "The model asset may be inaccessible, details see below.", e);
            throw new RuntimeException("Launch ArkPets failed, the model asset may be inaccessible.");
        }
        skeleton = new Skeleton(skeletonData);
        skeleton.updateWorldTransform();
        animList = new AnimClipGroup(skeletonData.getAnimations().toArray(Animation.class));
        // 4.Animation mixing
        AnimationStateData asd = new AnimationStateData(skeletonData);
        for (AnimClip i : animList)
            for (AnimClip j : animList)
                if (!i.fullName.equals(j.fullName))
                    asd.setMix(i.fullName, j.fullName, (float)durationNormal.toSeconds());
        // 5.Animation state setup
        animationState = new AnimationState(asd);
        animationState.apply(skeleton);
        composer = new AnimComposer(animationState){
            @Override
            protected void onApply(AnimData playing) {
                Logger.debug("Animation", "Apply " + playing);
                // Sync skeleton position data
                offsetY.reset(playing.offsetY() * scale);
                position.reset(position.end().x, position.end().y, playing.mobility() != 0 ? playing.mobility() : position.end().z);
            }
        };
        // 6.Canvas setup
        setCanvas(backgroundColor);
        stageInsertMap = new HashMap<>();
        for (AnimStage stage : animList.clusterByStage().keySet()) {
            // Figure out the suitable canvas size
            adjustCanvas(animList.findAnimations(stage), config.canvas_fitting_samples);
            if (!camera.isInsertMaxed()) {
                // Succeeded
                stageInsertMap.put(stage, camera.getInsert().clone());
                Logger.info("Character", stage + " using " + camera);
            } else {
                stageInsertMap.put(stage, new Insert((canvasReserveLength << 1) - (canvasMaxSize >> 1)));
                Logger.warn("Character", stage + " using naive camera since the auto fitting has failed");
            }
        }
        camera.setInsertMaxed();
    }

    /** Sets the canvas with the specified background color.
     * @param bgColor The background color which can include alpha value.
     */
    public void setCanvas(Color bgColor) {
        // Set position (centered)
        position.reset(camera.getWidth() >> 1, 0, 1);
        // Set background texture
        Pixmap pixmap = new Pixmap(canvasMaxSize, canvasMaxSize, Format.RGBA8888);
        pixmap.setColor(bgColor);
        pixmap.fill();
        bgTexture = new Texture(pixmap);
    }

    /** Requests to set the current animation of the character.
     * @param animData The animation data.
     * @return true if success.
     */
    public boolean setAnimation(AnimData animData) {
        return composer.offer(animData);
    }

    /** Requests to set the outline width of the character.
     * @param width The outline width in pixel.
     */
    public void setOutlineWidth(float width) {
        outlineWidth.reset(width);
    }

    /** Get the animation playing.
     * @return The animation data.
     */
    public AnimData getPlaying() {
        return composer.getPlaying();
    }

    /** Gets the specified pixel's color value.
     * Note that the image may be flipped along the y-axis.
     * @param x The X-axis coordinate.
     * @param y The Y-axis coordinate.
     * @return The RGBA8888 value of the specified pixel.
     */
    public int getPixel(int x, int y) {
        Pixmap pixmap = Pixmap.createFromFrameBuffer(x, y, 1, 1);
        int pixel = pixmap.getPixel(0, 0);
        pixmap.dispose();
        return pixel;
    }

    /** Adjusts the canvas' geometry to fit the given stage.
     * @param animStage The stage to be fitted.
     * @throws IndexOutOfBoundsException If the given stage isn't in the internal stage map.
     */
    public void adjustCanvas(AnimStage animStage) {
        if (!stageInsertMap.containsKey(animStage))
            throw new IndexOutOfBoundsException("No such key " + animStage);
        camera.setInsert(stageInsertMap.get(animStage));
    }

    /** Renders the character to the graphics.
     * The animation will be updated according to {@code Gdx.graphics.getDeltaTime()}.
     */
    protected void renderToBatch() {
        // Update skeleton position and geometry
        position.reset(camera.getWidth() >> 1, position.end().y, position.end().z);
        position.addProgress(Gdx.graphics.getDeltaTime());
        offsetY.addProgress(Gdx.graphics.getDeltaTime());
        outlineWidth.addProgress(Gdx.graphics.getDeltaTime());
        skeleton.setPosition(position.now().x, position.now().y + offsetY.now());
        skeleton.setScaleX(position.now().z);
        skeleton.updateWorldTransform();
        batch.getProjectionMatrix().set(camera.combined);
        // Apply current animation
        animationState.apply(skeleton);
        animationState.update(Gdx.graphics.getDeltaTime());
        // Render Pass 1: Render the skeleton
        camera.getFBO().begin();
        shader1.bind();
        batch.setShader(shader1);
        ScreenUtils.clear(0, 0, 0, 0, true);
        batch.begin();
        batch.draw(bgTexture, 0, 0);
        renderer.draw(batch, skeleton);
        batch.end();
        batch.setShader(null);
        camera.getFBO().end();
        // Render Pass 2: Render the outline
        Texture passedTexture = camera.getFBO().getColorBufferTexture();
        shader2.bind();
        shader2.setUniformf("u_outlineColor", 1f, 1f, 0f);
        shader2.setUniformf("u_outlineWidth", outlineWidth.now());
        shader2.setUniformi("u_textureSize", passedTexture.getWidth(), passedTexture.getHeight());
        shader2.setUniformf("u_alpha", alpha);
        batch.setShader(shader2);
        ScreenUtils.clear(0, 0, 0, 0, true);
        batch.begin();
        batch.draw(passedTexture,
                0, 0, 0, 0, camera.getWidth(), camera.getHeight(),
                1, 1, 0,
                0, 0, passedTexture.getWidth(), passedTexture.getHeight(),
                false, true);
        batch.end();
        batch.setShader(null);
    }

    private ShaderProgram getShader(String path2vertex, String path2fragment) {
        ShaderProgram shader = new ShaderProgram(Gdx.files.internal(path2vertex), Gdx.files.internal(path2fragment));
        if (!shader.isCompiled()) {
            Logger.error("Shader", "Shader program failed to compile.");
            Logger.error("Shader", "Shader source: " + path2vertex + " & " + path2fragment);
            Logger.error("Shader", "Shader log: " + shader.getLog());
            throw new RuntimeException("Launch ArkPets failed, failed to compile shaders.");
        }
        return shader;
    }

    private void adjustCanvas(AnimClipGroup animClips, int fittingSamples) {
        float timePerSample = fittingSamples / (float)fpsDefault;
        // Prepare a Frame Buffer Object
        camera.setInsertMaxed();
        camera.getFBO().begin();
        ScreenUtils.clear(0, 0, 0, 0, true);
        // Render all animations to the FBO
        for (AnimClip animClip : animClips) {
            composer.reset();
            composer.offer(new AnimData(animClip));
            float totalTime = animationState.getCurrent(0).getAnimation().getDuration();
            if (totalTime > 0) {
                if (timePerSample <= 0 || totalTime <= timePerSample * 2) {
                    // Render the middle frame as the only sample
                    animationState.update(totalTime / 2);
                    renderAsSnapshot();
                } else {
                    // Render each interval frame as samples
                    for (float t = 0; t < totalTime; t += timePerSample) {
                        renderAsSnapshot();
                        animationState.update(timePerSample);
                    }
                }
            }
        }
        // Take down the snapshot from the rendered FBO
        Pixmap snapshot = Pixmap.createFromFrameBuffer(0, 0, camera.getWidth(), camera.getHeight());
        // PixmapIO.writePNG(new FileHandle("temp/temp.png"), snapshot);
        camera.getFBO().end();
        // Crop the canvas in order to fit the snapshot
        camera.cropTo(snapshot, false, true);
        snapshot.dispose();
    }

    private void renderAsSnapshot() {
        position.reset(camera.getWidth() >> 1, position.end().y, position.end().z);
        skeleton.setPosition(position.end().x, position.end().y + offsetY.end());
        skeleton.setScaleX(position.end().z);
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
        batch.getProjectionMatrix().set(camera.combined);

        batch.begin();
        renderer.draw(batch, skeleton);
        batch.end();
    }
}
