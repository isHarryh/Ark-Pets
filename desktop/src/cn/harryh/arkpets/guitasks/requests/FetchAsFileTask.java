/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.network.Connections;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.StringUtils;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;


/** The task fetches the given remote content and saves it to a specified local path.
 * @implNote Implement the {@link #onDownloadedFile(File)} for future operations to the downloaded file.
 */
abstract public class FetchAsFileTask extends GuiTask {
    protected final String localPath;
    private final long contentLengthLimit;

    public FetchAsFileTask(StackPane parent, GuiTaskStyle style, String localPath, long contentLengthLimit) {
        super(parent, style);
        this.localPath = localPath;
        this.contentLengthLimit = contentLengthLimit;
    }

    /** Returns the remote path from which the file will be fetched.
     * @return The remote path to be fetched.
     */
    abstract protected String getRemotePath();

    /** Called when the file has been successfully downloaded.
     * @param file The downloaded file object representing the local file.
     */
    abstract protected void onDownloadedFile(File file);

    @Override
    protected final Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                URL remoteURL = new URL(getRemotePath());
                File localFile = new File(localPath);
                Logger.info("Network", "Fetching " + StringUtils.getMaskedURL(remoteURL) + " to " + localFile);

                this.updateMessage("正在尝试建立连接");
                HttpsURLConnection connection = Connections.createHttpsConnection(remoteURL);
                Connections.raiseForStatus(connection);
                long max = connection.getContentLengthLong();
                OutputStream os = Files.newOutputStream(localFile.toPath());

                Connections.Recorder recorder = new Connections.Recorder() {
                    @Override
                    public void receive(int size) throws IOException {
                        super.receive(size);
                        long total = getTotalBytes();
                        if (isCancelled()) {
                            Logger.warn("Network", "Cancelled fetching manually, size: " + total);
                            InterruptedIOException e = new InterruptedIOException("Cancelled fetching manually");
                            e.bytesTransferred = (int) total;
                            throw e;
                        }
                        if (total > contentLengthLimit) {
                            Logger.error("Network", "Exceeded content length limit, size: " + total);
                            throw new IOException("Exceeded content length limit");
                        }
                        updateMessage("当前已下载：" + getTotalBytesString() + (getBytesPerSecondString().equals("0") ?
                                "" : " (" + getBytesPerSecondString() + "/s)"));
                        updateProgress(getTotalBytes(), max);
                    }
                };

                this.updateMessage("正在等待数据响应");
                try {
                    Connections.consume(connection, os, recorder);
                    Logger.info("Network", "Fetched " + StringUtils.getMaskedURL(remoteURL) +
                            " , size: " + recorder.getTotalBytes());
                } catch (InterruptedIOException e) {
                    return false;
                }
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();
    }

    @Override
    protected void onSucceeded(boolean result) {
        onDownloadedFile(new File(localPath));
    }
}
