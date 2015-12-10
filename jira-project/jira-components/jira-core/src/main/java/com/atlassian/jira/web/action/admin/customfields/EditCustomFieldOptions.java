/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

@WebSudoRequired
public class EditCustomFieldOptions extends AbstractEditConfigurationItemAction
{
    // ------------------------------------------------------------------------------------------------------ Properties

    private String addValue;
    private String value;
    private String selectedValue;
    private boolean confirm;
    private Collection hlFields;

    private Long selectedParentOptionId;
    private Map customFieldValuesHolder = new HashMap();
    private Options options;
    private Object defaultValues;
    private final ReindexMessageManager reindexMessageManager;

    private static final String NEW_OPTION_POSITION_PREFIX = "newOptionPosition_";
    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final IssueManager issueManager;


    // ---------------------------------------------------------------------------------------------------- Constructors
    public EditCustomFieldOptions(IssueManager issueManager, final ReindexMessageManager reindexMessageManager,
            ManagedConfigurationItemService managedConfigurationItemService)
    {
        super(managedConfigurationItemService);
        this.issueManager = issueManager;
        this.reindexMessageManager = reindexMessageManager;
        this.hlFields = new LinkedList();
    }


    // -------------------------------------------------------------------------------------------------- Action Methods
    public String doDefault() throws Exception
    {
        setReturnUrl(null);
        if (!(getCustomField().getCustomFieldType() instanceof MultipleSettableCustomFieldType))
        { addErrorMessage(getText("admin.errors.customfields.cannot.set.options", "'" + getCustomField().getCustomFieldType().getName() + "'")); }

        EditCustomFieldDefaults.populateDefaults(getFieldConfig(), customFieldValuesHolder);

        return super.doDefault();
    }

    protected void doValidation()
    {
        if (getCustomField() == null)
        {
            addErrorMessage(getText("admin.errors.customfields.no.field.selected.for.edit"));
        }

        if (validateFieldLocked())
        {
            return;
        }
    }

    @RequiresXsrfCheck
    public String doConfigureOption()
    {
        Map parameters = ActionContext.getParameters();
        if (parameters.containsKey("moveOptionsToPosition"))
        {
            // Move the options to a different position
            return changeOptionPositions(parameters);
        }

        throw new IllegalStateException("Unknown operation.");
    }

    private String changeOptionPositions(Map parameters)
    {
        Map<Integer, Option> optionPositions = new TreeMap<Integer, Option>();

        // Loop through the submitted parameters and find out which options to move
        for (final Object o : parameters.keySet())
        {
            String paramName = (String) o;
            if (paramName.startsWith(NEW_OPTION_POSITION_PREFIX) && TextUtils.stringSet(getTextValueFromParams(paramName)))
            {
                String fieldId = paramName.substring(NEW_OPTION_POSITION_PREFIX.length());
                Integer newOptionPosition = null;
                try
                {
                    newOptionPosition = Integer.valueOf(getTextValueFromParams(paramName));
                    Integer newIndex = new Integer(newOptionPosition.intValue() - 1);
                    if (newOptionPosition.intValue() <= 0 || newOptionPosition.intValue() > getDisplayOptions().size())
                    {
                        addError(paramName, getText("admin.errors.invalid.position"));
                    }
                    else if (optionPositions.containsKey(newIndex))
                    {
                        addError(paramName, getText("admin.errors.invalid.position"));
                    }
                    else
                    {
                        optionPositions.put(newIndex, getOptions().getOptionById(Long.decode(fieldId)));
                    }
                }
                catch (NumberFormatException e)
                {
                    addError(paramName, getText("admin.errors.invalid.position"));
                }
            }
        }

        if (!invalidInput())
        {
            getOptions().moveOptionToPosition(optionPositions);
            // Mark fields for highlighting
            for (final Option option : optionPositions.values())
            {
                populateHlField(option);
            }

            return redirectToView();
        }

        return getResult();
    }

    private String redirectToView()
    {
        StringBuilder redirectUrl = new StringBuilder("EditCustomFieldOptions!default.jspa?fieldConfigId=").append(getFieldConfigId());

        if (getSelectedParentOptionId() != null)
        {
            redirectUrl.append("&selectedParentOptionId=" + getSelectedParentOptionId());
        }
        for (final Object hlField : hlFields)
        {
            redirectUrl.append("&currentOptions=").append(urlEncode((String) hlField));
        }

        return getRedirect(redirectUrl.toString());
    }

    private String getTextValueFromParams(String newPositionFieldName)
    {
        String[] newFieldPositionArray = (String[]) ActionContext.getParameters().get(newPositionFieldName);

        if (newFieldPositionArray != null && newFieldPositionArray.length > 0)
            return newFieldPositionArray[0];
        else
            return "";
    }

