/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

import com.isharryh.arkpets.easings.EasingLinear;
import com.isharryh.arkpets.easings.EasingLinearVector3;
import com.isharryh.arkpets.utils.AnimData;
import com.isharryh.arkpets.utils.FlexibleWindowCtrl;
import com.isharryh.arkpets.utils.FrameCtrl;


public class ArkChar {
    private final OrthographicCamera camera;
    private final TwoColorPolygonBatch batch;
    private Texture bgTexture;
    public Vector3 positionCur;
    public Vector3 positionTar;
    public EasingLinearVector3 positionEas;
    public int offset_y;

    private final Skeleton skeleton;
    private final SkeletonRenderer renderer;
    private final int MAX_SKELETON_SIZE = 500;
    private SkeletonData skeletonData;
    private AnimationState animationState;
    private Pixmap lastTexture;

    public FlexibleWindowCtrl flexibleLayout;
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
        // Initialize configuration
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
        camera = new OrthographicCamera();
        batch = new TwoColorPolygonBatch();
        bgTexture = null;
        positionEas = new EasingLinearVector3(new EasingLinear(0, 1, 0.2f));
        positionCur = new Vector3(0, 0, 0);
        positionTar = new Vector3(0, 0, 0);
        offset_y = 0;
        anim_queue = new AnimData[2];

        // Transfer params

        // Initialize the Skeleton
        loadSkeletonData($fp_atlas, $fp_skel, $anim_scale);
        skeleton = new Skeleton(skeletonData);
        animationState.apply(skeleton);
        skeleton.updateWorldTransform();
    }

    private void loadSkeletonData (String $fp_atlas, String $fp_skel, float $scale) {
        // Load atlas & skel/json files to SkeletonData
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal($fp_atlas));
        try {
            switch ($fp_skel.substring($fp_skel.lastIndexOf(".")).toLowerCase()) {
                case ".skel":
                    SkeletonBinary binary = new SkeletonBinary(atlas);
                    binary.setScale($scale);
                    skeletonData = binary.readSkeletonData(Gdx.files.internal($fp_skel));
                    break;
                case ".json":
                    SkeletonJson json = new SkeletonJson(atlas);
                    json.setScale($scale);
                    skeletonData = json.readSkeletonData(Gdx.files.internal($fp_skel));
                    break;
                // default:
            }
        } catch (SerializationException e) {
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
                    asd.setMix(i, j, 0.2f);
        animationState = new AnimationState(asd);
    }

    /** Set the canvas with transparent background.
     */
    public void setCanvas(int $anim_width, int $anim_height, int $anim_fps) {
        this.setCanvas($anim_width, $anim_height, $anim_fps, new Color(0, 0, 0, 0));
    }

    /** Set the canvas with the specified background color.
     */
    public void setCanvas(int $anim_width, int $anim_height, int $anim_fps, Color $bgColor) {
        // Transfer params
        flexibleLayout = new FlexibleWindowCtrl(
                new Vector2($anim_width, $anim_height),
                ($anim_width + $anim_height) * 2
        );
        anim_fps = $anim_fps;
        // Set position (center)
        setPositionTar(MAX_SKELETON_SIZE * 0.5f, 0, 1);
        updateCanvas();
        // Set background image
        Pixmap pixmap = new Pixmap($anim_width, $anim_height, Format.RGBA8888);
        pixmap.setColor($bgColor);
        pixmap.fill();
        bgTexture = new Texture(pixmap);
    }

    /** Fix the canvas size to make it adapted to the animation.
     */
    public void fixCanvasSize() {
        if (!flexibleLayout.fixToBestCroppedSize(getCurrentTexture(false), 40, 80, false, true))
            return;
        System.out.println(
                "^"+flexibleLayout.curInsert.top+
                        "\tv"+flexibleLayout.curInsert.bottom+
                        "\t<"+flexibleLayout.curInsert.left+
                        "\t>"+flexibleLayout.curInsert.right
        );
        //updateCanvas();
    }

    /** Update the canvas and the camera.
     * If you didn't update the canvas in properly, unexpected rendering may cause.
     */
    public void updateCanvas() {
        camera.setToOrtho(false, flexibleLayout.getWidth(), flexibleLayout.getHeight());
        camera.translate(
                ((MAX_SKELETON_SIZE - flexibleLayout.getHeight()) >> 1) - flexibleLayout.curInsert.left,
                -flexibleLayout.curInsert.bottom
        ); // Translated X = (Canvas - Camera) / 2 - Insert
        camera.update();
        batch.getProjectionMatrix().set(camera.combined);
    }

    /** Set the target position.
     */
    public void setPositionTar(float $pos_x, float $pos_y, float $flip) {
        // Set target position
        positionTar.set($pos_x, $pos_y, $flip);
        positionEas.eX.update($pos_x);
        positionEas.eY.update($pos_y + offset_y);
        positionEas.eZ.update($flip);
    }

    /** Set the current position.
     */
    public void setPositionCur(float $deltaTime) {
        // Set current position
        positionCur.set(
                positionEas.eX.step($deltaTime),
                positionEas.eY.step($deltaTime),
                positionEas.eZ.step($deltaTime)
        );
        skeleton.setPosition(positionCur.x, positionCur.y);
        skeleton.setScaleX(positionCur.z);
        skeleton.updateWorldTransform();
    }

    /** Set a new animation
     * @return true=success, false=failure.
     */
    public boolean setAnimation(AnimData $animData) {
        if ($animData != null && (anim_queue[0] == null || anim_queue[0].INTERRUPTABLE)) {
            anim_queue[1] = $animData;
            return true;
        }
        return false;
    }

    /** Get the current framebuffer contents as a Pixmap.
     * Note that the image may not be flipped along the y-axis.
     * @param debug Whether to show debug additions in the pixmap. Note that this will modify the original pixmap.
     * @return Pixmap object.
     */
    public Pixmap getCurrentTexture(boolean debug) {
        Pixmap pixmap = lastTexture == null ? Pixmap.createFromFrameBuffer(0, 0, flexibleLayout.getWidth(), flexibleLayout.getHeight()) : lastTexture;
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
        return pixmap;
    }

    /** Render the animation to batch.
     */
    public void toScreen() {
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
        // Write the current graphic texture to cache
        lastTexture = new Pixmap(flexibleLayout.getWidth(), flexibleLayout.getHeight(), Format.RGBA8888);
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Gdx.gl.glReadPixels(0, 0, flexibleLayout.getWidth(), flexibleLayout.getHeight(),
                GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, lastTexture.getPixels());
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
                anim_frame.next();
            } else {
                return;
            }
        }
        // Render the next frame
        toScreen();
    }

    private void changeAnimation() {
        // Overwrite the current animation(index0) with the new one
        anim_queue[0] = anim_queue[1];
        Gdx.app.log("info", "Anim:"+anim_queue[0].ANIM_NAME);
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
}
