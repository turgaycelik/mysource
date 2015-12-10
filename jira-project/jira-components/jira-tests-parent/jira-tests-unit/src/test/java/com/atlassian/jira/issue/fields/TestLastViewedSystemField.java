package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.util.LuceneUtils;

import org.apache.lucene.util.NumericUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static junit.framework.Assert.assertEquals;

@RunWith (MockitoJUnitRunner.class)
public class TestLastViewedSystemField
{
    LastViewedSystemField fieldUnderTest;

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private VelocityTemplatingEngine templatingEngine;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private JiraAuthenticationContext authenticationContext;
    @Mock
    private ColumnViewDateTimeHelper columnViewDateTimeHelper;
    @Mock
    private DateTimeFormatterFactory dateTimeFormatterFactory;
    @Mock
    private UserIssueHistoryManager historyManager;

    @Before
    public void setUp()
    {
        fieldUnderTest = new LastViewedSystemField(
                templatingEngine,
                applicationProperties,
                authenticationContext,
                columnViewDateTimeHelper,
                dateTimeFormatterFactory,
                historyManager
        );
    }

    @Test
    public void testShouldBeAbleToRetrieveValueOfFieldViaSorter()
    {
        Date expectedValue = new Date(2014, 0, 30, 0, 0, 0);
        LastViewedSystemField lastViewedSystemField = createLastViewedSystemField();
        Object valueFromField = lastViewedSystemField.getSorter().getValueFromLuceneField(NumericUtils.longToPrefixCoded(expectedValue.getTime() / 1000));
        assertEquals("the value should be a date", Date.class, valueFromField.getClass());
        assertEquals(expectedValue, valueFromField);
    }

    private LastViewedSystemField createLastViewedSystemField() {
        return new LastViewedSystemField(
                templatingEngine,
                applicationProperties,
                authenticationContext,
                columnViewDateTimeHelper,
                dateTimeFormatterFactory,
                historyManager
        );
    }
}
