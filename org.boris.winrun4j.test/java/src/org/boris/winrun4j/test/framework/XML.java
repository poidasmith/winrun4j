/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.framework;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class XML
{
    public static Document parse(String s) throws XMLException {
        try {
            return builder().parse(new InputSource(new StringReader(s)));
        } catch (Exception e) {
            throw new XMLException(e);
        }
    }

    public static Document parse(Reader reader) throws XMLException {
        try {
            return builder().parse(new InputSource(reader));
        } catch (Exception e) {
            throw new XMLException(e);
        }
    }

    public static Document parse(InputStream is) throws XMLException {
        try {
            return builder().parse(new InputSource(is));
        } catch (Exception e) {
            throw new XMLException(e);
        }
    }

    public static void toString(Node d, Writer w) throws XMLException {
        try {
            DOMSource ds = new DOMSource(d);
            StreamResult sr = new StreamResult(w);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer ser;
            ser = tf.newTransformer();
            ser.setOutputProperty(OutputKeys.INDENT, "yes");
            ser.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            ser.transform(ds, sr);
        } catch (Exception e) {
            throw new XMLException(e);
        }
    }

    public static String toString(Document d) throws XMLException {
        StringWriter sw = new StringWriter();
        toString(d, sw);
        return sw.toString();
    }

    public static String toString(Element e) throws XMLException {
        StringWriter sw = new StringWriter();
        toString(e, sw);
        return sw.toString();
    }

    public static Document newDocument() throws XMLException {
        return builder().newDocument();
    }

    private static DocumentBuilder builder() throws XMLException {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XMLException(e);
        }
    }
}
