package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.jira.util.I18nHelper;
import org.apache.lucene.queryParser.QueryParser;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestTextQueryValidator
{
    private TextQueryValidator validator;

    @Before
    public void setUp()
    {
        validator = new TextQueryValidator();
    }

    @Test
    public void validateEscapesQueryBeforeHandlingItToTheLuceneQueryParser() throws Exception
    {
        QueryParser queryParser = mock(QueryParser.class);
        String query = "query:1";

        validateQuery(queryParser, query);

        verify(queryParser).parse("query\\:1");
    }

    private void validateQuery(final QueryParser queryParser, final String query)
    {
        String fieldName = "any";
        String sourceFunction = "any";
        boolean shortMessage = false;
        I18nHelper i18nHelper = null;
        validator.validate(queryParser, query, fieldName, sourceFunction, shortMessage, i18nHelper);
    }
}
