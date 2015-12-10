package com.atlassian.jira.webtest.webdriver.tests.admin.issue.types.schemes;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.IconPicker;
import com.atlassian.jira.pageobjects.config.ResetData;
import com.atlassian.jira.pageobjects.elements.AvatarId;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.AddIssueType;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.AddNewIssueTypeToSchemeDialog;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.EditIssueTypeSchemePage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.ManageIssueTypeSchemePage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.ViewIssueTypesPage;
import com.atlassian.jira.pageobjects.util.UserSessionHelper;
import com.atlassian.jira.pageobjects.websudo.JiraSudoFormDialog;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudo;
import com.atlassian.jira.pageobjects.xsrf.XsrfDialog;
import com.atlassian.jira.testkit.client.IssueTypeControl;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.pageobjects.pages.admin.issuetype.ManageIssueTypeSchemePage.IssueTypeSchemeRow;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.0.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.ISSUE_TYPES })
public class TestIssueTypeSchemes extends BaseJiraWebTest
{
    private static final String FIELD_URL = "iconurl";
    private static final String ISSUE_TYPE_BUG = "Bug";
    private static final String ISSUE_TYPE_IMPRO = "Improvement";
    private static final String ISSUE_TYPE_NEW = "New Feature";
    private static final String ISSUE_TYPE_TASK = "Task";
    private static final String FIELD_NAME = "name";

    private static void assertSubtasksDisabled(final AddNewIssueTypeToSchemeDialog addIssueTypeDialog)
    {
        assertFalse("Subtasks should be disabled", addIssueTypeDialog.isSubtasksEnabled());
    }

    private IssueTypeControl.IssueType complexType;
    private IssueTypeControl.IssueType simpleType;
    private IssueTypeControl.IssueType subStaskType;

    @Before
    public void setUp() throws Exception
    {
        final String imageUrl = ViewIssueTypesPage.IssueType.getDefaultIssueTypeAvatarId().asRelativeLinkForDefaultSize(Avatar.Type.ISSUETYPE);
        complexType = new IssueTypeControl.IssueType(null, "NewIssueType", "New Issue Type Description", imageUrl, false);
        simpleType = new IssueTypeControl.IssueType(null, "SimpleNewIssueType", null, imageUrl, false);
        subStaskType = new IssueTypeControl.IssueType(null, "Subtask", "With Description", imageUrl, true);
    }

    @Test
    public void cantAddIssueTypeToDefaultScheme()
    {
        final ManageIssueTypeSchemePage schemePage = jira.goTo(ManageIssueTypeSchemePage.class);
        final IssueTypeSchemeRow defaultRow = schemePage.getDefaultScheme();
        final EditIssueTypeSchemePage editIssueTypeSchemePage = defaultRow.editIssueTypeScheme();
        
        assertTrue(editIssueTypeSchemePage.isModifyingDefaultScheme());
        assertFalse(editIssueTypeSchemePage.canAddIssueType());
    }

    @ResetData
    @Test
    public void canAddIssueTypeToNewScheme()
    {
        backdoor.subtask().disable();

        final String newSchemeName = "newScheme";
        final String newSchemeDescription = "description";

        //Create a new scheme.
        final ManageIssueTypeSchemePage managePage = jira.goTo(ManageIssueTypeSchemePage.class);
        final EditIssueTypeSchemePage editPage = managePage.createNewScheme()
                .setName(newSchemeName).setDescription(newSchemeDescription);
        assertCanAddIssueTypes(editPage, newSchemeName, newSchemeDescription, null);
    }

