package com.maythiwat.engce125.lab1;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class EditorFrame extends JFrame {
    private JPanel mainPanel;
    private JTextArea textArea;
    private JButton addButton;
    private JLabel charCountLabel;
    private JButton removeButton;
    private JLabel detectedLabel;

    private volatile String textToCheck = "";
    private static final List<String> wordsList =
            new ArrayList<>(List.of("hello", "world", "java", "thread", "example"));

    public EditorFrame() {
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("WordLanna 2026");
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        pack();

        Font customFont = new Font("Anuphan", Font.PLAIN, 14);
        UIManager.put("OptionPane.font", new FontUIResource(customFont));
        UIManager.put("OptionPane.messageFont", new FontUIResource(customFont));
        UIManager.put("OptionPane.buttonFont", new FontUIResource(customFont));
        UIManager.put("Button.font", new FontUIResource(customFont));
        UIManager.put("Label.font", new FontUIResource(customFont));
        UIManager.put("TextField.font", new FontUIResource(customFont));

        Thread typeThread = new Thread(() -> {
            textArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    String text = textArea.getText();
                    charCountLabel.setText(text.length() + " Character" + (text.length() > 1 ? "s" : ""));
                    textToCheck = text;
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == '-') {
                        if (textArea.getSelectedText() != null) {
                            wordsList.remove(textArea.getSelectedText().trim());
                            e.consume();
                            detectWords(textArea.getText());
                        }
                    }else if (e.getKeyChar() == '+') {
                        if (textArea.getSelectedText() != null) {
                            if (!wordsList.contains(textArea.getSelectedText().trim())) {
                                wordsList.add(textArea.getSelectedText().trim());
                            }
                            e.consume();
                            detectWords(textArea.getText());
                        }
                    }
                }
            });
        });

        Thread checkThread = new Thread(() -> {
            new javax.swing.Timer(100, e -> {
                int found = detectWords(textToCheck);
                if (found > -1) {
                    detectedLabel.setText("Detected: " + found);
                    detectedLabel.setForeground(found > 0 ? Color.RED : Color.BLACK);
                }
            }).start();
        });

        Thread dictionaryThread = new Thread(() -> {
            addButton.addActionListener(e -> showWordDialog(true));
            removeButton.addActionListener(e -> showWordDialog(false));
        });

        typeThread.start();
        checkThread.start();
        dictionaryThread.start();
    }

    private int detectWords(String text) {
        if (textArea.getSelectionStart() != textArea.getSelectionEnd()) {
            return -1;
        }

        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();

        if (text == null || text.isEmpty()) return 0;

        int total = 0;
        String lower = text.toLowerCase();
        for (String w : wordsList) {
            String word = w.toLowerCase();
            int idx = 0;
            while ((idx = lower.indexOf(word, idx)) != -1) {
                total++;
                int start = idx;
                int end = idx + word.length();

                try {
                    int g = 220 - (total % 8) * 20;
                    highlighter.addHighlight(
                            start,
                            end,
                            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, g, 0))
                    );
                } catch (Exception _) {
                }

                idx += word.length();
            }
        }

        return total;
    }

    private void showWordDialog(boolean isAdd) {
        String show = String.join(", ", wordsList);
        String userInput = JOptionPane.showInputDialog(
                mainPanel,
                "<html><body><p style='width: 240px;'>Current word list:<br/><b>" + show + "</b><br/><br/>" +
                        "Please enter word to " + (isAdd ? "add" : "remove") + ":</p></body></html>",
                isAdd ? "Add new word" : "Remove word",
                JOptionPane.QUESTION_MESSAGE
        );

        if (userInput != null && !userInput.trim().isEmpty()) {
            String word = userInput.trim();

            if (isAdd) {
                if (!wordsList.contains(word)) {
                    wordsList.add(word);
                }
            } else {
                wordsList.remove(word);
            }

            showWordDialog(isAdd);
        }
    }
}
