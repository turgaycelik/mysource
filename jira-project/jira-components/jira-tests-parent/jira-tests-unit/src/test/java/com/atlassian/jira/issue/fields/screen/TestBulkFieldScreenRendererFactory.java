package com.atlassian.jira.issue.fields.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.MockCustomField;
import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptorImpl;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.module.ModuleFactory;

import com.opensymphony.workflow.loader.ActionDescriptor;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.BulkFieldScreenRendererFactory}
 *
 * @since v4.1
 */
public class TestBulkFieldScreenRendererFactory
{
    @Test
    public void testCreateRendererNoTabs() throws Exception
    {
        final IMocksControl iMocksControl = EasyMock.createControl();

        final FieldLayoutManager layoutManager = iMocksControl.createMock(FieldLayoutManager.class);
        final FieldManager fieldManager = iMocksControl.createMock(FieldManager.class);

        EasyMock.expect(fieldManager.getUnavailableFields()).andReturn(Collections.<Field>emptySet());

        BulkFieldScreenRendererFactory factory = new BulkFieldScreenRendererFactory(fieldManager, layoutManager, EasyMock.createMock(HackyFieldRendererRegistry.class))
        {
            @Override
            FieldScreen getScreen(final ActionDescriptor actionDescriptor)
            {
                return null;
            }
        };

        iMocksControl.replay();

        final BulkFieldScreenRendererImpl screenRenderer = factory.createRenderer(Collections.<Issue>emptyList(), null);
        assertTrue(screenRenderer.getFieldScreenRenderTabs().isEmpty());

        iMocksControl.verify();
    }

    @Test
    public void testCreateRendererNoUnavailable() throws Exception
    {
        final Project mockProject = new MockProject(5);
        final IssueType mockIssueType1 = new MockIssueType("6", "Six");
        final IssueType mockIssueType2 = new MockIssueType("7", "Seven");

        //A couple of different issues.
        final MockIssue issue1 = new MockIssue(3);
        issue1.setIssueTypeObject(mockIssueType1);
        issue1.setProjectObject(mockProject);

        final MockIssue issue2 = new MockIssue(4);
        issue2.setIssueTypeObject(mockIssueType2);
        issue2.setProjectObject(mockProject);

        //Create some orderable fields fields.
        final MockFieldManager mfm = new MockFieldManager();
        final OrderableField of1 = mfm.addMockOrderableField(1);
        final OrderableField of2 = mfm.addMockOrderableField(2);
        final OrderableField of3 = mfm.addMockOrderableField(3);

        //Create the screens.
        final MockFieldScreen fieldScreen = new MockFieldScreen();
        fieldScreen.addMockTab().setName("EmptyScreen");

        final MockFieldScreenTab tab1 = fieldScreen.addMockTab();
        tab1.setName("One");
        tab1.addFieldScreenLayoutItem().setOrderableField(of1);
        tab1.addFieldScreenLayoutItem().setOrderableField(of2);

        final MockFieldScreenTab tab2 = fieldScreen.addMockTab();
        tab2.setName("Two");
        tab2.addFieldScreenLayoutItem().setOrderableField(of3);

        //Create some field layouts
        MockFieldLayoutManager mflm = new MockFieldLayoutManager();
        MockFieldLayout fieldLayout = mflm.addLayoutItem(issue1);
        fieldLayout.addFieldLayoutItem(of1);
        fieldLayout.addFieldLayoutItem(of2);
        fieldLayout.addFieldLayoutItem(of3);

        fieldLayout = mflm.addLayoutItem(issue2);
        fieldLayout.addFieldLayoutItem(of1);
        fieldLayout.addFieldLayoutItem(of2);
        fieldLayout.addFieldLayoutItem(of3);

        BulkFieldScreenRendererFactory factory = new BulkFieldScreenRendererFactory(mfm, mflm, EasyMock.createMock(HackyFieldRendererRegistry.class))
        {
            @Override
            FieldScreen getScreen(final ActionDescriptor actionDescriptor)
            {
                return fieldScreen;
            }
        };

        final List<Issue> issues = Arrays.<Issue>asList(issue1, issue2);
        final BulkFieldScreenRendererImpl screenRenderer = factory.createRenderer(issues, null);
        final List<FieldScreenRenderTab> tabList = screenRenderer.getFieldScreenRenderTabs();
        assertEquals(2, tabList.size());

        //Check the first tab. Its items should be in the same order as the screen its generated from.
        FieldScreenRenderTab actualTab = tabList.get(0);
        assertEquals("One", actualTab.getName());
        List<FieldScreenRenderLayoutItem> actualLayoutItems = actualTab.getFieldScreenRenderLayoutItems();
        assertEquals(2, actualLayoutItems.size());

        int count = 0;
        for (FieldScreenRenderLayoutItem item : actualLayoutItems)
        {
            final FieldScreenLayoutItem screenLayoutItem = tab1.getFieldScreenLayoutItem(count++);
            assertEquals(screenLayoutItem, item.getFieldScreenLayoutItem());

            final BulkFieldScreenRenderLayoutItemImpl bulkItem = (BulkFieldScreenRenderLayoutItemImpl) item;
            assertCollectionEquals(bulkItem.getFieldLayoutItems(), createFieldLayoutItem(mflm, issues, screenLayoutItem.getOrderableField()));
        }

        //Check the first tab. Its items should be in the same order as the screen its generated from.
        actualTab = tabList.get(1);
        assertEquals("Two", actualTab.getName());
        actualLayoutItems = actualTab.getFieldScreenRenderLayoutItems();
        assertEquals(1, actualLayoutItems.size());

        count = 0;
        for (FieldScreenRenderLayoutItem item : actualLayoutItems)
        {
            final FieldScreenLayoutItem screenLayoutItem = tab2.getFieldScreenLayoutItem(count++);
            assertEquals(screenLayoutItem, item.getFieldScreenLayoutItem());

            final BulkFieldScreenRenderLayoutItemImpl bulkItem = (BulkFieldScreenRenderLayoutItemImpl) item;
            assertCollectionEquals(bulkItem.getFieldLayoutItems(), createFieldLayoutItem(mflm, issues, screenLayoutItem.getOrderableField()));

        }
    }

