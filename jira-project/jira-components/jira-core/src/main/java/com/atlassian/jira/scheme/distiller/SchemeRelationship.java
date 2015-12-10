package com.atlassian.jira.scheme.distiller;

import com.atlassian.jira.scheme.Scheme;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is used by the {@link SchemeRelationships} class to hold an individual relationship by entityType
 * (ie 'Browse Project' permission, or 'Issue Created' notification).
 */
public class SchemeRelationship
{
    private Object entityType;
    private String entityTypeDisplayName;
    private Map schemeEntitiesByScheme;
    private boolean allMatch;
    private Collection intersectingEntities;
    private Map nonIntersectingEntities;

    public SchemeRelationship(Object entityType, String entityTypeDisplayName, Collection schemes)
    {
        this.entityType = entityType;
        this.entityTypeDisplayName = entityTypeDisplayName;
        this.allMatch = doAllSchemesMatchForEntityTypeId(schemes, entityType);
        this.nonIntersectingEntities = new HashMap();
        this.schemeEntitiesByScheme = initEntitiesByScheme(schemes, entityType);
    }

    public boolean allMatch()
    {
        return allMatch;
    }

    public List getSchemeEntities(Scheme scheme)
    {
        List entities = (List) schemeEntitiesByScheme.get(scheme);
        Collections.sort(entities);
        if (entities == null)
        {
            entities = Collections.EMPTY_LIST;
        }
        return entities;
    }

    /**
     * Returns an intersection of matching scheme-entities accross all
     * schemes being compared in this relationship
     * @return a collection of the matching @link SchemeEntity objects
     */
    public Collection getMatchingSchemeEntities()
    {
        if(intersectingEntities == null)
        {
            for (final Object o : schemeEntitiesByScheme.keySet())
            {
                Scheme key = (Scheme) o;
                List schemeEntities = (List) schemeEntitiesByScheme.get(key);
                //must be the first list of entities
                if (intersectingEntities == null)
                {
                    intersectingEntities = schemeEntities;
                }
                else
                {
                    intersectingEntities = CollectionUtils.intersection(schemeEntities, intersectingEntities);
                }
            }
        }

        return intersectingEntities;
    }


    public Collection getNonMatchingSchemeEntities(Scheme scheme)
    {
        Collection result = (Collection) nonIntersectingEntities.get(scheme);
        if(result == null)
        {
            List schemeEntities = (List) schemeEntitiesByScheme.get(scheme);
            result = new ArrayList(schemeEntities);
            result.removeAll(getMatchingSchemeEntities());
            nonIntersectingEntities.put(scheme, result);
        }

        return result;
    }

    public Map getSchemeEntitiesByScheme()
    {
        return schemeEntitiesByScheme;
    }

    public String getEntityTypeDisplayName()
    {
        return entityTypeDisplayName;
    }

    public Object getEntityType()
    {
        return entityType;
    }

    public Set getAllSchemeEntities()
    {
        Set ret = new HashSet();
        for (final Object o : schemeEntitiesByScheme.keySet())
        {
            Scheme scheme = (Scheme) o;
            ret.addAll((List) schemeEntitiesByScheme.get(scheme));
        }
        return ret;
    }

    private boolean doAllSchemesMatchForEntityTypeId(Collection schemes, Object entityTypeId)
    {
        Set sameEntities = new HashSet();
        for (final Object scheme1 : schemes)
        {
            Scheme scheme = (Scheme) scheme1;
            Collection entitiesByType = scheme.getEntitiesByType(entityTypeId);
            sameEntities.add(new HashSet(entitiesByType));
            if (sameEntities.size() > 1)
            {
                return false;
            }
        }
        return true;
    }

    private Map initEntitiesByScheme(Collection schemes, Object entityType)
    {
        // Create a map of scheme to entities
        Map entitiesByScheme = new HashMap();
        for (final Object scheme1 : schemes)
        {
            Scheme scheme = (Scheme) scheme1;
            entitiesByScheme.put(scheme, scheme.getEntitiesByType(entityType));
        }
        return entitiesByScheme;
    }    
}
