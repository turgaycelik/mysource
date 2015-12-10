package com.atlassian.jira.jql.parser.antlr;

import org.antlr.runtime.CharStream;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A simple pair to hold both a position and a type (both integer). Implementing in here to keep the grammar a clean
 * as possible (yeah right).
 *
 * @since v4.0
 */
final class AntlrPosition
{
    private final int tokenType;

    private final int index;
    private final int charPosition;
    private final int linePosition;

    AntlrPosition(final int tokenType, CharStream stream)
    {
        this.index = stream.index();
        this.linePosition = stream.getLine();
        this.charPosition = stream.getCharPositionInLine();
        this.tokenType = tokenType;
    }
    
    int getLineNumber()
    {
        return linePosition;
    }

    int getCharNumber()
    {
        return charPosition;
    }

    int getTokenType()
    {
        return tokenType;
    }

    int getIndex()
    {
        return index;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("index", index).
                append("linePosition", linePosition).
                append("charPosition", charPosition).
                append("tokenType", tokenType).
                toString();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AntlrPosition that = (AntlrPosition) o;

        if (charPosition != that.charPosition)
        {
            return false;
        }
        if (index != that.index)
        {
            return false;
        }
        if (linePosition != that.linePosition)
        {
            return false;
        }
        if (tokenType != that.tokenType)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = tokenType;
        result = 31 * result + index;
        result = 31 * result + charPosition;
        result = 31 * result + linePosition;
        return result;
    }
}
