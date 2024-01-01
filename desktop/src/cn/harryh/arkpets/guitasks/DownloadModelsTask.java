/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.utils.Logger;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.nio.file.Files;

import static cn.harryh.arkpets.Const.PathConfig;


public class DownloadModelsTask extends FetchGitHubRemoteTask {
    public DownloadModelsTask(StackPane root, GuiTaskStyle style) {
        super(
                root,
                style,
                PathConfig.urlModelsZip,
                PathConfig.tempModelsZipCachePath,
                Const.isHttpsTrustAll,
                true);

        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (Exception e) {
            Logger.warn("Task", "Failed to create temp dir.");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getHeader() {
        return "正在下载模型资源文件...";
    }
}
