/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldScope;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.jelly.tag.JellyUtils;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.web.action.admin.customfields.CustomFieldValidator;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

public class CreateCustomField extends TagSupport
{
    private String customFieldIdVar;
    private String fieldType;
    private String fieldScope;
    private String fieldName;
    private String description;
    private String searcher;
    private String issueTypeId;

    private String issueType;
    private String projectKey;
    private Long project;

    private static final Logger log = Logger.getLogger(CreateCustomField.class);
    public static final String FIELD_TYPE_PREFIX = com.atlassian.jira.web.action.admin.customfields.CreateCustomField.FIELD_TYPE_PREFIX;
    private CustomField createdCustomField = null;
    private final OptionsManager optionsManager;
    private final CustomFieldManager customFieldManager;
    private final CustomFieldValidator customFieldValidator;
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final JiraContextTreeManager treeManager;
    private long numberAdded = 0;

    public CreateCustomField(OptionsManager optionsManager, 
                             CustomFieldManager customFieldManager, 
                             CustomFieldValidator customFieldValidator, 
                             ProjectManager projectManager,
                             ConstantsManager constantsManager,
                             JiraContextTreeManager treeManager)
    {
        this.optionsManager = optionsManager;
        this.customFieldManager = customFieldManager;
        this.customFieldValidator = customFieldValidator;
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.treeManager = treeManager;
    }

    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {
        validate();

        CustomFieldSearcher cfs;
        if (ObjectUtils.isValueSelected(searcher))
        {
            cfs = customFieldManager.getCustomFieldSearcher(getSearcher());
        }
        else
        {
            cfs = null;
        }
        try
        {
            final CustomFieldType customFieldType = customFieldManager.getCustomFieldType(getFieldType());

            if (CustomFieldScope.ISSUETYPE.equals(getFieldScope()))
            {
                createdCustomField = customFieldManager.createCustomField(getFieldName(), 
                                                                          getDescription(), 
                                                                          customFieldType, 
                                                                          cfs, 
                                                                          EasyList.build(GlobalIssueContext.getInstance()),
                                                                          EasyList.build(getIssueTypeGv()));
            }
            else if (CustomFieldScope.PROJECT.equals(getFieldScope()))
            {
                createdCustomField = customFieldManager.createCustomField(getFieldName(), 
                                                                          getDescription(), 
                                                                          customFieldType, 
                                                                          cfs, 
                                                                          EasyList.build(new ProjectContext(getProjectObject(), treeManager)),
                                                                          EasyList.buildNull());
            }
            else
            {
                createdCustomField = customFieldManager.createCustomField(getFieldName(), 
                                                                          getDescription(), 
                                                                          customFieldType, 
                                                                          cfs, 
                                                                          EasyList.build(GlobalIssueContext.getInstance()),
                                                                          EasyList.buildNull());
            }


            // If the user has specifed a variable place the customfield id in to that variable.
            if (StringUtils.isNotEmpty(getCustomFieldIdVar()))
            {
                // Do a query to retrieve the custom field with the name and sort by id so that the most recent one comes up.
                getContext().setVariable(getCustomFieldIdVar(), createdCustomField);
            }

            Script body = getBody();
            if (body != null)
            {
                body.run(getContext(), xmlOutput);
            }
        }
        catch (GenericEntityException e)
        {
            throw new JellyTagException(e);
        }

    }

    private void validate() throws JellyTagException
    {
        ErrorCollection errorCollection = customFieldValidator.validateType(getFieldType());
        ErrorCollection errors = customFieldValidator.validateDetails(getFieldName(), getFieldType(), getSearcher());
        errorCollection.addErrorCollection(errors);
        JellyUtils.processErrorCollection(errorCollection);
    }


    public Option addSelectValue(String value)
    {
        Option newOption = null;

        if (createdCustomField != null)
        {
            List schemes = createdCustomField.getConfigurationSchemes();
            if (schemes != null && !schemes.isEmpty())
            {
                FieldConfigScheme sc = (FieldConfigScheme) schemes.get(0);
                Map configs = sc.getConfigsByConfig();
                if (configs != null && !configs.isEmpty())
                {
                    FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                    newOption = optionsManager.createOption(config,
                                                            null,
                                                            new Long(numberAdded),
                                                            value);
                    numberAdded++;
                }
            }
        }

        return newOption;
    }


    public String getFieldType()
    {
        if (fieldType != null && fieldType.indexOf(':') > -1)
        {
            return fieldType;
        }

        return com.atlassian.jira.web.action.admin.customfields.CreateCustomField.FIELD_TYPE_PREFIX + fieldType;
    }

    public void setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
    }

    public String getFieldScope()
    {
        return fieldScope;
    }

    public void setFieldScope(String fieldScope)
    {
        this.fieldScope = fieldScope;
    }

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

    public String getSearcher()
    {
        if (searcher != null && searcher.indexOf(':') > -1)
        {
            return searcher;
        }

        return com.atlassian.jira.web.action.admin.customfields.CreateCustomField.FIELD_TYPE_PREFIX + searcher;
    }

    public void setSearcher(String searcher)
    {
        this.searcher = searcher;
    }

    public String getIssueTypeId()
    {
        if (issueTypeId == null && getIssueType() != null)
        {
            List<GenericValue> issueTypes = constantsManager.getAllIssueTypes();
            for (final GenericValue issueType : issueTypes)
            {
                if (getIssueType().equals(issueType.getString("name")))
                {
                    issueTypeId = issueType.getString("id");
                }
            }
        }

        return issueTypeId;
    }

    public Long getIssueTypeIdAsLong()
    {
        try
        {
            long issueTypeId = Long.parseLong(getIssueTypeId());
            return new Long(issueTypeId);
        }
        catch (RuntimeException e)
        {
            log.warn(e);
            return null;
        }
    }

    public void setIssueTypeId(String issueTypeId)
    {
        this.issueTypeId = issueTypeId;
    }

    public Long getProject()
    {
        if (project == null && getProjectKey() != null)
        {
            GenericValue projectGv = projectManager.getProjectByKey(getProjectKey());
            if (projectGv != null)
            {
                project = projectGv.getLong("id");
            }
        }

        return project;
    }

    public void setProject(Long project)
    {
        this.project = project;
    }

    public String getCustomFieldIdVar()
    {
        return customFieldIdVar;
    }

    public void setCustomFieldIdVar(String customFieldIdVar)
    {
        this.customFieldIdVar = customFieldIdVar;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public void setProjectKey(String projectKey)
    {
        this.projectKey = projectKey;
    }

    public String getIssueType()
    {
        return issueType;
    }

    public void setIssueType(String issueType)
    {
        this.issueType = issueType;
    }

    private Project getProjectObject()
    {
        return projectManager.getProjectObj(getProject());
    }

    private GenericValue getIssueTypeGv()
    {
        return constantsManager.getIssueType(getIssueTypeId());
    }
}
