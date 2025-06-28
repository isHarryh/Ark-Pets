/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.utils.GuiPrefabs;
import javafx.scene.layout.StackPane;

import java.net.URL;


abstract public class FetchTask extends GuiTask {
    protected final long contentLengthLimit;

    public FetchTask(StackPane parent, GuiTaskStyle style, long contentLengthLimit) {
        super(parent, style);
        if (contentLengthLimit <= 0)
            throw new IllegalArgumentException("Content length limit must be greater than 0");
        this.contentLengthLimit = contentLengthLimit;
    }

    /** Returns the remote URL to which the request will be sent.
     * @return The target URL.
     */
    abstract protected URL getTargetURL();

    @Override
    protected String getInitialContent() {
        return "正在激活神经通路";
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();
    }
}
