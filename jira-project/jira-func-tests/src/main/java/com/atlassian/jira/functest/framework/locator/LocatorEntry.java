package com.atlassian.jira.functest.framework.locator;

import org.w3c.dom.Node;

/**
 * When iterating a {@link Locator}, the iterator() method returns
 * {@link LocatorEntry} objects that contain each {@link org.w3c.dom.Node}s , the text of the
 * node, as well as the index that the node would occur at if a called to
 * {@link Locator#getNodes()} was made.
 *
 * @since 4.0
 */
public interface LocatorEntry
{
    /**
     * @return the Node that has been iterated to
     */
    public Node getNode();

    /**
     * @return the text of the Node that has been iterated to, according to the Locator
     */
    public String getText();

    /**
     * @return the HTML of the Node that has been iterated to, according to the Locator
     */
    public String getHTML();

    /**
     * @return the index of the Node had it been returned via {@link Locator#getNodes()}
     */
    public int getIndex();
}