    @ResetData
    @Test
    public void canAddIssueTypeToCopyScheme()
    {
        backdoor.subtask().disable();

        //Copy the default issue type scheme
        ManageIssueTypeSchemePage managePage = jira.goTo(ManageIssueTypeSchemePage.class);
        final IssueTypeSchemeRow defaultRow = managePage.getDefaultScheme();
        EditIssueTypeSchemePage editPage = defaultRow.copyIssueTypeScheme();

        assertCopyOfSchemeFor(defaultRow, editPage);

        //Change the name of the new scheme.
        String newSchemeName = "NewScheme";
        String newSchemeDescription = "Really new scheme";
        editPage.setName(newSchemeName).setDescription(newSchemeDescription);

        //Add A new Issue Type to the system and the current scheme.
        AddNewIssueTypeToSchemeDialog addIssueTypeDialog = editPage.createNewIssueType();

        assertSubtasksDisabled(addIssueTypeDialog);
        addIssueTypeDialog.setName(complexType.getName())
                .setDescription(complexType.getDescription());
        editPage = addIssueTypeDialog.submit();

        assertIssueTypeAddedTo(complexType, editPage);

        //Save the scheme and make sure the issue type has been persisted.
        managePage = editPage.submitSave();
        IssueTypeSchemeRow newScheme = managePage.getSchemeForName(newSchemeName);
        assertThat(newScheme.getName(), equalTo(newSchemeName));
        assertThat(newScheme.getDescription(), equalTo(newSchemeDescription));
        assertThat(newScheme.getIssueTypes(), Matchers.<String>hasItem(complexType.getName()));
        assertThat(newScheme.getDefaultIssueType(), equalTo(ISSUE_TYPE_BUG));

        newSchemeName = "YetAnotherNewScheme";
        newSchemeDescription = "";

        //Copy the scheme we just created.
        editPage = newScheme.copyIssueTypeScheme();

        assertCopyOfSchemeFor(newScheme, editPage);

        //Change the name of the scheme.
        editPage.setName(newSchemeName).setDescription(newSchemeDescription);

        //Add a new issue type.
        addIssueTypeDialog = editPage.createNewIssueType();
        assertSubtasksDisabled(addIssueTypeDialog);
        addIssueTypeDialog.setName(simpleType.getName());
        editPage = addIssueTypeDialog.submit();

        assertIssueTypeAddedTo(simpleType, editPage);

        //Add subtask issue type without saving the scheme.
        backdoor.subtask().enable();
        addIssueTypeDialog = editPage.createNewIssueType();
        assertTrue(addIssueTypeDialog.isSubtasksEnabled());
        addIssueTypeDialog.setName(subStaskType.getName())
                .setSubtask(subStaskType.isSubtask())
                .setDescription(subStaskType.getDescription());

        editPage = addIssueTypeDialog.submit();
        assertIssueTypeAddedTo(subStaskType, editPage);

        //Save the two new issue types.
        managePage = editPage.submitSave();
        newScheme = managePage.getSchemeForName(newSchemeName);
        assertThat(newScheme.getName(), equalTo(newSchemeName));
        assertThat(newScheme.getDescription(), Matchers.nullValue());
        assertThat(newScheme.getIssueTypes(), hasItems(subStaskType.getName(), simpleType.getName()));
        assertThat(newScheme.getDefaultIssueType(), equalTo(ISSUE_TYPE_BUG));
    }

    @ResetData
    @Test
    public void canAddIssueTypeToExistingScheme()
    {
        backdoor.subtask().disable();

        ManageIssueTypeSchemePage managePage = jira.goTo(ManageIssueTypeSchemePage.class);

        //Copy scheme so that we can edit it.
        final IssueTypeSchemeRow defaultRow = managePage.getDefaultScheme();
        EditIssueTypeSchemePage editPage = defaultRow.copyIssueTypeScheme();

        assertCopyOfSchemeFor(defaultRow, editPage);

        final String newSchemeName = "NewScheme";
        final String newSchemeDescription = "Really new scheme";
        editPage.setName(newSchemeName).setDescription(newSchemeDescription);
        managePage = editPage.submitSave();

        final IssueTypeSchemeRow newScheme = managePage.getSchemeForName(newSchemeName);
        assertThat(newScheme.getName(), equalTo(newSchemeName));
        assertThat(newScheme.getDescription(), equalTo(newSchemeDescription));
        assertThat(newScheme.getDefaultIssueType(), equalTo(ISSUE_TYPE_BUG));

        editPage = newScheme.editIssueTypeScheme();
        assertCanAddIssueTypes(editPage, newSchemeName, newSchemeDescription, ISSUE_TYPE_BUG);
    }

