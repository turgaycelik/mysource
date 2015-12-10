package com.atlassian.jira;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Defines a domain specific data type. An example of this is Issue, Version, or Date.
 *
 * Fields handle data of a specified type and searchers and functions know which
 * data-types they can handle working on.
 *
 * @since v4.0
 */
public interface JiraDataType
{
    /**
     * Provides a string representation of this JiraDataType's actual types. A JiraDataType can declare that it
     * is made up of distinct types and this is why we return a collection of string representations.
     *
     * @return string representation of this JiraDataTypes's actual types.
     */
    Collection<String> asStrings();

    /**
     * Determines if this type matches the passed in other JiraDataType.
     *
     * This method runs through the data types and will return true if any of the types are equals to the other
     * types.
     *
     * This method should be reflexive, if a.match(b) == true then b.match(a) == true
     *
     * There is a special case which is {@link Object}. This means all and any comparison against Object.class will
     * return true for the match method.
     *
     * @param otherType the data type to compare to, not null.
     * @return true if any of this types are assignable to the other types.
     */
    boolean matches(@Nonnull JiraDataType otherType);
}
