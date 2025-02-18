package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.utils.GuiPrefabs;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import static cn.harryh.arkpets.Const.appName;
import static cn.harryh.arkpets.Const.durationFast;


public class Titlebar implements Controller<ArkHomeFX> {
    @FXML
    public Text titleText;
    @FXML
    public HBox macTitleButtons;
    @FXML
    public HBox titleButtons;
    @FXML
    public HBox title;
    @FXML
    public ImageView titleMacCloseImage;
    @FXML
    public ImageView titleMacMinimizeImage;

    public static String forceUiStyle = "";

    private ArkHomeFX app;
    private double xOffset;
    private double yOffset;
    private final Rectangle2D area = new Rectangle2D(0, 0, 16, 16);
    private final Rectangle2D hoverArea = new Rectangle2D(16, 0, 16, 16);
    private final Rectangle2D activeArea = new Rectangle2D(32, 0, 16, 16);
    private final Rectangle2D disableArea = new Rectangle2D(48, 0, 16, 16);
    private boolean inHBox;
    private boolean focused;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        if (forceUiStyle.equals("mac") || com.sun.jna.Platform.isMac()) {
            initMacTitlebar();
        } else if (forceUiStyle.equals("win") || com.sun.jna.Platform.isWindows()){

        }
    }

    @FXML
    public void titleBarPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    public void titleBarDragged(MouseEvent event) {
        app.stage.setX(event.getScreenX() - xOffset);
        app.stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    public void windowMinimize(MouseEvent event) {
        GuiPrefabs.fadeOutWindow(app.stage, durationFast, e -> {
            app.stage.hide();
            app.stage.setIconified(true);
        });
    }

    @FXML
    public void windowClose(MouseEvent event) {
        String solidExitTip = (app.config != null && app.config.launcher_solid_exit) ?
                "退出程序将会同时退出已启动的桌宠。" : "退出程序后已启动的桌宠将会保留。";
        GuiPrefabs.Dialogs.createConfirmDialog(app.body,
                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_HELP_ALT, GuiPrefabs.COLOR_INFO),
                "确认退出",
                "现在退出 " + appName + " 吗？",
                "根据您的设置，" + solidExitTip + "\n使用最小化 [-] 按钮可以隐藏窗口到系统托盘。",
                app.rootModule::exit).show();
    }

    @FXML
    public void mouseEnterBtnBoxMac() {
        if (focused) {
            titleMacCloseImage.setViewport(hoverArea);
            titleMacMinimizeImage.setViewport(hoverArea);
            inHBox = true;
        }
    }

    @FXML
    public void mouseExitBtnBoxMac() {
        if (focused) {
            titleMacCloseImage.setViewport(area);
            titleMacMinimizeImage.setViewport(area);
            inHBox = false;
        }
    }

    @FXML
    public void closePressedMac() {
        titleMacCloseImage.setViewport(activeArea);
    }

    @FXML
    public void closeReleasedMac() {
        if (inHBox) {
            titleMacCloseImage.setViewport(hoverArea);
        } else {
            titleMacCloseImage.setViewport(area);
        }
    }

    @FXML
    public void minimizePressedMac() {
        titleMacMinimizeImage.setViewport(activeArea);
    }

    @FXML
    public void minimizeReleasedMac() {
        if (inHBox) {
            titleMacMinimizeImage.setViewport(hoverArea);
        } else {
            titleMacMinimizeImage.setViewport(area);
        }
    }

    private void initMacTitlebar() {
        AnchorPane.setLeftAnchor(title, 52.0);
        macTitleButtons.setVisible(true);
        AnchorPane.setRightAnchor(titleButtons, null);
        titleButtons.setVisible(false);
        app.stage.focusedProperty().addListener((e, oldValue, newValue) -> {
            focused = newValue;
            if (!newValue) {
                titleMacCloseImage.setViewport(disableArea);
                titleMacMinimizeImage.setViewport(disableArea);
            } else {
                titleMacCloseImage.setViewport(area);
                titleMacMinimizeImage.setViewport(area);
            }
        });
    }
}
