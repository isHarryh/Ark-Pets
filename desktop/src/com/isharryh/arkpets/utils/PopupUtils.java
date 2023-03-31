/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.utils;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;


public class PopupUtils {
    public static final String COLOR_INFO = "#37B";
    public static final String COLOR_SUCCESS = "#5B5";
    public static final String COLOR_WARNING = "#E93";
    public static final String COLOR_DANGER = "#F54";
    public static final String COLOR_WHITE = "#FFF";
    public static final String COLOR_BLACK = "#000";
    public static final String COLOR_DARK_GRAY = "#222";
    public static final String COLOR_GRAY = "#444";
    public static final String COLOR_LIGHT_GRAY = "#666";

    public static class IconUtil {
        public static final String ICON_INFO = "m12 2c5.514 0 10 4.486 10 10s-4.486 10-10 10-10-4.486-10-10 4.486-10 10-10zm0-2c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-.001 5.75c.69 0 1.251.56 1.251 1.25s-.561 1.25-1.251 1.25-1.249-.56-1.249-1.25.559-1.25 1.249-1.25zm2.001 12.25h-4v-1c.484-.179 1-.201 1-.735v-4.467c0-.534-.516-.618-1-.797v-1h3v6.265c0 .535.517.558 1 .735v.999z";
        public static final String ICON_INFO_ALT = "m12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-.001 5.75c.69 0 1.251.56 1.251 1.25s-.561 1.25-1.251 1.25-1.249-.56-1.249-1.25.559-1.25 1.249-1.25zm2.001 12.25h-4v-1c.484-.179 1-.201 1-.735v-4.467c0-.534-.516-.618-1-.797v-1h3v6.265c0 .535.517.558 1 .735v.999z";
        public static final String ICON_SUCCESS = "m12 2c5.514 0 10 4.486 10 10s-4.486 10-10 10-10-4.486-10-10 4.486-10 10-10zm0-2c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm4.393 7.5l-5.643 5.784-2.644-2.506-1.856 1.858 4.5 4.364 7.5-7.643-1.857-1.857z";
        public static final String ICON_SUCCESS_ALT = "m12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-1.25 17.292l-4.5-4.364 1.857-1.858 2.643 2.506 5.643-5.784 1.857 1.857-7.5 7.643z";
        public static final String ICON_WARNING = "m12 5.177l8.631 15.823h-17.262l8.631-15.823zm0-4.177l-12 22h24l-12-22zm-1 9h2v6h-2v-6zm1 9.75c-.689 0-1.25-.56-1.25-1.25s.561-1.25 1.25-1.25 1.25.56 1.25 1.25-.561 1.25-1.25 1.25z";
        public static final String ICON_WARNING_ALT = "m12 1l-12 22h24l-12-22zm-1 8h2v7h-2v-7zm1 11.25c-.69 0-1.25-.56-1.25-1.25s.56-1.25 1.25-1.25 1.25.56 1.25 1.25-.56 1.25-1.25 1.25z";
        public static final String ICON_DANGER = "m16.142 2l5.858 5.858v8.284l-5.858 5.858h-8.284l-5.858-5.858v-8.284l5.858-5.858h8.284zm.829-2h-9.942l-7.029 7.029v9.941l7.029 7.03h9.941l7.03-7.029v-9.942l-7.029-7.029zm-8.482 16.992l3.518-3.568 3.554 3.521 1.431-1.43-3.566-3.523 3.535-3.568-1.431-1.432-3.539 3.583-3.581-3.457-1.418 1.418 3.585 3.473-3.507 3.566 1.419 1.417z";
        public static final String ICON_DANGER_ALT = "m16.971 0h-9.942l-7.029 7.029v9.941l7.029 7.03h9.941l7.03-7.029v-9.942l-7.029-7.029zm-1.402 16.945l-3.554-3.521-3.518 3.568-1.418-1.418 3.507-3.566-3.586-3.472 1.418-1.417 3.581 3.458 3.539-3.583 1.431 1.431-3.535 3.568 3.566 3.522-1.431 1.43z";

        /** Get a SVGPath Node using the given path string and color.
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
    }


    public static class DialogUtil {
        public static void disposeDialog(JFXDialog dialog, Pane root) {
            dialog.close();
            dialog.getDialogContainer().getChildren().remove(dialog);
            root.getChildren().remove(dialog.getDialogContainer());
            root.requestFocus();
        }

        public static JFXDialog createCenteredDialog(Pane root, boolean overlayClose) {
            StackPane container = new StackPane();
            container.setPrefSize(root.getWidth(), root.getHeight());
            root.getChildren().add(container);
            JFXDialog dialog = new JFXDialog();
            dialog.setDialogContainer(container);
            dialog.setOverlayClose(overlayClose);
            dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
            return dialog;
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
            h2.setStyle("-fx-font-size:16px;-fx-text-fill:" + COLOR_DARK_GRAY);
            h2.setWrapText(true);
            return h2;
        }

        public static Node getPrefabsH3(String text) {
            Label h3 = new Label(text);
            h3.setStyle("-fx-font-size:13px;-fx-min-height:36px;-fx-wrap-text:true;-fx-text-fill:" + COLOR_LIGHT_GRAY);
            h3.setWrapText(true);
            return h3;
        }

        public static JFXButton getCancelButton(JFXDialog dialog, Pane root) {
            JFXButton button = new JFXButton();
            button.setText("取 消");
            button.setTextFill(Paint.valueOf(COLOR_WHITE));
            button.setStyle("-fx-font-size:13px;-fx-text-fill:" + COLOR_WHITE + ";-fx-background-color:" + COLOR_INFO);
            button.setOnAction(e -> disposeDialog(dialog, root));
            return button;
        }

        public static JFXButton getOkayButton(JFXDialog dialog, Pane root) {
            JFXButton button = new JFXButton();
            button.setText("确 认");
            button.setTextFill(Paint.valueOf(COLOR_WHITE));
            button.setStyle("-fx-font-size:13px;-fx-text-fill:" + COLOR_WHITE + ";-fx-background-color:" + COLOR_INFO);
            button.setOnAction(e -> disposeDialog(dialog, root));
            return button;
        }

        public static JFXButton getTrustButton(JFXDialog dialog, Pane root) {
            JFXButton button = new JFXButton();
            button.setText("信 任");
            button.setStyle("-fx-font-size:13px;-fx-text-fill:" + COLOR_WHITE + ";-fx-background-color:" + COLOR_WARNING);
            button.setOnAction(e -> disposeDialog(dialog, root));
            return button;
        }
    }
}
