/*
 * Atlassian Source Code Template.
 * User: mike
 * Created: 26/09/2002 16:21:17
 * Time: 4:32:34 PM
 */
package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.admin.RenderableProperty;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.customfield.CreateValidationResult;
import com.atlassian.jira.bc.customfield.CustomFieldDefinition;
import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldDescription;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWizardActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class CreateCustomField extends JiraWizardActionSupport
{

    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String FIELD_TYPE_PREFIX = "com.atlassian.jira.plugin.system.customfieldtypes:";

    // ------------------------------------------------------------------------------------------------- Type Properties
    private String fieldName;
    private String description;
    private String fieldType;
    private String searcher;

    private boolean global = true;
    private boolean basicMode = true;

    private Long[] projectCategories;
    private Long[] projects = new Long[0];
    private String[] issuetypes = {"-1"};

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final ProjectManager projectManager;
    private final SubTaskManager subTaskManager;
    private final CustomFieldDescription customFieldDescription;
    private final CustomFieldManager customFieldManager;
    private final ConstantsManager constantsManager;
    private final CustomFieldValidator customFieldValidator;
    private final FeatureManager featureManager;
    private final CustomFieldService customFieldService;

    private List<CustomFieldType<?,?>> fieldTypes;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public CreateCustomField(CustomFieldValidator customFieldValidator, ConstantsManager constantsManager,
            CustomFieldManager customFieldManager, ProjectManager projectManager, SubTaskManager subTaskManager,
            CustomFieldDescription customFieldDescription, FeatureManager featureManager,
            final CustomFieldService customFieldService)
    {
        this.customFieldValidator = customFieldValidator;
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.projectManager = projectManager;
        this.subTaskManager = subTaskManager;
        this.customFieldDescription = customFieldDescription;
        this.customFieldService = customFieldService;
        this.featureManager = featureManager;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        String url;
        if (!isFinishClicked() && isNextClicked())
        {
            url =  doCustomFieldType();
        }
        else if(isFinishClicked())
        {
            url =  doAddDetails();
        }
        else
        {
            url =  INPUT;
        }

        if (!invalidInput())
        {
            super.doExecute();
        }

        return url;
    }

    public String doCustomFieldType() throws Exception
    {
        addErrorCollection(customFieldValidator.validateType(getFieldType()));

        if (invalidInput())
        {
            return "input";
        }

        return "details";
    }

    public String doAddDetails() throws Exception
    {
        final CustomFieldDefinition.Builder builder =  CustomFieldDefinition.builder()
            .name(getFieldName())
            .description(getDescription())
            .cfType(getFieldType())
            .searcherKey(getSearcher())
            .isGlobal(isGlobal())
            .isAllIssueTypes(isGlobalIssueTypes(getIssuetypes()))
            .addProjectIds(getProjects())
            .addIssueTypeIds(getIssuetypes());


        final ServiceOutcome<CreateValidationResult> outcome = customFieldService.validateCreate(getLoggedInUser(), builder.build());

        if (!outcome.isValid())
        {
            addErrorCollection(outcome.getErrorCollection());
            return "details";
        }

        final ServiceOutcome<CustomField> customFieldServiceOutcome = customFieldService.create(outcome.getReturnedValue());
        return getRedirect("AssociateFieldToScreens!default.jspa?fieldId="+customFieldServiceOutcome.getReturnedValue().getId()+"&returnUrl=ViewCustomFields.jspa");
    }

    private boolean isGlobalIssueTypes(final String[] issueTypes)
    {
        for (final String issuetype : issueTypes)
        {
            if ("-1".equals(issuetype))
            {
                return true;
            }
        }

        return false;
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    public Collection<CustomFieldType<?,?>> getFieldTypes()
    {
        if (fieldTypes == null)
        {
            fieldTypes = Lists.newArrayList(customFieldService.getCustomFieldTypesForUser(getLoggedInApplicationUser()));
        }
        return fieldTypes;
    }


    public List getSearchers()
    {
        return customFieldManager.getCustomFieldSearchers(customFieldManager.getCustomFieldType(getFieldType()));
    }

    public RenderableProperty getDescriptionProperty()
    {
        return customFieldDescription.createRenderablePropertyFor((CustomField) null);
    }

    public CustomFieldType getCustomFieldType()
    {
        return customFieldManager.getCustomFieldType(getFieldType());
    }

    public Collection getAllProjects() throws Exception
    {
        return projectManager.getProjects();
    }

    public Collection getAllProjectCategories() throws Exception
    {
        return projectManager.getProjectCategories();
    }

    public Collection getAllIssueTypes() throws Exception
    {
        if (subTaskManager.isSubTasksEnabled())
        {
            return constantsManager.getAllIssueTypeObjects();
        }
        else
        {
            return constantsManager.getRegularIssueTypeObjects();
        }
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getFieldType()
    {
        return fieldType;
    }

    /**
     * Returns true if the current field type is not null and valid.
     * This is used as a safety precaution against XSS. See JRA-21173.
     *
     * @return true if the current field type is not null and valid.
     */
    public boolean isFieldTypeValid()
    {
        return customFieldValidator.isValidType(getFieldType());
    }

    public void setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
    }

    public String getSearcher()
    {
        // If the searcher is null, then pre-pick the first one.
        if (StringUtils.isEmpty(searcher) && StringUtils.isNotEmpty(fieldType) )
        {
            List searchers = getSearchers();
            if (searchers != null && !searchers.isEmpty())
            { searcher = ((CustomFieldSearcher) searchers.iterator().next()).getDescriptor().getCompleteKey(); }
        }

        return searcher;
    }

    public boolean isOnDemand()
    {
        return featureManager.isOnDemand();
    }

    public void setSearcher(String searcher)
    {
        this.searcher = searcher;
    }


    public Long[] getProjects()
    {
        return projects;
    }

    public void setProjects(Long[] projects)
    {
        this.projects = projects;
    }

    public String[] getIssuetypes()
    {
        return issuetypes;
    }

    public void setIssuetypes(String[] issuetypes)
    {
        this.issuetypes = issuetypes;
    }

    public Map getGlobalContextOption()
    {
        return CustomFieldContextManagementBean.getGlobalContextOption();
    }

    public boolean isGlobal()
    {
        return global;
    }

    public void setGlobal(boolean global)
    {
        this.global = global;
    }

    public boolean isBasicMode()
    {
        return basicMode;
    }

    public void setBasicMode(boolean basicMode)
    {
        this.basicMode = basicMode;
    }

    public Long[] getProjectCategories()
    {
        return projectCategories;
    }

    public void setProjectCategories(Long[] projectCategories)
    {
        this.projectCategories = projectCategories;
    }

    public int getTotalSteps()
    {
        return 2;
    }
}

