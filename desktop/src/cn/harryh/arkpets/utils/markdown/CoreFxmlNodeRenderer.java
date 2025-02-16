/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils.markdown;

import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.*;


/** The modded node renderer that renders all the core nodes to FXML used by JavaFX.
 * This implementation is adapted from {@link org.commonmark.renderer.html.HtmlRenderer} class.
 */
public class CoreFxmlNodeRenderer extends AbstractVisitor implements NodeRenderer {
    protected final HtmlNodeRendererContext context;
    private final HtmlWriter writer;
    private final TextFlowCoordinator textFlow;
    private final Map<ListBlock, Integer> listItemCount;
    private String lastHref;

    public CoreFxmlNodeRenderer(HtmlNodeRendererContext context) {
        this.context = context;
        this.writer = context.getWriter();
        this.textFlow = new TextFlowCoordinator(writer);
        this.listItemCount = new HashMap<>(4);
        this.lastHref = null;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(
                Document.class, Heading.class, Paragraph.class,
                BlockQuote.class, BulletList.class, FencedCodeBlock.class,
                HtmlBlock.class, ThematicBreak.class, IndentedCodeBlock.class,
                Link.class, ListItem.class, OrderedList.class,
                Image.class, Emphasis.class, StrongEmphasis.class,
                Text.class, Code.class, HtmlInline.class,
                SoftLineBreak.class, HardLineBreak.class
        );
    }

    @Override
    public void render(Node node) {
        node.accept(this);
    }

    @Override
    public void visit(Document document) {
        // FXML headers
        writer.raw(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<?import javafx.scene.control.*?>\n" +
                        "<?import javafx.scene.layout.*?>\n" +
                        "<?import javafx.scene.shape.*?>\n" +
                        "<?import javafx.scene.text.*?>\n" +
                        "<?import java.lang.*?>\n"
        );

        // Use VBox to wrap the whole document
        writer.tag("VBox", FxmlPrefabs.DOCUMENT.getAttrs());
        writer.line();

        visitChildren(document);

        writer.tag("/VBox");
        writer.line();
    }

    @Override
    public void visit(Heading heading) {
        textFlow.openTextFlow();

        textFlow.openText(switch (heading.getLevel()) {
            case 1 -> FxmlPrefabs.H1.getAttrs();
            case 2 -> FxmlPrefabs.H2.getAttrs();
            case 3 -> FxmlPrefabs.H3.getAttrs();
            default -> FxmlPrefabs.H4.getAttrs();
        });
        visitChildren(heading);

        textFlow.closeTextFlow();
    }

    @Override
    public void visit(Paragraph paragraph) {
        textFlow.openTextFlow();

        textFlow.openText(getHyperlinkAttachedAttrs(FxmlPrefabs.TEXT.getAttrs()));
        visitChildren(paragraph);

        textFlow.closeTextFlow();
    }

    @Override
    public void visit(BlockQuote blockQuote) {
        writer.tag("VBox", FxmlPrefabs.BLOCK_QUOTE.getAttrs());
        writer.line();

        visitChildren(blockQuote);

        writer.tag("/VBox");
        writer.line();
    }

    @Override
    public void visit(BulletList bulletList) {
        writer.tag("VBox");
        writer.line();

        visitChildren(bulletList);

        writer.tag("/VBox");
        writer.line();
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        String literal = fencedCodeBlock.getLiteral();
        Map<String, String> attrs = new LinkedHashMap<>();
        String info = fencedCodeBlock.getInfo();
        if (info != null && !info.isEmpty()) {
            int space = info.indexOf(" ");
            String language = space == -1 ? info : info.substring(0, space);
            attrs.put("class", "language-" + language);
        }
        renderCodeBlock(literal);
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        // TODO Not implemented
    }

    @Override
    public void visit(ThematicBreak thematicBreak) {
        writer.tag("Separator", Map.of(), true);
        writer.line();
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        writer.tag("VBox", FxmlPrefabs.BLOCK_QUOTE.getAttrs());
        writer.line();

        visitChildren(indentedCodeBlock);

        writer.tag("/VBox");
        writer.line();
    }

    @Override
    public void visit(Link link) {
        textFlow.openTextFlow();

        lastHref = link.getDestination();
        textFlow.openText(getHyperlinkAttachedAttrs(FxmlPrefabs.TEXT.getAttrs()));
        visitChildren(link);
        textFlow.closeText();
        lastHref = null;
    }

