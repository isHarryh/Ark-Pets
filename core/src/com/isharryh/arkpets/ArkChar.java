/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Animation.MixBlend;
import com.esotericsoftware.spine.Animation.MixDirection;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import com.isharryh.arkpets.easings.EasingLinear;
import com.isharryh.arkpets.easings.EasingLinearVector3;
import com.isharryh.arkpets.utils.AnimCtrl;
import com.isharryh.arkpets.utils.FrameCtrl;


public class ArkChar {
    private OrthographicCamera camera;
    private TwoColorPolygonBatch batch;
    private Texture bgTexture;
    public Vector3 positionCur;
    public Vector3 positionTar;
    public EasingLinearVector3 positionEas;
    public int offset_y;
    public Matrix4 transform;

    private Skeleton skeleton;
    private SkeletonRenderer renderer;
    private SkeletonData skeletonData;
    private Animation animation;
    private AnimationState animationState;

    private int anim_width;
    private int anim_height;
    public String[] anim_list;
    public AnimCtrl[] anim_queue;
    public FrameCtrl anim_frame;
    public int anim_fps;
    public float f_time; // Duration(Sec) per frame

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
        transform = new Matrix4();
        anim_queue = new AnimCtrl[2];

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

    /** Set the canvas.
     * @param $anim_width
     * @param $anim_height
     * @param $anim_fps
     */
    public void setCanvas(int $anim_width, int $anim_height, int $anim_fps) {
        this.setCanvas($anim_width, $anim_height, $anim_fps, new Color(0, 0, 0, 0));
    }

    /** Set the canvas.
     * @param $anim_width
     * @param $anim_height
     * @param $anim_fps
     * @param $bgColor
     */
    public void setCanvas(int $anim_width, int $anim_height, int $anim_fps, Color $bgColor) {
        // Transfer params
        anim_width = $anim_width;
        anim_height = $anim_height;
        anim_fps = $anim_fps;
        // Set position (center)
        setPositionTar(anim_width / 2f, 0, 1);
        camera.setToOrtho(false, anim_width, anim_height);
        camera.update();
        batch.getProjectionMatrix().set(camera.combined);
        transform = batch.getTransformMatrix();
        // Set background image
        Pixmap pixmap = new Pixmap($anim_width, $anim_height, Format.RGBA8888);
        pixmap.setColor($bgColor);
        pixmap.fill();
        bgTexture = new Texture(pixmap);
    }

    /** Set the target position.
     * @param $pos_x
     * @param $pos_y
     * @param $flip
     */
    public void setPositionTar(float $pos_x, float $pos_y, float $flip) {
        // Set target position
        positionTar.set($pos_x, $pos_y, $flip);
        positionEas.eX.update($pos_x);
        positionEas.eY.update($pos_y + offset_y);
        positionEas.eZ.update($flip);
    }

    /** Set the current position.
     * @param $deltaTime
     */
    public void setPositionCur(float $deltaTime) {
        // Set current position
        positionCur.set(positionEas.eX.step($deltaTime), positionEas.eY.step($deltaTime), positionEas.eZ.step($deltaTime));
        skeleton.setPosition(positionCur.x, positionCur.y);
        skeleton.setScaleX(positionCur.z);
        skeleton.updateWorldTransform();
    }

    /** Set a new animation
     * @param $animCtrl
     * @return true=success, false=failure.
     */
    public boolean setAnimation(AnimCtrl $animCtrl) {
        if ($animCtrl != null && (anim_queue[0] == null || anim_queue[0].INTERRUPTABLE)) {
            anim_queue[1] = $animCtrl;
            return true;
        }
        return false;
    }

    public void toImg(Pixmap $pixmap, int $frame) {
        // Apply Animation
        animation.apply(skeleton, ($frame - 1) * f_time, ($frame - 1) * f_time, false, null, 1, MixBlend.first, MixDirection.in);
        skeleton.updateWorldTransform();
        // Render the skeleton to the FBO
        ScreenUtils.clear(0, 0, 0, 0);
        batch.begin();
        renderer.draw(batch, skeleton);
        batch.end();
        // Copy the FBO to the pixmap
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Gdx.gl.glReadPixels(0, 0, anim_width, anim_height,
                GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, $pixmap.getPixels());
        // Save the image
        String name = animation.getName() + "_" + $frame + ".png";
        System.out.println(name);
        PixmapIO.writePNG(new FileHandle("output/" + name), $pixmap);
    }

    /** Render the animation to batch.
     * @param $frame
     */
    public void toScreen(int $frame) {
        // Apply Animation
        setPositionTar(positionTar.x, positionTar.y, positionTar.z);
        setPositionCur(Gdx.graphics.getDeltaTime());
        animationState.apply(skeleton);
        animationState.update(Gdx.graphics.getDeltaTime());
        // OLD METHOD: animation.apply(skeleton, ($frame - 1) * anim_frame.F_TIME, ($frame - 1) * anim_frame.F_TIME, false, null, 1, MixBlend.first, MixDirection.in);
        skeleton.updateWorldTransform();
        // Render the skeleton to the FBO
        ScreenUtils.clear(0,0,0,0);
        batch.begin();
        if (bgTexture != null)
            batch.draw(bgTexture, 0, 0);
        renderer.draw(batch, skeleton);
        batch.end();
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
                if (!anim_queue[1].ANIM_NAME.equals(anim_queue[0].ANIM_NAME))
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
        toScreen(anim_frame.F_CUR);
    }

    private void changeAnimation() {
        anim_queue[0] = anim_queue[1];
        Gdx.app.log("info", "Anim:"+anim_queue[0].ANIM_NAME);
        if (anim_queue[0].MOBILITY != 0)
            setPositionTar(positionTar.x, positionTar.y, anim_queue[0].MOBILITY > 0 ? 1 : -1);
        offset_y = anim_queue[0].OFFSET_Y;
        animation = skeletonData.findAnimation(anim_queue[0].ANIM_NAME);
        anim_frame = new FrameCtrl(animation.getDuration(), anim_fps);
        animationState.setAnimation(0, anim_queue[0].ANIM_NAME, anim_queue[0].LOOP);
    }
}
