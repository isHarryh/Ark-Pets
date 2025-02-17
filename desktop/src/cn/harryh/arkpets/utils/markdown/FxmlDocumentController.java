/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils.markdown;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;


/** The controller that the FXML document rendered by {@link FxmlConvertor} bind to.
 */
public final class FxmlDocumentController {
    @FXML
    private VBox body;

    private Consumer<String> hyperlinkConsumer;

    public FxmlDocumentController() {
        hyperlinkConsumer = null;
        Platform.runLater(() -> {
            setupWidthListener();
            updateAllCodeBlock(body);
        });
    }

    /** This method is used to bind to the {@code onMouseClicked} attribute in the rendered FXML document.
     */
    @FXML
    @SuppressWarnings("unused")
    private void handleHyperlinkClick(MouseEvent event) {
        if (event.getSource() instanceof Node node) {
            if (node.getUserData() instanceof String string) {
                if (hyperlinkConsumer != null) {
                    hyperlinkConsumer.accept(string);
                }
            }
        }
    }

    /** Sets the consumer that accepts the URL of the hyperlink the user clicked.
     * <p>
     * Remember to verify the security of the URL. The URL may be:
     * <ul>
     *     <li>Web "http://" link</li>
     *     <li>Web "https://" link</li>
     *     <li>Email "mailto:" link</li>
     *     <li>Relative file link</li>
     *     <li>Other unknown link</li>
     * </ul>
     * @param consumer A string consumer.
     */
    public void setHyperlinkConsumer(Consumer<String> consumer) {
        hyperlinkConsumer = consumer;
    }

    /** Gets the body node of the FXML document.
     * @return The body node.
     */
    public VBox getBodyNode() {
        return body;
    }

    private void setupWidthListener() {
        if (body != null) {
            body.maxWidthProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.doubleValue() > 0.0) {
                    updateAllGridPanes(body, newValue.doubleValue());
                }
            });
            if (body.getMaxWidth() > 0.0) {
                updateAllGridPanes(body, body.getMaxWidth());
            }
        }
    }

    private void updateAllGridPanes(Parent node, double containerWidth) {
        for (Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof GridPane gridPane) {
                // Update the `maxWidth` of all columns according to the container width
                for (ColumnConstraints column : gridPane.getColumnConstraints()) {
                    // Weight data is temporarily stored through the 'minWidth' attribute
                    double percentage = column.getMinWidth();
                    if (percentage > 0.0) {
                        column.setMaxWidth(containerWidth * percentage);
                    }
                }
            }
            if (child instanceof Parent parent) {
                updateAllGridPanes(parent, containerWidth);
            }
        }
    }

    private void updateAllCodeBlock(Parent node) {
        for (Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof TextArea textArea) {
                if (textArea.getUserData() instanceof String string) {
                    textArea.setText(new String(Base64.getDecoder().decode(string), StandardCharsets.UTF_8));
                }
            }
            if (child instanceof Parent parent) {
                updateAllCodeBlock(parent);
            }
        }
    }
}
