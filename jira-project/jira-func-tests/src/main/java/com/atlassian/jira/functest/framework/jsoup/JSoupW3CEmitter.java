package com.atlassian.jira.functest.framework.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.select.Elements;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This guy will wrap a {@link org.jsoup.nodes.Document} and produce a W3C DOM tree.
 *
 * JSoup produces DOM like nodes not actual w3c nodes.  This class maps them into the w3c world.
 *
 * The DOM tree produced is essentially read only.  Many of the DOM operations are unsupported
 * and will throw an unsupported operation exception if called.
 */
public class JSoupW3CEmitter
{
    private final org.jsoup.nodes.Document document;
    private final Map<org.jsoup.nodes.Node, Node> seenNodes = new HashMap<org.jsoup.nodes.Node, Node>();
    private final Map<org.jsoup.nodes.Attribute, Attr> seenAttrs = new HashMap<org.jsoup.nodes.Attribute, Attr>();


    public static JSoupW3CEmitter parse(String htmlInput)
    {
        final org.jsoup.nodes.Document document = Jsoup.parse(htmlInput);
        return new JSoupW3CEmitter(document);
    }

    public JSoupW3CEmitter()
    {
        this.document = null;
    }

    public JSoupW3CEmitter(org.jsoup.nodes.Document document)
    {
        this.document = document;
    }

    public NodeList select(String cssSelector, Node node)
    {
        if (node instanceof EmittedElement)
        {
            EmittedElement emittedElement = (EmittedElement) node;
            return wrapNodeList(emittedElement.element.select(cssSelector));
        }
        if (node instanceof EmittedDocument)
        {
            EmittedDocument emittedDocument = (EmittedDocument) node;
            return wrapNodeList(emittedDocument.document.select(cssSelector));
        }
        throw new IllegalArgumentException("You must provide a Document or Element node");
    }

    public Document getDocument()
    {
        return wrapDocument(document);
    }

    private Document wrapDocument(org.jsoup.nodes.Document document)
    {
        return (Document) wrapNode(document);
    }

    private Element wrapElement(org.jsoup.nodes.Element element)
    {
        return (Element) wrapNode(element);
    }

    private Node wrapNode(org.jsoup.nodes.Node node)
    {
        if (node == null)
        {
            return null;
        }
        final Node seenNode = seenNodes.get(node);
        if (seenNode != null)
        {
            return seenNode;
        }

        if (node instanceof org.jsoup.nodes.Document)
        {
            return emit(node, new EmittedDocument(node));
        }
        if (node instanceof org.jsoup.nodes.Element)
        {
            return emit(node, new EmittedElement(node));
        }
        if (node instanceof org.jsoup.nodes.TextNode)
        {
            return emit(node, new EmittedText(node));
        }
        if (node instanceof org.jsoup.nodes.Comment)
        {
            return emit(node, new EmittedComment(node));
        }
        if (node instanceof org.jsoup.nodes.DataNode)
        {
            return emit(node, new EmittedCDATASection(node));
        }
        return emit(node, new EmittedNode(node));
    }

    Attr wrapAttr(final org.jsoup.nodes.Element element, final int index, final org.jsoup.nodes.Attribute attribute)
    {
        Attr emittedAttr = seenAttrs.get(attribute);
        if (emittedAttr == null)
        {
            emittedAttr = new EmittedAttr(element, index, attribute);
            seenAttrs.put(attribute, emittedAttr);
        }
        return emittedAttr;
    }

    private Node emit(org.jsoup.nodes.Node seenNode, Node node)
    {
        seenNodes.put(seenNode, node);
        return node;
    }


    private static void unsupported()
    {
        throw new DOMException((short) 666, "Unimplemented Operation");
    }


    private NodeList wrapNodeList(final List<? extends org.jsoup.nodes.Node> nodes)
    {
        NodeList nodeList;
        if (nodes != null)
        {
            nodeList = new EmittedNodeList(nodes);
        } else {
            nodeList = new EmittedNodeList(Collections.<org.jsoup.nodes.Node>emptyList());
        }
        return nodeList;
    }

    /**
     * -----------------------------------------------------------------
     */
    class EmittedNodeList implements NodeList
    {
        private final List<? extends org.jsoup.nodes.Node> nodes;

