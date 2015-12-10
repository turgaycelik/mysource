package com.atlassian.jira.issue.search.handlers;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DueDateIndexer;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.impl.WorkRatioSearcher;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.permission.ClausePermissionChecker;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.WorkRatioClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.jql.validator.WorkRatioValidator;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.search.searchers.information.MockSearcherInformation;
import com.atlassian.jira.mock.jql.validator.MockClausePermissionChecker;
import com.atlassian.jira.util.ComponentFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link WorkRatioSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestWorkRatioSearchHandlerFactory extends MockControllerTestCase
{
    @Test
    public void testCreateSearchHandler()
    {
        _testCreateSearchHandler(WorkRatioSearchHandlerFactory.class,
                WorkRatioClauseQueryFactory.class,
                WorkRatioValidator.class,
                SystemSearchConstants.forWorkRatio(),
                WorkRatioSearcher.class);
    }

    private void _testCreateSearchHandler(final Class<? extends SimpleSearchHandlerFactory> handlerFactoryType,
            final Class<? extends ClauseQueryFactory> clauseQueryFactoryType,
            final Class<? extends ClauseValidator> validatorType,
            final ClauseInformation clauseInformation,
            final Class<? extends IssueSearcher<SearchableField>> searcherClass)
    {
        //Create these, they don't get called.
        final ClauseValidator validator = mockController.getMock(validatorType);
        final ClauseQueryFactory queryFactory = mockController.getMock(clauseQueryFactoryType);

        // We want this to be used to build our handler
        mockController.addObjectInstance(clauseInformation);
        final SearchableField expectedField = mockController.getMock(SearchableField.class);

        final FieldClausePermissionChecker.Factory mockFactory = mockController.getMock(FieldClausePermissionChecker.Factory.class);
        mockFactory.createPermissionChecker(IssueFieldConstants.TIMETRACKING);
        final ClausePermissionChecker checker = new MockClausePermissionChecker();
        mockController.setReturnValue(checker);

        //The issue searcher should be initialised.
        @SuppressWarnings ({ "unchecked" }) final IssueSearcher<SearchableField> issueSearcher = mockController.getMock(IssueSearcher.class);
        issueSearcher.init(expectedField);

        final MockSearcherInformation searcherInformation = new MockSearcherInformation("dummy");
        final List<FieldIndexer> expectedIndexers = Collections.<FieldIndexer>singletonList(new DueDateIndexer(null));
        searcherInformation.setIndexers(expectedIndexers);

        issueSearcher.getSearchInformation();
        mockController.setReturnValue(searcherInformation);

        //We should try and create the searcher object.
        ComponentFactory componentFactory = mockController.getMock(ComponentFactory.class);
        componentFactory.createObject(searcherClass);
        mockController.setReturnValue(issueSearcher);

        final SimpleSearchHandlerFactory testFactory = mockController.instantiate(handlerFactoryType);
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
        assertSame(queryFactory, clauseRegistration.getHandler().getFactory());
        assertSame(validator, clauseRegistration.getHandler().getValidator());
        assertEquals(clauseInformation, clauseRegistration.getHandler().getInformation());

        mockController.verify();
    }
}
