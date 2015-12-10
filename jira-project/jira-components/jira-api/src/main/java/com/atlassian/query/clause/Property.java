package com.atlassian.query.clause;

import java.text.MessageFormat;
import java.util.List;

import com.atlassian.annotations.ExperimentalApi;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Encapsulates the entity property key and object reference data.
 *
 * @since v6.2
 */
@ExperimentalApi
public class Property
{
    public static final Function<String, List<String>> SPLIT_STRING = new Function<String, List<String>>()
    {
        @Override
        public List<String> apply(final String input)
        {
            return ImmutableList.copyOf(input.split("\\."));
        }
    };
    private static final Joiner joiner = Joiner.on('.');
    private final List<String> keys;
    private final List<String> objectReferences;

    public Property(final List<String> keys, final List<String> objectReferences)
    {
        this.keys = ImmutableList.copyOf(Iterables.concat(Collections2.transform(keys, SPLIT_STRING)));
        this.objectReferences = ImmutableList.copyOf(Iterables.concat(Collections2.transform(objectReferences, SPLIT_STRING)));
    }

    /**
     * @return the key of the entity property.
     */
    public List<String> getKeys()
    {
        return keys;
    }

    /**
     * @return the path to the searched json value.
     */
    public List<String> getObjectReferences()
    {
        return objectReferences;
    }

    public String getAsPropertyString()
    {
        return MessageFormat.format("{0}${1}", getKeysAsString(), getObjectReferencesAsString());
    }

    public String getKeysAsString()
    {
        return joiner.join(keys);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) { return true; }
        if (!(obj instanceof Property)) { return false; }

        final Property property = (Property) obj;
        return Objects.equal(keys, property.keys)
                && Objects.equal(objectReferences, property.objectReferences);
    }

    @Override
    public String toString()
    {

        return String.format("[%s].%s", getKeysAsString(), getObjectReferencesAsString());
    }

    public String getObjectReferencesAsString()
    {
        return joiner.join(getObjectReferences());
    }
}
