package com.atlassian.diff;

import org.apache.commons.jrcs.diff.Chunk;
import org.apache.commons.jrcs.diff.Delta;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.apache.commons.jrcs.diff.Revision;
import org.junit.Test;

import java.util.List;

import static com.atlassian.diff.WordLevelDiffer.Word;
import static org.junit.Assert.assertEquals;

public class TestWordLevelDiffer
{
    public static String CR = "\r";
    public static String LF = "\n";

    @Test
    public void testDiffLineAdditions() throws DifferentiationFailedException
    {
        // Additions at ends.
        String originalLine = "a b";
        String revisedLine = "1 2 a b 3 4";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        assertChunk(chunks.get(0), DiffType.ADDED_WORDS, "1 2");
        assertChunk(chunks.get(1), DiffType.UNCHANGED, "a b");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, " 3 4");

        // Additions inside only
        revisedLine = "a 1 2 b";

        chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "a");
        assertChunk(chunks.get(1), DiffType.ADDED_WORDS, " 1 2");
        assertChunk(chunks.get(2), DiffType.UNCHANGED, " b");
    }

    @Test
    public void testDiffLineDeletions() throws DifferentiationFailedException
    {
        // Deletions from ends.
        String originalLine = "1 2 a b 3 4";
        String revisedLine = "a b";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        assertChunk(chunks.get(0), DiffType.DELETED_WORDS, "1 2");
        assertChunk(chunks.get(1), DiffType.UNCHANGED, " a b");
        assertChunk(chunks.get(2), DiffType.DELETED_WORDS, " 3 4");

        // Deletions inside only
        originalLine = "a 1 2 b";

        chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "a");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, " 1 2");
        assertChunk(chunks.get(2), DiffType.UNCHANGED, " b");
    }

    @Test
    public void testDiffLineChanges() throws DifferentiationFailedException
    {
        // Changes at ends.
        String originalLine = "1 2 a b c d 3 4";
        String revisedLine = "5 6 a b c d 7 8";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(5, chunks.size());

        assertChunk(chunks.get(0), DiffType.DELETED_WORDS, "1 2");
        assertChunk(chunks.get(1), DiffType.ADDED_WORDS, "5 6");
        assertChunk(chunks.get(2), DiffType.UNCHANGED, " a b c d");
        assertChunk(chunks.get(3), DiffType.DELETED_WORDS, " 3 4");
        assertChunk(chunks.get(4), DiffType.ADDED_WORDS, " 7 8");

        // Changes inside only
        originalLine = "5 6 e f g h 7 8";

        chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(4, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "5 6");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, " e f g h");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, " a b c d");
        assertChunk(chunks.get(3), DiffType.UNCHANGED, " 7 8");
    }

    @Test
    public void testWordsOfContext() throws DifferentiationFailedException
    {
        Word[] originalContent = WordLevelDiffer.tokenize("The quick black fox jumped on the lazy dog.");
        Word[] revisedContent = WordLevelDiffer.tokenize("The quick brown fox jumped over the lazy dog.");

        Delta delta;
        Chunk chunkBefore, chunkAfter;
        String wordsOfContext;

        Revision revision = new Diff(originalContent).diff(revisedContent);

        delta = revision.getDelta(0);
        chunkBefore = null;
        chunkAfter = delta.getOriginal();
        wordsOfContext = WordLevelDiffer.getUnchangedWordsBetweenChunks(originalContent, chunkBefore, chunkAfter).getText();
        assertEquals("The quick", wordsOfContext);

        delta = revision.getDelta(1);
        chunkBefore = chunkAfter;
        chunkAfter = delta.getOriginal();
        wordsOfContext = WordLevelDiffer.getUnchangedWordsBetweenChunks(originalContent, chunkBefore, chunkAfter).getText();
        assertEquals(" fox jumped", wordsOfContext);

        chunkBefore = chunkAfter;
        chunkAfter = null;
        wordsOfContext = WordLevelDiffer.getUnchangedWordsBetweenChunks(originalContent, chunkBefore, chunkAfter).getText();
        assertEquals(" the lazy dog.", wordsOfContext);
    }

    @Test
    public void testSpaceAdditions() throws Exception
    {
        String originalLine = "The quick brown foxjumped over the lazy dog.";
        String revisedLine  = "The quick brown fox jumped over the lazy dog.";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(4, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "The quick brown");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, " foxjumped");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, " fox jumped");
        assertChunk(chunks.get(3), DiffType.UNCHANGED, " over the lazy dog.");
    }
    @Test
    public void testChangeOfSpace() throws Exception
    {
        String originalLine = "The quick brown fox  jumped over the lazy dog.";
        String revisedLine  = "The quick brown fox jumped over the lazy dog.";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(1, chunks.size());

        final WordChunk chunk0 = (WordChunk)chunks.get(0);
        assertEquals(DiffType.UNCHANGED, chunk0.getType());
        assertEquals(originalLine, chunk0.getText());
    }

    // "numbers" and "numbers," should show up as a char change, not a word replacement.
    @Test
    public void testWordAndCharacterAdditions() throws Exception
    {
        String originalLine = "Update the port numbers for the test runner.";
        String revisedLine  = "Update the port numbers, dbnames and cluster.names for the test runner.";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "Update the port numbers");
        assertChunk(chunks.get(1), DiffType.ADDED_WORDS, ", dbnames and cluster.names");
        assertChunk(chunks.get(2), DiffType.UNCHANGED, " for the test runner.");
    }

    @Test
    public void testChangeInNewlines() throws Exception
    {
        String originalLine = "project = 'ABC'\nAND assignee is not empty";
        String revisedLine  = "project = 'ABC' AND assignee is empty";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(5, chunks.size());

        final DiffChunk chunk0 = chunks.get(0);
        assertEquals(DiffType.UNCHANGED, chunk0.getType());
        assertEquals("project = 'ABC'", chunk0.getText());

        final DiffChunk chunk1 = chunks.get(1);
        assertEquals(DiffType.DELETED_WORDS, chunk1.getType());
        assertEquals("\n", chunk1.getText());

        final DiffChunk chunk2 = chunks.get(2);
        assertEquals(DiffType.UNCHANGED, chunk2.getType());
        assertEquals("AND assignee is", chunk2.getText());

        final DiffChunk chunk3 = chunks.get(3);
        assertEquals(DiffType.DELETED_WORDS, chunk3.getType());
        assertEquals(" not", chunk3.getText());

        final DiffChunk chunk4 = chunks.get(4);
        assertEquals(DiffType.UNCHANGED, chunk4.getType());
        assertEquals(" empty", chunk4.getText());
    }


    @Test
    public void testEmptyLines() throws Exception
    {
        String originalLine = "";
        String revisedLine  = "";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(0, chunks.size());
    }

    @Test
    public void testModifiedEndOfLine() throws Exception
    {
        String originalLine = "1st\n2nd\n3rd";
        String revisedLine  = "1st\n2nd foo\n3rd";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        final DiffChunk chunk0 = chunks.get(0);
        assertEquals(DiffType.UNCHANGED, chunk0.getType());
        assertEquals("1st\n2nd", chunk0.getText());

        final DiffChunk chunk1 = chunks.get(1);
        assertEquals(DiffType.ADDED_WORDS, chunk1.getType());
        assertEquals(" foo", chunk1.getText());

        final DiffChunk chunk2 = chunks.get(2);
        assertEquals(DiffType.UNCHANGED, chunk2.getType());
        assertEquals("\n3rd", chunk2.getText());
    }

    @Test
    public void testDiffNumeric() throws Exception
    {
        // Alpha, numeric and underscore are all counted as parts of words.
        String originalLine = "Use foo2 to help with aa32aa stuff";
        String revisedLine = "Use foo3 to help with aa32bb stuff";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(7, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "Use");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, " foo2");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, " foo3");
        assertChunk(chunks.get(3), DiffType.UNCHANGED, " to help with");
        assertChunk(chunks.get(4), DiffType.DELETED_WORDS, " aa32aa");
        assertChunk(chunks.get(5), DiffType.ADDED_WORDS, " aa32bb");
        assertChunk(chunks.get(6), DiffType.UNCHANGED, " stuff");
    }

    @Test
    public void testDiffUnderscores() throws Exception
    {
        // Underscore is counted as parts of words, but dash is not
        String originalLine = "The V8 engine needs more fish-ye for Fist_Go";
        String revisedLine = "The V-8 engine needs more fish-eye for Fast_Go";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(9, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "The");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, " V8");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, " V-8");
        assertChunk(chunks.get(3), DiffType.UNCHANGED, " engine needs more fish-");
        assertChunk(chunks.get(4), DiffType.DELETED_WORDS, "ye");
        assertChunk(chunks.get(5), DiffType.ADDED_WORDS, "eye");
        assertChunk(chunks.get(6), DiffType.UNCHANGED, " for");
        assertChunk(chunks.get(7), DiffType.DELETED_WORDS, " Fist_Go");
        assertChunk(chunks.get(8), DiffType.ADDED_WORDS, " Fast_Go");
    }

    @Test
    public void testDiffSingleQuote() throws Exception
    {
        // Sometimes ' is a single-quote:
        String originalLine = "The 'turtle nec' sweater";
        String revisedLine = "The 'turtle neck' sweater";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(4, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "The 'turtle");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, " nec");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, " neck");
        assertChunk(chunks.get(3), DiffType.UNCHANGED, "' sweater");

        originalLine = "The 'purple neck' sweater";
        revisedLine = "The 'turtle neck' sweater";

        chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(4, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "The '");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, "purple");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, "turtle");
        assertChunk(chunks.get(3), DiffType.UNCHANGED, " neck' sweater");
    }

