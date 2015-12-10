package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.index.analyzer.SubtokenFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.Reader;

/**
 * This class wraps the given analyzer.
 * <p/>
 * The {@link #tokenStream(String,java.io.Reader)} method wraps the resulting
 * {@link org.apache.lucene.analysis.TokenStream} in the {@link com.atlassian.jira.issue.index.analyzer.SubtokenFilter}.
 * <p/>
 * Note: This works on more than just Java exceptions due to the functionality implemented by {@link com.atlassian.jira.issue.index.analyzer.SubtokenFilter}
 * (JRA-7774).
 *
 * @see com.atlassian.jira.issue.index.analyzer.SubtokenFilter
 * @since v3.12
 */
public class JavaExceptionAnalyzer extends Analyzer
{
    private final Analyzer analyzer;

    /**
     * Constructs a new instance and wraps the given analyzer
     *
     * @param analyzer analyzer to wrap
     */
    public JavaExceptionAnalyzer(Analyzer analyzer)
    {
        this.analyzer = analyzer;
    }

    /**
     * Returns the token stream of the underlying analyzer with ExceptionFilter wrapped around it
     *
     * @param fieldName field name
     * @param reader    reader
     * @return token stream of the underlying analyzer with ExceptionFilter wrapped around it
     */
    public final TokenStream tokenStream(String fieldName, Reader reader)
    {
        return new SubtokenFilter(analyzer.tokenStream(fieldName, reader));
    }

    @Override
    public final TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException
    {
        return super.reusableTokenStream(fieldName, reader);
    }

    public int getPositionIncrementGap(String fieldName)
    {
        return analyzer.getPositionIncrementGap(fieldName);
    }

}
