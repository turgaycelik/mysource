package com.atlassian.validation;

import java.util.Set;

/**
 * Generic enumerator interface to support some form of enumerated values. Values in the enumeration are assumed
 * to be unique
 *
 * @since v4.4
 */
public interface Enumerator<T>
{
    public Set<T> getEnumeration();
}
