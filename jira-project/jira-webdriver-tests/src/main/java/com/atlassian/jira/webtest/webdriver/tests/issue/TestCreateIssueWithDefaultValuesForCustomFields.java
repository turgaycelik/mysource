package com.atlassian.jira.webtest.webdriver.tests.issue;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.framework.fields.CustomField;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.Option;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.WebDriverSelectElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.junit.Test;
import org.openqa.selenium.By;

import javax.inject.Inject;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestCreateIssueWithDefaultValuesForCustomFields extends BaseJiraWebTest
{
    @Inject
    private PageBinder pageBinder;

    @Test
    @Restore ("xml/JRA-30942.xml")
    public void testTogglingProjectSetDefaultValuesForCustoFieldsProperly()
    {
        jira.gotoHomePage();
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        createIssueDialog.fill("summary", "summmary retain");

        createIssueDialog.selectProject("P1");
        MultiSelectCustomField customField = createIssueDialog.getCustomField("customfield_10000", MultiSelectCustomField.class);
        Iterable<String> selectedOptions = customField.getSelectedOptions();
        assertThat(selectedOptions, hasItems("AAA", "BBB"));
        assertThat(Lists.newArrayList(selectedOptions), hasSize(2));
        assertThat(createIssueDialog.getFieldValue("customfield_10001"), is("CONTEXT 1"));

        createIssueDialog.selectProject("P2");
        customField = createIssueDialog.getCustomField("customfield_10000", MultiSelectCustomField.class);
        selectedOptions = customField.getSelectedOptions();
        assertThat(selectedOptions, hasItems("BBB", "CCC"));
        assertThat(Lists.newArrayList(selectedOptions), hasSize(2));
        assertThat(createIssueDialog.getFieldValue("customfield_10001"), is("CONTEXT 2"));

        assertThat(createIssueDialog.getFieldValue("summary"), is("summmary retain"));
    }

    @Test
    @Restore ("xml/JRA-30942.xml")
    public void testCreateAnotherShouldRetainDirtyCustomFieldValues()
    {
        jira.gotoHomePage();

        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());
        createIssueDialog.checkCreateMultiple();

        createIssueDialog.fill("summary", "summmary retain");
        PageElement textCustomfield = createIssueDialog.getCustomFieldElement("customfield_10001");
        textCustomfield.select().clear().type("value");

        createIssueDialog = createIssueDialog.submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);
        assertEquals("Field value was retained", createIssueDialog.getFieldValue("customfield_10001"), "value");

        createIssueDialog.close();
        createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        assertThat("Field value is default after dialog reopen", createIssueDialog.getFieldValue("customfield_10001"), is("CONTEXT 1"));
    }

    @Test
    @Restore ("xml/JRA-33997.xml")
    public void testToggleIssueTypeShouldRetainDirtyCustomFieldValues() throws Exception
    {
        jira.gotoHomePage();

        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        createIssueDialog.selectProject("P1");
        // TODO uncomment once JRA-41586 is resolved, until then it is ok to have it like this, because P1/Bug is the defaault
        // createIssueDialog.selectIssueType("Bug");

        // instead assert that these are defaults
        assertEquals("P1 is selected project", createIssueDialog.getProject(), "P1");
        assertEquals("Bug is selected issue type", createIssueDialog.getIssueType(), "Bug");

        createIssueDialog.switchToCustomMode().addFields("customfield_10001");

        PageElement textCustomfield = createIssueDialog.getCustomFieldElement("customfield_10001");
        textCustomfield.select().clear().type("value");

        createIssueDialog.selectIssueType("Task");
        assertEquals("Field value was retained after issue type change", createIssueDialog.getFieldValue("customfield_10001"), "value");

        createIssueDialog.selectProject("P2");
        assertEquals("Field value was retained after project change", createIssueDialog.getFieldValue("customfield_10001"), "value");
    }

    @Test
    @Restore ("xml/JRA-33997.xml")
    public void testToggleIssueTypeShouldRetainDirtyCustomCascadingSelect() throws Exception
    {
        final String CUSTOM_FIELD_ID = "customfield_10100";
        final String CUSTOM_FIELD_CHILD_ID = "customfield_10100:1";

        jira.gotoHomePage();

        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        createIssueDialog.fill("summary", "summmary retain");

        createIssueDialog.selectProject("P1");

        assertEquals("P1 is selected project", createIssueDialog.getProject(), "P1");
        assertEquals("Bug is selected issue type", createIssueDialog.getIssueType(), "Bug");


        createIssueDialog.switchToCustomMode().addFields(CUSTOM_FIELD_ID);

        SelectCustomField parentCombo = createIssueDialog.getCustomField(CUSTOM_FIELD_ID, SelectCustomField.class);
        SelectCustomField childCombo = createIssueDialog.getCustomField(CUSTOM_FIELD_CHILD_ID, SelectCustomField.class);
        childCombo.selectOption("Sub-option A 2");

        createIssueDialog.selectIssueType("Task");
        assertEquals("Child field value was retained after issue type change", createIssueDialog.getCustomField(CUSTOM_FIELD_CHILD_ID, SelectCustomField.class).getSelectedText(), "Sub-option A 2");

        parentCombo.selectOption("Option B");
        childCombo.selectOption("Sub-option B 2");

        createIssueDialog.selectIssueType("Bug");
        assertEquals("Parent field value was retained after issue type change", createIssueDialog.getCustomField(CUSTOM_FIELD_ID, SelectCustomField.class).getSelectedText(), "Option B");
        assertEquals("Child field value was retained after issue type change", createIssueDialog.getCustomField(CUSTOM_FIELD_CHILD_ID, SelectCustomField.class).getSelectedText(), "Sub-option B 2");

        createIssueDialog.selectProject("P2");
        assertEquals("Parent field value was retained after issue type change", createIssueDialog.getCustomField(CUSTOM_FIELD_ID, SelectCustomField.class).getSelectedText(), "Option B");
        assertEquals("Child field value was retained after issue type change", createIssueDialog.getCustomField(CUSTOM_FIELD_CHILD_ID, SelectCustomField.class).getSelectedText(), "Sub-option B 2");

        // check if change to child field only will be retained
        childCombo.selectOption("Sub-option B 1");

        createIssueDialog.selectProject("P1");
        assertEquals("Child field value was retained after project change", createIssueDialog.getCustomField(CUSTOM_FIELD_CHILD_ID, SelectCustomField.class).getSelectedText(), "Sub-option B 1");

        createIssueDialog.selectIssueType("Task");
        assertEquals("Child field value was retained after issue type change", createIssueDialog.getCustomField(CUSTOM_FIELD_CHILD_ID, SelectCustomField.class).getSelectedText(), "Sub-option B 1");

        // after create mutiple it should retain its value, I'm very sorry for such a long test, but this stuff tends to be broken very often :)
        createIssueDialog.checkCreateMultiple();
        createIssueDialog = createIssueDialog.submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);
        assertEquals("Field value was retained", createIssueDialog.getCustomField(CUSTOM_FIELD_ID, SelectCustomField.class).getSelectedText(), "Option B");
        assertEquals("Child field value was retained after submit", createIssueDialog.getCustomField(CUSTOM_FIELD_CHILD_ID, SelectCustomField.class).getSelectedText(), "Sub-option B 1");
    }

    public static class SelectCustomField implements CustomField
    {
        private WebDriverSelectElement field;

        public SelectCustomField(PageElement form, String id)
        {
            this.field = form.find(By.id(id), WebDriverSelectElement.class);
        }

        public void selectOption(final String value)
        {
            final Option option = Iterables.find(field.getAllOptions(), new Predicate<Option>()
            {
                @Override
                public boolean apply(final Option option)
                {
                    return option.text().equals(value);
                }
            });

            if(option != null)
            {
                field.select(option);
            }
        }

        public String getSelectedText() {
            return field.getSelected().text();
        }
    }


    public static class MultiSelectCustomField implements CustomField {

        private PageElement field;

        public MultiSelectCustomField(PageElement form, String id)
        {
            this.field = form.find(By.id(id));
        }

        private Iterable<String> getSelectedOptions()
        {
            Iterable<PageElement> selectedOptions = Iterables.filter(field.findAll(By.tagName("option")), new Predicate<PageElement>()
            {
                @Override
                public boolean apply(PageElement option)
                {
                    return option.isSelected();
                }
            });

            return Iterables.transform(selectedOptions, new Function<PageElement, String>()
            {
                @Override
                public String apply(PageElement option)
                {
                    return option.getText();
                }
            });
        }
    }


}

