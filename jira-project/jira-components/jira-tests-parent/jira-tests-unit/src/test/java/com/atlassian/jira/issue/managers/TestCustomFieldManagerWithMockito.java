package com.atlassian.jira.issue.managers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.field.CustomFieldCreatedEvent;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldDescription;
import com.atlassian.jira.issue.fields.CustomFieldFactory;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.MockFieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.search.searchers.MockCustomFieldSearcher;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.customfields.CreateCustomField;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 *
 * @since v5.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestCustomFieldManagerWithMockito
{
    @Mock private PluginAccessor pluginAccessor;
    @Mock private EventPublisher eventPublisher;
    @Mock private FieldManager fieldManager;
    @Mock private OfBizDelegator delegator;
    @Mock private FieldConfigSchemeManager fieldConfigSchemeManager;
    @Mock private CustomFieldDescription customFieldDescription;
    @Mock private TranslationManager translationManager;
    @Mock private I18nHelper.BeanFactory i18nFactory;
    @Mock private JiraAuthenticationContext authenticationContext;
    @Mock private CustomFieldFactory customFieldFactory;

    @Before
    public void setUp()
    {
        delegator = new MockOfBizDelegator();

        final MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        componentAccessorWorker.registerMock(OfBizDelegator.class, delegator);
        ComponentAccessor.initialiseWorker(componentAccessorWorker);

        doNothing().when(fieldManager).refresh();

        Collection<Plugin> plugins = Collections.singletonList(null);
        when(pluginAccessor.getPlugins()).thenReturn(plugins);
    }

    @Test
    public void testCreatePublishesEvent() throws GenericEntityException
    {
        final DefaultCustomFieldManager customFieldManager = new DefaultCustomFieldManager(pluginAccessor, delegator, fieldConfigSchemeManager, null, null, null, null, null, null, fieldManager, eventPublisher, new MemoryCacheManager(), customFieldFactory, null, null)
        {
            @Override
            public CustomField getCustomFieldObject(Long id)
            {
                return null;
            }
        };
        when(customFieldFactory.create(any(GenericValue.class))).thenReturn(mock(CustomField.class));
        customFieldManager.createCustomField("fieldName", "description", new MockCustomFieldType(), null, null, null);
        verify(eventPublisher).publish(isA(CustomFieldCreatedEvent.class));
    }

    /**
     * https://jira.atlassian.com/browse/JRA-30070
     */
    @Test
    public void testCreateDoesntPutCustomFieldTwiceInTheCache() throws GenericEntityException
    {
        final DefaultCustomFieldManager customFieldManager = createManager();
        CustomFieldType cfType = mock(CustomFieldType.class);
        when(cfType.getKey()).thenReturn(CreateCustomField.FIELD_TYPE_PREFIX + "textfield");

        CustomFieldSearcher cfSearcher = mock(CustomFieldSearcher.class);
        CustomFieldSearcherModuleDescriptor cfSearcherDescriptor = mock(CustomFieldSearcherModuleDescriptor.class);
        when(cfSearcher.getDescriptor()).thenReturn(cfSearcherDescriptor);

        CustomFieldTypeModuleDescriptor cfTypeDescriptor = mock(CustomFieldTypeModuleDescriptor.class);
        when(pluginAccessor.getEnabledPluginModule(CreateCustomField.FIELD_TYPE_PREFIX + "textfield")).thenReturn((ModuleDescriptor) cfTypeDescriptor);

        when(cfTypeDescriptor.getModule()).thenReturn(cfType);

        String name = "Test";

        CustomField customField = customFieldWithName(name);
        when(customFieldFactory.create(any(GenericValue.class))).thenReturn(customField);
        when(customFieldFactory.copyOf(customField)).thenReturn(customField);

        customFieldManager.createCustomField(name, null, cfType, cfSearcher, null, Collections.singletonList(null));
        List<CustomField> customFields = customFieldManager.getCustomFieldObjects();
        Assert.assertEquals(1, customFields.size());
        Assert.assertEquals(name, customFields.get(0).getName());
    }

    private CustomField customFieldWithName(final String name)
    {
        CustomField customField = mock(CustomField.class);
        when(customField.getCustomFieldType()).thenReturn(mock(CustomFieldType.class));
        when(customField.getName()).thenReturn(name);
        return customField;
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDefaultSearcherNull()
    {
        final DefaultCustomFieldManager manager = createManager();
        manager.getDefaultSearcher(null);
    }

    @Test
    public void getDefaultSearcher()
    {
        final DefaultCustomFieldManager manager = spy(createManager());
        CustomFieldType<?,?> type = new MockCustomFieldType("type", "Type");
        final MockCustomFieldSearcher searcher = new MockCustomFieldSearcher("searcher");

        doReturn(Lists.newArrayList(searcher)).when(manager).getCustomFieldSearchers(type);
        assertSame(searcher, manager.getDefaultSearcher(type));
    }

    @Test
    public void getDefaultSearcherNoSearcher()
    {
        final DefaultCustomFieldManager manager = spy(createManager());
        CustomFieldType<?,?> type = new MockCustomFieldType("type", "Type");
        doReturn(Collections.emptyList()).when(manager).getCustomFieldSearchers(type);
        assertNull(manager.getDefaultSearcher(type));
    }

    @Test
    public void testSchemeEquality()
    {
        MockFieldConfigScheme scheme1 = new MockFieldConfigScheme();
        scheme1.setId(1L);
        MockFieldConfigScheme scheme2 = new MockFieldConfigScheme();
        scheme2.setId(2L);
        MockFieldConfigScheme scheme3 = new MockFieldConfigScheme();
        scheme3.setId(3L);
        MockFieldConfigScheme scheme4 = new MockFieldConfigScheme();
        scheme4.setId(4L);

        final DefaultCustomFieldManager manager = spy(createManager());

        assertThat(manager.areConfigSchemesEqual(null, null), is(true));
        assertThat("Nulls should not equal anything else", manager.areConfigSchemesEqual(null, ImmutableList.<FieldConfigScheme>of()), is(false));
        assertThat("Nulls should not equal anything else", manager.areConfigSchemesEqual(null, ImmutableList.<FieldConfigScheme>of(scheme2)), is(false));

        assertThat(manager.areConfigSchemesEqual(ImmutableList.<FieldConfigScheme>of(scheme1, scheme2, scheme3), ImmutableList.<FieldConfigScheme>of(scheme1, scheme2, scheme3)), is(true));
        assertThat(manager.areConfigSchemesEqual(ImmutableList.<FieldConfigScheme>of(scheme1, scheme2, scheme3), ImmutableList.<FieldConfigScheme>of(scheme1, scheme2, scheme4)), is(false));
        assertThat(manager.areConfigSchemesEqual(ImmutableList.<FieldConfigScheme>of(scheme1, scheme2, scheme3), ImmutableList.<FieldConfigScheme>of(scheme1, scheme2)), is(false));
        assertThat(manager.areConfigSchemesEqual(ImmutableList.<FieldConfigScheme>of(scheme1), ImmutableList.<FieldConfigScheme>of(scheme1, scheme2)), is(false));
        assertThat(manager.areConfigSchemesEqual(ImmutableList.<FieldConfigScheme>of(), ImmutableList.<FieldConfigScheme>of(scheme1, scheme2)), is(false));
    }


    private DefaultCustomFieldManager createManager()
    {
        return new DefaultCustomFieldManager(pluginAccessor, delegator, fieldConfigSchemeManager, null, null, null, null, null, null, fieldManager, eventPublisher, new MemoryCacheManager(), customFieldFactory, null, null);
    }
}
