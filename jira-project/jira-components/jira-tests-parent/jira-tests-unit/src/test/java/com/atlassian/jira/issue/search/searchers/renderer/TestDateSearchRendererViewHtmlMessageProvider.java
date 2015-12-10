package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.issue.search.searchers.renderer.DateSearchRendererViewHtmlMessageProvider.Result;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith (MockitoJUnitRunner.class)
public class TestDateSearchRendererViewHtmlMessageProvider
{
    private DateSearcherConfig config = new DateSearcherConfig("test", new ClauseNames("test"), "test");

    @Test
    public void nullIfBothFieldsAreNull()
    {
        assertNull(getResult(null, null, "test"));
    }

    @Test
    public void testBothFieldsAreNotNull()
    {
        assertResult("-2m", "-1m", "period.fromago.toago:120:60", true, true);

        assertResult("-2m", "0", "period.fromago.tonow:120", true, true);

        assertResult("-2m", "1m", "period.fromago.tofromnow:120:60", true, true);

        assertResult("0", "2m", "period.fromnow.tofromnow:120", true, true);
        assertResult("0", "2m", "period.fromnow.tofromnow:120", true, true);

        assertResult("1m", "2m", "period.fromfromnow.tofromnow:60:120", true, true);
        assertResult("1m", "2m", "period.fromfromnow.tofromnow:60:120", true, true);

        assertResult("0", "0", "period.fromnow.tonow", true, true);
        assertResult("0", "0", "period.fromnow.tonow", true, true);

        assertResult("0", "1m", "navigator.filter.duedate", "duedate.dueinnext.overdue:60", true, true);
        assertResult("0", "0", "navigator.filter.duedate", "period.fromnow.tonow", true, true);
    }

    @Test
    public void testOnlyPreviousFieldIsNotNull()
    {
        assertResult("-1m", null, "withinthelast:60", true, false);

        assertResult("0", null, "withinthelast:0", true, false);

        assertResult("1m", null, "withinthelast:60", true, false);
    }

    @Test
    public void testOnlyNextFieldIsNotNull()
    {
        assertResult(null, "value", "morethan:value", false, true);
        assertResult(null, "-value", "morethan:value", false, true);

        assertResult(null, "-1m", "morethanago:60", false, true);

        assertResult(null, "0", "morethannow", false, true);

        assertResult(null, "1m", "morethanfromnow:60", false, true);

        assertResult(null, "0", "navigator.filter.duedate", "duedate.nowoverdue", false, true);
        assertResult(null, "1m", "navigator.filter.duedate", "duedate.dueinnext.only:60", false, true);
        assertResult(null, "-1m", "navigator.filter.duedate", "morethanago:60", false, true);
    }

    private void assertResult(String previousFieldValue, String nextFieldValue,
            String expectedMessage, boolean expectedPrevious, boolean expectedNext)
    {
        Result result = getResult(previousFieldValue, nextFieldValue);
        assertEquals(expectedMessage, result.message);
        assertEquals(expectedPrevious, result.previous);
        assertEquals(expectedNext, result.next);
    }

    private void assertResult(String previousFieldValue, String nextFieldValue, String searchNameKey,
            String expectedMessage, boolean expectedPrevious, boolean expectedNext)
    {
        Result result = getResult(previousFieldValue, nextFieldValue, searchNameKey);
        assertEquals(expectedMessage, result.message);
        assertEquals(expectedPrevious, result.previous);
        assertEquals(expectedNext, result.next);
    }

    private Result getResult(String previousFieldValue, String nextFieldValue)
    {
        return getResult(previousFieldValue, nextFieldValue, "test");
    }

    @SuppressWarnings("unchecked")
    private Result getResult(String previousFieldValue, String nextFieldValue, String searchNameKey)
    {
        FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        fieldValuesHolder.put(config.getPreviousField(), previousFieldValue);
        fieldValuesHolder.put(config.getNextField(), nextFieldValue);

        return new DateSearchRendererViewHtmlMessageProvider(new MockMessageResolver(),
                fieldValuesHolder, config, searchNameKey)
                .getResult();
    }

    private static class MockMessageResolver implements DateSearchRendererViewHtmlMessageProvider.MessageResolver
    {
        @Override
        public String getText(String key, String... parameters)
        {
            StringBuilder sb = new StringBuilder(key.substring("navigator.filter.date.".length()));
            for (String parameter : parameters)
            {
                sb.append(":").append(parameter);
            }

            return sb.toString();
        }

        @Override
        public String prettyPrint(long periodOffSet)
        {
            return String.valueOf(periodOffSet);
        }
    }
}
