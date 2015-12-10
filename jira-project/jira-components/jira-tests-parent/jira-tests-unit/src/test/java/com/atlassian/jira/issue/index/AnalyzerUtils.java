package com.atlassian.jira.issue.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import junit.framework.Assert;

public class AnalyzerUtils
{
    public static SimpleToken[] tokensFromAnalysis(Analyzer analyzer, String text) throws IOException
    {
        TokenStream stream =
                analyzer.tokenStream("contents", new StringReader(text));
        CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
        PositionIncrementAttribute incrementAttribute =  stream.addAttribute(PositionIncrementAttribute.class);
        TypeAttribute typeAttribute = stream.addAttribute(TypeAttribute.class);
        OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);

        List<SimpleToken> tokenList = new ArrayList<SimpleToken>();
        while (stream.incrementToken())
        {
            if (charTermAttribute == null) break;

            tokenList.add(new SimpleToken(charTermAttribute.toString(), incrementAttribute.getPositionIncrement(), typeAttribute.type(), offsetAttribute.startOffset(), offsetAttribute.endOffset()));
        }

        return tokenList.toArray(new SimpleToken[tokenList.size()]);
    }


    public static void displayTokensWithPositions(Analyzer analyzer,
                                                  String text) throws IOException
    {
        SimpleToken[] tokens = tokensFromAnalysis(analyzer, text);

        int position = 0;

        for (SimpleToken token : tokens)
        {
            int increment = token.increment;

            if (increment > 0)
            {
                position = position + increment;
                System.out.println();
                System.out.print(position + ": ");
            }

            System.out.print("[" + token.term + "] ");
        }
        System.out.println();
    }

    public static void displayTokensWithFullDetails(Analyzer analyzer, String text) throws IOException
    {
        SimpleToken[] tokens = tokensFromAnalysis(analyzer, text);

        int position = 0;

        for (SimpleToken token : tokens)
        {
            int increment = token.increment;

            if (increment > 0)
            {
                position = position + increment;
                System.out.println();
                System.out.print(position + ": ");
            }

            System.out.print("[" + token.term + "|" +
                    token.type + "] ");
        }
        System.out.println();
    }

    public static void assertTokensEqual(Token[] tokens, String[] strings)
    {
        Assert.assertEquals(strings.length, tokens.length);

        for (int i = 0; i < tokens.length; i++)
        {
            Assert.assertEquals("index " + i, strings[i], tokens[i].term());
        }
    }
    public static void displayTokens(Analyzer analyzer, String text) throws IOException
    {
        SimpleToken[] tokens = tokensFromAnalysis(analyzer, text);

        for (SimpleToken token : tokens)
        {
            System.out.print("[" + token.term + "] ");
        }
    }

    private static class SimpleToken
    {
        private final String term;
        private final int increment;
        private final String type;
        private final int start;
        private final int end;

        public SimpleToken(String term, int increment, String type, int start, int end)
        {
            this.term = term;
            this.increment = increment;
            this.type = type;
            this.start = start;
            this.end = end;
        }
    }
}