    @Test
    public void testCreateRendererUnavailableFields() throws Exception
    {
        final Project mockProject = new MockProject(5);
        final IssueType mockIssueType1 = new MockIssueType("6", "Six");
        final IssueType mockIssueType2 = new MockIssueType("7", "Seven");

        //A couple of different issues.
        final MockIssue issue1 = new MockIssue(3);
        issue1.setIssueTypeObject(mockIssueType1);
        issue1.setProjectObject(mockProject);

        final MockIssue issue2 = new MockIssue(4);
        issue2.setIssueTypeObject(mockIssueType2);
        issue2.setProjectObject(mockProject);

        //Create some orderable fields fields.
        final MockFieldManager mfm = new MockFieldManager();
        final OrderableField of1 = mfm.addMockOrderableField(1);
        final OrderableField of2 = mfm.addMockOrderableField(2);
        final OrderableField of3 = mfm.addMockOrderableField(3);
        mfm.addUnavilableField(of2);

        //Create the screens.
        final MockFieldScreen fieldScreen = new MockFieldScreen();
        fieldScreen.addMockTab().setName("EmptyScreen");

        final MockFieldScreenTab tab1 = fieldScreen.addMockTab();
        tab1.setName("One");
        tab1.addFieldScreenLayoutItem().setOrderableField(of1);
        tab1.addFieldScreenLayoutItem().setOrderableField(of2);

        final MockFieldScreenTab tab2 = fieldScreen.addMockTab();
        tab2.setName("Two");
        tab2.addFieldScreenLayoutItem().setOrderableField(of3);

        //Create some field layouts
        MockFieldLayoutManager mflm = new MockFieldLayoutManager();
        MockFieldLayout fieldLayout = mflm.addLayoutItem(issue1);
        fieldLayout.addFieldLayoutItem(of1);
        fieldLayout.addFieldLayoutItem(of2);
        fieldLayout.addFieldLayoutItem(of3);

        fieldLayout = mflm.addLayoutItem(issue2);
        fieldLayout.addFieldLayoutItem(of1);
        fieldLayout.addFieldLayoutItem(of2);
        fieldLayout.addFieldLayoutItem(of3);

        BulkFieldScreenRendererFactory factory = new BulkFieldScreenRendererFactory(mfm, mflm, EasyMock.createMock(HackyFieldRendererRegistry.class))
        {
            @Override
            FieldScreen getScreen(final ActionDescriptor actionDescriptor)
            {
                return fieldScreen;
            }
        };

        final List<Issue> issues = Arrays.<Issue>asList(issue1, issue2);
        final BulkFieldScreenRendererImpl screenRenderer = factory.createRenderer(issues, null);
        final List<FieldScreenRenderTab> tabList = screenRenderer.getFieldScreenRenderTabs();
        assertEquals(2, tabList.size());

        //Check the first tab.
        FieldScreenRenderTab actualTab = tabList.get(0);
        assertEquals("One", actualTab.getName());
        List<FieldScreenRenderLayoutItem> actualLayoutItems = actualTab.getFieldScreenRenderLayoutItems();
        assertEquals(1, actualLayoutItems.size());

        FieldScreenRenderLayoutItem item = actualLayoutItems.get(0);
        FieldScreenLayoutItem screenLayoutItem = tab1.getFieldScreenLayoutItem(0);
        assertEquals(screenLayoutItem, item.getFieldScreenLayoutItem());

        BulkFieldScreenRenderLayoutItemImpl bulkItem = (BulkFieldScreenRenderLayoutItemImpl) item;
        assertCollectionEquals(bulkItem.getFieldLayoutItems(), createFieldLayoutItem(mflm, issues, screenLayoutItem.getOrderableField()));

        //Check the second tab
        actualTab = tabList.get(1);
        assertEquals("Two", actualTab.getName());
        actualLayoutItems = actualTab.getFieldScreenRenderLayoutItems();
        assertEquals(1, actualLayoutItems.size());

        item = actualLayoutItems.get(0);
        screenLayoutItem = tab2.getFieldScreenLayoutItem(0);
        assertEquals(screenLayoutItem, item.getFieldScreenLayoutItem());

        bulkItem = (BulkFieldScreenRenderLayoutItemImpl) item;
        assertCollectionEquals(bulkItem.getFieldLayoutItems(), createFieldLayoutItem(mflm, issues, screenLayoutItem.getOrderableField()));
    }

