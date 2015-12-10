package com.atlassian.jira.jql.query;

import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.index.IndexDocumentConfiguration;
import com.atlassian.jira.index.property.PluginIndexConfiguration;
import com.atlassian.jira.index.property.PluginIndexConfigurationManager;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.jql.util.NumberIndexValueConverter;
import com.atlassian.jira.jql.util.SimpleIndexValueConverter;
import com.atlassian.jira.jql.validator.IssuePropertyClauseValidator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.Property;
import com.atlassian.query.clause.TerminalClause;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static com.atlassian.jira.index.IndexDocumentConfiguration.ExtractConfiguration;
import static com.atlassian.jira.index.IndexDocumentConfiguration.KeyConfiguration;
import static com.atlassian.jira.jql.query.QueryFactoryResult.createFalseResult;

/**
 * Factory for producing clauses for issue properties.
 *
 * @since v6.2
 */
@InjectableComponent
public class IssuePropertyClauseQueryFactory implements ClauseQueryFactory
{
    private final PluginIndexConfigurationManager pluginIndexConfigurationManager;
    private final DoubleConverter doubleConverter;
    private final JqlDateSupport jqlDateSupport;
    private final JqlOperandResolver operandResolver;
    private final JiraAuthenticationContext authenticationContext;

    public IssuePropertyClauseQueryFactory(final PluginIndexConfigurationManager pluginIndexConfigurationManager, final DoubleConverter doubleConverter,
            final JqlDateSupport jqlDateSupport, final JqlOperandResolver operandResolver, final JiraAuthenticationContext authenticationContext)
    {
        this.pluginIndexConfigurationManager = pluginIndexConfigurationManager;
        this.doubleConverter = doubleConverter;
        this.jqlDateSupport = jqlDateSupport;
        this.operandResolver = operandResolver;
        this.authenticationContext = authenticationContext;
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull final QueryCreationContext queryCreationContext,
            @Nonnull final TerminalClause terminalClause)
    {
        if (terminalClause.getProperty().isDefined() && IssuePropertyClauseValidator.isSupportedOperator(terminalClause.getOperator()))
        {
            return getLuceneQuery(terminalClause);
        }
        else
        {
            return createFalseResult();
        }
    }

    private QueryFactoryResult getLuceneQuery(final TerminalClause terminalClause)
    {
        final Property property = terminalClause.getProperty().get();
        final Iterable<IndexDocumentConfiguration.Type> propertyTypes = getPropertyTypes(property);
        final Iterable<OperatorSpecificQueryFactory> queryFactories = getQueryFactories(propertyTypes);

        final String fieldName = "ISSUEPROP_" + property.getAsPropertyString();
        return new GenericClauseQueryFactory(fieldName, Lists.newArrayList(queryFactories), operandResolver)
                .getQuery(new QueryCreationContextImpl(authenticationContext.getUser()), terminalClause);
    }

    private Iterable<OperatorSpecificQueryFactory> getQueryFactories(Iterable<IndexDocumentConfiguration.Type> types)
    {
        return Iterables.concat(Iterables.transform(types, new Function<IndexDocumentConfiguration.Type, Iterable<OperatorSpecificQueryFactory>>()
        {
            @Override
            public Iterable<OperatorSpecificQueryFactory> apply(final IndexDocumentConfiguration.Type type)
            {
                final ImmutableList.Builder<OperatorSpecificQueryFactory> builder = ImmutableList.builder();
                switch (type)
                {
                    case NUMBER:
                        final NumberIndexValueConverter valueConverter = new NumberIndexValueConverter(doubleConverter);
                        builder.add(new ActualValueEqualityQueryFactory(valueConverter));
                        builder.add(new ActualValueRelationalQueryFactory(valueConverter));
                        break;
                    case TEXT:
                        builder.add(new LikeQueryFactory());
                        break;
                    case DATE:
                        builder.add(new DateEqualityQueryFactory(jqlDateSupport));
                        builder.add(new DateRelationalQueryFactory(jqlDateSupport));
                        break;
                    case STRING:
                        builder.add(new ActualValueEqualityQueryFactory(new SimpleIndexValueConverter(false)));
                        break;
                }
                return builder.build();
            }
        }));
    }

    private Iterable<IndexDocumentConfiguration.Type> getPropertyTypes(final Property property)
    {
        final Iterable<PluginIndexConfiguration> configurations = pluginIndexConfigurationManager.getDocumentsForEntity(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName());
        final String propertyKey = property.getKeysAsString();
        final String objRef = property.getObjectReferencesAsString();

        final Iterable<KeyConfiguration> keyConfigurations = Iterables.concat(Iterables.transform(configurations, new Function<PluginIndexConfiguration, List<KeyConfiguration>>()
        {
            @Override
            public List<KeyConfiguration> apply(final PluginIndexConfiguration indexConfiguration)
            {
                return indexConfiguration.getIndexDocumentConfiguration().getKeyConfigurations();
            }
        }));

        final Iterable<KeyConfiguration> filteredConfigurations = Iterables.filter(keyConfigurations, new Predicate<KeyConfiguration>()
        {
            @Override
            public boolean apply(final KeyConfiguration keyConfiguration)
            {
                return keyConfiguration.getPropertyKey().equals(propertyKey);
            }
        });

        final Iterable<ExtractConfiguration> extractConfigurations = Iterables.concat(Iterables.transform(filteredConfigurations, new Function<KeyConfiguration, List<ExtractConfiguration>>()
        {
            @Override
            public List<ExtractConfiguration> apply(final KeyConfiguration keyConfiguration)
            {
                return keyConfiguration.getExtractorConfigurations();
            }
        }));

        return Iterables.transform(Iterables.filter(extractConfigurations, new Predicate<ExtractConfiguration>()
        {
            @Override
            public boolean apply(final ExtractConfiguration extractConfig)
            {
                return extractConfig.getPath().equals(objRef);
            }
        }), new Function<ExtractConfiguration, IndexDocumentConfiguration.Type>()
        {
            @Override
            public IndexDocumentConfiguration.Type apply(final ExtractConfiguration extractConfiguration)
            {
                return extractConfiguration.getType();
            }
        });
    }
}
