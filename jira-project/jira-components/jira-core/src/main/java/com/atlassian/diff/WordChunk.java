package com.atlassian.diff;

import java.util.ArrayList;
import java.util.List;

public class WordChunk implements DiffChunk
{
    private DiffType type;
    private List<CharacterChunk> characterChunks;
    private String text;

    public WordChunk(DiffType type, String text)
    {
        this.type = type;
        this.text = text;
    }

    // HACK - Could WordChunk be split like DiffLine into String and Chunk versions?
    public WordChunk(DiffType type, List<CharacterChunk> characterChunks)
    {
        this.type = type;
        this.characterChunks = new ArrayList<CharacterChunk>(characterChunks);
    }

    public DiffType getType()
    {
        return type;
    }

    public String getText()
    {
        return text;
    }

    public boolean isChangedChunk()
    {
        return (type == DiffType.CHANGED_WORDS);
    }

    public List<CharacterChunk> getCharacterChunks()
    {
        return characterChunks;
    }

    public String toString()
    {
        return "" + type + " : " + ((text != null) ? text : characterChunks);
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

        final WordChunk wordChunk = (WordChunk) o;

        if (characterChunks != null ? !characterChunks.equals(wordChunk.characterChunks) : wordChunk.characterChunks != null)
        {
            return false;
        }
        if (text != null ? !text.equals(wordChunk.text) : wordChunk.text != null)
        {
            return false;
        }
        if (type != wordChunk.type)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (characterChunks != null ? characterChunks.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }
}