    @Test
    public void testCreateRendererCustomFieldsFields() throws Exception
    {
        final Project mockProject = new MockProject(5);
        final IssueType mockIssueType1 = new MockIssueType("6", "Six");
        final IssueType mockIssueType2 = new MockIssueType("7", "Seven");

        //A couple of different issues.
        final MockIssue issue1 = new MockIssue(3);
        issue1.setIssueTypeObject(mockIssueType1);
        issue1.setProjectObject(mockProject);

        final MockIssue issue2 = new MockIssue(4);
        issue2.setIssueTypeObject(mockIssueType2);
        issue2.setProjectObject(mockProject);

        //Create some orderable fields fields.
        final MockFieldManager mfm = new MockFieldManager();
        final OrderableField of1 = mfm.addMockOrderableField(1);
        final MockCustomField of2 = mfm.addMockCustomField(2);
        makeViewExists(of2, false);
        final MockCustomField of3 = mfm.addMockCustomField(3);
        makeViewExists(of3, true);

        //Create the screens.
        final MockFieldScreen fieldScreen = new MockFieldScreen();
        fieldScreen.addMockTab().setName("EmptyScreen");

        final MockFieldScreenTab tab1 = fieldScreen.addMockTab();
        tab1.setName("One");
        tab1.addFieldScreenLayoutItem().setOrderableField(of1);
        tab1.addFieldScreenLayoutItem().setOrderableField(of2);

        final MockFieldScreenTab tab2 = fieldScreen.addMockTab();
        tab2.setName("Two");
        tab2.addFieldScreenLayoutItem().setOrderableField(of3);

        //Create some field layouts
        MockFieldLayoutManager mflm = new MockFieldLayoutManager();
        MockFieldLayout fieldLayout = mflm.addLayoutItem(issue1);
        fieldLayout.addFieldLayoutItem(of1);
        fieldLayout.addFieldLayoutItem(of2);
        fieldLayout.addFieldLayoutItem(of3);

        fieldLayout = mflm.addLayoutItem(issue2);
        fieldLayout.addFieldLayoutItem(of1);
        fieldLayout.addFieldLayoutItem(of2);
        fieldLayout.addFieldLayoutItem(of3);

        BulkFieldScreenRendererFactory factory = new BulkFieldScreenRendererFactory(mfm, mflm, EasyMock.createMock(HackyFieldRendererRegistry.class))
        {
            @Override
            FieldScreen getScreen(final ActionDescriptor actionDescriptor)
            {
                return fieldScreen;
            }
        };

        final List<Issue> issues = Arrays.<Issue>asList(issue1, issue2);
        final BulkFieldScreenRendererImpl screenRenderer = factory.createRenderer(issues, null);
        final List<FieldScreenRenderTab> tabList = screenRenderer.getFieldScreenRenderTabs();
        assertEquals(2, tabList.size());

        //Check the first tab.
        FieldScreenRenderTab actualTab = tabList.get(0);
        assertEquals("One", actualTab.getName());
        List<FieldScreenRenderLayoutItem> actualLayoutItems = actualTab.getFieldScreenRenderLayoutItems();
        assertEquals(1, actualLayoutItems.size());

        FieldScreenRenderLayoutItem item = actualLayoutItems.get(0);
        FieldScreenLayoutItem screenLayoutItem = tab1.getFieldScreenLayoutItem(0);
        assertEquals(screenLayoutItem, item.getFieldScreenLayoutItem());

        BulkFieldScreenRenderLayoutItemImpl bulkItem = (BulkFieldScreenRenderLayoutItemImpl) item;
        assertCollectionEquals(bulkItem.getFieldLayoutItems(), createFieldLayoutItem(mflm, issues, screenLayoutItem.getOrderableField()));

        //Check the second tab
        actualTab = tabList.get(1);
        assertEquals("Two", actualTab.getName());
        actualLayoutItems = actualTab.getFieldScreenRenderLayoutItems();
        assertEquals(1, actualLayoutItems.size());

        item = actualLayoutItems.get(0);
        screenLayoutItem = tab2.getFieldScreenLayoutItem(0);
        assertEquals(screenLayoutItem, item.getFieldScreenLayoutItem());

        bulkItem = (BulkFieldScreenRenderLayoutItemImpl) item;
        assertCollectionEquals(bulkItem.getFieldLayoutItems(), createFieldLayoutItem(mflm, issues, screenLayoutItem.getOrderableField()));
    }


    private Collection<FieldLayoutItem> createFieldLayoutItem(FieldLayoutManager flm,
            Collection<Issue> issues, OrderableField field)
    {
        Collection<FieldLayoutItem> items = new ArrayList<FieldLayoutItem>();
        for (Issue issue : issues)
        {
            items.add(flm.getFieldLayout(issue).getFieldLayoutItem(field));
        }
        return items;
    }

    private void assertCollectionEquals(Collection<?> actual, Collection<?> expected)
    {
        assertEquals(actual.size(), expected.size());
        assertTrue(expected.containsAll(actual));
    }

    private static CustomField makeViewExists(MockCustomField field, boolean exists)
    {

        final MockCustomFieldType customFieldType = field.createCustomFieldType();
        customFieldType.init(createViewExistsCFType(exists));
        field.setCustomFieldType(customFieldType);

        return field;
    }

    private static CustomFieldTypeModuleDescriptor createViewExistsCFType(final boolean exists)
    {
        return new CustomFieldTypeModuleDescriptorImpl(new MockSimpleAuthenticationContext(null), null, ModuleFactory.LEGACY_MODULE_FACTORY, null)
        {
            @Override
            public boolean isViewTemplateExists()
            {
                return exists;
            }
        };
    }
}
