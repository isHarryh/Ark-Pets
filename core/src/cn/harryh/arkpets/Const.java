/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import javafx.util.Duration;


public final class Const {
    // Constants list

    public static final int[] appVersion        = {2, 0, 0};
    public static final String appVersionStr    = appVersion[0] + "." + appVersion[1] + "." + appVersion[2];

    public static final int zipBufferSizeDefault    = 8 * 1024;
    public static final int httpBufferSizeDefault   = 8 * 1024;
    public static final int httpTimeoutDefault      = 30 * 1000;

    public static final int fpsDefault          = 30;
    public static final int coreWidthDefault    = 150;
    public static final int coreHeightDefault   = 150;
    public static final int canvasReserveLength = 100;
    public static final int canvasMaxSize       = 720;
    public static final int windowLongDefault   = 0x80088;

    public static final int behaviorBaseWeight  = 320;
    public static final int behaviorWeightLv1   = 32;
    public static final int behaviorWeightLv2   = 64;

    public static final float behaviorMinTimeLv1    = 2.5f;
    public static final float behaviorMinTimeLv2    = 5.0f;
    public static final float behaviorMinTimeLv3    = 7.5f;

    public static final float skelBaseScale         = 0.33333f;
    public static final float linearEasingDuration  = 0.2f;

    public static final String appName      = "ArkPets";
    public static final String coreTitle    = "ArkPets Core";
    public static final String desktopTitle = "ArkPets Launcher " + appVersionStr;

    public static final Duration durationFast   = new Duration(150);
    public static final Duration durationNormal = new Duration(300);

    public static final String configExternal   = "ArkPetsConfig.json";
    public static final String configInternal   = "/ArkPetsConfigDefault.json";

    public static final String iconFileIco  = "icon.ico";
    public static final String iconFilePng  = "icon.png";

    public static final String charsetDefault   = "UTF-8";
    public static final String charsetVBS       = "GBK";

    public static final String startupTarget    = "ArkPets.exe";
    public static final String startUpScript    = "ArkPetsStartupService.vbs";


    public static class PathConfig {
        public static final String urlApi           = "https://arkpets.harryh.cn/p/arkpets/client/api.php";
        public static final String urlOfficial      = "https://arkpets.harryh.cn/p/arkpets/?from=client";
        public static final String urlModelsZip     = "isHarryh/Ark-Models/archive/refs/heads/main.zip";
        public static final String urlModelsData    = "isHarryh/Ark-Models/main/models_data.json";
        public static final String tempDirPath      = "temp/";
        public static final String tempModelsUnzipDirPath       = tempDirPath + "models_unzipped/";
        public static final String tempModelsZipCachePath       = tempDirPath + "ArkModels.zip";
        public static final String tempQueryVersionCachePath    = tempDirPath + "ApiQueryVersionCache";
        public static final String fileModelsDataPath           = "models_data.json";
    }


    public static class LogLevels {
        public static final String error    = "ERROR";
        public static final String warn     = "WARN";
        public static final String info     = "INFO";
        public static final String debug    = "DEBUG";
        public static final String errorArg = "--quiet";
        public static final String warnArg  = "--warn";
        public static final String infoArg  = "--info";
        public static final String debugArg = "--debug";
    }

}
