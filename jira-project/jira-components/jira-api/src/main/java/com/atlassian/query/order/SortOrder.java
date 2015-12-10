package com.atlassian.query.order;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;

/**
 * An enumeration the defines the possible ordering for a Sort.
 *
 * @since 4.0
 */
public enum SortOrder
{
    /**
     * The ascending order.
     */
    ASC,

    /**
     * The descending order.
     */
    DESC;

    /**
     * Find the SortOrder represented in the passed string. The matching is based on the names of the value in the
     * enumeration. All matching is done in a case insensitive manner. The order {@link #ASC} will be returned if
     * no match is found.
     *
     * @param value the string to parse.
     * @return the parsed SortOrder.
     */
    public static SortOrder parseString(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return null;
        }
        else
        {
            //This is in English, so we don't have to worry about proper case folding.
            String fold = value.toUpperCase(Locale.ENGLISH);
            for (SortOrder order : SortOrder.values())
            {
                if (order.name().equals(fold))
                {
                    return order;
                }
            }
            return ASC;
        }
    }
}
