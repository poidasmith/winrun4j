package org.boris.winrun4j.test;


import java.util.Iterator;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;


public class WinRunTest {
    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        StringBuffer ab = new StringBuffer();
        ab.append("WinRun4J");
        if(args.length > 0) {
            ab.append(" - ");
        }
        for(int i= 0; i < args.length; i++) {
            ab.append(args[i]);
            ab.append(" ");
        }
        frame.setTitle(ab.toString());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        StringBuffer sb = new StringBuffer();
        JTextPane text = new JTextPane();
        Properties p = System.getProperties();
        for(Iterator i = p.keySet().iterator(); i.hasNext(); ) {
            String k = (String) i.next();
            sb.append(k);
            sb.append("=");
            sb.append(p.getProperty((String) k));
            sb.append("\n");
        }
        sb.append("\n\nINI Properties\n=============\n\n");
        for(Iterator i = p.keySet().iterator(); i.hasNext(); ) {
            String k = (String) i.next();
            sb.append(k);
            sb.append("=");
            sb.append(p.getProperty((String) k));
            sb.append("\n");
        }
        text.setText(sb.toString());
        frame.getContentPane().add(new JScrollPane(text));
        frame.setSize(500, 500);
        frame.setLocation(30, 30);
        Thread.sleep(5000); // for the splash screen
        frame.show();
        System.out.println("Testing stdout stream redirection from Java");
        System.err.println("Testing stderr stream redirection from Java");
    }
}
