package com.atlassian.jira.jql.context;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.SimpleAllTextCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAllTextClauseContextFactory
{
    @Mock private CustomFieldManager customFieldManager;
    @Mock private SearchHandlerManager searchHandlerManager;
    @Mock private ContextSetUtil contextSetUtil;

    private AllTextClauseContextFactory factory;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        factory = new AllTextClauseContextFactory(customFieldManager, searchHandlerManager, contextSetUtil);
    }

    @After
    public void tearDown() throws Exception
    {
        customFieldManager = null;
        searchHandlerManager = null;
        contextSetUtil = null;
        factory = null;
        theUser = null;
    }

    @Test
    public void testGetClauseContextMergesContextsFromFactories() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");
        final TerminalClauseImpl clause = new TerminalClauseImpl("text", Operator.LIKE, operand);

        final ClauseContext subContext1 = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE)));
        final ClauseContext subContext2 = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(new ProjectContextImpl(50L), AllIssueTypesContext.INSTANCE)));

        final Set<ClauseContext> expectedSet = CollectionBuilder.newBuilder(subContext1, subContext2).asSet();

        final ClauseContextFactory subFactory1 = mock(ClauseContextFactory.class);
        when(subFactory1.getClauseContext(theUser, clause))
                .thenReturn(subContext1);

        final ClauseContextFactory subFactory2 = mock(ClauseContextFactory.class);
        when(subFactory2.getClauseContext(theUser, clause))
                .thenReturn(subContext2);

        factory = new AllTextClauseContextFactory(customFieldManager, searchHandlerManager, contextSetUtil)
        {
            @Override
            List<ClauseContextFactory> getFactories(final User searcher)
            {
                return CollectionBuilder.list(subFactory1, subFactory2);
            }
        };

        factory.getClauseContext(theUser, clause);

        verify(contextSetUtil).union(expectedSet);
    }

    @Test
    public void testGetAllSystemFieldFactories() throws Exception
    {
        final ClauseHandler commentHandler = mock(ClauseHandler.class);
        final ClauseContextFactory commentFactory = mock(ClauseContextFactory.class);
        when(searchHandlerManager.getClauseHandler(theUser, SystemSearchConstants.forComments().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(commentHandler));
        when(commentHandler.getClauseContextFactory())
                .thenReturn(commentFactory);

        final ClauseHandler descriptionHandler = mock(ClauseHandler.class);
        final ClauseContextFactory descriptionFactory = mock(ClauseContextFactory.class);
        when(searchHandlerManager.getClauseHandler(theUser, SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(descriptionHandler));
        when(descriptionHandler.getClauseContextFactory())
                .thenReturn(descriptionFactory);

        final ClauseHandler environmentHandler = mock(ClauseHandler.class);
        final ClauseContextFactory environmentFactory = mock(ClauseContextFactory.class);
        when(searchHandlerManager.getClauseHandler(theUser, SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(environmentHandler));
        when(environmentHandler.getClauseContextFactory())
                .thenReturn(environmentFactory);

        final ClauseHandler summaryHandler = mock(ClauseHandler.class);
        final ClauseContextFactory summaryFactory = mock(ClauseContextFactory.class);
        when(searchHandlerManager.getClauseHandler(theUser, SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName()))
                .thenReturn(Collections.singletonList(summaryHandler));
        when(summaryHandler.getClauseContextFactory())
                .thenReturn(summaryFactory);

        final List<ClauseContextFactory> result = factory.getAllSystemFieldFactories(theUser);
        assertThat(result, containsInAnyOrder(commentFactory, descriptionFactory, environmentFactory, summaryFactory));
    }

    @Test
    public void testGetAllCustomFieldFactories() throws Exception
    {
        final CustomField customFieldNullSearcher = mock(CustomField.class);
        when(customFieldNullSearcher.getCustomFieldSearcher())
                .thenReturn(null);

        final CustomFieldSearcher numberSearcher = mock(CustomFieldSearcher.class);
        final CustomField customFieldNonText = mock(CustomField.class);
        when(customFieldNonText.getCustomFieldSearcher())
                .thenReturn(numberSearcher);

        final ClauseHandler textHandler = mock(ClauseHandler.class);
        final ClauseContextFactory textFactory = mock(ClauseContextFactory.class);
        when(searchHandlerManager.getClauseHandler(theUser, "customfield_10000"))
                .thenReturn(Collections.singletonList(textHandler));
        when(textHandler.getClauseContextFactory())
                .thenReturn(textFactory);

        final CustomFieldSearcher freeTextSearcher = mock(CustomFieldSearcher.class);
        
        SimpleAllTextCustomFieldSearcherClauseHandler cfSupportsAllText  = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.LIKE).asSet(), null);
        when(freeTextSearcher.getCustomFieldSearcherClauseHandler()).thenReturn(cfSupportsAllText);
        
        final CustomField customField = mock(CustomField.class);
        when(customField.getCustomFieldSearcher())
                .thenReturn(freeTextSearcher);
        when(customField.getClauseNames())
                .thenReturn(new ClauseNames("customfield_10000"));

        when(customFieldManager.getCustomFieldObjects())
                .thenReturn(CollectionBuilder.list(customFieldNullSearcher, customFieldNonText, customField));
        
        final List<ClauseContextFactory> result = factory.getAllCustomFieldFactories(theUser);
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
        final ClauseContextFactory textFactory = mock(ClauseContextFactory.class);

        final CustomFieldSearcher customFieldSearcher1 = mock(CustomFieldSearcher.class);
        final CustomField customFieldDoesNotSupportLIKEOoperator = mock(CustomField.class);
        when(customFieldDoesNotSupportLIKEOoperator.getCustomFieldSearcher())
                .thenReturn(customFieldSearcher1);
        SimpleAllTextCustomFieldSearcherClauseHandler cfDoesNotSupportsAllText  = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.GREATER_THAN_EQUALS).asSet(), null);
        when(customFieldSearcher1.getCustomFieldSearcherClauseHandler()).thenReturn(cfDoesNotSupportsAllText);

        when(customFieldManager.getCustomFieldObjects())
                .thenReturn(CollectionBuilder.list(customFieldSupportsLIKEOoperator, customFieldDoesNotSupportLIKEOoperator));

        when(searchHandlerManager.getClauseHandler(theUser, "customfield_10000"))
                .thenReturn(Collections.singletonList(textHandler));
        when(textHandler.getClauseContextFactory())
                .thenReturn(textFactory);

        final List<ClauseContextFactory> result = factory.getAllCustomFieldFactories(theUser);
        assertThat(result, contains(textFactory));
    }

    @Test
    public void testGetFactoriesHappyPath() throws Exception
    {
        final AtomicBoolean systemCalled = new AtomicBoolean(false);
        final AtomicBoolean customCalled = new AtomicBoolean(false);

        factory = new AllTextClauseContextFactory(customFieldManager, searchHandlerManager, contextSetUtil)
        {
            @Override
            List<ClauseContextFactory> getAllSystemFieldFactories(final User searcher)
            {
                systemCalled.set(true);
                return Collections.emptyList();
            }

            @Override
            List<ClauseContextFactory> getAllCustomFieldFactories(final User user)
            {
                customCalled.set(true);
                return Collections.emptyList();
            }
        };

        factory.getFactories(theUser);

        assertThat("systemCalled", systemCalled.get(), is(true));
        assertThat("customCalled", customCalled.get(), is(true));
    }
}