    @ResetData
    @Test
    public void errorsOnAddIssueType()
    {
        final String schemeName = "errorsOnAddIssueType";

        backdoor.subtask().disable();

        ManageIssueTypeSchemePage managePage = jira.goTo(ManageIssueTypeSchemePage.class);

        //Copy scheme so that we can edit it.
        final IssueTypeSchemeRow defaultRow = managePage.getDefaultScheme();
        EditIssueTypeSchemePage editPage = defaultRow.copyIssueTypeScheme();

        assertCopyOfSchemeFor(defaultRow, editPage);

        editPage.setName(schemeName);
        final AddNewIssueTypeToSchemeDialog addDialog = editPage.createNewIssueType();

        //No name should return error.
        addDialog.setName("").setDescription("Something").submitFail();
        assertThat(addDialog.getFormErrors(), hasEntry(FIELD_NAME, "You must specify a name."));

        addDialog.setName(ISSUE_TYPE_BUG).submitFail();
        assertThat(addDialog.getFormErrors(), hasEntry(FIELD_NAME, "An issue type with this name already exists."));

        addDialog.setName(simpleType.getName()).setDescription("");
        editPage = addDialog.submit();

        assertIssueTypeAddedTo(simpleType, editPage);

        managePage = editPage.submitSave();
        final IssueTypeSchemeRow newScheme = managePage.getSchemeForName(schemeName);
        assertThat(newScheme.getName(), equalTo(schemeName));
        assertThat(newScheme.getIssueTypes(), hasItems(simpleType.getName()));
        assertThat(newScheme.getDefaultIssueType(), equalTo(ISSUE_TYPE_BUG));
    }

    @ResetData
    @Test
    public void webSudoWorksInAddIssueTypeDialog()
    {
        final String name = "webSudoWorksInAddIssueTypeDialog";

        ManageIssueTypeSchemePage managePage = jira.goTo(ManageIssueTypeSchemePage.class);
        EditIssueTypeSchemePage editPage = managePage.getDefaultScheme().copyIssueTypeScheme();

        backdoor.websudo().enable();

        final UserSessionHelper userSessionHelper = pageBinder.bind(UserSessionHelper.class);
        userSessionHelper.clearWebSudo();

        //Websudo with fail
        JiraWebSudo formDialog = editPage.addIssueTypeAndBind(JiraSudoFormDialog.class, AddNewIssueTypeToSchemeDialog.ID);
        formDialog = formDialog.authenticateFail("otherpassword");
        formDialog.cancel(EditIssueTypeSchemePage.class);

        formDialog = editPage.addIssueTypeAndBind(JiraSudoFormDialog.class, AddNewIssueTypeToSchemeDialog.ID);
        editPage.setName(name);

        //Websudo with success.
        final AddNewIssueTypeToSchemeDialog addDialog = formDialog.authenticate(FunctTestConstants.ADMIN_PASSWORD, AddNewIssueTypeToSchemeDialog.class);
        addDialog.setName(simpleType.getName());
        editPage = addDialog.submit();

        assertIssueTypeAddedTo(simpleType, editPage);

        managePage = editPage.submitSave();
        final IssueTypeSchemeRow newScheme = managePage.getSchemeForName(name);
        assertThat(newScheme.getName(), equalTo(name));
        assertThat(newScheme.getIssueTypes(), hasItems(simpleType.getName()));
        assertThat(newScheme.getDefaultIssueType(), equalTo(ISSUE_TYPE_BUG));

        backdoor.websudo().disable();
    }

    @Test
    public void userSessionTimeoutInAddIssueTypeDialog()
    {
        final ManageIssueTypeSchemePage managePage = jira.goTo(ManageIssueTypeSchemePage.class);
        final EditIssueTypeSchemePage editPage = managePage.getDefaultScheme().copyIssueTypeScheme();

        final AddNewIssueTypeToSchemeDialog dialog = editPage.createNewIssueType();

        final UserSessionHelper userSessionHelper = pageBinder.bind(UserSessionHelper.class);
        userSessionHelper.invalidateSession();

        // make sure we get the "session expired" message
        final XsrfDialog xsrfPage = dialog.submitFail(XsrfDialog.class, AddNewIssueTypeToSchemeDialog.ID);
        assertTrue(xsrfPage.isSessionExpired());
        assertTrue(xsrfPage.hasParamaters());
        
        final JiraLoginPage jiraLoginPage = xsrfPage.login();
        jiraLoginPage.loginAsSystemAdminAndFollowRedirect(EditIssueTypeSchemePage.class);
    }

