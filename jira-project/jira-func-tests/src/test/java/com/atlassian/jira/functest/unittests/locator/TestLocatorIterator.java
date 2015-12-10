package com.atlassian.jira.functest.unittests.locator;

import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.LocatorEntry;
import com.atlassian.jira.functest.framework.locator.LocatorIterator;
import junit.framework.TestCase;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class TestLocatorIterator extends TestCase
{
    static String[] TEST_STRS = new String[] { "Alpha", "Beta", "Delta", "Gamma" };
    static Node[] TEST_NODES = createNodes(TEST_STRS);
    static Locator LOCATOR_EMPTY = createLocator(new Node[0]);
    static Locator LOCATOR = createLocator(TEST_NODES);

    public void testHasNext()
    {

        LocatorIterator iterator = new LocatorIterator(LOCATOR_EMPTY);
        assertFalse(iterator.hasNext());
        try
        {
            iterator.next();
            fail("Should have thrown NoSuchElementException");
        }
        catch (NoSuchElementException expected)
        {
        }

        iterator = new LocatorIterator(LOCATOR);
        assertTrue(iterator.hasNext());
        for (int i = 0; i < TEST_NODES.length; i++)
        {
            assertTrue(iterator.hasNext());
            iterator.next();
        }
        assertFalse(iterator.hasNext());
        try
        {
            iterator.next();
            fail("Should have thrown NoSuchElementException");
        }
        catch (NoSuchElementException expected)
        {
        }
    }

    public final void testNext()
    {
        LocatorIterator iterator = new LocatorIterator(LOCATOR);
        assertTrue(iterator.hasNext());

        int count = 0;
        for (; iterator.hasNext();)
        {
            LocatorEntry info = (LocatorEntry) iterator.next();
            assertNotNull(info);
            assertNotNull(info.getNode());
            assertNotNull(info.getText());
            assertEquals(count, info.getIndex());
            count++;
            int i = info.getIndex();
            assertSame(TEST_NODES[i], info.getNode());
            assertEquals(TEST_STRS[i], info.getText());
        }
        assertFalse(iterator.hasNext());
    }

    public final void testRemove()
    {
        LocatorIterator iterator = new LocatorIterator(LOCATOR);
        try
        {
            iterator.remove();
            fail("expecting UnsupportedOperationException");
        }
        catch (UnsupportedOperationException e)
        {
        }
        while (iterator.hasNext())
        {
            iterator.next();
            try
            {
                iterator.remove();
                fail("expecting UnsupportedOperationException");
            }
            catch (UnsupportedOperationException e)
            {
            }
        }
    }

    private static Locator createLocator(final Node[] nodes)
    {
        return new Locator()
        {
            @Override
            public boolean exists()
            {
                return nodes != null && nodes.length > 0;
            }

            public Node[] getNodes()
            {
                return nodes;
            }

            public Node getNode()
            {
                return null;
            }

            public String getText()
            {
                return null;
            }

            public String getText(Node node)
            {
                return node.getNodeValue();
            }

            public Iterator<LocatorEntry> iterator()
            {
                return null;
            }

            @Override
            public Iterable<LocatorEntry> allMatches()
            {
                return null;
            }

            public String getHTML()
            {
                return null;
            }

            public String getRawText()
            {
                return null;
            }

            public String getRawText(final Node node)
            {
                return null;
            }

            public String getHTML(final Node node)
            {
                return null;
            }

            public boolean hasNodes()
            {
                return getNodes().length == 0;
            }
        };
    }

    private static Text[] createNodes(String textArr[])
    {
        Text text[] = new Text[textArr.length];
        for (int i = 0; i < textArr.length; i++)
        {
            TextHandler handler = new TextHandler(textArr[i]);
            text[i] = (Text) Proxy.newProxyInstance(Text.class.getClassLoader(), new Class[] { Text.class }, handler);
        }
        return text;
    }

    static class TextHandler implements InvocationHandler
    {
        private String text;

        public TextHandler(String text)
        {
            this.text = text;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if ("getNodeValue".equals(method.getName()))
            {
                return text;
            }
            return null;
        }
    }
}
