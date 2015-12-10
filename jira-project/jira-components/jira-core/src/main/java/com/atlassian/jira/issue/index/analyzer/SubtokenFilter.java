package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * This Filter does some final filtering on the Tokens returned by the standard Lucene tokenizers in order to create the
 * exact tokens required for JIRA.
 *
 * <p/>
 * Currently, the StandardTokenizer takes anything of the 'alpha.alpha.alpha' form, and keeps it all together, because
 * it htinks it may be a server hostname (like "www.atlassian.com").
 * This is useful, however it prevents searches on the words between the dots.
 * An example is searching for 'NullPointerException' when 'java.lang.NullPointerException' has
 * been indexed.
 * This filter tokenizes the individual words, as well as the full phrase, allowing searching to
 * be done on either. (JRA-6397)
 * <p/>
 * <b>In addition</b>, a comma separated list of numbers (eg "123,456,789") is not tokenized at the commas.
 * This prevents searching on just "123".
 * This filter tokenizes the individual numbers, as well as the full phrase, allowing searching to
 * be done on either. (JRA-7774)
 */
public class SubtokenFilter extends TokenFilter
{
    private static final String TOKEN_TYPE_HOST = StandardTokenizer.TOKEN_TYPES[ClassicTokenizer.HOST];
    private static final String TOKEN_TYPE_NUM = StandardTokenizer.TOKEN_TYPES[ClassicTokenizer.NUM];
    private static final String TOKEN_TYPE_EXCEPTION = "EXCEPTION";

    private CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private PositionIncrementAttribute incrementAttribute =  addAttribute(PositionIncrementAttribute.class);
    private TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

    private State current;
    private String nextType;
    private List<CharSequence> subtokenStack = newLinkedList();

    public SubtokenFilter(TokenStream tokenStream)
    {
        super(tokenStream);
    }

    @Override
    public final boolean incrementToken() throws IOException
    {
        if (!subtokenStack.isEmpty())
        {
            restoreState(current);

            final CharSequence remove = subtokenStack.remove(0);
            termAttribute.setLength(0).append(remove);
            incrementAttribute.setPositionIncrement(0);
            typeAttribute.setType(nextType);

            return true;
        }

        if (!input.incrementToken())
        {
            return false;
        }

        if (TOKEN_TYPE_HOST.equals(typeAttribute.type()) || TypeAttribute.DEFAULT_TYPE.equals(typeAttribute.type()))
        {
            addSubtokensToStack('.', TOKEN_TYPE_EXCEPTION);
        }
        //Comma separated alphanum are not separated correctly (JRA-7774)
        else if (TOKEN_TYPE_NUM.equals(typeAttribute.type()))
        {
            addSubtokensToStack(',', TOKEN_TYPE_NUM);
        }

        return true;
    }

    private void addSubtokensToStack(char separatorChar, String newTokenType)
    {
        char[] termBuffer = termAttribute.buffer();
        int termLength = termAttribute.length();
        int offset = 0;

        // We iterate over the array, trying to find the separatorChar ('.' or ',')
        for (int index = 0; index <= termLength; index++)
        {
            // Note that we actually iterate past the last character in the array. At this point index == termLength.
            // We must check for this condition first to stop ArrayIndexOutOfBoundsException.
            // Being at the end of the array is a subtoken border just like the separator character ('.'), except we don't want to
            // add a duplicate token if no separator was already found. Hence we also check for offset > 0.
            if ((index < termLength && termBuffer[index] == separatorChar)
                 || (index == termLength && offset > 0))
            {
                int subtokenLength = index - offset;
                // Check that this is not an "empty" subtoken
                if (subtokenLength > 0)
                {
                    if (subtokenStack.isEmpty())
                    {
                        nextType = newTokenType;
                        current = captureState();
                    }
                    subtokenStack.add(termAttribute.subSequence(offset, subtokenLength + offset));
                }
                offset = index + 1;
            }
        }
    }

    @Override
    public void reset() throws IOException
    {
        super.reset();
        current = null;
        nextType = null;
        subtokenStack.clear();
    }
}