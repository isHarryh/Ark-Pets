/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.concurrent.ProcessPool;
import com.jfoenix.controls.*;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.stage.Window;
import javafx.util.Duration;

import javax.net.ssl.SSLException;
import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipException;

import static cn.harryh.arkpets.Const.durationNormal;


@SuppressWarnings("unused")
public class GuiPrefabs {
    public static final String tooltipStyle = "-fx-text-fill:#FFF;-fx-font-size:10px;-fx-font-weight:normal;";


    public static class Colors {
        public static final String COLOR_INFO       = "#37B";
        public static final String COLOR_SUCCESS    = "#5B5";
        public static final String COLOR_WARNING    = "#E93";
        public static final String COLOR_DANGER     = "#F54";
        public static final String COLOR_WHITE      = "#FFF";
        public static final String COLOR_BLACK      = "#000";
        public static final String COLOR_DARK_GRAY  = "#222";
        public static final String COLOR_GRAY       = "#444";
        public static final String COLOR_LIGHT_GRAY = "#666";

        private Colors() {
        }
    }


    public static void fadeInNode(Node node, Duration duration, EventHandler<ActionEvent> onFinished) {
        FadeTransition fadeT = new FadeTransition(duration, node);
        node.setVisible(true);
        if (onFinished != null)
            fadeT.setOnFinished(onFinished);
        fadeT.setFromValue(0);
        fadeT.setToValue(1);
        fadeT.playFromStart();
    }

    public static void fadeOutNode(Node node, Duration duration, EventHandler<ActionEvent> onFinished) {
        FadeTransition fadeT = new FadeTransition(duration, node);
        fadeT.setOnFinished(e -> {
            node.setVisible(false);
            if (onFinished != null)
                onFinished.handle(e);
        });
        fadeT.setFromValue(1);
        fadeT.setToValue(0);
        fadeT.playFromStart();
    }

    public static void fadeInWindow(Window window, Duration duration, EventHandler<ActionEvent> onFinished) {
        if (window.opacityProperty().getValue() != 0 && window.opacityProperty().getValue() != 1)
            return;
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().setAll(new KeyFrame(Duration.ZERO, new KeyValue(window.opacityProperty(), 0)),
                new KeyFrame(duration, new KeyValue(window.opacityProperty(), 1)));
        timeline.setOnFinished(onFinished);
        timeline.playFromStart();
    }

    public static void fadeOutWindow(Window window, Duration duration, EventHandler<ActionEvent> onFinished) {
        if (window.opacityProperty().getValue() != 0 && window.opacityProperty().getValue() != 1)
            return;
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().setAll(new KeyFrame(Duration.ZERO, new KeyValue(window.opacityProperty(), 1)),
                new KeyFrame(duration, new KeyValue(window.opacityProperty(), 0)));
        timeline.setOnFinished(onFinished);
        timeline.playFromStart();
    }

    public static void replaceStyleClass(Node node, String from, String to) {
        HashSet<String> styleClass = new HashSet<>(Set.copyOf(node.getStyleClass()));
        styleClass.remove(from);
        styleClass.add(to);
        node.getStyleClass().setAll(styleClass);
    }


