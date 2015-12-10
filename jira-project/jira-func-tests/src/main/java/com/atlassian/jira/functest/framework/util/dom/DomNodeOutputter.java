package com.atlassian.jira.functest.framework.util.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/** A class to walk a DOM tree and output canonical HTML */
class DomNodeOutputter
{
    private final StringBuffer sb;
    private final boolean useLowerCase;

    public DomNodeOutputter(Node targetNode, StringBuffer sb, boolean useLowerCase)
    {
        this.sb = sb;
        this.useLowerCase = useLowerCase;
        walkNode(targetNode);
    }

    private void walkNode(Node node)
    {
        short nodeType = node.getNodeType();
        // output the node
        if (nodeType == Node.DOCUMENT_NODE)
        {
            walkElement(((Document) node).getDocumentElement());
        }
        if (nodeType == Node.ELEMENT_NODE)
        {
            walkElement(node);
        }
        else
        {
            outputSimpleNode(node);
        }
    }

    private void walkElement(Node node)
    {
        Element e = (Element) node;
        outputElementOpen(e);
        if (e.hasChildNodes())
        {
            NodeList nodeList = e.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                Node child = nodeList.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE)
                {
                    walkElement(child);
                }
                else
                {
                    outputSimpleNode(child);
                }
            }
        }
        outputElementClose(e);
    }

    private void outputSimpleNode(Node node)
    {
        short nodeType = node.getNodeType();
        if (nodeType == Node.ATTRIBUTE_NODE)
        {
            outputAttr(node);
        }
        else if (nodeType == Node.TEXT_NODE)
        {
            sb.append(node.getNodeValue());
        }
        else if (nodeType == Node.COMMENT_NODE)
        {
            sb.append("<!--");
            sb.append(node.getNodeValue());
            sb.append("-->");
        }
        else if (nodeType == Node.CDATA_SECTION_NODE)
        {
            CDATASection cdataSection = (CDATASection) node;
            sb.append("<![[");
            sb.append(cdataSection.getData());
            sb.append("]]>");
        }
    }

    private void outputElementOpen(Node node)
    {
        Element e = (Element) node;
        sb.append('<');
        sb.append(getName(e.getTagName()));

        Attr[] attrs = getAttributes(e);
        for (Attr attr : attrs)
        {
            sb.append(' ');
            outputAttr(attr);
        }
        sb.append('>');
    }

    private void outputElementClose(Node node)
    {
        Element e = (Element) node;
        sb.append('<');
        sb.append('/');
        sb.append(getName(e.getTagName()));
        sb.append('>');
    }

    private void outputAttr(Node node)
    {
        Attr attr = (Attr) node;
        sb.append(attr.getName());
        sb.append('=');
        sb.append('"');
        sb.append(attr.getValue());
        sb.append('"');
    }


    private Attr[] getAttributes(Element e)
    {
        List l = new ArrayList();
        NamedNodeMap attrList = e.getAttributes();
        int length = attrList.getLength();
        for (int i = 0; i < length; i++)
        {
            Node child = attrList.item(i);
            if (child.getNodeType() == Node.ATTRIBUTE_NODE)
            {
                l.add(child);
            }
        }
        return (Attr[]) l.toArray(new Attr[l.size()]);
    }

    private String getName(String name)
    {
        return (this.useLowerCase ? name.toLowerCase() : name);
    }
}
