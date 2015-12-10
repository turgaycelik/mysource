package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.util.transformers.JiraTransformers;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * The SearchInputTransformer for the Project system field.
 *
 * @since v4.0
 */
public class ProjectSearchInputTransformer implements SearchInputTransformer
{
    private final FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private final JqlOperandResolver operandResolver;
    private final ProjectIndexInfoResolver projectIndexInfoResolver;
    private final ProjectManager projectManager;
    private final UserProjectHistoryManager projectHistoryManager;
    private final JiraAuthenticationContext authenticationContext;

    public ProjectSearchInputTransformer(final ProjectIndexInfoResolver projectIndexInfoResolver, final JqlOperandResolver operandResolver,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final ProjectManager projectManager, final UserProjectHistoryManager projectHistoryManager, final JiraAuthenticationContext authenticationContext)
    {
        this.fieldFlagOperandRegistry = fieldFlagOperandRegistry;
        this.operandResolver = operandResolver;
        this.projectIndexInfoResolver = projectIndexInfoResolver;
        this.projectManager = projectManager;
        this.projectHistoryManager = projectHistoryManager;
        this.authenticationContext = authenticationContext;
    }

    public void populateFromParams(final User user, final FieldValuesHolder fieldValuesHolder, final ActionParams actionParams)
    {
        final String url = SystemSearchConstants.forProject().getUrlParameter();
        String[] params = actionParams.getValuesForKey(url);
        fieldValuesHolder.put(url, params == null ? null : Sets.newLinkedHashSet(Arrays.asList(params)));
    }

    ///CLOVER:OFF
    public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        // We currently dont do anything
    }
    ///CLOVER:ON

    public void populateFromQuery(final User user, final FieldValuesHolder fieldValuesHolder, final Query query, final SearchContext searchContext)
    {
        final Set<String> uncleanedValues = getNavigatorValuesAsStrings(user, query);

        final List<String> values = new ArrayList<String>(uncleanedValues);
        CollectionUtils.transform(values, JiraTransformers.NULL_SWAP);
        fieldValuesHolder.put(SystemSearchConstants.forProject().getUrlParameter(), values);
        setProjectIdInSession(uncleanedValues);
    }

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        return createNavigatorStructureChecker().checkSearchRequest(query);
    }

    public Set<String> getIdValuesAsStrings(final User searcher, final Query query)
    {
        IndexedInputHelper helper = createIndexedInputHelper();
        return helper.getAllIndexValuesForMatchingClauses(searcher, SystemSearchConstants.forProject().getJqlClauseNames(), query);
    }

    private Set<String> getNavigatorValuesAsStrings(User searcher, Query query)
    {
        IndexedInputHelper helper = createIndexedInputHelper();
        return helper.getAllNavigatorValuesForMatchingClauses(searcher, SystemSearchConstants.forProject().getJqlClauseNames(), query);
    }

    public Clause getSearchClause(final User user, final FieldValuesHolder fieldValuesHolder)
    {
        List<String> projects = (List<String>) fieldValuesHolder.get(SystemSearchConstants.forProject().getUrlParameter());
        if (projects != null && projects.size() > 0)
        {
            List<Operand> operands = new ArrayList<Operand>();
            for (String idStr : projects)
            {
                // remove the "ALL" flag
                if (!idStr.equals("-1"))
                {
                    operands.add(getProjectOperandForIdString(idStr));
                }
            }
            if (operands.size() == 1)
            {
                return new TerminalClauseImpl(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, operands.get(0));
            }
            else if (operands.size() > 1)
            {
                return new TerminalClauseImpl(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), Operator.IN, new MultiValueOperand(operands));
            }
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
            final Project project = projectManager.getProjectObj(id);
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

    /**
     * Sets the project Id in session if only one project was selected.
     *
     * @param selectedProjectIds Collecyion of Longs. Assumes that all non-null Long are valid project Ids
     */
    void setProjectIdInSession(Set<String> selectedProjectIds)
    {
        if (selectedProjectIds != null && selectedProjectIds.size() == 1)
        {
            String idStr = selectedProjectIds.iterator().next();
            Long id = getValueAsLong(idStr);
            if (id != null)
            {
                final Project project = projectManager.getProjectObj(id);
                if (project != null)
                {
                    projectHistoryManager.addProjectToHistory(authenticationContext.getLoggedInUser(), project);
                }
            }
        }
    }

    NavigatorStructureChecker createNavigatorStructureChecker()
    {
        return new NavigatorStructureChecker<Project>(SystemSearchConstants.forProject().getJqlClauseNames(), false, fieldFlagOperandRegistry, operandResolver);
    }

    IndexedInputHelper createIndexedInputHelper()
    {
        return new DefaultIndexedInputHelper<Project>(projectIndexInfoResolver, operandResolver, fieldFlagOperandRegistry);
    }

    private Long getValueAsLong(final String value)
    {
        try
        {
            return new Long(value);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