        EmittedNodeList(final List<? extends org.jsoup.nodes.Node> nodes)
        {
            this.nodes = nodes;
        }

        public Node item(final int index)
        {
            return wrapNode(nodes.get(index));
        }

        public int getLength()
        {
            return nodes.size();
        }
    }

    private class FoundAttr
    {
        int index;
        Attribute attribute;

        FoundAttr(final int index, final Attribute attribute)
        {
            this.index = index;
            this.attribute = attribute;
        }
    }

    private FoundAttr findAttribute(org.jsoup.nodes.Element element, String name)
    {
        int index = 0;
        for (Attribute attribute : element.attributes())
        {
            if (name.equalsIgnoreCase(attribute.getKey()))
            {
                return new FoundAttr(index, attribute);
            }
            index++;
        }
        return null;
    }

    /**
     * -----------------------------------------------------------------
     */
    class EmittedNameNodeMap implements NamedNodeMap
    {
        private final org.jsoup.nodes.Element element;

        EmittedNameNodeMap(final org.jsoup.nodes.Element element)
        {
            this.element = element;
        }

        public Node getNamedItem(final String name)
        {
            final JSoupW3CEmitter.FoundAttr foundAttr = findAttribute(element, name);
            if (foundAttr != null)
            {
                return wrapAttr(element, foundAttr.index, foundAttr.attribute);
            }
            return null;
        }

        public Node setNamedItem(final Node arg) throws DOMException
        {
            unsupported();
            return null;
        }

        public Node removeNamedItem(final String name) throws DOMException
        {
            unsupported();
            return null;
        }

        public Node item(final int index)
        {
            final List<Attribute> attributeList = element.attributes().asList();
            final Attribute foundAttr = attributeList.get(index);
            if (foundAttr != null)
            {
                return new EmittedAttr(element, index, foundAttr);
            }
            return null;
        }

        public int getLength()
        {
            return element.attributes().size();
        }

        public Node getNamedItemNS(final String namespaceURI, final String localName) throws DOMException
        {
            unsupported();
            return null;
        }

        public Node setNamedItemNS(final Node arg) throws DOMException
        {
            unsupported();
            return null;
        }

        public Node removeNamedItemNS(final String namespaceURI, final String localName) throws DOMException
        {
            unsupported();
            return null;
        }
    }


    /**
     * -----------------------------------------------------------------
     */
    class EmittedDocument extends EmittedNode implements Document
    {
        final org.jsoup.nodes.Document document;

        EmittedDocument(final org.jsoup.nodes.Node node)
        {
            super(node);
            document = (org.jsoup.nodes.Document) node;
        }

        public NodeList getElementsByTagName(final String tagName)
        {
            return wrapNodeList(document.getElementsByTag(tagName));
        }

        public NodeList getElementsByTagNameNS(final String namespaceURI, final String localName)
        {
            unsupported();
            return null;
        }

        public DocumentType getDoctype()
        {
            unsupported();
            return null;
        }

        public DOMImplementation getImplementation()
        {
            unsupported();
            return null;
        }

        public Element getDocumentElement()
        {
            final List<org.jsoup.nodes.Node> children = document.childNodes();
            if (children.size() > 0)
            {
                return (Element) wrapNode(children.get(0));
            }
            return null;
        }

        public Element createElement(final String tagName) throws DOMException
        {
            unsupported();
            return null;
        }

        public DocumentFragment createDocumentFragment()
        {
            unsupported();
            return null;
        }

        public Text createTextNode(final String data)
        {
            unsupported();
            return null;
        }

        public Comment createComment(final String data)
        {
            unsupported();
            return null;
        }

        public CDATASection createCDATASection(final String data) throws DOMException
        {
            unsupported();
            return null;
        }

        public ProcessingInstruction createProcessingInstruction(final String target, final String data)
                throws DOMException
        {
            unsupported();
            return null;
        }

        public Attr createAttribute(final String name) throws DOMException
        {
            unsupported();
            return null;
        }

        public EntityReference createEntityReference(final String name) throws DOMException
        {
            unsupported();
            return null;
        }

        public Node importNode(final Node importedNode, final boolean deep) throws DOMException
        {
            unsupported();
            return null;
        }

        public Element createElementNS(final String namespaceURI, final String qualifiedName) throws DOMException
        {
            unsupported();
            return null;
        }

