/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.NetUtils;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;

import static cn.harryh.arkpets.Const.httpBufferSizeDefault;
import static cn.harryh.arkpets.Const.httpTimeoutDefault;


abstract public class FetchRemoteTask extends GuiTask {
    protected final String remotePath;
    protected final String destPath;
    protected final boolean isHttpsTrustAll;

    public FetchRemoteTask(StackPane root, GuiTaskStyle style, String remotePath, String destPath, boolean isHttpTrustAll) {
        super(root, style);
        this.remotePath = remotePath;
        this.destPath = destPath;
        this.isHttpsTrustAll = isHttpTrustAll;
    }

    @Override
    protected Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Logger.info("Network", "Fetching " + remotePath + " to " + destPath);
                this.updateMessage("正在尝试建立连接");

                NetUtils.BufferLog log = new NetUtils.BufferLog(httpBufferSizeDefault);
                HttpsURLConnection connection = NetUtils.ConnectionUtil.createHttpsConnection(new URL(remotePath),
                        httpTimeoutDefault,
                        httpTimeoutDefault,
                        isHttpsTrustAll);
                final InputStream  is = connection.getInputStream();
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
    }
}
