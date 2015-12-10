package com.atlassian.validation;

import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.Set;

/**
 * Enumerates possible values for an application property
 *
 * @since v4.4
 */
public final class ApplicationPropertyEnumerator implements Enumerator<String>
{
    private Set<String> enumeratedValues;

    private ApplicationPropertyEnumerator(final Iterable<String> enumeratedValues)
    {
        this.enumeratedValues = enumeratedValues == null ?
                Collections.<String>emptySet() : ImmutableSet.copyOf(enumeratedValues);
    }

    public Set<String> getEnumeration()
    {
        return enumeratedValues;
    }

    public static ApplicationPropertyEnumerator of(final Iterable<String> enumeratedValues)
    {
        return new ApplicationPropertyEnumerator(enumeratedValues);
    }
}
