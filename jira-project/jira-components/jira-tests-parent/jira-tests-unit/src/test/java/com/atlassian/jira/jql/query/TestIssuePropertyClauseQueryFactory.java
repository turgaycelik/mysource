package com.atlassian.jira.jql.query;

import java.util.Set;

import com.atlassian.fugue.Option;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.index.IndexDocumentConfiguration;
import com.atlassian.jira.index.property.PluginIndexConfiguration;
import com.atlassian.jira.index.property.PluginIndexConfigurationManager;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.query.clause.Property;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.lucene.parsing.DefaultLuceneQueryParserFactory;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.apache.lucene.search.Query;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static com.atlassian.jira.index.IndexDocumentConfiguration.ExtractConfiguration;
import static com.atlassian.jira.index.IndexDocumentConfiguration.KeyConfiguration;
import static com.atlassian.jira.index.IndexDocumentConfiguration.Type;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v6.2
 */
public class TestIssuePropertyClauseQueryFactory
{
    private static final Splitter splitter = Splitter.on('.');

    @Rule public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    @Mock public PluginIndexConfigurationManager pluginIndexConfigurationManager;
    @Mock public DoubleConverter doubleConverter;
    @Mock public JqlDateSupport jqlDateSupport;
    @Mock public JqlOperandResolver operandResolver;
    @Mock public JiraAuthenticationContext authenticationContext;
    @AvailableInContainer DefaultLuceneQueryParserFactory luceneQueryParserFactory = new DefaultLuceneQueryParserFactory();

    private IssuePropertyClauseQueryFactory queryFactory;

    @Before
    public void setUp()
    {
        queryFactory = new IssuePropertyClauseQueryFactory(pluginIndexConfigurationManager, doubleConverter, jqlDateSupport, operandResolver, authenticationContext);
    }

    @Test
    public void queryForValidSingleValueString()
    {
        final Iterable<PluginIndexConfiguration> pluginIndexConfigurations = Lists.newArrayList(mockPluginIndexConfiguration("issue.status", "resolution", Type.STRING));
        when(pluginIndexConfigurationManager.getDocumentsForEntity(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName()))
                .thenReturn(pluginIndexConfigurations);
        when(operandResolver.isValidOperand(any(Operand.class))).thenReturn(true);
        SingleValueOperand operand = new SingleValueOperand("resolved");
        TerminalClauseImpl terminalClause = new TerminalClauseImpl("issue.property", Operator.EQUALS, operand, property("issue.status", "resolution"));
        when(operandResolver.getValues(any(QueryCreationContext.class), eq(operand), eq(terminalClause)))
                .thenReturn(Lists.newArrayList(new QueryLiteral(operand, "resolved")));

        QueryFactoryResult result = queryFactory.getQuery(mock(QueryCreationContext.class), terminalClause);

        final Query query = result.getLuceneQuery();

        assertThat(query, notNullValue(Query.class));
        assertQuery(query, "issue.status", "resolution", "resolved");
    }

    @Test
    public void queryForValidMultipleValueString()
    {
        final Iterable<PluginIndexConfiguration> pluginIndexConfigurations = Lists.newArrayList(mockPluginIndexConfiguration("issue.status", "resolution", Type.STRING));
        when(pluginIndexConfigurationManager.getDocumentsForEntity(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName()))
                .thenReturn(pluginIndexConfigurations);
        MultiValueOperand operand = new MultiValueOperand("resolved", "unresolved");
        when(operandResolver.isValidOperand(eq(operand))).thenReturn(true);
        when(operandResolver.isListOperand(eq(operand))).thenReturn(true);
        TerminalClauseImpl clause = new TerminalClauseImpl("issue.property", Operator.IN, operand, property("issue.status", "resolution"));
        when(operandResolver.getValues(any(QueryCreationContext.class), eq(operand), eq(clause)))
                .thenReturn(Lists.newArrayList(new QueryLiteral(operand, "resolved"), new QueryLiteral(operand, "unresolved")));

        QueryFactoryResult result = queryFactory.getQuery(mock(QueryCreationContext.class), clause);

        final Query query = result.getLuceneQuery();

        assertThat(query, notNullValue(Query.class));
        assertQuery(query, "issue.status", "resolution", "resolved");
        assertQuery(query, "issue.status", "resolution", "unresolved");
    }