    @ResetData
    @Test
    public void dragAndDropIssueTypeSchemeCreate()
    {
        final ManageIssueTypeSchemePage managePage = jira.goTo(ManageIssueTypeSchemePage.class);
        final String schemeName = "dragAndDropIssueTypeSchemeCreate";
        final String schemeDescription = "dragAndDropIssueTypeSchemeCreate test";
        final EditIssueTypeSchemePage createPage = managePage.createNewScheme()
                .setName(schemeName).setDescription(schemeDescription);

        //Initial state is empty for new.
        assertThat(createPage.getDefaultIssueType(), nullValue());
        assertThat(createPage.getEnabledDefaultOptions(), isEmpty());
        assertThat(createPage.getSelectedIssueTypes(), isEmpty());
        assertThat(createPage.getAvailableIssueTypes(), containsOnly(ISSUE_TYPE_BUG, ISSUE_TYPE_IMPRO, ISSUE_TYPE_NEW, ISSUE_TYPE_TASK));

        assertDragAndDrop(schemeName, schemeDescription, createPage);
    }

    @ResetData
    @Test
    public void dragAndDropIssueTypeSchemeCopy()
    {
        final ManageIssueTypeSchemePage managePage = jira.goTo(ManageIssueTypeSchemePage.class);
        final String schemeName = "dragAndDropIssueTypeSchemeCopy";
        final String schemeDescription = "dragAndDropIssueTypeSchemeCopy test";

        EditIssueTypeSchemePage copyPage = managePage.getDefaultScheme().copyIssueTypeScheme();
        copyPage = copyPage.setName(schemeName).setDescription(schemeDescription);

        //Copied the default scheme which has all issue types select.
        assertThat(copyPage.getDefaultIssueType(), equalTo(ISSUE_TYPE_BUG));
        assertThat(copyPage.getEnabledDefaultOptions(), containsOnly(ISSUE_TYPE_BUG, ISSUE_TYPE_IMPRO, ISSUE_TYPE_NEW, ISSUE_TYPE_TASK));
        assertThat(copyPage.getSelectedIssueTypes(), containsOnly(ISSUE_TYPE_BUG, ISSUE_TYPE_IMPRO, ISSUE_TYPE_NEW, ISSUE_TYPE_TASK));
        assertThat(copyPage.getAvailableIssueTypes(), isEmpty());

        assertDragAndDrop(schemeName, schemeDescription, copyPage);
    }

    @ResetData
    @Test
    public void dragAndDropIssueTypeSchemeEdit()
    {
        ManageIssueTypeSchemePage managePage = jira.goTo(ManageIssueTypeSchemePage.class);
        final String schemeName = "dragAndDropIssueTypeSchemeEdit";
        final String schemeDescription = "dragAndDropIssueTypeSchemeEdit test";

        final EditIssueTypeSchemePage copyPage = managePage.getDefaultScheme().copyIssueTypeScheme();

        managePage = copyPage.setName(schemeName).setDescription(schemeDescription).submitSave();
        assertDragAndDrop(schemeName, schemeDescription, managePage.getSchemeForName(schemeName).editIssueTypeScheme());
    }

    @ResetData
    @Test
    public void testErrors()
    {
        ManageIssueTypeSchemePage managePage = jira.goTo(ManageIssueTypeSchemePage.class);
        EditIssueTypeSchemePage copyPage = managePage.getDefaultScheme().copyIssueTypeScheme();

        copyPage = copyPage.setName("").submitSaveWithError();
        assertThat(copyPage.getFormErrors(), hasEntry(FIELD_NAME, "You must enter a valid name."));
        assertThat(copyPage.getGlobalErrors(), isEmpty());

        final String validNewName = "validNewName";
        copyPage = copyPage.removeAllIssueTypes().setName(validNewName).submitSaveWithError();
        assertThat(copyPage.getGlobalErrors(), hasItem("You must select at least one option"));
        assertTrue(copyPage.getFormErrors().isEmpty());

        managePage = copyPage.selectAllIssueTypes().submitSave();

        copyPage = managePage.createNewScheme().setName(validNewName);
        copyPage = copyPage.selectAllIssueTypes().submitSaveWithError();
        assertThat(copyPage.getGlobalErrors(), isEmpty());
        assertThat(copyPage.getFormErrors(), hasEntry(FIELD_NAME, "This name already exists please choose another one"));
    }