    @Override
    public void visit(ListItem listItem) {
        String prefix = "· ";
        if (listItem.getParent() instanceof OrderedList orderedList) {
            if (listItemCount.containsKey(orderedList)) {
                prefix = listItemCount.get(orderedList).toString() + ". ";
                listItemCount.put(orderedList, listItemCount.get(orderedList) + 1);
            }
        }
        writer.tag("HBox", FxmlPrefabs.LIST_BLOCK_OUTER.getAttrs());
        writer.line();

        writer.tag("Text", FxmlPrefabs.TEXT.getAttrs());
        writer.text(prefix);
        writer.tag("/Text");
        writer.line();

        writer.tag("VBox", FxmlPrefabs.LIST_BLOCK_INNER.getAttrs());
        writer.line();

        visitChildren(listItem);

        writer.line();
        writer.tag("/VBox");
        writer.line();

        writer.tag("/HBox");
        writer.line();
    }

    @Override
    public void visit(OrderedList orderedList) {
        renderListBlock(orderedList);
    }

    @Override
    public void visit(Image image) {
        // TODO Not implemented
    }

    @Override
    public void visit(Emphasis emphasis) {
        textFlow.openText(getHyperlinkAttachedAttrs(FxmlPrefabs.EMPHASIS.getAttrs()));
        visitChildren(emphasis);
        textFlow.closeText();
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        textFlow.openText(getHyperlinkAttachedAttrs(FxmlPrefabs.STRONG_EMPHASIS.getAttrs()));
        visitChildren(strongEmphasis);
        textFlow.closeText();
    }

    @Override
    public void visit(Text text) {
        if (!textFlow.isTextFlowContinuing()) {
            textFlow.openText(getHyperlinkAttachedAttrs(FxmlPrefabs.TEXT.getAttrs()));
        }
        writer.text(text.getLiteral());
    }

    @Override
    public void visit(Code code) {
        if (!textFlow.isTextFlowContinuing()) {
            textFlow.openText(getHyperlinkAttachedAttrs(FxmlPrefabs.TEXT.getAttrs()));
        }
        writer.text(code.getLiteral());
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        // TODO Not implemented
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        textFlow.closeTextFlow();
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        textFlow.closeTextFlow();
    }

    protected void visitChildren(Node parent) {
        Node next;
        for (Node node = parent.getFirstChild(); node != null; node = next) {
            next = node.getNext();
            this.context.render(node);
        }
    }

    private void renderCodeBlock(String literal) {
        writer.tag("VBox", FxmlPrefabs.CODE_BLOCK.getAttrs());
        writer.line();
        textFlow.openTextFlow();

        textFlow.openText(getHyperlinkAttachedAttrs(FxmlPrefabs.TEXT.getAttrs()));
        writer.text(literal);

        textFlow.closeTextFlow();
        writer.tag("/VBox");
        writer.line();
    }

    private void renderListBlock(ListBlock listBlock) {
        writer.tag("VBox", FxmlPrefabs.LIST_BLOCK_OUTER.getAttrs());
        writer.line();

        if (listBlock instanceof OrderedList orderedList) {
            int start = orderedList.getMarkerStartNumber() != null ? orderedList.getMarkerStartNumber() : 1;
            listItemCount.put(orderedList, start);
            visitChildren(orderedList);
            listItemCount.remove(orderedList);
        } else {
            visitChildren(listBlock);
        }

        writer.line();
        writer.tag("/VBox");
        writer.line();
    }

    private boolean isInTightList(Paragraph paragraph) {
        Node parent = paragraph.getParent();
        if (parent != null) {
            Node gramps = parent.getParent();
            if (gramps instanceof ListBlock) {
                ListBlock list = (ListBlock) gramps;
                return list.isTight();
            }
        }
        return false;
    }

    private Map<String, String> getAttrs(Node node, String tagName) {
        return getAttrs(node, tagName, Map.of());
    }

    private Map<String, String> getAttrs(Node node, String tagName, Map<String, String> defaultAttributes) {
        return context.extendAttributes(node, tagName, defaultAttributes);
    }

    private Map<String, String> getHyperlinkAttachedAttrs(Map<String, String> defaultAttrs) {
        if (lastHref != null && !lastHref.isBlank()) {
            Map<String, String> attrs = new HashMap<>(8);
            attrs.putAll(defaultAttrs);
            attrs.putAll(FxmlPrefabs.HYPERLINK.getAttrs());
            attrs.put("onMouseClicked", "#handleHyperlinkClick");
            attrs.put("userData", lastHref);
            return attrs;
        } else {
            return defaultAttrs;
        }
    }


    private static class AltTextVisitor extends AbstractVisitor {
        private final StringBuilder sb = new StringBuilder();

        private AltTextVisitor() {
        }

        String getAltText() {
            return sb.toString();
        }

        public void visit(Text text) {
            sb.append(text.getLiteral());
        }

        public void visit(SoftLineBreak softLineBreak) {
            sb.append('\n');
        }

        public void visit(HardLineBreak hardLineBreak) {
            sb.append('\n');
        }
    }
}
