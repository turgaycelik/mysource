package com.atlassian.diff;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestCharLevelDiffer
{
    @Test
    public void testTweakedChunksForLinkBracketing() throws Exception
    {
        String deletedChunkWords = "http://dashboards-test.atlassian.com,";
        String addedChunkWords = "[http://dashboards-test.atlassian.com],";

        List<CharacterChunk> tweakedChunks = CharLevelDiffer.getCharacterChunks(deletedChunkWords, addedChunkWords);
        assertEquals(4, tweakedChunks.size());

        assertChunk(tweakedChunks.get(0), DiffType.ADDED_CHARACTERS, "[");
        assertChunk(tweakedChunks.get(1), DiffType.UNCHANGED, "http://dashboards-test.atlassian.com");
        assertChunk(tweakedChunks.get(2), DiffType.ADDED_CHARACTERS, "]");
        assertChunk(tweakedChunks.get(3), DiffType.UNCHANGED, ",");
    }

    @Test
    public void testTweakedChunksForCharMove() throws Exception
    {
        String deletedChunkWords = "all *Engineering";
        String addedChunkWords = "*all Engineering";

        List<CharacterChunk> tweakedChunks = CharLevelDiffer.getCharacterChunks(deletedChunkWords, addedChunkWords);
        assertEquals(4, tweakedChunks.size());

        assertChunk(tweakedChunks.get(0), DiffType.ADDED_CHARACTERS, "*");
        assertChunk(tweakedChunks.get(1), DiffType.UNCHANGED, "all ");
        assertChunk(tweakedChunks.get(2), DiffType.DELETED_CHARACTERS, "*");
        assertChunk(tweakedChunks.get(3), DiffType.UNCHANGED, "Engineering");
    }

    @Test
    public void testTweakedChunksForSpaceInsertion() throws Exception
    {
        String deletedChunkWords = "Management\\\\";
        String addedChunkWords = "Management \\\\";

        List<CharacterChunk> tweakedChunks = CharLevelDiffer.getCharacterChunks(deletedChunkWords, addedChunkWords);
        assertEquals(3, tweakedChunks.size());

        assertChunk(tweakedChunks.get(0), DiffType.UNCHANGED, "Management");
        assertChunk(tweakedChunks.get(1), DiffType.ADDED_CHARACTERS, " ");
        assertChunk(tweakedChunks.get(2), DiffType.UNCHANGED, "\\\\");
    }

    @Test
    public void testTweakedChunksForWordChange() throws Exception
    {
        String deletedChunkWords = "ABC";
        String addedChunkWords = "XYZ";

        List<CharacterChunk> tweakedChunks = CharLevelDiffer.getCharacterChunks(deletedChunkWords, addedChunkWords);
        assertNull(tweakedChunks);
    }

    @Test
    public void testTweakedChunksForWordLonger() throws Exception
    {
        String deletedChunkWords = "ABC";
        String addedChunkWords = "ABCD";

        List<CharacterChunk> tweakedChunks = CharLevelDiffer.getCharacterChunks(deletedChunkWords, addedChunkWords);
        assertEquals(2, tweakedChunks.size());

        assertChunk(tweakedChunks.get(0), DiffType.UNCHANGED, "ABC");
        assertChunk(tweakedChunks.get(1), DiffType.ADDED_CHARACTERS, "D");
    }

    private void assertChunk(DiffChunk actualChunk, DiffType expectedType, String expectedText)
    {
        assertEquals(expectedType, actualChunk.getType());
        assertEquals(expectedText, actualChunk.getText());
    }
}
