/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils.markdown;

import java.util.Map;


/** The FXML document attribute prefabs for styling a common document.
 */
public enum FxmlPrefabs {
    DOCUMENT(Map.of(
            "xmlns:fx", "http://javafx.com/fxml/1",
            "fx:id", "body",
            "spacing", "7.5",
            "style", "-fx-font-size:13px;-fx-font-weight:normal;" +
                    "-fx-padding:10px;-fx-wrap-text:true;"
    )),
    H1(Map.of(
            "style", "-fx-font-size:21px;-fx-font-weight:bold;"
    )),
    H2(Map.of(
            "style", "-fx-font-size:19px;-fx-font-weight:bold;"
    )),
    H3(Map.of(
            "style", "-fx-font-size:17px;-fx-font-weight:bold;"
    )),
    H4(Map.of(
            "style", "-fx-font-size:15px;-fx-font-weight:bold;"
    )),
    BLOCK_QUOTE(Map.of(
            "spacing", "7.5",
            "style", "-fx-background-color:#2481;-fx-background-radius:2.5px;" +
                    "-fx-border-color:#2488;-fx-border-width:0 0 0 2.5px;-fx-border-radius:2.5px;" +
                    "-fx-padding:10px;"
    )),
    EMPHASIS(Map.of(
            "style", "-fx-font-weight:normal;-fx-font-style:oblique;"
    )),
    STRONG_EMPHASIS(Map.of(
            "style", "-fx-font-weight:bold;-fx-font-style:normal;"
    )),
    STRIKETHROUGH(Map.of(
            "style", "-fx-strikethrough:true;"
    )),
    TEXT(Map.of(
    )),
    CODE_BLOCK(Map.of(
            "editable", "false",
            "style", "-fx-background-color:#2481;-fx-background-radius:7.5px;" +
                    "-fx-border-color:#2484;-fx-border-width:1px;-fx-border-radius:7.5px;" +
                    "-fx-padding:10px;-fx-font-family:monospace;-fx-font-size:13px;"
    )),
    HYPERLINK(Map.of(
            "fill", "#248F",
            "underline", "true"
    )),
    LIST_BLOCK_OUTER(Map.of(
            "style", "-fx-padding:0 5px;"
    )),
    LIST_BLOCK_INNER(Map.of(
            "style", "-fx-padding:0 10px;"
    )),
    TABLE(Map.of(
            "style", "-fx-padding:10px 5px;-fx-hgap:0;-fx-vgap:0;"
    )),
    TABLE_CELL(Map.of(
            "style", "-fx-padding:5px 10px;" +
                    "-fx-border-color:#2482;-fx-border-width:1px;-fx-border-radius:0;"
    ));

    private final Map<String, String> attrsMap;

    FxmlPrefabs(Map<String, String> attrsMapMap) {
        this.attrsMap = attrsMapMap;
    }

    public Map<String, String> getAttrs() {
        return attrsMap;
    }
}
