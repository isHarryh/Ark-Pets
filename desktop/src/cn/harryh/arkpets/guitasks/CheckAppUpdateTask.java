/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.Version;
import com.alibaba.fastjson.JSONObject;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

import static cn.harryh.arkpets.Const.PathConfig;
import static cn.harryh.arkpets.Const.appVersion;


public class CheckAppUpdateTask extends FetchRemoteTask {
    public CheckAppUpdateTask(StackPane parent, GuiTaskStyle style, String sourceStr) {
        super(parent,
                style,
                PathConfig.urlApi + "?type=queryVersion&cliVer=" + appVersion + "&source=" + sourceStr,
                PathConfig.tempQueryVersionCachePath,
                Const.isHttpsTrustAll);

        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (Exception e) {
            Logger.warn("Task", "Failed to create temp dir.");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getHeader() {
        return "正在下载软件版本信息...";
    }

    @Override
    protected void onSucceeded(boolean result) {
        // When finished downloading the latest app ver-info:
        try {
            // Try to parse the latest app ver-info
            JSONObject queryVersionResult = Objects.requireNonNull(JSONObject.parseObject(IOUtils.FileUtil.readByte(new File(PathConfig.tempQueryVersionCachePath))));
            // TODO show in-test version
            if (queryVersionResult.getString("msg").equals("success")) {
                // If the response status is "success":
                int[] stableVersionResult = queryVersionResult.getJSONObject("data").getObject("stableVersion", int[].class);
                Version stableVersion = new Version(stableVersionResult);
                if (appVersion.lessThan(stableVersion)) {
                    // On update is available:
                    Const.isUpdateAvailable = true;
                    if (style != GuiTaskStyle.HIDDEN)
                        GuiPrefabs.Dialogs.createCommonDialog(parent,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_INFO_ALT, GuiPrefabs.COLOR_INFO),
                                "检查软件更新",
                                "检测到软件有新的版本！",
                                "当前版本 " + appVersion + " 可更新到 " + stableVersion + "\n请访问ArkPets官网或GitHub下载新的安装包。",
                                null).show();
                } else {
                    // On up-to-dated:
                    Const.isUpdateAvailable = false;
                    if (style != GuiTaskStyle.HIDDEN)
                        GuiPrefabs.Dialogs.createCommonDialog(parent,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_SUCCESS_ALT, GuiPrefabs.COLOR_SUCCESS),
                                "检查软件更新",
                                "尚未发现新的正式版本。",
                                "当前版本 " + appVersion + " 已是最新",
                                null).show();
                }
                Logger.info("Checker", "Application version check finished, newest: " + stableVersion);
            } else {
                // On API failed:
                Logger.warn("Checker", "Application version check failed (api failed)");
                if (style != GuiTaskStyle.HIDDEN)
                    GuiPrefabs.Dialogs.createCommonDialog(parent,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_DANGER, GuiPrefabs.COLOR_DANGER),
                            "检查软件更新",
                            "服务器返回了无效的消息。",
                            "可能是兼容性问题或服务器不可用。\n您可以访问ArkPets官网或GitHub仓库以查看是否有新版本。",
                            null).show();
            }
        } catch (Exception e) {
            // On parsing failed:
            Logger.error("Checker", "Application version check failed unexpectedly, details see below.", e);
            if (style != GuiTaskStyle.HIDDEN)
                GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();
        }
    }
}
