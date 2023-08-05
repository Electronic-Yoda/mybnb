package tool;

import javax.swing.*;
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
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addText(String text) {
        textArea.append(text + "\n");
    }
}

