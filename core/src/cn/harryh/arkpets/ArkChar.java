/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.animations.AnimClip;
import cn.harryh.arkpets.animations.AnimClipGroup;
import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.easings.EasingLinear;
import cn.harryh.arkpets.easings.EasingLinearVector3;
import cn.harryh.arkpets.utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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

    public final Vector3 positionCur;
    public final Vector3 positionTar;
    public final EasingLinearVector3 positionEas;
    public int offsetY;

    protected final Skeleton skeleton;
    protected final SkeletonRenderer renderer;
    protected SkeletonData skeletonData;
    protected AnimationState animationState;

    public final CroppingCtrl flexibleLayout;
    public final AnimClipGroup animList;
    public final AnimComposer composer;

    /** Initializes an ArkPets character.
     * @param assetLocation The path string to the model's directory.
     * @param assetAccessor The Asset Accessor of the model.
     * @param scale The scale of the character.
     */
    public ArkChar(String assetLocation, AssetCtrl.AssetAccessor assetAccessor, float scale) {
        // Initialize graphics
        camera = new OrthographicCamera();
        batch = new TwoColorPolygonBatch();
        fbo = new FrameBuffer(Format.RGBA8888, canvasMaxSize, canvasMaxSize, false);
        flexibleLayout = new CroppingCtrl(new Vector2(canvasMaxSize, canvasMaxSize), 0);
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
        // Initialize layout
        positionEas = new EasingLinearVector3(new EasingLinear(0, 1, linearEasingDuration));
        positionCur = new Vector3(0, 0, 0);
        positionTar = new Vector3(0, 0, 0);
        offsetY = 0;
        // Initialize SkeletonData
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
        // Initialize Skeleton
        skeleton = new Skeleton(skeletonData);
        skeleton.updateWorldTransform();
        animList = new AnimClipGroup(skeletonData.getAnimations().toArray(Animation.class));
        // Set animation mix
        AnimationStateData asd = new AnimationStateData(skeletonData);
        for (AnimClip i: animList)
            for (AnimClip j: animList)
                if (!i.fullName.equals(j.fullName))
                    asd.setMix(i.fullName, j.fullName, linearEasingDuration);
        // Initialize AnimationState
        animationState = new AnimationState(asd);
        animationState.apply(skeleton);
        composer = new AnimComposer(animationState);
        // Initialize canvas
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
        // Set position (center)
        setPositionTar(canvasMaxSize >> 1, 0, 1);
        updateCanvas();
        // Set background image
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
        setPositionCur(Float.MAX_VALUE);
        composer.reset();
        composer.offer(new AnimData(anim_clip));
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

    /** Sets the skeleton's target position.
     */
    public void setPositionTar(float pos_x, float pos_y, float flip) {
        positionTar.set(pos_x, pos_y, flip);
        positionEas.eX.update(pos_x);
        positionEas.eY.update(pos_y + offsetY);
        positionEas.eZ.update(flip);
    }

    /** Sets the skeleton's current position.
     */
    public void setPositionCur(float deltaTime) {
        positionCur.set(
                positionEas.eX.step(deltaTime),
                positionEas.eY.step(deltaTime),
                positionEas.eZ.step(deltaTime)
        );
        skeleton.setPosition(positionCur.x, positionCur.y);
        skeleton.setScaleX(positionCur.z);
        skeleton.updateWorldTransform();
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
    protected void renderToScreen() {
        // Apply Animation
        offsetY = composer.getPlaying().offsetY();
        setPositionTar(positionTar.x, positionTar.y, composer.getPlaying().mobility() > 0 ? 1 : -1);
        setPositionCur(Gdx.graphics.getDeltaTime());
        animationState.apply(skeleton);
        animationState.update(Gdx.graphics.getDeltaTime());
        skeleton.updateWorldTransform();
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
        renderToScreen();
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Gdx.gl.glReadPixels(0, 0, pixmap.getWidth(), pixmap.getHeight(),
                GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixmap.getPixels());
        fbo.end();
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
                    state.setAnimation(0, playing.name(), playing.isLoop());
                    Logger.debug("Animation", "Apply " + playing);
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
    } // End class AnimComposer
} // End class ArkChar