    @ResetData
    @Test
    public void resetButtonRevertsChanges()
    {
        final String name = "Something";

        ManageIssueTypeSchemePage managePage = jira.goTo(ManageIssueTypeSchemePage.class);
        EditIssueTypeSchemePage copyPage = managePage.getDefaultScheme().copyIssueTypeScheme();

        final String origDefaultIssueType = copyPage.getDefaultIssueType();
        final List<String> selectedIssueTypes = copyPage.getSelectedIssueTypes();
        final List<String> availableIssueTypes = copyPage.getAvailableIssueTypes();

        managePage = copyPage.setName(name).setDescription("").submitSave();
        copyPage = managePage.getSchemeForName(name).editIssueTypeScheme();
        copyPage.removeAllIssueTypes().selectIssueType(ISSUE_TYPE_NEW).setDefaultIssueType(ISSUE_TYPE_NEW);
        copyPage.setName("Some Bad Kind Of Name").setDescription("This is yet another description.");

        copyPage = copyPage.reset();

        assertThat(copyPage.getDefaultIssueType(), equalTo(origDefaultIssueType));
        assertThat(copyPage.getEnabledDefaultOptions(), containsOnly(selectedIssueTypes));
        assertThat(copyPage.getSelectedIssueTypes(), containsOnly(selectedIssueTypes));
        assertThat(copyPage.getAvailableIssueTypes(), containsOnly(availableIssueTypes));
    }

    private void assertDragAndDrop(final String schemeName, final String schemeDescription, EditIssueTypeSchemePage editPage)
    {
        //Make sure remove all works.
        editPage = editPage.removeAllIssueTypes();
        assertThat(editPage.getDefaultIssueType(), nullValue());
        assertThat(editPage.getEnabledDefaultOptions(), isEmpty());
        assertThat(editPage.getSelectedIssueTypes(), isEmpty());
        assertThat(editPage.getAvailableIssueTypes(), containsOnly(ISSUE_TYPE_BUG, ISSUE_TYPE_IMPRO, ISSUE_TYPE_NEW, ISSUE_TYPE_TASK));

        //Make sure add all works.
        editPage = editPage.selectAllIssueTypes();
        assertThat(editPage.getDefaultIssueType(), nullValue());
        assertThat(editPage.getEnabledDefaultOptions(), containsOnly(ISSUE_TYPE_BUG, ISSUE_TYPE_IMPRO, ISSUE_TYPE_NEW, ISSUE_TYPE_TASK));
        assertThat(editPage.getSelectedIssueTypes(), containsOnly(ISSUE_TYPE_BUG, ISSUE_TYPE_IMPRO, ISSUE_TYPE_NEW, ISSUE_TYPE_TASK));
        assertThat(editPage.getAvailableIssueTypes(), isEmpty());

        //Add bug. The default should not change.
        editPage = editPage.removeAllIssueTypes().selectIssueType(ISSUE_TYPE_BUG);
        assertThat(editPage.getEnabledDefaultOptions(), containsOnly(ISSUE_TYPE_BUG));
        assertThat(editPage.getSelectedIssueTypes(), containsOnly(ISSUE_TYPE_BUG));
        assertThat(editPage.getAvailableIssueTypes(), containsOnly(ISSUE_TYPE_IMPRO, ISSUE_TYPE_NEW, ISSUE_TYPE_TASK));
        assertThat(editPage.getDefaultIssueType(), nullValue());

        //Set the default to bug.
        editPage = editPage.setDefaultIssueType(ISSUE_TYPE_BUG);
        assertThat(editPage.getDefaultIssueType(), equalTo(ISSUE_TYPE_BUG));

        //Removing bug should also reset the default.
        editPage = editPage.removeIssueType(ISSUE_TYPE_BUG);
        assertThat(editPage.getEnabledDefaultOptions(), isEmpty());
        assertThat(editPage.getSelectedIssueTypes(), isEmpty());
        assertThat(editPage.getAvailableIssueTypes(), containsOnly(ISSUE_TYPE_BUG, ISSUE_TYPE_IMPRO, ISSUE_TYPE_NEW, ISSUE_TYPE_TASK));
        assertThat(editPage.getDefaultIssueType(), nullValue());

        //Add a new issue type.
        final String reallyNewIssueType = "ReallyNewIssueType";
        editPage = editPage.createNewIssueType().setName(reallyNewIssueType).submit(EditIssueTypeSchemePage.class);
        assertThat(editPage.getEnabledDefaultOptions(), containsOnly(reallyNewIssueType));
        assertThat(editPage.getSelectedIssueTypes(), containsOnly(reallyNewIssueType));
        assertThat(editPage.getAvailableIssueTypes(), containsOnly(ISSUE_TYPE_BUG, ISSUE_TYPE_IMPRO, ISSUE_TYPE_NEW, ISSUE_TYPE_TASK));
        assertThat(editPage.getDefaultIssueType(), nullValue());

        //Select "New Feature" issue type.
        editPage = editPage.selectIssueType(ISSUE_TYPE_NEW);
        assertThat(editPage.getEnabledDefaultOptions(), containsOnly(ISSUE_TYPE_NEW, reallyNewIssueType));
        assertThat(editPage.getSelectedIssueTypes(), containsOnly(ISSUE_TYPE_NEW, reallyNewIssueType));
        assertThat(editPage.getAvailableIssueTypes(), containsOnly(ISSUE_TYPE_BUG, ISSUE_TYPE_IMPRO, ISSUE_TYPE_TASK));
        assertThat(editPage.getDefaultIssueType(), nullValue());

        //Set the default and submit the form to create the scheme.
        final ManageIssueTypeSchemePage schemePage = editPage.setDefaultIssueType(reallyNewIssueType).submitSave();
        final IssueTypeSchemeRow schemeForName = schemePage.getSchemeForName(schemeName);

        //Assert the file state of the scheme.
        assertThat(schemeForName.getName(), equalTo(schemeName));
        assertThat(schemeForName.getDescription(), equalTo(schemeDescription));
        assertThat(schemeForName.getIssueTypes(), containsOnly(ISSUE_TYPE_NEW, reallyNewIssueType));
        assertThat(schemeForName.getDefaultIssueType(), equalTo(reallyNewIssueType));
    }

