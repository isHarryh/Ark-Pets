/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.NetUtils;
import cn.harryh.arkpets.utils.PopupUtils;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
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

                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;
                File file = new File(destPath);
                URL urlFile = new URL(remotePath);
                HttpsURLConnection connection = NetUtils.ConnectionUtil.createHttpsConnection(urlFile, httpTimeoutDefault, httpTimeoutDefault, isHttpsTrustAll);

                try {
                    bis = new BufferedInputStream(connection.getInputStream(), httpBufferSizeDefault);
                    bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()), httpBufferSizeDefault);
                    int len = httpBufferSizeDefault;
                    long sum = 0;
                    long max = connection.getContentLengthLong();
                    byte[] bytes = new byte[len];
                    while ((len = bis.read(bytes)) != -1) {
                        bos.write(bytes, 0, len);
                        sum += len;
                        this.updateMessage("当前已下载：" + NetUtils.getFormattedSizeString(sum));
                        this.updateProgress(sum, max);
                        if (this.isCancelled()) {
                            this.updateMessage("下载进程已被取消");
                            break;
                        }
                    }
                    this.updateProgress(max, max);
                    bos.flush();
                    Logger.info("Network", "Fetched to " + destPath + " , size: " + sum);
                } finally {
                    try {
                        connection.getInputStream().close();
                        if (bis != null)
                            bis.close();
                        if (bos != null)
                            bos.close();
                    } catch (Exception ignored){
                    }
                }
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            PopupUtils.DialogUtil.createErrorDialog(root, e).show();
    }
}
