package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigSchemePersisterImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class ManageConfigurationScheme extends JiraWebActionSupport
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String REDIRECT_URL_PREFIX = "ConfigureCustomField!default.jspa?customFieldId=";

    // ------------------------------------------------------------------------------------------------- Type Properties
    private Long fieldConfigSchemeId;
    private Long customFieldId;
    private FieldConfigScheme fieldConfigScheme;

    private String name;
    private String description;

    private boolean global = true;
    private boolean basicMode = true;

    private Long[] projectCategories;
    private Long[] projects;
    private String[] issuetypes = { "-1" };
    private Long[] fieldConfigIds = new Long[0];

    // ---------------------------------------------------------------------------------------------------- Dependencies
    protected final CustomFieldManager customFieldManager;
    protected final FieldConfigSchemeManager fieldConfigSchemeManager;
    protected final FieldConfigManager fieldConfigManager;
    private final SubTaskManager subTaskManager;
    protected final ProjectManager projectManager;
    protected final ConstantsManager constantsManager;
    protected final JiraContextTreeManager treeManager;
    protected final ReindexMessageManager reindexMessageManager;
    protected final CustomFieldContextConfigHelper customFieldContextConfigHelper;
    protected final ManagedConfigurationItemService managedConfigurationItemService;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public ManageConfigurationScheme(final FieldConfigManager fieldConfigManager, final CustomFieldManager customFieldManager,
            final FieldConfigSchemeManager fieldConfigSchemeManager, final ProjectManager projectManager,
            final ConstantsManager constantsManager, final JiraContextTreeManager treeManager, final SubTaskManager subTaskManager,
            final ReindexMessageManager reindexMessageManager, final CustomFieldContextConfigHelper customFieldContextConfigHelper,
            ManagedConfigurationItemService managedConfigurationItemService)
    {
        this.customFieldManager = customFieldManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.treeManager = treeManager;
        this.fieldConfigManager = fieldConfigManager;
        this.subTaskManager = subTaskManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
        this.customFieldContextConfigHelper = notNull("customFieldContextConfigHelper", customFieldContextConfigHelper);
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    @Override
    protected void doValidation()
    {
        if (StringUtils.isBlank(name))
        {
            addError("name", getText("admin.errors.must.enter.name"));
        }

        if ((issuetypes == null) || (issuetypes.length == 0))
        {
            addError("issuetypes", getText("admin.errors.must.select.issue.type"));
        }

        if (!isGlobal() && ((projects == null) || (projects.length == 0)))
        {
            addError("projects", getText("admin.errors.must.select.project"));
        }
    }

    @Override
    public String doDefault() throws Exception
    {
        if (isFieldLocked())
        {
            return "locked";
        }

        // Set up the context so the view can see it nicely.
        final FieldConfigScheme configScheme = getConfig();
        setGlobal(isGlobalAvailable());
        if (configScheme != null)
        {
            setName(configScheme.getName());
            setDescription(configScheme.getDescription());

            // Check if this is still editable in "basic" mode
            if (configScheme.isBasicMode())
            {
                setBasicMode(true);
                setGlobal(configScheme.isAllProjects());

                final Set issueTypesList = configScheme.getAssociatedIssueTypes();
                setIssuetypes(GenericValueUtils.transformToStrings(issueTypesList, "id"));

                final List projectCategoriesList = configScheme.getAssociatedProjectCategories();
                setProjectCategories(GenericValueUtils.transformToLongIds(projectCategoriesList));

                final List projectsList = configScheme.getAssociatedProjects();
                setProjects(GenericValueUtils.transformToLongIds(projectsList));

                // Set the config
                final MultiMap configMap = configScheme.getConfigsByConfig();
                if (configMap == null)
                {
                    fieldConfigIds = new Long[0];
                }
                else
                {
                    final Set entries = configScheme.getConfigsByConfig().keySet();
                    fieldConfigIds = new Long[entries.size()];
                    int i = 0;
                    for (final Object entry : entries)
                    {
                        final FieldConfig config = (FieldConfig) entry;
                        fieldConfigIds[i] = config.getId();
                        i++;
                    }
                }

            }
            else
            {
                // Complex mode
                setBasicMode(false);
                // @TODO
            }

        }

        return super.doDefault();
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (isFieldLocked())
        {
            return "locked";
        }

        FieldConfigScheme configScheme = new FieldConfigScheme.Builder(getConfig()).setName(getName()).setDescription(getDescription()).toFieldConfigScheme();

        if (isBasicMode())
        {
            // Add the contexts
            final List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(isGlobal(), getProjectCategories(), getProjects(),
                treeManager);

            // Add the issue types
            final List<GenericValue> issueTypes = CustomFieldUtils.buildIssueTypes(constantsManager, getIssuetypes());

            boolean messageRequired;
            if (configScheme.getId() == null)
            {
                messageRequired = customFieldContextConfigHelper.doesAddingContextToCustomFieldAffectIssues(getLoggedInUser(), getCustomField(),
                    contexts, issueTypes, false);

                configScheme = fieldConfigSchemeManager.createFieldConfigScheme(configScheme, contexts, issueTypes, getCustomField());
            }
            else
            {
                // keep a handle on the old scheme (pre edit) projects and issue types
                messageRequired = customFieldContextConfigHelper.doesChangingContextAffectIssues(getLoggedInUser(), getCustomField(), configScheme,
                    isGlobal(), contexts, issueTypes);

                // Update so keep the old config
                if (issueTypes != null)
                {
                    // Since we know that there is only one config
                    final Long configId = getFieldConfigIds()[0];
                    final FieldConfig config = fieldConfigManager.getFieldConfig(configId);
                    final Map<String, FieldConfig> configs = new HashMap<String, FieldConfig>(issueTypes.size());
                    for (final GenericValue issueType : issueTypes)
                    {
                        final String issueTypeId = issueType == null ? null : issueType.getString(FieldConfigSchemePersisterImpl.ENTITY_ID);
                        configs.put(issueTypeId, config);
                    }
                    configScheme = new FieldConfigScheme.Builder(configScheme).setConfigs(configs).toFieldConfigScheme();
                }
                configScheme = fieldConfigSchemeManager.updateFieldConfigScheme(configScheme, contexts, getCustomField());
            }

            if (messageRequired)
            {
                reindexMessageManager.pushMessage(getLoggedInUser(), "admin.notifications.task.custom.fields");
            }

            fieldConfigScheme = configScheme;
        }
        else
        {
            // @TODO advanced config
        }

        ComponentAccessor.getFieldManager().refresh();
        customFieldManager.refreshConfigurationSchemes(getCustomFieldId());

        return redirectToView();
    }

    @RequiresXsrfCheck
    public String doRemove() throws Exception
    {
        if (isFieldLocked())
        {
            return "locked";
        }

        final FieldConfigScheme configScheme = getConfig();

        if (customFieldContextConfigHelper.doesRemovingSchemeFromCustomFieldAffectIssues(getLoggedInUser(), getCustomField(), configScheme))
        {
            reindexMessageManager.pushMessage(getLoggedInUser(), "admin.notifications.task.custom.fields");
        }

        fieldConfigSchemeManager.removeFieldConfigScheme(configScheme.getId());

        ComponentAccessor.getFieldManager().refresh();
        customFieldManager.refreshConfigurationSchemes(getCustomFieldId());
        return redirectToView();
    }

    // ------------------------------------------------------------------------------------------ Private Helper Methods
    public FieldConfigScheme getConfig()
    {
        if ((fieldConfigScheme == null) && (fieldConfigSchemeId != null))
        {
            fieldConfigScheme = fieldConfigSchemeManager.getFieldConfigScheme(fieldConfigSchemeId);
        }

        return fieldConfigScheme;
    }

    public CustomField getCustomField()
    {
        return customFieldManager.getCustomFieldObject(getCustomFieldId());
    }

    public Collection getAllProjectCategories() throws Exception
    {
        return projectManager.getProjectCategories();
    }

    public Collection getAllProjects() throws Exception
    {
        Collection availableProjects = Collections.EMPTY_LIST;

        final Collection projects = projectManager.getProjects();

        if (projects != null)
        {
            availableProjects = new ArrayList(projects);
            availableProjects = CollectionUtils.subtract(availableProjects, getCustomField().getAssociatedProjects());
            final FieldConfigScheme fieldConfigScheme = getFieldConfigScheme();
            if (fieldConfigScheme != null)
            {
                final List currentlySlectedProjects = fieldConfigScheme.getAssociatedProjects();
                if (currentlySlectedProjects != null)
                {
                    availableProjects.addAll(currentlySlectedProjects);
                }
            }
        }

        return availableProjects;
    }

    public Collection getAllIssueTypes() throws Exception
    {
        if (subTaskManager.isSubTasksEnabled())
        {
            return constantsManager.getAllIssueTypeObjects();
        }
        else
        {
            final ArrayList returnValues = new ArrayList(constantsManager.getRegularIssueTypeObjects());
            // Now, since subtasks are disabled we want to make sure we add any subtask issue types that are already
            // selected in the custom field and make sure that the sort order is the same as when we call getAllIssueTypeObjects
            final List intersection = new ArrayList(CollectionUtils.intersection(constantsManager.getSubTaskIssueTypes(),
                getCustomField().getAssociatedIssueTypes()));
            Collections.sort(intersection);
            for (final Object anIntersection : intersection)
            {
                final GenericValue genericValue = (GenericValue) anIntersection;
                returnValues.add(0, constantsManager.getIssueTypeObject(genericValue.getString("id")));
            }
            return returnValues;
        }
    }

    private String redirectToView()
    {
        return getRedirect(REDIRECT_URL_PREFIX + getCustomField().getIdAsLong() + "&fieldConfigSchemeId=" + getConfig().getId());
    }

    public boolean isGlobalAvailable()
    {
        if (!getCustomField().isAllProjects())
        {
            return true;
        }
        else
        {
            return (getFieldConfigScheme() != null) && getFieldConfigScheme().isAllProjects();
        }
    }

    public boolean isFieldLocked()
    {
        CustomField customField = getCustomField();
        ManagedConfigurationItem managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(customField);
        boolean locked = !managedConfigurationItemService.doesUserHavePermission(getLoggedInUser(), managedConfigurationItem);
        if (locked)
        {
            addErrorMessage(getText("admin.managed.configuration.items.customfield.error.cannot.alter.context.locked", customField.getName()), Reason.FORBIDDEN);
        }
        return locked;
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public void setFieldConfigSchemeId(final Long fieldConfigSchemeId)
    {
        this.fieldConfigSchemeId = fieldConfigSchemeId;
    }

    public Long getFieldConfigSchemeId()
    {
        return fieldConfigSchemeId;
    }

    public Long[] getProjects()
    {
        return projects;
    }

    public void setProjects(final Long[] projects)
    {
        this.projects = projects;
    }

    public String[] getIssuetypes()
    {
        return issuetypes;
    }

    public void setIssuetypes(final String[] issuetypes)
    {
        this.issuetypes = issuetypes;
    }

    public Long getCustomFieldId()
    {
        return customFieldId;
    }

    public void setCustomFieldId(final Long customFieldId)
    {
        this.customFieldId = customFieldId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public FieldConfigScheme getFieldConfigScheme()
    {
        return fieldConfigScheme;
    }

    public void setFieldConfigScheme(final FieldConfigScheme fieldConfigScheme)
    {
        this.fieldConfigScheme = fieldConfigScheme;
    }

    public Long[] getProjectCategories()
    {
        return projectCategories;
    }

    public void setProjectCategories(final Long[] projectCategories)
    {
        this.projectCategories = projectCategories;
    }

    public Map getGlobalContextOption()
    {
        return CustomFieldContextManagementBean.getGlobalContextOption();
    }

    public boolean isBasicMode()
    {
        return basicMode;
    }

    public void setBasicMode(final boolean basicMode)
    {
        this.basicMode = basicMode;
    }

    public boolean isGlobal()
    {
        return global;
    }

    public void setGlobal(final boolean global)
    {
        this.global = global;
    }

    public Long[] getFieldConfigIds()
    {
        return fieldConfigIds;
    }

    public void setFieldConfigIds(final Long[] fieldConfigIds)
    {
        this.fieldConfigIds = fieldConfigIds;
    }
}