    private void assertCanAddIssueTypes(EditIssueTypeSchemePage editPage, final String newSchemeName, final String newSchemeDescription, final String defaultIssueType)
    {
        final ManageIssueTypeSchemePage managePage;
        final IssueTypeSchemeRow newScheme;

        //Add A new Issue Type.
        AddNewIssueTypeToSchemeDialog addIssueTypeDialog = editPage.createNewIssueType();

        assertSubtasksDisabled(addIssueTypeDialog);
        addIssueTypeDialog.setName(complexType.getName())
                .setDescription(complexType.getDescription());
        editPage = addIssueTypeDialog.submit();

        assertIssueTypeAddedTo(complexType, editPage);

        //Add a second new issue type.
        addIssueTypeDialog = editPage.createNewIssueType();
        assertSubtasksDisabled(addIssueTypeDialog);
        addIssueTypeDialog.setName(simpleType.getName());
        editPage = addIssueTypeDialog.submit();

        assertIssueTypeAddedTo(simpleType, editPage);

        //Add a third new issue type.
        backdoor.subtask().enable();
        addIssueTypeDialog = editPage.createNewIssueType();
        assertTrue(addIssueTypeDialog.isSubtasksEnabled());
        addIssueTypeDialog.setName(subStaskType.getName())
                .setSubtask(subStaskType.isSubtask())
                .setDescription(subStaskType.getDescription());

        editPage = addIssueTypeDialog.submit();
        assertIssueTypeAddedTo(subStaskType, editPage);

        //Save the three new issue types to the scheme.
        managePage = editPage.submitSave();
        newScheme = managePage.getSchemeForName(newSchemeName);
        assertThat(newScheme.getName(), equalTo(newSchemeName));
        assertThat(newScheme.getDescription(), equalTo(newSchemeDescription));
        assertThat(newScheme.getIssueTypes(), hasItems(complexType.getName(), subStaskType.getName(), simpleType.getName()));
        assertThat(newScheme.getDefaultIssueType(), equalTo(defaultIssueType));
    }

