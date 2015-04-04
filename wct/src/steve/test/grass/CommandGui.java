package steve.test.grass;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;


public class CommandGui extends JFrame {
    private JTextArea textArea;

    private void setGui() {
        try {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container cp = getContentPane();
            textArea = new JTextArea();
            cp.add(new JScrollPane(textArea), BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runCommand(String[] command) {
        CommandRunner runner = new CommandRunner();
        runner.runCommand(command);
    }

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        CommandGui f = new CommandGui();
                        f.setGui();
                        f.setSize(400, 400);
                        f.setVisible(true);

                        // PUT YOUR COMMAND HERE
                        String[] command = { "C:\\ncsu\\gis582\\project\\test.bat" };
                        f.runCommand(command);
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CommandRunner extends SwingWorker<Integer, String> {
        private Process process;
        private Integer result = -1;

        @Override
        public Integer doInBackground() {
            InputStreamReader in = null;
            InputStreamReader err = null;

            try {
                in = new InputStreamReader(process.getInputStream());
                err = new InputStreamReader(process.getErrorStream());

                char[] buf = new char[1 << 10]; // 1KiB buffer
                int numRead = -1;

                while ((numRead = in.read(buf)) > -1) {
                    publish(new String(buf, 0, numRead));
                }
                while ((numRead = err.read(buf)) > -1) {
                    publish(new String(buf, 0, numRead));
                }

                result = new Integer(process.waitFor());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) { /* ignore */
                }
            }

            return result;
        }

        @Override
        protected void process(java.util.List<String> chunks) {
            for (String text : chunks) {
                textArea.append(text);
            }
        }

        public void runCommand(String[] command) {
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                process = pb.start();
                execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