        public Attr createAttributeNS(final String namespaceURI, final String qualifiedName) throws DOMException
        {
            unsupported();
            return null;
        }

        public Element getElementById(final String elementId)
        {
            return wrapElement(document.getElementById(elementId));
        }

        public String getInputEncoding()
        {
            unsupported();
            return null;
        }

        public String getXmlEncoding()
        {
            unsupported();
            return null;
        }

        public boolean getXmlStandalone()
        {
            unsupported();
            return false;
        }

        public void setXmlStandalone(final boolean xmlStandalone) throws DOMException
        {
            unsupported();
        }

        public String getXmlVersion()
        {
            unsupported();
            return null;
        }

        public void setXmlVersion(final String xmlVersion) throws DOMException
        {
            unsupported();
        }

        public boolean getStrictErrorChecking()
        {
            unsupported();
            return false;
        }

        public void setStrictErrorChecking(final boolean strictErrorChecking)
        {
            unsupported();
        }

        public String getDocumentURI()
        {
            unsupported();
            return null;
        }

        public void setDocumentURI(final String documentURI)
        {
            unsupported();
        }

        public Node adoptNode(final Node source) throws DOMException
        {
            unsupported();
            return null;
        }

        public DOMConfiguration getDomConfig()
        {
            unsupported();
            return null;
        }

        public void normalizeDocument()
        {
            unsupported();
        }

        public Node renameNode(final Node n, final String namespaceURI, final String qualifiedName) throws DOMException
        {
            unsupported();
            return null;
        }
    }

    /**
     * -----------------------------------------------------------------
     */
    class EmittedElement extends EmittedNode implements Element
    {
        final org.jsoup.nodes.Element element;

        EmittedElement(final org.jsoup.nodes.Node node)
        {
            super(node);
            element = (org.jsoup.nodes.Element) node;
        }

        public String getTagName()
        {
            return node.nodeName();
        }

        public String getAttribute(final String name)
        {
            return element.attr(name);
        }

        public void setAttribute(final String name, final String value) throws DOMException
        {
            unsupported();
        }

        public void removeAttribute(final String name) throws DOMException
        {
            unsupported();
        }

        public Attr getAttributeNode(final String name)
        {
            final JSoupW3CEmitter.FoundAttr foundAttr = findAttribute(element, name);
            if (foundAttr != null)
            {
                return wrapAttr(element,foundAttr.index,foundAttr.attribute);
            }
            return null;
        }

        public Attr setAttributeNode(final Attr newAttr) throws DOMException
        {
            unsupported();
            return null;
        }

        public Attr removeAttributeNode(final Attr oldAttr) throws DOMException
        {
            unsupported();
            return null;
        }

        public NodeList getElementsByTagName(final String name)
        {
            final Elements elements = element.getElementsByTag(name);
            return wrapNodeList(elements);
        }

        public String getAttributeNS(final String namespaceURI, final String localName) throws DOMException
        {
            unsupported();
            return null;
        }

        public void setAttributeNS(final String namespaceURI, final String qualifiedName, final String value)
                throws DOMException
        {
            unsupported();
        }

        public void removeAttributeNS(final String namespaceURI, final String localName) throws DOMException
        {
            unsupported();
        }

        public Attr getAttributeNodeNS(final String namespaceURI, final String localName) throws DOMException
        {
            unsupported();
            return null;
        }

        public Attr setAttributeNodeNS(final Attr newAttr) throws DOMException
        {
            unsupported();
            return null;
        }

        public NodeList getElementsByTagNameNS(final String namespaceURI, final String localName) throws DOMException
        {
            unsupported();
            return null;
        }

        public boolean hasAttribute(final String name)
        {
            return element.attr(name) != null;
        }

        public boolean hasAttributeNS(final String namespaceURI, final String localName) throws DOMException
        {
            unsupported();
            return false;
        }

        public TypeInfo getSchemaTypeInfo()
        {
            unsupported();
            return null;
        }

        public void setIdAttribute(final String name, final boolean isId) throws DOMException
        {
            unsupported();
        }

        public void setIdAttributeNS(final String namespaceURI, final String localName, final boolean isId)
                throws DOMException
        {
            unsupported();
        }

