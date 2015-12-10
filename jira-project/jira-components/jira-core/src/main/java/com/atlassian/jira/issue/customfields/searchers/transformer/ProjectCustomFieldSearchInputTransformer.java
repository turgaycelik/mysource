package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} for project custom fields.
 *
 * @since v4.0
 */
public class ProjectCustomFieldSearchInputTransformer extends AbstractSingleValueCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    private final ClauseNames clauseNames;
    private final JqlOperandResolver jqlOperandResolver;
    private final ProjectIndexInfoResolver indexInfoResolver;
    private final FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private final NameResolver<Project> projectResolver;
    private IndexedInputHelper indexedInputHelper;

    public ProjectCustomFieldSearchInputTransformer(String urlParameterName, final ClauseNames clauseNames, CustomField field,
            final JqlOperandResolver jqlOperandResolver,
            final ProjectIndexInfoResolver indexInfoResolver, final FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final NameResolver<Project> projectResolver,
            final CustomFieldInputHelper customFieldInputHelper)
    {
        super(field, clauseNames, urlParameterName, customFieldInputHelper);
        this.clauseNames = clauseNames;
        this.jqlOperandResolver = jqlOperandResolver;
        this.indexInfoResolver = indexInfoResolver;
        this.fieldFlagOperandRegistry = fieldFlagOperandRegistry;
        this.projectResolver = notNull("projectResolver", projectResolver);

        indexedInputHelper = createIndexedInputHelper();
    }

    @Override
    protected CustomFieldParams getParamsFromSearchRequest(final User user, Query query, final SearchContext searchContext)
    {
        if (query == null)
        {
            return null;
        }

        final Set<String> valuesAsStrings = indexedInputHelper.getAllNavigatorValuesForMatchingClauses(user, clauseNames, query);
        return new CustomFieldParamsImpl(getCustomField(), valuesAsStrings);
    }

    @Override
    Clause createSearchClause(final User user, final String value)
    {
        // Remove the "-1" all flag
        if (!value.equals("-1"))
        {
            final Operand o = getProjectOperandForIdString(value);
            return new TerminalClauseImpl(getClauseName(user, clauseNames), Operator.EQUALS, o);
        }
        return null;
    }

    /**
     * Attempts to resolve the input string as an id for a project.
     *
     * @param idStr the id string
     * @return an operand that is the project's key. If the project does not exist, the id is returned as a long.
     * If the id was non-numeric the id is returned as a string.
     */
    private SingleValueOperand getProjectOperandForIdString(final String idStr)
    {
        try
        {
            final Long id = new Long(idStr);
            final Project project = projectResolver.get(id);
            final SingleValueOperand o;
            if (project == null)
            {
                o = new SingleValueOperand(id);
            }
            else
            {
                o = new SingleValueOperand(project.getKey());
            }
            return o;
        }
        catch (NumberFormatException e)
        {
            // we got some invalid project id - we will fall back to using String as the operand
            return new SingleValueOperand(idStr);
        }
    }

    ///CLOVER:OFF
    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        return convertForNavigator(query).fitsNavigator();
    }

    IndexedInputHelper createIndexedInputHelper()
    {
        return new DefaultIndexedInputHelper<Project>(indexInfoResolver, jqlOperandResolver, fieldFlagOperandRegistry);
    }
    ///CLOVER:ON
}