    public static class Icons {
        public static final String ICON_INFO        = "m12 2c5.514 0 10 4.486 10 10s-4.486 10-10 10-10-4.486-10-10 4.486-10 10-10zm0-2c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-.001 5.75c.69 0 1.251.56 1.251 1.25s-.561 1.25-1.251 1.25-1.249-.56-1.249-1.25.559-1.25 1.249-1.25zm2.001 12.25h-4v-1c.484-.179 1-.201 1-.735v-4.467c0-.534-.516-.618-1-.797v-1h3v6.265c0 .535.517.558 1 .735v.999z";
        public static final String ICON_INFO_ALT    = "m12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-.001 5.75c.69 0 1.251.56 1.251 1.25s-.561 1.25-1.251 1.25-1.249-.56-1.249-1.25.559-1.25 1.249-1.25zm2.001 12.25h-4v-1c.484-.179 1-.201 1-.735v-4.467c0-.534-.516-.618-1-.797v-1h3v6.265c0 .535.517.558 1 .735v.999z";
        public static final String ICON_HELP        = "m12 2c5.514 0 10 4.486 10 10s-4.486 10-10 10-10-4.486-10-10 4.486-10 10-10zm0-2c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm1.25 17c0 .69-.559 1.25-1.25 1.25-.689 0-1.25-.56-1.25-1.25s.561-1.25 1.25-1.25c.691 0 1.25.56 1.25 1.25zm1.393-9.998c-.608-.616-1.515-.955-2.551-.955-2.18 0-3.59 1.55-3.59 3.95h2.011c0-1.486.829-2.013 1.538-2.013.634 0 1.307.421 1.364 1.226.062.847-.39 1.277-.962 1.821-1.412 1.343-1.438 1.993-1.432 3.468h2.005c-.013-.664.03-1.203.935-2.178.677-.73 1.519-1.638 1.536-3.022.011-.924-.284-1.719-.854-2.297z";
        public static final String ICON_HELP_ALT    = "m12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm0 18.25c-.69 0-1.25-.56-1.25-1.25s.56-1.25 1.25-1.25c.691 0 1.25.56 1.25 1.25s-.559 1.25-1.25 1.25zm1.961-5.928c-.904.975-.947 1.514-.935 2.178h-2.005c-.007-1.475.02-2.125 1.431-3.468.573-.544 1.025-.975.962-1.821-.058-.805-.73-1.226-1.365-1.226-.709 0-1.538.527-1.538 2.013h-2.01c0-2.4 1.409-3.95 3.59-3.95 1.036 0 1.942.339 2.55.955.57.578.865 1.372.854 2.298-.016 1.383-.857 2.291-1.534 3.021z";
        public static final String ICON_SUCCESS     = "m12 2c5.514 0 10 4.486 10 10s-4.486 10-10 10-10-4.486-10-10 4.486-10 10-10zm0-2c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm4.393 7.5l-5.643 5.784-2.644-2.506-1.856 1.858 4.5 4.364 7.5-7.643-1.857-1.857z";
        public static final String ICON_SUCCESS_ALT = "m12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-1.25 17.292l-4.5-4.364 1.857-1.858 2.643 2.506 5.643-5.784 1.857 1.857-7.5 7.643z";
        public static final String ICON_WARNING     = "m12 5.177l8.631 15.823h-17.262l8.631-15.823zm0-4.177l-12 22h24l-12-22zm-1 9h2v6h-2v-6zm1 9.75c-.689 0-1.25-.56-1.25-1.25s.561-1.25 1.25-1.25 1.25.56 1.25 1.25-.561 1.25-1.25 1.25z";
        public static final String ICON_WARNING_ALT = "m12 1l-12 22h24l-12-22zm-1 8h2v7h-2v-7zm1 11.25c-.69 0-1.25-.56-1.25-1.25s.56-1.25 1.25-1.25 1.25.56 1.25 1.25-.56 1.25-1.25 1.25z";
        public static final String ICON_DANGER      = "m16.142 2l5.858 5.858v8.284l-5.858 5.858h-8.284l-5.858-5.858v-8.284l5.858-5.858h8.284zm.829-2h-9.942l-7.029 7.029v9.941l7.029 7.03h9.941l7.03-7.029v-9.942l-7.029-7.029zm-8.482 16.992l3.518-3.568 3.554 3.521 1.431-1.43-3.566-3.523 3.535-3.568-1.431-1.432-3.539 3.583-3.581-3.457-1.418 1.418 3.585 3.473-3.507 3.566 1.419 1.417z";
        public static final String ICON_DANGER_ALT  = "m16.971 0h-9.942l-7.029 7.029v9.941l7.029 7.03h9.941l7.03-7.029v-9.942l-7.029-7.029zm-1.402 16.945l-3.554-3.521-3.518 3.568-1.418-1.418 3.507-3.566-3.586-3.472 1.418-1.417 3.581 3.458 3.539-3.583 1.431 1.431-3.535 3.568 3.566 3.522-1.431 1.43z";
        public static final String ICON_UPDATE      = "m12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm1 15.889v-2.223s-3.78-.114-7 3.333c1.513-6.587 7-7.778 7-7.778v-2.221l5 4.425-5 4.464z";

        /** Gets an SVGPath Node using the given path string and color.
         * @param svg The SVG path string.
         * @param color The specified color string, e.g.#FFFFFF.
         * @return JavaFX SVGPath Node.
         */
        public static SVGPath getIcon(String svg, String color) {
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(svg);
            svgPath.setFill(Paint.valueOf(color));
            return svgPath;
        }