    @RequiresXsrfCheck
    public String doAdd() throws Exception
    {
        doValidation();
        if (!TextUtils.stringSet(addValue))
        {
            addError("addValue", getText("admin.errors.customfields.invalid.select.list.value"));
        }

        if (invalidInput())
            return getResult();

        Options options = getOptions();

        if (options.getOptionForValue(getAddValue(), getSelectedParentOptionId()) != null)
        {
            addError("addValue", getText("admin.errors.customfields.value.already.exists"));
            return Action.ERROR;
        }

        //set the options
        options.addOption(options.getOptionById(getSelectedParentOptionId()), getAddValue());
        if (!getDisplayOptions().isEmpty())
            hlFields.add(getAddValue());

        return redirectToView();

    }

    @RequiresXsrfCheck
    public String doSort() throws Exception
    {
        getOptions().sortOptionsByValue(getSelectedParentOption());

        return getRedirect(getRedirectUrl());
    }

    @RequiresXsrfCheck
    public String doMoveToFirst() throws Exception
    {
        populateHlField(getSelectedOption());
        getOptions().moveToStartSequence(getSelectedOption());

        return redirectToView();
    }

    @RequiresXsrfCheck
    public String doMoveUp() throws Exception
    {
        populateHlField(getSelectedOption());
        getOptions().decrementSequence(getSelectedOption());

        return redirectToView();
    }

    @RequiresXsrfCheck
    public String doMoveDown() throws Exception
    {
        populateHlField(getSelectedOption());
        getOptions().incrementSequence(getSelectedOption());

        return redirectToView();
    }

    @RequiresXsrfCheck
    public String doMoveToLast() throws Exception
    {
        populateHlField(getSelectedOption());
        getOptions().moveToLastSequence(getSelectedOption());

        return redirectToView();
    }

    private void populateHlField(Option option)
    {
        hlFields.add(option.getValue());
    }

    public String getNewPositionTextBoxName(int optionId)
    {
        return NEW_OPTION_POSITION_PREFIX + optionId;
    }

    public String getNewPositionValue(int optionId)
    {
        return getTextValueFromParams(getNewPositionTextBoxName(optionId));
    }

    @RequiresXsrfCheck
    public String doRemove() throws Exception
    {
        if (!confirm)
            return "confirmdelete";

        removeValuesFromIssues();
        getOptions().removeOption(getSelectedOption());


        return getRedirect(getRedirectUrl());
    }

    @RequiresXsrfCheck
    public String doDisable() throws Exception
    {
        getOptions().disableOption(getSelectedOption());

        return redirectToView();
    }

    @RequiresXsrfCheck
    public String doEnable() throws Exception
    {
        getOptions().enableOption(getSelectedOption());

        return redirectToView();
    }

    public String doEdit() throws Exception
    {
        setValue(getSelectedOption().getValue());
        return "edit";
    }

    @RequiresXsrfCheck
    public String doUpdate() throws Exception
    {
        if (!TextUtils.stringSet(value))
        {
            addError("value", getText("admin.errors.customfields.invalid.select.list.value"));
            return "edit";
        }

        Options options = getOptions();
        Option duplicateOption = options.getOptionForValue(value, getSelectedParentOptionId());
        if (duplicateOption != null && !getSelectedOption().getOptionId().equals(duplicateOption.getOptionId()))
        {
            addError("value", getText("admin.errors.customfields.value.already.exists"));
            return "edit";
        }

        getOptions().setValue(getSelectedOption(), value);

        return getRedirect(getRedirectUrl());
    }


