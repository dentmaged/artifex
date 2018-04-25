package org.anchor.game.editor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;

public class LogRedirector implements Runnable {

    private JEditorPane log;
    private BufferedReader reader;

    private PrintStream output;
    private boolean error;

    private LogRedirector(JEditorPane editorPane, PipedOutputStream outputStream, PrintStream original) {
        this(editorPane, outputStream, original, false);
    }

    private LogRedirector(JEditorPane log, PipedOutputStream outputStream, PrintStream output, boolean error) {
        this.log = log;
        this.output = output;
        this.error = error;

        try {
            reader = new BufferedReader(new InputStreamReader(new PipedInputStream(outputStream)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                line = (error ? "[ERROR] " : "[INFO] ") + line;

                output.println(line);
                log.setText(log.getText() + line + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error redirecting output : " + e.getMessage());
        }
    }

    public static void redirect(JEditorPane editorPane) {
        redirectOut(editorPane);
        redirectErr(editorPane);
    }

    private static void redirectOut(JEditorPane editorPane) {
        PipedOutputStream outputStream = new PipedOutputStream();
        PrintStream out = System.out;
        System.setOut(new PrintStream(outputStream, true));

        new Thread(new LogRedirector(editorPane, outputStream, out)).start();
    }

    private static void redirectErr(JEditorPane editorPane) {
        PipedOutputStream outputStream = new PipedOutputStream();
        PrintStream err = System.err;
        System.setErr(new PrintStream(outputStream, true));

        new Thread(new LogRedirector(editorPane, outputStream, err, true)).start();
    }

}