        private Icons() {
        }
    }


    public static class DialogUtil {
        public static void disposeDialog(JFXDialog dialog, StackPane root) {
            if (dialog == null || root == null)
                return;
            if (dialog.isVisible())
                dialog.setOnDialogClosed(e -> root.requestFocus());
            dialog.close();
        }

        public static JFXDialog createCenteredDialog(StackPane root, boolean overlayClose) {
            JFXDialog dialog = new JFXDialog();
            dialog.setDialogContainer(root);
            dialog.setOverlayClose(overlayClose);
            dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
            return dialog;
        }

        public static JFXDialog createCommonDialog(StackPane root, Node graphic, String title, String header, String content, String detail) {
            JFXDialog dialog = DialogUtil.createCenteredDialog(root, false);
            VBox body = new VBox();
            Label h2 = (Label)DialogUtil.getPrefabsH2(header);
            Label h3 = (Label)DialogUtil.getPrefabsH3(content);
            body.setSpacing(5);
            body.getChildren().add(h2);
            body.getChildren().add(new Separator());
            body.getChildren().add(h3);

            JFXDialogLayout layout = new JFXDialogLayout();
            layout.setHeading(DialogUtil.getHeading(graphic, title, Colors.COLOR_LIGHT_GRAY));
            layout.setBody(body);
            layout.setActions(DialogUtil.getOkayButton(dialog, root));
            dialog.setContent(layout);

            if (detail != null && !detail.isEmpty()) {
                JFXTextArea textArea = new JFXTextArea();
                textArea.setEditable(false);
                textArea.setScrollTop(0);
                textArea.getStyleClass().add("popup-detail-field");
                textArea.appendText(detail);
                body.getChildren().add(textArea);
            }
            return dialog;
        }

        public static JFXDialog createConfirmDialog(StackPane root, Node graphic, String title, String header, String content, Runnable onConfirmed) {
            JFXDialog dialog = DialogUtil.createCenteredDialog(root, true);
            VBox body = new VBox();
            Label h2 = (Label)DialogUtil.getPrefabsH2(header);
            Label h3 = (Label)DialogUtil.getPrefabsH3(content);
            body.setSpacing(5);
            body.getChildren().add(h2);
            body.getChildren().add(new Separator());
            body.getChildren().add(h3);

            JFXDialogLayout layout = new JFXDialogLayout();
            layout.setHeading(DialogUtil.getHeading(graphic, title, Colors.COLOR_LIGHT_GRAY));
            layout.setBody(body);
            JFXButton confirmButton = getOkayButton(dialog, root);
            JFXButton cancelButton = getCancelButton(dialog, root);
            confirmButton.setOnAction(e -> {
                dialog.setOnDialogClosed(ev -> onConfirmed.run());
                dialog.close();
            });
            layout.setActions(cancelButton, confirmButton);
            dialog.setContent(layout);
            return dialog;
        }

