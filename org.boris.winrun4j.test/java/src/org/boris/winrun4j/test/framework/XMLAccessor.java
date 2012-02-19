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

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLAccessor
{
    private Document doc;
    private Node base;

    public static XMLAccessor parse(Reader r) throws XMLException {
        return new XMLAccessor(XML.parse(r));
    }

    public static XMLAccessor parse(InputStream is) throws XMLException {
        return new XMLAccessor(XML.parse(is));
    }

    public XMLAccessor(Document doc) {
        this.doc = doc;
        this.base = doc.getDocumentElement();
    }

    public Document getDocument() {
        return doc;
    }

    public void setBase(Node base) {
        if (base != null && base.getOwnerDocument() == doc)
            this.base = base;
    }

    public Node getBase() {
        return base;
    }

    public Node getNode(String xpath) throws XMLException {
        try {
            return (Node) evaluate(xpath, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new XMLException(e);
        }
    }

    public NodeList getNodes(String xpath) throws XMLException {
        try {
            return (NodeList) evaluate(xpath, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new XMLException(e);
        }
    }

    public String getString(String xpath) throws XMLException {
        try {
            return (String) evaluate(xpath, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new XMLException(e);
        }
    }

    private Object evaluate(String xpath, QName rt)
            throws XPathExpressionException {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        XPathExpression xpe = xp.compile(xpath);
        return xpe.evaluate(base, rt);
    }
}
