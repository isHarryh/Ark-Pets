/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.network.Connections;
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
import java.util.UUID;


/** The task fetches the given remote content and saves it to a specified local directory.
 * @implNote Implement the {@link #onDownloadedFile(File)} for future operations to the downloaded file.
 */
abstract public class FetchAsFileTask extends FetchTask {
    private final File localDir;
    private File localFile;

    public FetchAsFileTask(StackPane parent, GuiTaskStyle style, String localDirPath, long contentLengthLimit) {
        super(parent, style, contentLengthLimit);
        localDir = new File(localDirPath);
    }

    public FetchAsFileTask(StackPane parent, GuiTaskStyle style, String localDirPath) {
        this(parent, style, localDirPath, 4L << 30); // 4 GB for default
    }

    /** Called when the file has been successfully downloaded.
     * @param file The downloaded file object representing the local file.
     */
    abstract protected void onDownloadedFile(File file);

    @Override
    protected final Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    Files.createDirectories(localDir.toPath());
                } catch (IOException e) {
                    Logger.warn("Network", "Cannot create the directory for file download");
                    throw e;
                }
                URL remoteURL = getTargetURL();
                Logger.info("Network", "Fetching " + StringUtils.getMaskedURL(remoteURL));

                this.updateMessage("正在尝试建立连接");
                HttpsURLConnection connection = Connections.createHttpsConnection(remoteURL);
                Connections.raiseForStatus(connection);

                long fullSize = connection.getContentLengthLong();
                String filename = Connections.extractDownloadFilename(connection, UUID.randomUUID().toString());
                localFile = new File(localDir, filename);
                OutputStream os = Files.newOutputStream(localFile.toPath());
                Logger.debug("Network",  "Downloading " + StringUtils.getMaskedURL(remoteURL) + " -> " + localFile);

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
                        String bps = getBytesPerSecondString();
                        updateMessage("当前已下载：" +
                                getTotalBytesString() +
                                (fullSize <= 0 ? "" : " / " + StringUtils.getFormattedSizeString(fullSize)) +
                                ("0".equals(bps) ? "" : " (" + bps + "/s)"));
                        updateProgress(getTotalBytes(), fullSize);
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
    protected void onSucceeded(boolean result) {
        if (localFile == null || !localFile.isFile())
            throw new IllegalStateException("Condition not met because localFile is null or not a file");
        onDownloadedFile(localFile);
    }
}
