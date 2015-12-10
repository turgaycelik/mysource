package com.atlassian.jira.issue.managers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldDescription;
import com.atlassian.jira.issue.fields.CustomFieldFactory;
import com.atlassian.jira.issue.fields.CustomFieldScopeFactory;
import com.atlassian.jira.issue.fields.CustomFieldTestImpl;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigPersister;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.index.managers.FieldIndexerManager;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.searchers.MockCustomFieldSearcher;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptors;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptors;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestCustomFieldManager
{
    private DefaultCustomFieldManager customFieldManager;

    // Level 1 Dependencies
    @Mock
    private PluginAccessor mockPluginAccessor;
    @Mock
    private OfBizDelegator mockDelegator;
    @Mock
    private FieldScreenManager mockScreenManager;
    @Mock
    private EventPublisher mockEventPublisher;
    @Mock
    private FieldConfigSchemeManager mockConfigSchemeManager;
    @Mock
    private NotificationSchemeManager mockNotificationSchemeManager;
    @Mock
    private FieldManager mockFieldManager;
    @Mock
    private CustomFieldValuePersister mockFieldValuePersister;
    @Mock
    private FeatureManager mockFeatureManager;
    @Mock
    private JiraAuthenticationContext mockJiraAuthenticationContext;
    @Mock
    private CustomFieldFactory customFieldFactory;
    @Mock
    private CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors;
    @Mock
    private CustomFieldSearcherModuleDescriptors customFieldSearcherModuleDescriptors;

    // Level 2 Dependencies
    @Mock
    private CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor;
    @Mock
    private CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor;
    @Mock
    private CustomFieldSearcher customFieldSearcher;
    @Mock
    private FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;
    @Mock
    private List<Plugin> plugins;
    @Mock
    private GenericValue mockGenericValue;
    @Mock
    private CustomFieldType customFieldType;
    @Mock
    private CustomFieldDescription customFieldDescription;
    @Mock
    private IssueSearcherManager issueSearchManager;
    @Mock
    private FieldIndexerManager fieldIndexerManager;
    @Mock
    private TranslationManager translationManager;

    @Before
    public void setUp() throws Exception
    {
        stub(mockPluginAccessor.getPlugins()).toReturn(plugins);
        stub(plugins.isEmpty()).toReturn(false);
        customFieldManager = new DefaultCustomFieldManager(mockPluginAccessor, mockDelegator, mockConfigSchemeManager, null, null, null, mockScreenManager, mockFieldValuePersister, mockNotificationSchemeManager, mockFieldManager, mockEventPublisher, new MemoryCacheManager(), customFieldFactory, customFieldTypeModuleDescriptors, customFieldSearcherModuleDescriptors);
        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(EventPublisher.class, mock(EventPublisher.class))
                .addMock(IssueSearcherManager.class, issueSearchManager)
                .addMock(FieldIndexerManager.class, fieldIndexerManager)
        );
    }

    @Test
    public void testGetCustomFieldTypeDelegatesOnCustomFieldTypeModuleDescriptors()
    {
        String key = "aPluginKey";
        CustomFieldType expectedCustomFieldType = mock(CustomFieldType.class);
        when(customFieldTypeModuleDescriptors.getCustomFieldType(key)).thenReturn(Option.some(expectedCustomFieldType));

        CustomFieldType actualCustomFieldType = customFieldManager.getCustomFieldType(key);

        assertThat(actualCustomFieldType, is(expectedCustomFieldType));
    }

    @Test
    public void testGetCustomFieldTypesDelegatesOnCustomFieldTypeModuleDescriptors()
    {
        List<CustomFieldType<?, ?>> expectedCustomFieldTypes = Collections.emptyList();
        when(customFieldTypeModuleDescriptors.getCustomFieldTypes()).thenReturn(expectedCustomFieldTypes);

        List<CustomFieldType<?, ?>> actualCustomFieldType = customFieldManager.getCustomFieldTypes();

        assertThat(actualCustomFieldType, is(expectedCustomFieldTypes));
    }

    @Test
    public void testRemoveCustomFieldPossiblyLeavingOrphanedDataWithNullId() throws Exception
    {
        try
        {
            customFieldManager.removeCustomFieldPossiblyLeavingOrphanedData(null);
            fail("Expected exception to be thrown");
        }
        catch (IllegalArgumentException e)
        {
            // yay
        }
    }

    @Test
    public void testRemoveCustomFieldPossiblyLeavingOrphanedDataWithBadId() throws Exception
    {
        stub(mockDelegator.findById("CustomField", 999L)).toReturn(null);

        try
        {
            customFieldManager.removeCustomFieldPossiblyLeavingOrphanedData(999L);
            fail("Expected exception to be thrown");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Tried to remove custom field with id '999' that doesn't exist!", e.getMessage());
        }
    }

    @Test
    public void testRemoveCustomFieldPossiblyLeavingOrphanedDataWithExistingCustomField() throws Exception
    {

        final long fieldId = 999L;
        final String fieldSId = "customfield_" + fieldId;
        final String fieldName = "name";
        CustomField mockCustomField = mock(CustomField.class);
        List<GenericValue> genericValues = Collections.singletonList(mockGenericValue);

        stub(mockGenericValue.getLong("id")).toReturn(999L);
        stub(mockGenericValue.getString("name")).toReturn(fieldName);
        stub(mockGenericValue.getString("customfieldtypekey")).toReturn("CustomFieldType");

        doReturn(customFieldTypeModuleDescriptor).when(mockPluginAccessor).getEnabledPluginModule("CustomFieldType");

        stub(customFieldTypeModuleDescriptor.getModule()).toReturn(customFieldType);

        stub(mockDelegator.findAll("CustomField")).toReturn(genericValues);

        stub(mockDelegator.removeByAnd("ColumnLayoutItem", EasyMap.build("fieldidentifier", fieldSId))).toReturn(1);
        stub(mockDelegator.removeByAnd("FieldLayoutItem", EasyMap.build("fieldidentifier", fieldSId))).toReturn(1);

        stub(mockCustomField.getId()).toReturn(fieldSId);
        stub(mockCustomField.getName()).toReturn(fieldName);
        stub(mockCustomField.getIdAsLong()).toReturn(fieldId);
        stub(mockCustomField.remove()).toReturn(new HashSet<Long>(Lists.newArrayList(10001L)));

        DefaultCustomFieldManager customFieldManagerSpy = spy(customFieldManager);
        doReturn(mockCustomField).when(customFieldManagerSpy).getCustomFieldObject(fieldId);


        customFieldManagerSpy.removeCustomFieldPossiblyLeavingOrphanedData(fieldId);
        assertTrue(customFieldManager.getCustomFieldObjects().isEmpty());
    }

    @Test
    public void testRemoveCustomFieldPossiblyLeavingOrphanedDataWithExistingCustomFieldFromdb() throws Exception
    {
        final long fieldId = 999L;
        final String fieldSId = "customfield_" + fieldId;

        stub(mockDelegator.findById("CustomField", 999L)).toReturn(mockGenericValue);

        stub(mockDelegator.removeByAnd("ColumnLayoutItem", EasyMap.build("fieldidentifier", fieldSId))).toReturn(1);
        stub(mockDelegator.removeByAnd("FieldLayoutItem", EasyMap.build("fieldidentifier", fieldSId))).toReturn(1);

        final CustomFieldValuePersister customFieldValuePersister = mock(CustomFieldValuePersister.class);
        stub(customFieldValuePersister.removeAllValues(fieldSId)).toReturn(new HashSet<Long>(Lists.newArrayList(10001L)));

        when(customFieldFactory.create(any(GenericValue.class))).thenReturn(mock(CustomField.class));

        customFieldManager.removeCustomFieldPossiblyLeavingOrphanedData(fieldId);
        assertTrue(customFieldManager.getCustomFieldObjects().isEmpty());
    }

    @Test
    public void testUpdateCustomFieldObject() throws Exception
    {
        final String oldName = "oldName";
        final String newName = "newName";
        doReturn(customFieldTypeModuleDescriptor).when(mockPluginAccessor).getEnabledPluginModule("key");

        Map<String, Object> createFields = createCustomFieldMap(oldName, "key", "description");
        final GenericValue genericValue = createGv(CustomField.ENTITY_TABLE_NAME, createFields);
        when(mockDelegator.createValue(eq(CustomField.ENTITY_TABLE_NAME), anyMap())).thenReturn(genericValue);
        when(mockDelegator.findById(eq(CustomField.ENTITY_TABLE_NAME), anyLong())).thenReturn(genericValue);

        CustomField oldCustomField = mock(CustomField.class);
        when(oldCustomField.getId()).thenReturn(String.valueOf(genericValue.getLong(CustomField.ENTITY_ID)));
        when(oldCustomField.getCustomFieldType()).thenReturn(mock(CustomFieldType.class));
        when(oldCustomField.getGenericValue()).thenReturn(genericValue);
        when(oldCustomField.getName()).thenReturn(oldName);
        when(customFieldFactory.create(genericValue)).thenReturn(oldCustomField);
        when(customFieldFactory.copyOf(oldCustomField)).thenReturn(oldCustomField);

        CustomFieldTestImpl cf = copyOf(customFieldManager.createCustomField(oldName, "description", customFieldType, null, null, null));
        final Long fieldId = cf.getIdAsLong();

        CustomFieldTestImpl updatedCustomField =  mock(CustomFieldTestImpl.class);
        when(updatedCustomField.getIdAsLong()).thenReturn(fieldId);
        when(updatedCustomField.getName()).thenReturn(newName);
        when(updatedCustomField.getUntranslatedName()).thenReturn(newName);
        GenericValue gv = spy(new GenericValue(cf.copyGenericValue()));

        when(updatedCustomField.copyGenericValue()).thenReturn(gv);
        when(updatedCustomField.getGenericValue()).thenReturn(gv);
        when(updatedCustomField.getCustomFieldType()).thenReturn(customFieldType);
        when(mockDelegator.findById(eq("CustomField"), anyLong())).thenReturn(gv);
        when(customFieldFactory.create(gv)).thenReturn(updatedCustomField);
        when(customFieldFactory.copyOf(updatedCustomField)).thenReturn(updatedCustomField);

        doNothing().when(gv).store();
        gv.setString("name", newName);
        customFieldManager.updateCustomField(updatedCustomField);

        assertEquals(newName, customFieldManager.getCustomFieldObject(fieldId).getUntranslatedName());
    }

    private CustomFieldTestImpl copyOf(final CustomField customField)
    {
        return new CustomFieldTestImpl(
                customField,
                mockJiraAuthenticationContext,
                mockConfigSchemeManager,
                mock(PermissionManager.class),
                mock(RendererManager.class),
                fieldConfigSchemeClauseContextUtil,
                customFieldDescription,
                mockFeatureManager,
                translationManager,
                mock(CustomFieldScopeFactory.class),
                mock(CustomFieldTypeModuleDescriptors.class),
                mock(CustomFieldSearcherModuleDescriptors.class)
        );
    }

    private Map<String, Object> createCustomFieldMap (String fieldName, String key, String description)
    {
        Map<String, Object> createFields = new HashMap<String, Object>(4);
        if (StringUtils.isNotEmpty(fieldName))
        {
            createFields.put("name", StringUtils.abbreviate(fieldName, FieldConfigPersister.ENTITY_LONG_TEXT_LENGTH));
        }
        if (StringUtils.isNotEmpty(key))
        {
            createFields.put("customfieldtypekey", key);
        }
        if (StringUtils.isNotEmpty(description))
        {
            createFields.put("description", description);
        }
        return createFields;
    }

    private GenericValue createGv(String fieldName, Map<String, Object> params)
    {
        if (!params.containsKey("id"))
        {
            params.put("id", 1L);
        }
        return CoreFactory.getGenericDelegator().makeValue("CustomField", params);
    }

    @Test
    public void testGetCustomFieldSearcherDelegatesOnCustomFieldSearcherModuleDescriptors()
    {
        String key = "aPluginKey";
        CustomFieldSearcher expectedSearcher = mock(CustomFieldSearcher.class);
        when(customFieldSearcherModuleDescriptors.getCustomFieldSearcher(key)).thenReturn(Option.some(expectedSearcher));

        CustomFieldSearcher actualSearcher = customFieldManager.getCustomFieldSearcher(key);

        assertThat(actualSearcher, is(expectedSearcher));
    }

    @Test
    public void testGetCustomFieldSearcherClass()
    {
        // Key is "None"
        assertNull(customFieldManager.getCustomFieldSearcherClass("-1"));

        // Wrong type
        doReturn(customFieldTypeModuleDescriptor).when(mockPluginAccessor).getEnabledPluginModule("key");
        assertNull(customFieldManager.getCustomFieldSearcherClass("key"));

        // Right type
        doReturn(customFieldSearcherModuleDescriptor).when(mockPluginAccessor).getEnabledPluginModule("key");
        doReturn(MockCustomFieldSearcher.class).when(customFieldSearcherModuleDescriptor).getModuleClass();
        assertEquals(MockCustomFieldSearcher.class, customFieldManager.getCustomFieldSearcherClass("key"));
    }
}
