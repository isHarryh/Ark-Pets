/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils.markdown;

import org.commonmark.ext.gfm.tables.*;
import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/** The modded node renderer that renders all the table nodes to FXML used by JavaFX.
 * This implementation is adapted from {@link org.commonmark.ext.gfm.tables.internal.TableHtmlNodeRenderer} class.
 */
public class TableFxmlNodeRenderer implements NodeRenderer {
    private final HtmlNodeRendererContext context;
    private final HtmlWriter writer;
    private final Map<TableBlock, RowColumnValue> rowColumnCount;

    public TableFxmlNodeRenderer(HtmlNodeRendererContext context) {
        this.context = context;
        this.writer = context.getWriter();
        this.rowColumnCount = new HashMap<>(4);
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(TableBlock.class, TableHead.class, TableBody.class, TableRow.class, TableCell.class);
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
        }
    }

    protected void renderBlock(TableBlock tableBlock) {
        writer.tag("GridPane", FxmlPrefabs.TABLE.getAttrs());
        writer.line();

        rowColumnCount.put(tableBlock, new RowColumnValue());
        renderChildren(tableBlock);
        rowColumnCount.remove(tableBlock);

        writer.tag("/GridPane");
        writer.line();
    }

    protected void renderHead(TableHead tableHead) {
        if (tableHead.getParent() instanceof TableBlock tableBlock) {
            renderChildren(tableHead);
            rowColumnCount.get(tableBlock).increaseRow();
        } else {
            throw new RuntimeException("Illegal parent of table head");
        }
    }

    protected void renderBody(TableBody tableBody) {
        if (tableBody.getParent() instanceof TableBlock tableBlock) {
            renderChildren(tableBody);
            rowColumnCount.get(tableBlock).increaseRow();
        } else {
            throw new RuntimeException("Illegal parent of table body");
        }
    }

    protected void renderRow(TableRow tableRow) {
        if (tableRow.getParent().getParent() instanceof TableBlock tableBlock) {
            renderChildren(tableRow);
            rowColumnCount.get(tableBlock).increaseRow();
        } else {
            throw new RuntimeException("Illegal parent of table row");
        }
    }

    protected void renderCell(TableCell tableCell) {
        if (tableCell.getParent().getParent().getParent() instanceof TableBlock tableBlock) {
            writer.tag("HBox", getCellAttrs(tableCell));
            writer.line();

            if (allChildrenAreText(tableCell)) {
                writer.tag("TextFlow");
                writer.tag("Text", FxmlPrefabs.TEXT.getAttrs());
                renderChildren(tableCell);
                writer.tag("/Text");
                writer.tag("/TextFlow");
            }

            writer.tag("/HBox");
            writer.line();
            rowColumnCount.get(tableBlock).increaseColumn();
        } else {
            throw new RuntimeException("Illegal parent of table cell");
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

    private void renderChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }

    private boolean allChildrenAreText(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            if (!(node instanceof Text ||
                    node instanceof Code ||
                    node instanceof Link ||
                    node instanceof HtmlInline ||
                    node instanceof Emphasis ||
                    node instanceof StrongEmphasis
            )) {
                return false;
            }
            node = next;
        }
        return true;
    }


    private static class RowColumnValue {
        private int row;
        private int column;

        private RowColumnValue() {
            row = 0;
            column = 0;
        }

        public void increaseRow() {
            row += 1;
            column = 0;
        }

        public void increaseColumn() {
            column += 1;
        }
    }
}