        public void setIdAttributeNode(final Attr idAttr, final boolean isId) throws DOMException
        {
            unsupported();
        }
    }

    /**
     * -----------------------------------------------------------------
     */
    class EmittedCDATASection extends EmittedText implements CDATASection
    {
        EmittedCDATASection(final org.jsoup.nodes.Node node)
        {
            super(node);
        }
    }


    /**
     * -----------------------------------------------------------------
     */
    class EmittedText extends EmittedCharacterData implements Text
    {
        EmittedText(final org.jsoup.nodes.Node node)
        {
            super(node);
        }

        public Text splitText(final int offset) throws DOMException
        {
            unsupported();
            return null;
        }

        public boolean isElementContentWhitespace()
        {
            final String s = getWholeText();
            if (s != null && s.length() > 0)
            {
                for (char c : s.toCharArray())
                {
                    if (!Character.isWhitespace(c))
                    {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        public String getWholeText()
        {
            return getNodeValue();
        }

        public Text replaceWholeText(final String content) throws DOMException
        {
            unsupported();
            return null;
        }
    }

    /**
     * -----------------------------------------------------------------
     */
    class EmittedCharacterData extends EmittedNode implements CharacterData
    {
        EmittedCharacterData(final org.jsoup.nodes.Node node)
        {
            super(node);
        }

        public String getData() throws DOMException
        {
            return getNodeValue();
        }

        public void setData(final String data) throws DOMException
        {
            unsupported();
        }

        public int getLength()
        {
            final String s = getNodeValue();
            return s == null ? 0 : s.length();
        }

        public String substringData(final int offset, final int count) throws DOMException
        {
            unsupported();
            return null;
        }

        public void appendData(final String arg) throws DOMException
        {
            unsupported();
        }

        public void insertData(final int offset, final String arg) throws DOMException
        {
            unsupported();
        }

        public void deleteData(final int offset, final int count) throws DOMException
        {
            unsupported();
        }

        public void replaceData(final int offset, final int count, final String arg) throws DOMException
        {
            unsupported();
        }
    }

    /**
     * -----------------------------------------------------------------
     */
    class EmittedComment extends EmittedNode implements Comment
    {
        EmittedComment(final org.jsoup.nodes.Node node)
        {
            super(node);
        }

        public String getData() throws DOMException
        {
            return getNodeValue();
        }

        public void setData(final String data) throws DOMException
        {
            unsupported();
        }

        public int getLength()
        {
            final String s = getNodeValue();
            return s == null ? 0 : s.length();
        }

        public String substringData(final int offset, final int count) throws DOMException
        {
            unsupported();
            return null;
        }

        public void appendData(final String arg) throws DOMException
        {
            unsupported();
        }

        public void insertData(final int offset, final String arg) throws DOMException
        {
            unsupported();
        }

        public void deleteData(final int offset, final int count) throws DOMException
        {
            unsupported();
        }

        public void replaceData(final int offset, final int count, final String arg) throws DOMException
        {
            unsupported();
        }
    }

    /**
     * -----------------------------------------------------------------
     */
    class EmittedAttr implements Attr, JSoupNode
    {
        private final org.jsoup.nodes.Element parent;
        private final int index;
        private final Attribute attribute;

        EmittedAttr(org.jsoup.nodes.Element parent, final int index, final Attribute attribute)
        {
            this.parent = parent;
            this.index = index;
            this.attribute = attribute;
        }

        public String getName()
        {
            return attribute.getKey();
        }

        public boolean getSpecified()
        {
            return true;
        }

        public String getValue()
        {
            return attribute.getValue();
        }

        public void setValue(final String value) throws DOMException
        {
            unsupported();
        }

        public Element getOwnerElement()
        {
            return (Element) wrapNode(parent);
        }

        public TypeInfo getSchemaTypeInfo()
        {
            unsupported();
            return null;
        }

        public boolean isId()
        {
            return "id".equalsIgnoreCase(attribute.getKey());
        }

        // we cant quite be an emitted node since there is not direct mapping in jsoup

        public String getNodeName()
        {
            return getName();
        }

        public String getNodeValue() throws DOMException
        {
            return getValue();
        }

        public void setNodeValue(final String nodeValue) throws DOMException
        {
            unsupported();
        }

        public short getNodeType()
        {
            return Node.ATTRIBUTE_NODE;
        }

        public Node getParentNode()
        {
            return wrapNode(parent);
        }

        public NodeList getChildNodes()
        {
            return wrapNodeList(Collections.<org.jsoup.nodes.Node>emptyList());
        }

        public Node getFirstChild()
        {
            return null;
        }

        public Node getLastChild()
        {
            return null;
        }

        public Node getPreviousSibling()
        {
            if (index > 0)
            {
                final Attribute attribute = parent.attributes().asList().get(index - 1);
                return new EmittedAttr(parent, index - 1, attribute);
            }
            return null;
        }

        public Node getNextSibling()
        {
            final List<Attribute> attributeList = parent.attributes().asList();
            if (index < attributeList.size() - 1)
            {
                final Attribute attribute = attributeList.get(index + 1);
                return new EmittedAttr(parent, index + 1, attribute);
            }
            return null;
        }

        public NamedNodeMap getAttributes()
        {
            unsupported();
            return null;
        }

        public Document getOwnerDocument()
        {
            unsupported();
            return null;
        }

        public Node insertBefore(final Node newChild, final Node refChild) throws DOMException
        {
            unsupported();
            return null;
        }

        public Node replaceChild(final Node newChild, final Node oldChild) throws DOMException
        {
            unsupported();
            return null;
        }

        public Node removeChild(final Node oldChild) throws DOMException
        {
            unsupported();
            return null;
        }

        public Node appendChild(final Node newChild) throws DOMException
        {
            unsupported();
            return null;
        }

        public boolean hasChildNodes()
        {
            return false;
        }

        public Node cloneNode(final boolean deep)
        {
            unsupported();
            return null;
        }

        public void normalize()
        {
        }

        public boolean isSupported(final String feature, final String version)
        {
            return false;
        }

        public String getNamespaceURI()
        {
            return null;
        }

        public String getPrefix()
        {
            return null;
        }

        public void setPrefix(final String prefix) throws DOMException
        {
            unsupported();
        }

        public String getLocalName()
        {
            unsupported();
            return null;
        }

        public boolean hasAttributes()
        {
            return false;
        }

        public String getBaseURI()
        {
            return null;
        }

        public short compareDocumentPosition(final Node other) throws DOMException
        {
            unsupported();
            return 0;
        }

        public String getTextContent() throws DOMException
        {
            return getValue();
        }

        public void setTextContent(final String textContent) throws DOMException
        {
            unsupported();
        }

        public boolean isSameNode(final Node other)
        {
            unsupported();
            return false;
        }

        public String lookupPrefix(final String namespaceURI)
        {
            unsupported();
            return null;
        }

        public boolean isDefaultNamespace(final String namespaceURI)
        {
            unsupported();
            return false;
        }

        public String lookupNamespaceURI(final String prefix)
        {
            unsupported();
            return null;
        }

        public boolean isEqualNode(final Node arg)
        {
            unsupported();
            return false;
        }

        public Object getFeature(final String feature, final String version)
        {
            unsupported();
            return null;
        }

        public Object setUserData(final String key, final Object data, final UserDataHandler handler)
        {
            unsupported();
            return null;
        }

        public Object getUserData(final String key)
        {
            unsupported();
            return null;
        }
    }

    /**
     * -----------------------------------------------------------------
     */
    class EmittedNode implements JSoupNode
    {
        final org.jsoup.nodes.Node node;

        EmittedNode(org.jsoup.nodes.Node node)
        {
            this.node = node;
        }

        @Override
        public String toString()
        {
            return new StringBuilder(getClass().getSimpleName())
                    .append('@').append(System.identityHashCode(this))
                    .append(' ').append(getNodeName())
                    .append(" : ").append(node)
                    .toString();
        }

        public String getNodeName()
        {
            return node.nodeName();
        }

        public String getNodeValue() throws DOMException
        {
            if (node instanceof org.jsoup.nodes.Element)
            {
                return ((org.jsoup.nodes.Element) node).text();
            }
            if (node instanceof org.jsoup.nodes.TextNode)
            {
                return ((org.jsoup.nodes.TextNode) node).getWholeText();
            }
            if (node instanceof org.jsoup.nodes.DataNode)
            {
                return ((org.jsoup.nodes.DataNode) node).getWholeData();
            }
            return "";
        }

        public void setNodeValue(final String nodeValue) throws DOMException
        {
            unsupported();
        }

        public short getNodeType()
        {
            if (node instanceof org.jsoup.nodes.Document)
            {
                return Node.DOCUMENT_NODE;
            }
            if (node instanceof org.jsoup.nodes.Element)
            {
                return Node.ELEMENT_NODE;
            }
            if (node instanceof org.jsoup.nodes.Comment)
            {
                return Node.COMMENT_NODE;
            }
            if (node instanceof org.jsoup.nodes.TextNode)
            {
                return Node.TEXT_NODE;
            }
            if (node instanceof org.jsoup.nodes.DataNode)
            {
                return Node.CDATA_SECTION_NODE;
            }
            return Node.TEXT_NODE;
        }

        public Node getParentNode()
        {
            return wrapNode(node.parent());
        }

        public NodeList getChildNodes()
        {
            return wrapNodeList(node.childNodes());
        }

        public Node getFirstChild()
        {
            final List<org.jsoup.nodes.Node> children = node.childNodes();
            if (children.size() == 0)
            {
                return null;
            }
            return wrapNode(children.get(0));
        }

        public Node getLastChild()
        {
            final List<org.jsoup.nodes.Node> children = node.childNodes();
            if (children.size() == 0)
            {
                return null;
            }
            return wrapNode(children.get(children.size() - 1));
        }

        public Node getPreviousSibling()
        {
            return wrapNode(node.previousSibling());
        }

        public Node getNextSibling()
        {
            return wrapNode(node.nextSibling());
        }

        public NamedNodeMap getAttributes()
        {
            if (node instanceof org.jsoup.nodes.Element)
            {
                return new EmittedNameNodeMap((org.jsoup.nodes.Element) node);
            }
            return null;
        }

        public Document getOwnerDocument()
        {
            return null;
        }

        public Node insertBefore(final Node newChild, final Node refChild) throws DOMException
        {
            unsupported();
            return null;
        }

        public Node replaceChild(final Node newChild, final Node oldChild) throws DOMException
        {
            unsupported();
            return null;
        }

        public Node removeChild(final Node oldChild) throws DOMException
        {
            unsupported();
            return null;
        }

        public Node appendChild(final Node newChild) throws DOMException
        {
            unsupported();
            return null;
        }

        public boolean hasChildNodes()
        {
            return node instanceof org.jsoup.nodes.Element && node.childNodes().size() > 0;
        }

        public Node cloneNode(final boolean deep)
        {
            unsupported();
            return null;
        }

        public void normalize()
        {
        }

        public boolean isSupported(final String feature, final String version)
        {
            unsupported();
            return false;
        }

        public String getNamespaceURI()
        {
            unsupported();
            return null;
        }

        public String getPrefix()
        {
            unsupported();
            return null;
        }

        public void setPrefix(final String prefix) throws DOMException
        {
            unsupported();
        }

        public String getLocalName()
        {
            unsupported();
            return null;
        }

        public boolean hasAttributes()
        {
            return node instanceof org.jsoup.nodes.Element && node.attributes().size() > 0;
        }

        public String getBaseURI()
        {
            unsupported();
            return null;
        }

        public short compareDocumentPosition(final Node other) throws DOMException
        {
            unsupported();
            return 0;
        }

        public String getTextContent() throws DOMException
        {
            unsupported();
            return null;
        }

        public void setTextContent(final String textContent) throws DOMException
        {
            unsupported();
        }

        public boolean isSameNode(final Node other)
        {
            unsupported();
            return false;
        }

        public String lookupPrefix(final String namespaceURI)
        {
            unsupported();
            return null;
        }

        public boolean isDefaultNamespace(final String namespaceURI)
        {
            unsupported();
            return false;
        }

        public String lookupNamespaceURI(final String prefix)
        {
            unsupported();
            return null;
        }

        public boolean isEqualNode(final Node arg)
        {
            unsupported();
            return false;
        }

        public Object getFeature(final String feature, final String version)
        {
            unsupported();
            return null;
        }

        public Object setUserData(final String key, final Object data, final UserDataHandler handler)
        {
            unsupported();
            return null;
        }

        public Object getUserData(final String key)
        {
            unsupported();
            return null;
        }
    }
}
