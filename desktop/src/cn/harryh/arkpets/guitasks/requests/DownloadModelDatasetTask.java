/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.network.SourceStrategy;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.net.URL;

import static cn.harryh.arkpets.Const.PathConfig;


public class DownloadModelDatasetTask extends FetchAsFileTask {
    private SourceStrategy.Source selectedSource;

    public DownloadModelDatasetTask(StackPane parent, GuiTaskStyle style) {
        super(parent, style, PathConfig.tempDirPath);
        selectedSource = null;
    }

    @Override
    protected String getHeader() {
        return "正在下载模型版本信息...";
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
    protected URL getTargetURL() {
        selectedSource = SourceStrategy.getStrategy("ModelDataset").getBestSource();
        return selectedSource.toURL();
    }

    @Override
    protected void onDownloadedFile(File file) {
    }
}
