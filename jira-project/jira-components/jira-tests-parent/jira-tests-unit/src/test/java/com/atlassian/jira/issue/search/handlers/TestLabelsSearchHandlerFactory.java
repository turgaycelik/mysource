package com.atlassian.jira.issue.search.handlers;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DueDateIndexer;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.impl.LabelsSearcher;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.permission.ClausePermissionChecker;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.permission.ClauseSanitiser;
import com.atlassian.jira.jql.permission.DefaultClausePermissionHandler;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.permission.NoOpClauseSanitiser;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.LabelsClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.jql.validator.LabelsValidator;
import com.atlassian.jira.mock.issue.search.searchers.information.MockSearcherInformation;
import com.atlassian.jira.mock.jql.validator.MockClausePermissionChecker;
import com.atlassian.jira.util.ComponentFactory;

import org.easymock.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link LabelsSearchHandlerFactory}.
 *
 * @since v4.2
 */
public class TestLabelsSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateSearchHandler()
    {
        //Create these, they don't get called.
        final ClauseValidator validator = mockController.getMock(LabelsValidator.class);
        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ClauseQueryFactory queryFactory = new LabelsClauseQueryFactory(operandResolver, DocumentConstants.ISSUE_LABELS_FOLDED);

        final MultiClauseDecoratorContextFactory.Factory multiFactory = mockController.getMock(MultiClauseDecoratorContextFactory.Factory.class);
        expect(multiFactory.create(EasyMock.<ClauseContextFactory>anyObject())).andReturn(createMock(ClauseContextFactory.class)).anyTimes();

        // We want this to be used to build our handler
        mockController.addObjectInstance(SystemSearchConstants.forLabels());
        final SearchableField expectedField = mockController.getMock(SearchableField.class);

        final FieldClausePermissionChecker.Factory mockFactory = mockController.getMock(FieldClausePermissionChecker.Factory.class);
        final ClausePermissionChecker checker = new MockClausePermissionChecker();
        expect(mockFactory.createPermissionChecker(expectedField)).andReturn(checker);

        //The issue searcher should be initialised.
        @SuppressWarnings ({ "unchecked" }) final IssueSearcher<SearchableField> issueSearcher = mockController.getMock(IssueSearcher.class);
        issueSearcher.init(expectedField);

        final MockSearcherInformation searcherInformation = new MockSearcherInformation("dummy");
        final List<FieldIndexer> expectedIndexers = Collections.<FieldIndexer>singletonList(new DueDateIndexer(null));
        searcherInformation.setIndexers(expectedIndexers);

        expect(issueSearcher.getSearchInformation()).andReturn(searcherInformation);

        //We should try and create the searcher object.
        ComponentFactory componentFactory = mockController.getMock(ComponentFactory.class);
        expect(componentFactory.createObject((Class) LabelsSearcher.class)).andReturn(issueSearcher);

        final SimpleSearchHandlerFactory testFactory = mockController.instantiate(LabelsSearchHandlerFactory.class);
        final SearchHandler searchHandler = testFactory.createHandler(expectedField);

        //We should have no extra registrations.
        assertTrue(searchHandler.getClauseRegistrations().isEmpty());

        //The indexers should come from the information.
        assertEquals(expectedIndexers, searchHandler.getIndexers());

        //Make sure the searcher is correctly configured.
        final SearchHandler.SearcherRegistration registration = searchHandler.getSearcherRegistration();
        assertSame(registration.getIssueSearcher(), issueSearcher);
        assertEquals(1, registration.getClauseHandlers().size());

        final SearchHandler.ClauseRegistration clauseRegistration = registration.getClauseHandlers().get(0);
        assertEquals(queryFactory, clauseRegistration.getHandler().getFactory());
        assertSame(validator, clauseRegistration.getHandler().getValidator());
        assertEquals(SystemSearchConstants.forLabels(), clauseRegistration.getHandler().getInformation());

        ClauseSanitiser clauseSanitiser = NoOpClauseSanitiser.NOOP_CLAUSE_SANITISER;
        final ClausePermissionHandler expectedPermissionHandler = new DefaultClausePermissionHandler(checker, clauseSanitiser);
        assertEquals(expectedPermissionHandler, clauseRegistration.getHandler().getPermissionHandler());

        mockController.verify();
    }

}
