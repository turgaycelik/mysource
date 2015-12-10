package com.atlassian.jira.jql.resolver;

import java.util.List;

/**
 * Turns a searchable value (operand) (such as what may be typed into the right side of a clause) into an indexed value.
 * Operands may be represented by name or some other format that doesn't exactly match the indexed value and
 * implementations of this perform the transformation. In some cases (notably numeric types) there would be little or
 * no transformation of the original value held by the operand.
 * <p/>
 * Multiple values are returned because lookups cannot be guaranteed to be 1:1 as in name to id lookups for
 * certain issue fields.
 *
 * @since 4.0
 */
public interface IndexInfoResolver<T>
{
    /**
     * Provides the values in the index for the operand with the given String value.
     *
     * @param rawValue the value whose indexed term equivalent is to be returned.
     * @return the values to put or search for in the index, possibly empty, never containing null.
     */
    public List<String> getIndexedValues(String rawValue);

    /**
     * Provides the values in the index for the single value operand with the given Long value.
     *
     * @param rawValue the value whose indexed term equivalent is to be returned.
     * @return the values to put or search for in the index, possibly empty, never containing null.
     */
    public List<String> getIndexedValues(Long rawValue);

    /**
     * Gets an indexed value from a domain object.
     *
     * @param indexedObject the domain object. Does not accept null.
     * @return the indexed value.
     */
    String getIndexedValue(T indexedObject);
}
