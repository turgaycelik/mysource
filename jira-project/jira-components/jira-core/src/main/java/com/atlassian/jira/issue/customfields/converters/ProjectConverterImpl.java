package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class ProjectConverterImpl implements ProjectConverter
{
    private static final Logger LOGGER = Logger.getLogger(ProjectConverter.class);

    private final ProjectManager projectManager;

    public ProjectConverterImpl(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public String getString(final Project project)
    {
        if (project == null)
            return "-1";

        return project.getId().toString();
    }

    public String getString(GenericValue project)
    {
        if (project == null)
            return "-1";

        return project.getLong("id").toString();
    }

    public Project getProjectObject(final String stringValue) throws FieldValidationException
    {
        if (StringUtils.isBlank(stringValue) || "-1".equals(stringValue))
        {
            return null;
        }

        try
        {
            Long id = Long.valueOf(stringValue);
            return getProjectObject(id);
        }
        catch (NumberFormatException e)
        {
            LOGGER.error(e, e);
            throw new FieldValidationException("Project Id '" + stringValue + "' is not a number.");
        }
    }

    public Project getProjectObject(final Long projectId) throws FieldValidationException
    {
        if (projectId == null)
        {
            return null;
        }
        try
        {
            return projectManager.getProjectObj(projectId);
        }
        catch (Exception e)
        {
            LOGGER.error(e, e);
            throw new FieldValidationException("Invalid Project Id '" + projectId + "'");
        }
    }

    public GenericValue getProject(String stringValue) throws FieldValidationException
    {
        if (!TextUtils.stringSet(stringValue) || "-1".equals(stringValue))
            return null;

        try
        {
            Long id = Long.valueOf(stringValue);
            return getProject(id);
        }
        catch (NumberFormatException e)
        {
            LOGGER.error(e, e);
            throw new FieldValidationException("Project Id is not a number '" + stringValue + "'");
        }
    }

    //extra 'helper' method
    public GenericValue getProject(Long projectId) throws FieldValidationException
    {
        try
        {
            return projectManager.getProject(projectId);
        }
        catch (Exception e)
        {
            LOGGER.error(e, e);
            throw new FieldValidationException("Invalid Project Id '" + projectId + "'");
        }
    }
}
