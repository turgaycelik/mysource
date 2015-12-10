package com.atlassian.jira.functest.framework.util.dom;

import org.w3c.dom.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This class is used to wrap a {@link org.w3c.dom.Node} with a better toString() functionality.   Its
 * package level and its exposed publically via the DomKit class.
 */
class DomBetterNode
{
    private static final Class[] PROXIED_DOM_CLASSES = new Class[]{Node.class, Element.class, Document.class, Attr.class, Text.class, Notation.class, CDATASection.class, CharacterData.class, DocumentFragment.class, ProcessingInstruction.class, DocumentType.class};

    /**
     * This returns a {@link org.w3c.dom.Node} implementation that has better toString() attached to it.  Its useful inside a debugger
     * so you can easily see what content a node has.  Each {@link org.w3c.dom.Node} that is linked to from this node
     * will also get the better toString() methods applied to it.
     * <p/>
     * You can call this function safely on already wrapped Node objects since it can detect if they have already been wrapped
     *
     * @param node the Node to make into a better one
     * @return a proxied implementation of {@link org.w3c.dom.Node} that has the new functionality
     */
    static Node betterNode(Node node)
    {
        if (node != null)
        {
            Class objClass = node.getClass();
            if (!isAlreadyProxied(objClass))
            {
                return (Node) Proxy.newProxyInstance(objClass.getClassLoader(), PROXIED_DOM_CLASSES, new BetterNodeInvocationHandler(node));
            }
        }
        return node;
    }

    private static boolean isAlreadyProxied(Class aClass)
    {
        return Proxy.isProxyClass(aClass);
    }

    /**
     * This is an InvocationHandler that wraps a {@link org.w3c.dom.Node} with a better toString()
     * method.
     */
    private static class BetterNodeInvocationHandler implements InvocationHandler
    {
        private Node wrappedNode;

        private BetterNodeInvocationHandler(Node wrappedNode)
        {
            this.wrappedNode = wrappedNode;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if ("toString".equals(method.getName()) && method.getReturnType().equals(String.class) && method.getParameterTypes().length == 0)
            {
                return betterToString();
            }
            Class returnType = method.getReturnType();
            Object returnValue = method.invoke(wrappedNode, args);
            if (Node.class.isAssignableFrom(returnType) && returnValue != null)
            {
                // wrap the node again as its escapes us
                return betterNode((Node) returnValue);
            }
            return returnValue;
        }

        private String betterToString()
        {
            StringBuilder sb = new StringBuilder()
                    .append(wrappedNode.getNodeName()).append("-->")
                    .append(DomKit.getCollapsedText(wrappedNode));
            return sb.toString();
        }
    }
}
