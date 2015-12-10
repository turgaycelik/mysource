package com.atlassian.jira.issue.search.handlers;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DueDateIndexer;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory;
import com.atlassian.jira.jql.permission.ClausePermissionChecker;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.permission.ClauseSanitiser;
import com.atlassian.jira.jql.permission.DefaultClausePermissionHandler;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.permission.NoOpClauseSanitiser;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.search.searchers.information.MockSearcherInformation;
import com.atlassian.jira.mock.jql.validator.MockClausePermissionChecker;
import com.atlassian.jira.util.ComponentFactory;

import org.easymock.EasyMock;
import org.hamcrest.Matchers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

/**
 * Class that can help when testing {@link com.atlassian.jira.issue.search.handlers.SimpleSearchHandlerFactory} based
 * classes.
 *
 * @since v4.0
 */
abstract class AbstractTestSimpleSearchHandlerFactory extends MockControllerTestCase
{
    /**
     * Test to make sure the SearchHandler is generated as expected. This method assumes that that
     * class under test has a two element contructor of the form (ComponentFactory, ClauseQueryFactory , ClauseValidator).
     *
     * @param handlerFactoryType the SimpleSearchHandlerFactory under tests.
     * @param clauseQueryFactoryType the ClauseQueryFactory class that should be returned in the SearchHandler
     * @param validatorType the ClauseValidator class that should be returned in the SearchHandler.
     * @param clauseInformation contains the names that should be placed in the SearchHandler .
     * @param searcherClass the Searcher that should be created and placed in the returned SearchHandler .
     * @param clauseSanitiser the sanitiser expected to be in the returned SearchHandler. Can be null to indicate that
     *  the {@link com.atlassian.jira.jql.permission.NoOpClauseSanitiser} should be used.
     */
    void _testSystemSearcherHandler(final Class<? extends SearchHandlerFactory> handlerFactoryType,
            final Class<? extends ClauseQueryFactory> clauseQueryFactoryType,
            final Class<? extends ClauseValidator> validatorType,
            final ClauseInformation clauseInformation,
            final Class<? extends IssueSearcher<SearchableField>> searcherClass, ClauseSanitiser clauseSanitiser)
    {
        //Create these, they don't get called.
        final ClauseValidator validator = mockController.getMock(validatorType);
        final ClauseQueryFactory queryFactory = mockController.getMock(clauseQueryFactoryType);

        final MultiClauseDecoratorContextFactory.Factory multiFactory = mockController.getMock(MultiClauseDecoratorContextFactory.Factory.class);
        expect(multiFactory.create(EasyMock.<ClauseContextFactory>anyObject())).andReturn(createMock(ClauseContextFactory.class)).anyTimes();

        // We want this to be used to build our handler
        mockController.addObjectInstance(clauseInformation);
        final SearchableField expectedField = mockController.getMock(SearchableField.class);

        final FieldClausePermissionChecker.Factory mockFactory = mockController.getMock(FieldClausePermissionChecker.Factory.class);
        final ClausePermissionChecker checker = new MockClausePermissionChecker();
        expect(mockFactory.createPermissionChecker(expectedField)).andReturn(checker);

        //The issue searcher should be initialised.
        @SuppressWarnings ({ "unchecked" }) final IssueSearcher<SearchableField> issueSearcher = mockController.getMock(IssueSearcher.class);
        issueSearcher.init(expectedField);

        final MockSearcherInformation<SearchableField> searcherInformation = new MockSearcherInformation<SearchableField>("dummy");
        final List<FieldIndexer> expectedIndexers = Collections.<FieldIndexer>singletonList(new DueDateIndexer(null));
        searcherInformation.setIndexers(expectedIndexers);

        expect(issueSearcher.getSearchInformation()).andReturn(searcherInformation);

        //We should try and create the searcher object.
        ComponentFactory componentFactory = mockController.getMock(ComponentFactory.class);
        expect(componentFactory.createObject(searcherClass)).andReturn(issueSearcher);

        final SearchHandlerFactory testFactory = mockController.instantiate(handlerFactoryType);
        final SearchHandler searchHandler = testFactory.createHandler(expectedField);

        //We should have no extra registrations.
        assertThat(searchHandler.getClauseRegistrations(), Matchers.<SearchHandler.ClauseRegistration>empty());

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

        clauseSanitiser = clauseSanitiser == null ? NoOpClauseSanitiser.NOOP_CLAUSE_SANITISER : clauseSanitiser;
        final ClausePermissionHandler expectedPermissionHandler = new DefaultClausePermissionHandler(checker, clauseSanitiser);
        assertEquals(expectedPermissionHandler, clauseRegistration.getHandler().getPermissionHandler());

        mockController.verify();
    }
}
