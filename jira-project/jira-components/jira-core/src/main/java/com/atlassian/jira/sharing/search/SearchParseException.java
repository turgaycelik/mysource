package com.atlassian.jira.sharing.search;

import com.atlassian.jira.sharing.SharedEntityColumn;

/**
 * There was a problem parsing the search parameters.
 * 
 * @since v3.13
 */
public class SearchParseException extends RuntimeException
{
    private final SharedEntityColumn column;

    public SearchParseException(final String message, final SharedEntityColumn column)
    {
        super(message);
        this.column = column;
    }

    public SearchParseException(final Throwable cause, final SharedEntityColumn column)
    {
        super(cause);
        this.column = column;
    }

    public SearchParseException(final String message, final Throwable cause, final SharedEntityColumn column)
    {
        super(message, cause);
        this.column = column;
    }

    public SharedEntityColumn getColumn()
    {
        return column;
    }
}
