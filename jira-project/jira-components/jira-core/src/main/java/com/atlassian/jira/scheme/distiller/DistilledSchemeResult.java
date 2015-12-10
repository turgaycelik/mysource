package com.atlassian.jira.scheme.distiller;

import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the result of a 'distilling' opertion of the {@link SchemeDistiller}. This will contain the original
 * schemes that contained the same {@link com.atlassian.jira.scheme.SchemeEntity}'s as each other and it will
 * contain the new resulting 'distilled' scheme. This will also contain the type of scheme we are dealing with
 * (i.e. notification, permission, etc).
 */
public class DistilledSchemeResult
{
    private String type;
    private List originalSchemes;
    private Map schemeToProjects;
    private Scheme resultingScheme;
    private String resultingSchemeTempName;
    private boolean selected;


    public DistilledSchemeResult(String type, Collection originalSchemes, Map schemeToProjects, Scheme resultingScheme)
    {
        this.type = type;
        this.originalSchemes = new ArrayList(originalSchemes);
        Collections.sort(this.originalSchemes, new SchemeComparator());
        this.schemeToProjects = schemeToProjects;
        this.resultingScheme = resultingScheme;
        this.selected = false;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public String getResultingSchemeTempName()
    {
        return resultingSchemeTempName;
    }

    public void setResultingSchemeTempName(String resultingSchemeTempName)
    {
        this.resultingSchemeTempName = resultingSchemeTempName;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Collection getOriginalSchemes()
    {
        return originalSchemes;
    }

    public void setOriginalSchemes(Collection originalSchemes)
    {
        this.originalSchemes = new ArrayList(originalSchemes);
        Collections.sort(this.originalSchemes, new SchemeComparator());
    }

    public Collection getAssociatedProjectsForScheme(Scheme scheme)
    {
        Collection associatedProjects = (Collection) schemeToProjects.get(scheme);
        if (associatedProjects == null)
        {
            associatedProjects = Collections.EMPTY_LIST;
        }
        return associatedProjects;
    }

    /**
     * This will return all the unique projects that the original schemes were associated with.
     * @return unique set of projects
     */
    public Set getAllAssociatedProjects()
    {
        Set allProjects = new HashSet();
        for (final Object o : schemeToProjects.keySet())
        {
            Scheme scheme = (Scheme) o;
            allProjects.addAll((Collection) schemeToProjects.get(scheme));
        }
        return allProjects;
    }

    public Scheme getResultingScheme()
    {
        return resultingScheme;
    }

    public void setResultingScheme(Scheme resultingScheme)
    {
        this.resultingScheme = resultingScheme;
    }
}