    protected String doExecute() throws Exception
    {
        return INPUT;
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods

    private void removeValuesFromIssues()
    {
        Collection<Issue> issues = getAffectedIssues();
        for (final Issue issue : issues)
        {
            MultipleSettableCustomFieldType customFieldType = (MultipleSettableCustomFieldType) getCustomField().getCustomFieldType();
            customFieldType.removeValue(getCustomField(), issue, getSelectedOption());

        }
        reindexMessageManager.pushMessage(getLoggedInUser(), "admin.notifications.task.custom.fields");
    }

    public Collection<Issue> getAffectedIssues()
    {
        final MultipleSettableCustomFieldType customFieldType = (MultipleSettableCustomFieldType) getCustomField().getCustomFieldType();
        Collection ids = customFieldType.getIssueIdsWithValue(getCustomField(),
                                                              getSelectedOption());
        Collection<Issue> issues = new ArrayList<Issue>(ids.size());

        for (final Object id1 : ids)
        {
            Long id = (Long) id1;
            final Issue issue = issueManager.getIssueObject(id);
            final FieldConfig relevantConfigFromGv = getCustomField().getRelevantConfig(issue);
            if (getFieldConfig().equals(relevantConfigFromGv))
            {
                issues.add(issue);
            }
        }
        return issues;
    }

    // ------------------------------------------------------------------------------------------- Non-trivial accessors

    // ------------------------------------------------------------------------------------------ Private Helper Methods
    public Options getOptions()
    {
        if (options == null)
        {
            Long selectedParentOptionId = getSelectedParentOptionId();
            options = getCustomField().getOptions(selectedParentOptionId != null ? selectedParentOptionId.toString() : null, getFieldConfig(), null);
        }
        return options;
    }

    public Collection getDisplayOptions()
    {
        final Options options = getOptions();
        final Option parentOption = options.getOptionById(getSelectedParentOptionId());
        if (parentOption != null)
        {
            return parentOption.getChildOptions();
        }
        else
        {
            return options;
        }
    }

    public Option getSelectedOption()
    {
        return getOptions().getOptionById(OptionUtils.safeParseLong(getSelectedValue()));
    }

    public Option getSelectedParentOption()
    {
        return getOptions().getOptionById((getSelectedParentOptionId()));
    }

    public Object getDefaultValues()
    {
        if (defaultValues == null)
        {
            Object dbDefaultValues = getCustomField().getCustomFieldType().getDefaultValue(getFieldConfig());
            if (dbDefaultValues instanceof String)
            {
                final Collection tempCollection = new ArrayList(1);
                tempCollection.add(dbDefaultValues);
                defaultValues = tempCollection;
            }
            else
            {
                defaultValues = dbDefaultValues;
            }
        }

        return defaultValues;
    }

    public boolean isDefaultValue(String value)
    {
        Object defaults = getDefaultValues();

        if (defaults instanceof Collection)
        {
            Collection defCollection = (Collection) defaults;

            Option option = options.getOptionById(OptionUtils.safeParseLong(value));

            if (option != null)
            {
                return defCollection.contains(option.getValue());
            }
            else
            {
                return false;
            }
        }
        else if (defaults instanceof CustomFieldParams)
        {
            CustomFieldParams fieldParams = (CustomFieldParams) defaults;
            Collection allFieldValues = fieldParams.getValuesForNullKey();
            for (Object defaultOptionId : allFieldValues)
            {
                if (value != null && value.equals(defaultOptionId))
                {
                    return true;
                }
                else if (defaultOptionId instanceof Option)
                {
                    Option option = (Option) defaultOptionId;
                    if (option.getOptionId().toString().equals(value))
                    {
                        return true;
                    }
                }

            }
        }

        return false;
    }

    public int getButtonRowSize()
    {
        int rowSize = 2;
        if (getDisplayOptions().size() > 1)
            rowSize++;

        return rowSize;
    }

    public boolean isCascadingSelect()
    {
        return getCustomField().getCustomFieldType() instanceof CascadingSelectCFType;
    }

    private String getRedirectUrl()
    {
        if (getSelectedParentOptionId() == null)
        {
            return getBaseUrl();
        }
        else
        {
            return getUrlWithParent("default");
        }
    }

    private String getBaseUrl()
    {
        return getBaseUrl("default");
    }

    private String getBaseUrl(String action)
    {
        return "EditCustomFieldOptions!" + action + ".jspa?fieldConfigId=" + getFieldConfig().getId() +
                "&atl_token=" + urlEncode(getXsrfToken());
    }

    public String getSelectedParentOptionUrlPreifx()
    {
        return getBaseUrl() + "&selectedParentOptionId=";
    }

    public String getSelectedParentOptionUrlPrefix(String action)
    {
        return getBaseUrl(action) + "&selectedParentOptionId=";
    }

    public String getDoActionUrl(Option option, String action)
    {

        return getUrlWithParent(action) + "&selectedValue=" + (option != null ? option.getOptionId().toString() : "");
    }

    public String getUrlWithParent(String action)
    {
        if (getSelectedParentOptionId() == null)
        {
            return getBaseUrl(action);
        }
        else
        {
            return getBaseUrl(action) + "&selectedParentOptionId=" + getSelectedParentOptionId();

        }
    }

    // ---------------------------------------------------------------------- Accessors & Mutators for action properties
    public String getAddValue()
    {
        return addValue;
    }

    public void setAddValue(String addValue)
    {
        this.addValue = addValue;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getSelectedValue()
    {
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue)
    {
        this.selectedValue = selectedValue;
    }

    public Long getSelectedParentOptionId()
    {
        return selectedParentOptionId;
    }

    public void setSelectedParentOptionId(Long selectedParentOptionId)
    {
        this.selectedParentOptionId = selectedParentOptionId;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public Collection getHlOptions()
    {
        return hlFields;
    }

    public void setCurrentOptions(String[] currentFields)
    {
        this.hlFields = Arrays.asList(currentFields);
    }
}
