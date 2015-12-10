package com.atlassian.jira.webtest.webdriver.tests.screeneditor;

import com.atlassian.jira.functest.framework.matchers.IterableMatchers;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.components.ScreenEditor;
import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.admin.screen.EditScreenTab;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.atlassian.jira.functest.framework.matchers.IterableMatchers.emptyIterable;
import static com.atlassian.jira.functest.framework.matchers.IterableMatchers.hasItems;
import static com.atlassian.jira.functest.framework.matchers.IterableMatchers.isSingleton;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractTestScreenEditorComponent extends BaseJiraWebTest
{
    @Inject
    private Timeouts timeouts;

    private static final String SCOTTS_TAB = "Scotts Tab";
    private static final String FIELD_TAB = "Field Tab";
    private static final String RESOLUTION = "Resolution";

    @Before
    public void setUp()
    {
        backdoor.restoreDataFromResource("TestScreensResource.xml", LicenseKeys.V2_COMMERCIAL.getLicenseString());
    }

    public abstract ScreenEditor getScreenEditor();

    @Test
    public void testAddingFields()
    {
        final ScreenEditor screenEditor = getScreenEditor();
        final EditScreenTab currentTab = screenEditor.getCurrentTab();
        currentTab.addField("Linked Issues");
        waitUntil(fieldsToAdd(currentTab.getFieldsPicker()), IterableMatchers.isSingleton(RESOLUTION));
        waitUntilTrue("Expected [Linked Issues] field to be added", currentTab.hasField("Linked Issues"));
        currentTab.addField(RESOLUTION);
        waitUntilTrue("Expected [Resolution field] to be added", currentTab.hasField(RESOLUTION));
        waitUntil(fieldsToAdd(currentTab), emptyIterable(String.class));
        assertTrue(openCreateDialog().getFields().contains(RESOLUTION));
    }

    @Test
    public void testRemovingFields()
    {
        final ScreenEditor screenEditor = getScreenEditor();
        final EditScreenTab currentTab = screenEditor.getCurrentTab();
        currentTab.removeField("Labels");
        waitUntilFalse("Expected [Labels] field to be removed", currentTab.hasField("Labels"));
        waitUntil(fieldsToAdd(currentTab), hasItems(String.class, "Labels"));
        currentTab.removeField("Attachment");
        waitUntilFalse("Expected [Attachment] field to be removed", currentTab.hasField("Attachment"));
        waitUntil(fieldsToAdd(currentTab), hasItems(String.class, "Attachment"));
        final List<String> fields = openCreateDialog().getFields();
        assertFalse(fields.contains("Labels"));
        assertFalse(fields.contains("Attachment"));
    }

    @Test
    public void testAddingAndRemovingTab()
    {
        ScreenEditor screenEditor = getScreenEditor();
        final EditScreenTab scottsTab = screenEditor.addTab(SCOTTS_TAB);
        assertEquals(Lists.newArrayList(FIELD_TAB, SCOTTS_TAB), screenEditor.getTabs());
        assertThat(scottsTab.getFieldNames().byDefaultTimeout(), Matchers.<String>emptyIterable());
        assertThat(scottsTab.addField(RESOLUTION).getFieldNames().byDefaultTimeout(), containsInAnyOrder(RESOLUTION));
        waitUntil(fieldsToAdd(scottsTab), isSingleton("Linked Issues"));
        assertEquals(Lists.newArrayList(RESOLUTION), openCreateDialog().openTab(SCOTTS_TAB).getFields());
        screenEditor = getScreenEditor();
        screenEditor.openTab(SCOTTS_TAB).removeTab();
        final EditScreenTab currentTab = screenEditor.getCurrentTab();
        assertEquals(FIELD_TAB, currentTab.getName());
        assertEquals(Lists.newArrayList(FIELD_TAB), screenEditor.getTabs());
        waitUntil(fieldsToAdd(currentTab), hasItems(String.class, RESOLUTION));
    }

    @Test
    public void testAddTabFail()
    {
        final ScreenEditor screenEditor = getScreenEditor();
        final String error = screenEditor.addTabExpectingFail(FIELD_TAB);
        assertEquals("Tab Field Tab already exists", error);
    }

    @Test
    public void testReorderingTabs()
    {
        final ScreenEditor screenEditor = getScreenEditor();
        final EditScreenTab scottsTab = screenEditor.addTab(SCOTTS_TAB);
        scottsTab.addField(RESOLUTION);
        screenEditor.moveTabAfter(FIELD_TAB, SCOTTS_TAB);
        assertEquals(Lists.newArrayList(SCOTTS_TAB, FIELD_TAB), openCreateDialog().getTabs());
        assertEquals(Lists.newArrayList(RESOLUTION), openCreateDialog().getFields());
    }

    @Test
    public void testRenamingTabFail()
    {
        final ScreenEditor screenEditor = getScreenEditor();
        final String error = screenEditor.getCurrentTab().renameExpectingError("");
        assertEquals("Tab name cannot be empty", error);
    }

    private CreateIssueDialog openCreateDialog()
    {
        jira.visit(DashboardPage.class);
        return pageBinder.bind(JiraHeader.class).createIssue();
    }

    private TimedQuery<Iterable<String>> fieldsToAdd(final EditScreenTab tab)
    {
        return fieldsToAdd(tab.getFieldsPicker());
    }

    /**
     * We need to reopen the suggestions each time we re-evaluate the query, otherwise the initial state will
     * remain pre-loaded into the suggestions.
     *
     * @param fieldsPicker fields picker to manipulate
     * @return timed query representing the current list of fields available to add
     */
    private TimedQuery<Iterable<String>> fieldsToAdd(final SingleSelect fieldsPicker)
    {
        return Queries.forSupplier(timeouts, new Supplier<Iterable<String>>()
        {
            @Override
            public Iterable<String> get()
            {
                clearAndAndReopenSuggestions(fieldsPicker);
                return fieldsPicker.getSuggestionsTimed().now();
            }
        });
    }

    private void clearAndAndReopenSuggestions(final SingleSelect fieldsPicker)
    {
        waitUntilFalse(fieldsPicker.clear().isSuggestionsOpen());
        fieldsPicker.triggerSuggestions();
        waitUntilTrue(fieldsPicker.isSuggestionsOpen());
    }
}
