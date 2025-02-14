/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils.markdown;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.internal.TableBlockParser;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;


/** Extension for <a href="https://github.github.com/gfm/#tables-extension-">GFM tables</a> using "|" pipes.
 * This implementation is adapted from {@link org.commonmark.ext.gfm.tables.TablesExtension} class.
 */
public class TablesExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
    private TablesExtension() {
    }

    public static Extension create() {
        return new TablesExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new TableBlockParser.Factory());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(TableFxmlNodeRenderer::new);
    }
}
