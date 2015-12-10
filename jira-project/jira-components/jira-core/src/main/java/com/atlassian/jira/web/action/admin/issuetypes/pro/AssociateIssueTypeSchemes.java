package com.atlassian.jira.web.action.admin.issuetypes.pro;

import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.fields.option.ProjectOption;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import com.atlassian.jira.web.action.admin.issuetypes.AbstractManageIssueTypeOptionsAction;
import com.atlassian.jira.web.action.admin.issuetypes.ExecutableAction;
import com.atlassian.jira.web.action.admin.issuetypes.IssueTypeManageableOption;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.collections.CollectionUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@WebSudoRequired
public class AssociateIssueTypeSchemes extends AbstractManageIssueTypeOptionsAction implements ExecutableAction
{
    // ------------------------------------------------------------------------------------------------------- Constants
    protected final String NO_REDIRECT = "NO_REDIRECT";
    // ------------------------------------------------------------------------------------------------- Type Properties
    private Long[] projects;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    protected final ProjectManager projectManager;
    private final JiraContextTreeManager treeManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public AssociateIssueTypeSchemes(FieldConfigSchemeManager configSchemeManager,
            IssueTypeSchemeManager issueTypeSchemeManager, FieldManager fieldManager, OptionSetManager optionSetManager,
            IssueTypeManageableOption manageableOptionType, BulkMoveOperation bulkMoveOperation,
            SearchProvider searchProvider, ProjectManager projectManager, JiraContextTreeManager treeManager,
            IssueManager issueManager)
    {
        super(configSchemeManager, issueTypeSchemeManager, fieldManager, optionSetManager, manageableOptionType,
                bulkMoveOperation, searchProvider, issueManager);
        this.projectManager = projectManager;
        this.treeManager = treeManager;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    // -------------------------------------------------------------------------------------------------- Action Methods
    public String doDefault() throws Exception
    {
        final FieldConfigScheme configScheme = getConfigScheme();
        final List projectsList = configScheme.getAssociatedProjects();
        setProjects(GenericValueUtils.transformToLongIds(projectsList));

        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        return doExecute(true);
    }

    protected String doExecute(boolean useRedirect) throws Exception
    {
        // Make sure that no issue types have become obselete
        if (getProjects() != null && getProjects().length > 0)
        {
            OptionSet targetOptionSet = optionSetManager.getOptionsForConfig(getConfigScheme().getOneAndOnlyConfig());

            Collection<Long> affectedProjectIds = getChangedProjectIds();

            Collection obseleteOptionIds = new HashSet();
            IssueTypeField issueTypeField = fieldManager.getIssueTypeField();
            for (final Long projectId : affectedProjectIds)
            {
                FieldConfig relevantConfig = issueTypeField.getRelevantConfig(new IssueContextImpl(projectId, null));
                OptionSet existingOptionSet = optionSetManager.getOptionsForConfig(relevantConfig);

                // Add all obselete issue types
                obseleteOptionIds.addAll(CollectionUtils.subtract(existingOptionSet.getOptionIds(), targetOptionSet.getOptionIds()));
            }

            if (!obseleteOptionIds.isEmpty())
            {
                Query query = getQuery(affectedProjectIds, obseleteOptionIds);
                SearchResults searchResults = searchProvider.search(query, getLoggedInUser(), PagerFilter.getUnlimitedFilter());
                List affectedIssues = searchResults.getIssues();
                if (affectedIssues != null && !affectedIssues.isEmpty())
                {
                    return migrateIssues(this, affectedIssues, targetOptionSet.getOptionIds());
                }
            }
        }

        run();

        if (useRedirect)
        {
            return getRedirect(getConfigScheme());
        }
        else
        {
            // dont use a redirect
            return NO_REDIRECT;
        }
    }

    public void run()
    {
        // Associate with the selected projects
        FieldConfigScheme configScheme = getConfigScheme();

        // Set the contexts
        List contexts = CustomFieldUtils.buildJiraIssueContexts(false,
                                                                null,
                                                                getProjects(),
                                                                treeManager);

        configScheme = configSchemeManager.updateFieldConfigScheme(configScheme, contexts, getConfigurableField());

        fieldManager.refresh();
    }

    // --------------------------------------------------------------------------------------------- View Helper Methods
    public Collection getAllProjects() throws Exception
    {
        Collection availableProjects = Collections.EMPTY_LIST;
        final Collection projects = projectManager.getProjects();
        if (projects != null)
        {
            availableProjects = CollectionUtils.collect(projects, ProjectOption.TRANSFORMER);
        }

        return availableProjects;
    }

    public Collection getOptions(FieldConfigScheme configScheme)
    {
        if (configScheme != null)
        {
            final FieldConfig config = configScheme.getOneAndOnlyConfig();
            return optionSetManager.getOptionsForConfig(config).getOptions();
        }

        return Collections.EMPTY_LIST;
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public Long[] getProjects()
    {
        return projects;
    }

    public void setProjects(Long[] projects)
    {
        this.projects = projects;
    }

    // -------------------------------------------------------------------------------------------------- Private helpers
    private Collection<Long> getChangedProjectIds()
    {
        List<GenericValue> previousProjects = getConfigScheme().getAssociatedProjects();
        List<Long> previousProjectIds;
        if (previousProjects != null && !previousProjects.isEmpty())
        {
            previousProjectIds = GenericValueUtils.transformToLongIdsList(previousProjects);
        }
        else
        {
            previousProjectIds = Collections.emptyList();
        }

        List<Long> newProjectIds;
        if (getProjects() != null && getProjects().length > 0)
        {
            newProjectIds = Arrays.asList(getProjects());
        }
        else
        {
            newProjectIds = Collections.emptyList();
        }

        Collection<Long> affectedProjectIds =  CollectionUtils.disjunction(previousProjectIds, newProjectIds);
        return affectedProjectIds;
    }
}
