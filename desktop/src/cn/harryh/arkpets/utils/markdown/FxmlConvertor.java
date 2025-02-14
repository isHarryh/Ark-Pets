/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils.markdown;

import cn.harryh.arkpets.utils.IOUtils;
import javafx.fxml.FXMLLoader;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;


/** The helper class for Markdown-to-FXML document conversion.
 */
public class FxmlConvertor {
    /** Converts a Markdown document to an FXML document.
     * @param markdown The Markdown content.
     * @return The converted FXML content.
     */
    public static String toFxml(String markdown) {
        Parser parser = Parser.builder()
                .extensions(List.of(TablesExtension.create()))
                .build();
        org.commonmark.node.Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(List.of(TablesExtension.create()))
                .nodeRendererFactory(CoreFxmlNodeRenderer::new).build();
        return renderer.render(document);
    }

    /** Converts a Markdown document to an FXML document.
     * @param markdown The Markdown content.
     * @return The converted JavaFX Node that contains the given content.
     */
    public static javafx.scene.Node toFxmlVBox(String markdown) {
        String fxml = toFxml(markdown);
        try {
            // TODO Debug only
            IOUtils.FileUtil.writeString(new File("temp.fxml"), "UTF-8", fxml, false);
            FXMLLoader loader = new FXMLLoader();
            return loader.load(new ByteArrayInputStream(fxml.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
