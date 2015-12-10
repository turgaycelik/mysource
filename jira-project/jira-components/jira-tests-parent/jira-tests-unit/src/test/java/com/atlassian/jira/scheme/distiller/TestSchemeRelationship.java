package com.atlassian.jira.scheme.distiller;

import java.util.Collection;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.scheme.AbstractSchemeTest;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 
 */
public class TestSchemeRelationship extends AbstractSchemeTest
{
    private SchemeRelationship schemeRelationship;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        List schemes = getSchemesForType("testtype");

        schemeRelationship = new SchemeRelationship(ENTITY_TYPE, "test entity type", schemes);
    }

    @Test
    public void testGetMatchingSchemeEntities()
    {
        Collection matchingSchemeEntities = schemeRelationship.getMatchingSchemeEntities();
        assertEquals("matching schemes entities are correct size", 3, matchingSchemeEntities.size());
        assertTrue("matching schemes contains entity1", matchingSchemeEntities.contains(entity1));
        assertTrue("matching schemes contains entity2", matchingSchemeEntities.contains(entity2));
        assertTrue("matching schemes contains entity3", matchingSchemeEntities.contains(entity3));
        assertFalse("matching schemes contains entity4", matchingSchemeEntities.contains(entity4));
    }

    @Test
    public void testGetNonMatchingSchemeEntities()
    {
        assertFalse("All scheme entities do not match", schemeRelationship.allMatch());

        Collection nonMatchingSchemeEntities = schemeRelationship.getNonMatchingSchemeEntities(testScheme1);
        assertEquals("non matching schemes entities for scheme1 are correct size", 0, nonMatchingSchemeEntities.size());

        nonMatchingSchemeEntities = schemeRelationship.getNonMatchingSchemeEntities(testScheme2);
        assertEquals("non matching schemes entities for scheme2 are correct size", 0, nonMatchingSchemeEntities.size());

        nonMatchingSchemeEntities = schemeRelationship.getNonMatchingSchemeEntities(testScheme3);
        assertEquals("non matching schemes entities for scheme3 are correct size", 1, nonMatchingSchemeEntities.size());
        assertTrue("non matching schemes contains entity4", nonMatchingSchemeEntities.contains(entity4));
    }

    @Test
    public void testAllSchemeEntitiesMatch()
    {
        // Test the negative case
        assertFalse("All scheme entities do not match", schemeRelationship.allMatch());

        // Test the positive case
        schemeRelationship = new SchemeRelationship(ENTITY_TYPE, "testentities", EasyList.build(testScheme1, testScheme2));
        assertTrue("All scheme entities match", schemeRelationship.allMatch());
    }
}
