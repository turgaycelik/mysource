package com.atlassian.diff;

import org.apache.commons.jrcs.diff.*;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * Diffs a String as a sequence of chars; used when words are changed to determine if the word has been merely altered
 * or completely replaced.
 *
 * Could be used for diffing content in languages that don't use spaces for word separation.
 */
public class CharLevelDiffer
{
    static List<CharacterChunk> getCharacterChunks(String originalText, String revisedText)
        throws DifferentiationFailedException
    {
        if (StringUtils.isBlank(originalText) || StringUtils.isBlank(revisedText))
        {
            // can't be a character change if entire string added or removed!
            return null;
        }

        Character[] origChars = getCharacters(originalText);
        Character[] revdChars = getCharacters(revisedText);
        Revision revision = new Diff(origChars).diff(revdChars);

        int numDeltas = revision.size();
        if (numDeltas > 2)
        {
            // too many separate char changes (arbitrary limit)
            return null;
        }

        Chunk previousOriginalChunk = null;

        List<CharacterChunk> charChunks = new ArrayList<CharacterChunk>();
        for (int deltaIndex = 0; deltaIndex < numDeltas; deltaIndex++)
        {
            Delta delta = revision.getDelta(deltaIndex);
            if (delta instanceof ChangeDelta)
            {
                // if any characters are changed, this isn't a "tweak", it's a word deletion/addition.
                return null;
            }

            Chunk originalChunk = delta.getOriginal();
            Chunk revisedChunk = delta.getRevised();

            List<String> originalChunkChars = originalChunk.chunk();
            List<String> revisedChunkChars = revisedChunk.chunk();

            // Add any unchanged content in context before the delta
            CharacterChunk unchangedChunk = getUnchangedCharsBetweenChunks(originalText, previousOriginalChunk, originalChunk);
            if (unchangedChunk != null)
            {
                charChunks.add(unchangedChunk);
            }

            // Add the delta between original and revised
            if (delta instanceof DeleteDelta || delta instanceof ChangeDelta)
            {
                String deletedChunkText = StringUtils.join(originalChunkChars, "");
                charChunks.add(new CharacterChunk(DiffType.DELETED_CHARACTERS, deletedChunkText));
            }
            if (delta instanceof AddDelta || delta instanceof ChangeDelta)
            {
                String addedChunkText = StringUtils.join(revisedChunkChars, "");
                charChunks.add(new CharacterChunk(DiffType.ADDED_CHARACTERS, addedChunkText));
            }

            previousOriginalChunk = originalChunk;
        }

        // Add any trailing unchanged content in context.
        CharacterChunk unchangedChunk = getUnchangedCharsBetweenChunks(originalText, previousOriginalChunk, null);
        if (unchangedChunk != null)
        {
            charChunks.add(unchangedChunk);
        }

        return charChunks;
    }

    static CharacterChunk getUnchangedCharsBetweenChunks(String originalContent, Chunk chunkBefore, Chunk chunkAfter)
    {
        int unchangedStart = (chunkBefore != null) ? chunkBefore.last() + 1 : 0;
        int unchangedEnd = (chunkAfter != null) ? chunkAfter.first() : originalContent.length();
        if (unchangedEnd <= unchangedStart)
        {
            return null;
        }

        String chunkText = originalContent.substring(unchangedStart, unchangedEnd);

        return new CharacterChunk(DiffType.UNCHANGED, chunkText);
    }

    // HACK, but don't know a method to get Characters (not chars) from String.
    private static Character[] getCharacters(CharSequence string)
    {
        Character[] chars = new Character[string.length()];
        for (int i = 0; i < string.length(); i++)
        {
            chars[i] = string.charAt(i);
        }
        return chars;
    }
}
