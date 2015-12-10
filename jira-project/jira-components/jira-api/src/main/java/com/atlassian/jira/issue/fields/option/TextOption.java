package com.atlassian.jira.issue.fields.option;

import com.atlassian.annotations.Internal;

@Internal
public class TextOption extends AbstractOption implements Option
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private String id;
    private String name;
    private String description;
    private String imagePath;
    private String cssClass;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public TextOption(String id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public TextOption(String id, String name, String cssClass)
    {
        this.cssClass = cssClass;
        this.name = name;
        this.id = id;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    public String getCssClass()
    {
        return cssClass;
    }

    public void setCssClass(String cssClass)
    {
        this.cssClass = cssClass;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
}
