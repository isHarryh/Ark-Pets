/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;


abstract public class UnzipTask extends GuiTask {
    protected final String zipPath;
    protected final String destPath;

    public UnzipTask(StackPane parent, GuiTaskStyle style, String zipPath, String destPath) {
        super(parent, style);
        this.zipPath = zipPath;
        this.destPath = destPath;
    }

    @Override
    protected Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Logger.info("Unzip", "Unzipping " + zipPath + " to " + destPath);
                IOUtils.ZipUtil.unzip(zipPath, destPath, true);
                Logger.info("Unzip", "Unzipped to " + destPath + " , finished");
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();
    }
}
