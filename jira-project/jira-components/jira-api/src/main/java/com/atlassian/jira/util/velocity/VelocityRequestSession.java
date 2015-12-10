package com.atlassian.jira.util.velocity;

import java.util.Enumeration;

/**
 * An object that stores Session information for a user;
 *
 * @since v4.0
 */
public interface VelocityRequestSession
{
    /**
     * Returns a string containing the unique identifier assigned to this session.
     *
     * @return a string specifying the identifier assigned to this session
     */
    String getId();

    /**
     * Returns the object bound with the specified name in this session, or null if no object is bound under the name.
     *
     * @param name a string specifying the name of the object
     * @return the object with the specified name
     */
    Object getAttribute(String name);

    /**
     * Returns an Enumeration of String objects containing the names of all the objects bound to this session.
     *
     * @return an Enumeration of String objects specifying the names of all the objects bound to this session
     */
    Enumeration<String> getAttributeNames();

    /**
     * Binds an object to this session, using the name specified. If an object of the same name is already bound to the session, the object is replaced.
     *
     * @param name  the name to which the object is bound; cannot be null
     * @param value the object to be bound; cannot be null
     */
    void setAttribute(String name, Object value);

    /**
     * Removes the object bound with the specified name from this session. If the session does not have an object bound with the specified name, this method does nothing.
     *
     * @param name the name of the object to remove from this session
     */
    void removeAttribute(String name);

    /**
     * Invalidates this session and unbinds any objects bound to it.
     */
    void invalidate();
}
