package com.atlassian.jira.issue.fields;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;

import org.apache.velocity.exception.VelocityException;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v4.0
 */
public class TestResolutionDateSystemField extends MockControllerTestCase
{
    @Mock
    ColumnViewDateTimeHelper mockColumnViewDateTimeHelper;

    @Mock
    VelocityTemplatingEngine templatingEngine;

    @Mock
    ApplicationProperties applicationProperties;

    @Mock
    JiraAuthenticationContext authContext;

    @Mock
    FieldLayoutItem fieldLayoutItem;

    Map displayParams;

    @Test
    public void testGetColumnViewHtmlNoDate() throws VelocityException
    {
        final Timestamp testDate = new Timestamp(123);

        final Issue mockIssue = getMock(Issue.class);
        expect(mockIssue.getResolutionDate()).andStubReturn(testDate);

        final Map<String, Object> params = new HashMap<String, Object>();
        final ResolutionDateSystemField field = new ResolutionDateSystemField(templatingEngine, applicationProperties, authContext, null, mockColumnViewDateTimeHelper, null)
        {
            @Override
            protected Map<String, Object> getVelocityParams(final FieldLayoutItem fieldLayoutItem, final I18nHelper i18nHelper, final Map displayParams, final Issue issue)
            {
                return params;
            }
        };

        expect(mockColumnViewDateTimeHelper.render(field, fieldLayoutItem, displayParams, mockIssue, testDate)).andStubReturn("Nuthing");

        mockController.replay();

        final String columnViewHtml = field.getColumnViewHtml(fieldLayoutItem, displayParams, mockIssue);
        assertThat(columnViewHtml, equalTo("Nuthing"));
    }

    @Test
    public void testGetColumnViewHtml() throws VelocityException
    {
        final Timestamp resDate = new Timestamp(1);

        final Issue mockIssue = mockController.getMock(Issue.class);
        expect(mockIssue.getResolutionDate()).andStubReturn(resDate);

        final Map<String, Object> params = new HashMap<String, Object>();
        final ResolutionDateSystemField field = new ResolutionDateSystemField(templatingEngine, applicationProperties, authContext, null, mockColumnViewDateTimeHelper, null)
        {
            @Override
            protected Map<String, Object> getVelocityParams(final FieldLayoutItem fieldLayoutItem, final I18nHelper i18nHelper, final Map displayParams, final Issue issue)
            {
                return params;
            }
        };

        expect(mockColumnViewDateTimeHelper.render(field, fieldLayoutItem, displayParams, mockIssue, resDate)).andStubReturn("12/10/08");
        mockController.replay();

        final String columnViewHtml = field.getColumnViewHtml(fieldLayoutItem, displayParams, mockIssue);
        assertThat(columnViewHtml, equalTo("12/10/08"));
    }

    @Before
    public void setUp() throws Exception
    {
        displayParams = MapBuilder.build("key1", new Object(), "key2", new Object());
        expect(authContext.getI18nHelper()).andStubReturn(new MockI18nHelper());
    }
}
