package com.isharryh.arkpets.utils;

import com.badlogic.gdx.files.FileHandle;


public class AssetCtrl {
    public final String NAME;
    public final String PATH;
    public FileHandle ATLAS;
    public FileHandle PNG;
    public FileHandle SKEL;

    /** Spine animation asset controller.
     * @param $name The name of this spine.
     * @param $f_png The file handle of the atlas file.
     */
    public AssetCtrl(String $name, FileHandle $f_atlas) {
        NAME = $name;
        ATLAS = $f_atlas;
        PATH = ATLAS.pathWithoutExtension();
        PNG = ATLAS.sibling(ATLAS.nameWithoutExtension()+".png");
        SKEL = ATLAS.sibling(ATLAS.nameWithoutExtension()+".skel");
    }

    /** Get the name of the spine asset.
     * @return The name.
     */
    public String toString() {
        return NAME;
    }

}