    @Test
    public void queryForValidSingleValueText()
    {
        final Iterable<PluginIndexConfiguration> pluginIndexConfigurations = Lists.newArrayList(mockPluginIndexConfiguration("issue.status", "resolution", Type.TEXT));
        when(pluginIndexConfigurationManager.getDocumentsForEntity(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName()))
                .thenReturn(pluginIndexConfigurations);
        SingleValueOperand operand = new SingleValueOperand("resolved");
        when(operandResolver.isValidOperand(eq(operand))).thenReturn(true);
        TerminalClauseImpl clause = new TerminalClauseImpl("issue.property", Operator.LIKE, operand, property("issue.status", "resolution"));
        when(operandResolver.getValues(any(QueryCreationContext.class), eq(operand), eq(clause)))
                .thenReturn(Lists.newArrayList(new QueryLiteral(operand, "resolved")));

        QueryFactoryResult result = queryFactory.getQuery(mock(QueryCreationContext.class), clause);

        final Query query = result.getLuceneQuery();
        assertThat(query, notNullValue(Query.class));
        assertQuery(query, "issue.status", "resolution", "resolved");
    }

    @Test
    public void invalidClauseType()
    {
        IssuePropertyClauseQueryFactory queryFactory = new IssuePropertyClauseQueryFactory(pluginIndexConfigurationManager, doubleConverter, jqlDateSupport, operandResolver, authenticationContext);
        QueryFactoryResult result =
                queryFactory.getQuery(mock(QueryCreationContext.class), new TerminalClauseImpl("issue.status", Operator.EQUALS, "resolved"));

        assertEquals(result, QueryFactoryResult.createFalseResult());
    }

    @Test
    public void invalidOperator()
    {
        IssuePropertyClauseQueryFactory queryFactory = new IssuePropertyClauseQueryFactory(pluginIndexConfigurationManager, doubleConverter, jqlDateSupport, operandResolver, authenticationContext);

        Set<Operator> invalidOperators = ImmutableSet.<Operator>builder().addAll(OperatorClasses.CHANGE_HISTORY_PREDICATES)
                .addAll(OperatorClasses.CHANGE_HISTORY_OPERATORS).build();
        for (Operator invalidOperator : invalidOperators)
        {
            QueryFactoryResult result =
                    queryFactory.getQuery(mock(QueryCreationContext.class),
                            new TerminalClauseImpl("issue.property", invalidOperator, new SingleValueOperand("resolved"),
                                    Option.some(new Property(Lists.newArrayList("issue", "status"), Lists.<String>newArrayList()))));

            assertEquals(result, QueryFactoryResult.createFalseResult());
        }
    }

    private PluginIndexConfiguration mockPluginIndexConfiguration(final String propertyKey, final String objReference, Type type)
    {
        final IndexDocumentConfiguration indexDocumentConfiguration = mock(IndexDocumentConfiguration.class);
        final KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        final ExtractConfiguration extractConfiguration = mock(ExtractConfiguration.class);
        final PluginIndexConfiguration pluginIndexConfiguration = mock(PluginIndexConfiguration.class);

        when(extractConfiguration.getPath()).thenReturn(objReference);
        when(extractConfiguration.getType()).thenReturn(type);
        when(pluginIndexConfiguration.getIndexDocumentConfiguration()).thenReturn(indexDocumentConfiguration);
        when(keyConfiguration.getPropertyKey()).thenReturn(propertyKey);
        when(keyConfiguration.getExtractorConfigurations()).thenReturn(Lists.newArrayList(extractConfiguration));
        when(indexDocumentConfiguration.getKeyConfigurations()).thenReturn(Lists.newArrayList(keyConfiguration));

        return pluginIndexConfiguration;
    }

    private Option<Property> property(final String propertyKey, final String resolution)
    {
        return Option.some(new Property(Lists.newArrayList(splitter.split(propertyKey)), Lists.newArrayList(splitter.split(resolution))));
    }

    private static void assertQuery(final Query query, final String propertyKey, final String objReference, final String value)
    {
        assertThat(query.toString(), Matchers.containsString(propertyKey + "$" + objReference + ":" + value));
    }
}
