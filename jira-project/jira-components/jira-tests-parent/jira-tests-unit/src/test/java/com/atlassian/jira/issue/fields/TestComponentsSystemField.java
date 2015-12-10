package com.atlassian.jira.issue.fields;

import java.util.Collections;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestComponentsSystemField
{
    @Before
    public void setUp() throws Exception
    {
        UtilsForTestSetup.loadDatabaseDriver();
        UtilsForTestSetup.deleteAllEntities();
        final MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        componentAccessorWorker.registerMock(OfBizDelegator.class, OfBizFactory.getOfBizDelegator());
        ComponentAccessor.initialiseWorker(componentAccessorWorker);
    }

    @After
    public void tearDown() throws Exception
    {
        UtilsForTestSetup.deleteAllEntities();
    }

    @Test
    public void testComponentsSystemFieldCompareIdSets()
    {
        ComponentsSystemField componentsSystemField = new ComponentsSystemField(null, null, null, null, null, null, null, null, null, null);

        GenericValue component1 = UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(1), "description", ""));
        GenericValue component1Copy = new GenericValue(component1);
        GenericValue component1CopyNull = new GenericValue(component1); component1CopyNull.set("description", null);
        GenericValue component2 = UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(2), "description", "comp 2"));
        GenericValue component3 = UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(3), "description", "comp 3"));

        //check null cases
        assertTrue(componentsSystemField.compareIdSets(null, null));
        assertFalse(componentsSystemField.compareIdSets(null, EasyList.build(component1)));
        assertFalse(componentsSystemField.compareIdSets(EasyList.build(component1), null));

        //check null and empty cases
        assertFalse(componentsSystemField.compareIdSets(null, Collections.EMPTY_LIST));
        assertFalse(componentsSystemField.compareIdSets(Collections.EMPTY_LIST, null));

        //check empty cases
        assertTrue(componentsSystemField.compareIdSets(Collections.EMPTY_LIST, Collections.EMPTY_LIST));
        assertFalse(componentsSystemField.compareIdSets(Collections.EMPTY_LIST, EasyList.build(component1)));
        assertFalse(componentsSystemField.compareIdSets(EasyList.build(component1), Collections.EMPTY_LIST));

        //compare same single values
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component1), EasyList.build(component1)));
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component2), EasyList.build(component2)));

        //check same single value instances with/without different different attribute values
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component1), EasyList.build(component1Copy)));
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component1Copy), EasyList.build(component1CopyNull)));
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component1CopyNull), EasyList.build(component1)));

        //check different single values
        assertFalse(componentsSystemField.compareIdSets(EasyList.build(component1), EasyList.build(component2)));
        assertFalse(componentsSystemField.compareIdSets(EasyList.build(component2), EasyList.build(component1)));

        //check multiple values are same
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component1, component2), EasyList.build(component1, component2)));
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component1, component2, component3), EasyList.build(component1, component2, component3)));

        //check multiple values are same even with different ordering
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component1, component2), EasyList.build(component2, component1)));
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component1, component3, component2), EasyList.build(component2, component1, component3)));

        //check multiple values are same even with duplicate elements
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component1, component1Copy), EasyList.build(component1CopyNull, component1)));
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component1, component2), EasyList.build(component2, component1, component2)));
        assertTrue(componentsSystemField.compareIdSets(EasyList.build(component1, component3, component1, component3, component2), EasyList.build(component2, component1, component3)));

        //check multiple values are different (no common subset)
        assertFalse(componentsSystemField.compareIdSets(EasyList.build(component1), EasyList.build(component2, component3)));

        //check multiple values are different (with common subset)
        assertFalse(componentsSystemField.compareIdSets(EasyList.build(component1), EasyList.build(component1, component2, component3)));
        assertFalse(componentsSystemField.compareIdSets(EasyList.build(component1, component3), EasyList.build(component1, component2, component3)));
        assertFalse(componentsSystemField.compareIdSets(EasyList.build(component2, component1), EasyList.build(component3, component2)));
    }
}
