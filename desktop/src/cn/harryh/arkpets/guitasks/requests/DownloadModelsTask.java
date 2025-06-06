/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.network.Source;
import cn.harryh.arkpets.utils.Logger;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.nio.file.Files;

import static cn.harryh.arkpets.Const.PathConfig;


public class DownloadModelsTask extends FetchAsFileTask {
    private Source.GitHubSource selectedSource;

    public DownloadModelsTask(StackPane parent, GuiTaskStyle style) {
        super(parent, style, PathConfig.tempModelsZipCachePath, 4L << 30); // 4 GB
        selectedSource = null;

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

    @Override
    protected void onFailed(Throwable e) {
        selectedSource.receiveError();
        super.onFailed(e);
    }

    @Override
    protected void onCancelled() {
        selectedSource.receiveError();
        super.onCancelled();
    }

    @Override
    protected String getRemotePath() {
        selectedSource = Source.GitHubSource.getMostAvailable();
        return selectedSource.archivePreUrl + PathConfig.urlModelsZip;
    }

    @Override
    protected void onDownloadedFile(File file) {
    }
}
