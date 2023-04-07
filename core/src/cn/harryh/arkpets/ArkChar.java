/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.easings.EasingLinear;
import cn.harryh.arkpets.easings.EasingLinearVector3;
import cn.harryh.arkpets.utils.AnimData;
import cn.harryh.arkpets.utils.CroppingCtrl;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;

import static cn.harryh.arkpets.Const.*;


public class ArkChar {
    private final OrthographicCamera camera;
    private final TwoColorPolygonBatch batch;
    private final FrameBuffer fbo;
    private Texture bgTexture;
    public Vector3 positionCur;
    public Vector3 positionTar;
    public EasingLinearVector3 positionEas;
    public int offset_y;

    private final Skeleton skeleton;
    private final SkeletonRenderer renderer;
    private SkeletonData skeletonData;
    private AnimationState animationState;

    public CroppingCtrl flexibleLayout;
    public String[] anim_list;
    public AnimData[] anim_queue;
    public FrameCtrl anim_frame;
    public int anim_fps;


    /** Initialize an ArkPets character.
     * @param $fp_atlas The file path of the atlas file.
     * @param $fp_skel The file path of the skel file.
     * @param $anim_scale The scale of the character.
     */
    public ArkChar(String $fp_atlas, String $fp_skel, float $anim_scale) {
        // Graphic
        camera = new OrthographicCamera();
        batch = new TwoColorPolygonBatch();
        fbo = new FrameBuffer(Format.RGBA8888, canvasMaxSize, canvasMaxSize, false);
        flexibleLayout = new CroppingCtrl(new Vector2(canvasMaxSize, canvasMaxSize), 0);

        // Layout
        positionEas = new EasingLinearVector3(new EasingLinear(0, 1, linearEasingDuration));
        positionCur = new Vector3(0, 0, 0);
        positionTar = new Vector3(0, 0, 0);
        offset_y = 0;
        anim_queue = new AnimData[2];

        // Skeleton
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
        loadSkeletonData($fp_atlas, $fp_skel, $anim_scale);
        skeleton = new Skeleton(skeletonData);
        animationState.apply(skeleton);
        skeleton.updateWorldTransform();
        for (int i = 0; i < anim_list.length; i++)
            adjustCanvas(Math.round(canvasReserveLength * $anim_scale), anim_list[i], i == 0);
        Logger.info("Character", "Canvas size " + flexibleLayout.getWidth() + " * " + flexibleLayout.getHeight());
        updateCanvas();
    }

    private void loadSkeletonData (String $fp_atlas, String $fp_skel, float $scale) {
        // Load atlas & skel/json files to SkeletonData
        try {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal($fp_atlas));
            switch ($fp_skel.substring($fp_skel.lastIndexOf(".")).toLowerCase()) {
                case ".skel" -> {
                    SkeletonBinary binary = new SkeletonBinary(atlas);
                    binary.setScale($scale);
                    skeletonData = binary.readSkeletonData(Gdx.files.internal($fp_skel));
                }
                case ".json" -> {
                    SkeletonJson json = new SkeletonJson(atlas);
                    json.setScale($scale);
                    skeletonData = json.readSkeletonData(Gdx.files.internal($fp_skel));
                }
                // default:
            }
        } catch (SerializationException | GdxRuntimeException e) {
            Logger.error("Character", "The model asset may be inaccessible, details see below.", e);
            throw new RuntimeException("Launch ArkPets failed, the model asset may be inaccessible.");
        }

        // Write animations' names to the array
        Array<Animation> animations = skeletonData.getAnimations();
        int anim_cont = animations.size;
        anim_list = new String[anim_cont];
        for (int i = 0; i < anim_cont; i++)
            anim_list[i] = animations.get(i).getName();

