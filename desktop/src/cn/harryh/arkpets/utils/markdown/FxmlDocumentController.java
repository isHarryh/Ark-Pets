/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils.markdown;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;


/** The controller that the FXML document rendered by {@link FxmlConvertor} bind to.
 */
public final class FxmlDocumentController {
    @FXML
    private VBox body;

    private Consumer<String> hyperlinkConsumer;

    public FxmlDocumentController() {
        hyperlinkConsumer = null;
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
}
