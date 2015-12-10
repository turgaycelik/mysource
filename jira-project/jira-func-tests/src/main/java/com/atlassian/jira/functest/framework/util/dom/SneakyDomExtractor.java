package com.atlassian.jira.functest.framework.util.dom;

import com.meterware.httpunit.HTMLPage;
import com.meterware.httpunit.WebResponse;
import net.sourceforge.jwebunit.HttpUnitDialog;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.WeakHashMap;

/**
 * This gets around some of the limitations in HttpUnit such that the DOM is not cloneable even if it has a public
 * accessor.  So we do some jiggery pokery via reflection and get it anyway.
 *
 * @since v3.13
 */
public class SneakyDomExtractor
{
    private final static WeakHashMap<WebResponse, Document> preparsedDOMObjects = new WeakHashMap<WebResponse, Document>();

    /**
     * Returns the DOM {@link org.w3c.dom.Document} associated with the current web page in the {@link net.sourceforge.jwebunit.WebTester}
     *
     * @param tester the web tester in question
     * @return A Document or null if it cant be obtained
     */
    public static Document getDOM(WebTester tester)
    {
        HttpUnitDialog dialog = tester.getDialog();
        WebResponse webResponse = (dialog == null ? null : dialog.getResponse());
        //
        // have we already obtained the DOM document for this WebResponse.  if so we can save some
        // MIPS by using the previously saved one.
        //
        Document document = preparsedDOMObjects.get(webResponse);
        if (document == null && webResponse != null)
        {
            document = getDOMViaSneakyReflection(webResponse);
            // copy the DOM and make it lower case.  This is our natural mode
            // and we would want to write XPATH etc... in lower case
            document = wrapDocument(document);
            document = (Document) DomKit.copyDOM(document, true);

            // cache the parsed and wrapped DOM document away keyed on WebResponse object
            preparsedDOMObjects.put(webResponse, document);
        }
        return document;
    }

    /**
     * WebResponse has a getDOM() method however it uses deep cloning and this breaks in Xerces.
     * <p/>
     * So we cheat.  We use reflection to get the internal DOM tree directly from
     * the WebResponse class and then return that.  This relies on being able to
     * get access to package level methods via Method.setAccessible().
     *
     * @param webResponse the web response
     * @return a W3C Document
     */
    private static Document getDOMViaSneakyReflection(WebResponse webResponse)
    {
        Document doc;
        try
        {
            //doc = webResponse.getDOM();
            Method getReceivedPage = WebResponse.class.getDeclaredMethod("getReceivedPage");
            getReceivedPage.setAccessible(true);

            HTMLPage htmlPage = (HTMLPage) getReceivedPage.invoke(webResponse);

            // now try and access its ParsedHTML.getOriginalDOM method
            Method getOriginalDOM = htmlPage.getClass().getSuperclass().getDeclaredMethod("getOriginalDOM");
            getOriginalDOM.setAccessible(true);

            Node node = (Node) getOriginalDOM.invoke(htmlPage);
            doc = (Document) node;
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        return doc;
    }

    /**
     * If the Document implementation is the org.apache implementation then we wrap it to
     * get around some problems.
     *
     * @param originalDocument the document returned
     * @return the wrapped document
     */
    private static Document wrapDocument(Document originalDocument)
    {
        if (originalDocument.getClass().getName().indexOf("org.apache.html.dom") != -1 || true)
        {
            SneakyInvocationHandler handler = new SneakyInvocationHandler(originalDocument);
            return (Document) Proxy.newProxyInstance(Document.class.getClassLoader(), new Class[] { Document.class }, handler);
        }
        else
        {
            return originalDocument;
        }
    }


    /**
     * This proxied invocation handler will override the Document.getDocumentElement() method
     * because its broken in the HTTPUnit/Apache HtmlDocumentImpl classes.  We are sneaky by navigating to the
     * first Element child of the Document.
     */
    private static class SneakyInvocationHandler implements InvocationHandler
    {
        private final Document originalDocument;


        private SneakyInvocationHandler(Document originalDocument)
        {
            this.originalDocument = originalDocument;
        }

        public Object invoke(Object object, Method method, Object[] objects) throws Throwable
        {
            if ("getDocumentElement".equals(method.getName()))
            {
                return getDocumentElement();
            }
            else
            {
                return method.invoke(originalDocument, objects);
            }
        }

        private Element getDocumentElement()
        {
            return getFirstElement(originalDocument);
        }

        private Element getFirstElement(Node parent)
        {
            NodeList nodeList = parent.getChildNodes();
            int length = nodeList.getLength();
            for (int i = 0; i < length; i++)
            {
                Node child = nodeList.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE)
                {
                    return (Element) child;
                }
            }
            return null;
        }

    }

}
