/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.animations.AnimClip;
import cn.harryh.arkpets.animations.AnimClip.AnimStage;
import cn.harryh.arkpets.animations.AnimClipGroup;
import cn.harryh.arkpets.animations.AnimComposer;
import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.assets.ModelItem.ModelAssetAccessor;
import cn.harryh.arkpets.assets.SkeletonLoader;
import cn.harryh.arkpets.render.ComplexShader;
import cn.harryh.arkpets.render.PlainShader;
import cn.harryh.arkpets.transitions.EasingFunction;
import cn.harryh.arkpets.transitions.TransitionFloat;
import cn.harryh.arkpets.transitions.TransitionVector3;
import cn.harryh.arkpets.utils.DynamicOrthographicCamara;
import cn.harryh.arkpets.utils.DynamicOrthographicCamara.Insert;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.PixmapWrapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.SerializationException;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;

import java.util.HashMap;

import static cn.harryh.arkpets.Const.PathConfig.tempDirPath;
import static cn.harryh.arkpets.Const.*;
import static java.io.File.separator;


public class ArkChar {
    protected final DynamicOrthographicCamara camera;
    protected final TransitionVector3 position;

    private final TwoColorPolygonBatch spineBatch;
    private final SpriteBatch finalBatch;
    private Texture bgTexture;
    private final float outlineWidth;
    private final Color outlineColor;
    private final Color shadowColor;
    private final TransitionFloat offsetY;
    private final TransitionFloat outlineAlpha;
    private final TransitionFloat alpha;

    private final PlainShader shader1;
    private final ComplexShader shader2;
    private final Skeleton skeleton;
    private final SkeletonRenderer renderer;

    private final AnimComposer composer;
    private final AnimationState animationState;
    protected final AnimClipGroup animList;
    protected final HashMap<AnimStage, Insert> stageInsertMap;

