package com.atlassian.jira.issue.fields.option;

import org.apache.commons.collections.Transformer;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;

public class ProjectOption extends AbstractOption implements Option
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final Transformer TRANSFORMER = new Transformer()
    {
        public Object transform(Object input)
        {
            return new ProjectOption((GenericValue) input);
        }
    };
    // ------------------------------------------------------------------------------------------------- Type Properties
    private GenericValue project;
    private List childOptions = Collections.EMPTY_LIST;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public ProjectOption(GenericValue project)
    {
        this.project = project;
    }

    public ProjectOption(GenericValue project, List childOptions)
    {
        this.project = project;
        if (childOptions != null)
        {
            this.childOptions = childOptions;
        }
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public String getId()
    {
        return project != null ? project.get("id").toString() : "";
    }

    public String getName()
    {
        return project.getString("name");
    }

    public String getDescription()
    {
        return project.getString("description");
    }

    public List getChildOptions()
    {
        return childOptions;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
}
