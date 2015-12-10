package com.atlassian.jira.scheme;

import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * This will create a Scheme
 */
public interface SchemeFactory
{
    /**
     * Will take a {@link GenericValue} that represents a {@link Scheme}
     * and return the actual Scheme object and it's associated entities.
     * @param schemeGV
     * @return the scheme represented by the GenericValue
     */
    public Scheme getScheme(GenericValue schemeGV);

    public List<Scheme> getSchemes(List<GenericValue> schemeGVs);

    /**
     * Will take a {@link GenericValue} that represents a {@link Scheme}
     * and return the actual Scheme object and it's associated entities, NOTE: the
     * entities returned from this method will not have the id field populated
     * this allows you to test the equality of entities ignoring the fact that
     * they are actually representing different records in the persistent store.
     * @param schemeGV
     * @return the scheme represented by the GenericValue
     */
    public Scheme getSchemeWithEntitiesComparable(GenericValue schemeGV);

    public List<Scheme> getSchemesWithEntitiesComparable(List<GenericValue> schemeGVs);

    /**
     * This will take a {@link GenericValue} that represents a {@link Scheme}
     * and a {@link Collection} of GenericValue's representing a {@link SchemeEntity}
     * and return the actual Scheme object and it's associated entities as objects.
     * @param schemeGV
     * @return the scheme represented by the GenericValue
     */
    public Scheme getScheme(GenericValue schemeGV, Collection<GenericValue> schemeEntityGVs);
}
