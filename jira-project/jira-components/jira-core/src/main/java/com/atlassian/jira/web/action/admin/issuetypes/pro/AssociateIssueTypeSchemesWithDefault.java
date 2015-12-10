package com.atlassian.jira.web.action.admin.issuetypes.pro;

import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.fields.option.ProjectOption;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import com.atlassian.jira.web.action.admin.issuetypes.AbstractManageIssueTypeOptionsAction;
import com.atlassian.jira.web.action.admin.issuetypes.IssueTypeManageableOption;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@WebSudoRequired
public class AssociateIssueTypeSchemesWithDefault extends AbstractManageIssueTypeOptionsAction
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private Long[] projects;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    protected final ProjectManager projectManager;
    private final JiraContextTreeManager treeManager;
    private Collection availableProjects;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public AssociateIssueTypeSchemesWithDefault(FieldConfigSchemeManager configSchemeManager,
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
        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Set the contexts
        List contexts = CustomFieldUtils.buildJiraIssueContexts(false,
                                                                null,
                                                                getProjects(),
                                                                treeManager);
        configSchemeManager.removeSchemeAssociation(contexts, getConfigurableField());

        fieldManager.refresh();

        return getRedirect(getConfigScheme());
    }


    // --------------------------------------------------------------------------------------------- View Helper Methods
    public Collection getAllProjects() throws Exception
    {
        if (availableProjects == null)
        {
            // Only show projects that don't have the default issue type scheme as the default

            final Collection projects = new ArrayList(projectManager.getProjects());
            final FieldConfigScheme defaultIssueTypeScheme = issueTypeSchemeManager.getDefaultIssueTypeScheme();
            CollectionUtils.filter(projects, new Predicate()
            {
                public boolean evaluate(Object object)
                {
                    GenericValue project = (GenericValue) object;
                    FieldConfigScheme configScheme = issueTypeSchemeManager.getConfigScheme(project);
                    return !defaultIssueTypeScheme.equals(configScheme);
                }
            });

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

    public boolean isDefault()
    {
        return true;
    }

    // -------------------------------------------------------------------------------------------------- Private helpers
    private Collection getChangedProjectIds()
    {
        List previousProjects = getConfigScheme().getAssociatedProjects();
        List previousProjectIds;
        if (previousProjects != null && !previousProjects.isEmpty())
        {
            previousProjectIds = GenericValueUtils.transformToLongIdsList(previousProjects);
        }
        else
        {
            previousProjectIds = Collections.EMPTY_LIST;
        }

        List newProjectIds;
        if (getProjects() != null && getProjects().length > 0)
        {
            newProjectIds = Arrays.asList(getProjects());
        }
        else
        {
            newProjectIds = Collections.EMPTY_LIST;
        }

        Collection affectedProjectIds = CollectionUtils.disjunction(previousProjectIds, newProjectIds);
        return affectedProjectIds;
    }
}
