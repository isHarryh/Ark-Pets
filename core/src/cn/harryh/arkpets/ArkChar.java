/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.animations.AnimClip;
import cn.harryh.arkpets.animations.AnimClipGroup;
import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.assets.AssetItem;
import cn.harryh.arkpets.utils.*;
import cn.harryh.arkpets.transitions.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.SerializationException;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;

import static cn.harryh.arkpets.Const.*;
import static java.io.File.separator;


public class ArkChar {
    protected final OrthographicCamera camera;
    protected final TwoColorPolygonBatch batch;
    protected final FrameBuffer fbo;
    protected Texture bgTexture;

    protected final AnimComposer composer;
    protected final TransitionVector3 position;
    protected final TransitionFloat offsetY;

    protected final Skeleton skeleton;
    protected final SkeletonRenderer renderer;
    protected final SkeletonData skeletonData;
    protected final AnimationState animationState;

    public final CroppingCtrl flexibleLayout;
    public final AnimClipGroup animList;

    /** Initializes an ArkPets character.
     * @param assetLocation The path string to the model's directory.
     * @param assetAccessor The Asset Accessor of the model.
     * @param scale The scale of the character.
     */
    public ArkChar(String assetLocation, AssetItem.AssetAccessor assetAccessor, float scale) {
        // 1.Graphics setup
        camera = new OrthographicCamera();
        batch = new TwoColorPolygonBatch();
        fbo = new FrameBuffer(Format.RGBA8888, canvasMaxSize, canvasMaxSize, false);
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
        // 2.Geometry setup
        position = new TransitionVector3(TernaryFunction.LINEAR, linearEasingDuration);
        offsetY = new TransitionFloat(TernaryFunction.LINEAR, linearEasingDuration);
        flexibleLayout = new CroppingCtrl(new Vector2(canvasMaxSize, canvasMaxSize), 0);
        // 3.Skeleton setup
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
        for (AnimClip i: animList)
            for (AnimClip j: animList)
                if (!i.fullName.equals(j.fullName))
                    asd.setMix(i.fullName, j.fullName, linearEasingDuration);
        // 5.Animation state setup
        animationState = new AnimationState(asd);
        animationState.apply(skeleton);
        composer = new AnimComposer(animationState){
            @Override
            protected void onApply(AnimData animData) {
                Logger.debug("Animation", "Apply " + playing);
                // Sync skeleton position data
                offsetY.reset((float)playing.offsetY());
                position.reset(position.end().x, position.end().y, playing.mobility() != 0 ? playing.mobility() : position.end().z);
            }
        };
        // 6.Canvas setup
        for (int i = 0; i < animList.size(); i++)
            adjustCanvas(Math.round(canvasReserveLength * scale), animList.get(i), i == 0);
        Logger.info("Character", "Canvas size " + flexibleLayout.getWidth() + " * " + flexibleLayout.getHeight());
        updateCanvas();
    }

    /** Sets the canvas with transparent background.
     */
    public void setCanvas() {
        this.setCanvas(new Color(0, 0, 0, 0));
    }

    /** Sets the canvas with the specified background color.
     */
    public void setCanvas(Color bgColor) {
        // Set position (centered)
        position.reset(canvasMaxSize >> 1, 0, 1);
        updateCanvas();
        // Set background texture
        Pixmap pixmap = new Pixmap(canvasMaxSize, canvasMaxSize, Format.RGBA8888);
        pixmap.setColor(bgColor);
        pixmap.fill();
        bgTexture = new Texture(pixmap);
    }

    /** Updates the canvas and the camera to keep the character in the center.
     */
    public void updateCanvas() {
        camera.setToOrtho(false, flexibleLayout.getWidth(), flexibleLayout.getHeight());
        camera.translate((canvasMaxSize - flexibleLayout.getWidth()) >> 1, 0);
        camera.update();
        batch.getProjectionMatrix().set(camera.combined);
    }

    /** Adjusts the canvas to fit the character's size.
     */
    public void adjustCanvas(int reserved_length, AnimClip anim_clip, boolean initialize) {
        setCanvas();
        composer.reset();
        composer.offer(new AnimData(anim_clip));
        position.addProgress(Float.MAX_VALUE);
        animationState.update(animationState.getCurrent(0).getAnimation().getDuration() / 2); // Take the middle frame as sample
        Pixmap snapshot = new Pixmap(canvasMaxSize, canvasMaxSize, Format.RGBA8888);
        renderToPixmap(snapshot);
        flexibleLayout.fitToBestCroppedSize(
                snapshot,
                1, reserved_length,
                false, true, initialize
        );
        snapshot.dispose();
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

    /** Saves the current framebuffer contents as an image file. (Only test-use)
     * Note that the image may not be flipped along the y-axis.
     * @param debug Whether to show debug additions in the pixmap. Note that this will modify the original pixmap.
     */
    @Deprecated
    protected void saveCurrentTexture(boolean debug) {
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, flexibleLayout.getWidth(), flexibleLayout.getHeight());
        if (debug) {
            pixmap.setColor(new Color(1, 0, 0, 0.75f));
            if (flexibleLayout.curInsert.bottom > 0)
                pixmap.drawRectangle(0, 0, pixmap.getWidth(), flexibleLayout.curInsert.bottom);
            if (flexibleLayout.curInsert.top > 0)
                pixmap.drawRectangle(0, pixmap.getHeight() - flexibleLayout.curInsert.top, pixmap.getWidth(), flexibleLayout.curInsert.top);
            if (flexibleLayout.curInsert.left > 0)
                pixmap.drawRectangle(0, 0, flexibleLayout.curInsert.left, pixmap.getHeight());
            if (flexibleLayout.curInsert.right > 0)
                pixmap.drawRectangle(pixmap.getWidth() - flexibleLayout.curInsert.right, 0, flexibleLayout.curInsert.right, pixmap.getHeight());
        }
        PixmapIO.writePNG(new FileHandle("temp.png"), pixmap);
        pixmap.dispose();
    }

    /** Renders the character to the Gdx 2D Batch.
     * The animation will be updated by {@code Gdx.graphics.getDeltaTime()};
     */
    protected void renderToBatch() {
        // Apply Animation
        position.addProgress(Gdx.graphics.getDeltaTime());
        offsetY.addProgress(Gdx.graphics.getDeltaTime());
        skeleton.setPosition(position.now().x, position.now().y + offsetY.now());
        skeleton.setScaleX(position.now().z);
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
        animationState.update(Gdx.graphics.getDeltaTime());
        // Render the skeleton to the batch
        updateCanvas();
        ScreenUtils.clear(0, 0, 0, 0, true);
        batch.begin();
        if (bgTexture != null)
            batch.draw(bgTexture, 0, 0);
        renderer.draw(batch, skeleton);
        batch.end();
    }

    /** Renders the character to a given Pixmap.
     * Note that the mismatch of the Pixmap's size and the screen's size may cause data loss;
     * @param pixmap The reference of the given Pixmap object.
     */
    protected void renderToPixmap(Pixmap pixmap) {
        fbo.begin();
        renderToBatch();
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Gdx.gl.glReadPixels(0, 0, pixmap.getWidth(), pixmap.getHeight(),
                GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixmap.getPixels());
        fbo.end();
    }

    public static class AnimComposer {
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

        protected void onApply(AnimData animData) {
        }
    } // End class AnimComposer
} // End class ArkChar
