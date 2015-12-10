package com.atlassian.jira.functest.framework.locator;

import org.w3c.dom.Node;

import java.util.Iterator;

/**
 * A {@link Locator} is responsible for "locating" DOM {@link org.w3c.dom.Node}'s on a web page and also
 * getting the text of those {@link org.w3c.dom.Node}'s.
 * <p/>
 * You use a Locator to "narrow" down the areas of a web page you want to make assertions on.  So it may be a specified
 * element with an id or a set of elements with specific class.  {@link Locator}'s also abstract how the text is returned
 * for page elements.  So it may include markup text or it may not, that is up to the specialised type of Locator
 * <p/>
 * {@link Locator}'s should implement <code>toString()</code> in a way that is meaning full when test cases fail and you want to know in what context.
 * <p/>
 * NOTE : {@link Locator}'s are one shot objects.  A call to getNodes() should return the same Nodes every time it is called.  Even if the underlying
 * data source has changed (for example the WebTester page has changed) the Locator MUST return the same data.
 *
 * @since v3.13
 */
public interface Locator
{
    /**
     * Return true at least one element will be matches by this locator.
     *
     * @return true if the locator will match one element or false otherwise.
     */
    public boolean exists();

    /**
     * A {@link com.atlassian.jira.functest.framework.locator.Locator} may return 0 nodes, 1 node or multiple nodes.
     * <p/>
     *
     * @return a non null array of Nodes that match, or a zero sized array of no matches are made
     */
    public Node[] getNodes();

    /**
     * This convenience methods is the the equivalent of getNodes()[0] except when the number of nodes
     * found is zero, in which case it should return null.
     *
     * @return null if not nodes found or the getNodes()[0]
     */
    public Node getNode();

    /**
     * Returns the combined collapsed text of the Nodes that <code>getNodes()</code> would return or empty string.
     * <p/>
     * NOTE : This text is the "COLLAPSED" text of the nodes, that is all the child {@link org.w3c.dom.Text} nodes
     * smooshed together to removed repeating whitespace.
     *
     * @return the text of the Nodes that the locator is designed to locate.
     * @throws RuntimeException if something goes wrong
     */
    public String getText();

    /**
     * Returns the collapsed text of the specified Node.  The node MUST be one of the nodes that a call to <code>getNodes()</code>
     * would return.
     * <p/>
     * NOTE : This text is the "COLLAPSED" text of the node, that is all the child {@link org.w3c.dom.Text} nodes
     * smooshed together to removed repeating whitespace.
     *
     * @param node the node that we previously returned by a call to getNodes()
     * @return the text f the Node according to this Locator
     * @throws IllegalArgumentException if the node is not one that would be returned by a call to getNodes()
     */
    public String getText(Node node);

    /**
     * Returns the combined raw text of the Nodes that <code>getNodes()</code> would return or empty string.
     * <p/>
     * NOTE : This text is the "RAW" text of the nodes, that is all the child {@link org.w3c.dom.Text} nodes
     * appended together with not whitespace removal at all.
     *
     * @return the text of the Nodes that the locator is designed to locate.
     * @throws RuntimeException if something goes wrong
     */
    public String getRawText();

    /**
     * Returns the raw text of the specified Node.  The node MUST be one of the nodes that a call to <code>getNodes()</code>
     * would return.
     * <p/>
     * NOTE : This text is the "RAW" text of the node, that is all the child {@link org.w3c.dom.Text} nodes
     * appended together with not whitespace removal at all.
     *
     * @param node the node that we previously returned by a  call to getNodes()
     * @return the text of the Node according to this Locator
     * @throws IllegalArgumentException if the node is not one that would be returned by a call to getNodes()
     */
    public String getRawText(Node node);

    /**
     * Returns the HTML that a called to {@link com.atlassian.jira.functest.framework.locator.Locator#getNodes()} represent, including the top level nodes themselves.
     *
     * @return the HTML of the nodes
     */
    public String getHTML();

    /**
     * Returns the HTML of the specified node.  The node MUST be one of the nodes that a call to <code>getNodes()</code>
     * would return.
     * @param node the node that we previously returned by a  call to getNodes()
     * @return the HTML of the node
     */
    public String getHTML(Node node);

    /**
     * This will return a {@link java.util.Iterator} that returns {@link com.atlassian.jira.functest.framework.locator.LocatorEntry} objects
     *
     * @return a {@link java.util.Iterator} that returns {@link com.atlassian.jira.functest.framework.locator.LocatorEntry} objects
     */
    public Iterator<LocatorEntry> iterator();

    /**
     * Get all matches of this locator as an iterable collection of locator entries
     *
     * @return iterable collection of locator entries representing matches of this locator
     */
    public Iterable<LocatorEntry> allMatches();

    /**
     * This will return true if the Locator found some {@link org.w3c.dom.Node}'s.  This is really a convenience method
     * for getNodes().length > 0
     *
     * @return true if the Locator found some nodes
     */
    public boolean hasNodes();

    /**
     * A meaningful "debugging" representation of this Locator to help when test fail.
     *
     * @return A meaningful "debugging" representation of this Locator to help when test fail.
     */
    public String toString();
}
