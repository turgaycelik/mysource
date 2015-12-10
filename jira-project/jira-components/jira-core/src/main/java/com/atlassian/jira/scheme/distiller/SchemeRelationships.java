package com.atlassian.jira.scheme.distiller;

import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeComparator;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This object represents a table of results which are broken-down by scheme and entityType (ie 'Browse Project'
 * permission, or 'Issue Created' notification).
 */
public class SchemeRelationships
{
    private Map relationshipByEntityType;
    private Collection distilledSchemes = null;
    private List schemes = null;
    private Collection distilledResults;

    public SchemeRelationships(Collection distilledResults, Collection schemes, Collection entityTypes)
    {
        this.distilledResults = distilledResults;
        this.distilledSchemes = new ArrayList();
        for (final Object distilledResult : distilledResults)
        {
            DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) distilledResult;
            distilledSchemes.add(distilledSchemeResult.getResultingScheme());
        }
        this.schemes = new ArrayList(distilledSchemes);
        this.schemes.addAll(schemes);
        // Sort the schemes
        Collections.sort(this.schemes, new SchemeComparator());
        this.relationshipByEntityType = new ListOrderedMap();

        createSchemeRelationshipsForEntityTypes(entityTypes, this.schemes);
    }

    public DistilledSchemeResult getDistilledSchemeResultForScheme(Scheme resultingScheme)
    {
        for (final Object distilledResult : distilledResults)
        {
            DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) distilledResult;
            if (distilledSchemeResult.getResultingScheme().equals(resultingScheme))
            {
                return distilledSchemeResult;
            }
        }
        return null;
    }

    public Collection getEntityTypes()
    {
        return relationshipByEntityType.keySet();
    }

    public Collection getSchemes()
    {
        return schemes;
    }

    public Collection getDistilledSchemes()
    {
        return distilledSchemes;
    }

    public boolean isSchemeDistilled(Scheme scheme)
    {
        return distilledSchemes.contains(scheme);
    }

    public boolean allMatchForEntityType(Object entityType)
    {
        return getSchemeRelationshipForEntityType(entityType).allMatch();
    }

    public List getSchemeRelationships()
    {
        return new ArrayList(relationshipByEntityType.values());
    }

    public SchemeRelationship getSchemeRelationshipForEntityType(Object entityType)
    {
        SchemeRelationship schemeRelationship = (SchemeRelationship) relationshipByEntityType.get(entityType);
        if (schemeRelationship != null)
        {
            return schemeRelationship;
        }
        throw new IllegalArgumentException("There are no scheme relationships registered for entityType: " + entityType);
    }

    public double getSchemeDifferencePercentage()
    {
        double matchingSchemeEntitiesCount = (double) getAllMatchingSchemeEntities().size();
        double totalSchemeEntitiesCount = (double) getAllSchemeEntities().size();
        if (totalSchemeEntitiesCount == 0)
        {
            return 0d;
        }
        return (1d - matchingSchemeEntitiesCount / totalSchemeEntitiesCount);
    }

    private void createSchemeRelationshipsForEntityTypes(Collection entityTypes, Collection schemes)
    {
        // Run through all the entityTypes and create a SchemeRelation for each one.
        for (final Object entityType : entityTypes)
        {
            SchemeEntityType schemeEntityType = (SchemeEntityType) entityType;

            this.relationshipByEntityType.put(schemeEntityType.getEntityTypeId(),
                    new SchemeRelationship(schemeEntityType.getEntityTypeId(), schemeEntityType.getDisplayName(), schemes));
        }
    }

    private Set getAllSchemeEntities()
    {
        Set ret = new HashSet();
        for (Object key : relationshipByEntityType.keySet())
        {
            SchemeRelationship schemeRelationship = (SchemeRelationship) relationshipByEntityType.get(key);
            Set allSchemeEntities = schemeRelationship.getAllSchemeEntities();
            if (allSchemeEntities != null)
            {
                ret.addAll(allSchemeEntities);
            }
        }
        return ret;
    }

    private Set getAllMatchingSchemeEntities()
    {
        Set ret = new HashSet();
        for (Object key : relationshipByEntityType.keySet())
        {
            SchemeRelationship schemeRelationship = (SchemeRelationship) relationshipByEntityType.get(key);
            Collection matchingSchemeEntities = schemeRelationship.getMatchingSchemeEntities();
            if (matchingSchemeEntities != null)
            {
                ret.addAll(matchingSchemeEntities);
            }
        }
        return ret;
    }

}
