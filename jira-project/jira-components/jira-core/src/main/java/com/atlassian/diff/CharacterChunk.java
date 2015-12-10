package com.atlassian.diff;

/**
 * Represents a group of characters sharing the same diff type - unchanged, added or deleted.
 */
public class CharacterChunk implements DiffChunk
{
    private final DiffType type;
    private final String text;

    public CharacterChunk(DiffType type, String text)
    {
        this.type = type;
        this.text = text;
    }

    public DiffType getType()
    {
        return type;
    }

    public String getText()
    {
        return text;
    }

    public String toString()
    {
        return "" + type + " : " + text;
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

        final CharacterChunk that = (CharacterChunk) o;

        if (text != null ? !text.equals(that.text) : that.text != null)
        {
            return false;
        }
        if (type != that.type)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }
}