package com.atlassian.jira.webtest.webdriver.tests.admin.customfields;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.model.AdminAction;
import com.atlassian.jira.pageobjects.pages.admin.customfields.ConfigureFieldDialog;
import com.atlassian.jira.pageobjects.pages.admin.customfields.FieldWisherDialog;
import com.atlassian.jira.pageobjects.pages.admin.customfields.TypeSelectionCustomFieldDialog;
import com.atlassian.jira.pageobjects.pages.admin.customfields.ViewCustomFields;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.testkit.beans.CustomFieldConfig;
import com.atlassian.jira.testkit.beans.CustomFieldResponse;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.junit.Test;

import java.util.List;

import static com.atlassian.jira.testkit.beans.CustomFieldConfig.isGlobalPredicate;
import static com.atlassian.jira.testkit.beans.CustomFieldOption.getNameFunction;
import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @since v6.1
 */
@RestoreOnce ("TestAddCustomField.xml")
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION })
public class TestAddCustomField extends BaseJiraWebTest
{
    final String labelsName = "Labels";
    final String labelsTypeKey = "com.atlassian.jira.plugin.system.customfieldtypes:labels";
    final String labelsSearcherKey = "com.atlassian.jira.plugin.system.customfieldtypes:labelsearcher";

    final String multiCheckboxesName = "Checkboxes";
    final String multiCheckboxesTypeKey = "com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes";
    final String multiCheckboxesSearcherKey = "com.atlassian.jira.plugin.system.customfieldtypes:multiselectsearcher";

    @Test
    public void testAddFromGlobalPage()
    {
        final ViewCustomFields viewCustomFields = pageBinder.navigateToAndBind(ViewCustomFields.class);
        assertAddLabelsField(viewCustomFields.addCustomField(), "testAddFromGlobalPage", new Function<ConfigureFieldDialog, Object>()
        {
            @Override
            public Object apply(final ConfigureFieldDialog input)
            {
                return input.nextAndThenAssociate();
            }
        });
    }

    @Test
    public void testAddWithOptionsFromGlobalPage()
    {
        final ViewCustomFields viewCustomFields = pageBinder.navigateToAndBind(ViewCustomFields.class);
        assertAddOptionsField(viewCustomFields.addCustomField(), "testAddWithOptionsFromGlobalPage", new Function<ConfigureFieldDialog, Object>()
        {
            @Override
            public Object apply(final ConfigureFieldDialog input)
            {
                return input.nextAndThenAssociate();
            }
        });
    }

    @Test
    public void testAddFieldFromViewIssuePage()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("TES-1");
        final FieldWisherDialog fieldWisherDialog = viewIssuePage.getIssueMenu()
                .invokeAdmin(AdminAction.ADD_FIELD);

        final TypeSelectionCustomFieldDialog addFieldDialog = fieldWisherDialog.create("field that does not exist");