    private void assertCopyOfSchemeFor(final IssueTypeSchemeRow row, final EditIssueTypeSchemePage editPage)
    {
        assertTrue(editPage.canAddIssueType());
        assertThat(editPage.getName(), equalTo("Copy of " + row.getName()));
        assertThat(editPage.getDescription(), equalTo(row.getDescription()));
        assertThat(editPage.getDefaultIssueType(), equalTo(row.getDefaultIssueType()));
    }

    private void assertIssueTypeAddedTo(final IssueTypeControl.IssueType type, final EditIssueTypeSchemePage editPage)
    {
        final List<IssueTypeControl.IssueType> allIssueTypes = backdoor.issueType().getIssueTypes();
        assertThat(allIssueTypes, Matchers.<IssueTypeControl.IssueType>hasItem(issueType(type)));
        assertThat(editPage.getSelectedIssueTypes(), Matchers.<String>hasItem(type.getName()));
        assertThat(editPage.getEnabledDefaultOptions(), Matchers.<String>hasItem(type.getName()));
    }

    private static IssueTypeMatcher issueType(final IssueTypeControl.IssueType expected)
    {
        return new IssueTypeMatcher(expected);
    }
    
    private static <T extends Collection<?>> CollectionContainsOnlyMatcher<T> containsOnly(final Collection<?> elements)
    {
        return new CollectionContainsOnlyMatcher<T>(ImmutableList.copyOf(elements));
    }
    
    private static <T extends Collection<?>> CollectionContainsOnlyMatcher<T> containsOnly(final Object... elements)
    {
        return new CollectionContainsOnlyMatcher<T>(Arrays.asList(elements));
    }

    private static <T extends Collection<?>> CollectionIsEmptyMatcher<T> isEmpty()
    {
        return new CollectionIsEmptyMatcher<T>();
    }
    
    private static class CollectionIsEmptyMatcher<T extends Collection<?>> extends BaseMatcher<T>
    {
        @Override
        public boolean matches(final Object o)
        {
            return o instanceof Collection<?> && ((Collection) o).isEmpty();
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendValue("<empty collection>");
        }
    }
    
    private static class CollectionContainsOnlyMatcher<T extends Collection<?>> extends BaseMatcher<T>
    {
        private final Collection<?> expectedItem;

        private CollectionContainsOnlyMatcher(final Collection<?> expectedItem)
        {
            this.expectedItem = expectedItem;
        }

        @Override
        public boolean matches(final Object o)
        {
            if (o instanceof Collection<?>)
            {
                final Collection<?> actual = (Collection<?>) o;
                for (final Object element : actual)
                {
                    if (!expectedItem.contains(element))
                    {
                        return false;
                    }
                }
                return true;
            }
            else
            {
                return false;
            }
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendValue(expectedItem);
        }
    }
    
    private static class IssueTypeMatcher extends BaseMatcher<IssueTypeControl.IssueType>
    {
        private final IssueTypeControl.IssueType expected;

        private IssueTypeMatcher(final IssueTypeControl.IssueType expected)
        {
            this.expected = expected;
        }

        @Override
        public boolean matches(final Object o)
        {
            if (o instanceof IssueTypeControl.IssueType)
            {
                final IssueTypeControl.IssueType actual = (IssueTypeControl.IssueType) o;
                return StringUtils.equals(actual.getName(), expected.getName()) &&
                        StringUtils.equals(actual.getDescription(), expected.getDescription()) &&
                        iconUrlAsAvatarsEqual(actual) &&
                        actual.isSubtask() == expected.isSubtask();
            }
            else
            {
                return false;
            }
        }

        private boolean iconUrlAsAvatarsEqual(final IssueTypeControl.IssueType actual) {
            AvatarId expectedAvatarId = AvatarId.fromImageLink(expected.getIconUrl());
            AvatarId actualAvatarId = AvatarId.fromImageLink(actual.getIconUrl());

            return expectedAvatarId.equals(actualAvatarId);
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendValue(expected);
        }
    }
}
