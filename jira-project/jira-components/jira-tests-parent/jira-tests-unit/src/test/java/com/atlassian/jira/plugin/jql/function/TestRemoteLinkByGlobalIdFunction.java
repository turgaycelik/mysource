package com.atlassian.jira.plugin.jql.function;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.GetException;
import com.atlassian.jira.issue.link.DefaultRemoteIssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.issue.link.TestDefaultRemoteIssueLinkManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ranges;

import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.plugin.jql.function.RemoteLinksByGlobalIdFunction.FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestRemoteLinkByGlobalIdFunction
{
    public static final String SAMPLE_GLOBAL_ID = "a-global-id";
    public static final String SAMPLE_GLOBAL_ID2 = "another-global-id";
    // mocks for injections
    @Mock
    private IssueLinkManager issueLinkManager;
    @Mock
    private RemoteIssueLinkManager remoteIssueLinkManager;

    @InjectMocks
    private RemoteLinksByGlobalIdFunction function;

    @Mock
    private JqlFunctionModuleDescriptor moduleDescriptor;
    private MockI18nHelper i18nHelper;
    @Mock
    private User user;
    private QueryCreationContext queryCreationContext;


    @Before
    public void setUp() throws Exception
    {
        i18nHelper = new MockI18nHelper();
        when(moduleDescriptor.getI18nBean()).thenReturn(i18nHelper);
        function.init(moduleDescriptor);

        queryCreationContext = new QueryCreationContextImpl((ApplicationUser)null);
    }

    @Test
    public void testValidate()
    {
        final FunctionOperand operand = new FunctionOperand(FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID, SAMPLE_GLOBAL_ID);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);
        when(issueLinkManager.isLinkingEnabled()).thenReturn(true);
        final MessageSet result = function.validate(user, operand, clause);

        assertFalse(result.hasAnyMessages());
    }

    @Test
    public void testValidateLinkingDisabled()
    {
        i18nHelper.stubWith("jira.jql.function.issue.linking.disabled", "disabled " + FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID);
        final FunctionOperand operand = new FunctionOperand(FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);
        when(issueLinkManager.isLinkingEnabled()).thenReturn(false);
        final MessageSet result = function.validate(user, operand, clause);

        assertThat(result.getErrorMessages(), hasSize(1));
        assertThat(result.getErrorMessages(), hasItem("disabled " + FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID));
    }

    @Test
    public void testValidateNoArguments()
    {
        i18nHelper.stubWith("jira.jql.function.arg.incorrect.range", "error msg");
        i18nHelper.stubWith("jira.jql.function.remote.link.by.global.id.incorrect.usage", "usage");
        when(issueLinkManager.isLinkingEnabled()).thenReturn(true);
        final FunctionOperand operand = new FunctionOperand(FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);
        final MessageSet result = function.validate(user, operand, clause);

        assertThat(result.getErrorMessages(), hasSize(1));
        assertThat(result.getErrorMessages(), hasItem("error msg usage"));
    }

    @Test
    public void testValidateMoreThanMaxArguments()
    {
        i18nHelper.stubWith("jira.jql.function.arg.incorrect.range", "error msg");
        i18nHelper.stubWith("jira.jql.function.remote.link.by.global.id.incorrect.usage", "usage");
        when(issueLinkManager.isLinkingEnabled()).thenReturn(true);
        final Collection<String> arguments = Collections2.transform(
                Ranges.closedOpen(0, DefaultRemoteIssueLinkManager.MAX_GLOBAL_ID_LIST_SIZE_FOR_FIND + 1).asSet(DiscreteDomains.integers()),
                new Function<Integer, String>()
                {
                    @Override
                    public String apply(@Nullable final Integer input)
                    {
                        return SAMPLE_GLOBAL_ID + "-" + input;
                    }
                });
        final FunctionOperand operand = new FunctionOperand(FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID, arguments);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);
        final MessageSet result = function.validate(user, operand, clause);

        assertThat(result.getErrorMessages(), hasSize(1));
        assertThat(result.getErrorMessages(), hasItem("error msg usage"));
    }

    @Test
    public void testValidateMaxArguments()
    {
        i18nHelper.stubWith("jira.jql.function.arg.incorrect.range", "error msg");
        i18nHelper.stubWith("jira.jql.function.remote.link.by.global.id.incorrect.usage", "usage");
        when(issueLinkManager.isLinkingEnabled()).thenReturn(true);
        final Collection<String> arguments = Collections2.transform(
                Ranges.closedOpen(0, DefaultRemoteIssueLinkManager.MAX_GLOBAL_ID_LIST_SIZE_FOR_FIND).asSet(DiscreteDomains.integers()),
                new Function<Integer, String>()
                {
                    @Override
                    public String apply(@Nullable final Integer input)
                    {
                        return SAMPLE_GLOBAL_ID + "-" + input;
                    }
                });
        final FunctionOperand operand = new FunctionOperand(FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID, arguments);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);
        final MessageSet result = function.validate(user, operand, clause);

        assertThat(result.getErrorMessages(), hasSize(0));
    }

    @Test
    public void testGetValues() throws Exception
    {
        when(issueLinkManager.isLinkingEnabled()).thenReturn(true);

        final List<RemoteIssueLink> links = mockLinksForGlobalId(SAMPLE_GLOBAL_ID, 10001L, 10002L, 10003L);
        when(remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(ImmutableList.of(SAMPLE_GLOBAL_ID))).thenReturn(links);
        final FunctionOperand operand = new FunctionOperand(FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID, SAMPLE_GLOBAL_ID);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);
        final List<QueryLiteral> values = function.getValues(queryCreationContext, operand, clause);
        assertThat(values, hasSize(links.size()));
        for (int i = 0 ; i < links.size(); i++)
        {
            assertThat(values.get(i).getLongValue(), equalTo(links.get(i).getIssueId()));
        }
    }

    @Test
    public void testGetValuesMultipleArguments() throws Exception
    {
        when(issueLinkManager.isLinkingEnabled()).thenReturn(true);

        final List<RemoteIssueLink> links1 = mockLinksForGlobalId(SAMPLE_GLOBAL_ID, 10001L, 10003L, 10005L);
        final List<RemoteIssueLink> links2 = mockLinksForGlobalId(SAMPLE_GLOBAL_ID2, 10002L, 10004L, 10006L);
        final List<RemoteIssueLink> links = ImmutableList.copyOf(Iterables.concat(links1, links2));
        when(remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(eqCollection(ImmutableList.of(SAMPLE_GLOBAL_ID, SAMPLE_GLOBAL_ID2))))
                .thenReturn(links);
        final FunctionOperand operand = new FunctionOperand(FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID, SAMPLE_GLOBAL_ID, SAMPLE_GLOBAL_ID2);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);
        final List<QueryLiteral> values = function.getValues(queryCreationContext, operand, clause);
        assertThat(values, hasSize(links.size()));
        for (int i = 0 ; i < links.size(); i++)
        {
            assertThat(values.get(i).getLongValue(), equalTo(links.get(i).getIssueId()));
        }
    }

    @Test
    public void testGetValuesMultipleArgumentsOverlap() throws Exception
    {
        when(issueLinkManager.isLinkingEnabled()).thenReturn(true);

        final List<RemoteIssueLink> links1 = mockLinksForGlobalId(SAMPLE_GLOBAL_ID, 10001L, 10003L, 10005L);
        final List<RemoteIssueLink> links2 = mockLinksForGlobalId(SAMPLE_GLOBAL_ID2, 10002L, 10003L, 10006L);
        final List<RemoteIssueLink> links = ImmutableList.copyOf(Iterables.concat(links1, links2));
        when(remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(eqCollection(ImmutableList.of(SAMPLE_GLOBAL_ID, SAMPLE_GLOBAL_ID2))))
                .thenReturn(links);
        final FunctionOperand operand = new FunctionOperand(FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID, SAMPLE_GLOBAL_ID, SAMPLE_GLOBAL_ID2);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);
        final List<QueryLiteral> values = function.getValues(queryCreationContext, operand, clause);
        assertThat(values, hasSize(5));
        final List<Long> issueIds = Lists.transform(values, new Function<QueryLiteral, Long>()
        {
            @Override
            public Long apply(final QueryLiteral literal)
            {
                return literal.getLongValue();
            }
        });
        assertThat(issueIds, hasItems(10001L, 10002L, 10003L, 10005L, 10006L));
    }

    @Test
    public void testGetValuesLinkingDisabled() throws Exception
    {
        when(issueLinkManager.isLinkingEnabled()).thenReturn(false);

        mockLinksForGlobalId(SAMPLE_GLOBAL_ID, 10001L, 10002L, 10003L);
        final FunctionOperand operand = new FunctionOperand(FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID, SAMPLE_GLOBAL_ID);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);
        final List<QueryLiteral> values = function.getValues(queryCreationContext, operand, clause);
        assertThat(values, hasSize(0));
    }

    @Test
    public void testGetValuesNoArguments() throws Exception
    {
        when(issueLinkManager.isLinkingEnabled()).thenReturn(true);

        final FunctionOperand operand = new FunctionOperand(FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);
        mockLinksForGlobalId(SAMPLE_GLOBAL_ID, 10001L, 10002L, 10003L);
        final List<QueryLiteral> values = function.getValues(queryCreationContext, operand, clause);
        assertThat(values, hasSize(0));
    }

    private List<RemoteIssueLink> mockLinksForGlobalId(final String globalId, final Long... issueIds) throws GetException
    {
        final ImmutableList<Long> issueIdList = ImmutableList.copyOf(issueIds);

        return ImmutableList.copyOf(Lists.transform(issueIdList, new Function<Long, RemoteIssueLink>()
        {
            @Override
            public RemoteIssueLink apply(@Nullable final Long issueId)
            {
                return TestDefaultRemoteIssueLinkManager.populatedBuilder(issueId).globalId(globalId).build();
            }
        }));
    }

    private <T> Collection<T> eqCollection(final Collection<T> expectedItems)
    {
        return argThat(new TypeSafeMatcher<Collection<T>>()
        {
            @Override
            protected boolean matchesSafely(final Collection<T> items)
            {
                return expectedItems.size() == items.size() && CollectionUtils.intersection(items, expectedItems).size() == items.size();
            }

            @Override
            public void describeTo(final Description description)
            {
            }
        });
    }
}
