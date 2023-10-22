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
import java.util.Objects;

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
    public int offset_y;

    protected final Skeleton skeleton;
    protected final SkeletonRenderer renderer;
    protected SkeletonData skeletonData;
    protected AnimationState animationState;

    public CroppingCtrl flexibleLayout;
    public AnimClipGroup anim_list;
    public AnimData[] anim_queue;
    public FrameCtrl anim_frame;
    public int anim_fps;

    /** Initializes an ArkPets character.
     * @param $asset_location The path string to the model's directory.
     * @param $asset_accessor The Asset Accessor of the model.
     * @param $anim_scale The scale of the character.
     */
    public ArkChar(String $asset_location, AssetCtrl.AssetAccessor $asset_accessor, float $anim_scale) {
        // Graphic
        camera = new OrthographicCamera();
        batch = new TwoColorPolygonBatch();
        fbo = new FrameBuffer(Format.RGBA8888, canvasMaxSize, canvasMaxSize, false);
        flexibleLayout = new CroppingCtrl(new Vector2(canvasMaxSize, canvasMaxSize), 0);
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
        // Layout
        positionEas = new EasingLinearVector3(new EasingLinear(0, 1, linearEasingDuration));
        positionCur = new Vector3(0, 0, 0);
        positionTar = new Vector3(0, 0, 0);
        offset_y = 0;
        anim_queue = new AnimData[2];
        // Load skeleton
        loadSkeletonData($asset_location, $asset_accessor, $anim_scale);
        // Set animation mix
        AnimationStateData asd = new AnimationStateData(skeletonData);
        for (AnimClip i: anim_list)
            for (AnimClip j: anim_list)
                if (!i.fullName.equals(j.fullName))
                    asd.setMix(i.fullName, j.fullName, linearEasingDuration);
        animationState = new AnimationState(asd);
        // Initialize skeleton
        skeleton = new Skeleton(skeletonData);
        animationState.apply(skeleton);
        skeleton.updateWorldTransform();
        // Initialize canvas
        for (int i = 0; i < anim_list.size(); i++)
            adjustCanvas(Math.round(canvasReserveLength * $anim_scale), anim_list.get(i), i == 0);
        Logger.info("Character", "Canvas size " + flexibleLayout.getWidth() + " * " + flexibleLayout.getHeight());
        updateCanvas();
    }

    /** Sets the canvas with transparent background.
     */
    public void setCanvas(int $anim_fps) {
        this.setCanvas($anim_fps, new Color(0, 0, 0, 0));
    }

    /** Sets the canvas with the specified background color.
     */
    public void setCanvas(int $anim_fps, Color $bgColor) {
        // Transfer params
        anim_fps = $anim_fps;
        // Set position (center)
        setPositionTar(canvasMaxSize >> 1, 0, 1);
        updateCanvas();
        // Set background image
        Pixmap pixmap = new Pixmap(canvasMaxSize, canvasMaxSize, Format.RGBA8888);
        pixmap.setColor($bgColor);
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
    public void adjustCanvas(int $reserved_length, AnimClip $anim_clip, boolean $initialize) {
        setCanvas(fpsDefault);
        setAnimation(new AnimData($anim_clip, null, false, true, 0, 0));
        setPositionCur(Float.MAX_VALUE);
        changeAnimation();
        animationState.update(anim_frame.getDuration() / 2); // Take the middle frame as sample
        Pixmap snapshot = new Pixmap(canvasMaxSize, canvasMaxSize, Format.RGBA8888);
        renderToPixmap(snapshot);
        flexibleLayout.fitToBestCroppedSize(
                snapshot,
                1, $reserved_length,
                false, true, $initialize
        );
        snapshot.dispose();
    }

    /** Sets the skeleton's target position.
     */
    public void setPositionTar(float $pos_x, float $pos_y, float $flip) {
        positionTar.set($pos_x, $pos_y, $flip);
        positionEas.eX.update($pos_x);
        positionEas.eY.update($pos_y + offset_y);
        positionEas.eZ.update($flip);
    }

    /** Sets the skeleton's current position.
     */
    public void setPositionCur(float $deltaTime) {
        positionCur.set(
                positionEas.eX.step($deltaTime),
                positionEas.eY.step($deltaTime),
                positionEas.eZ.step($deltaTime)
        );
        skeleton.setPosition(positionCur.x, positionCur.y);
        skeleton.setScaleX(positionCur.z);
        skeleton.updateWorldTransform();
    }

    /** Requests to set a new animation.
     * @return true=success, false=failure.
     */
    public boolean setAnimation(AnimData $animData) {
        if ($animData != null && (anim_queue[0] == null || anim_queue[0].isEmpty() || anim_queue[0].interruptable())) {
            anim_queue[1] = $animData;
            return true;
        }
        return false;
    }

    protected void loadSkeletonData(String $asset_location, AssetCtrl.AssetAccessor $asset_accessor, float $scale) {
        // Load atlas & skel files to SkeletonData
        try {
            String path2atlas = $asset_location + separator + $asset_accessor.getFirstFileOf(".atlas");
            String path2skel = $asset_location + separator + $asset_accessor.getFirstFileOf(".skel");
            // Load atlas
            TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(path2atlas));
            // Load skel (use SkeletonJson instead of SkeletonBinary if the file type is JSON)
            SkeletonBinary binary = new SkeletonBinary(atlas);
            binary.setScale($scale);
            skeletonData = binary.readSkeletonData(Gdx.files.internal(path2skel));
        } catch (SerializationException | GdxRuntimeException e) {
            Logger.error("Character", "The model asset may be inaccessible, details see below.", e);
            throw new RuntimeException("Launch ArkPets failed, the model asset may be inaccessible.");
        }
        anim_list = new AnimClipGroup(skeletonData.getAnimations().toArray(Animation.class));
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

    /** Renders the character to the screen.
     */
    protected void renderToScreen() {
        // Apply Animation
        setPositionTar(positionTar.x, positionTar.y, positionTar.z);
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
     * @param $pixmap The given Pixmap object.
     */
    protected void renderToPixmap(Pixmap $pixmap) {
        fbo.begin();
        renderToScreen();
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Gdx.gl.glReadPixels(0, 0, $pixmap.getWidth(), $pixmap.getHeight(),
                GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, $pixmap.getPixels());
        fbo.end();
    }

    /** Renders the next frame.
     */
    protected void next() {
        if (anim_queue[1] != null && !anim_queue[1].isEmpty()) {
            // Update the animation queue if it is null
            if (anim_queue[0] == null || anim_queue[0].isEmpty()) {
                changeAnimation();
            } else {
                // Interrupt the last animation if it is interruptable
                if (anim_queue[0].interruptable())
                    if (!Objects.equals(anim_queue[1].name(), anim_queue[0].name())
                            || anim_queue[1].mobility() != anim_queue[0].mobility())
                        changeAnimation();
            }
        }
        // Try the next frame
        anim_frame.next();
        // End the animation if it only needs to play once
        if (anim_frame.hasLooped() && !anim_queue[0].loop()) {
            if (anim_queue[0].animNext() != null && !anim_queue[0].animNext().isEmpty()) {
                anim_queue[1] = anim_queue[0].animNext();
                changeAnimation();
            } else if (anim_queue[1] != null && !anim_queue[1].isEmpty()) {
                changeAnimation();
            } else {
                return;
            }
        }
        // Render the next frame
        renderToScreen();
    }


    protected void changeAnimation() {
        // Overwrite the current animation(index0) with the new one
        anim_queue[0] = anim_queue[1];
        Logger.debug("Animation", "Apply " + anim_queue[0]);
        // Let the character face to the correct direction if the new animation is a moving animation
        if (anim_queue[0].mobility() != 0)
            setPositionTar(positionTar.x, positionTar.y, anim_queue[0].mobility() > 0 ? 1 : -1);
        // Let the character have the correct y-offset
        offset_y = anim_queue[0].offsetY();
        // Update the animation state with the new animation
        Animation animation = skeletonData.findAnimation(anim_queue[0].name());
        anim_frame = new FrameCtrl(animation.getDuration(), anim_fps);
        animationState.setAnimation(0, anim_queue[0].name(), anim_queue[0].loop());
    }


    protected static class FrameCtrl {
        public int F_CUR;
        public boolean LOOPED;
        public final int F_MAX;
        public final float DURATION;

        /** Frame Data Controller object.
         * @param $duration The time(seconds) that the animation plays once.
         * @param $fps Frame per second.
         */
        public FrameCtrl(float $duration, int $fps) {
            LOOPED = false;
            DURATION = $duration;
            float f_TIME = (float) 1 / $fps;
            F_CUR = 0;
            F_MAX = (int) Math.floor($duration / f_TIME) + 2;
        }

        /** Steps to the next frame.
         */
        public void next() {
            if (F_CUR >= F_MAX) {
                LOOPED = true;
                F_CUR = 1;
            } else {
                F_CUR++;
            }
        }

        /** Gets the duration of each loop.
         * @return The time(seconds) that the animation plays once.
         */
        public float getDuration() {
            return DURATION;
        }

        /** Returns true if the animation has looped before.
         */
        public boolean hasLooped() {
            return LOOPED;
        }

        /** Returns true if the current frame is the final frame of one loop.
         */
        public boolean isLoopEnd() {
            return F_CUR == F_MAX;
        }
    }
}
