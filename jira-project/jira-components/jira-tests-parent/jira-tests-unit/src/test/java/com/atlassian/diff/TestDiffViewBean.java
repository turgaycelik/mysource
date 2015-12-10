package com.atlassian.diff;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestDiffViewBean
{
    @Test
    public void testGetFilteredListOneGood() throws Exception
    {
        List<DiffChunk> originalChunks = CollectionBuilder.<DiffChunk>newBuilder(
                new WordChunk(DiffType.UNCHANGED, "project =")
        ).asList();

        final List<DiffChunk> result = DiffViewBean.getFilteredList(originalChunks, Collections.singleton(DiffType.ADDED_WORDS));
        assertEquals(originalChunks, result);
    }

    @Test
    public void testGetFilteredListOneGoodOneBad() throws Exception
    {
        List<DiffChunk> originalChunks = CollectionBuilder.<DiffChunk>newBuilder(
                new WordChunk(DiffType.UNCHANGED, "project ="),
                new WordChunk(DiffType.ADDED_WORDS, "project =")
        ).asList();

        List<DiffChunk> expectedChunks = CollectionBuilder.<DiffChunk>newBuilder(
                new WordChunk(DiffType.UNCHANGED, "project =")
        ).asList();

        final List<DiffChunk> result = DiffViewBean.getFilteredList(originalChunks, Collections.singleton(DiffType.ADDED_WORDS));
        assertEquals(expectedChunks, result);
    }

    @Test
    public void testGetFilteredListNestedBadCharacterChunks() throws Exception
    {
        List<DiffChunk> originalChunks = CollectionBuilder.<DiffChunk>newBuilder(
                new WordChunk(DiffType.UNCHANGED, "project ="),
                new WordChunk(DiffType.CHANGED_WORDS, CollectionBuilder.<CharacterChunk>newBuilder(
                        new CharacterChunk(DiffType.UNCHANGED, "abc"),
                        new CharacterChunk(DiffType.DELETED_CHARACTERS, "123")
                ).asList())
        ).asList();

        List<DiffChunk> expectedChunks = CollectionBuilder.<DiffChunk>newBuilder(
                new WordChunk(DiffType.UNCHANGED, "project ="),
                new WordChunk(DiffType.CHANGED_WORDS, CollectionBuilder.<CharacterChunk>newBuilder(
                        new CharacterChunk(DiffType.UNCHANGED, "abc")
                ).asList())
        ).asList();

        final List<DiffChunk> result = DiffViewBean.getFilteredList(originalChunks, Collections.singleton(DiffType.DELETED_CHARACTERS));
        assertEquals(expectedChunks, result);
    }

    @Test
    public void testGetFilteredListNestedBadCharacterChunksMultipleExclusions() throws Exception
    {
        List<DiffChunk> originalChunks = CollectionBuilder.<DiffChunk>newBuilder(
                new WordChunk(DiffType.UNCHANGED, "project ="),
                new WordChunk(DiffType.CHANGED_WORDS, CollectionBuilder.<CharacterChunk>newBuilder(
                        new CharacterChunk(DiffType.UNCHANGED, "abc"),
                        new CharacterChunk(DiffType.DELETED_CHARACTERS, "123"),
                        new CharacterChunk(DiffType.ADDED_CHARACTERS, "XYZ")
                ).asList()),
                new WordChunk(DiffType.DELETED_WORDS, "issuetypes")
        ).asList();

        List<DiffChunk> expectedChunks = CollectionBuilder.<DiffChunk>newBuilder(
                new WordChunk(DiffType.UNCHANGED, "project ="),
                new WordChunk(DiffType.CHANGED_WORDS, CollectionBuilder.<CharacterChunk>newBuilder(
                        new CharacterChunk(DiffType.UNCHANGED, "abc"),
                        new CharacterChunk(DiffType.ADDED_CHARACTERS, "XYZ")
                ).asList())
        ).asList();

        final List<DiffChunk> result = DiffViewBean.getFilteredList(originalChunks, CollectionBuilder.newBuilder(DiffType.DELETED_CHARACTERS, DiffType.DELETED_WORDS).asSet());
        assertEquals(expectedChunks, result);
    }
}
