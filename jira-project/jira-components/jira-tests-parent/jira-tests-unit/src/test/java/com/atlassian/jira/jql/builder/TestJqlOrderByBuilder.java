package com.atlassian.jira.jql.builder;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static com.atlassian.query.order.SortOrder.ASC;
import static com.atlassian.query.order.SortOrder.DESC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestJqlOrderByBuilder
{
    private JqlOrderByBuilder orderByBuilder;
    private JqlClauseBuilder whereClauseBuilder;
    private JqlQueryBuilder parentBuilder;

    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);


    @Before
    public void setUp() throws Exception
    {
        parentBuilder = JqlQueryBuilder.newBuilder();
        whereClauseBuilder = parentBuilder.where();
        orderByBuilder = parentBuilder.orderBy();
    }

    @Test
    public void testClear() throws Exception
    {
        orderByBuilder.add("test");
        assertEquals(new OrderByImpl(new SearchSort("test")), orderByBuilder.buildOrderBy());

        orderByBuilder.clear();
        assertEquals(OrderByImpl.NO_ORDER, orderByBuilder.buildOrderBy());

        orderByBuilder.add("test").add("test2");
        assertEquals(new OrderByImpl(new SearchSort("test"), new SearchSort("test2")), orderByBuilder.buildOrderBy());

        assertEquals(OrderByImpl.NO_ORDER, orderByBuilder.clear().buildOrderBy());
    }

    @Test
    public void testCloneFromExisting() throws Exception
    {
        @SuppressWarnings ({ "deprecation" }) final OrderByImpl existingOrderBy = new OrderByImpl(new SearchSort("Test", SortOrder.DESC), new SearchSort("Blah", SortOrder.ASC), new SearchSort("Heee", (SortOrder) null), new SearchSort("ASC", "Haaaa"));
        orderByBuilder.setSorts(existingOrderBy);
        assertEquals(existingOrderBy, orderByBuilder.buildOrderBy());
    }

    @Test
    public void testEndOrderBy() throws Exception
    {
        assertEquals(parentBuilder, orderByBuilder.endOrderBy());
    }

    @Test
    public void testSetSorts() throws Exception
    {
        @SuppressWarnings ({ "deprecation" }) final OrderByImpl existingOrderBy = new OrderByImpl(new SearchSort("Test", SortOrder.DESC), new SearchSort("Blah", SortOrder.ASC), new SearchSort("Heee", (SortOrder) null), new SearchSort("ASC", "Haaaa"));
        orderByBuilder.priority(SortOrder.ASC);
        orderByBuilder.setSorts(existingOrderBy.getSearchSorts());
        assertEquals(existingOrderBy, orderByBuilder.buildOrderBy());
    }

    @Test
    public void testGetQuery() throws Exception
    {
        whereClauseBuilder.project("HSP");
        orderByBuilder.priority(ASC).add("Test", DESC);

        Clause expectedWhere = new TerminalClauseImpl("project", Operator.EQUALS, "HSP");
        OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("priority", ASC), new SearchSort("Test", DESC));
        Query expectedQuery = new QueryImpl(expectedWhere, expectedOrderBy, null);
        assertEquals(expectedQuery, orderByBuilder.buildQuery());
    }

    @Test
    public void testGetQueryNoParent() throws Exception
    {
        JqlOrderByBuilder builder = new JqlOrderByBuilder(null);
        builder.priority(ASC).add("Test", DESC);

        OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("priority", ASC), new SearchSort("Test", DESC));
        Query expectedQuery = new QueryImpl(null, expectedOrderBy, null);
        assertEquals(expectedQuery, builder.buildQuery());
    }

    @Test
    public void testEmptyOrderBy() throws Exception
    {
        final OrderBy orderBy = orderByBuilder.buildOrderBy();
        assertTrue(orderBy.getSearchSorts().isEmpty());
    }

    @Test
    public void testAddStringClause() throws Exception
    {
        final OrderBy orderBy = orderByBuilder.add("Test", DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort("Test", DESC), orderBy.getSearchSorts().get(0));
    }
    
    @Test
    public void testAddStringClauseNoOrder() throws Exception
    {
        final OrderBy orderBy = orderByBuilder.add("Test").buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort("Test"), orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testPriorityAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forPriority().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testPriorityDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forPriority().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }
    
    @Test
    public void testPriorityNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forPriority().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testPriorityAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.currentEstimate(null).priority(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forPriority().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testCurrentEstimateAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.currentEstimate(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forCurrentEstimate().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testCurrentEstimateDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.currentEstimate(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forCurrentEstimate().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testCurrentEstimateNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.currentEstimate(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forCurrentEstimate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testCurrentEstimateAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).currentEstimate(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forCurrentEstimate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testVotesAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.votes(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forVotes().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testVotesDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.votes(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forVotes().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testVotesNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.votes(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forVotes().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testVotesAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).votes(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forVotes().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueKeyAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.issueKey(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueKey().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueKeyDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.issueKey(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueKey().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueKeyNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.issueKey(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueKey().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueKeyAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).issueKey(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueKey().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testProjectAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.project(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testProjectDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.project(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testProjectNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.project(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testProjectAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).project(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueTypeAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.issueType(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueTypeDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.issueType(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueTypeNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.issueType(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueTypeAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).issueType(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testCreatedDateAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.createdDate(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forCreatedDate().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testCreatedDateDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.createdDate(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forCreatedDate().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testCreatedDateNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.createdDate(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forCreatedDate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testCreatedDateAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).createdDate(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forCreatedDate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testLastViewedDateAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.lastViewedDate(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forLastViewedDate().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testLastViewedDateDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.lastViewedDate(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forLastViewedDate().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testLastViewedDateNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.lastViewedDate(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forLastViewedDate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testLastViewedDateAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).lastViewedDate(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forLastViewedDate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testDueDateAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.dueDate(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forDueDate().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testDueDateDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.dueDate(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forDueDate().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testDueDateNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.dueDate(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forDueDate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testDueDateAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).dueDate(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forDueDate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testUpdatedDateAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.updatedDate(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forUpdatedDate().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testUpdatedDateDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.updatedDate(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forUpdatedDate().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testUpdatedDateNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.updatedDate(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forUpdatedDate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testUpdatedDateAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).updatedDate(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forUpdatedDate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testSummaryAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.summary(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testSummaryDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.summary(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testSummaryNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.summary(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testSummaryAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).summary(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testResolutionAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.resolution(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forResolution().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testResolutionDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.resolution(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forResolution().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testResolutionNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.resolution(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forResolution().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testResolutionAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).resolution(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forResolution().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testStatusAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.status(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forStatus().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testStatusDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.status(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forStatus().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testStatusNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.status(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forStatus().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testStatusAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).status(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forStatus().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testComponentAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.component(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forComponent().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testComponentDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.component(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forComponent().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testComponentNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.component(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forComponent().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testComponentAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).component(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forComponent().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testAffectedVersionAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.affectedVersion(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testAffectedVersionDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.affectedVersion(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testAffectedVersionNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.affectedVersion(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testAffectedVersionAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).affectedVersion(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testFixForVersionAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.fixForVersion(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forFixForVersion().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testFixForVersionDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.fixForVersion(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forFixForVersion().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testFixForVersionNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.fixForVersion(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forFixForVersion().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testFixForVersionAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).fixForVersion(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forFixForVersion().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testResolutionDateAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.resolutionDate(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forResolutionDate().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testResolutionDateDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.resolutionDate(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forResolutionDate().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testResolutionDateNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.resolutionDate(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forResolutionDate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testResolutionDateAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).resolutionDate(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forResolutionDate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testDescriptionAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.description(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testDescriptionDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.description(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testDescriptionNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.description(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testDescriptionAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).description(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testEnvironmentAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.environment(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testEnvironmentDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.environment(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testEnvironmentNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.environment(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testEnvironmentAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).environment(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testReporterAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.reporter(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forReporter().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testReporterDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.reporter(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forReporter().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testReporterNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.reporter(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forReporter().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testReporterAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).reporter(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forReporter().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testAssigneeAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.assignee(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forAssignee().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testAssigneeDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.assignee(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forAssignee().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testAssigneeNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.assignee(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forAssignee().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testAssigneeAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).assignee(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forAssignee().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueIdAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.issueId(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueId().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueIdDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.issueId(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueId().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueIdNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.issueId(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueId().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testIssueIdAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).issueId(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forIssueId().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testWorkRatioAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.workRatio(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forWorkRatio().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testWorkRatioDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.workRatio(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forWorkRatio().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testWorkRatioNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.workRatio(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forWorkRatio().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testWorkRatioAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).workRatio(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forWorkRatio().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testOriginalEstimateAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.originalEstimate(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forOriginalEstimate().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testOriginalEstimateDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.originalEstimate(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forOriginalEstimate().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testOriginalEstimateNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.originalEstimate(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forOriginalEstimate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testOriginalEstimateAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).originalEstimate(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forOriginalEstimate().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testTimeSpentAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.timeSpent(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forTimeSpent().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testTimeSpentDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.timeSpent(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forTimeSpent().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testTimeSpentNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.timeSpent(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forTimeSpent().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testTimeSpentAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).timeSpent(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forTimeSpent().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testSecurityLevelAsc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.securityLevel(ASC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forSecurityLevel().getJqlClauseNames().getPrimaryName(), ASC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testSecurityLevelDesc() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.securityLevel(DESC).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forSecurityLevel().getJqlClauseNames().getPrimaryName(), DESC),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testSecurityLevelNull() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.securityLevel(null).buildOrderBy();
        assertEquals(1, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forSecurityLevel().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }

    @Test
    public void testSecurityLevelAddedFirst() throws Exception
    {
        final OrderBy orderBy = this.orderByBuilder.priority(null).securityLevel(null, true).buildOrderBy();
        assertEquals(2, orderBy.getSearchSorts().size());
        assertEquals(new SearchSort(SystemSearchConstants.forSecurityLevel().getJqlClauseNames().getPrimaryName(), (SortOrder)null),
                orderBy.getSearchSorts().get(0));
    }
}
