package com.atlassian.jira.functest.framework.util.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * A helper class to copy DOM nodes from one tree into another
 *
 * @since v3.13
 */
public class DomNodeCopier
{
    private final boolean convertToLowerCase;
    private final Node copiedNode;

    public DomNodeCopier(Node srcNode, boolean convertToLowerCase)
    {
        this.convertToLowerCase = convertToLowerCase;
        this.copiedNode = copyNode(srcNode);
    }

    public Node getCopiedNode()
    {
        return copiedNode;
    }

    private Node copyNode(Node srcNode)
    {
        Element srcElement;
        short srcNodeType = srcNode.getNodeType();
        if (srcNodeType == Node.DOCUMENT_NODE)
        {
            srcElement = ((Document) srcNode).getDocumentElement();
        }
        else if (srcNodeType == Node.ELEMENT_NODE)
        {
            srcElement = (Element) srcNode;
        }
        else
        {
            throw new IllegalArgumentException("Only allowed to copy Documents/ Elements");
        }
        String qualifiedName = (convertToLowerCase ? srcElement.getTagName().toLowerCase() : srcElement.getTagName());

        DOMImplementation domImplementation = DomKit.getNonValidatingDocumentBuilder().getDOMImplementation();
        DocumentType documentType = domImplementation.createDocumentType(qualifiedName, "-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
        Document targetDoc = domImplementation.createDocument("http://www.w3.org/1999/xhtml", qualifiedName, documentType);

        Element targetElement = targetDoc.getDocumentElement();
        copyElementAttrs(srcElement, targetElement);

        NodeList nodeList = srcElement.getChildNodes();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++)
        {
            Node childNode = nodeList.item(i);
            if (childNode.getNodeType() != Node.ATTRIBUTE_NODE)
            {
                copyChildNode(childNode, targetElement);
            }
        }

        // what object should we return?
        if (srcNodeType == Node.DOCUMENT_NODE)
        {
            return targetDoc;
        }
        else
        {
            return targetElement;
        }
    }

    private void copyChildNode(Node srcNode, Element targetParentNode)
    {
        short srcNodeType = srcNode.getNodeType();
        switch (srcNodeType)
        {
            case Node.ELEMENT_NODE:
                copyElementDeep((Element) srcNode, targetParentNode);
                break;

            default:
                copyStandaloneNode(srcNode, targetParentNode);
                break;
        }
    }

    private void copyElementDeep(Element srcElement, Element targetParentElement)
    {
        String tagName = (convertToLowerCase ? srcElement.getTagName().toLowerCase() : srcElement.getTagName());
        Document targetDoc = targetParentElement.getOwnerDocument();
        Element targetElement = targetDoc.createElement(tagName);
        targetParentElement.appendChild(targetElement);
        copyElementAttrs(srcElement, targetElement);

        NodeList nodeList = srcElement.getChildNodes();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++)
        {
            Node childNode = nodeList.item(i);
            if (childNode.getNodeType() != Node.ATTRIBUTE_NODE)
            {
                copyChildNode(childNode, targetElement);
            }
        }

    }

    private void copyElementAttrs(Element srcElement, Element targetElement)
    {
        NamedNodeMap nodeList = srcElement.getAttributes();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++)
        {
            Attr src = (Attr) nodeList.item(i);
            final String name = src.getName();
            targetElement.setAttribute(name, src.getValue());
        }
    }


    private void copyStandaloneNode(Node srcNode, Element targetParentElement)
    {
        Node targetNode = null;
        Document doc = targetParentElement.getOwnerDocument();
        short srcNodeType = srcNode.getNodeType();
        switch (srcNodeType)
        {
            case Node.CDATA_SECTION_NODE:
            {

                CDATASection src = (CDATASection) srcNode;
                targetNode = doc.createCDATASection(src.getData());
                break;
            }

            case Node.COMMENT_NODE:
            {

                Comment src = (Comment) srcNode;
                targetNode = doc.createComment(src.getData());
                break;
            }

            case Node.TEXT_NODE:
            {

                Text src = (Text) srcNode;
                targetNode = doc.createTextNode(src.getData());
                break;
            }

            case Node.ENTITY_REFERENCE_NODE:
            {

                EntityReference src = (EntityReference) srcNode;
                targetNode = doc.createEntityReference(src.getNodeName());
                targetNode.setNodeValue(src.getNodeValue());
                break;
            }

            default:
                break;

        }
        if (targetNode != null)
        {
            targetParentElement.appendChild(targetNode);
        }
    }
}
