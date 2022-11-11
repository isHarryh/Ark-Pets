/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;


public class ArkConfig {
    private static FileHandle configCustom
            = Gdx.files.local("ArkPetsCustom.config");
    private static FileHandle configDefault
            = Gdx.files.internal("ArkPetsDefault.config");

    // The following is the config items
    public float display_scale;
    public int   display_fps;
    public int   display_margin_bottom;
    public int[] display_monitor_info;
    public String character_recent;
    public int     behavior_ai_activation;
    public boolean behavior_allow_walk;
    public boolean behavior_allow_sit;
    public boolean behavior_allow_interact;

    /** You are supposed to use the static function {@code ArkConfig.init()} to instantiate the ArkConfig,
     * instead of using this constructor.
     */
    public ArkConfig() {
    }

    /** Get the config in String format.
     * @return The text.
     */
    public String readConfig() {
        return JSON.toJSONString(this, true);
    }

    /** Save the config to the custom file.
     */
    public void saveConfig() {
        configCustom.writeString(readConfig(), false);
    }

    /** Instantiate an ArkConfig.
     * @return ArkConfig object.
     */
    public static ArkConfig init() {
        if (!configCustom.exists() || configCustom.isDirectory()) {
            configDefault.copyTo(configCustom);
        }
        return JSONObject.parseObject(configCustom.readString("UTF-8"), ArkConfig.class);
    }

    /** Get a specific row in a long text.
     * @param fullText The long text.
     * @param rowId The id of the row, start from 1.
     * @return The content of the row.
     */
    public static String readRow(String fullText, int rowId) {
        // Return the specified row in a text
        fullText = fullText.replaceAll("\r\n", "\n");
        String spl = "\n";
        int splLen = spl.length();
        int curA = 0;
        int curB = 0;
        
        if (rowId < 2) {
            if (rowId <= 0)
                return "\0"; // Illegal rowId
            curB = fullText.indexOf(spl);
        } else {
            for (int i = 1; i < rowId; i++) {
                curB = fullText.indexOf(spl, curA) + splLen;
                if (curB < curA)
                    return "\0"; // Out of content
                curA = curB;
            }
            curB = fullText.indexOf(spl, curA);
        }
        return (curA < curB && curB < fullText.length()) ?
                fullText.substring(curA, curB) : "";
    }

    /** Compare two object whether their values are the same,
     * no matter what types they are.
     * @return true=same, false=diff.
     */
    public static boolean compare(Object $a, Object $b) {
        if ($a == null || $b == null)
            return false;
        if (String.valueOf($a).equals(String.valueOf($b)))
            return true;
        else
            return false;
    }

}
