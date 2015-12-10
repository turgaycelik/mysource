package com.atlassian.jira.scheme.distiller;

import java.util.Collections;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.scheme.AbstractSchemeTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 */
public class TestSchemeRelationships extends AbstractSchemeTest
{
    @Test
    public void testDifferencePercentageSomeDifference()
    {
        SchemeRelationships schemeRelationships = new SchemeRelationships(Collections.EMPTY_LIST, getSchemesForType("test scheme type"),
                EasyList.build(new SchemeEntityType(ENTITY_TYPE, "test type")));

        Assert.assertEquals("Percentage is 25%", new Double(0.25d),
                new Double(schemeRelationships.getSchemeDifferencePercentage()));
    }

    @Test
    public void testDifferencePercentageNoDifference()
    {
        // This is a bit of a hack
        getSchemesForType("test scheme type");
        SchemeRelationships schemeRelationships = new SchemeRelationships(Collections.EMPTY_LIST, EasyList.build(testScheme1, testScheme2),
                EasyList.build(new SchemeEntityType(ENTITY_TYPE, "test type")));

        Assert.assertEquals("Percentage is 0%", new Double(0.0d),
                new Double(schemeRelationships.getSchemeDifferencePercentage()));
    }

    @Test
    public void testDifferencePercentageAllDifferent()
    {
        getSchemesForType("test scheme type");
        SchemeRelationships schemeRelationships = new SchemeRelationships(Collections.EMPTY_LIST, EasyList.build(testScheme1, testScheme4),
                EasyList.build(new SchemeEntityType(ENTITY_TYPE, "test type")));

        Assert.assertEquals("Percentage is 100%", new Double(1.0d),
                new Double(schemeRelationships.getSchemeDifferencePercentage()));
    }


    @Test
    public void testDistilledSchemes()
    {
        getSchemesForType("test scheme type");
        SchemeRelationships schemeRelationships = new SchemeRelationships(
                EasyList.build(new DistilledSchemeResult("test scheme type",Collections.EMPTY_LIST,null, testScheme3)),
                EasyList.build(testScheme2, testScheme1),
                EasyList.build(new SchemeEntityType(ENTITY_TYPE, "test type")));

        Assert.assertEquals("Percentage is 25%", new Double(0.25d),
                new Double(schemeRelationships.getSchemeDifferencePercentage()));
    }


}
