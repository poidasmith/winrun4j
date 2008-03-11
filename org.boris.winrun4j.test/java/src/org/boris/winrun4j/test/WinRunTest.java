package org.boris.winrun4j.test;

import java.util.Iterator;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.boris.winrun4j.ActivationListener;
import org.boris.winrun4j.DDE;
import org.boris.winrun4j.EventLog;
import org.boris.winrun4j.FileAssociationListener;
import org.boris.winrun4j.INI;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.RegistryKey;

public class WinRunTest {
    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        StringBuffer ab = new StringBuffer();
        ab.append("WinRun4J");
        if (args.length > 0) {
            ab.append(" - ");
        }
        for (int i = 0; i < args.length; i++) {
            ab.append(args[i]);
            ab.append(" ");
        }
        frame.setTitle(ab.toString());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            sb.append("\n");
        }
        sb.append("\n\n");
        final JTextPane text = new JTextPane();
        Properties p = System.getProperties();
        for (Iterator i = p.keySet().iterator(); i.hasNext();) {
            String k = (String) i.next();
            sb.append(k);
            sb.append("=");
            sb.append(p.getProperty((String) k));
            sb.append("\n");
        }
        sb.append("\n\nINI Properties\n=============\n\n");
        p = INI.getProperties();
        for (Iterator i = p.keySet().iterator(); i.hasNext();) {
            String k = (String) i.next();
            sb.append(k);
            sb.append("=");
            sb.append(p.getProperty((String) k));
            sb.append("\n");
        }

        // Test logger
        Log.info("INFO: test1\n");
        Log.warning("WARN: test2\n");
        Log.error("ERR: test3\n");
        Log.setLastError("Last Error Test");
        sb.append("\nLast Error:" + Log.getLastError() + "\n");

        // Test event log
        EventLog.report("WinRun4J Test", EventLog.INFORMATION,
                "A test information log");

        // Test registry
        sb.append("\n\nRegistry Test\n=============\n\n");
        RegistryKey key = new RegistryKey(RegistryKey.HKEY_CURRENT_USER,
                "Control Panel\\Appearance\\Schemes");
        key.open();
        String[] names = key.getValueNames();
        for (int i = 0; i < names.length && i < 5; i++) {
            sb.append(names[i]);
            sb.append("\n");
        }
        key.close();

        text.setText(sb.toString());
        frame.getContentPane().add(new JScrollPane(text));
        frame.setSize(500, 500);
        frame.setLocation(30, 30);
        // Thread.sleep(5000); // for the splash screen
        frame.setVisible(true);
        System.out.println("Testing stdout stream redirection from Java");
        System.err.println("Testing stderr stream redirection from Java");

        // Add file association listener
        DDE.addFileAssocationListener(new FileAssociationListener() {
            public void execute(String cmdLine) {
                text.setText(cmdLine + "\n" + text.getText());
            }
        });

        DDE.addActivationListener(new ActivationListener() {
            public void activate() {
                text.setText("Activation occurred\n" + text.getText());
            }
        });
    }
}
