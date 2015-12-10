package com.atlassian.jira.issue.changehistory;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataMarshaller;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizListIterator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.singleton;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ofbiz.core.entity.EntityOperator.IN;

@RunWith (ListeningMockitoRunner.class)
public class TestChangeHistoryBatch
{
    static final long ISSUE_ID = 10L;
    static final int ISSUE_CHANGE_GROUPS = 1400;

    @Mock
    private OfBizDelegator ofBizDelegator;

    @Mock
    private IssueManager issueManager;

    @Mock
    private UserManager userManager;

    @Mock
    private HistoryMetadataMarshaller historyMetadataMarshaller;

    private Issue issue;

    private List<Long> ids;
    private List<Long> ids_0_to_749;
    private List<Long> ids_750_to_1399;

    private List<GenericValue> changeGroups;

    private List<GenericValue> changeItems;
    private List<GenericValue> changeItems_0_to_749;
    private List<GenericValue> changeItems_750_to_1399;

    @Before
    public void setUp() throws Exception
    {
        issue = new MockIssue(10L);

        userManager = new MockUserManager();

        ids = Lists.newArrayList();
        ids_0_to_749 = Lists.newArrayList();
        ids_750_to_1399 = Lists.newArrayList();
        changeGroups = Lists.newArrayList();
        changeItems = Lists.newArrayList();
        changeItems_0_to_749 = Lists.newArrayList();
        changeItems_750_to_1399 = Lists.newArrayList();

        // put each change group / item into the right bucket
        for (long i = 0; i < ISSUE_CHANGE_GROUPS; i++)
        {
            MockGenericValue changeGroup = new MockGenericValue("ChangeGroup", i);
            MockGenericValue changeItem = new MockGenericValue("ChangeItem", ImmutableMap.of("id", i, "group", i));

            ids.add(i);
            changeGroups.add(changeGroup);
            changeItems.add(changeItem);

            if (i < 750)
            {
                ids_0_to_749.add(i);
                changeItems_0_to_749.add(changeItem);
            }
            else
            {
                ids_750_to_1399.add(i);
                changeItems_750_to_1399.add(changeItem);
            }
        }
    }

    @Test
    public void inClauseShouldNotContainMoreThan1000Elements()
    {
        // return 1400 change groups for the test issue
        when(ofBizDelegator.findListIteratorByCondition(eq("ChangeGroup"), refEq(new EntityExpr("issue", IN, singleton(ISSUE_ID))), isNull(EntityCondition.class), isNull(Collection.class), eq(ImmutableList.of("created ASC", "id ASC")), isNull(EntityFindOptions.class))).thenReturn(new MockOfBizListIterator(changeGroups));

        // return the right change items depending on how many the batcher asks for
        when(ofBizDelegator.findByAnd(eq("ChangeItem"), eqCondition(ImmutableList.<EntityCondition>of(new EntityExpr("group", EntityOperator.IN, ids))))).thenReturn(changeItems);
        when(ofBizDelegator.findByAnd(eq("ChangeItem"), eqCondition(ImmutableList.<EntityCondition>of(new EntityExpr("group", EntityOperator.IN, ids_0_to_749))))).thenReturn(changeItems_0_to_749);
        when(ofBizDelegator.findByAnd(eq("ChangeItem"), eqCondition(ImmutableList.<EntityCondition>of(new EntityExpr("group", EntityOperator.IN, ids_750_to_1399))))).thenReturn(changeItems_750_to_1399);

        // run the test
        List<ChangeHistory> changeHistories = ChangeHistoryBatch.createBatchForIssue(singleton(issue), ofBizDelegator, issueManager, userManager).asList();
        assertThat(changeHistories.size(), equalTo(ISSUE_CHANGE_GROUPS));

        for (ChangeHistory changeHistory : changeHistories)
        {
            List<ChangeItemBean> beans = changeHistory.getChangeItemBeans();
            assertThat(beans.size(), equalTo(1));
        }

        // make sure we asked for id's 1..1000, and then 1001..1400. databases do not like IN clauses with more than
        // 1000 elements in them.
        verify(ofBizDelegator).findByAnd(eq("ChangeItem"), eqCondition(ImmutableList.<EntityCondition>of(new EntityExpr("group", EntityOperator.IN, ids_0_to_749))));
        verify(ofBizDelegator).findByAnd(eq("ChangeItem"), eqCondition(ImmutableList.<EntityCondition>of(new EntityExpr("group", EntityOperator.IN, ids_750_to_1399))));
    }

    private List<EntityCondition> eqCondition(ImmutableList<EntityCondition> conditions)
    {
        return argThat(new MatchesConditionList(conditions));
    }

    /**
     * Matcher for List&lt;EntityCondition&gt;. This is only needed becuse EntityCondition doesn't implement
     * equals()...
     */
    static class MatchesConditionList extends ArgumentMatcher<List<EntityCondition>>
    {
        private final List<EntityCondition> expectedList;

        MatchesConditionList(List<EntityCondition> expectedList)
        {
            this.expectedList = expectedList;
        }

        @Override
        public boolean matches(Object argument)
        {
            if (argument instanceof List)
            {

                List<EntityCondition> actualList = (List) argument;
                if (expectedList.size() == actualList.size())
                {
                    for (int i = 0; i < actualList.size(); i++)
                    {
                        EntityCondition expected = expectedList.get(i);
                        EntityCondition actual = actualList.get(i);

                        if (expected instanceof EntityExpr && actual instanceof EntityExpr)
                        {
                            EntityExpr expectedExpr = (EntityExpr) expected;
                            EntityExpr actualExpr = (EntityExpr) actual;

                            // I am force to make some assumptions here due to the fact that EntityExpr doesn't
                            // implement equals()... bah!
                            EqualsBuilder equalsCheck = new EqualsBuilder()
                                    .append(expectedExpr.getOperator(), actualExpr.getOperator())
                                    .append(expectedExpr.getLhs(), actualExpr.getLhs())
                                    .append(Sets.newHashSet((Iterable) expectedExpr.getRhs()), Sets.newHashSet((Iterable) actualExpr.getRhs()));

                            if (!equalsCheck.isEquals())
                            {
                                return false;
                            }
                        }
                        else if (!reflectionEquals(expected, actual))
                        {
                            return false;
                        }
                    }

                    return true;
                }
            }

            return false;
        }
    }
}
