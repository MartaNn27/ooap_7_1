import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

interface UndoableCommand {
    void execute();
    void undo();
}

class CutCommand implements UndoableCommand {
    private TextEditorApp editor;
    private String backup;
    private int selectionStart;
    private int selectionEnd;

    public CutCommand(TextEditorApp editor) {
        this.editor = editor;
    }

    @Override
    public void execute() {
        JTextArea textArea = editor.getTextArea();
        selectionStart = textArea.getSelectionStart();
        selectionEnd = textArea.getSelectionEnd();
        backup = textArea.getSelectedText();
        StringSelection selection = new StringSelection(backup);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
        textArea.replaceRange("", selectionStart, selectionEnd);
    }

    @Override
    public void undo() {
        JTextArea textArea = editor.getTextArea();
        textArea.insert(backup, selectionStart);
    }
}

class CopyCommand implements UndoableCommand {
    private TextEditorApp editor;

    public CopyCommand(TextEditorApp editor) {
        this.editor = editor;
    }

    @Override
    public void execute() {
        JTextArea textArea = editor.getTextArea();
        String selectedText = textArea.getSelectedText();
        if (selectedText != null) {
            StringSelection selection = new StringSelection(selectedText);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        }
    }

    @Override
    public void undo() {
        // Undo for CopyCommand is not applicable
    }
}

class PasteCommand implements UndoableCommand {
    private TextEditorApp editor;
    private String backup;
    private int caretPosition;

    public PasteCommand(TextEditorApp editor) {
        this.editor = editor;
    }

    @Override
    public void execute() {
        JTextArea textArea = editor.getTextArea();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                backup = (String) contents.getTransferData(DataFlavor.stringFlavor);
                caretPosition = textArea.getCaretPosition();
                textArea.insert(backup, caretPosition);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void undo() {
        JTextArea textArea = editor.getTextArea();
        textArea.replaceRange("", caretPosition, caretPosition + backup.length());
    }
}

class ItalicCommand implements UndoableCommand {
    private TextEditorApp editor;
    private int selectionStart;
    private int selectionEnd;

    public ItalicCommand(TextEditorApp editor) {
        this.editor = editor;
    }

    @Override
    public void execute() {
        JTextArea textArea = editor.getTextArea();
        selectionStart = textArea.getSelectionStart();
        selectionEnd = textArea.getSelectionEnd();
        if (selectionStart != selectionEnd) {
            Font font = textArea.getFont().deriveFont(Font.ITALIC);
            textArea.setFont(font);
        } else {
            JOptionPane.showMessageDialog(null, "No text selected!");
        }
    }

    @Override
    public void undo() {
        JTextArea textArea = editor.getTextArea();
        Font font = textArea.getFont().deriveFont(Font.PLAIN); // Revert to plain font
        textArea.setFont(font);
    }
}

class Invoker {
    private Stack<UndoableCommand> history;

    public Invoker() {
        history = new Stack<>();
    }

    public void storeAndExecute(UndoableCommand command) {
        command.execute();
        history.push(command);
    }

    public void undoLastCommand() {
        if (!history.isEmpty()) {
            UndoableCommand command = history.pop();
            command.undo();
        }
    }
}

class TextEditorApp {
    private JFrame frame;
    private JTextArea textArea;
    private Invoker invoker;

    public TextEditorApp() {
        frame = new JFrame("Simple Notepad Clone");
        textArea = new JTextArea();
        invoker = new Invoker();

        JButton cutButton = new JButton("Cut");
        cutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invoker.storeAndExecute(new CutCommand(TextEditorApp.this));
            }
        });

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invoker.storeAndExecute(new CopyCommand(TextEditorApp.this));
            }
        });

        JButton pasteButton = new JButton("Paste");
        pasteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invoker.storeAndExecute(new PasteCommand(TextEditorApp.this));
            }
        });

        JButton italicButton = new JButton("Italic");
        italicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invoker.storeAndExecute(new ItalicCommand(TextEditorApp.this));
            }
        });

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invoker.undoLastCommand();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cutButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(pasteButton);
        buttonPanel.add(italicButton);
        buttonPanel.add(undoButton);

        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public JTextArea getTextArea() {
        return textArea;
    }
}

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TextEditorApp();
            }
        });
    }
}