        public static JFXDialog createErrorDialog(StackPane root, Throwable e) {
            JFXDialog dialog = DialogUtil.createCenteredDialog(root, false);

            VBox content = new VBox();
            Label h2 = (Label)DialogUtil.getPrefabsH2("啊哦~ ArkPets启动器抛出了一个异常。");
            Label h3 = (Label)DialogUtil.getPrefabsH3("请重试操作，或查看帮助文档与日志。如需联系开发者，请提供下述信息：");
            content.setSpacing(5);
            content.getChildren().add(h2);
            content.getChildren().add(new Separator());
            content.getChildren().add(h3);

            JFXTextArea textArea = new JFXTextArea();
            textArea.setEditable(false);
            textArea.setScrollTop(0);
            textArea.getStyleClass().add("popup-detail-field");
            textArea.appendText("[Exception] " + e.getClass().getSimpleName() + "\n");
            textArea.appendText("[Message] " + (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "") + "\n");
            textArea.appendText("\n[StackTrace]\nCaused by " + e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n");
            for (StackTraceElement ste : e.getStackTrace())
                textArea.appendText("  at " + ste + "\n");
            content.getChildren().add(textArea);

            JFXDialogLayout layout = new JFXDialogLayout();
            layout.setHeading(DialogUtil.getHeading(Icons.getIcon(Icons.ICON_DANGER, Colors.COLOR_DANGER), "发生异常", Colors.COLOR_DANGER));
            layout.setBody(content);
            layout.setActions(DialogUtil.getOkayButton(dialog, root));
            dialog.setContent(layout);

            if (e instanceof ProcessPool.UnexpectedExitCodeException) {
                h2.setText("检测到桌宠异常退出");
                h3.setText("桌宠运行时异常退出。如果该现象是在启动后立即发生的，可能是因为暂不支持该模型。您可以稍后重试或查看日志文件。");
            } else if (e instanceof FileNotFoundException) {
                h3.setText("未找到某个文件或目录，请稍后重试。详细信息：");
            } else if (e instanceof NetUtils.HttpResponseCodeException ex) {
                h2.setText("神经递质接收异常");
                switch (ex.getType()) {
                    case REDIRECTION -> h3.setText("请求的网络地址被重定向转移。详细信息：");
                    case CLIENT_ERROR -> {
                        h3.setText("可能是客户端引发的网络错误，详细信息：");
                        switch (ex.getCode()) {
                            case 403 -> h3.setText("(403)访问被拒绝。详细信息：");
                            case 404 -> h3.setText("(404)找不到要访问的目标。详细信息：");
                        }
                    }
                    case SERVER_ERROR -> {
                        h3.setText("可能是服务器引发的网络错误，详细信息：");
                        switch (ex.getCode()) {
                            case 500 -> h3.setText("(500)服务器内部故障，请稍后重试。详细信息：");
                            case 502 -> h3.setText("(502)服务器网关故障，请稍后重试。详细信息：");
                        }
                    }
                }
            } else if (e instanceof UnknownHostException) {
                h2.setText("无法建立神经连接");
                h3.setText("找不到服务器地址。可能是因为网络未连接或DNS解析失败，请尝试更换网络环境、检查防火墙和代理设置。");
            } else if (e instanceof ConnectException) {
                h2.setText("无法建立神经连接");
                h3.setText("在建立连接时发生了问题。请尝试更换网络环境、检查防火墙和代理设置。");
            } else if (e instanceof SocketException) {
                h2.setText("无法建立神经连接");
                h3.setText("在访问套接字时发生了问题。请尝试更换网络环境、检查防火墙和代理设置。");
            } else if (e instanceof SocketTimeoutException) {
                h2.setText("神经递质接收异常");
                h3.setText("接收数据超时。请尝试更换网络环境、检查防火墙和代理设置。");
            } else if (e instanceof SSLException) {
                h2.setText("无法建立安全的神经连接");
                h3.setText("SSL证书错误，请检查代理设置。您也可以尝试[信任]所有证书后重试刚才的操作。");
                JFXButton apply = DialogUtil.getTrustButton(dialog, root);
                apply.setOnAction(ev -> {
                    Const.isHttpsTrustAll = true;
                    DialogUtil.disposeDialog(dialog, root);
                });
                DialogUtil.attachAction(dialog, apply, 0);
            } else if (e instanceof ZipException) {
                h3.setText("压缩文件相关错误。可能是文件不完整或已损坏，请稍后重试。");
            }
            return dialog;
        }

        public static void attachAction(JFXDialog dialog, Node action, int index) {
            ObservableList<Node> actionList = ((JFXDialogLayout)dialog.getContent()).getActions();
            if (index < 0)
                actionList.add(action);
            else
                actionList.add(index, action);
        }

        public static Node getHeading(Node graphic, String text, String color) {
            Label label = new Label(text);
            label.setGraphic(graphic);
            label.setGraphicTextGap(5);
            label.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + color);
            return label;
        }

        public static Node getPrefabsH2(String text) {
            Label h2 = new Label(text);
            h2.setStyle("-fx-font-size:16px;-fx-text-fill:" + Colors.COLOR_DARK_GRAY);
            h2.setWrapText(true);
            return h2;
        }

        public static Node getPrefabsH3(String text) {
            Label h3 = new Label(text);
            h3.setStyle("-fx-font-size:12px;-fx-min-height:38px;-fx-wrap-text:true;-fx-text-fill:" + Colors.COLOR_LIGHT_GRAY);
            h3.setWrapText(true);
            return h3;
        }

