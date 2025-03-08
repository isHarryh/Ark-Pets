/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils.markdown;

import javafx.fxml.FXMLLoader;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;


/** The helper class for Markdown-to-FXML document conversion.
 */
public class FxmlConvertor {
    /** Converts a Markdown document to an FXML document.
     * @param markdown The Markdown content.
     * @return The converted FXML content.
     */
    public static String toFxmlString(String markdown) {
        Parser parser = Parser.builder()
                .extensions(List.of(
                        AutolinkExtension.create(),
                        StrikethroughExtension.create(),
                        TablesExtension.create()
                ))
                .build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder()
                .nodeRendererFactory(CoreFxmlNodeRenderer::new).build();
        return renderer.render(document);
    }

    /** Converts a Markdown document to an FXML document.
     * @param markdown The Markdown content.
     * @return The {@link FxmlDocumentController} that bound with the converted FXML document.
     */
    public static FxmlDocumentController toFxmlController(String markdown) {
        String fxml = toFxmlString(markdown);
        try {
            FxmlDocumentController controller = new FxmlDocumentController();
            FXMLLoader loader = new FXMLLoader();
            loader.setClassLoader(FxmlConvertor.class.getClassLoader());
            loader.setController(controller);
            loader.load(new ByteArrayInputStream(fxml.getBytes()));
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
