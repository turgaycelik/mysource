package com.atlassian.jira.upgrade.tasks;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenStore;
import com.atlassian.jira.issue.label.CachingLabelStore;
import com.atlassian.jira.issue.search.CachingSearchRequestStore;
import com.atlassian.jira.jql.parser.DefaultJqlQueryParser;
import com.atlassian.jira.jql.util.JqlStringSupportImpl;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.portal.CachingPortletConfigurationStore;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.GenericValue;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestUpgradeTask_Build552
{
    private OfBizDelegator mockOfBizDelegator;
    private LocaleManager mockLocaleManager;
    private I18nHelper.BeanFactory mockBeanFactory;
    private CustomFieldManager mockCustomFieldManager;
    private CachingLabelStore cachingLabelStore;
    private CachingSearchRequestStore cachingSearchRequestStore;
    private CachingPortletConfigurationStore cachingPortletConfigurationStore;

    @Before
    public void setUp() throws Exception
    {
        mockOfBizDelegator = createMock(OfBizDelegator.class);
        mockLocaleManager = createMock(LocaleManager.class);
        mockBeanFactory = createMock(I18nHelper.BeanFactory.class);
        mockCustomFieldManager = createMock(CustomFieldManager.class);
        cachingLabelStore = createMock(CachingLabelStore.class);
        cachingSearchRequestStore = createMock(CachingSearchRequestStore.class);
        cachingPortletConfigurationStore = createMock(CachingPortletConfigurationStore.class);
    }

    @Test
    public void testDoUpgradeNoFields() throws Exception
    {
        expect(mockOfBizDelegator.findByAnd("CustomField",
                ImmutableMap.of("customfieldtypekey", "com.atlassian.jira.plugin.labels:labels"))).
                andReturn(Collections.<GenericValue>emptyList());

        replay(mockOfBizDelegator);
        UpgradeTask_Build552 upgradeTask = new UpgradeTask_Build552(mockOfBizDelegator, null, null, null, null, null, null, null, null, null, null, null, null);
        upgradeTask.doUpgrade(false);

        verify(mockOfBizDelegator);
    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        final AtomicBoolean migrateDataCalled = new AtomicBoolean(false);
        final AtomicBoolean updateSRCalled = new AtomicBoolean(false);
        final AtomicBoolean updateNavigatorColumns = new AtomicBoolean(false);
        final AtomicBoolean updateGadgets = new AtomicBoolean(false);
        final AtomicBoolean updateFieldScreens = new AtomicBoolean(false);

        final GenericValue customFieldGv1 = new MockGenericValue("CustomField", ImmutableMap.of("name", "Labels", "id", 12232L));
        final GenericValue customFieldGv2 = new MockGenericValue("CustomField", ImmutableMap.of("name", "Epic", "id", 10001L));
        expect(mockOfBizDelegator.findByAnd("CustomField",
                ImmutableMap.of("customfieldtypekey", "com.atlassian.jira.plugin.labels:labels"))).
                andReturn(ImmutableList.of(customFieldGv1, customFieldGv2));
        expect(mockLocaleManager.getInstalledLocales()).andReturn(ImmutableSet.of(Locale.ENGLISH));
        expect(mockBeanFactory.getInstance(Locale.ENGLISH)).andReturn(new MockI18nBean());

        expect(mockOfBizDelegator.bulkUpdateByAnd("CustomField",
                ImmutableMap.of
                        (
                                "customfieldtypekey", "com.atlassian.jira.plugin.system.customfieldtypes:labels",
                                "customfieldsearcherkey", "com.atlassian.jira.plugin.system.customfieldtypes:labelsearcher"
                        ),
                ImmutableMap.of("customfieldtypekey", "com.atlassian.jira.plugin.labels:labels"))).andReturn(2);

        mockCustomFieldManager.removeCustomFieldPossiblyLeavingOrphanedData(12232L);
        mockCustomFieldManager.refresh();
        cachingLabelStore.onClearCache(null);

        expect(mockOfBizDelegator.removeByOr("CustomFieldValue", "customfield", ImmutableList.of(12232L, 10001L))).andReturn(2);

        replay(mockOfBizDelegator, mockLocaleManager, mockBeanFactory, mockCustomFieldManager, cachingLabelStore,
                cachingSearchRequestStore, cachingPortletConfigurationStore);
        UpgradeTask_Build552 upgradeTask = new UpgradeTask_Build552(mockOfBizDelegator, mockLocaleManager, null, null, null, mockCustomFieldManager, mockBeanFactory, null, null, null, cachingLabelStore, cachingSearchRequestStore, cachingPortletConfigurationStore)
        {
            //these methods will be tested in isolation!

            @Override
            void migrateCustomFieldData(final List<Long> fieldsToConvertToSystem, final List<Long> customFieldIds)
            {
                migrateDataCalled.set(true);
                assertEquals(1, fieldsToConvertToSystem.size());
                assertTrue(fieldsToConvertToSystem.contains(12232L));
                assertEquals(2, customFieldIds.size());
                assertTrue(customFieldIds.contains(12232L));
                assertTrue(customFieldIds.contains(10001L));
            }

            @Override
            void updateSearchRequests() throws GenericEntityException
            {
                updateSRCalled.set(true);
            }

            @Override
            void updateIssueNavigatorColumns(final List<Long> fieldsToConvertToSystem)
            {
                updateNavigatorColumns.set(true);
                assertEquals(1, fieldsToConvertToSystem.size());
                assertTrue(fieldsToConvertToSystem.contains(12232L));
            }

            @Override
            void updateGadgetConfigurations(final List<Long> fieldsToConvertToSystem) throws GenericEntityException
            {
                updateGadgets.set(true);
                assertEquals(1, fieldsToConvertToSystem.size());
                assertTrue(fieldsToConvertToSystem.contains(12232L));
            }

            @Override
            void updateFieldScreensWithSystemField(final List<Long> fieldsToConvertToSystem)
            {
                updateFieldScreens.set(true);
                assertEquals(1, fieldsToConvertToSystem.size());
                assertTrue(fieldsToConvertToSystem.contains(12232L));
            }
        };
        upgradeTask.doUpgrade(false);

        assertTrue(migrateDataCalled.get());
        assertTrue(updateSRCalled.get());
        assertTrue(updateNavigatorColumns.get());
        assertTrue(updateGadgets.get());
        assertTrue(updateFieldScreens.get());
        verify(mockOfBizDelegator, mockLocaleManager, mockBeanFactory, mockCustomFieldManager, cachingLabelStore,
                cachingSearchRequestStore, cachingPortletConfigurationStore);
    }

    @Test
    public void testMigrateData()
    {
        final GenericValue customFieldGv1 = new MockGenericValue("CustomFieldValue", 
                ImmutableMap.of("issue", 22001L, "customfield", 12232L, "textvalue", "this are some test labels"));
        final GenericValue customFieldGv2 = new MockGenericValue("CustomFieldValue", 
                ImmutableMap.of("issue", 21003L, "customfield", 10001L, "textvalue", "heres another label"));
        final GenericValue customFieldGv3 = new MockGenericValue("CustomFieldValue", 
                MapBuilder.build("issue", 21023L, "customfield", 10001L, "textvalue", null));

        final OfBizListIterator listIterator = createMock(OfBizListIterator.class);
        expect(listIterator.next()).andReturn(customFieldGv1);
        expect(listIterator.next()).andReturn(customFieldGv2);
        expect(listIterator.next()).andReturn(customFieldGv3);
        expect(listIterator.next()).andReturn(null);
        listIterator.close();
        expect(mockOfBizDelegator.findListIteratorByCondition("CustomFieldValue", new MockEntityExpr("customfield", EntityOperator.IN,
                ImmutableList.of(12232L, 10001L)), null, ImmutableList.of("issue", "customfield", "textvalue"), null, null)).
                andReturn(listIterator);

        //these obviously wont return null, but for the purposes of this test we don't care what they return.
        expect(mockOfBizDelegator.createValue("Label", ImmutableMap.<String, Object>of("issue", 22001L, "label", "this"))).andReturn(null);
        expect(mockOfBizDelegator.createValue("Label", ImmutableMap.<String, Object>of("issue", 22001L, "label", "are"))).andReturn(null);
        expect(mockOfBizDelegator.createValue("Label", ImmutableMap.<String, Object>of("issue", 22001L, "label", "some"))).andReturn(null);
        expect(mockOfBizDelegator.createValue("Label", ImmutableMap.<String, Object>of("issue", 22001L, "label", "test"))).andReturn(null);
        expect(mockOfBizDelegator.createValue("Label", ImmutableMap.<String, Object>of("issue", 22001L, "label", "labels"))).andReturn(null);
        expect(mockOfBizDelegator.createValue("Label", ImmutableMap.<String, Object>of("issue", 21003L, "fieldid", 10001L, "label", "heres"))).andReturn(null);
        expect(mockOfBizDelegator.createValue("Label", ImmutableMap.<String, Object>of("issue", 21003L, "fieldid", 10001L, "label", "another"))).andReturn(null);
        expect(mockOfBizDelegator.createValue("Label", ImmutableMap.<String, Object>of("issue", 21003L, "fieldid", 10001L, "label", "label"))).andReturn(null);

        replay(mockOfBizDelegator, listIterator);
        UpgradeTask_Build552 upgradeTask = new UpgradeTask_Build552(mockOfBizDelegator, null, null, null, null, null, null, null, null, null, null, null, null);
        upgradeTask.migrateCustomFieldData(ImmutableList.of(12232L), ImmutableList.of(12232L, 10001L));

        verify(mockOfBizDelegator, listIterator);
    }

    @Test
    public void testUpdateSearchRequests() throws GenericEntityException
    {
        final GenericValue srGv1 = new MockGenericValue("SearchRequest", ImmutableMap.of("id", 33001L));
        final GenericValue srGv2 = new MockGenericValue("SearchRequest", ImmutableMap.of("id", 33003L));
        final GenericValue srGv3 = new MockGenericValue("SearchRequest", ImmutableMap.of("id", 33023L));
        final GenericValue srGvNull = new MockGenericValue("SearchRequest", ImmutableMap.of("id", 33033L));
        final GenericValue fullSrGv1 = new MyMockGenericValue("SearchRequest", ImmutableMap.of("id", 33001L, "request", ""));
        final GenericValue fullSrGv2 = new MyMockGenericValue("SearchRequest",
                ImmutableMap.of("id", 33003L, "request", "project = MKY and cf[10003] in (poo, bear)"));
        final GenericValue fullSrGv3 = new MyMockGenericValue("SearchRequest",
                ImmutableMap.of("id", 33023L, "request", "Epic != donkeys"));
        final GenericValue fullSrGvNull = new MyMockGenericValue("SearchRequest",
                MapBuilder.build("id", 33033L, "request", null));
        expect(mockOfBizDelegator.findByCondition("SearchRequest", null, ImmutableList.of("id"))).andReturn(ImmutableList.of(srGv1, srGv2, srGv3, srGvNull));

        expect(mockOfBizDelegator.findByCondition("SearchRequest", new MockEntityExpr("id", EntityOperator.IN, ImmutableList.of(33001L, 33003L, 33023L, 33033L)), null)).
                andReturn(ImmutableList.of(fullSrGv1, fullSrGv2, fullSrGv3, fullSrGvNull));
        cachingSearchRequestStore.onClearCache(null);

        replay(mockOfBizDelegator, cachingSearchRequestStore);
        final DefaultJqlQueryParser jqlQueryParser = new DefaultJqlQueryParser();
        UpgradeTask_Build552 upgradeTask = new UpgradeTask_Build552(mockOfBizDelegator, null, null,
                jqlQueryParser, new JqlStringSupportImpl(jqlQueryParser), null, null, null, null, null, null, cachingSearchRequestStore, null)
        {
            @Override
            Map<String, String> getSubstitutions(final List<GenericValue> fieldsToConvertToSystemGvs)
            {
                return ImmutableMap.of("cf[10003]", "labels", "Epic", "labels");
            }
        };

        upgradeTask.updateSearchRequests();
        assertFalse(((MyMockGenericValue) fullSrGv1).isStored());
        assertTrue(((MyMockGenericValue) fullSrGv2).isStored());
        assertEquals("project = MKY AND labels in (poo, bear)", ((MyMockGenericValue) fullSrGv2).getNewJql());
        assertTrue(((MyMockGenericValue) fullSrGv3).isStored());
        assertEquals("labels != donkeys", ((MyMockGenericValue) fullSrGv3).getNewJql());
        assertFalse(((MyMockGenericValue) fullSrGvNull).isStored());

        verify(mockOfBizDelegator, cachingSearchRequestStore);
    }

    @Test
    public void testUpdateGadgetConfigurations() throws GenericEntityException
    {
        final GenericValue pcGv1 = new MyMockGenericValue("PortletConfiguration", ImmutableMap.of("id", 12023L));
        final GenericValue pcGv2 = new MyMockGenericValue("PortletConfiguration", ImmutableMap.of("id", 12027L));

        expect(mockOfBizDelegator.findByLike("PortletConfiguration",
                ImmutableMap.of("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.plugin.labels:labels-gadget/templates/plugins/labels/gadget/labels-gadget.xml"))).
                andReturn(ImmutableList.of(pcGv1, pcGv2));

        final EntityCondition entityCondition = new MockEntityConditionList(ImmutableList.of(new MockEntityExpr("portletconfiguration", EntityOperator.IN, ImmutableList.of(12023L, 12027L)),
                new MockEntityExpr("userprefkey", EntityOperator.EQUALS, "fieldId")), EntityOperator.AND);

        final GenericValue userPrefGv1 = new MyMockGenericValue("GadgetUserPreference", newHashMap(of("userprefvalue", "customfield_10002")));
        final GenericValue userPrefGv2 = new MyMockGenericValue("GadgetUserPreference", ImmutableMap.of("userprefvalue", "someotherfield"));
        final GenericValue userPrefGv3 = new MyMockGenericValue("GadgetUserPreference", ImmutableMap.of("userprefvalue", "customfield_10202"));

        expect(mockOfBizDelegator.findByCondition("GadgetUserPreference", entityCondition, null)).andReturn(ImmutableList.of(userPrefGv1, userPrefGv2, userPrefGv3));

        expect(mockOfBizDelegator.bulkUpdateByPrimaryKey("PortletConfiguration",
                ImmutableMap.of("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:labels-gadget/gadgets/labels-gadget.xml"),
                ImmutableList.of(12023L, 12027L))).andReturn(2);
        cachingPortletConfigurationStore.onClearCache(null);

        replay(mockOfBizDelegator, cachingPortletConfigurationStore);
        final UpgradeTask_Build552 upgradeTask = new UpgradeTask_Build552(mockOfBizDelegator, null, null, null, null, null, null, null, null, null, null, null, cachingPortletConfigurationStore);

        upgradeTask.updateGadgetConfigurations(ImmutableList.of(10002L));

        assertTrue(((MyMockGenericValue) userPrefGv1).isStored());
        assertEquals("labels", ((MyMockGenericValue) userPrefGv1).getNewUserPrefValue());
        assertFalse(((MyMockGenericValue) userPrefGv2).isStored());
        assertFalse(((MyMockGenericValue) userPrefGv3).isStored());

        verify(mockOfBizDelegator, cachingPortletConfigurationStore);
    }

    @Test
    public void testUpdateGadgetConfigurationsEmpty() throws GenericEntityException
    {
        expect(mockOfBizDelegator.findByLike("PortletConfiguration",
                ImmutableMap.of("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.plugin.labels:labels-gadget/templates/plugins/labels/gadget/labels-gadget.xml"))).
                andReturn(Collections.<GenericValue>emptyList());

        replay(mockOfBizDelegator);
        final UpgradeTask_Build552 upgradeTask = new UpgradeTask_Build552(mockOfBizDelegator, null, null, null, null, null, null, null, null, null, null, null, null);

        upgradeTask.updateGadgetConfigurations(ImmutableList.of(10002L));

        verify(mockOfBizDelegator);
    }

    @Test
    public void testUpgradeIssueNavigatorColumns() throws Exception
    {
        final OfBizListIterator mockIterator = createMock(OfBizListIterator.class);
        expect(mockOfBizDelegator.findListIteratorByCondition("ColumnLayoutItem", new MockEntityExpr("fieldidentifier", EntityOperator.IN, ImmutableList.of(
                "customfield_10020", "customfield_10030")), null, ImmutableList.of("id", "columnlayout"), ImmutableList.of("columnlayout ASC",
                "horizontalposition ASC"), null)).andReturn(mockIterator);

        // first columnlayoutitem is an instance of a single custom field in a column layout
        // this columnlayoutitem will be simply changed to use the system field
        final MockGenericValue resLayoutItem1 = new MockGenericValue("ColumnLayoutItem", ImmutableMap.of("id", 100L, "columnlayout", 2000L));

        // next 3 columnlayoutitems are an instance of 3 separate custom fields (all resolution date) in the same column layout
        // the first of these will be simply changed to use the system field
        // the other 2 will be removed
        final MockGenericValue resLayoutItem2 = new MockGenericValue("ColumnLayoutItem", ImmutableMap.of("id", 101L, "columnlayout", 2010L));
        final MockGenericValue resLayoutItem3 = new MockGenericValue("ColumnLayoutItem", ImmutableMap.of("id", 102L, "columnlayout", 2010L));
        final MockGenericValue resLayoutItem4 = new MockGenericValue("ColumnLayoutItem", ImmutableMap.of("id", 103L, "columnlayout", 2010L));

        expect(mockIterator.next()).andReturn(resLayoutItem1);
        expect(mockIterator.next()).andReturn(resLayoutItem2);
        expect(mockIterator.next()).andReturn(resLayoutItem3);
        expect(mockIterator.next()).andReturn(resLayoutItem4);
        expect(mockIterator.next()).andReturn(null);
        mockIterator.close();

        expect(mockOfBizDelegator.removeByOr("ColumnLayoutItem", "id", ImmutableList.of(102L, 103L))).andReturn(2);
        expect(mockOfBizDelegator.bulkUpdateByPrimaryKey("ColumnLayoutItem", ImmutableMap.of("fieldidentifier", "labels"), ImmutableList.of(100L, 101L))).andReturn(2);

        final ColumnLayoutManager columnLayoutManager = createMock(ColumnLayoutManager.class);
        columnLayoutManager.refresh();
        replay(mockOfBizDelegator, mockIterator, columnLayoutManager);

        final UpgradeTask_Build552 upgradeTask = new UpgradeTask_Build552(mockOfBizDelegator, null, columnLayoutManager, null, null, null, null, null, null, null, null, null, null);
        upgradeTask.updateIssueNavigatorColumns(ImmutableList.of(10020L, 10030L));

        verify(mockOfBizDelegator, mockIterator, columnLayoutManager);
    }

    @Test
    public void testUpgradeIssueNavigatorColumnsWithNoLayoutsToUpdate() throws Exception
    {

        final OfBizListIterator mockIterator = createMock(OfBizListIterator.class);
        expect(mockOfBizDelegator.findListIteratorByCondition("ColumnLayoutItem", new MockEntityExpr("fieldidentifier", EntityOperator.IN, ImmutableList.of(
                "customfield_10020", "customfield_10030")), null, ImmutableList.of("id", "columnlayout"), ImmutableList.of("columnlayout ASC",
                "horizontalposition ASC"), null)).andReturn(mockIterator);

        expect(mockIterator.next()).andReturn(null);
        mockIterator.close();

        final ColumnLayoutManager columnLayoutManager = createMock(ColumnLayoutManager.class);
        columnLayoutManager.refresh();

        replay(mockOfBizDelegator, mockIterator, columnLayoutManager);

        final UpgradeTask_Build552 upgradeTask = new UpgradeTask_Build552(mockOfBizDelegator, null, columnLayoutManager, null, null, null, null, null, null, null, null, null, null);
        upgradeTask.updateIssueNavigatorColumns(ImmutableList.of(10020L, 10030L));

        verify(mockOfBizDelegator, mockIterator, columnLayoutManager);
    }

    @Test
    public void testGetSubstitutions()
    {
        UpgradeTask_Build552 upgradeTask = new UpgradeTask_Build552(mockOfBizDelegator, null, null, null, null, null, null, null, null, null, null, null, null);
        final GenericValue cfGv1 = new MockGenericValue("CustomField", ImmutableMap.of("id", 10023L, "name", "fooBar"));
        final GenericValue cfGv2 = new MockGenericValue("CustomField", ImmutableMap.of("id", 10423L, "name", "Epic"));
        final Map<String, String> substitutions = upgradeTask.getSubstitutions(ImmutableList.of(cfGv1, cfGv2));
        assertEquals(4, substitutions.size());
        assertEquals("labels", substitutions.get("cf[10023]"));
        assertEquals("labels", substitutions.get("fooBar"));
        assertEquals("labels", substitutions.get("cf[10423]"));
        assertEquals("labels", substitutions.get("Epic"));
    }

    @Test
    public void testUpdateFieldScreensWithSystemField() throws GenericModelException
    {
        final FieldScreenSchemeManager mockFieldScreenSchemeManager = createMock(FieldScreenSchemeManager.class);
        final FieldLayoutManager mockFieldLayoutManager = createMock(FieldLayoutManager.class);
        final FieldScreenStore mockFieldScreenStore = createMock(FieldScreenStore.class);

        expect(mockOfBizDelegator.bulkUpdateByAnd("FieldLayoutItem",
                        ImmutableMap.of("fieldidentifier", "labels"),
                        ImmutableMap.of("fieldidentifier", CustomFieldUtils.CUSTOM_FIELD_PREFIX + 10020L))).andReturn(3);
        expect(mockOfBizDelegator.bulkUpdateByAnd("FieldScreenLayoutItem",
                        ImmutableMap.of("fieldidentifier", "labels"),
                        ImmutableMap.of("fieldidentifier", CustomFieldUtils.CUSTOM_FIELD_PREFIX + 10020L))).andReturn(1);

        expect(mockOfBizDelegator.bulkUpdateByAnd("FieldLayoutItem",
                        ImmutableMap.of("fieldidentifier", "labels"),
                        ImmutableMap.of("fieldidentifier", CustomFieldUtils.CUSTOM_FIELD_PREFIX + 10030L))).andReturn(1);
        expect(mockOfBizDelegator.bulkUpdateByAnd("FieldScreenLayoutItem",
                        ImmutableMap.of("fieldidentifier", "labels"),
                        ImmutableMap.of("fieldidentifier", CustomFieldUtils.CUSTOM_FIELD_PREFIX + 10030L))).andReturn(2);

        final GenericValue cfGv1 = new MockGenericValue("FieldLayoutItem", ImmutableMap.of("id", 10023L, "fieldlayout", 10000L));
        final GenericValue cfGv2 = new MockGenericValue("FieldLayoutItem", ImmutableMap.of("id", 10013L,"fieldlayout", 10000L));
        final GenericValue cfGv3 = new MockGenericValue("FieldLayoutItem", ImmutableMap.of("id", 10098L, "fieldlayout", 10232L));
        expect(mockOfBizDelegator.findByAnd("FieldLayoutItem", ImmutableMap.of("fieldidentifier", "labels"))).andReturn(ImmutableList.of(cfGv1, cfGv2, cfGv3));
        expect(mockOfBizDelegator.removeByOr("FieldLayoutItem", "id", ImmutableList.of(10013L))).andReturn(1);

        final GenericValue cfGv4 = new MockGenericValue("FieldScreenLayoutItem", ImmutableMap.of("id", 10043L, "fieldscreentab", 10000L));
        final GenericValue cfGv5 = new MockGenericValue("FieldScreenLayoutItem", ImmutableMap.of("id", 10053L, "fieldscreentab", 10000L));
        final GenericValue cfGv6 = new MockGenericValue("FieldScreenLayoutItem", ImmutableMap.of("id", 10198L, "fieldscreentab", 10000L));
        expect(mockOfBizDelegator.findByAnd("FieldScreenLayoutItem", ImmutableMap.of("fieldidentifier", "labels"))).andReturn(ImmutableList.of(cfGv4, cfGv5, cfGv6));
        expect(mockOfBizDelegator.removeByOr("FieldScreenLayoutItem", "id", ImmutableList.of(10053L, 10198L))).andReturn(2);

        mockFieldScreenSchemeManager.refresh();
        mockFieldLayoutManager.refresh();
        mockFieldScreenStore.refresh();

        replay(mockOfBizDelegator, mockFieldScreenSchemeManager, mockFieldLayoutManager, mockFieldScreenStore);

        final UpgradeTask_Build552 upgradeTask = new UpgradeTask_Build552(mockOfBizDelegator, null, null, null, null, null, null, mockFieldScreenSchemeManager, mockFieldLayoutManager, mockFieldScreenStore, null, null, null);
        upgradeTask.updateFieldScreensWithSystemField(ImmutableList.of(10020L, 10030L));

        verify(mockOfBizDelegator, mockFieldScreenSchemeManager, mockFieldLayoutManager, mockFieldScreenStore);
    }

    @Test
    public void testMetaData() throws Exception
    {
        UpgradeTask_Build552 upgradeTask = new UpgradeTask_Build552(null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertEquals("552", upgradeTask.getBuildNumber());
        assertEquals("Converts label custom fields to the new label system field.", upgradeTask.getShortDescription());
    }

    static class MyMockGenericValue extends MockGenericValue
    {
        private boolean stored = false;
        private String newJql = null;
        private String newUserPrefValue = null;

        public MyMockGenericValue(String entityName, Map fields)
        {
            super(entityName, fields);
        }

        @Override
        public void setString(final String s, final String s1)
        {
            super.setString(s, s1);
            if (s.equals("request"))
            {
                newJql = s1;
            }
            else if (s.equals("userprefvalue"))
            {
                newUserPrefValue = s1;
            }
        }

        @Override
        public void store() throws GenericEntityException
        {
            stored = true;
        }

        public String getNewJql()
        {
            return newJql;
        }

        public String getNewUserPrefValue()
        {
            return newUserPrefValue;
        }

        public boolean isStored()
        {
            return stored;
        }
    }
}