        public static JFXButton getCancelButton(JFXDialog dialog, StackPane root) {
            JFXButton button = new JFXButton();
            button.setText("取 消");
            button.setTextFill(Paint.valueOf(Colors.COLOR_WHITE));
            button.setStyle("-fx-font-size:13px;-fx-text-fill:" + Colors.COLOR_WHITE + ";-fx-background-color:" + Colors.COLOR_INFO);
            button.setOnAction(e -> disposeDialog(dialog, root));
            return button;
        }

        public static JFXButton getOkayButton(JFXDialog dialog, StackPane root) {
            JFXButton button = new JFXButton();
            button.setText("确 认");
            button.setTextFill(Paint.valueOf(Colors.COLOR_WHITE));
            button.setStyle("-fx-font-size:13px;-fx-text-fill:" + Colors.COLOR_WHITE + ";-fx-background-color:" + Colors.COLOR_INFO);
            button.setOnAction(e -> disposeDialog(dialog, root));
            return button;
        }

        public static JFXButton getGotoButton(JFXDialog dialog, StackPane root) {
            JFXButton button = new JFXButton();
            button.setText("前 往");
            button.setStyle("-fx-font-size:13px;-fx-text-fill:" + Colors.COLOR_WHITE + ";-fx-background-color:" + Colors.COLOR_SUCCESS);
            button.setOnAction(e -> disposeDialog(dialog, root));
            return button;
        }

        public static JFXButton getTrustButton(JFXDialog dialog, StackPane root) {
            JFXButton button = new JFXButton();
            button.setText("信 任");
            button.setStyle("-fx-font-size:13px;-fx-text-fill:" + Colors.COLOR_WHITE + ";-fx-background-color:" + Colors.COLOR_WARNING);
            button.setOnAction(e -> disposeDialog(dialog, root));
            return button;
        }
    }


    /** @since ArkPets 3.0
     */
    public static class PeerNodeComposer {
        protected int activatedId;
        protected final HashMap<Integer, PeerNodeData> peerNodeMap;

        public PeerNodeComposer() {
            activatedId = -1;
            peerNodeMap = new HashMap<>();
        }

        protected void add(int id, PeerNodeData nodeData) {
            if (id < 0)
                throw new IllegalArgumentException("ID can't be negative.");
            if (peerNodeMap.containsKey(id))
                throw new IllegalStateException("ID already existed.");
            peerNodeMap.put(id, nodeData);
        }

        public void add(int id, Node... nodes) {
            add(id, new PeerNodeData(nodes, Event::consume, Event::consume));
        }

        public void add(int id, EventHandler<ActionEvent> onActivating, EventHandler<ActionEvent> onSuppressing, Node... nodes) {
            add(id, new PeerNodeData(nodes, onActivating, onSuppressing));
        }

        public void activate(int id) {
            if (!peerNodeMap.containsKey(id))
                throw new IllegalStateException("ID not found");
            peerNodeMap.keySet().forEach(i -> {
                if (i == id)
                    peerNodeMap.get(i).handleActivating();
                else
                    peerNodeMap.get(i).handleSuppressing();
            });
            activatedId = id;
        }

        public void toggle(int id, int replacementId) {
            if (!peerNodeMap.containsKey(id) || !peerNodeMap.containsKey(replacementId))
                throw new IllegalStateException("ID not found");
            activate(activatedId == id ? replacementId : id);
        }

        public int getActivatedId() {
            return activatedId;
        }

        protected record PeerNodeData(Node[] nodes, EventHandler<ActionEvent> onActivating, EventHandler<ActionEvent> onSuppressing) {
            public void handleActivating() {
                for (Node node : nodes)
                    activateNode(node);
                if (onActivating != null)
                    onActivating.handle(new ActionEvent(this, Event.NULL_SOURCE_TARGET));
                if (nodes.length > 0 && nodes[0] != null)
                    nodes[0].requestFocus();
            }

            public void handleSuppressing() {
                for (Node node : nodes)
                    suppressNode(node);
                if (onSuppressing != null)
                    onSuppressing.handle(new ActionEvent(this, Event.NULL_SOURCE_TARGET));
            }

            private void activateNode(Node node) {
                node.setManaged(true);
                fadeInNode(node, durationNormal, null);
            }

            private void suppressNode(Node node) {
                node.setVisible(false);
                node.setManaged(false);
            }
        }
    }
}
