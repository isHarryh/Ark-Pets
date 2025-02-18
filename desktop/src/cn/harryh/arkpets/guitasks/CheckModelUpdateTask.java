/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson.JSONObject;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static cn.harryh.arkpets.Const.PathConfig;
import static cn.harryh.arkpets.Const.charsetDefault;


public class CheckModelUpdateTask extends FetchGitHubRemoteTask {
    public CheckModelUpdateTask(StackPane parent, GuiTaskStyle style) {
        super(
                parent,
                style,
                PathConfig.urlModelsData,
                PathConfig.tempDirPath + PathConfig.fileModelsDataPath,
                Const.isHttpsTrustAll,
                false);

        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (Exception e) {
            Logger.warn("Task", "Failed to create temp dir.");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getHeader() {
        return "正在下载模型版本信息...";
    }

    @Override
    protected void onSucceeded(boolean result) {
        // When finished downloading the remote repo models info:
        try {
            String versionDescription;
            try {
                // Try to parse the remote repo models info
                JSONObject newModelsDataset = JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.tempDirPath + PathConfig.fileModelsDataPath), charsetDefault));
                versionDescription = newModelsDataset.getString("gameDataVersionDescription");
            } catch (Exception e) {
                // When failed to parse the remote repo models info
                versionDescription = "unknown";
                Logger.error("Checker", "Unable to parse remote model repo version, details see below.", e);
                GuiPrefabs.Dialogs.createCommonDialog(parent,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                        "检查模型更新",
                        "无法判断模型仓库版本。",
                        "因发生错误，无法解析远程模型仓库的版本。",
                        null).show();
            }
            // When finished parsing the remote models info:
            // TODO do judgment more precisely
            // Compare the remote models info and the local models info by their MD5

            if (IOUtils.FileUtil.getMD5(new File(PathConfig.fileModelsDataPath)).equals(IOUtils.FileUtil.getMD5(new File(PathConfig.tempDirPath + PathConfig.fileModelsDataPath)))) {
                Logger.info("Checker", "Model repo version check finished (up-to-dated)");
                GuiPrefabs.Dialogs.createCommonDialog(parent,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_SUCCESS_ALT, GuiPrefabs.COLOR_SUCCESS),
                        "检查模型更新",
                        "无需进行模型库更新。",
                        "本地模型库的版本与远程模型库的一致。",
                        "提示：远程模型库的版本不一定和游戏官方同步更新。\n模型库版本描述：\n" + versionDescription).show();
            } else {
                // If the result of comparison is "not the same"
                String oldVersionDescription;
                try {
                    // Try to parse the local repo models info
                    JSONObject oldModelsDataset = JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.fileModelsDataPath), charsetDefault));
                    oldVersionDescription = oldModelsDataset.getString("gameDataVersionDescription");
                } catch (Exception e) {
                    // When failed to parse the remote local models info
                    oldVersionDescription = "unknown";
                    Logger.error("Checker", "Unable to parse local model repo version, details see below.", e);
                    GuiPrefabs.Dialogs.createCommonDialog(parent,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                            "检查模型更新",
                            "无法判断模型库版本。",
                            "因发生错误，无法解析本地模型库的版本。",
                            null).show();
                }
                GuiPrefabs.Dialogs.createCommonDialog(parent,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_INFO_ALT, GuiPrefabs.COLOR_INFO),
                        "检查模型更新",
                        "模型库似乎有更新！",
                        "您可以 [重新下载] 模型，以更新模型库版本。",
                        "远程模型库版本描述：\n" + versionDescription + "\n\n本地模型库版本描述：\n" + oldVersionDescription).show();
                Logger.info("Checker", "Model repo version check finished (not up-to-dated)");
            }
        } catch (IOException e) {
            Logger.error("Checker", "Model repo version check failed unexpectedly, details see below.", e);
            if (style != GuiTaskStyle.HIDDEN)
                GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();
        }
    }
}
