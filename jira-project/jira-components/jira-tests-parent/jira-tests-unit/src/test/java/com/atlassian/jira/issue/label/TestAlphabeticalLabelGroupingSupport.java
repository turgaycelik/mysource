package com.atlassian.jira.issue.label;

import java.util.Set;

import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestAlphabeticalLabelGroupingSupport
{
    @Test
    public void testSimple()
    {
        final Set<String> labels = CollectionBuilder.newBuilder("here", "are", "many", "variations", "of", "passages", "Lorem", "Ipsum",
                "available", "but", "the", "majority", "have", "suffered", "alteration", "in", "some", "form", "by", "injected",
                "humour", "or", "randomised", "words", "which", "dont", "look", "zoo", "even", "slightly", "believable", "If", "you", "1234", "3432", "5abcv").asSet();
        AlphabeticalLabelGroupingSupport labelGroup = new AlphabeticalLabelGroupingSupport(labels);
        assertEquals(CollectionBuilder.newBuilder("A-H", "I-O", "P-Y", "Z", "0-9").asSortedSet(), labelGroup.getKeys());
        assertEquals(CollectionBuilder.newBuilder("alteration", "are", "available", "believable", "by", "but", "dont", "even", "form", "have", "here", "humour").asSortedSet(), labelGroup.getContents("A-H"));
        assertEquals(CollectionBuilder.newBuilder("Ipsum", "injected", "in", "If", "Lorem", "look", "majority", "many", "of", "or").asSortedSet(), labelGroup.getContents("I-O"));
        assertEquals(CollectionBuilder.newBuilder("passages", "randomised", "suffered", "slightly", "some", "the", "variations", "words", "which", "you").asSortedSet(), labelGroup.getContents("P-Y"));
        assertEquals(CollectionBuilder.newBuilder("zoo").asSortedSet(), labelGroup.getContents("Z"));
        assertEquals(CollectionBuilder.newBuilder("5abcv", "3432", "1234").asSortedSet(), labelGroup.getContents("0-9"));
    }

    @Test
    public void testOneBucket()
    {
        final Set<String> labels = CollectionBuilder.newBuilder("here", "are", "many", "variations", "of", "passages").asSet();
        AlphabeticalLabelGroupingSupport labelGroup = new AlphabeticalLabelGroupingSupport(labels);
        assertEquals(CollectionBuilder.newBuilder("A-Z").asSortedSet(), labelGroup.getKeys());
        assertEquals(CollectionBuilder.newBuilder("are", "here", "many", "of", "passages", "variations").asSortedSet(), labelGroup.getContents("A-Z"));
    }

    @Test
    public void testSymbols()
    {
        final Set<String> labels = CollectionBuilder.newBuilder("here", "are", "many", "variations", "of", "passages", "Lorem", "Ipsum",
                "available", "but", "the", "majority", "have", "suffered", "alteration", "in", "some", "form", "by", "injected", "\u6309\u5f00\u53d1\u4eba\u5458").asSet();
        AlphabeticalLabelGroupingSupport labelGroup = new AlphabeticalLabelGroupingSupport(labels);
        assertEquals(CollectionBuilder.newBuilder("A-I", "J-Z", "\u6309").asSortedSet(), labelGroup.getKeys());
        assertEquals(CollectionBuilder.newBuilder("alteration", "available", "are", "but", "by", "form", "have", "here", "injected", "Ipsum", "in").asSortedSet(), labelGroup.getContents("A-I"));
        assertEquals(CollectionBuilder.newBuilder("Lorem", "majority", "many", "of", "passages", "suffered", "some", "the", "variations").asSortedSet(), labelGroup.getContents("J-Z"));
        assertEquals(CollectionBuilder.newBuilder("\u6309\u5f00\u53d1\u4eba\u5458").asSortedSet(), labelGroup.getContents("\u6309"));
    }
}
