package com.atlassian.jira.issue.fields.renderer;

import java.util.Collections;
import java.util.Set;

import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.issue.fields.AffectedVersionsSystemField;
import com.atlassian.jira.issue.fields.ComponentsSystemField;
import com.atlassian.jira.issue.fields.DescriptionSystemField;
import com.atlassian.jira.issue.fields.FixVersionsSystemField;
import com.atlassian.jira.issue.fields.MockCustomField;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.issue.fields.renderer.HackyRendererType.FROTHER_CONTROL;
import static com.atlassian.jira.issue.fields.renderer.HackyRendererType.SELECT_LIST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestDefaultHackyFieldRendererRegistry
{
    private FixVersionsSystemField fixVersionSystemField;
    private AffectedVersionsSystemField affectedVersionsSystemField;
    private ComponentsSystemField componentsSystemField;
    private DescriptionSystemField descriptionSystemField;
    private MockCustomField mockMultiVersionCustomField;
    private MockCustomField mockTextCustomField;
    private MockCustomField mockSingleVersionCustomField;
    private DefaultHackyFieldRendererRegistry registry;

    @Before
    public void setUp() throws Exception
    {
        registry = new DefaultHackyFieldRendererRegistry();

        fixVersionSystemField = new FixVersionsSystemField(null, null, null, null, null, null, null, null, null, null);
        affectedVersionsSystemField = new AffectedVersionsSystemField(null, null, null, null, null, null, null, null, null, null);
        componentsSystemField = new ComponentsSystemField(null, null, null, null, null, null, null, null, null, null);
        descriptionSystemField = new DescriptionSystemField(null, null, null, null, null, null, null, null);
        
        mockMultiVersionCustomField = new MockCustomField();
        mockMultiVersionCustomField.setCustomFieldType(new MockCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:multiversion", "Version"));

        mockTextCustomField = new MockCustomField();
        mockTextCustomField.setCustomFieldType(new MockCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:text", "Text"));

        mockSingleVersionCustomField = new MockCustomField();
        mockSingleVersionCustomField.setCustomFieldType(new MockCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:version", "Single version"));
    }
    
    @Test
    public void testShouldOverrideDefaultRenderers() throws Exception
    {
        assertTrue(registry.shouldOverrideDefaultRenderers(fixVersionSystemField));
        assertTrue(registry.shouldOverrideDefaultRenderers(affectedVersionsSystemField));
        assertTrue(registry.shouldOverrideDefaultRenderers(componentsSystemField));
        assertFalse(registry.shouldOverrideDefaultRenderers(descriptionSystemField));

        assertTrue(registry.shouldOverrideDefaultRenderers(mockMultiVersionCustomField));
        assertFalse(registry.shouldOverrideDefaultRenderers(mockTextCustomField));
        assertFalse(registry.shouldOverrideDefaultRenderers(mockSingleVersionCustomField));
    }

    @Test
    public void testGetRendererTypes() throws Exception
    {
        final Set<HackyRendererType> versionsAndComponentsRenderers = CollectionBuilder.newBuilder(SELECT_LIST, FROTHER_CONTROL).asImmutableListOrderedSet();
        final Set<HackyRendererType> emptySet = Collections.emptySet();

        assertEquals(versionsAndComponentsRenderers, registry.getRendererTypes(fixVersionSystemField));
        assertEquals(versionsAndComponentsRenderers, registry.getRendererTypes(affectedVersionsSystemField));
        assertEquals(versionsAndComponentsRenderers, registry.getRendererTypes(componentsSystemField));
        assertEquals(emptySet, registry.getRendererTypes(descriptionSystemField));

        assertEquals(versionsAndComponentsRenderers, registry.getRendererTypes(mockMultiVersionCustomField));
        assertEquals(emptySet, registry.getRendererTypes(mockTextCustomField));
        assertEquals(emptySet, registry.getRendererTypes(mockSingleVersionCustomField));
    }

    @Test
    public void testGetDefaultRendererType() throws Exception
    {
        assertEquals(HackyRendererType.FROTHER_CONTROL, registry.getDefaultRendererType(fixVersionSystemField));
        assertEquals(HackyRendererType.FROTHER_CONTROL, registry.getDefaultRendererType(affectedVersionsSystemField));
        assertEquals(HackyRendererType.FROTHER_CONTROL, registry.getDefaultRendererType(componentsSystemField));
        assertNull(registry.getDefaultRendererType(descriptionSystemField));

        assertEquals(HackyRendererType.FROTHER_CONTROL, registry.getDefaultRendererType(mockMultiVersionCustomField));
        assertNull(registry.getDefaultRendererType(mockTextCustomField));
        assertNull(registry.getDefaultRendererType(mockSingleVersionCustomField));
    }

}
