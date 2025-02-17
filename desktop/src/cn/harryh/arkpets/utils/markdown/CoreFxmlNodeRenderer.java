/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils.markdown;

import org.commonmark.ext.gfm.tables.*;
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
    private final Map<TableBlock, RowColumnValue> rowColumnCount;
    private String lastHref;

    public CoreFxmlNodeRenderer(HtmlNodeRendererContext context) {
        this.context = context;
        this.writer = context.getWriter();
        this.textFlow = new TextFlowCoordinator(writer);
        this.listItemCount = new HashMap<>(4);
        this.rowColumnCount = new HashMap<>(4);
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
                SoftLineBreak.class, HardLineBreak.class, TableBlock.class,
                TableHead.class, TableBody.class, TableRow.class,
                TableCell.class
        );
    }

    @Override
    public void render(Node node) {
        if (node instanceof TableBlock tableBlock) {
            renderBlock(tableBlock);
        } else if (node instanceof TableHead tableHead) {
            renderHead(tableHead);
        } else if (node instanceof TableBody tableBody) {
            renderBody(tableBody);
        } else if (node instanceof TableRow tableRow) {
            renderRow(tableRow);
        } else if (node instanceof TableCell tableCell) {
            renderCell(tableCell);
        } else {
            node.accept(this);
        }
    }

    // REGION: BASIC NODES

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

    // REGION: TABLE EXTENSION

    protected void renderBlock(TableBlock tableBlock) {
        writer.tag("GridPane", FxmlPrefabs.TABLE.getAttrs());
        writer.line();

        RowColumnValue rc = new RowColumnValue();
        rowColumnCount.put(tableBlock, rc);

        visitChildren(tableBlock);

        double weightSum = rc.columnWeight.stream().mapToDouble(Double::doubleValue).sum();
        if (weightSum > 0.0) {
            writer.tag("columnConstraints");
            writer.line();
            rc.columnWeight.forEach(w -> {
                // Weight data is temporarily stored through the 'minWidth' attribute
                writer.tag("ColumnConstraints", Map.of(
                        "minWidth", "%.6f".formatted(w / weightSum)
                ), true);
                writer.line();
            });
            writer.tag("/columnConstraints");
            writer.line();
        }

        rowColumnCount.remove(tableBlock);

        writer.tag("/GridPane");
        writer.line();
    }

    protected void renderHead(TableHead tableHead) {
        visitChildren(tableHead);
    }

    protected void renderBody(TableBody tableBody) {
        visitChildren(tableBody);
    }

    protected void renderRow(TableRow tableRow) {
        if (tableRow.getParent().getParent() instanceof TableBlock tableBlock) {
            visitChildren(tableRow);
            rowColumnCount.get(tableBlock).increaseRow();
        } else {
            throw new RuntimeException("Illegal parent of table row");
        }
    }

    protected void renderCell(TableCell tableCell) {
        if (tableCell.getParent().getParent().getParent() instanceof TableBlock tableBlock) {
            writer.tag("VBox", getCellAttrs(tableCell));
            writer.line();

            visitChildren(tableCell);
            textFlow.closeTextFlow();

            writer.tag("/VBox");
            writer.line();

            RowColumnValue rc = rowColumnCount.get(tableBlock);
            rc.setColumnWeight(Math.sqrt(tableCell.getWidth() / ((getChildrenLength(tableCell) / 2.0 + 1))));
            rc.increaseColumn();
        } else {
            throw new RuntimeException("Illegal parent of table cell");
        }
    }

    // REGION: INTERNAL METHODS

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

    private int getChildrenLength(Node parent) {
        int count = 0;
        for (Node node = parent.getFirstChild(); node != null; node = node.getNext()) {
            count += 1;
        }
        return count;
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

    private Map<String, String> getCellAttrs(TableCell tableCell) {
        if (tableCell.getParent().getParent().getParent() instanceof TableBlock tableBlock) {
            RowColumnValue rc = rowColumnCount.get(tableBlock);
            Map<String, String> attrs = new HashMap<>(8);
            attrs.put("GridPane.rowIndex", String.valueOf(rc.row));
            attrs.put("GridPane.columnIndex", String.valueOf(rc.column));
            attrs.put("alignment", switch (tableCell.getAlignment()) {
                case LEFT -> "CENTER_LEFT";
                case CENTER -> "CENTER";
                case RIGHT -> "CENTER_RIGHT";
            });
            attrs.putAll(FxmlPrefabs.TABLE_CELL.getAttrs());
            return attrs;
        } else {
            throw new RuntimeException("Illegal parent of table cell");
        }
    }

    // REGION: INTERNAL CLASSES

    private static class RowColumnValue {
        private int row;
        private int column;
        private final ArrayList<Double> columnWeight;

        private RowColumnValue() {
            row = 0;
            column = 0;
            columnWeight = new ArrayList<>(8);
        }

        public void increaseRow() {
            row += 1;
            column = 0;
        }

        public void increaseColumn() {
            column += 1;
        }

        public void setColumnWeight(double weight) {
            if (column > columnWeight.size()) {
                throw new RuntimeException("Inconsistent column index");
            } else if (column == columnWeight.size()) {
                columnWeight.add(weight);
            } else {
                columnWeight.set(column, columnWeight.get(column) + weight);
            }
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