    /** Initializes an ArkPets character.
     * @param config The ArkPets Config instance which contains the asset's information and other essential settings.
     * @param scale The scale of the skeleton.
     */
    public ArkChar(ArkConfig config, float scale) {
        // 1.Graphics setup
        camera = new DynamicOrthographicCamara(canvasMaxSize, canvasMaxSize, Math.round(canvasReserveLength * scale));
        camera.setMaxInsert(0);
        camera.setMinInsert(canvasReserveLength - canvasMaxSize);
        spineBatch = new TwoColorPolygonBatch();
        finalBatch = new SpriteBatch();
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
        /* Shader pedantic should be disabled to avoid uniform not-found error. */
        ShaderProgram.pedantic = false;
        shader1 = new PlainShader(config.render_enable_angle);
        shader2 = new ComplexShader(config.render_enable_angle, config.render_shader_high_quality);
        Logger.debug("Shader", "Shader program compiled");
        spineBatch.setShader(shader1);
        finalBatch.setShader(shader2);
        // 2.Geometry setup
        EasingFunction easingFunction = ArkConfig.getEasingFunctionFrom(config.transition_type);
        float easingDuration = Math.max(0, config.transition_duration);
        position = new TransitionVector3(easingFunction, easingDuration);
        offsetY = new TransitionFloat(easingFunction, easingDuration);
        outlineAlpha = new TransitionFloat(easingFunction, easingDuration);
        alpha = new TransitionFloat(easingFunction, easingDuration);
        // 3.Skeleton setup
        SkeletonData skeletonData;
        try {
            String assetLocation = config.character_asset;
            ModelAssetAccessor modelAssetAccessor = new ModelAssetAccessor(config.character_files);
            String path2atlas = assetLocation + separator + modelAssetAccessor.getFirstFileOf(".atlas");
            String path2skel = assetLocation + separator + modelAssetAccessor.getFirstFileOf(".skel");
            FileHandle atlasFile = Gdx.files.internal(path2atlas);
            FileHandle skelFile = Gdx.files.internal(path2skel);
            // Load skel
            try {
                SkeletonLoader skeletonLoader = new SkeletonLoader(skelFile);
                Logger.info("Character", "Skeleton loading as " + (skeletonLoader.isJson() ? "JSON" : "binary"));
                if (skeletonLoader.needFix()) {
                    skeletonLoader = skeletonLoader.fixed();
                    Logger.warn("Character", "Skeleton fixed");
                }
                skeletonData = skeletonLoader.loadSkeletonDataWith(atlasFile, scale * skelBaseScale, config.render_enable_mipmap);
                Logger.debug("Character", "Skeleton loaded with Spine version " + skeletonLoader.version);
            } catch (Exception e) {
                Logger.error("Character", "Failed to load skeleton, details see below.", e);
                throw new RuntimeException("Launch ArkPets failed, the model asset may be inaccessible.");
            }
        } catch (SerializationException | GdxRuntimeException e) {
            Logger.error("Character", "The model asset may be inaccessible, details see below.", e);
            throw new RuntimeException("Launch ArkPets failed, the model asset may be inaccessible.");
        }
        skeleton = new Skeleton(skeletonData);
        skeleton.updateWorldTransform();
        // 4.Animation setup
        AnimationStateData asd = new AnimationStateData(skeletonData);
        animList = new AnimClipGroup(skeletonData.getAnimations().toArray(Animation.class));
        // 5.Animation state setup
        animationState = new AnimationState(asd);
        animationState.apply(skeleton);
        composer = new AnimComposer(animationState) {
            @Override
            protected void onApply(AnimData playing) {
                Logger.debug("Animation", "Apply " + playing);
                // Sync skeleton position data
                offsetY.reset(playing.animClip().type.offsetY * scale);
                position.reset(position.end().x, position.end().y, playing.mobility() != 0 ? playing.mobility() : position.end().z);
            }
        };
        // 6.Canvas setup
        setCanvas(ArkConfig.getGdxColorFrom(config.canvas_color));
        outlineWidth = config.render_outline_width;
        outlineColor = new Color(Color.CLEAR);
        shadowColor = ArkConfig.getGdxColorFrom(config.render_shadow_color);
        // 7.Canvas fitting
        stageInsertMap = new HashMap<>();
        for (AnimStage stage : animList.clusterByStage().keySet()) {
            // Figure out the suitable canvas size
            adjustCanvas(stage, config.canvas_sampling_interval, config.canvas_coverage);
            if (!camera.isInsertMaxed()) {
                // Succeeded
                stageInsertMap.put(stage, camera.getInsert().clone());
                Logger.info("Character", stage + " using " + camera);
            } else {
                // Failed, then not to put into stageInsertMap
                Logger.warn("Character", stage + " unable to find a proper canvas size");
            }
        }
        camera.setInsertMaxed();
        // 8.Animation mixing
        animList.applyCompleteAnimMix(asd, config.render_animation_mixture);
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

    /** Requests to set the outline's alpha value of the character.
     * @param newAlpha The new alpha value ranging in [0,1].
     */
    public void setOutlineAlpha(float newAlpha) {
        outlineAlpha.reset(Math.max(0f, Math.min(1f, newAlpha)));
    }

    /** Requests to set the outline's color of the character.
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

    /** Gets the animation playing.
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
        if (!stageInsertMap.containsKey(animStage)) {
            Logger.error("Character", "Failed to adjust the canvas because the given stage corrupted");
            throw new IndexOutOfBoundsException("No such key " + animStage);
        }
        camera.setInsert(stageInsertMap.get(animStage));
    }

    /** Renders the character to the graphics.
     * The animation will be updated according to {@code Gdx.graphics.getDeltaTime()}.
     */
    protected void render() {
        // Update skeleton position and geometry
        position.reset(camera.getWidth() >> 1, position.end().y, position.end().z);
        position.addProgress(Gdx.graphics.getDeltaTime());
        offsetY.addProgress(Gdx.graphics.getDeltaTime());
        outlineAlpha.addProgress(Gdx.graphics.getDeltaTime());
        alpha.addProgress(Gdx.graphics.getDeltaTime());
        skeleton.setPosition(position.now().x, position.now().y + offsetY.now());
        skeleton.setScaleX(position.now().z);
        skeleton.updateWorldTransform();
        spineBatch.getProjectionMatrix().set(camera.combined);
        finalBatch.getProjectionMatrix().set(camera.combined);
        // Apply current animation
        animationState.apply(skeleton);
        animationState.update(Gdx.graphics.getDeltaTime());
        // Render Pass 1: Render the skeleton
        camera.getFBO().begin();
        shader1.bind();
        shader1.setAlpha(1.0f);
        ScreenUtils.clear(0, 0, 0, 0, true);
        spineBatch.begin();
        renderer.draw(spineBatch, skeleton);
        spineBatch.end();
        camera.getFBO().end();
        // Render Pass 2: Render additional effects
        Texture passedTexture = camera.getFBO().getColorBufferTexture();
        shader2.bind();
        shader2.setOutlineColor(outlineColor);
        shader2.setOutlineWidth(outlineWidth);
        shader2.setOutlineAlpha(outlineAlpha.now());
        shader2.setShadowColor(shadowColor);
        shader2.setTextureSize(passedTexture);
        shader2.setAlpha(alpha.now());
        ScreenUtils.clear(0, 0, 0, 0, true);
        finalBatch.begin();
        finalBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        finalBatch.draw(bgTexture, 0, 0);
        finalBatch.draw(passedTexture,
                0, 0, 0, 0, camera.getWidth(), camera.getHeight(),
                1, 1, 0,
                0, 0, passedTexture.getWidth(), passedTexture.getHeight(),
                false, true);
        finalBatch.end();
    }

    /** Renders the character to the graphics additively, ignoring delta time and complex shaders.
     * @param alpha The additive rendering alpha.
     */
    protected void renderAdditive(float alpha) {
        position.reset(camera.getWidth() >> 1, position.end().y, position.end().z);
        skeleton.setPosition(position.end().x, position.end().y + offsetY.end());
        skeleton.setScaleX(position.end().z);
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
        spineBatch.getProjectionMatrix().set(camera.combined);
        shader1.bind();
        shader1.setAlpha(alpha);
        spineBatch.begin();
        renderer.draw(spineBatch, skeleton);
        spineBatch.end();
    }

    private void adjustCanvas(AnimStage stage, int framePerSample, float coverage) {
        // Pre stats total samples
        float timePerSample = framePerSample / (float) fpsDefault;
        int totalSamples = 0;
        for (AnimClip animClip : animList.findAnimations(stage)) {
            composer.reset();
            composer.offer(new AnimData(animClip));
            float totalTime = animationState.getCurrent(0).getAnimation().getDuration();
            if (totalTime > 0) {
                totalSamples += timePerSample <= 0 || totalTime <= timePerSample * 2
                        ? 1 : (int) Math.floor(totalTime / timePerSample);
            }
        }
        if (totalSamples <= 0)
            return;
        // Prepare a Frame Buffer Object
        camera.setInsertMaxed();
        camera.getFBO().begin();
        ScreenUtils.clear(0, 0, 0, 0, true);
        // Render all animations to the FBO
        float alphaPerSample = (float) Math.max(1.0 - 254.0 / 255.0, Math.min(1.0,
                1.0 - Math.pow(10.0, -4.0 / totalSamples) + Math.pow(10, 1.0 / totalSamples - 2.0)
        ));
        for (AnimClip animClip : animList.findAnimations(stage)) {
            composer.reset();
            composer.offer(new AnimData(animClip));
            float totalTime = animationState.getCurrent(0).getAnimation().getDuration();
            if (totalTime > 0) {
                if (timePerSample <= 0 || totalTime <= timePerSample * 2) {
                    // Render the middle frame as the only sample
                    animationState.update(totalTime / 2);
                    renderAdditive(alphaPerSample);
                } else {
                    // Render each interval frame as samples
                    for (float t = 0; t < totalTime; t += timePerSample) {
                        renderAdditive(alphaPerSample);
                        animationState.update(timePerSample);
                    }
                }
            }
        }
        // Take down the snapshot from the rendered FBO
        PixmapWrapper pw = PixmapWrapper.fromCamera(camera);
        camera.getFBO().end();
        // Crop the canvas in order to fit the snapshot
        float alphaThreshold = Math.max(0f, Math.min(coverage, 1f));
        Insert insert;
        do {
            insert = camera.getFittedInsert(pw.getPixmap(), alphaThreshold, false, true);
            if (!insert.equals(camera.getInsert()) || alphaThreshold < 0.75f)
                break;
            alphaThreshold *= 0.9375f;
        } while (true);
        if (alphaThreshold != coverage)
            Logger.warn("Character", stage + " has inappropriate canvas coverage setting, auto adjusted to " + alphaThreshold);
        // For debugging
        if (isDebugEnabled) {
            pw.drawCmap("tab16t", "a");
            pw.drawUnfilledRectangle(Color.RED,
                    -insert.left,
                    -insert.bottom,
                    camera.getWidth() + insert.left + insert.right,
                    camera.getHeight() + insert.top + insert.bottom,
                    2);
            FileHandle file = new FileHandle(tempDirPath).child("acSnapshot-" + skeleton.toString() + "-" + stage.id() + ".png");
            pw.savePixmap(file, true);
            Logger.debug("Character", "Saved acSnapshot to: " + file.path());
        }
        // Complete
        camera.setInsert(insert);
        pw.dispose();
    }
}
