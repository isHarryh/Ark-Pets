/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils.markdown;

import org.commonmark.renderer.html.HtmlWriter;

import java.util.Map;


/** The coordinator providing methods for safely rendering the FXML element {@code TextFlow} and {@code Text}.
 */
public class TextFlowCoordinator {
    private final HtmlWriter writer;
    private TextFlowStatus status;

    public TextFlowCoordinator(HtmlWriter writer) {
        this.writer = writer;
        this.status = TextFlowStatus.CLOSED;
    }

    /** Opens a {@code TextFlow} element.
     */
    public void openTextFlow() {
        switch (status) {
            case OPENED -> {
                // Text flow already opened -> do nothing
            }
            case CONTINUING -> {
                // Active text remaining -> close text
                writer.tag("/Text");
                writer.line();
            }
            case CLOSED -> {
                // Text flow not opened yet -> open text flow
                writer.tag("TextFlow");
                writer.line();
            }
        }
        status = TextFlowStatus.OPENED;
    }

    /** Closes a {@code TextFlow} element.
     */
    public void closeTextFlow() {
        switch (status) {
            case OPENED -> {
                // Text flow opened -> close text flow
                writer.tag("/TextFlow");
                writer.line();
            }
            case CONTINUING -> {
                // Active text remaining -> close text and text flow
                writer.tag("/Text");
                writer.line();
                writer.tag("/TextFlow");
                writer.line();
            }
            case CLOSED -> {
                // Text flow already closed -> do nothing
            }
        }
        status = TextFlowStatus.CLOSED;
    }

    /** Opens (or reopens) a new {@code Text} element.
     * @param attrs The additional attributes for the {@code Text}.
     */
    public void openText(Map<String, String> attrs) {
        switch (status) {
            case OPENED -> {
                // Text flow opened -> open text
                writer.tag("Text", attrs);
            }
            case CONTINUING -> {
                // Active text remaining -> reopen text
                writer.tag("/Text");
                writer.line();
                writer.tag("Text", attrs);
            }
            case CLOSED -> {
                // Text flow closed -> open text flow and text
                writer.tag("TextFlow");
                writer.line();
                writer.tag("Text", attrs);
            }
        }
        status = TextFlowStatus.CONTINUING;
    }

    /** Closes a {@code Text} element.
     */
    public void closeText() {
        switch (status) {
            case OPENED, CLOSED -> {
                // No active text -> do nothing
            }
            case CONTINUING -> {
                // Active text remaining -> close text
                writer.tag("/Text");
                writer.line();
                status = TextFlowStatus.OPENED;
            }
        }
    }

    /** Checks whether the {@code Text} element is opened.
     * @return {@code true} if the {@code Text} element is opened.
     */
    public boolean isTextFlowContinuing() {
        return status == TextFlowStatus.CONTINUING;
    }


    private enum TextFlowStatus {
        OPENED,
        CONTINUING,
        CLOSED
    }
}
