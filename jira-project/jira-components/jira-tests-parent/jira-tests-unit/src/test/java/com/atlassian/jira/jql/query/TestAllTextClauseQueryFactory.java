package com.atlassian.jira.jql.query;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.SimpleAllTextCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAllTextClauseQueryFactory
{
    @Mock private CustomFieldManager customFieldManager;
    @Mock private SearchHandlerManager searchHandlerManager;

    private AllTextClauseQueryFactory factory;
    private QueryCreationContext queryCreationContext;
    private ApplicationUser theUser = null;

    @Before
    public void setUp()
    {
        factory = new AllTextClauseQueryFactory(customFieldManager, searchHandlerManager);
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @After
    public void tearDown()
    {

    }

    @Test
    public void testGetClauseContextUnsupportedOperators() throws Exception
    {
        for (Operator operator : Operator.values())
        {
            if (operator != Operator.LIKE)
            {
                assertFalseResultForUnsupportedOperator(operator);
            }
        }
    }

    @Test
    public void testGetQueryMergesResults() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("text", Operator.LIKE, "test");

        final ClauseQueryFactory factory1 = mock(ClauseQueryFactory.class);
        when(factory1.getQuery(queryCreationContext, clause))
                .thenReturn(QueryFactoryResult.createFalseResult());

        final ClauseQueryFactory factory2 = mock(ClauseQueryFactory.class);
        when(factory2.getQuery(queryCreationContext, clause))
                .thenReturn(QueryFactoryResult.createFalseResult());

        factory = new AllTextClauseQueryFactory(customFieldManager, searchHandlerManager)
        {
            @Override
            List<ClauseQueryFactory> getFactories(final QueryCreationContext queryCreationContext)
            {
                return CollectionBuilder.list(factory1, factory2);
            }
        };

        assertEquals(QueryFactoryResult.createFalseResult(), factory.getQuery(queryCreationContext, clause));
    }

    @Test
    public void testGetAllSystemFieldFactoriesNoOverride() throws Exception
    {
        final ClauseHandler commentHandler = mock(ClauseHandler.class);
        final ClauseQueryFactory commentFactory = mock(ClauseQueryFactory.class);
        when(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, SystemSearchConstants.forComments().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(commentHandler));
        when(commentHandler.getFactory())
                .thenReturn(commentFactory);

        final ClauseHandler descriptionHandler = mock(ClauseHandler.class);
        final ClauseQueryFactory descriptionFactory = mock(ClauseQueryFactory.class);
        when(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(descriptionHandler));
        when(descriptionHandler.getFactory())
                .thenReturn(descriptionFactory);

        final ClauseHandler environmentHandler = mock(ClauseHandler.class);
        final ClauseQueryFactory environmentFactory = mock(ClauseQueryFactory.class);
        when(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(environmentHandler));
        when(environmentHandler.getFactory())
                .thenReturn(environmentFactory);

        final ClauseHandler summaryHandler = mock(ClauseHandler.class);
        final ClauseQueryFactory summaryFactory = mock(ClauseQueryFactory.class);
        when(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(summaryHandler));
        when(summaryHandler.getFactory())
                .thenReturn(summaryFactory);

        final List<ClauseQueryFactory> result = factory.getAllSystemFieldFactories(queryCreationContext);
        assertThat(result, containsInAnyOrder(commentFactory, descriptionFactory, environmentFactory, summaryFactory));
    }

    @Test
    public void testGetAllSystemFieldFactoriesOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);

        final ClauseHandler commentHandler = mock(ClauseHandler.class);
        final ClauseQueryFactory commentFactory = mock(ClauseQueryFactory.class);
        when(searchHandlerManager.getClauseHandler(SystemSearchConstants.forComments().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(commentHandler));
        when(commentHandler.getFactory())
                .thenReturn(commentFactory);

        final ClauseHandler descriptionHandler = mock(ClauseHandler.class);
        final ClauseQueryFactory descriptionFactory = mock(ClauseQueryFactory.class);
        when(searchHandlerManager.getClauseHandler(SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(descriptionHandler));
        when(descriptionHandler.getFactory())
                .thenReturn(descriptionFactory);

        final ClauseHandler environmentHandler = mock(ClauseHandler.class);
        final ClauseQueryFactory environmentFactory = mock(ClauseQueryFactory.class);
        when(searchHandlerManager.getClauseHandler(SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(environmentHandler));
        when(environmentHandler.getFactory())
                .thenReturn(environmentFactory);

        final ClauseHandler summaryHandler = mock(ClauseHandler.class);
        final ClauseQueryFactory summaryFactory = mock(ClauseQueryFactory.class);
        when(searchHandlerManager.getClauseHandler(SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(summaryHandler));
        when(summaryHandler.getFactory())
                .thenReturn(summaryFactory);

        final List<ClauseQueryFactory> result = factory.getAllSystemFieldFactories(queryCreationContext);
        assertThat(result, containsInAnyOrder(commentFactory, descriptionFactory, environmentFactory, summaryFactory));
    }

    @Test
    public void testGetAllCustomFieldFactoriesNoOverride() throws Exception
    {
        final CustomField customFieldNullSearcher = mock(CustomField.class);
        when(customFieldNullSearcher.getCustomFieldSearcher())
                .thenReturn(null);

        SimpleAllTextCustomFieldSearcherClauseHandler cfSupportsAllText = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.LIKE).asSet(), null);

        final CustomFieldSearcher numberSearcher = mock(CustomFieldSearcher.class);
        final CustomField customFieldNonTextSearcher = mock(CustomField.class);
        when(customFieldNonTextSearcher.getCustomFieldSearcher())
                .thenReturn(numberSearcher);

        final ClauseHandler textHandler = mock(ClauseHandler.class);
        final ClauseQueryFactory textFactory = mock(ClauseQueryFactory.class);
        when(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, "customfield_10000"))
                .thenReturn(Collections.singletonList(textHandler));
        when(textHandler.getFactory())
                .thenReturn(textFactory);
        final CustomFieldSearcher freeTextSearcher = mock(CustomFieldSearcher.class);
        final CustomField customFieldTextSearcher = mock(CustomField.class);
        when(customFieldTextSearcher.getCustomFieldSearcher())
                .thenReturn(freeTextSearcher);
        when(freeTextSearcher.getCustomFieldSearcherClauseHandler()).thenReturn(cfSupportsAllText);
        when(customFieldTextSearcher.getClauseNames())
                .thenReturn(new ClauseNames("customfield_10000"));

        when(customFieldManager.getCustomFieldObjects())
                .thenReturn(CollectionBuilder.list(customFieldNullSearcher, customFieldNonTextSearcher, customFieldTextSearcher));

        final List<ClauseQueryFactory> result = factory.getAllCustomFieldFactories(queryCreationContext);
        assertThat(result, contains(textFactory));
    }

    @Test
    public void testGetAllCustomFieldFactoriesOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);

        final CustomField customFieldNull = mock(CustomField.class);
        when(customFieldNull.getCustomFieldSearcher())
                .thenReturn(null);

        SimpleAllTextCustomFieldSearcherClauseHandler cfSupportsAllText  = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.LIKE).asSet(), null);

        final ClauseHandler textHandler = mock(ClauseHandler.class);
        final ClauseQueryFactory textFactory = mock(ClauseQueryFactory.class);
        when(searchHandlerManager.getClauseHandler("customfield_10000"))
                .thenReturn(Collections.singletonList(textHandler));
        when(textHandler.getFactory())
                .thenReturn(textFactory);

        final CustomFieldSearcher freeTextSearcher = mock(CustomFieldSearcher.class);

        final CustomField customFieldText = mock(CustomField.class);
        when(customFieldText.getCustomFieldSearcher())
                .thenReturn(freeTextSearcher);
        when(freeTextSearcher.getCustomFieldSearcherClauseHandler()).thenReturn(cfSupportsAllText);
        when(customFieldText.getClauseNames())
                .thenReturn(new ClauseNames("customfield_10000"));

        final CustomFieldSearcher numberSearcher = mock(CustomFieldSearcher.class);
        when(numberSearcher.getCustomFieldSearcherClauseHandler()).thenReturn(null);
        final CustomField customFieldNonText = mock(CustomField.class);

        when(customFieldNonText.getCustomFieldSearcher())
                .thenReturn(numberSearcher);

        when(customFieldManager.getCustomFieldObjects())
                .thenReturn(CollectionBuilder.list(customFieldNull, customFieldNonText, customFieldText));

        final List<ClauseQueryFactory> result = factory.getAllCustomFieldFactories(queryCreationContext);
        assertThat(result, contains(textFactory));
    }

    @Test
    public void testGetAllCustumFieldFactoriesSupportedOperators() throws Exception
    {
        final CustomFieldSearcher customFieldSearcher = mock(CustomFieldSearcher.class);
        final CustomField customFieldSupportsLIKEOoperator = mock(CustomField.class);
        when(customFieldSupportsLIKEOoperator.getCustomFieldSearcher())
                .thenReturn(customFieldSearcher);
        SimpleAllTextCustomFieldSearcherClauseHandler cfSupportsAllText  = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.LIKE).asSet(), null);
        when(customFieldSearcher.getCustomFieldSearcherClauseHandler()).thenReturn(cfSupportsAllText);
        when(customFieldSupportsLIKEOoperator.getClauseNames())
                .thenReturn(new ClauseNames("customfield_10000"));
        final ClauseHandler textHandler = mock(ClauseHandler.class);
        final ClauseQueryFactory textFactory = mock(ClauseQueryFactory.class);

        final CustomFieldSearcher customFieldSearcher1 = mock(CustomFieldSearcher.class);
        final CustomField customFieldDoesNotSupportLIKEOoperator = mock(CustomField.class);
        when(customFieldDoesNotSupportLIKEOoperator.getCustomFieldSearcher())
                .thenReturn(customFieldSearcher1);
        SimpleAllTextCustomFieldSearcherClauseHandler cfDoesNotSupportsAllText  = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.GREATER_THAN_EQUALS).asSet(), null);
        when(customFieldSearcher1.getCustomFieldSearcherClauseHandler()).thenReturn(cfDoesNotSupportsAllText);

        when(customFieldManager.getCustomFieldObjects())
                .thenReturn(CollectionBuilder.list(customFieldSupportsLIKEOoperator, customFieldDoesNotSupportLIKEOoperator));

        when(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, "customfield_10000"))
                .thenReturn(Collections.singletonList(textHandler));
        when(textHandler.getFactory())
                .thenReturn(textFactory);

        final List<ClauseQueryFactory> result = factory.getAllCustomFieldFactories(queryCreationContext);
        assertThat(result, contains(textFactory));
    }

    @Test
    public void testGetFactoriesHappyPath() throws Exception
    {
        final AtomicBoolean systemCalled = new AtomicBoolean(false);
        final AtomicBoolean customCalled = new AtomicBoolean(false);

        factory = new AllTextClauseQueryFactory(customFieldManager, searchHandlerManager)
        {
            @Override
            List<ClauseQueryFactory> getAllSystemFieldFactories(final QueryCreationContext searcher)
            {
                systemCalled.set(true);
                return Collections.emptyList();
            }

            @Override
            List<ClauseQueryFactory> getAllCustomFieldFactories(final QueryCreationContext user)
            {
                customCalled.set(true);
                return Collections.emptyList();
            }
        };

        factory.getFactories(queryCreationContext);

        assertThat("systemCalled", systemCalled.get(), is(true));
        assertThat("customCalled", customCalled.get(), is(true));
    }

    private void assertFalseResultForUnsupportedOperator(final Operator operator)
    {
        assertEquals(QueryFactoryResult.createFalseResult(), factory.getQuery(queryCreationContext, new TerminalClauseImpl("text", operator, "test")));
    }
}