//    @Test
//    public void testDiffApostrophe() throws Exception
//    {
//        // Sometimes ' is an apostrophe
//        String originalLine = "I can't I won't I shouldn't do it.";
//        String revisedLine = "I cannot I wouldn't I should not do it.";
//
//        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
//        assertEquals(10, chunks.size());
//
//        assertChunk(chunks.get(0), DiffType.UNCHANGED, "I");
//        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, " can't");
//        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, " cannot");
//        assertChunk(chunks.get(3), DiffType.UNCHANGED, " I");
//        assertChunk(chunks.get(4), DiffType.DELETED_WORDS, " won't");
//        assertChunk(chunks.get(5), DiffType.ADDED_WORDS, " wouldn't");
//        assertChunk(chunks.get(6), DiffType.UNCHANGED, " I");
//        assertChunk(chunks.get(7), DiffType.DELETED_WORDS, " shouldn't");
//        assertChunk(chunks.get(8), DiffType.ADDED_WORDS, " should not");
//        assertChunk(chunks.get(9), DiffType.UNCHANGED, " do it.");
//    }

    @Test
    public void testDiffWithAccentedCharacters() throws Exception
    {
        // JRA-25882
        String originalLine = "Los Ni\u00f1as H\u00e9ros";
        String revisedLine = "Los Ni\u00f1os H\u00e9roes";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        String unchanged = "Los";
        assertChunk(chunks.get(0), DiffType.UNCHANGED, unchanged);
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, originalLine.substring(unchanged.length()));
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, revisedLine.substring(unchanged.length()));
    }

    @Test
    public void testDiffLineRussian() throws Exception
    {
        // JRA-25597
        String originalLine = "\u043e\u0442\u043a\u0440\u044b\u0442\u044c";
        String revisedLine = "\u043e\u0431\u0440\u0430\u0431\u043e\u0442\u0430\u043d";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(2, chunks.size());

        assertChunk(chunks.get(0), DiffType.DELETED_WORDS, originalLine);
        assertChunk(chunks.get(1), DiffType.ADDED_WORDS, revisedLine);
    }

    private void assertChunk(DiffChunk actualChunk, DiffType expectedType, String expectedText)
    {
        assertEquals(expectedType, actualChunk.getType());
        assertEquals(expectedText, actualChunk.getText());
    }

    @Test
    public void diffLineIgnoresEOLDifferenceCRvsCRLF() throws Exception
    {
        final String originalLine = getOriginalLineWithLineSeparator(CR);
        final String revisedLine = getRevisedlLineWithLineSeparator(CR + LF);

        final List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);

        assertDiffChunksOk(originalLine, getAddedWords(), chunks);
    }

    @Test
    public void diffLineIgnoresEOLDifferenceLFvsCRLF() throws Exception
    {
        final String originalLine = getOriginalLineWithLineSeparator(LF);
        final String revisedLine = getRevisedlLineWithLineSeparator(CR + LF);

        final List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);

        assertDiffChunksOk(originalLine, getAddedWords(), chunks);
    }

    @Test
    public void diffLineIgnoresEOLDifferenceCRLFvsCR() throws Exception
    {
        final String originalLine = getOriginalLineWithLineSeparator(CR + LF);
        final String revisedLine = getRevisedlLineWithLineSeparator(CR);

        final List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);

        assertDiffChunksOk(originalLine, getAddedWords(), chunks);
    }

    @Test
    public void diffLineIgnoresEOLDifferenceCRLFvsLF() throws Exception
    {
        final String originalLine = getOriginalLineWithLineSeparator(CR + LF);
        final String revisedLine = getRevisedlLineWithLineSeparator(LF);

        final List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);

        assertDiffChunksOk(originalLine, getAddedWords(), chunks);
    }

    @Test
    public void diffLineHappyEOLPath() throws Exception
    {
        final String originalLine = getOriginalLineWithLineSeparator(CR + LF);
        final String revisedLine = getRevisedlLineWithLineSeparator(CR + LF);

        final List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);

        assertDiffChunksOk(originalLine, getAddedWords(), chunks);
    }

    @Test
    public void diffLineIgnoresMultipleEOLDifferenceCRvsCRLF() throws Exception
    {
        final String originalLine = "one\n\n\ntwo";
        final String revisedLine = "one\r\r\n\ntwo three";

        final List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);

        assertDiffChunksOk(originalLine, " three", chunks);
    }

    @Test
    public void diffLineReportsAdditionalEOLAdded() throws Exception
    {
        final String originalLine = "one\r\n\rtwo";
        final String revisedLine = "one\r\n\r\n\ntwo three";

        final List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);

        assertEquals("Exactly 4 diff chunks are expected", 4, chunks.size());
        assertChunk(chunks.get(0), DiffType.UNCHANGED, "one\r\n\r");
        assertChunk(chunks.get(1), DiffType.ADDED_WORDS, "\n");
        assertChunk(chunks.get(2), DiffType.UNCHANGED, "two");
        assertChunk(chunks.get(3), DiffType.ADDED_WORDS, " three");
    }

    @Test
    public void diffLineEOLWithSingleSpaceBetweenLFs() throws Exception
    {
        final String originalLine = "one\n \ntwo";
        final String revisedLine = "one\r\n  \r\ntwo";

        final List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);

        assertEquals("Exactly 4 diff chunks are expected", 4, chunks.size());
        assertChunk(chunks.get(0), DiffType.UNCHANGED, "one\n");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, " \n");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, "  \r\n");
        assertChunk(chunks.get(3), DiffType.UNCHANGED, "two");
    }

    @Test
    public void diffLineEOLWithMultipleSpacesBetweenLFs() throws Exception
    {
        final String originalLine = "one\n  \ntwo";
        final String revisedLine = "one\r\n  \r\ntwo";

        final List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);

        assertEquals("Exactly 1 diff chunk is expected", 1, chunks.size());
        assertChunk(chunks.get(0), DiffType.UNCHANGED, originalLine);
    }

    @Test
    public void diffLineEOLWithMultipleSpacesAddedBetweenCRLFs() throws Exception
    {
        final String originalLine = "one\r\n  \r\ntwo";
        final String revisedLine = "one\r\n   \r\ntwo";

        final List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);

        assertEquals("Exactly 4 diff chunks are expected", 4, chunks.size());
        assertChunk(chunks.get(0), DiffType.UNCHANGED, "one\r\n");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, "  \r\n");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, "   \r\n");
        assertChunk(chunks.get(3), DiffType.UNCHANGED, "two");
    }

    private String getOriginalLineWithLineSeparator(final String separator)
    {
        return "And now" + separator + "for something completely different";
    }

    private String getRevisedlLineWithLineSeparator(final String separator)
    {
        return getOriginalLineWithLineSeparator(separator) + getAddedWords();
    }

    private String getAddedWords() {return ": the larch";}

    private void assertDiffChunksOk(final String unchanged, final String added, final List<DiffChunk> chunks)
    {
        assertEquals("Exactly 2 diff chunks are expected", 2, chunks.size());
        assertChunk(chunks.get(0), DiffType.UNCHANGED, unchanged);
        assertChunk(chunks.get(1), DiffType.ADDED_WORDS, added);
    }


}
