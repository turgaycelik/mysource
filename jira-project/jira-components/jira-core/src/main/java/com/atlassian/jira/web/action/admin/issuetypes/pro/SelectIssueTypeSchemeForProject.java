package com.atlassian.jira.web.action.admin.issuetypes.pro;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.fields.option.ProjectOption;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ExceptionUtil;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import com.atlassian.jira.web.action.admin.issuetypes.IssueTypeManageableOption;
import com.atlassian.query.Query;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class SelectIssueTypeSchemeForProject extends AssociateIssueTypeSchemes
{
    // ------------------------------------------------------------------------------------------------------- Constants
    private static final String CREATE_SCHEME = "createScheme";
    private static final String CHOOSE_SCHEME = "chooseScheme";
    private static final String CHOOSE_PROJECT = "chooseProject";

    // ------------------------------------------------------------------------------------------------- Type Properties
    private Long projectId;
    private String createType;
    private Long sameAsProjectId;
    private String[] selectedOptions;

    // ----------------------------------------------------------------------------------------------- Cached Properties
    private long subTaskCount = -1;
    private long standardIssuesCount = -1;

    // ---------------------------------------------------------------------------------------------------- Dependencies


    // ---------------------------------------------------------------------------------------------------- Constructors
    public SelectIssueTypeSchemeForProject(FieldConfigSchemeManager configSchemeManager,
            IssueTypeSchemeManager issueTypeSchemeManager, FieldManager fieldManager, OptionSetManager optionSetManager,
            IssueTypeManageableOption manageableOptionType, BulkMoveOperation bulkMoveOperation,
            SearchProvider searchProvider, ProjectManager projectManager, JiraContextTreeManager treeManager,
            IssueManager issueManager)
    {
        super(configSchemeManager, issueTypeSchemeManager, fieldManager, optionSetManager, manageableOptionType,
                bulkMoveOperation, searchProvider, projectManager, treeManager, issueManager);
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    public String doDefault() throws Exception
    {
        FieldConfigScheme currentScheme = getCurrentIssueTypeScheme();
        if (currentScheme != null)
        {
            setSchemeId(currentScheme.getId());
        }
        setCreateType(CHOOSE_SCHEME);
        return INPUT;
    }

    protected void doValidation()
    {
        if (CREATE_SCHEME.equals(getCreateType()))
        {
            FieldConfigScheme existingAutoCreatedScheme = getExistingAutoCreatedScheme();
            if (existingAutoCreatedScheme != null)
            {
                addErrorMessage(getText("admin.errors.already.an.issue.type.scheme.with.that.name"));
            }

            // Validate that you've chosen the minimum
            if (getSelectedOptions() != null)
            {
                boolean hasNormalIssueType = false;
                boolean hasSubTaskIssueType = false;
                for (int i = 0; i < getSelectedOptions().length; i++)
                {
                    String id = getSelectedOptions()[i];
                    IssueType issueType = ComponentAccessor.getConstantsManager().getIssueTypeObject(id);
                    if (!issueType.isSubTask())
                    {
                        hasNormalIssueType = true;
                    }
                    else
                    {
                        hasSubTaskIssueType = true;
                    }
                }

                if (!hasNormalIssueType)
                {
                    addError("selectedOptions", getText("admin.errors.must.select.at.least.one.standard.issue.type"));
                }

                try
                {
                    if (!hasSubTaskIssueType && getSubTaskIssues() > 0)
                    {
                        addError("selectedOptions", getText("admin.errors.project.has.subtasks",""+getSubTaskIssues()));
                    }
                }
                catch (SearchException e)
                {
                    addErrorMessage(getText("admin.projects.issuetypescheme.error.sever", ExceptionUtil.getExceptionAsHtml(e)));
                }
            }
            else
            {
                addError("selectedOptions", getText("admin.errors.must.select.at.least.one.standard.issue.type"));
            }

        }
        else if (CHOOSE_PROJECT.equals(getCreateType()))
        {
            if (sameAsProjectId == null)
            {
                addError("sameAsProjectId", getText("admin.projects.issuetypescheme.error.must.select.project"));
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // If is create scheme then create it!
        if (CREATE_SCHEME.equals(getCreateType()))
        {
            FieldConfigScheme createdScheme = issueTypeSchemeManager.create(getDefaultNameForNewScheme(), "", EasyList.build(getSelectedOptions()));
            setConfigScheme(createdScheme);
        }
        else if (CHOOSE_PROJECT.equals(getCreateType()))
        {
            FieldConfigScheme schemeOfSelectedProject = configSchemeManager.getFieldConfigScheme(sameAsProjectId);
            setConfigScheme(schemeOfSelectedProject);
        }


        // Sets the value of the config scheme
        if (getCurrentIssueTypeScheme().equals(getConfigScheme()))
        {
            return getRedirect("/plugins/servlet/project-config/" + getProject().getString("key") + "/issuetypes");
        }
        // If the target scheme is global..
        else if (getConfigScheme().isGlobal())
        {
            // We need to remove from the old config
            setConfigScheme(getCurrentIssueTypeScheme());
            FieldConfigScheme configScheme = getConfigScheme();
            final List projectsList = configScheme.getAssociatedProjects();
            Long[] projectIds = GenericValueUtils.transformToLongIds(CollectionUtils.subtract(projectsList, EasyList.build(getProject())));
            setProjects(projectIds);

            String result = super.doExecute(false);
            if (result.equals(super.NO_REDIRECT))
            {
                return getRedirect("/plugins/servlet/project-config/" + getProject().getString("key") + "/issuetypes");
            }
            else
            {
                return result;
            }
        }
        else
        {
            // Sets the value of the projects
            FieldConfigScheme configScheme = getConfigScheme();

            final List projectsList;
            List associatedProjects = configScheme.getAssociatedProjects();
            if (associatedProjects != null && !associatedProjects.isEmpty())
            {
                projectsList = new ArrayList(associatedProjects);
            }
            else
            {
                projectsList = new ArrayList(1);
            }

            projectsList.add(getProject());
            Long[] projectIds = GenericValueUtils.transformToLongIds(projectsList);
            setProjects(projectIds);

            String result = super.doExecute(false);

            if (result.equals(super.NO_REDIRECT))
            {
                return getRedirect("/plugins/servlet/project-config/" + getProject().getString("key") + "/issuetypes");
            }
            else
            {
                return result;
            }
        }
    }

    // --------------------------------------------------------------------------------------------- View Helper Methods
    public GenericValue getProject()
    {
        return projectManager.getProject(getProjectId());
    }

    public FieldConfigScheme getCurrentIssueTypeScheme()
    {
        return issueTypeSchemeManager.getConfigScheme(getProject());
    }

    // --------------------------------------------------------------------------------------------- View Helper Methods
    public Collection getAllProjects() throws Exception
    {
        return CollectionUtils.select(super.getAllProjects(), new Predicate()
        {
            public boolean evaluate(Object object)
            {
                ProjectOption projectOption = (ProjectOption) object;
                return !getProjectId().toString().equals(projectOption.getId());
            }
        });
    }

    public List getAllSchemes()
    {
        return issueTypeSchemeManager.getAllSchemes();
    }

    public FieldConfigScheme getConfigSchemeForProject(String project)
    {
        return issueTypeSchemeManager.getConfigScheme(projectManager.getProjectByName(project));
    }

    public Map getTypeOptions() throws Exception
    {
        ListOrderedMap typeOptions = new ListOrderedMap();
        typeOptions.put(CHOOSE_SCHEME, getText("admin.projects.issuetypescheme.radio1"));
        if (!getAllProjects().isEmpty())
        {
            typeOptions.put(CHOOSE_PROJECT, getText("admin.projects.issuetypescheme.radio2"));
        }
        typeOptions.put(CREATE_SCHEME, getText("admin.projects.issuetypescheme.radio4"));
        return typeOptions;
    }

    public String getDefaultNameForNewScheme()
    {
        return getText("admin.projects.issuetypescheme.new.issue.type", getProject().getString("name"));
    }

    public Collection getAllOptions()
    {
        final Collection constantObjects = ComponentAccessor.getConstantsManager().getConstantObjects(getManageableOption().getFieldId());
        Collection options = new ArrayList(constantObjects);
        CollectionUtils.transform(options, new Transformer()
        {
            public Object transform(Object input)
            {
                return new IssueConstantOption((IssueConstant) input);
            }
        });
        return options;
    }

    public long getSubTaskIssues() throws SearchException
    {
        if (subTaskCount == -1)
        {
            Query query = getQuery(EasyList.build(getProjectId()), EasyList.build(ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES));
            subTaskCount = searchProvider.searchCount(query, getLoggedInUser());
        }


        return subTaskCount;
    }

    public long getStandardIssues() throws SearchException
    {
        if (standardIssuesCount == -1)
        {
            Query query = getQuery(EasyList.build(getProjectId()), EasyList.build(ConstantsManager.ALL_STANDARD_ISSUE_TYPES));
            standardIssuesCount = searchProvider.searchCount(query, getLoggedInUser());
        }
        return standardIssuesCount;
    }

    public FieldConfigScheme getExistingAutoCreatedScheme()
    {
        Collection c = CollectionUtils.select(issueTypeSchemeManager.getAllSchemes(), new FieldConfigPredicate(null, getDefaultNameForNewScheme()));
        if (c != null && !c.isEmpty())
        {
            return (FieldConfigScheme) c.iterator().next();
        }
        else
        {
            return null;
        }
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public String getCreateType()
    {
        return createType;
    }

    public void setCreateType(String createType)
    {
        this.createType = createType;
    }

    public Long getSameAsProjectId()
    {
        return sameAsProjectId;
    }

    public void setSameAsProjectId(Long sameAsProjectId)
    {
        this.sameAsProjectId = sameAsProjectId;
    }

    public String[] getSelectedOptions()
    {
        return selectedOptions;
    }

    public void setSelectedOptions(String[] selectedOptions)
    {
        this.selectedOptions = selectedOptions;
    }

    // -------------------------------------------------------------------------------------------------- Private helper


}
