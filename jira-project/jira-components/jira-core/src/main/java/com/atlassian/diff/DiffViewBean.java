package com.atlassian.diff;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.CollectionUtil;
import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A wrapper class to provide additional views on a list of {@link com.atlassian.diff.DiffChunk}s retrieved from the
 * {@link com.atlassian.diff.WordLevelDiffer}.
 *
 * @since v4.0
 */
public class DiffViewBean
{
    private static final Logger log = Logger.getLogger(DiffViewBean.class);

    private final List<DiffChunk> chunks;

    DiffViewBean(final List<DiffChunk> chunks)
    {
        this.chunks = chunks;
    }

    /**
     * @return only the chunks that would have been present in the original text i.e. no Added Chunks
     */
    public List<DiffChunk> getOriginalChunks()
    {
        return getFilteredList(chunks, CollectionBuilder.newBuilder(DiffType.ADDED_WORDS, DiffType.ADDED_CHARACTERS, DiffType.ADDED_LINES).asSet());
    }

    /**
     * @return only the chunks that are present in the revised text i.e. no Deleted Chunks
     */
    public List<DiffChunk> getRevisedChunks()
    {
        return getFilteredList(chunks, CollectionBuilder.newBuilder(DiffType.DELETED_WORDS, DiffType.DELETED_CHARACTERS, DiffType.DELETED_LINES).asSet());
    }

    /**
     * @return all the chunks in a single list
     */
    public List<DiffChunk> getUnifiedChunks()
    {
        return CollectionUtil.copyAsImmutableList(chunks);
    }

    /**
     * Filters some source chunks based on a set of excluded {@link com.atlassian.diff.DiffType}s.
     * Will go down into the {@link com.atlassian.diff.CharacterChunk} level as well.
     *
     * @param source the list of chunks to filter
     * @param excludedTypes the diff types to exclude
     * @return an unmodifiable list of the remaining chunks
     */
    static List<DiffChunk> getFilteredList(final List<DiffChunk> source, final Set<DiffType> excludedTypes)
    {
        final List<DiffChunk> result = new ArrayList<DiffChunk>(source.size());

        for (DiffChunk chunk : source)
        {
            if (!excludedTypes.contains(chunk.getType()))
            {
                // Now run through and make sure there are no invalid subchunks
                if (chunk instanceof WordChunk)
                {
                    WordChunk wordChunk = (WordChunk)chunk;
                    if (wordChunk.getCharacterChunks() != null)
                    {
                        final List<CharacterChunk> notDeletedChunks = getFilteredListOfCharacterChunks(wordChunk.getCharacterChunks(), excludedTypes);
                        chunk = new WordChunk(wordChunk.getType(), notDeletedChunks);
                    }
                }
                result.add(chunk);
            }
        }

        return CollectionUtil.copyAsImmutableList(result);
    }

    /**
     * @see #getFilteredList(java.util.List, java.util.Set)
     * @param source the list of chunks to filter
     * @param excludedTypes the diff types to exclude
     * @return an unmodifiable list of the remaining chunks
     */
    private static List<CharacterChunk> getFilteredListOfCharacterChunks(final List<CharacterChunk> source, final Set<DiffType> excludedTypes)
    {
        if (source != null)
        {
            final List<CharacterChunk> result = new ArrayList<CharacterChunk>(source.size());

            for (CharacterChunk chunk : source)
            {
                if (!excludedTypes.contains(chunk.getType()))
                {
                    result.add(chunk);
                }
            }

            return CollectionUtil.copyAsImmutableList(result);
        }
        return Collections.emptyList();
    }

    /**
     * Creates a {@link com.atlassian.diff.DiffViewBean} for the given original and revised text lines.
     *
     * @param originalLine the original line of text
     * @param revisedLine the revised line of text
     * @return the diff view bean; or null if there was an exception thrown while calculating the diff.
     */
    public static DiffViewBean createWordLevelDiff(String originalLine, String revisedLine)
    {
        try
        {
            return new DiffViewBean(WordLevelDiffer.diffLine(originalLine, revisedLine));
        }
        catch (DifferentiationFailedException e)
        {
            log.warn(e, e);
        }
        return null;
    }
}
