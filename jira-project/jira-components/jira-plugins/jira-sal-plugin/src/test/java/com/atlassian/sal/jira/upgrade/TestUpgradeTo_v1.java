package com.atlassian.sal.jira.upgrade;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.core.util.map.EasyMap;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class TestUpgradeTo_v1
{
    private UpgradeTo_v1 upgradeTo_v1;
    @Mock
    private JiraPropertySetFactory jiraPropertySetFactory;
    @Mock
    private OfBizDelegator ofBizDelegator;

    @Before
    public void setUp()
    {
        upgradeTo_v1 = new UpgradeTo_v1(jiraPropertySetFactory, ofBizDelegator);
    }

    @Test
    public void testUpgrade() throws Exception
    {
        addValueToList(1L, "#-#-#3");
        addEntryToList(1L, "thename", 2L, "thekey");
        PropertySet propertySet = mock(PropertySet.class);
        when(jiraPropertySetFactory.buildNoncachingPropertySet("thename", 2L)).thenReturn(propertySet);
        when(propertySet.getString("thekey#-#-#0")).thenReturn("part1");
        when(propertySet.getString("thekey#-#-#1")).thenReturn("part2");
        when(propertySet.getString("thekey#-#-#2")).thenReturn("part3");
        upgradeTo_v1.doUpgrade();
        // Verify the correct new value was put in
        verify(propertySet).setText("thekey", "part1part2part3");
        // Verify the old keys were deleted
        verify(propertySet).remove("thekey");
        verify(propertySet).remove("thekey#-#-#0");
        verify(propertySet).remove("thekey#-#-#1");
        verify(propertySet).remove("thekey#-#-#2");
    }

    @Test
    public void testUpgradeNoEntry() throws Exception
    {
        GenericValue property = addValueToList(1L, "#-#-#3");
        upgradeTo_v1.doUpgrade();
    }

    @Test
    public void testUpgradeMissingValue() throws Exception
    {
        addValueToList(1L, "#-#-#3");
        addEntryToList(1L, "thename", 2L, "thekey");
        PropertySet propertySet = mock(PropertySet.class);
        when(jiraPropertySetFactory.buildNoncachingPropertySet("thename", 2L)).thenReturn(propertySet);
        when(propertySet.getString("thekey#-#-#0")).thenReturn("part1");
        when(propertySet.getString("thekey#-#-#2")).thenReturn("part3");
        upgradeTo_v1.doUpgrade();
        // Verify the correct new value was put in
        verify(propertySet).setText("thekey", "part1part3");
        // Verify the old keys were deleted
        verify(propertySet).remove("thekey");
        verify(propertySet).remove("thekey#-#-#0");
        verify(propertySet).remove("thekey#-#-#2");
    }

    private GenericValue addEntryToList(long id, String entityName, long entityId, String key)
    {
        GenericValue gv = mock(GenericValue.class);
        when(gv.getString("entityName")).thenReturn(entityName);
        when(gv.getLong("entityId")).thenReturn(entityId);
        when(gv.getString("propertyKey")).thenReturn(key);
        when(ofBizDelegator.findByPrimaryKey("OSPropertyEntry", id)).thenReturn(gv);
        return gv;
    }

    private GenericValue addValueToList(long id, String value)
    {
        GenericValue gv = mock(GenericValue.class);
        when(gv.getLong("id")).thenReturn(id);
        when(gv.getString("value")).thenReturn(value);
        when(ofBizDelegator.findByLike("OSPropertyString", EasyMap.build("value", "#-#-#%"),
            Collections.EMPTY_LIST)).thenReturn(Arrays.asList(gv));
        return gv;
    }
}
