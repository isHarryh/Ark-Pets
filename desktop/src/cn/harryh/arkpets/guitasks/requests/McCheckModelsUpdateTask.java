/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import com.alibaba.fastjson.JSONObject;
import javafx.scene.layout.StackPane;


public class McCheckModelsUpdateTask extends FetchAsDataTask {
    private final String cdk;

    public McCheckModelsUpdateTask(StackPane parent, GuiTaskStyle style, String cdk) {
        super(parent, style, 4L << 30); // 4 GB
        this.cdk = cdk;
    }

    @Override
    protected String getHeader() {
        return "正在与 Mirror 酱建立联系";
    }

    @Override
    protected String getRemotePath() {
        return "https://mirrorchyan.com/api/resources/ArkModelsRepo/latest?cdk=" + cdk;
    }

    @Override
    protected void onReceivedData(JSONObject json) {
    }
}