        // Set animation mix
        AnimationStateData asd = new AnimationStateData(skeletonData);
        for (String i: anim_list)
            for (String j: anim_list)
                if (!i.equals(j))
                    asd.setMix(i, j, linearEasingDuration);
        animationState = new AnimationState(asd);
    }

    /** Set the canvas with transparent background.
     */
    public void setCanvas(int $anim_fps) {
        this.setCanvas($anim_fps, new Color(0, 0, 0, 0));
    }

    /** Set the canvas with the specified background color.
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

    /** Update the canvas and the camera to keep the character in the center.
     */
    public void updateCanvas() {
        camera.setToOrtho(false, flexibleLayout.getWidth(), flexibleLayout.getHeight());
        camera.translate((canvasMaxSize - flexibleLayout.getWidth()) >> 1, 0);
        camera.update();
        batch.getProjectionMatrix().set(camera.combined);
    }

    /** Adjust the canvas to fit the character's size.
     * @param $reserved_length The reserved length of the canvas (px).
     */
    public void adjustCanvas(int $reserved_length, String $anim_name, boolean $initialize) {
        setCanvas(fpsDefault);
        setAnimation(new AnimData($anim_name, false, true));
        setPositionCur(Float.MAX_VALUE);
        changeAnimation();
        animationState.update(anim_frame.F_TIME / 2); // Take the middle frame as sample
        Pixmap snapshot = new Pixmap(canvasMaxSize, canvasMaxSize, Format.RGBA8888);
        renderToPixmap(snapshot);
        flexibleLayout.fitToBestCroppedSize(
                snapshot,
                1, $reserved_length,
                false, true, $initialize
        );
        snapshot.dispose();
    }

    /** Set the skeleton's target position.
     */
    public void setPositionTar(float $pos_x, float $pos_y, float $flip) {
        positionTar.set($pos_x, $pos_y, $flip);
        positionEas.eX.update($pos_x);
        positionEas.eY.update($pos_y + offset_y);
        positionEas.eZ.update($flip);
    }

    /** Set the skeleton's current position.
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

    /** Request to set a new animation.
     * @return true=success, false=failure.
     */
    public boolean setAnimation(AnimData $animData) {
        if ($animData != null && (anim_queue[0] == null || anim_queue[0].INTERRUPTABLE)) {
            anim_queue[1] = $animData;
            return true;
        }
        return false;
    }

    /** Save the current framebuffer contents as an image file. (Only test-use)
     * Note that the image may not be flipped along the y-axis.
     * @param debug Whether to show debug additions in the pixmap. Note that this will modify the original pixmap.
     */
    @Deprecated
    private void saveCurrentTexture(boolean debug) {
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

    /** Render the character to the screen.
     */
    public void renderToScreen() {
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

    /** Render the character to a given Pixmap.
     * Note that the mismatch of the Pixmap's size and the screen's size may cause data loss;
     * @param $pixmap The given Pixmap object.
     */
    private void renderToPixmap(Pixmap $pixmap) {
        fbo.begin();
        renderToScreen();
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Gdx.gl.glReadPixels(0, 0, $pixmap.getWidth(), $pixmap.getHeight(),
                GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, $pixmap.getPixels());
        fbo.end();
    }

    /** Render the next frame.
     */
    public void next() {
        if (anim_queue[1] != null) {
            // Update the animation queue if it is null
            if (anim_queue[0] == null) {
                changeAnimation();
            } else {
            // Interrupt the last animation if it is interruptable
            if (anim_queue[0].INTERRUPTABLE)
                if (!anim_queue[1].ANIM_NAME.equals(anim_queue[0].ANIM_NAME)
                        || anim_queue[1].MOBILITY != anim_queue[0].MOBILITY)
                    changeAnimation();
            }
        }
        // Try the next frame
        anim_frame.next();
        // End the animation if it only needs to play once
        if (anim_frame.LOOPED && !anim_queue[0].LOOP) {
            if (anim_queue[0].ANIM_NEXT != null) {
                anim_queue[1] = anim_queue[0].ANIM_NEXT;
                changeAnimation();
            } else if (anim_queue[1] != null) {
                changeAnimation();
            } else {
                return;
            }
        }
        // Render the next frame
        renderToScreen();
    }

    private void changeAnimation() {
        // Overwrite the current animation(index0) with the new one
        anim_queue[0] = anim_queue[1];
        Logger.debug("Character", "Animation \"" + anim_queue[0].ANIM_NAME + "\"");
        // Let the character face to the correct direction if the new animation is a moving animation
        if (anim_queue[0].MOBILITY != 0)
            setPositionTar(positionTar.x, positionTar.y, anim_queue[0].MOBILITY > 0 ? 1 : -1);
        // Let the character have the correct y-offset
        offset_y = anim_queue[0].OFFSET_Y;
        // Update the animation state with the new animation
        Animation animation = skeletonData.findAnimation(anim_queue[0].ANIM_NAME);
        anim_frame = new FrameCtrl(animation.getDuration(), anim_fps);
        animationState.setAnimation(0, anim_queue[0].ANIM_NAME, anim_queue[0].LOOP);
    }


    public static class FrameCtrl {
        public int F_MAX;
        public int F_CUR;
        public float F_TIME;
        public boolean LOOPED;

        /** Frame Data Controller instance.
         * @param $duration The time(seconds) that the animation plays once.
         * @param $fps Frame per second.
         */
        public FrameCtrl(float $duration, int $fps) {
            LOOPED = false;
            F_TIME = (float) 1 / $fps;
            F_CUR = 0;
            F_MAX = (int) Math.floor($duration / F_TIME) + 2;
        }

        /** Step to the next frame.
         */
        public void next() {
            if (F_CUR >= F_MAX) {
                LOOPED = true;
                F_CUR = 1;
            } else {
                F_CUR++;
            }
        }
    }
}
