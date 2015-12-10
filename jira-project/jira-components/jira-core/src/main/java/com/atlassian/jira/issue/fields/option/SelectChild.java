package com.atlassian.jira.issue.fields.option;

/**
 * Marker interface for objects that can go in a <select> list.
 *
 * @since v5.0
 */
public interface SelectChild
{
    /**
     * Return true if this implementation represents an &lt;optgroup&gt;, else false.
     * @return true if this class is an option group
     */
    boolean isOptionGroup();
}