        final String fieldName = assertAddLabelsField(addFieldDialog, "testAddFieldFromViewIssuePage", new Function<ConfigureFieldDialog, Object>()
        {
            @Override
            public Object apply(final ConfigureFieldDialog input)
            {
                input.create();
                return null;
            }
        });
        assertScreensForProjectTes(fieldName);
    }

    @Test
    public void testAddWithOptionsFromViewIssuePage()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("TES-1");
        final FieldWisherDialog fieldWisherDialog = viewIssuePage.getIssueMenu()
                .invokeAdmin(AdminAction.ADD_FIELD);

        final TypeSelectionCustomFieldDialog addFieldDialog = fieldWisherDialog.create("field that does not exist");

        final String fieldName = assertAddOptionsField(addFieldDialog, "testAddWithOptionsViewIssuePage", new Function<ConfigureFieldDialog, Object>()
        {
            @Override
            public Object apply(final ConfigureFieldDialog input)
            {
                input.create();
                return null;
            }
        });
        assertScreensForProjectTes(fieldName);
    }

    @Test
    public void testAddExistingFieldNotOnScreenFromViewIssuePage()
    {
        final String name = "labels";
        final String description = "description";

        backdoor.customFields().createCustomField(name, description, labelsTypeKey, labelsSearcherKey);

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("TES-1");
        final FieldWisherDialog fieldWisherDialog = viewIssuePage.getIssueMenu()
                .invokeAdmin(AdminAction.ADD_FIELD);

        fieldWisherDialog.select(name);
        assertThat(fieldWisherDialog.getNextButtonText(), equalTo("Add"));
        fieldWisherDialog.clickNext();
        assertScreensForProjectTes(name);
    }

    @Test
    public void testAddExistingFieldAlreadyOnScreenWithNoValueFromViewIssuePage()
    {
        String fieldName = "AlreadyOnScreensWithNoValue";

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("TES-1");
        final FieldWisherDialog fieldWisherDialog = viewIssuePage.getIssueMenu()
                .invokeAdmin(AdminAction.ADD_FIELD);

        fieldWisherDialog.select(fieldName);
        assertThat(fieldWisherDialog.getNextButtonText(), equalTo("Edit"));
        fieldWisherDialog.clickNext();
        assertThat(viewIssuePage.isEditModalDialogVisibleFor(fieldName), equalTo(true));
    }

    @Test
    public void testAddExistingFieldAlreadyOnScreenWithValueFromViewIssuePage()
    {
        String fieldName = "AlreadyOnScreensWithValue";

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("TES-1");
        final FieldWisherDialog fieldWisherDialog = viewIssuePage.getIssueMenu()
                .invokeAdmin(AdminAction.ADD_FIELD);

        fieldWisherDialog.select(fieldName);
        assertThat(fieldWisherDialog.getNextButtonText(), equalTo("Edit"));
        fieldWisherDialog.clickNext();

        // 10001 is the ID of this field
        assertThat(viewIssuePage.isInlineEditVisibleFor(10001), equalTo(true));
    }

    private void assertScreensForProjectTes(final String fieldName)
    {
        //Field is added to ScreeA and ScreenB (the screens associated with project TEST) but not the default screen
        //(the screen associated with the other projects).
        assertThat(getFieldsOnScreen("ScreenA"), hasItem(fieldName));
        assertThat(getFieldsOnScreen("ScreenB"), hasItem(fieldName));
        assertThat(getFieldsOnScreen("Default Screen"), not(hasItem(fieldName)));
    }

    private List<String> getFieldsOnScreen(final String screen)
    {
        return backdoor.screensControl().getScreen(screen).getFields();
    }

    private String assertAddLabelsField(TypeSelectionCustomFieldDialog dialog, String postfix, Function<ConfigureFieldDialog, ?> next)
    {
        final String name = "labels " + postfix;
        final String description = "description " + postfix;

        next.apply(dialog.select(labelsName).next()
                .name(name).description(description));

        final List<CustomFieldResponse> customFields = backdoor.customFields().getCustomFields();
        final CustomFieldResponse response = Iterables.find(customFields, new Predicate<CustomFieldResponse>()
        {
            @Override
            public boolean apply(final CustomFieldResponse input)
            {
                return name.equals(input.name);
            }
        });
        assertThat(response.type, equalTo(labelsTypeKey));
        assertThat(response.description, equalTo(description));
        assertThat(response.searcher, equalTo(labelsSearcherKey));

        return name;
    }

    private String assertAddOptionsField(TypeSelectionCustomFieldDialog dialog, String postfix, Function<ConfigureFieldDialog, ?> next)
    {
        final String name = "labels " + postfix;
        final String description = "description " + postfix;

        next.apply(dialog.select(multiCheckboxesName).next()
                .name(name).description(description).addOption("One").addOption("Two"));

        final List<CustomFieldResponse> customFields = backdoor.customFields().getCustomFields(true);
        final CustomFieldResponse response = Iterables.find(customFields, new Predicate<CustomFieldResponse>()
        {
            @Override
            public boolean apply(final CustomFieldResponse input)
            {
                return name.equals(input.name);
            }
        });
        assertThat(response.type, equalTo(multiCheckboxesTypeKey));
        assertThat(response.description, equalTo(description));
        assertThat(response.searcher, equalTo(multiCheckboxesSearcherKey));

        final CustomFieldConfig config = Iterables.find(response.getConfig(), isGlobalPredicate());
        assertThat(config, not(nullValue()));
        assertThat(transform(config.getOptions(), getNameFunction()), contains("One", "Two"));

        return name;
    }
}
