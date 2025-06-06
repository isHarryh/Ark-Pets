/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.network.Connections;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.StringUtils;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.util.Objects;


/** The task fetches the given remote content and parses it as JSON API response data.
 * @implNote Implement the {@link #onReceivedData(JSONObject)} to retrieve the downloaded data.
 */
abstract public class FetchAsDataTask extends GuiTask {
    private final ByteArrayOutputStream response;
    private final long contentLengthLimit;
    private final int[] forgiveCodes;

    public FetchAsDataTask(StackPane parent, GuiTaskStyle style, long contentLengthLimit, int[] forgiveCodes) {
        super(parent, style);
        this.response = new ByteArrayOutputStream();
        this.contentLengthLimit = contentLengthLimit;
        this.forgiveCodes = forgiveCodes;
    }

    public FetchAsDataTask(StackPane parent, GuiTaskStyle style, long contentLengthLimit) {
        this(parent, style, contentLengthLimit, new int[0]);
    }

    /** Returns the remote path from which the data will be fetched.
     * @return The remote path to be fetched.
     */
    abstract protected String getRemotePath();

    /** Called when the data has been successfully fetched and parsed.
     * @param json The fetched data object, already parsed to JSON.
     */
    abstract protected void onReceivedData(JSONObject json);

    @Override
    protected final Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                URL remoteURL = new URL(getRemotePath());
                Logger.info("Network", "Fetching " + StringUtils.getMaskedURL(remoteURL));

                this.updateMessage("正在尝试建立连接");
                HttpsURLConnection connection = Connections.createHttpsConnection(remoteURL);
                Connections.raiseForStatus(connection, forgiveCodes);
                long max = connection.getContentLengthLong();
                response.reset();

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
                    Connections.consume(connection, response, recorder);
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
        String responseString;
        try {
            if (response.size() == 0)
                throw new IOException("Empty response");
            responseString = response.toString(Const.charsetDefault);
            onReceivedData(Objects.requireNonNull(JSONObject.parseObject(responseString)));
        } catch (IOException | JSONException | NullPointerException e) {
            Logger.error("Network", "Failed to parse response as JSON, details see below.", e);
        }
    }
}
