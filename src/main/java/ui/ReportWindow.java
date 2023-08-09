package ui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;

public class ReportWindow extends JFrame {
    private JTextArea textArea;

    public ReportWindow() {
        setTitle("Report");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        textArea = new JTextArea();
        textArea.setEditable(false);
        // Set the left margin to 20 pixels
        textArea.setMargin(new Insets(0, 20, 0, 0));
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addText(String text) {
        // Define a maximum number of lines
        final int maxLines = 50000;

        // Add the new text
        textArea.append(text + "\n");

        // Check if the number of lines has exceeded the maximum
        int lineCount = textArea.getLineCount();
        if (lineCount > maxLines) {
            // Determine how many lines to remove
            int linesToRemove = lineCount - maxLines;

            // Determine the offset of the lines to remove
            try {
                int endOffset = textArea.getLineStartOffset(linesToRemove);

                // Remove the oldest lines
                textArea.getDocument().remove(0, endOffset);
            } catch (BadLocationException e) {
                // Handle exception if needed
            }
        }
    }
}

