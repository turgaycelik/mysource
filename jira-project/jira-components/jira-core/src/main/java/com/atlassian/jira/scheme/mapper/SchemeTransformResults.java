package com.atlassian.jira.scheme.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Holds and categorizes many {@link SchemeTransformResult}'s.
 */
public class SchemeTransformResults
{
    private List associatedTransformedResults;
    private List unassociatedTransformedResults;
    private List unassociatedUntransformedResults;
    private List associatedUntransformedResults;
    private int associatedProjectsCount;

    public SchemeTransformResults()
    {
        associatedTransformedResults = new ArrayList();
        unassociatedTransformedResults = new ArrayList();
        unassociatedUntransformedResults = new ArrayList();
        associatedUntransformedResults = new ArrayList();
        associatedProjectsCount = 0;
    }

    public Collection getAll()
    {
        Collection allResults = new ArrayList();
        allResults.addAll(associatedTransformedResults);
        allResults.addAll(associatedUntransformedResults);
        allResults.addAll(unassociatedTransformedResults);
        allResults.addAll(unassociatedUntransformedResults);

        return allResults;
    }

    public Collection getAllSchemeTransformResults()
    {
        Collection transformedResults = new ArrayList();
        if (associatedTransformedResults != null)
        {
            transformedResults.addAll(associatedTransformedResults);
        }
        if (unassociatedTransformedResults != null)
        {
            transformedResults.addAll(unassociatedTransformedResults);
        }
        return transformedResults;
    }   

    public void addResult(SchemeTransformResult schemeTransformResult)
    {
        boolean transformed = schemeTransformResult.originalSchemeTransformed();
        boolean associated = !schemeTransformResult.getAssociatedProjects().isEmpty();
        if (transformed && associated)
        {
            associatedTransformedResults.add(schemeTransformResult);
            associatedProjectsCount += schemeTransformResult.getAssociatedProjects().size();
        }
        else if (transformed && !associated)
        {
            unassociatedTransformedResults.add(schemeTransformResult);
        }
        else if (!transformed && associated)
        {
            associatedUntransformedResults.add(schemeTransformResult);
        }
        else if (!transformed && !associated)
        {
            unassociatedUntransformedResults.add(schemeTransformResult);
        } 
    }

    public List getUnassociatedTransformedResults()
    {
        return unassociatedTransformedResults;
    }

    public List getAssociatedTransformedResults()
    {
        return associatedTransformedResults;
    }

    public List getUnassociatedUntransformedResults()
    {
        return unassociatedUntransformedResults;
    }

    public List getAssociatedUntransformedResults()
    {
        return associatedUntransformedResults;
    }

    public int getAssociatedProjectsCount()
    {
        return associatedProjectsCount;
    }
}
