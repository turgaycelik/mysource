package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.field.CustomFieldUpdatedEvent;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(ListeningMockitoRunner.class)
public class DefaultFieldLayoutManagerTest
{
    private static final long CF_ID_NUMERIC = 10L;
    private static final String CF_ID = "customfield_" + CF_ID_NUMERIC;
    private static final String CF_NAME = "name";
    private static final String CF_UPDATED_NAME = "new name";

    @Mock @AvailableInContainer ApplicationProperties applicationProperties;
    @Mock @AvailableInContainer CustomFieldManager customFieldManager;
    @Mock @AvailableInContainer FieldManager fieldManager;
    @Mock @AvailableInContainer HackyFieldRendererRegistry hackyFieldRendererRegistry;
    @Mock @AvailableInContainer InstrumentRegistry instrumentRegistry;

    @Mock ConstantsManager constantsManager;
    @Mock ColumnLayoutManager columnLayoutManager;
    @Mock EventPublisher eventPublisher;
    @Mock OfBizDelegator ofBizDelegator;
    @Mock ProjectManager projectManager;
    @Mock SubTaskManager subTaskManager;

    @Mock GenericValue fieldLayoutGv;
    @Mock GenericValue fieldLayoutItemGV;
    @Mock CustomField customField;
    @Mock CustomField updatedCustomField;

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        setUpCustomFieldMocks();
        setUpDefaultFieldLayoutMocks();
    }

    private void setUpCustomFieldMocks()
    {
        when(customField.getId()).thenReturn(CF_ID);
        when(customField.getName()).thenReturn(CF_NAME);
        when(updatedCustomField.getId()).thenReturn(CF_ID);
        when(updatedCustomField.getName()).thenReturn(CF_UPDATED_NAME);
    }

    @SuppressWarnings("deprecation")
    private void setUpDefaultFieldLayoutMocks() throws GenericEntityException
    {
        when(hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(any(OrderableField.class))).thenReturn(false);

        when(fieldManager.getColumnLayoutManager()).thenReturn(columnLayoutManager);
        when(fieldManager.isOrderableField(CF_ID)).thenReturn(true);
        when(fieldManager.getOrderableField(CF_ID)).thenReturn(customField);

        when(fieldLayoutItemGV.getString("fieldidentifier")).thenReturn(CF_ID);
        when(fieldLayoutGv.getRelated("ChildFieldLayoutItem")).thenReturn(singletonList(fieldLayoutItemGV));

        when(ofBizDelegator.findByAnd("FieldLayout", singletonMap("type", "default"))).thenReturn(singletonList(fieldLayoutGv));
    }

    @Test
    public void fieldLayoutManagerShouldUpdateCacheWhenItReceivesACustomFieldNotification() throws Exception
    {
        DefaultFieldLayoutManager fieldLayoutManager = createFieldLayoutManager();

        // check precondition
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(CF_ID_NUMERIC);
        assertThat(fieldLayout.getFieldLayoutItem(CF_ID).getOrderableField().getName(), equalTo(CF_NAME));

        // "update" the name and broadcast an event
        when(fieldManager.getOrderableField(CF_ID)).thenReturn(updatedCustomField);
        fieldLayoutManager.onCustomFieldUpdated(new CustomFieldUpdatedEvent(updatedCustomField, customField));

        // check postcondition
        FieldLayout fieldLayoutAfterUpdate = fieldLayoutManager.getFieldLayout(CF_ID_NUMERIC);
        assertThat(fieldLayoutAfterUpdate.getFieldLayoutItem(CF_ID).getOrderableField().getName(), equalTo(CF_UPDATED_NAME));
    }

    private DefaultFieldLayoutManager createFieldLayoutManager() throws Exception
    {
        return new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager,
                projectManager, new MockI18nHelper().factory(), null, new MemoryCacheManager(), eventPublisher);
    }
}
