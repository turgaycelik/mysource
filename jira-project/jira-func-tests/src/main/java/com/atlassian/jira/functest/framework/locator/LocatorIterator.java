package com.atlassian.jira.functest.framework.locator;

import com.atlassian.jira.functest.framework.util.dom.DomKit;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** The iterator implementation for Locator.iterator() */
public class LocatorIterator implements Iterator
{
    // TODO generify

    private int index = -1;
    private final Node[] nodes;
    private final Locator locator;

    public LocatorIterator(Locator locator)
    {
        if (locator == null)
        {
            throw new IllegalArgumentException("You must provide a non null Locator");
        }
        this.locator = locator;
        this.nodes = locator.getNodes();
    }

    public boolean hasNext()
    {
        int indexplus1 = index + 1;
        return indexplus1 >= 0 && indexplus1 < nodes.length;
    }

    public Object next()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException();
        }
        index++;
        Node node = nodes[index];
        return new LocatorInfoImpl(node, locator, index);
    }

    public void remove()
    {
        throw new UnsupportedOperationException("remove() method not supported!");
    }

    class LocatorInfoImpl implements LocatorEntry
    {

        private final int index;
        private final Node node;
        private final Locator locator;
        private String text;
        private String html;

        public LocatorInfoImpl(Node node, Locator locator, int index)
        {
            this.node = node;
            this.locator = locator;
            this.index = index;
        }

        public int getIndex()
        {
            return index;
        }

        public Node getNode()
        {
            return DomKit.betterNode(node);
        }

        public String getText()
        {
            if (text == null) {
                text = locator.getText(this.node);
            }
            return text;
        }

        public String getHTML()
        {
            if (html == null) {
                html = locator.getHTML(node);
            }
            return html;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder("i:");
            sb.append(index);
            sb.append(" text:");
            final String s = getText();
            sb.append(s.substring(0,Math.min(50,s.length())));
            return sb.toString();
        }
    }
}
