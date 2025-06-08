/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.guitasks.GuiTask.GuiTaskStyle;
import cn.harryh.arkpets.guitasks.requests.McCheckAppUpdateTask;
import cn.harryh.arkpets.network.api.McQueryVersion;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

import java.security.GeneralSecurityException;
import java.util.regex.Pattern;


public final class DownloadDialog implements DialogController<ArkHomeFX> {
    @FXML
    private AnchorPane dialog;
    @FXML
    private JFXButton dialogReturn;

    @FXML
    private Label mcIndicator;
    @FXML
    private Label mcPurchase;
    @FXML
    private JFXTextField mcCdkInput;
    @FXML
    private JFXButton mcConfirm;
    @FXML
    private Label psIndicator;
    @FXML
    private JFXButton psConfirm;

    private ArkHomeFX app;

    private Runnable afterConfirm;
    private BooleanProperty isMc;

    private static final Pattern cdkPattern = Pattern.compile("[\\w\\-]{4,256}");

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;

        afterConfirm = null;
        isMc = new SimpleBooleanProperty(app.config.getMcCdk() != null);

        initPs();
        initMc();
    }

    @Override
    public AnchorPane getDialogPane() {
        return dialog;
    }

    @Override
    public JFXButton getReturnButton() {
        return dialogReturn;
    }

    @Override
    public void notifyDialogOpened(Object data) {
        afterConfirm = null;
        if (data != null) {
            if (data instanceof Runnable runnable) {
                // The runnable will be started if any confirm action is triggered
                afterConfirm = runnable;
            } else {
                throw new IllegalArgumentException("Invalid data type");
            }
        }
        if (app.config.getMcCdk() != null) {
            isMc.setValue(true);
            mcIndicator.setManaged(true);
            mcIndicator.setVisible(true);
            psIndicator.setManaged(false);
            psIndicator.setVisible(false);
            mcCdkInput.setPromptText("点击下方按钮以验证当前 CDK");
        } else {
            isMc.setValue(false);
            mcIndicator.setManaged(false);
            mcIndicator.setVisible(false);
            psIndicator.setManaged(true);
            psIndicator.setVisible(true);
            mcCdkInput.setPromptText("请在这里输入或粘贴 CDK");
        }
    }

    public BooleanProperty getIsMcProperty() {
        return isMc;
    }

    private void initPs() {
        psConfirm.setOnAction(e -> {
            try {
                app.config.setMcCdk("");
                app.config.save();
                isMc.setValue(false);
            } catch (GeneralSecurityException ignored) {
            }
            Logger.info("DownloadDialog", "Chose the public source");

            triggerReturnActionCallback(new ActionEvent(this, null));
            if (afterConfirm != null)
                afterConfirm.run();
        });
    }

    private void initMc() {
        mcPurchase.setOnMouseClicked(e -> app.popBrowser(Const.PathConfig.urlMirrorChyan));
        mcCdkInput.setOnKeyPressed(e -> {
            if (e.getCode().getName().equals(KeyCode.ENTER.getName()))
                submitMcCdk();
        });
        mcConfirm.setOnAction(e -> submitMcCdk());
    }

    private void submitMcCdk() {
        String oldCdk = app.config.getMcCdk();
        String input = mcCdkInput.getText().strip();
        String cdk = input.isEmpty() && oldCdk != null ? oldCdk : input;

        if (cdk.isEmpty()) {
            mcCdkInput.clear();
            Logger.debug("DownloadDialog", "Empty CDK input");
            return;
        }
        if (!cdkPattern.matcher(cdk).matches()) {
            mcCdkInput.clear();
            Logger.debug("DownloadDialog", "Invalid CDK input");
            return;
        }

        Logger.info("DownloadDialog", "Verifying CDK");
        new McCheckAppUpdateTask(app.body, GuiTaskStyle.COMMON, cdk) {
            @Override
            protected void onReceivedData(JSONObject json) {
                McQueryVersion value = json.toJavaObject(McQueryVersion.class);
                try {
                    value.raiseForCode();
                    app.config.setMcCdk(cdk);
                    app.config.save();
                    isMc.setValue(true);

                    String exp = StringUtils.getSimpleTimeString(value.getCDKExpiredTime());
                    Logger.info("DownloadDialog", "Accepted CDK, expires at: " + exp);
                    mcCdkInput.clear();

                    if (afterConfirm != null) {
                        GuiPrefabs.Dialogs.createConfirmDialog(app.body,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_SUCCESS_ALT, GuiPrefabs.COLOR_SUCCESS),
                                "Mirror 酱准备就绪",
                                "CDK 验证完成！是否继续下一步？",
                                "CDK 会在本地加密存储，此 CDK 的有效期至 " + exp,
                                afterConfirm).show();
                    } else {
                        GuiPrefabs.Dialogs.createCommonDialog(app.body,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_SUCCESS_ALT, GuiPrefabs.COLOR_SUCCESS),
                                "Mirror 酱准备就绪",
                                "CDK 验证完成！以后下载资源会自动采用此 CDK",
                                "CDK 会在本地加密存储，此 CDK 的有效期至 " + exp,
                                null).show();
                    }

                    triggerReturnActionCallback(new ActionEvent(this, null));
                } catch (McQueryVersion.McException e) {
                    Logger.error("DownloadDialog", "Invalid CDK, " + e.getMessage());
                    GuiPrefabs.Dialogs.createErrorDialog(app.body, e).show();
                } catch (GeneralSecurityException e) {
                    Logger.error("DownloadDialog", "Failed to save CDK");
                    GuiPrefabs.Dialogs.createErrorDialog(app.body, e).show();
                }
            }
        }.start();
    }
}
