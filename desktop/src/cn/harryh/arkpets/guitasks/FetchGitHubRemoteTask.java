/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.NetUtils;
import cn.harryh.arkpets.utils.NetUtils.GitHubSource;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;

import static cn.harryh.arkpets.Const.httpBufferSizeDefault;
import static cn.harryh.arkpets.Const.httpTimeoutDefault;


abstract public class FetchGitHubRemoteTask extends GuiTask {
    protected final String remotePathSuffix;
    protected final String destPath;
    protected final boolean isArchive;
    protected final boolean isHttpsTrustAll;
    protected GitHubSource selectedSource;

    public FetchGitHubRemoteTask(StackPane root, GuiTaskStyle style, String remotePathSuffix, String destPath, boolean isHttpsTrustAll, boolean isArchive) {
        super(root, style);
        this.remotePathSuffix = remotePathSuffix;
        this.destPath = destPath;
        this.isArchive = isArchive;
        this.isHttpsTrustAll = isHttpsTrustAll;
    }

    @Override
    protected Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                this.updateMessage("正在选择最佳线路");
                Logger.info("Network", "Testing real delay");
                GitHubSource.sortByOverallAvailability(NetUtils.ghSources);
                selectedSource = (GitHubSource)NetUtils.ghSources.get(0);

                Logger.info("Network", "Selected the most available " + selectedSource);
                String remotePathPrefix = isArchive ? selectedSource.archivePreUrl : selectedSource.rawPreUrl;
                String remotePath = remotePathPrefix + remotePathSuffix;

                Logger.info("Network", "Fetching " + remotePath + " to " + destPath);
                this.updateMessage("正在尝试与 " + selectedSource.tag + " 建立连接");

                NetUtils.BufferLog log = new NetUtils.BufferLog(httpBufferSizeDefault);
                HttpsURLConnection connection = NetUtils.ConnectionUtil.createHttpsConnection(new URL(remotePath),
                        httpTimeoutDefault,
                        httpTimeoutDefault,
                        isHttpsTrustAll);
                final InputStream is = connection.getInputStream();
                final OutputStream os = Files.newOutputStream(new File(destPath).toPath());
                final BufferedInputStream  bis = new BufferedInputStream(is, httpBufferSizeDefault);
                final BufferedOutputStream bos = new BufferedOutputStream(os, httpBufferSizeDefault);

                try (bis; bos; is; os) {
                    int len = httpBufferSizeDefault;
                    long sum = 0;
                    long max = connection.getContentLengthLong();
                    byte[] bytes = new byte[len];
                    while ((len = bis.read(bytes)) != -1) {
                        bos.write(bytes, 0, len);
                        sum += len;
                        log.receive();
                        long speed = log.getSpeedPerSecond(500);
                        this.updateMessage("当前已下载：" + NetUtils.getFormattedSizeString(sum) +
                                (speed != 0 ? " (" + NetUtils.getFormattedSizeString(speed) + "/s)" : ""));
                        this.updateProgress(sum, max);
                        if (this.isCancelled()) {
                            this.updateMessage("下载进程已被取消");
                            selectedSource.receiveError();
                            break;
                        }
                    }
                    this.updateProgress(max, max);
                    bos.flush();
                    Logger.info("Network", "Fetched to " + destPath + " , size: " + sum);
                }
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.DialogUtil.createErrorDialog(root, e).show();
        if (selectedSource != null)
            selectedSource.receiveError();
    }
}
