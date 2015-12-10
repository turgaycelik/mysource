package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.NavigatorStructureChecker;
import com.atlassian.jira.issue.search.searchers.transformer.SearchContextVisibilityChecker;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.VersionIndexedInputHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The {@link SearchInputTransformer} for Version custom fields.
 *
 * @since v4.0
 */
@Deprecated
public class VersionCustomFieldSearchInputTransformer extends AbstractCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    private final String urlParameterName;
    private final ClauseNames clauseNames;
    private final IndexInfoResolver<Version> indexInfoResolver;
    private final JqlOperandResolver operandResolver;
    private final FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private final NavigatorStructureChecker navigatorStructureChecker;
    private final NameResolver<Version> versionResolver;
    private volatile IndexedInputHelper indexedInputHelper;
    private volatile DefaultIndexedInputHelper<Version> defaultIndexedInputHelper;
    private final VersionManager versionManager;

    public VersionCustomFieldSearchInputTransformer(String urlParameterName, final ClauseNames clauseNames, CustomField field,
            final IndexInfoResolver<Version> indexInfoResolver,
            final JqlOperandResolver operandResolver, final FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final NameResolver<Version> versionResolver,
            final CustomFieldInputHelper customFieldInputHelper, final VersionManager versionManager)
    {
        super(field, urlParameterName, customFieldInputHelper);
        this.versionManager = versionManager;
        this.versionResolver = notNull("versionResolver", versionResolver);
        this.urlParameterName = notNull("urlParameterName", urlParameterName);
        this.clauseNames = notNull("clauseNames", clauseNames);
        this.indexInfoResolver = notNull("indexInfoResolver", indexInfoResolver);
        this.operandResolver = notNull("operandRegistry", operandResolver);
        this.fieldFlagOperandRegistry = notNull("fieldFlagOperandRegistry", fieldFlagOperandRegistry);

        this.navigatorStructureChecker = createNavigatorStructureChecker();
    }

    @Deprecated
    public VersionCustomFieldSearchInputTransformer(String urlParameterName, final ClauseNames clauseNames, CustomField field,
            final IndexInfoResolver<Version> indexInfoResolver,
            final JqlOperandResolver operandResolver, final FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final SearchContextVisibilityChecker searchContextVisibilityChecker, final NameResolver<Version> versionResolver,
            final CustomFieldInputHelper customFieldInputHelper, final VersionManager versionManager)
    {
        this(urlParameterName, clauseNames, field, indexInfoResolver, operandResolver, fieldFlagOperandRegistry,
                versionResolver, customFieldInputHelper, versionManager);
    }

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        final boolean structureIsCorrect = navigatorStructureChecker.checkSearchRequest(query);
        if (!structureIsCorrect)
        {
            return structureIsCorrect;
        }
        // JRA-20046 - Now check that none of the versions are archived
        return !queryContainsArchivedVersions(user, query);
    }

    @Override
    protected CustomFieldParams getParamsFromSearchRequest(final User user, Query query, final SearchContext searchContext)
    {
        if (query == null)
        {
            return null;
        }

        final Set<String> valuesAsStrings = getIndexedInputHelper().getAllNavigatorValuesForMatchingClauses(user, clauseNames, query);
        return new CustomFieldParamsImpl(getCustomField(), valuesAsStrings);
    }

    public Clause getSearchClause(final User user, final FieldValuesHolder fieldValuesHolder)
    {
        if (fieldValuesHolder.containsKey(urlParameterName))
        {
            CustomFieldParams customFieldParams = (CustomFieldParams) fieldValuesHolder.get(urlParameterName);
            if (customFieldParams != null && !customFieldParams.isEmpty())
            {
                return getClauseFromParams(user, customFieldParams, fieldValuesHolder);
            }
        }
        return null;
    }

    // This is stupid, I know. We probably should have been using a delegate instead of this inheritance and now,
    // very close to the 4.0 ship date we have changed the way this transformer works and it really does not need all
    // the crap from the parent class, thus we are using some methods, but not this one, uggh.
    protected Clause getClauseFromParams(final User user, final CustomFieldParams params)
    {
        return null;
    }

    // actually used by getSearchClause()
    protected Clause getClauseFromParams(final User user, final CustomFieldParams params, final FieldValuesHolder fieldValuesHolder)
    {
        Collection<String> searchValues = Lists.newArrayList(params.getValuesForNullKey());

        // remove the "-1" ALL flag
        searchValues.removeAll(CollectionBuilder.newBuilder("", "-1").asCollection());
        if (!searchValues.isEmpty())
        {
            final Set<String> versionIds = getStrings(searchValues);

            // if the versions selected do not belong to the project selected, then we cannot reliably
            // use the name of the versions for the clause, since it might match some versions in another
            // project. hence, we will use the DefaultIndexInputHelper in that case to generate a clause
            // with ids for the operands
            final IndexedInputHelper indexInputHelper;
            if (isVersionsNotRelatedToProjects(versionIds, fieldValuesHolder))
            {
                indexInputHelper = getDefaultIndexedInputHelper();
            }
            else
            {
                indexInputHelper = getIndexedInputHelper();
            }
            return indexInputHelper.getClauseForNavigatorValues(getClauseName(user, clauseNames) , versionIds);
        }
        else
        {
            return null;
        }
    }

    /**
     * Check that the selected versions will fit with the selected project.
     *
     * @param versionIdsFromHolder version navigator values from the holder
     * @param fieldValuesHolder the general field values holder
     * @return true if at least one selected version does not match the projects specified
     */
    boolean isVersionsNotRelatedToProjects(final Set<String> versionIdsFromHolder, final FieldValuesHolder fieldValuesHolder)
    {
        final List<String> projects = (List<String>) fieldValuesHolder.get(SystemSearchConstants.forProject().getUrlParameter());
        if (projects == null || projects.isEmpty())
        {
            return false;
        }
        else if (projects.size() == 1 && projects.contains("-1"))
        {
            return false;
        }
        else if (projects.size() > 1)
        {
            return true;
        }

        for (String versionIdString : versionIdsFromHolder)
        {
            final Version version = getVersionFromNavigatorValue(versionIdString);
            if (version != null)
            {
                if (!projects.contains(version.getProjectObject().getId().toString()))
                {
                    // return as soon as we find one offending version
                    return true;
                }
            }
        }

        return false;
    }

    private Version getVersionFromNavigatorValue(final String versionIdString)
    {
        try
        {
            final Long versionId = new Long(versionIdString);
            return versionResolver.get(versionId);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private Set<String> getStrings(final Collection values)
    {
        if (values == null)
        {
            return null;
        }

        for (Object o : values)
        {
            if (!(o instanceof String))
            {
                throw new IllegalArgumentException("Why are we putting non-String values in the FieldValuesHolder for searcher '" + urlParameterName + "'???");
            }
        }

        // We have checked every element in the list is of type String, so it is safe to cast
        @SuppressWarnings ({ "unchecked", "UnnecessaryLocalVariable" })
        final Set<String> strings = new LinkedHashSet<String>(values);
        return strings;
    }

    NavigatorStructureChecker createNavigatorStructureChecker()
    {
        return new NavigatorStructureChecker<Version>(clauseNames, true, fieldFlagOperandRegistry, operandResolver);
    }

    IndexedInputHelper getIndexedInputHelper()
    {
        if(indexedInputHelper == null)
        {
            indexedInputHelper = createIndexedInputHelper();
        }
        return indexedInputHelper;
    }

    ///CLOVER:OFF
    private IndexedInputHelper createIndexedInputHelper()
    {
        return new VersionIndexedInputHelper(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, versionResolver);
    }
    ///CLOVER:ON

    /**
     * @return the {@link com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper} always
     */
    DefaultIndexedInputHelper getDefaultIndexedInputHelper()
    {
        if (defaultIndexedInputHelper == null)
        {
            defaultIndexedInputHelper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry);
        }
        return defaultIndexedInputHelper;
    }

    // Because this is only ever called AFTER we have done the structure check we can assume that there is one and only
    // one version in the query and that it contains a SingleValueOperand
    boolean queryContainsArchivedVersions(final User user, final Query query)
    {
        final SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor(clauseNames.getJqlFieldNames());
        if (query != null && query.getWhereClause() != null)
        {
            query.getWhereClause().accept(collector);
            if (collector.getClauses().size() == 1)
            {
                final TerminalClause terminalClause = collector.getClauses().get(0);

                final List<String> ids = new ArrayList<String>();
                final Operand operand = terminalClause.getOperand();
                final List<QueryLiteral> queryLiteralList = operandResolver.getValues(user, operand, terminalClause);
                for (QueryLiteral queryLiteral : queryLiteralList)
                {
                    if (queryLiteral.getStringValue() != null)
                    {
                        ids.addAll(indexInfoResolver.getIndexedValues(queryLiteral.getStringValue()));
                    }
                    else if (queryLiteral.getLongValue() != null)
                    {
                        ids.addAll(indexInfoResolver.getIndexedValues(queryLiteral.getLongValue()));
                    }
                }

                for (String idStr : ids)
                {
                    Long lid = parseLong(idStr);
                    if (lid != null)
                    {
                        final Version version = versionManager.getVersion(lid);
                        if (version != null)
                        {
                            if (version.isArchived())
                            {
                                return true;
                            }
                        }
                    }

                }
            }
        }

        return false;
    }

    private Long parseLong(String str)
    {
        try
        {
            return Long.valueOf(str);
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }

}
