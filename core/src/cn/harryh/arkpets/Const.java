/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.hwnd.HWndCtrl.NumberedTitleManager;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.Version;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


/** Constants definition class.
 */
public final class Const {
    // App version
    public static final Version appVersion              = new Version(3, 2, 0);
    public static final Version datasetLowestVersion    = new Version(2, 2, 0);

    // App name
    public static final String appName                          = "ArkPets";
    public static final String desktopTitle                     = appName + " Launcher " + appVersion;
    public static final NumberedTitleManager coreTitleManager   = new NumberedTitleManager(appName);

    // IO presets
    public static final int zipBufferSizeDefault      = 16 * 1024;
    public static final int httpBufferSizeDefault     = 16 * 1024;
    public static final int httpTimeoutDefault        = 20 * 1000;
    public static final long diskFreeSpaceRecommended = 1024 * 1024 * 1024L;

    // Graphics presets
    public static final int fpsDefault          = 30;
    public static final int coreWidthDefault    = 150;
    public static final int coreHeightDefault   = 150;
    public static final int canvasReserveLength = 80;
    public static final int canvasMaxSize       = 1080;
    public static final float skelBaseScale     = 0.3f;

    // Behavior presets
    public static final int behaviorBaseWeight      = 320;
    public static final int behaviorWeightLv1       = 32;
    public static final int behaviorWeightLv2       = 64;
    public static final float droppedThreshold      = 10f;

    // Duration presets
    public static final float easingDuration    = 0.3f;
    public static final Duration durationFast   = new Duration(150);
    public static final Duration durationNormal = new Duration(300);

    // Encoding presets
    public static final String charsetDefault   = "UTF-8";
    public static final String charsetVBS       = "GBK";

    // Paths of static files and internal files
    public static final String configExternal   = "ArkPetsConfig.json";
    public static final String configInternal   = "/ArkPetsConfigDefault.json";
    public static final String iconFilePng      = "/icons/icon.png";
    public static final String startupTarget    = "ArkPets.exe";
    public static final String startUpScript    = "ArkPetsStartupService.vbs";

    // Changeable constants
    public static boolean isHttpsTrustAll       = false;
    public static boolean isUpdateAvailable     = false;
    public static boolean isNewcomer            = false;

    // Socket C/S constants
    public static final String serverHost           = "localhost";
    public static final int[] serverPorts           = {8686, 8866, 8989, 8899, 8800};
    public static final int reconnectDelayMillis    = 5 * 1000;

    // Misc constants
    public static String ipPortRegex = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?):\\d{1,5}$";


    /** Paths presets definition class.
     */
    public static class PathConfig {
        public static final String urlApi           = "https://arkpets.harryh.cn/p/arkpets/client/api.php";
        public static final String urlDownload      = "https://arkpets.harryh.cn/p/arkpets/?from=client#/download";
        public static final String urlHelp          = "https://arkpets.harryh.cn/p/arkpets/?from=client#/help";
        public static final String urlOfficial      = "https://arkpets.harryh.cn/p/arkpets/?from=client";
        public static final String urlReadme        = "https://github.com/isHarryh/Ark-Pets#readme";
        public static final String urlLicense       = "https://github.com/isHarryh/Ark-Pets";
        public static final String urlModelsZip     = "isHarryh/Ark-Models/archive/refs/heads/main.zip";
        public static final String urlModelsData    = "isHarryh/Ark-Models/main/models_data.json";
        public static final String tempDirPath      = "temp/";
        public static final String fileModelsZipName            = "ArkModels";
        public static final String fileModelsDataPath           = "models_data.json";
        public static final String tempModelsUnzipDirPath       = tempDirPath + "models_unzipped/";
        public static final String tempModelsZipCachePath       = tempDirPath + fileModelsZipName + ".zip";
        public static final String tempQueryVersionCachePath    = tempDirPath + "ApiQueryVersionCache";
    }


    /** Logging presets definition class.
     */
    public static class LogConfig {
        public static final int logCoreMaxKeep      = 32;
        public static final int logDesktopMaxKeep   = 8;

        public static final String logCorePath      = "logs/core";
        public static final String logDesktopPath   = "logs/desktop";

        public static final String error    = "ERROR";
        public static final String warn     = "WARN";
        public static final String info     = "INFO";
        public static final String debug    = "DEBUG";
        public static final String errorArg = "--quiet";
        public static final String warnArg  = "--warn";
        public static final String infoArg  = "--info";
        public static final String debugArg = "--debug";
    }


    /** Fonts provider class.
     */
    public static class FontsConfig {
        private static final String fontFileRegular  = "/fonts/SourceHanSansCN-Regular.otf";
        private static final String fontFileBold     = "/fonts/SourceHanSansCN-Bold.otf";

        public static void loadFontsToJavafx() {
            javafx.scene.text.Font.loadFont(FontsConfig.class.getResourceAsStream(fontFileRegular),
                    javafx.scene.text.Font.getDefault().getSize());
            javafx.scene.text.Font.loadFont(FontsConfig.class.getResourceAsStream(fontFileBold),
                    javafx.scene.text.Font.getDefault().getSize());
        }

        public static void loadFontsToSwing() {
            try {
                InputStream in = Objects.requireNonNull(FontsConfig.class.getResourceAsStream(fontFileRegular));
                java.awt.Font font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, in);
                if (font != null) {
                    UIManager.put("Label.font", font.deriveFont(10f).deriveFont(Font.ITALIC));
                    UIManager.put("Menu.font", font.deriveFont(11f));
                    UIManager.put("MenuItem.font", font.deriveFont(11f));
                }
            } catch (FontFormatException | IOException e) {
                Logger.error("System", "Failed to load tray menu font, details see below.", e);
            }
        }
    }
}
