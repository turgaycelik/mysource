package com.atlassian.jira.issue.fields.option;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FieldConfigSchemeOption extends AbstractOption
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private FieldConfigScheme fieldConfigScheme;
    private List childOptions = Collections.EMPTY_LIST;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public FieldConfigSchemeOption(FieldConfigScheme fieldConfigScheme, Collection childOptions)
    {
        this.fieldConfigScheme = fieldConfigScheme;
        if (childOptions != null)
        {
            this.childOptions = EasyList.build(childOptions);
        }
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public String getId()
    {
        return fieldConfigScheme != null ? fieldConfigScheme.getId().toString() : null;
    }

    public Long getFieldConfigurationId()
    {
        if(fieldConfigScheme == null || fieldConfigScheme.getOneAndOnlyConfig() == null)
        {
            return null;
        }
        return fieldConfigScheme.getOneAndOnlyConfig().getId();
    }

    public String getName()
    {
        return fieldConfigScheme != null ? fieldConfigScheme.getName() : null;
    }

    public String getDescription()
    {
        return fieldConfigScheme != null ? fieldConfigScheme.getDescription() : null;
    }

    public List getChildOptions()
    {
        return childOptions;
    }

    public String getProjects()
    {
        if (fieldConfigScheme != null)
        {
            StringBuilder sb = new StringBuilder();
            if (!fieldConfigScheme.isGlobal())
            {
                final Iterator<Project> associatedProjects = fieldConfigScheme.getAssociatedProjectObjects().iterator();
                while(associatedProjects.hasNext())
                {
                    Project project = associatedProjects.next();
                    sb.append(project.getName());
                    if (associatedProjects.hasNext())
                    {
                        // JRA-26061 Truncate the optgroup label to prevent problems in FF7.
                        if (sb.length() > 40)
                        {
                            sb.append("...");
                            break;
                        }
                        else
                        {
                            sb.append(", ");
                        }
                    }
                }
            }
            else
            {
                sb.append("Default scheme (unlisted projects)");
            }
            
            return sb.toString();
        }
        else
        {
            return "";
        }
    }
    // -------------------------------------------------------------------------------------------------- Helper Methods
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final FieldConfigSchemeOption that = (FieldConfigSchemeOption) o;

        if (fieldConfigScheme != null ? !fieldConfigScheme.equals(that.fieldConfigScheme) : that.fieldConfigScheme != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (fieldConfigScheme != null ? fieldConfigScheme.hashCode() : 491);
    }
}
