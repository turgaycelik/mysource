package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilderFactory;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * @since v6.0.3
 */
public class IssueBeanBuilderTest
{
    @Mock
    private FieldLayoutManager fieldLayoutManager;
    @Mock
    private JiraAuthenticationContext authContext;
    @Mock
    private FieldManager fieldManager;
    @Mock
    private ResourceUriBuilder resourceUriBuilder;
    @Mock
    private BeanBuilderFactory beanBuilderFactory;
    @Mock
    private ContextUriInfo contextUriInfo;
    @Mock
    private Issue issue;
    @Mock
    IncludedFields fieldsToInclude;
    @Mock
    private IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory;
    @Mock
    private IssueWorkflowManager issueWorkflowManager;

    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);

    private IssueBeanBuilder issueBeanBuilder;

    @Before
    public void setUp() throws Exception
    {
        issueBeanBuilder = new IssueBeanBuilder(fieldLayoutManager, authContext, fieldManager,
                resourceUriBuilder, beanBuilderFactory, contextUriInfo, issue, fieldsToInclude,
                issueLinkBeanBuilderFactory, issueWorkflowManager);

    }

    @Test
    public void exceptionsThrownFromCustomFieldsShouldBeIgnored()
    {
        FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);
        final OrderableField restAwareField = mock(OrderableField.class, withSettings().extraInterfaces(RestAwareField.class));
        when(fieldLayoutItem.getOrderableField()).thenReturn(restAwareField);
        when(((RestAwareField) restAwareField).getJsonFromIssue(any(Issue.class), anyBoolean(), eq(fieldLayoutItem))).thenThrow(new RuntimeException());

        FieldJsonRepresentation fieldValue = null;
        try
        {
            fieldValue = issueBeanBuilder.getFieldValue(fieldLayoutItem, issue);
        }
        catch (RuntimeException e)
        {
            fail("Exception should not propagate");

        }

        assertThat("Exception should be translated to null", fieldValue, nullValue());
    }
}
