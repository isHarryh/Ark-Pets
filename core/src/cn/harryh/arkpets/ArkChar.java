/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.animations.AnimClip;
import cn.harryh.arkpets.animations.AnimClip.AnimStage;
import cn.harryh.arkpets.animations.AnimClipGroup;
import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.assets.AssetItem;
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
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
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
    private final AnimComposer composer;
    private final TransitionFloat offsetY;

    private final Skeleton skeleton;
    private final SkeletonRenderer renderer;
    private final AnimationState animationState;

    protected final AnimClipGroup animList;
    protected final HashMap<AnimStage, Insert> stageInsertMap;

    /** Initializes an ArkPets character.
     * @param assetLocation The path string to the model's directory.
     * @param assetAccessor The Asset Accessor of the model.
     * @param scale The scale of the character.
     */
    public ArkChar(String assetLocation, AssetItem.AssetAccessor assetAccessor, float scale) {
        // 1.Graphics setup
        camera = new DynamicOrthographicCamara(canvasMaxSize, canvasMaxSize, Math.round(canvasReserveLength * scale));
        camera.setMaxInsert(0);
        camera.setMinInsert(canvasReserveLength - canvasMaxSize);
        batch = new TwoColorPolygonBatch();
        renderer = new SkeletonRenderer();
        /* Pre-multiplied alpha shouldn't be applied to models released in Arknights 2.1.41 or later,
        otherwise you may get a corrupted rendering result. */
        renderer.setPremultipliedAlpha(false);
        // 2.Geometry setup
        position = new TransitionVector3(TernaryFunction.EASE_OUT_CUBIC, easingDuration);
        offsetY = new TransitionFloat(TernaryFunction.EASE_OUT_CUBIC, easingDuration);
        // 3.Skeleton setup
        SkeletonData skeletonData;
        try {
            String path2atlas = assetLocation + separator + assetAccessor.getFirstFileOf(".atlas");
            String path2skel = assetLocation + separator + assetAccessor.getFirstFileOf(".skel");
            // Load atlas
            TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(path2atlas));
            // Load skel (use SkeletonJson instead of SkeletonBinary if the file type is JSON)
            SkeletonBinary binary = new SkeletonBinary(atlas);
            binary.setScale(scale);
            skeletonData = binary.readSkeletonData(Gdx.files.internal(path2skel));
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
                    asd.setMix(i.fullName, j.fullName, easingDuration);
        // 5.Animation state setup
        animationState = new AnimationState(asd);
        animationState.apply(skeleton);
        composer = new AnimComposer(animationState){
            @Override
            protected void onApply(AnimData playing) {
                Logger.debug("Animation", "Apply " + playing);
                // Sync skeleton position data
                offsetY.reset((float)playing.offsetY());
                position.reset(position.end().x, position.end().y, playing.mobility() != 0 ? playing.mobility() : position.end().z);
            }
        };
        // 6.Canvas setup
        setCanvas(new Color(0, 0, 0, 0));
        stageInsertMap = new HashMap<>();
        for (AnimStage stage : animList.clusterByStage().keySet()) {
            // Figure out the suitable canvas size
            AnimClipGroup stageClips = animList.findAnimations(stage);
            double maxHypSize = 0;
            for (int i = 0; i < stageClips.size(); i++) {
                camera.setInsertMaxed();
                adjustCanvas(stageClips.get(i));
                if (camera.isInsertMaxed())
                    continue;
                double hypSize = Math.hypot(camera.getWidth(), camera.getHeight());
                if (hypSize > maxHypSize) {
                    maxHypSize = hypSize;
                    stageInsertMap.put(stage, camera.getInsert().clone());
                }
            }
            // See if it succeeded
            if (!stageInsertMap.containsKey(stage)) {
                stageInsertMap.put(stage, new Insert((canvasReserveLength << 1) - (canvasMaxSize >> 1)));
                Logger.warn("Character", stage + " figuring camera size failed");
            } else {
                camera.setInsert(stageInsertMap.get(stage));
                Logger.info("Character", stage + " using " + camera);
            }
        }
        camera.setInsertMaxed();
    }

    /** Sets the canvas with the specified background color.
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

    private void adjustCanvas(AnimClip animClip) {
        composer.reset();
        composer.offer(new AnimData(animClip));
        position.reset(camera.getWidth() >> 1, position.end().y, position.end().z);
        position.setToEnd();
        animationState.update(animationState.getCurrent(0).getAnimation().getDuration() / 2); // Take the middle frame as sample

        FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, camera.getWidth(), camera.getHeight(), false);
        fbo.begin();
        renderToBatch();
        Pixmap snapshot = new Pixmap(camera.getWidth(), camera.getHeight(), Format.RGBA8888);
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Gdx.gl.glReadPixels(0, 0, snapshot.getWidth(), snapshot.getHeight(),
                GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, snapshot.getPixels());
        fbo.end();

        if (camera.isInsertMaxed())
            camera.cropTo(snapshot, false, true);
        else
            camera.extendTo(snapshot, false, true);
        fbo.dispose();
        snapshot.dispose();
    }

    protected void adjustCanvas(AnimStage animStage) {
        if (!stageInsertMap.containsKey(animStage))
            throw new IndexOutOfBoundsException("No such key " + animStage);
        camera.setInsert(stageInsertMap.get(animStage));
    }

    /** Renders the character to the Gdx 2D Batch.
     * The animation will be updated by {@code Gdx.graphics.getDeltaTime()};
     */
    protected void renderToBatch() {
        // Apply Animation
        position.reset(camera.getWidth() >> 1, position.end().y, position.end().z);
        position.addProgress(Gdx.graphics.getDeltaTime());
        offsetY.addProgress(Gdx.graphics.getDeltaTime());
        skeleton.setPosition(position.now().x, position.now().y + offsetY.now());
        skeleton.setScaleX(position.now().z);
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
        animationState.update(Gdx.graphics.getDeltaTime());
        // Render the skeleton to the batch
        ScreenUtils.clear(0, 0, 0, 0, true);
        camera.update();
        batch.getProjectionMatrix().set(camera.combined);
        batch.begin();
        batch.draw(bgTexture, 0, 0);
        renderer.draw(batch, skeleton);
        batch.end();
    }


    protected static class AnimComposer {
        protected final AnimationState state;
        protected AnimData playing;

        public AnimComposer(AnimationState boundState) {
            AnimComposer composer = this;
            state = boundState;
            state.addListener(new AnimationState.AnimationStateAdapter() {
                @Override
                public void complete(AnimationState.TrackEntry entry) {
                    AnimData completed = composer.playing;
                    if (completed != null && !completed.isEmpty()) {
                        if (!completed.isLoop()) {
                            composer.reset();
                            if (completed.animNext() != null && !completed.animNext().isEmpty()) {
                                composer.offer(completed.animNext());
                            }
                        }
                    }
                }
            });
        }

        public boolean offer(AnimData animData) {
            if (animData != null && !animData.isEmpty()) {
                if (playing == null || playing.isEmpty() || (!playing.isStrict() && !playing.equals(animData))) {
                    playing = animData;
                    onApply(playing);
                    state.setAnimation(0, playing.name(), playing.isLoop());
                    return true;
                }
            }
            return false;
        }

        public AnimData getPlaying() {
            return playing;
        }

        public void reset() {
            playing = null;
        }

        protected void onApply(AnimData playing) {
        }
    } // End class AnimComposer
} // End class ArkChar
