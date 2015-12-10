package com.atlassian.jira.webtests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.AdministrationImpl;
import com.atlassian.jira.functest.framework.DefaultFuncTestHttpUnitOptions;
import com.atlassian.jira.functest.framework.Form;
import com.atlassian.jira.functest.framework.FormImpl;
import com.atlassian.jira.functest.framework.FuncTestWebClientListener;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.HtmlPage;
import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.LocatorFactoryImpl;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.NavigationImpl;
import com.atlassian.jira.functest.framework.Parser;
import com.atlassian.jira.functest.framework.ParserImpl;
import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.functest.framework.assertions.AssertionsImpl;
import com.atlassian.jira.functest.framework.assertions.IssueTableAssertions;
import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.assertions.TextAssertionsImpl;
import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.dump.TestCaseDumpKit;
import com.atlassian.jira.functest.framework.dump.TestInformationKit;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.log.LogOnBothSides;
import com.atlassian.jira.functest.framework.setup.JiraSetupInstanceHelper;
import com.atlassian.jira.functest.framework.util.IssueTableClient;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.ResourceRef;
import com.atlassian.jira.testkit.beans.CustomFieldResponse;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import com.atlassian.jira.testkit.client.dump.FuncTestTimer;
import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.testkit.client.log.FuncTestLoggerImpl;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.ComponentClient;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.jira.testkit.client.restclient.ProjectClient;
import com.atlassian.jira.testkit.client.restclient.ProjectRoleClient;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.testkit.client.restclient.VersionClient;
import com.atlassian.jira.webtests.table.ImageCell;
import com.atlassian.jira.webtests.table.SimpleCell;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import junit.framework.AssertionFailedError;
import net.sourceforge.jwebunit.WebTester;

import static com.atlassian.jira.webtests.LegacyProjectPermissionKeyMapping.getKey;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Base web test for JIRA. Extend this to make a functional test.
 *
 * @see <a href=https://extranet.atlassian.com/display/JIRADEV/JIRA+Functional+Tests+Developer+Guide}>Func Test
 *      Developer Guide</a> for details
 * @deprecated This is a legacy class that evolved as a result of a 'stuff every util into the base class' approach. It
 *             is probably one of the fattest classes in the JIRA code base. It has been replaced by {@link
 *             com.atlassian.jira.functest.framework.FuncTestCase} that attempts to separate its various
 *             responsibilities into multiple helper classes. <b>Always</b> use {@link
 *             com.atlassian.jira.functest.framework.FuncTestCase} for new func tests and attempt to migrate old tests
 *             to use it whenever possible.
 */
@SuppressWarnings ({ "deprecation", "JavaDoc" })
@Deprecated
public abstract class JIRAWebTest extends AbstractAtlassianWebTestCase implements EnvironmentAware, FunctTestConstants
{
    /**
     * Use this field to access the {@link com.atlassian.jira.webtests.util.JIRAEnvironmentData} in play
     */
    protected JIRAEnvironmentData environmentData;

    /**
     * Use this field to access the {@link Navigation} helper in play
     */
    protected Navigation navigation;

    /**
     * Used to set form values in tests.
     */
    protected Form form;

    /**
     * Used to find out about the current HTML page that the test is on.
     */
    protected HtmlPage page;

    /**
     * Use this field to access the {@link com.atlassian.jira.functest.framework.Parser} helper in play
     */
    protected Parser parse;

    /**
     * Use this field to access the {@link Administration} helper in play
     */
    protected Administration administration;

    /**
     * Use this field to access the {@link com.atlassian.jira.functest.framework.backdoor.Backdoor} helper in play,
     * which can make sly RPCs to the server.
     */
    protected Backdoor backdoor;

    protected IssueTableClient issueTableClient;

    /**
     * Use this field to access the {@link Assertions} helper in play
     */
    protected Assertions assertions;

    /**
     * Use this field to access the {@link com.atlassian.jira.functest.framework.assertions.TextAssertions} helper in
     * play
     */
    protected TextAssertions text;

    /**
     * Use this field to access the {@link com.atlassian.jira.functest.framework.assertions.IssueTableAssertions} helper
     * in play
     */
    protected IssueTableAssertions issueTableAssertions;

    /**
     * Use this field to access the {@link com.atlassian.jira.testkit.client.log.FuncTestLogger} in play
     */
    protected FuncTestLogger log;

    /**
     * Use this field to access the {@link com.atlassian.jira.functest.framework.LocatorFactory} in play
     */
    protected LocatorFactory locator;

    private FuncTestWebClientListener webClientListener;

    private IssueClient issueClient;

    private long startTime;

    private StopWatch stopWatch = new StopWatch();

    private static final int FIELD_TABLE_FIELD_NAME_COLUMN_INDEX = 0;
    private static final int FIELD_TABLE_OPERATIONS_COLUMN_INDEX = 2;
    private static final String HIDE_FIELD_OPERATION_NAME = "Hide";
    private static final String SHOW_FIELD_OPERATION_NAME = "Show";
    private static final String OPTIONAL_FIELD_OPERATION_NAME = "Optional";


    public static final String FIELD_SCOPE_GLOBAL = "global";
    public static final String PAGE_ISSUE_TYPE_SCREEN_SCHEMES = "/secure/admin/ViewIssueTypeScreenSchemes.jspa";

    public static final String PAGE_ENTERPRISE_FIELD_CONFIGURATIONS = "/secure/admin/ViewFieldLayouts.jspa";

    public static final String PAGE_USER_BROWSER = "/secure/admin/user/UserBrowser.jspa";
    public static final String PAGE_NOT_STANDARD_VIEW_FIELD_SCREEN_SCHEMES = "/secure/admin/ViewFieldScreenSchemes.jspa";

    private static final String PAGE_TIME_TRACKING = "/secure/admin/jira/TimeTrackingAdmin!default.jspa";
    private static final String PAGE_LINK_TYPES = "/secure/admin/jira/ViewLinkTypes.jspa";
    private static final String PAGE_CUSTOM_FIELDS = "/secure/admin/ViewCustomFields.jspa";

    public static final float JDK_1_5_VERSION = 1.5f;

    public static final String BULK_TRANSITION_ELEMENT_NAME = "wftransition";

    private final String EVENT_TYPE_TABLE = "eventTypeTable";
    private final int EVENT_TYPE_TABLE_NAME_COL = 0;

    private static Set<String> copiedFiles = Collections.synchronizedSet(new HashSet<String>());

    public JIRAWebTest(String name)
    {
        this(name, new LocalTestEnvironmentData());
    }

    public JIRAWebTest(String name, JIRAEnvironmentData environmentData)
    {
        super(name);
        this.environmentData = environmentData;
        this.tester = new WebTester();
        this.backdoor = new Backdoor(environmentData);
        this.issueTableClient = new IssueTableClient(environmentData);
    }

    public WebTester getTester()
    {
        return tester;
    }

    public HtmlPage getPage()
    {
        if (page == null)
        {
            page = new HtmlPage(tester);
        }
        return page;
    }

    public JIRAEnvironmentData getEnvironmentData()
    {
        if (environmentData == null)
        {
            environmentData = new LocalTestEnvironmentData();
        }

        return environmentData;
    }

    public void setEnvironmentData(JIRAEnvironmentData environmentData)
    {
        this.environmentData = environmentData;
    }

    public Administration getAdministration()
    {
        if (administration == null)
        {
            administration = new AdministrationImpl(getTester(), getEnvironmentData(), getNavigation(), getAssertions());
        }
        return administration;
    }

    public Backdoor getBackdoor()
    {
        return administration.backdoor();
    }

    protected Assertions getAssertions()
    {
        if (assertions == null)
        {
            assertions = new AssertionsImpl(getTester(), getEnvironmentData(), getNavigation(), getLocatorFactory());
        }
        return assertions;
    }

    public Navigation getNavigation()
    {
        if (navigation == null)
        {
            navigation = new NavigationImpl(getTester(), getEnvironmentData());
        }
        return navigation;
    }

    public FuncTestWebClientListener getWebClientListener()
    {
        return webClientListener;
    }

    public String getRedirect()
    {
        URL url = getDialog().getResponse().getURL();
        String queryString = url.getQuery() != null ? '?' + url.getQuery() : "";
        return url.getPath() + queryString;
    }

    public void assertRedirect(String path)
    {
        assertEquals(path, getRedirect());
    }

    public void assertRedirectPath(String s)
    {
        URL url = getDialog().getResponse().getURL();
        assertEquals(s, url.getPath());
    }


    /**
     * Logs a message in the atlassian-jira.log on the server
     */
    protected void jiraLog(String message, boolean testStarted)
    {
        try
        {
            backdoor.getTestkit().logControl().info(message);
        }
        catch (RuntimeException ignored)
        {
        }
    }

    public void dumpScreen(String filename)
    {
        try
        {
            PrintStream printStream = new PrintStream(new FileOutputStream(new File(getEnvironmentData().getWorkingDirectory().getAbsolutePath() + FS + filename + HTM)));
            dumpResponse(printStream);
            printStream.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Login as the user with the given username and identical password.
     *
     * @param usernameAndPassword the username and password duh.
     * @deprecated Use {@link com.atlassian.jira.functest.framework.Navigation#login(String)} ()} instead.
     */
    @Deprecated
    public void login(String usernameAndPassword)
    {
        login(usernameAndPassword, usernameAndPassword);
    }

    /**
     * @deprecated Use {@link Navigation#login(String, String)} instead.
     */
    @Deprecated
    public void login(String username, String password)
    {
        getNavigation().login(username, password);
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.Navigation#logout()} instead.
     */
    @Deprecated
    public void logout()
    {
        getNavigation().logout();
    }

    /**
     * This is used to allow access to in-accessible methods by overriding their accessible level to be available
     * publicly. For usage see {@link #getFormElementValue(String, String)}
     *
     * @param subject Object to invoke the method from
     * @param methodName the name of the method to invoke
     * @param methodArgs the actual arguments to pass in to the method call
     * @return output of the method call
     */
    private Object invoke(Object subject, String methodName, Object[] methodArgs)
    {
        //determine the argument class types from the actual method arguments
        Class[] params = new Class[methodArgs.length];
        for (int i = 0; i < methodArgs.length; i++)
        {
            params[i] = methodArgs[i].getClass();
        }

        //look for the method to invoke, if its not in the current class, look in the super class
        Method method = null;
        Class clazz = subject.getClass();
        while (method == null)
        {
            try
            {
                method = clazz.getDeclaredMethod(methodName, params);
            }
            catch (NoSuchMethodException e)
            {
                clazz = clazz.getSuperclass();
            }
        }
        //override the accessibility to be publicly available
        method.setAccessible(true);
        try
        {
            return method.invoke(subject, methodArgs);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the {@link com.meterware.httpunit.FormControl} with elementId in the specified form.
     *
     * @param form name of the form to look for the element
     * @param elementId the id of the element to get
     * @return {@link com.meterware.httpunit.FormControl} of the element
     */
    public Object getFormElement(String form, String elementId)
    {
        WebForm jiraform = null;
        try
        {
            jiraform = getDialog().getResponse().getFormWithName(form);
        }
        catch (SAXException se)
        {
            // lets try by id
        }

        if (jiraform == null)
        {
            try
            {
                jiraform = getDialog().getResponse().getFormWithID(form);
            }
            catch (SAXException e)
            {
                throw new RuntimeException(e);
            }
        }

        return invoke(jiraform, "getControlWithID", new String[] { elementId });
    }

    public Map getFormParameters(String form)
    {
        try
        {
            WebForm jiraform = getDialog().getResponse().getFormWithName(form);
            if (jiraform == null)
            {
                jiraform = getDialog().getResponse().getFormWithID(form);
            }
            return (Map) invoke(jiraform, "getFormParameters", new Object[0]);
        }
        catch (SAXException se)
        {
            throw new RuntimeException(se);
        }
    }

    public Object getFormParameter(String form, String elementName)
    {
        Map params = getFormParameters(form);
        Object formParameter = params.get(elementName);
        return formParameter;
    }

    public String[] getFormParameterValues(String form, String elementName)
    {
        return (String[]) invoke(getFormParameter(form, elementName), "getValues", new Object[0]);
    }

    /**
     * Get the single value of the field with elementId inside the form 'form'
     *
     * @param form form the field resides in
     * @param elementId id of the field
     * @return the value of the field
     */
    public String getFormElementValue(String form, String elementId)
    {
        Object formControl = getFormElement(form, elementId);
        return (String) invoke(formControl, "getValueAttribute", new Object[0]);
    }

    /**
     * Checks if a form element has the disabled flag set.
     *
     * @param form form the field resides in
     * @param elementId id of the field
     * @return the value of the field
     */
    public boolean isFormElementDisabled(String form, String elementId)
    {
        Object formControl = getFormElement(form, elementId);
        return (Boolean) invoke(formControl, "isDisabled", new Object[0]);
    }

    /**
     * Get the values of the field with elementId inside the form 'form'
     *
     * @param form form the field resides in
     * @param elementId id of the field
     * @return the values of the field
     */
    public String[] getFormElementValues(String form, String elementId)
    {
        Object formControl = getFormElement(form, elementId);
        return (String[]) invoke(formControl, "getValues", new Object[0]);
    }

    /**
     * Asserts that the expectedValue has been selected for the form element with Id elementId. use {@link
     * #assertOptionSelectedById(String, String)} to check if an id has been selected
     *
     * @param elementId id of the option (select/radio) field
     * @param expectedValue the expected value (the display name) to be selected
     */
    public void assertOptionSelected(String elementId, String expectedValue)
    {
        List selectedOptionIds = Arrays.asList(getDialog().getForm().getParameterValues(elementId));
        String expectedValueId;
        String[] optionValues = getDialog().getOptionsFor(elementId);//display names
        for (int i = 0; i < optionValues.length; i++)
        {
            String optionValue = optionValues[i];
            if (optionValue.equals(expectedValue))
            {
                expectedValueId = getDialog().getOptionValuesFor(elementId)[i];
                assertTrue("Expected option '" + expectedValue + "' for element '" + elementId + "' was not selected", selectedOptionIds.contains(expectedValueId));
                return;
            }
        }

        fail("Expected option value: '" + expectedValue + "' is not a selected value option for '" + elementId + "'");
    }

    /**
     * A more robust version of {@link #assertOptionsEqual(String, String[])}. This version is different in that, it
     * does not care about the ordering (So its workable with different JDKs or when you dont know/care about the
     * order).
     */
    public void assertOptionsEqualIgnoreOrder(String selectName, String[] expectedOptions)
    {
        List expectedOptionsList = Arrays.asList(expectedOptions);
        List actualOptionsList = Arrays.asList(getDialog().getOptionsFor(selectName));
        assertTrue("The expected options '" + expectedOptionsList + "' does not equal '" + actualOptionsList + "'", expectedOptionsList.containsAll(actualOptionsList) && actualOptionsList.containsAll(expectedOptionsList));
    }

    /**
     * Asserts that the expectedId has been selected for the form element with Id elementId. use {@link
     * #assertOptionSelected(String, String)} to check if a specific value has been selected
     *
     * @param elementId id of the option (select/radio) field
     * @param expectedId the expected id to be selected
     */
    public void assertOptionSelectedById(String elementId, String expectedId)
    {
        List selectedOptionIds = Arrays.asList(getDialog().getForm().getParameterValues(elementId));
        assertTrue("Expected selected option '" + expectedId + "' for element '" + elementId + "'.", selectedOptionIds.contains(expectedId));
    }

    /**
     * Assert form element with fieldId has expectedValue
     *
     * @param fieldId id of the option (select/radio) field
     * @param expectedValue the expected value of the form
     */
    public void assertFormElementHasValue(String fieldId, String expectedValue)
    {
        assertFormElementHasValue("jiraform", fieldId, expectedValue);
    }

    /**
     * Assert form element with fieldId has expectedValue
     *
     * @param formNameOrId Name or Id of the form
     * @param fieldId id of the option (select/radio) field
     * @param expectedValue the expected value of the form
     */
    public void assertFormElementHasValue(String formNameOrId, String fieldId, String expectedValue)
    {
        assertEquals(expectedValue, getFormElementValue(formNameOrId, fieldId));
    }

    /**
     * Assert form element with fieldName has expectedValue
     *
     * @param fieldName name of the field
     * @param expectedValue the expected value of the field
     */
    public void assertFormElementWithNameHasValue(String fieldName, String expectedValue)
    {
        assertFormElementWithNameHasValue("jiraform", fieldName, expectedValue);
    }

    /**
     * Assert form element with fieldName has expectedValue
     *
     * @param formNameOrId Name or Id of the form
     * @param fieldName name of the field
     * @param expectedValue the expected value of the field
     */
    public void assertFormElementWithNameHasValue(String formNameOrId, String fieldName, String expectedValue)
    {
        String[] formParameterValues = getFormParameterValues(formNameOrId, fieldName);
        if (formParameterValues != null && formParameterValues.length > 0)
        {
            assertEquals(expectedValue, formParameterValues[0]);
        }
        else
        {
            fail("Field with name '" + fieldName + "' is null and does not have the expected value '" + expectedValue + "'");
        }
    }

    /**
     * Assert the form textarea has the expectedValue
     *
     * @param fieldId id of the textarea to check
     * @param expectedValue the expected value of the textarea
     */
    public void assertFormTextAreaHasValue(String fieldId, String expectedValue)
    {
        assertFormTextAreaHasValue("jiraform", fieldId, expectedValue);
    }

    /**
     * Assert the form textarea has the expectedValue
     *
     * @param formNameOrId Name or Id of the form
     * @param fieldId id of the textarea to check
     * @param expectedValue the expected value of the textarea
     */
    public void assertFormTextAreaHasValue(String formNameOrId, String fieldId, String expectedValue)
    {
        String[] values = getFormElementValues(formNameOrId, fieldId);
        if (values != null && values.length > 0)
        {
            assertEquals(values[0].trim(), expectedValue);
            return;
        }
        fail("no values found for '" + fieldId + "'");
    }

    /**
     * Note: this uses two http round-trips and can make tests slow.
     *
     * @deprecated Use {@link Navigation#gotoAdminSection(String)} passing the linkId of the admin page you want to go
     *             to.
     */
    @Deprecated
    public void clickOnAdminPanel(String adminsubject, String adminpage)
    {
        gotoAdmin();
        if (getDialog().isLinkPresent(adminpage))
        {
            clickLink(adminpage);
        }
        else
        {
            clickLink(adminsubject);
        }
    }

    /**
     * Adds a project, or if a project with that name exists, does almost nothing. Choose a project name that will not
     * clash with operational links on the page such as "View Projects" or "Add".
     *
     * @param name the name of the project.
     * @param key the project key.
     * @param lead the username of the project lead.
     * @return the project id.
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.Project#addProject(String, String, String)}
     *             instead.
     */
    @Deprecated
    public long addProject(String name, String key, String lead)
    {
        return administration.project().addProject(name, key, lead);
    }

    /**
     * Delete project with the given name
     *
     * @param project the project name.
     */
    public void deleteProject(String project)
    {
        final Project projectByName = getProjectByName(project);
        gotoPage("/secure/admin/DeleteProject!default.jspa?pid=" + projectByName.id);
        assertTextPresent("Delete Project: " + project);
        submit("Delete");
    }

    private Project getProjectByName(String projectName)
    {
        ProjectClient projectClient = new ProjectClient(environmentData);

        final List<Project> projects = projectClient.getProjects();
        for (Project project : projects)
        {
            if (project.name.equals(projectName))
            {
                return project;
            }
        }
        return null;

    }

    private Component getComponentByname(String projectKey, String componentName)
    {
        ProjectClient projectClient = new ProjectClient(environmentData);
        final List<Component> components = projectClient.getComponents(projectKey);
        for (Component component : components)
        {
            if (component.name.equals(componentName))
            {
                return component;
            }
        }

        return null;

    }

    private Version getVersionByName(String projectKey, String versionName)
    {
        ProjectClient projectClient = new ProjectClient(environmentData);

        final List<Version> versions = projectClient.getVersions(projectKey);
        for (Version version : versions)
        {
            if (version.name.equals(versionName))
            {
                return version;
            }
        }
        return null;

    }

    /**
     * Adds a component with the given name (and no lead) to the projectName with the given name.
     *
     * @param projectName the name of the project.
     * @param name the name of the component.
     * @return the component id.
     */
    public String addComponent(String projectName, String name)
    {
        return addComponent(projectName, name, null);
    }

    /**
     * Adds a component with the given name and component lead to the projectName with the given name.
     *
     * @param projectName the name of the project.
     * @param name the name of the component.
     * @param componentLead the username of the lead for the component, may be null for none.
     * @return the component id.
     */
    public String addComponent(String projectName, String name, String componentLead)
    {
        final Project project = getProjectByName(projectName);
        final Component componentByname = getComponentByname(project.key, name);

        if (componentByname != null)
        {
            return "" + componentByname.id;
        }


        ComponentClient componentClient = new ComponentClient(environmentData);

        final Component component = componentClient.create(new Component().project(project.key).name(name).leadUserName(componentLead));

        return "" + component.id;

    }

    public void deleteComponent(String project, String name)
    {
        final Project projectObj = getProjectByName(project);
        final Component component = getComponentByname(projectObj.key, name);

        ComponentClient componentClient = new ComponentClient(environmentData);
        componentClient.delete("" + component.id);
    }

    public String addVersion(String project, String name, String description)
    {
        log("Adding version '" + name + "' to project '" + project + "'");

        VersionClient versionClient = new VersionClient(environmentData);
        final Version version = new Version();
        version.project(project).name(name).description(description);
        versionClient.create(version);
        return "" + version.id;

    }

    public void deleteVersion(Long id)
    {
        new VersionClient(environmentData).delete("" + id);
    }

    /**
     * add issue without getting its issue key
     */
    public void addIssueOnly(String project, String projectKey, String issueType, String summary, String priority, String[] components,
            String[] affectsVersions, String[] fixVersions, String assignTo, String environment, String description,
            String originalEstimate, String securityLevel, String dueDate)
    {
        log("Create Issue: Adding issue " + description);
        //make sure we're no longer in the admin section (where the create issue link is no longer displayed).
        if (tester.getDialog().isLinkPresent("leave_admin"))
        {
            tester.clickLink("leave_admin");
        }
        clickLink("create_link");
        assertTextPresent("Create Issue");
        if (project != null)
        {
            selectOption("pid", project);
        }
        if (issueType != null)
        {
            selectOption("issuetype", issueType);
        }
        setWorkingForm("issue-create");
        submit();

        assertTextPresent("CreateIssueDetails.jspa");
        setWorkingForm("issue-create");
        setFormElement("summary", summary);

        if (priority != null)
        {
            selectOption("priority", priority);
        }

        if (components != null && components.length > 0)
        {
            for (final String component : components)
            {
                selectMultiOption("components", component);
            }
        }
        if (affectsVersions != null && affectsVersions.length > 0)
        {
            for (final String affectsVersion : affectsVersions)
            {
                selectMultiOption("versions", affectsVersion);
            }
        }

        if (fixVersions != null && fixVersions.length > 0)
        {
            for (final String fixVersion : fixVersions)
            {
                selectMultiOption("fixVersions", fixVersion);
            }
        }

        if (assignTo != null)
        {
            selectOption("assignee", assignTo);
        }

        if (originalEstimate != null)
        {
            setFormElement("timetracking", originalEstimate);
        }

        if (environment != null)
        {
            setFormElement("environment", environment);
        }

        if (description != null)
        {
            setFormElement("description", description);
        }

        if (dueDate != null)
        {
            setFormElement("duedate", dueDate);
        }

        if (securityLevel != null)
        {
            selectOption("security", securityLevel);
        }
        assertSubmitButtonPresent("Create"); // Set via upgrade task 151 on old data
        submit();
    }

    /**
     * Adds an issue to the given project returning its key.
     *
     * @deprecated please use {@link com.atlassian.jira.functest.framework.navigation.IssueNavigation#createIssue(String,
     *             String, String)}
     */
    public String addIssue(String project, String projectKey, String issueType, String summary)
    {
        return addIssueViaRest(project, projectKey, issueType, summary, "Major", null, null, null);
    }

    /**
     * Adds an issue to the given project returning its key.
     */
    public String addIssue(String project, String projectKey, String issueType, String summary, String priority, String[] components,
            String[] affectsVersions, String[] fixVersions, String assignTo, String environment, String description,
            String originalEstimate, String securityLevel, String dueDate)
    {
        addIssueOnly(project, projectKey, issueType, summary, priority, components, affectsVersions, fixVersions, assignTo, environment, description, originalEstimate, securityLevel, dueDate);
        String issueKey;

        try
        {
            issueKey = extractIssueKey(projectKey);
        }
        catch (Exception e)
        {
            fail("Unable to retrieve issue key" + e.getMessage());
            return "fail";
        }

        return issueKey;
    }

    /**
     * Adds an issue to the given project returning its key. Different to {@link #addIssue(String, String, String,
     * String, String, String[], String[], String[], String, String, String, String, String, String)} in that it expects
     * the username for assignTo instead of the full name. Not all features currently work, so beware.
     */
    public String addIssueViaRest(String project, String projectKey, String issueType, String summary, String priority, String assignTo, String environment, String description)
    {
        return addIssueViaRestForResponse(project, projectKey, issueType, summary, priority, assignTo, environment, description).key;
    }

    public IssueCreateResponse addIssueViaRestForResponse(String project, String projectKey, String issueType, String summary, String priority, String assignTo, String environment, String description)
    {
        final IssueClient issueClient = getIssueClient();
        IssueFields fields = new IssueFields()
                .project(ResourceRef.withKey(projectKey))
                .issueType(ResourceRef.withName(issueType))
                .summary(summary);

        if (priority != null) { fields = fields.priority(ResourceRef.withName(priority)); }
        if (assignTo != null) { fields = fields.assignee(ResourceRef.withName(assignTo)); }
        if (environment != null) { fields = fields.environment(environment); }
        if (description != null) { fields = fields.description(description); }

        final IssueUpdateRequest request = new IssueUpdateRequest().fields(fields);
        final IssueCreateResponse response = issueClient.create(request);
        return response;
    }

    private IssueClient getIssueClient()
    {
        if (issueClient == null)
        {
            issueClient = new IssueClient(getEnvironmentData());
        }
        return issueClient;
    }

    public String extractIssueKey(String projectKey) throws IOException
    {
        String text;
        String issueKey;
        text = getDialog().getResponse().getText();
        int projectIdLocation = text.indexOf(projectKey);
        int endOfIssueKey = text.indexOf("]", projectIdLocation);
        issueKey = text.substring(projectIdLocation, endOfIssueKey);
        if (!issueKey.matches(projectKey + "-\\d+"))
        {
            fail("Invalid issue key: " + issueKey);
        }
        log("issueKey = " + issueKey);
        return issueKey;
    }

    public String getIssueKeyWithSummary(String summary, String projectKey)
    {
        return backdoor.issueNavControl().getIssueKeyForSummary(summary);
    }

    /**
     * Use {@link com.atlassian.jira.functest.framework.navigation.IssueNavigation#getId(String)} instead.
     *
     * @param issueKey The keys of the issue in play.
     * @return The id of the issue.
     */
    @Deprecated
    public String getIssueIdWithIssueKey(String issueKey)
    {
        gotoIssue(issueKey);

        String text;
        String issueId;

        try
        {
            text = getDialog().getResponse().getText();
            String paramName = "ViewVoters!default.jspa?id=";
            int issueIdLocation = text.indexOf(paramName) + paramName.length();
            issueId = text.substring(issueIdLocation, issueIdLocation + 5);
            log("issueId = " + issueId);
        }
        catch (IOException e)
        {
            fail("Unable to retrieve issue id" + e.getMessage());
            return "fail";
        }

        return issueId;
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.navigation.IssueNavigation#deleteIssue(String)}
     *             instead.
     */
    @Deprecated
    public void deleteIssue(String issueKey)
    {
        gotoIssue(issueKey);
        deleteCurrentIssue();
    }

    protected final void deleteCurrentIssue()
    {
        clickLink("delete-issue");
        setWorkingForm("delete-issue");
        submit("Delete");
    }

    /**
     * action keys: Resolve = 1 Reopen a resolved issue = 702 Reopen a closed issue = 901 close an issue = 701
     */
    public void progressWorkflow(String issueKey, int actionKey, String comment)
    {
        log(actionKey + ": " + issueKey);
        changeWorkflow(issueKey, actionKey);

        setWorkingForm("issue-workflow-transition");
        setFormElement("comment", comment);

        submit("Transition");

        assertTextPresent(comment);
    }

    public void progressAndResolve(String issueKey, int actionKey, String comment)
    {
        log(actionKey + ": " + issueKey);
        changeWorkflow(issueKey, actionKey);

        setWorkingForm("issue-workflow-transition");
        setFormElement("comment", comment);

        selectOption("resolution", "Fixed");

        submit("Transition");
        assertTextPresent(comment);
    }

    public void changeWorkflow(String issueKey, int actionKey)
    {
        gotoIssue(issueKey);
        clickLink("action_id_" + actionKey);
    }

    /**
     * Assigns the given issue to the user with the given full name.
     *
     * @param comment the comment to leave on the assignment action - may be null for no comment.
     * @deprecated please use {@link com.atlassian.jira.functest.framework.navigation.IssueNavigation#assignIssue(String,
     *             String, String)}
     */
    public void assignIssue(String issueKey, String comment, String userFullName)
    {
        gotoIssue(issueKey);
        clickLink("assign-issue");
        selectOption("assignee", userFullName);
        if (comment != null)
        {
            setFormElement("comment", comment);
        }
        clickButton("assign-issue-submit");
        assertTextPresent(userFullName);
        if (comment != null)
        {
            assertTextPresent(comment);
        }
    }

    /**
     * Creates a user with the given username and the same password, fullname and an email address username@example.com
     *
     * @param username the username.
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.UsersAndGroups#addUser(String)} instead.
     */
    @Deprecated
    public void addUser(String username)
    {
        addUser(username, username, username, username + "@example.com");
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.UsersAndGroups#addUser(String, String, String,
     *             String)} instead.
     */
    @Deprecated
    public void addUser(String username, String password, String fullname, String emailAddress)
    {
        log("Creating User " + username);
        gotoPage(PAGE_USER_BROWSER);
        clickLink("create_user");
        setFormElement("username", username);
        setFormElement("password", password);
        setFormElement("confirm", password);
        setFormElement("fullname", fullname);
        setFormElement("email", emailAddress);
        submit("Create");

    }

    public void navigateToUser(String username)
    {
        log("Navigating in UserBrowser to User " + username);
        gotoPage(PAGE_USER_BROWSER);
        clickLink(username);
    }

    public void deleteUser(String username)
    {
        log("Deleting User " + username);
        navigateToUser(username);
        clickLink("deleteuser_link");
        submit("Delete");
        assertTextNotPresent(username);
    }

    /**
     * Executes quicksearch with no search string to return all issues
     */
    public void runQuickSearch(String searchInput)
    {
        getNavigation().gotoDashboard();
        setWorkingForm("quicksearch");
        setFormElement("searchString", searchInput);
        submit();
    }

    /**
     * Executes quicksearch with no search string to return all issues
     *
     * @deprecated use {@link com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation#displayAllIssues()}
     */
    public void displayAllIssues()
    {
        getNavigation().issueNavigator().displayAllIssues();
    }

    public void assertIssueNavigatorDisplaying(String from, String to, String of)
    {
        assertTextSequence(new String[] { from, "?", to, "of", of });
    }

    /**
     * Goes to the view issue page
     *
     * @param issueKey the issue key
     * @deprecated Use {@link com.atlassian.jira.functest.framework.navigation.IssueNavigation#gotoIssue(String)}
     *             instead.
     */
    @Deprecated
    public void gotoIssue(String issueKey)
    {
        if (issueKey.equals(""))
        {
            getNavigation().issueNavigator().displayAllIssues();
        }
        else
        {
            getNavigation().issue().gotoIssue(issueKey);
        }
    }

    /**
     * Go to the project summary page for the given project.
     *
     * @param project_name the name of the project.
     */
    public void goToProject(String project_name)
    {
        gotoPage("/secure/project/ViewProjects.jspa");
        clickLinkWithText(project_name);
    }

    /**
     * Goes to the admin section, or, if already in the admin section, does nothing.
     *
     * @deprecated Use {@link com.atlassian.jira.functest.framework.Navigation#gotoAdmin()} or even better {@link
     *             Navigation#gotoAdminSection(String)} instead. If there is a specific method in {@link Navigation} to
     *             navigate to the admin page you want to go to, this should be the preferred way of navigating to it.
     *             e.g {@link com.atlassian.jira.functest.framework.Navigation#gotoDashboard()}
     */
    @Deprecated
    public void gotoAdmin()
    {
        getNavigation().gotoAdmin();
    }

    /**
     * Goes to the navigator section, or, if already in the section, does nothing.
     */
    public void gotoNavigator()
    {
        HTMLElement element = null;
        try
        {
            element = getDialog().getResponse().getElementWithID("searchButton");
        }
        catch (SAXException e)
        {
        }
        if (element == null)
        {
            log("going to Navigator page");
            clickLink("find_link");
        }
        else
        {
            log("already at Navigator");
        }
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.UsersAndGroups#addUserToGroup(String, String)}
     *             instead.
     */
    @Deprecated
    public void addUserToGroup(String userName, String groupName)
    {
        navigateToUser(userName);
        clickLink("editgroups_link");
        try
        {
            // use tester direct so we don't dump the page (as the super version does from WebTestCase)
            tester.selectOption("groupsToJoin", groupName);
            submit("join");
        }
        catch (Throwable ignoreItMeansTheyAreAlreadyInTheGroup)
        {
        }

        navigateToUser(userName);
        assertTextPresent(groupName);
    }

    public void addUserToProjectRole(String userName, String projectName, String roleName)
    {
        final Project projectByName = getProjectByName(projectName);

        ProjectRoleClient projectRoleClient = new ProjectRoleClient(environmentData);
        projectRoleClient.addActors(projectByName.key, roleName, null, new String[] { userName });
    }

    public void addGroupToProjectRole(String groupName, String projectName, String roleName)
    {
        final Project projectByName = getProjectByName(projectName);

        ProjectRoleClient projectRoleClient = new ProjectRoleClient(environmentData);
        projectRoleClient.addActors(projectByName.key, roleName, new String[] { groupName }, null);
    }

    public void removeUserFromProjectRole(String userName, String projectName, String roleName)
    {
        final Project projectByName = getProjectByName(projectName);

        ProjectRoleClient projectRoleClient = new ProjectRoleClient(environmentData);
        projectRoleClient.deleteUser(projectByName.key, roleName, userName);
    }

    public void removeGroupFromProjectRole(String groupName, String projectName, String roleName)
    {
        final Project projectByName = getProjectByName(projectName);

        ProjectRoleClient projectRoleClient = new ProjectRoleClient(environmentData);
        projectRoleClient.deleteGroup(projectByName.key, roleName, groupName);
    }

    /**
     * Check that the user with username is a member of the expectedGroupNames exactly.
     *
     * @param username username of the user to check group membership
     * @param expectedGroupNames all the group names the user is expected to be a member of
     */
    public void assertUserIsMemberOfGroups(String username, Collection expectedGroupNames)
    {
        // check that the new user is a member of both global user groups
        navigateToUser(username);
        clickLink("editgroups_link");
        setWorkingForm("user-edit-groups");
        //get the group names from the groups to leave select list
        List userGroupNames = Arrays.asList(getDialog().getOptionValuesFor("groupsToLeave"));
        //check that the user is in the exact same groups
        assertEquals("Expected user '" + username + "' to be member of group(s) '" + expectedGroupNames + "' but was '" + userGroupNames + "'", expectedGroupNames.size(), userGroupNames.size());
        for (final Object expectedGroupName1 : expectedGroupNames)
        {
            String expectedGroupName = (String) expectedGroupName1;
            assertTrue("Expected user '" + username + "' to be member of group(s) '" + expectedGroupNames + "' but was '" + userGroupNames + "'", userGroupNames.contains(expectedGroupName));
        }
    }

    protected void gotoDashboard()
    {
        clickLink("home_link");
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.UsersAndGroups#removeUserFromGroup(String,
     *             String)} instead.
     */
    @Deprecated
    public void removeUserFromGroup(String userName, String groupName)
    {
        navigateToUser(userName);
        clickLink("editgroups_link");
        selectOption("groupsToLeave", groupName);
        submit("leave");
        navigateToUser(userName);
        assertTextNotPresent(groupName);
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.UsersAndGroups#addGroup(String)} instead.
     */
    @Deprecated
    public void createGroup(String groupName)
    {
        gotoPage("/secure/admin/user/GroupBrowser.jspa");
        if (getDialog().isLinkPresentWithText(groupName))
        {
            clickLinkWithText(groupName);
            clickLink("del_" + groupName);
            submit("Delete");
        }

        setFormElement("addName", groupName);
        submit();
        assertLinkPresentWithText(groupName);
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.UsersAndGroups#deleteGroup(String)} instead.
     */
    @Deprecated
    public void removeGroup(String groupName)
    {
        gotoPage("/secure/admin/user/GroupBrowser.jspa");
        if (getDialog().isLinkPresent("del_" + groupName))
        {
            clickLink("del_" + groupName);
            submit("Delete");
        }
    }

    private List<String> fieldNamesToFieldIds(String[] fieldNames)
    {
        //Get the map(name, id) of current custom fields
        List<CustomFieldResponse> customFields = backdoor.customFields().getCustomFields();
        ImmutableMap.Builder<String, String> builderCustomFieldsById = ImmutableMap.builder();
        for (CustomFieldResponse customField : customFields)
        {
            builderCustomFieldsById.put(customField.name, customField.id);
        }
        Map<String, String> customFieldsById = builderCustomFieldsById.build();

        //Transform the list if fieldNames into fieldIds
        ImmutableList.Builder<String> builderFieldIds = ImmutableList.builder();
        for (String fieldName : fieldNames)
        {
            if (customFieldsById.containsKey(fieldName))
            {
                builderFieldIds.add(customFieldsById.get(fieldName));
            }
        }
        return builderFieldIds.build();
    }

    public void addColumnToIssueNavigator(String[] fieldNames)
    {
        backdoor.columnControl().addLoggedInUserColumns(fieldNamesToFieldIds(fieldNames));
    }

    public void addColumnToIssueNavigatorById(String[] fieldIds)
    {
        backdoor.columnControl().addLoggedInUserColumns(Arrays.asList(fieldIds));
    }

    public void restoreColumnDefaults()
    {
        backdoor.columnControl().restoreLoggedInUserColumns();
    }

    // Selects Project and Issue Type in 'Create Issue' operation
    public void createIssueStep1()
    {
        getNavigation().issue().goToCreateIssueForm(PROJECT_HOMOSAP, "Bug");

        assertTextPresent("CreateIssueDetails.jspa");
    }

    public void createIssueStep1(String project, String issueType)
    {
        getNavigation().issue().goToCreateIssueForm(project, issueType);
    }

    public void gotoFieldLayoutSchemes()
    {
        gotoPage("/secure/admin/ViewFieldLayoutSchemes.jspa");
        assertTextPresent("View Field Configuration Schemes");
    }

    public void addFieldLayoutScheme(String scheme_name, String scheme_desc)
    {
        gotoFieldLayoutSchemes();
        clickLink("add-field-configuration-scheme");
        setFormElement("fieldLayoutSchemeName", scheme_name);
        setFormElement("fieldLayoutSchemeDescription", scheme_desc);
        submit("Add");
    }

    public void deleteFieldLayoutScheme(String scheme_name)
    {
        gotoFieldLayoutSchemes();
        clickLink("del_" + scheme_name);
        assertTextPresent("Delete Field Configuration Scheme");
        assertTextPresent(scheme_name);
        submit("Delete");
    }

    public void copyFieldLayout(String fieldLayoutName)
    {
        gotoPage(PAGE_ENTERPRISE_FIELD_CONFIGURATIONS);
        assertTextPresent("View Field Configurations");
        clickLinkWithText("Copy");
        assertTextPresent("Copy Field Configuration:");
        setFormElement("fieldLayoutName", fieldLayoutName);
        submit();
    }

    public void addFieldLayoutSchemeEntry(String issueTypeName, String fieldLayoutName, String schemeName)
    {
        gotoFieldLayoutSchemes();
        clickLinkWithText(schemeName);
        clickLink("add-issue-type-field-configuration-association");
        selectOption("issueTypeId", issueTypeName);
        selectOption("fieldConfigurationId", fieldLayoutName);
        submit();
        assertTextPresent(issueTypeName);
    }

    public void associateFieldLayoutScheme(String project, String issue_type, String scheme_name)
    {
        final Project projectByName = getProjectByName(project);
        gotoPage("/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=" + projectByName.id);
        assertTextPresent("Field Layout Configuration Association");
        selectOption("schemeId", scheme_name);
        submit("Associate");
        assertThat(backdoor.project().getSchemes(projectByName.key).fieldConfigurationScheme.name, equalTo(scheme_name));
    }

    public void removeAssociationWithFieldLayoutScheme(String project, String issue_type, String scheme_name)
    {
        final Project projectByName = getProjectByName(project);
        gotoPage("/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=" + projectByName.id);
        assertTextPresent("Field Layout Configuration Association");
        selectOption("schemeId", "System Default Field Configuration");
        submit("Associate");
    }

    // changes a field from hide to show or vice wersa
    public void setHiddenFieldsOnEnterprise(String fieldLayoutName, String fieldName)
    {
        gotoFieldLayoutOnEnterprise(fieldLayoutName);
        assertViewIssueFields();
        doFieldOperation(fieldName, HIDE_FIELD_OPERATION_NAME);
    }

    public void showIssues(String jql)
    {
        gotoPage("/issues/?jql=" + jql);
    }

    private void gotoFieldLayoutOnEnterprise(String fieldLayoutName)
    {
        gotoPage(PAGE_ENTERPRISE_FIELD_CONFIGURATIONS);
        assertTextPresent("View Field Configurations");
        clickLinkWithText(fieldLayoutName);
    }

    public void setShownFieldsOnEnterprise(String fieldLayoutName, String fieldName)
    {
        gotoFieldLayoutOnEnterprise(fieldLayoutName);
        assertViewIssueFields();
        doFieldOperation(fieldName, SHOW_FIELD_OPERATION_NAME);
    }

    public void setRequiredFieldsOnEnterprise(String fieldLayoutName, String fieldName)
    {
        gotoFieldLayoutOnEnterprise(fieldLayoutName);
        assertViewIssueFields();
        setRequiredField(fieldName);
    }

    public void setOptionalFieldsOnEnterprise(String fieldLayoutName, String fieldName)
    {
        gotoFieldLayoutOnEnterprise(fieldLayoutName);
        assertViewIssueFields();
        doFieldOperation(fieldName, OPTIONAL_FIELD_OPERATION_NAME);
    }

    public void setRequiredField(String fieldName)
    {
        assertViewIssueFields();

        try
        {
            WebTable fieldTable = getDialog().getResponse().getTableWithID(FIELD_TABLE_ID);
            // First row is a headings row so skip it
            for (int i = 1; i < fieldTable.getRowCount(); i++)
            {
                String field = fieldTable.getCellAsText(i, FIELD_TABLE_FIELD_NAME_COLUMN_INDEX);
                if (field.contains(fieldName))
                {
                    TableCell linkCell = fieldTable.getTableCell(i, FIELD_TABLE_OPERATIONS_COLUMN_INDEX);
                    WebLink requiredLink = linkCell.getLinkWith("Required");
                    if (requiredLink == null)
                    {
                        fail("Cannot find 'required' link for field '" + fieldName + "'.");
                    }
                    else
                    {
                        requiredLink.click();
                        return;
                    }
                }
            }

            fail("Cannot find field with id '" + fieldName + "'.");
        }
        catch (SAXException e)
        {
            fail("Cannot find table with id '" + FIELD_TABLE_ID + "'.");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            fail("Cannot click 'required' link for field id '" + fieldName + "'.");
        }
    }

    private void gotoViewIssueFields()
    {
        gotoPage(PAGE_ENTERPRISE_FIELD_CONFIGURATIONS);
        assertTextPresent("View Field Configurations");
        clickLinkWithText("Configure");
        assertTextPresent("View Field Configuration");
    }

    // Sets fields to be required
    public void setRequiredFields()
    {
        resetFields();

        log("Set fields to be required");

        gotoViewIssueFields();
        setRequiredField(AFFECTS_VERSIONS_FIELD_ID);
        setRequiredField(FIX_VERSIONS_FIELD_ID);
        setRequiredField(COMPONENTS_FIELD_ID);
    }

    public void setHiddenFields(String fieldName)
    {
        log("Hide field " + fieldName);
        gotoViewIssueFields();
        assertViewIssueFields();
        doFieldOperation(fieldName, HIDE_FIELD_OPERATION_NAME);
    }

    public void setShownFields(String fieldName)
    {
        log("Show field " + fieldName);
        gotoViewIssueFields();
        assertViewIssueFields();
        doFieldOperation(fieldName, SHOW_FIELD_OPERATION_NAME);
    }

    public void doFieldOperation(String fieldName, String linkName)
    {
        assertViewIssueFields();

        try
        {
            WebTable fieldTable = getDialog().getResponse().getTableWithID(FIELD_TABLE_ID);
            // First row is a headings row so skip it
            for (int i = 1; i < fieldTable.getRowCount(); i++)
            {
                String field = fieldTable.getCellAsText(i, FIELD_TABLE_FIELD_NAME_COLUMN_INDEX);
                if (field.contains(fieldName))
                {
                    TableCell linkCell = fieldTable.getTableCell(i, FIELD_TABLE_OPERATIONS_COLUMN_INDEX);
                    WebLink link = linkCell.getLinkWith(linkName);
                    if (link == null)
                    {
                        // This is usually OK, as this happens when e.g. hiding a field that is already hidden
                        log("Link with name '" + linkName + "' does not exist.");
                        return;
                    }
                    else
                    {
                        link.click();
                        return;
                    }
                }
            }
            fail("Cannot find field with id '" + fieldName + "'.");
        }
        catch (SAXException e)
        {
            fail("Cannot find table with id '" + FIELD_TABLE_ID + "'.");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            fail("Cannot click '" + linkName + "' link for field id '" + fieldName + "'.");
        }
    }

    public void resetFields()
    {
        log("Restore default field settings");
        gotoViewIssueFields();
        if (getDialog().isLinkPresentWithText("Restore Defaults"))
        {
            clickLinkWithText("Restore Defaults");
        }
    }

    public void setDueDateToRequried()
    {
        resetFields();
        log("Set 'Due Date' Field to required");
        gotoViewIssueFields();
        setRequiredField(DUE_DATE_FIELD_ID);

    }

    public void setSecurityLevelToRequried()
    {
        resetFields();
        log("Set 'Security Level' Field to required");
        gotoViewIssueFields();

        setRequiredField(SECURITY_LEVEL_FIELD_ID);
    }

    /**
     * Grant Global Permission for specified group
     *
     * @deprecated Use {@link Administration#addGlobalPermission(int, String)} instead.
     */
    @Deprecated
    public void grantGlobalPermission(int permissionCode, String groupName)
    {
        String deleteLink = "del_" + permissionCode + "_" + groupName;
        navigation.gotoAdminSection("global_permissions");

        if (!(getDialog().isLinkPresent(deleteLink)))
        {
            if (permissionCode == BULK_CHANGE)
            {
                selectOption("globalPermType", "Bulk Change");
            }
            else if (permissionCode == CREATE_SHARED_OBJECTS)
            {
                selectOption("globalPermType", "Create Shared Objects");
            }
            else if (permissionCode == USE)
            {
                selectOption("globalPermType", "JIRA Users");
            }
            else if (permissionCode == GLOBAL_ADMIN)
            {
                selectOption("globalPermType", "JIRA Administrators");
            }
            else if (permissionCode == ADMINISTER)
            {
                selectOption("globalPermType", "JIRA Administrators");
            }
            else if (permissionCode == SYSTEM_ADMINISTER)
            {
                selectOption("globalPermType", "JIRA System Administrators");
            }
            selectOption("groupName", groupName);
            submit("Add");
        }
    }

    /**
     * Remove Global Permission for specified group
     *
     * @deprecated User {@link Administration#removeGlobalPermission(int, String)} instead.
     */
    @Deprecated
    public void removeGlobalPermission(int permissionCode, String groupName)
    {
        String deleteLink = "del_" + permissionCode + "_" + groupName;
        clickOnAdminPanel("admin.globalsettings", "global_permissions");

        if ((getDialog().isLinkPresent(deleteLink)))
        {
            clickLink(deleteLink);
            submit("Delete");
        }
    }

    /**
     * Create a new permission scheme
     */
    public void createPermissionScheme(String permission_name, String permission_desc)
    {
        gotoPermissionSchemes();
        clickLinkWithText("Add Permission Scheme");
        setFormElement("name", permission_name);
        setFormElement("description", permission_desc);
        submit("Add");
    }

    /**
     * Deletes a permission scheme
     */
    public void deletePermissionScheme(String permission_name)
    {
        gotoPermissionSchemes();
        clickLink("del_" + permission_name);
        submit("Delete");
    }

    /**
     * Associate a permission scheme with a project
     */
    public void associatePermSchemeToProject(String project, String permission_name)
    {
        final Project projectByName = getProjectByName(project);
        gotoPage("/secure/project/SelectProjectPermissionScheme!default.jspa?projectId=" + projectByName.id);
        selectOption("schemeIds", permission_name);
        submit("Associate");
    }

    /**
     * Remove permssion for a particular scheme
     */
    public void removeGroupPermission(String permission_scheme, int permission, String groupName)
    {
        String permissionKey = getKey(permission);
        gotoPermissionSchemes();
        clickLinkWithText(permission_scheme);
        assertTextPresent("Edit Permissions &mdash; " + permission_scheme);
        if (getDialog().isLinkPresent("del_perm_" + permissionKey + "_" + groupName))
        {
            clickLink("del_perm_" + permissionKey + "_" + groupName);
            submit("Delete");
            assertTextPresent("Edit Permissions &mdash; " + permission_scheme);
            assertLinkNotPresent("del_perm_" + permissionKey + "_" + groupName);
        }
    }

    /**
     * Grant permission
     */
    public void grantGroupPermission(String permission_scheme, int permission, String groupName)
    {
        String permissionKey = getKey(permission);
        gotoPermissionSchemes();
        clickLinkWithText(permission_scheme);
        assertTextPresent("Edit Permissions &mdash; " + permission_scheme);

        if (!getDialog().isLinkPresent("del_perm_" + permissionKey + "_" + groupName))
        {
            clickLink("add_perm_" + permissionKey);
            getDialog().setFormParameter("type", "group");
            assertRadioOptionSelected("type", "group");
            assertOptionValuesEqual("group", new String[] { "", Groups.ADMINISTRATORS, Groups.DEVELOPERS, Groups.USERS });
            selectOption("group", groupName);
            // Setting radio button option
            getDialog().setFormParameter("type", "group");
            assertRadioOptionSelected("type", "group");
            submit();
        }
    }

    /**
     * broken method will be deleted ASAP
     *
     * @deprecated broken do not use
     */
    @Deprecated
    public void grantGroupAllPermissions(String permission_scheme, String groupName)
    {
        gotoPermissionSchemes();
        clickLinkWithText(permission_scheme);
        assertTextPresent("Edit Permissions &mdash; " + permission_scheme);

        clickLinkWithText("Grant Permission");
        checkCheckbox("type", "group");
        selectOption("group", Groups.ADMINISTRATORS);
        String[] optionValues = getDialog().getOptionValuesFor("permissions");
        for (final String optionValue : optionValues)
        {
            selectMultiOptionByValue("permissions", optionValue);
        }
        submit();
    }

    /**
     * Remove permission
     *
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.PermissionSchemes#defaultScheme()} and call
     *             {@link com.atlassian.jira.functest.framework.admin.PermissionSchemes.PermissionScheme#removePermission(int,
     *             String)} on it.
     */
    @Deprecated
    public void removeGroupPermission(int permission, String groupName)
    {
        String permissionKey = getKey(permission);
        gotoPermissionSchemes();
        clickLinkWithText("Default Permission Scheme");
        assertTextPresent("Edit Permissions &mdash; Default Permission Scheme");
        if (getDialog().isLinkPresent("del_perm_" + permissionKey + "_" + groupName))
        {
            clickLink("del_perm_" + permissionKey + "_" + groupName);
            submit("Delete");
        }
        assertTextPresent("Edit Permissions &mdash; Default Permission Scheme");
        assertLinkNotPresent("del_perm_" + permissionKey + "_" + groupName);
    }

    public void removeRolePermission(int permission, int role)
    {
        String permissionKey = getKey(permission);
        gotoPermissionSchemes();
        clickLinkWithText("Default Permission Scheme");
        assertTextPresent("Edit Permissions &mdash; Default Permission Scheme");
        if (getDialog().isLinkPresent("del_perm_" + permissionKey + "_" + role))
        {
            clickLink("del_perm_" + permissionKey + "_" + role);
            submit("Delete");
        }
        assertTextPresent("Edit Permissions &mdash; Default Permission Scheme");
        assertLinkNotPresent("del_perm_" + permissionKey + "_" + role);
    }

    /**
     * Grant permission
     *
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.PermissionSchemes#defaultScheme()} and call
     *             {@link com.atlassian.jira.functest.framework.admin.PermissionSchemes.PermissionScheme#grantPermissionToGroup(int,
     *             String)} (int, String)} on it.
     */
    @Deprecated
    public void grantGroupPermission(int permission, String groupName)
    {
        String permissionKey = getKey(permission);
        gotoPermissionSchemes();
        clickLinkWithText("Default Permission Scheme");
        assertTextPresent("Edit Permissions &mdash; Default Permission Scheme");
        if (!getDialog().isLinkPresent("del_perm_" + permissionKey + "_" + groupName))
        {
            clickLink("add_perm_" + permissionKey);
            getDialog().setFormParameter("type", "group");
            assertRadioOptionSelected("type", "group");
            assertOptionValuesEqual("group", new String[] { "", Groups.ADMINISTRATORS, Groups.DEVELOPERS, Groups.USERS });
            selectOption("group", groupName);
            // Setting radio button option
            getDialog().setFormParameter("type", "group");
            assertRadioOptionSelected("type", "group");
            submit();
        }
    }

    public void grantRolePermission(int permission, int role)
    {
        String permissionKey = getKey(permission);
        gotoPermissionSchemes();
        clickLinkWithText("Default Permission Scheme");
        assertTextPresent("Edit Permissions &mdash; Default Permission Scheme");
        if (!getDialog().isLinkPresent("del_perm_" + permissionKey + "_" + role))
        {
            clickLink("add_perm_" + permissionKey);
            getDialog().setFormParameter("type", "projectrole");
            assertRadioOptionSelected("type", "projectrole");
            assertOptionValuesEqual("projectrole", new String[] { "", "10002", "10001", "10000" });
            checkCheckbox("projectrole", Integer.toString(role));
            // Setting radio button option
            getDialog().setFormParameter("type", "projectrole");
            assertRadioOptionSelected("type", "projectrole");
            submit();
        }
    }

    public void gotoPermissionSchemes()
    {
        gotoPage("/secure/admin/ViewPermissionSchemes.jspa");
    }

    public void grantPermissionToUserCustomField(String permissionScheme, String customFieldName, int permission)
    {
        String permissionKey = getKey(permission);
        gotoPermissionSchemes();
        clickLinkWithText(permissionScheme);
        clickLink("add_perm_" + permissionKey);
        checkCheckbox("type", "userCF");
        selectOption("userCF", customFieldName);
        submit(" Add ");
        assertTextPresent("(" + customFieldName + ")");
    }

    public void grantPermissionToReporter(int permission)
    {
        String permissionKey = getKey(permission);
        gotoPermissionSchemes();
        clickLinkWithText("Default Permission Scheme");
        clickLink("add_perm_" + permissionKey);
        checkCheckbox("type", "reporter");
        submit(" Add ");
    }

    public void removePermissionFromUserCustomField(String permissionScheme, String customFieldId, int permission)
    {
        String permissionKey = getKey(permission);
        gotoPermissionSchemes();
        clickLinkWithText(permissionScheme);
        clickLink("del_perm_" + permissionKey + "_customfield_" + customFieldId);
        submit("Delete");
    }

    public void gotoSchemeTools()
    {
        gotoAdmin();
        clickLink("scheme_tools");
    }

    public void gotoGroupToRoleMappingToolSchemeSelection()
    {
        gotoSchemeTools();
        clickLink("mapping_tool");
        assertTextPresent("Map Groups to Project Roles: Select Schemes");
    }

    public void gotoGroupToRoleMappingToolMappingSelection(String schemeType, String schemeName)
    {
        gotoGroupToRoleMappingToolSchemeSelection();
        selectOption("selectedSchemeType", schemeType);
        selectOption("selectedSchemeIds", schemeName);
        submit("Map Groups to Roles");
        assertTextPresent("Map Groups to Project Roles: Select Mappings");
    }

    /**
     * Goes to the Group to Role Mapping Scheme tool and goes through the wizard with the specified schemeType and
     * schemeName. In the mapping step, it will get the group to role mapping from the specified map
     * groupToRoleMapping.
     *
     * @param schemeType schemeType of the scheme specified by the schemeName
     * @param schemeName the name of the scheme to work on
     * @param groupToRoleMapping Map of group names to role names
     */
    public void mapGroupToRoles(String schemeType, String schemeName, Map groupToRoleMapping)
    {
        gotoGroupToRoleMappingToolMappingSelection(schemeType, schemeName);
        for (final Object o1 : groupToRoleMapping.keySet())
        {
            String groupName = (String) o1;
            selectOption(groupName + "_project_role", (String) groupToRoleMapping.get(groupName));
        }
        submit("Preview Mappings");

        //in the preview page, check we are mapping the correct entries
        try
        {
            WebTable groupToRoleTable = getDialog().getResponse().getTableWithID("group_to_role_mappings");
            for (final Object o : groupToRoleMapping.keySet())
            {
                String groupName = (String) o;
                //Check [group -> role] row exists

                String roleName = (String) groupToRoleMapping.get(groupName);
                boolean containsNonGlobalUsePermissionedGroup = tableIndexOf(groupToRoleTable,
                        Lists.newArrayList(groupName, new ImageCell("/images/icons/arrow_right_small.gif"), roleName))
                        != -1;

                boolean containsGlobalUsePermissionedGroup = tableIndexOf(groupToRoleTable,
                        Lists.newArrayList(groupName, new ImageCell("/images/icons/arrow_right_yellow.gif"), roleName))
                        != -1;

                assertTrue("Did not find a row matching: " + groupName + ", [arrow icon] , " + roleName, containsGlobalUsePermissionedGroup || containsNonGlobalUsePermissionedGroup);
            }
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }

        submit("Save");
        assertTextPresent("Map Groups to Project Roles: Results of Transformation for Schemes");
    }

    /**
     * activate time Tracking
     *
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.TimeTracking#enable(com.atlassian.jira.functest.framework.admin.TimeTracking.Mode)}
     *             instead.
     */
    @Deprecated
    public void activateTimeTracking()
    {
        submitAtPage(PAGE_TIME_TRACKING, "Activate", "time tracking already activated");
    }

    /**
     * deactivate time tracking
     *
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.TimeTracking#disable()} instead.
     */
    @Deprecated
    public void deactivateTimeTracking()
    {
        submitAtPage(PAGE_TIME_TRACKING, "Deactivate", "time tracking already deactivated");
    }

    /**
     * change the time tracking format (timetracking must be on before this call)
     */
    public void reconfigureTimetracking(String format)
    {
        gotoPage("/secure/admin/jira/TimeTrackingAdmin!default.jspa");
        submit("Deactivate");
        checkCheckbox("timeTrackingFormat", format);
        submit("Activate");
    }

    /**
     * Goes to the given URL, submits the given button or logs the given message if the given button doesn't exist.
     *
     * @param url url to go to to submit the button
     * @param button label on the button to submit at url
     * @param logOnFail null or a message to log if button isn't found
     */
    public void submitAtPage(String url, String button, String logOnFail)
    {
        gotoPage(url);
        if (getDialog().hasSubmitButton(button))
        {
            submit(button);
        }
        else if (logOnFail != null)
        {
            log(logOnFail);
        }
    }

    /**
     * Logs work on an issue. Note - this method requires activated time tracking
     *
     * @param issueKey the key of the issue to log work on.
     * @param timeLogged the time in a suitible format for JIRA's current settings (e.g. 2d 1h 30m)
     */
    public void logWorkOnIssue(String issueKey, String timeLogged)
    {
        logWorkOnIssueWithComment(issueKey, timeLogged, null);
    }

    /**
     * Logs work on an issue. Note - this method requires activated time tracking
     *
     * @param issueKey the key of the issue to log work on.
     * @param timeLogged the time in a suitible format for JIRA's current settings (e.g. 2d 1h 30m)
     * @param comment a comment to add for the work log - may be null to not leave a comment.
     */
    public void logWorkOnIssueWithComment(String issueKey, String timeLogged, String comment)
    {
        gotoIssue(issueKey);
        clickLink("log-work");
        setFormElement("timeLogged", timeLogged);
        if (comment != null)
        {
            setFormElement("comment", comment);
        }
        submit();
    }

    /**
     * activate issue linking
     *
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.IssueLinking#enable()} instead.
     */
    @Deprecated
    public void activateIssueLinking()
    {
        submitAtPage(getPage().addXsrfToken(PAGE_LINK_TYPES), "Activate", "linking already activated");
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.IssueLinking#disable()} instead.
     */
    @Deprecated
    public void deactivateIssueLinking()
    {
        submitAtPage(getPage().addXsrfToken(PAGE_LINK_TYPES), "Deactivate", "linking already deactivated");
    }

    public String getComponentId(String project, String component)
    {
        final Project projectByName = getProjectByName(project);

        final Component componentByname = getComponentByname(projectByName.key, component);

        return "" + componentByname.id;
    }

    public String getProjectId(String project)
    {
        return getProjectByName(project).id;
    }

    /**
     * Sets the component lead for a component in a project.
     *
     * @param project the project key.
     * @param userName the username of the lead.
     * @param fullName the full name of the lead - used for assertion that it worked!
     * @param component the component id.
     */
    public void setComponentLead(String project, String userName, String fullName, String component)
    {
        final Project projectByName = getProjectByName(project);

        final Component componentByname = getComponentByname(projectByName.key, component);

        ComponentClient componentClient = new ComponentClient(environmentData);

        componentClient.putResponse(componentByname.leadUserName(userName));
    }

    public void setComponentName(String project, String oldComponentName, String newComponentName)
    {
        final Project projectByName = getProjectByName(project);

        final Component componentByname = getComponentByname(projectByName.key, oldComponentName);

        ComponentClient componentClient = new ComponentClient(environmentData);

        componentClient.putResponse(componentByname.name(newComponentName));
    }

    /**
     * Clear component lead
     */
    public void clearComponentLead(String project, String component)
    {
        final Project projectByName = getProjectByName(project);

        final Component componentByname = getComponentByname(projectByName.key, component);

        ComponentClient componentClient = new ComponentClient(environmentData);

        componentClient.putResponse(componentByname.leadUserName(""));
    }

    /**
     * Set Component Assignee Options
     */
    public void setComponentAssigneeOptions(String project, String component, String option)
    {
        final Project projectByName = getProjectByName(project);

        final Component componentByname = getComponentByname(projectByName.key, component);

        ComponentClient componentClient = new ComponentClient(environmentData);


        if (option.equals("1"))
        {
            componentByname.assigneeType(Component.AssigneeType.COMPONENT_LEAD);
        }
        else if (option.equals("2"))
        {
            componentByname.assigneeType(Component.AssigneeType.PROJECT_LEAD);
        }
        else
        {
            componentByname.assigneeType(Component.AssigneeType.UNASSIGNED);
        }

        componentClient.putResponse(componentByname);
    }

    /**
     * Configure 'unassigned' issues option
     *
     * @deprecated Please use {@link com.atlassian.jira.functest.framework.admin.GeneralConfigurationImpl#setAllowUnassignedIssues(boolean)}
     *             instead.
     */
    @Deprecated
    public void setUnassignedIssuesOption(boolean enable)
    {
        gotoAdmin();
        clickLink("general_configuration");
        tester.clickLink("edit-app-properties");

        if (enable)
        {
            getDialog().setFormParameter("allowUnassigned", "true");
            assertRadioOptionSelected("allowUnassigned", "true");
        }
        else
        {
            getDialog().setFormParameter("allowUnassigned", "false");
            assertRadioOptionSelected("allowUnassigned", "false");
        }
        submit("Update");
    }

    /**
     * Set Project Lead
     *
     * @param project the project key.
     * @param username the username of the project lead.
     */
    public void setProjectLead(String project, String username)
    {
        final Project projectByName = getProjectByName(project);

        gotoPage("/secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=" + projectByName.id);

        FormParameterUtil formParameterUtil = new FormParameterUtil(tester, "project-edit-lead-and-default-assignee", "Update");
        formParameterUtil.addOptionToHtmlSelect("lead", new String[] { username });
        formParameterUtil.setFormElement("lead", username);
        formParameterUtil.submitForm();
    }

    // Vote for an Issue
    public void voteForIssue(String issueKey)
    {
        gotoIssue(issueKey);
        if (getDialog().getResponseText().contains("vote-state-off"))
        {
            clickLink("toggle-vote-issue");
        }
        assertTrue(getDialog().getResponseText().contains("vote-state-on"));
        assertLinkPresent("toggle-vote-issue");
    }

    // Unvote for an Issue
    public void unvoteForIssue(String issueKey)
    {
        gotoIssue(issueKey);
        if (getDialog().getResponseText().contains("vote-state-on"))
        {
            clickLink("toggle-vote-issue");
        }
        assertTrue(getDialog().getResponseText().contains("vote-state-off"));
        assertLinkPresent("toggle-vote-issue");
    }

    // Watch an Issue
    public void startWatchingAnIssue(String issueKey)
    {
        gotoIssue(issueKey);
        if (getDialog().getResponseText().contains("watch-state-off"))
        {
            clickLink("toggle-watch-issue");
        }
        assertTrue(getDialog().getResponseText().contains("watch-state-on"));
        assertLinkPresent("toggle-watch-issue");
    }

    public void startWatchingAnIssue(String issueKey, String[] userNames)
    {
        gotoIssue(issueKey);

        clickLink("manage-watchers");

        assertTextPresent("Watchers");

        StringBuilder userNameList = new StringBuilder();

        for (int i = 0, n = userNames.length; i < n; i++)
        {
            if (i != n - 1)
            {
                userNameList.append(userNames[i]).append(",");
            }
            else
            {
                userNameList.append(userNames[i]);
            }
        }

        setFormElement("userNames", userNameList.toString());
        submit();

        for (final String userName : userNames)
        {
            assertLinkPresent("watcher_link_" + userName);
        }
    }

    public void stopWatchingAnIssue(String issueKey)
    {
        gotoIssue(issueKey);
        if (getDialog().getResponseText().contains("watch-state-on"))
        {
            clickLink("toggle-watch-issue");
        }
        assertTrue(getDialog().getResponseText().contains("watch-state-off"));
        assertLinkPresent("toggle-watch-issue");
    }

    public void removeAllWatchers(String issueKey)
    {
        gotoIssue(issueKey);
        clickLink("view-watcher-list");
        checkCheckbox("all");
        getDialog().setWorkingForm("stopform");
        submit();
        assertTextPresent("There are no watchers.");
    }

    /**
     * @deprecated Enables sub-tasks. Use {@link com.atlassian.jira.functest.framework.admin.Subtasks#enable()}
     *             instead.
     */
    @Deprecated
    public void activateSubTasks()
    {
        gotoPage("/secure/admin/subtasks/ManageSubTasks.jspa");
        if (getDialog().isLinkPresentWithText("Enable"))
        {
            clickLinkWithText("Enable");
        }
        else
        {
            log("Subtasks already enabled");
        }
    }

    /**
     * This method forces deactivation of the subtasks by removing all issues<br>
     *
     * @return boolean - signals whether all the issues was deleted or not</b>
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.Subtasks#disable()} instead.
     */
    @Deprecated
    public boolean deactivateSubTasks()
    {
        log("Deactivating Sub-tasks");
        gotoPage("/secure/admin/subtasks/ManageSubTasks.jspa");
        if (getDialog().isLinkPresentWithText("Disable"))
        {
            clickLinkWithText("Disable");
        }
        else
        {
            log("Sub-tasks already disabled");
        }
        if (getDialog().isTextInResponse("Cannot disable subtasks."))
        {
            deleteAllIssuesInAllPages();
            deactivateSubTasks();
            return true;//signals that all issues was removed
        }
        assertLinkPresentWithText("Enable");
        return false;
    }

    /**
     * Adds a subtask with the given type and properties to the given issue.
     *
     * @param issueKey the issue key of the parent.
     * @param subTaskType the subtask issue type (try {@link #ISSUE_TYPE_SUB_TASK}
     * @param subTaskSummary the summary of the subtask
     * @param subTaskDescription the description for the subtask
     * @param originalEstimate the estimated time to complete the subtask (subtasks must be turned on)
     * @return the issueKey of the new subtask.
     */
    public String addSubTaskToIssue(String issueKey, String subTaskType, String subTaskSummary, String subTaskDescription, String originalEstimate)
    {
        createSubTaskStep1(issueKey, subTaskType);
        setFormElement("summary", subTaskSummary);
        setFormElement("description", subTaskDescription);
        if (originalEstimate != null)
        {
            setFormElement("timetracking", "2h");
        }
        submit("Create");

        String text;
        String subTaskKey;
        String projectKey = issueKey.substring(0, issueKey.indexOf('-'));

        try
        {
            text = getDialog().getResponse().getText();
            int projectIdLocation = text.indexOf(projectKey);
            int endOfSubTaskKey = text.indexOf("]", projectIdLocation);
            subTaskKey = text.substring(projectIdLocation, endOfSubTaskKey);
            log("subTaskKey = " + subTaskKey);
        }
        catch (IOException e)
        {
            fail("Unable to retrieve issue key" + e.getMessage());
            return "fail";
        }

        return subTaskKey;
    }

    /**
     * @deprecated please use {@link com.atlassian.jira.functest.framework.navigation.IssueNavigation#createSubTask(String,
     *             String, String, String)}
     */
    @Deprecated
    public String addSubTaskToIssue(String issueKey, String subTaskType, String subTaskSummary, String subTaskDescription)
    {
        return addSubTaskToIssue(issueKey, subTaskType, subTaskSummary, subTaskDescription, null);
    }

    /**
     * @param sub_task_name name
     * @param sub_task_description description
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.Subtasks#addSubTaskType(String, String)}
     *             instead.
     */
    public void createSubTaskType(String sub_task_name, String sub_task_description)
    {
        activateSubTasks();
        setFormElement("name", sub_task_name);
        setFormElement("description", sub_task_description);
        submit("Add");
        deactivateSubTasks();
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.Subtasks#deleteSubTaskType(String)} instead.
     */
    public void deleteSubTaskType(String sub_task_name)
    {
        activateSubTasks();
        clickLink("del_" + sub_task_name);
        submit("Delete");
        deactivateSubTasks();
    }

    public void createSubTaskStep1(String issueKey, String task_type)
    {
        activateSubTasks();
        gotoIssue(issueKey);
        clickLink("create-subtask");
        assertTextPresent("Create Sub-Task");
        if (getDialog().getElement("issuetype") == null)
        {
            log("Bypassing step 1 of sub task creation");
        }
        else
        {
            setWorkingForm("subtask-create-start");
            selectOption("issuetype", task_type);
            submit("Create");
        }
        assertElementPresent("subtask-create-details"); // ID of the Subtask Details
    }

    public void gotoIssueSecuritySchemes()
    {
        clickOnAdminPanel("admin.schemes", "security_schemes");
    }

    public void createSecurityScheme(String scheme_name, String scheme_description)
    {
        gotoIssueSecuritySchemes();
        clickLink("add_securityscheme");

        assertTextPresent("Add Issue Security Scheme");
        setFormElement("name", scheme_name);
        setFormElement("description", scheme_description);

        submit("Add");
    }

    public void createSecurityLevel(String scheme_name, String level_name, String level_description)
    {
        gotoIssueSecuritySchemes();
        clickLinkWithText(scheme_name);

        setFormElement("name", level_name);
        setFormElement("description", level_description);

        submit("Add Security Level");

        assertLinkPresent("add_" + level_name);

    }

    public void addGroupToSecurityLevel(String scheme_name, String level_name, String groupName)
    {
        addGroupToSecurityLevel(scheme_name, level_name, groupName, new String[] { "", Groups.ADMINISTRATORS, Groups.DEVELOPERS, Groups.USERS });
    }

    public void addGroupToSecurityLevel(String scheme_name, String level_name, String groupName, String[] expectedGroups)
    {
        gotoIssueSecuritySchemes();
        clickLinkWithText(scheme_name);

        clickLink("add_" + level_name);

        //add group
        getDialog().setFormParameter("type", "group");
        assertRadioOptionSelected("type", "group");
        assertOptionValuesEqual("group", expectedGroups);
        selectOption("group", groupName);
        // Setting radio button option
        getDialog().setFormParameter("type", "group");
        assertRadioOptionSelected("type", "group");
        submit();
    }

    public void addRoleToSecurityLevel(String scheme_name, String level_name, String roleName)
    {
        gotoIssueSecuritySchemes();
        clickLinkWithText(scheme_name);

        clickLink("add_" + level_name);

        // Set radio button option
        getDialog().setFormParameter("type", "projectrole");
        assertRadioOptionSelected("type", "projectrole");
        selectOption("projectrole", roleName);
        submit();
    }

    public void removeGroupFromSecurityLevel(String scheme_name, String level_name, String groupName)
    {
        gotoIssueSecuritySchemes();
        clickLinkWithText(scheme_name);

        clickLink("delGroup_" + groupName + "_" + level_name);
        submit("Delete");

    }

    public void removeRoleFromSecurityLevel(String scheme_name, String level_name, String roleId)
    {
        gotoIssueSecuritySchemes();
        clickLinkWithText(scheme_name);

        clickLink("delGroup_" + roleId + "_" + level_name);
        submit("Delete");
    }

    public void deleteSecurityScheme(String scheme_name)
    {
        gotoIssueSecuritySchemes();
        clickLink("del_" + scheme_name);

        assertTextPresent("Delete Issue Security Scheme");
        assertTextPresent(scheme_name);
        submit("Delete");
    }

    public void deleteSecurityLevel(String scheme_name, String level_name)
    {
        gotoIssueSecuritySchemes();
        clickLinkWithText(scheme_name);
        clickLink("delLevel_" + level_name);

        assertTextPresent("Delete Issue Security Level: " + level_name);
        submit("Delete");
    }

    public void removeAssociationOfSecuritySchemeFromProject(String project_name)
    {
        associateSecuritySchemeToProject(project_name, "None");
    }

    public void associateSecuritySchemeToProject(String project_name, String scheme_name)
    {
        grantGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);

        final Project projectByName = getProjectByName(project_name);

        gotoPage("/secure/project/SelectProjectIssueSecurityScheme!default.jspa?projectId=" + projectByName.id);

        selectOption("newSchemeId", scheme_name);
        submit();

        assertTextPresent("Step 2 of 2");

        submit("Associate");

        removeGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
    }

    public void gotoWorkFlowScheme()
    {
        gotoPage("/secure/admin/ViewWorkflowSchemes.jspa");
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.ViewWorkflows#goTo()} instead.
     */
    @Deprecated
    public void gotoWorkFlow()
    {
        gotoPage("/secure/admin/workflows/ListWorkflows.jspa");
    }

    public void addWorkFlowScheme(String workflowscheme_name, String workflowscheme_desc)
    {
        gotoWorkFlowScheme();
        clickLink("add_workflowscheme");
        setFormElement("name", workflowscheme_name);
        setFormElement("description", workflowscheme_desc);
        submit("Add");
    }

    public void deleteWorkFlowScheme(String workflowscheme_name)
    {
        gotoWorkFlowScheme();
        String linkId = "del_" + workflowscheme_name;
        if (getDialog().isLinkPresent(linkId))
        {
            clickLink(linkId);
            submit("Delete");
        }
        else
        {
            log("Workflow Scheme" + workflowscheme_name + " already deleted.");
        }
    }

    /**
     * Adds a workflow with the given name and description.
     *
     * @param workflow_name name of the workflow.
     * @param workflow_desc description of the workflow.
     */
    public void addWorkFlow(String workflow_name, String workflow_desc)
    {
        gotoWorkFlow();
        clickLink("add-workflow");
        setFormElement("newWorkflowName", workflow_name);
        setFormElement("description", workflow_desc);
        submit("Add");
    }

    public void deleteWorkFlow(String workflow_name)
    {
        gotoWorkFlow();
        String linkId = "del_" + workflow_name;
        if (getDialog().isLinkPresent(linkId))
        {
            clickLink(linkId);
            submit("Delete");
        }
        else
        {
            log("Workflow " + workflow_name + " already deleted.");
        }
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.ViewWorkflows#copyWorkflow(String, String,
     *             String)} instead.
     */
    @Deprecated
    public void copyWorkFlow(String original_workflow_name, String new_workflow_name, String new_workflow_desc)
    {
        gotoWorkFlow();
        clickLink("copy_" + original_workflow_name);
        setFormElement("newWorkflowName", new_workflow_name);
        setFormElement("description", new_workflow_desc);
        submit("Update");
    }

    public void addLinkedStatus(String status_name, String status_desc)
    {
        gotoPage("/secure/admin/AddStatus!default.jspa");
        setFormElement("name", status_name);
        setFormElement("description", status_desc);
        submit("Add");
    }

    public void deleteLinkedStatus(String statusId)
    {
        gotoPage("/secure/admin/ViewStatuses.jspa");
        clickLink("del_" + statusId);
        submit("Delete");
    }

    public void deleteStep(String workflow_name, String step_name)
    {
        gotoWorkFlow();
        clickLink("edit_live_" + workflow_name);
        clickLink("workflow-text");
        clickLinkWithText(step_name);
        clickLink("del_step");
        submit("Delete");
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.WorkflowSteps#addTransition(String, String,
     *             String, String, String)} instead.
     */
    @Deprecated
    public void addTransition(String workflow_name, String step_name, String transition_name, String transition_desc, String destination_step, String transitionFieldScreen)
    {
        gotoWorkFlow();
        clickLink("edit_live_" + workflow_name);
        clickLink("workflow-text");
        navigation.clickLinkWithExactText(step_name);
        clickLink("add_transition");
        setFormElement("transitionName", transition_name);
        setFormElement("description", transition_desc);
        selectOption("destinationStep", destination_step);
        if (transitionFieldScreen != null)
        {
            selectOption("view", transitionFieldScreen);
        }

        submit("Add");
    }

    public void editTransitionScreen(String workflow_name, String transition_name, String transitionFieldScreen)
    {
        administration.workflows().goTo().workflowSteps(workflow_name);
        clickLinkWithText(transition_name);
        clickLink("edit_transition");
        setFormElement("transitionName", transition_name);
        if (transitionFieldScreen != null)
        {
            selectOption("view", transitionFieldScreen);
        }

        submit();
    }

    public void deleteTransition(String workflow_name, String step_name, String transition_name)
    {
        gotoWorkFlow();
        clickLink("edit_live_" + workflow_name);
        clickLink("workflow-text");
        clickLinkWithText(step_name);
        clickLink("del_transition");
        selectOption("transitionIds", transition_name);
        submit("Delete");
    }

    public void activateWorkflow(String workflow_name)
    {
        gotoWorkFlow();
        clickLink("activate_" + workflow_name);
        submit("Activate");
    }

    public void assignWorkflowScheme(long workflowscheme_id, String issuetype, String workflow_name)
    {
        backdoor.workflowSchemes().updateScheme(backdoor.workflowSchemes().getWorkflowScheme(workflowscheme_id).setMapping(issuetype, workflow_name));
    }

    public void unassignWorkflowScheme(String workflowscheme_name, String issuetype, String workflow_name)
    {
        gotoWorkFlowScheme();
        clickLinkWithText(workflowscheme_name);
        clickLink("del_" + issuetype + "_" + workflow_name);
        submit("Delete");
    }

    /**
     * @param project the project name
     * @param workflow_scheme the scheme name
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.Project#associateWorkflowScheme(String,
     *             String)} instead.
     */
    @Deprecated
    public void associateWorkFlowSchemeToProject(String project, String workflow_scheme)
    {
        associateWorkFlowSchemeToProject(project, workflow_scheme, null);
    }

    /**
     * Check that certain fields have been indexed correctly. This will move to the XML Index View of a given issuekey,
     * then execute some xpath to assert that the index is in order.
     *
     * @param path xpath expression of the base node we are checking.  Eg: "//item"
     * @param expectedItemNodes Map containing key (xpath expression starting from path) : value (the expected value).
     * For example: "description" : "some description"
     * @param unexpectedItemNodes List of nodes from path that are *not* expected to be present. For example, if the
     * //item/[environment = "some environment"] should NOT be found, then the map would be: "environment" : "some
     * environment"
     * @param issueKey issue key of item we are checking.  Eg: "HSP-1"
     */
    protected void assertIndexedFieldCorrect(String path, Map expectedItemNodes, Map unexpectedItemNodes, String issueKey)
    {
        try
        {
            gotoPage("/si/jira.issueviews:issue-xml/" + issueKey + "/" + issueKey + ".xml?jira.issue.searchlocation=index");
            String responseText = getDialog().getResponse().getText();
            org.w3c.dom.Document doc = XMLUnit.buildControlDocument(responseText);
            assertEquals("text/xml", getDialog().getResponse().getContentType());

            if (expectedItemNodes != null)
            {
                for (Object o : expectedItemNodes.entrySet())
                {
                    Map.Entry pairs = (Map.Entry) o;
                    String itemNode = pairs.getKey().toString();
                    String expectedValue = pairs.getValue().toString();

                    String xpathExpression = path + "[" + itemNode + "= &quot;" + expectedValue + "&quot; ] ";
                    log("Searching for existence of xpath " + xpathExpression);
                    XMLAssert.assertXpathExists(xpathExpression, doc);
                }
            }

            if (unexpectedItemNodes != null)
            {
                for (Object o : unexpectedItemNodes.entrySet())
                {
                    Map.Entry pairs = (Map.Entry) o;
                    String itemNode = pairs.getKey().toString();
                    String expectedValue = pairs.getValue().toString();
                    String xpathExpression = path + "[" + itemNode + "= &quot;" + expectedValue + "&quot; ] ";
                    log("Searching for nonexistence of xpath " + xpathExpression);
                    XMLAssert.assertXpathNotExists(xpathExpression, doc);
                }
            }
        }
        catch (Throwable e)
        {
            raiseRuntimeException(e);
        }
        finally
        {
            //we need to go back to the an HTML area so that subsequent tests will not fail due to being in an XML area
            //even if it fails (so we can exxecute teardown)
            gotoPage("/secure/Dashboard.jspa");
        }
    }

    public void gotoPageNoLog(String url)
    {
        super.gotoPage(url);
    }

    public void gotoPage(String url)
    {
        log("going to page " + url);
        super.gotoPage(url);
    }


    /**
     * This will try and restrive the given URL and assert that it fails to be retrieved.
     *
     * @param assertionMessage the assertion message
     * @param url the url to the page we DONT want to exist
     */
    public void assertPageDoesNotExist(String assertionMessage, String url)
    {
        log("asserting that the page does not exist. [" + url + "]");
        try
        {
            super.gotoPage(url);
            fail("Page unexpectedly exists - " + assertionMessage + " [" + url + "]");
        }
        catch (RuntimeException e)
        {
            // expected - cant get more specific in terms of exception type however.
        }
    }

    public void gotoCustomFields()
    {
        gotoPage(PAGE_CUSTOM_FIELDS);
    }

    /**
     * Creates a global custom field for all issue types with the given details.
     *
     * @param fieldType use the constants CUSTOM_FIELD_TYPE*.
     * @param fieldName a name for the field.
     * @return returns the field id.
     */
    public String addCustomField(String fieldType, String fieldName)
    {
        String desc = "a custom field called " + fieldName + " of type " + fieldType;
        return addCustomField(fieldType, FIELD_SCOPE_GLOBAL, fieldName, desc, null, null, null);
    }

    public String addCustomField(String fieldType, String fieldScope, String fieldName, String fieldDescription, String issueType, String project, String searcher)
    {
        return addCustomFieldWithMultipleIssueTypes(fieldType, fieldScope, fieldName, fieldDescription, new String[] { issueType }, project, searcher);
    }

    public String addCustomFieldWithMultipleIssueTypes(String fieldType, String fieldScope, String fieldName, String fieldDescription, String[] issueTypes, String project, String searcher)

    {
        if (!getDialog().isLinkPresentWithText("Add Custom Field"))
        {
            gotoCustomFields();
        }

        clickLinkWithText("Add Custom Field");
        getDialog().setFormParameter("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:" + fieldType);
        assertRadioOptionSelected("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:" + fieldType);
        submit(BUTTON_NAME_NEXT);

        assertTextPresent("Step 2 of 2");
        assertTextPresent(fieldType);
        setFormElement("fieldName", fieldName);
        setFormElement("description", fieldDescription);

        if (searcher != null)
        {
            selectOption("searcher", searcher);
        }

        // Sets the context
        if (FIELD_SCOPE_GLOBAL.equalsIgnoreCase(fieldScope))
        {
            setFormElement(FIELD_SCOPE_GLOBAL, "true");
        }
        else
        {
            if (issueTypes != null)
            {
                for (String issueType : issueTypes)
                {
                    if (issueType != null)
                    {
                        selectMultiOption("issuetypes", issueType);
                    }
                }
            }
            if (project != null)
            {
                setFormElement(FIELD_SCOPE_GLOBAL, "false");
                selectOption("projects", project);
            }
        }

        submit(BUTTON_NAME_NEXT);

        gotoCustomFields();
        clickLink("edit_" + fieldName);
        return getDialog().getFormParameterValue("id");
    }

    public void deleteCustomField(String fieldId)
    {
        gotoCustomFields();
        clickLink("del_" + CUSTOM_FIELD_PREFIX + fieldId);
        submit("Delete");
    }

    public void addCustomFieldOption(String fieldId, String fieldOption)
    {
        if (!getDialog().isTextInResponse("Edit Options for Custom Field") || !getDialog().isTextInResponse(fieldId))
        {
            gotoCustomFields();
            clickLink("config_" + fieldId);
            clickLinkWithText("Edit Options");
            assertTextPresent("Edit Options for Custom Field");
        }
        setFormElement("addValue", fieldOption);
        submit("Add");
    }

    public void configureCustomFieldOption(String fieldId, String fieldOption)
    {
        if (!getDialog().isTextInResponse("Edit Options for Custom Field") || !getDialog().isTextInResponse(fieldId))
        {
            gotoCustomFields();
            clickLink("config_customfield_" + fieldId);
            clickLinkWithText("Edit Options");
            assertTextPresent("Edit Options for Custom Field");
        }
        setFormElement("addValue", fieldOption);
        submit("Add");
    }

    public void configureDefaultCustomFieldValue(String fieldId, String fieldOption)
    {
        gotoDefaultValue(fieldId);
        setFormElement("customfield_" + fieldId, fieldOption);
        submit("Set Default");
    }

    public void configureDefaultCheckBoxCustomFieldValue(String fieldId, String fieldOption)
    {
        gotoDefaultValue(fieldId);
        setFormElement(CUSTOM_FIELD_PREFIX + fieldId, "10016");
        submit("Set Default");
    }

    public void configureDefaultMultiCustomFieldValue(String fieldId, String fieldOption, String fieldOption2)
    {
        gotoDefaultValue(fieldId);
        selectOption(CUSTOM_FIELD_PREFIX + fieldId, fieldOption);

        selectOption(CUSTOM_FIELD_PREFIX + fieldId + ":1", fieldOption2);
        submit("Set Default");
    }

    private void gotoDefaultValue(String fieldId)
    {
        if (!getDialog().isTextInResponse("Set Custom Field Defaults") || !getDialog().isTextInResponse(fieldId))
        {
            gotoCustomFields();
            clickLink("config_customfield_" + fieldId);
            clickLinkWithText("Edit Default Value");
            assertTextPresent("Set Custom Field Defaults");
        }
    }

    public void delCustomFieldOption(String fieldId, String fieldOption)
    {
        if (!getDialog().isTextInResponse("Edit Options for Custom Field") || !getDialog().isTextInResponse(fieldId))
        {
            gotoCustomFields();
            clickLink("config_" + fieldId);
            clickLinkWithText("Edit Options");
            assertTextPresent("Edit Options for Custom Field");
        }
        clickLink("del_" + fieldOption);
        submit("Delete");
    }

    public void removeAllCustomFields()
    {
        while (true)
        {
            gotoCustomFields();
            if (!getDialog().isLinkPresentWithText("Del"))
            {
                break;
            }
            else
            {
                clickLinkWithText("Del");
                submit("Delete");
            }
        }
    }

    public String createCustomFields(String fieldType, String fieldScope, String fieldName, String fieldDescription, String issueType, String projectType, String[] fieldOptions)
    {
        String fieldId = addCustomField(fieldType, fieldScope, fieldName, fieldDescription, issueType, projectType, null);

        if (fieldOptions != null)
        {
            for (String fieldOption : fieldOptions)
            {
                addCustomFieldOption(CUSTOM_FIELD_PREFIX + fieldId, fieldOption);
            }
        }
        return fieldId;
    }

    public void editIssueWithCustomFields(String issueKey, List<CustomFieldValue> cfValues)
    {
        gotoIssue(issueKey);
        clickLink("edit-issue");

        for (CustomFieldValue cfValue : cfValues)
        {
            if (CUSTOM_FIELD_TYPE_RADIO.equals(cfValue.getCfType()))
            {
                getDialog().setFormParameter(CUSTOM_FIELD_PREFIX + cfValue.getCfId(), cfValue.getCfValue());
                assertRadioOptionSelected(CUSTOM_FIELD_PREFIX + cfValue.getCfId(), cfValue.getCfValue());
            }

            if (CUSTOM_FIELD_TYPE_TEXTFIELD.equals(cfValue.getCfType()) || CUSTOM_FIELD_TYPE_USERPICKER.equals(cfValue.getCfType()) || CUSTOM_FIELD_TYPE_DATEPICKER.equals(cfValue.getCfType()))
            {
                setFormElement(CUSTOM_FIELD_PREFIX + cfValue.getCfId(), cfValue.getCfValue());
            }

            if (CUSTOM_FIELD_TYPE_SELECT.equals(cfValue.getCfType()))
            {
                selectOption(CUSTOM_FIELD_PREFIX + cfValue.getCfId(), cfValue.getCfValue());
            }

            if (CUSTOM_FIELD_TYPE_MULTISELECT.equals(cfValue.getCfType()))
            {
                selectOption(CUSTOM_FIELD_PREFIX + cfValue.getCfId(), cfValue.getCfValue());
            }

            if (CUSTOM_FIELD_TYPE_CHECKBOX.equals(cfValue.getCfType()))
            {
                checkCheckbox(CUSTOM_FIELD_PREFIX + cfValue.getCfId(), cfValue.getCfValue());
            }
        }

        submit("Update");
    }

    public void editIssueWithCustomFields(String issueKey, String customFieldId, String customFieldValue, String customFieldType)
    {
        gotoIssue(issueKey);
        clickLink("edit-issue");

        if (CUSTOM_FIELD_TYPE_RADIO.equals(customFieldType))
        {
            getDialog().setFormParameter(CUSTOM_FIELD_PREFIX + customFieldId, customFieldValue);
            assertRadioOptionSelected(CUSTOM_FIELD_PREFIX + customFieldId, customFieldValue);
        }

        if (CUSTOM_FIELD_TYPE_TEXTFIELD.equals(customFieldType) || CUSTOM_FIELD_TYPE_USERPICKER.equals(customFieldType) || CUSTOM_FIELD_TYPE_DATEPICKER.equals(customFieldType))
        {
            setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, customFieldValue);
        }

        if (CUSTOM_FIELD_TYPE_SELECT.equals(customFieldType))
        {
            selectOption(CUSTOM_FIELD_PREFIX + customFieldId, customFieldValue);
        }

        if (CUSTOM_FIELD_TYPE_MULTISELECT.equals(customFieldType))
        {
            selectOption(CUSTOM_FIELD_PREFIX + customFieldId, customFieldValue);
        }

        if (CUSTOM_FIELD_TYPE_CHECKBOX.equals(customFieldType))
        {
            checkCheckbox(CUSTOM_FIELD_PREFIX + customFieldId, customFieldValue);
        }

        submit("Update");
    }

    public Collection<String> createIssuesInBulk(int numberOfIssues, String project, String projectKey, String issueType, String summary,
            String priority, String[] components, String[] affectsVersions, String[] fixVersions, String assignTo,
            String environment, String description, String originalEstimate, String securityLevel)
    {
        Collection<String> issuesTemp = new ArrayList<String>();
        for (int i = 0; i < numberOfIssues; i++)
        {
            issuesTemp.add(addIssue(project, projectKey, issueType, summary + i, priority, components, affectsVersions, fixVersions, assignTo, environment, description, originalEstimate, securityLevel, null));
        }

        return issuesTemp;
    }

    public void sortIssues(String field, String direction)
    {
        gotoPage("/secure/IssueNavigator.jspa?sorter/field=" + field + "&sorter/order=" + direction);
    }

    public void sortIssues(String page, String field, String direction)
    {
        gotoPage("/secure/" + page + "?sorter/field=" + field + "&sorter/order=" + direction);
    }

    public void deleteAllIssuesInAllPages()
    {
        boolean mailServerExists = isMailServerExists();

        grantGlobalPermission(BULK_CHANGE, Groups.USERS);
        displayAllIssues();
        log("Deleting all issues");
        assertElementPresent("issuetable");
        gotoPage("/secure/views/bulkedit/BulkEdit1!default.jspa?reset=true&tempMax=10000");

        assertTextPresent("Bulk Operation: " + "Choose Issues");

        tester.setWorkingForm("bulkedit");
        WebForm form = tester.getDialog().getForm();
        String[] parameterNames = form.getParameterNames();
        for (String name : parameterNames)
        {
            if (name.startsWith("bulkedit_"))
            {
                checkCheckbox(name);
            }
        }

        submit("Next");

        assertTextPresent("Bulk Operation: " + "Choose Operation");
        setFormElement("operation", "bulk.delete.operation.name");
        assertRadioOptionSelected("operation", "bulk.delete.operation.name");
        submit("Next");

        // Do nothing - send mail notification option not needed
        if (mailServerExists)
        {
            submit("Next");
        }

        assertTextPresent("Bulk Operation: " + "Confirmation");
        submit("Confirm");
        waitAndReloadBulkOperationProgressPage();
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
    }

    public void createSessionSearchForAll()
    {
        gotoPage("/issues/?jql=");
    }

    public boolean userExists(String userName)
    {
        gotoPage(PAGE_USER_BROWSER);
        return getDialog().isLinkPresentWithText(userName);
    }

    public boolean fieldSchemeExists(String fieldSchemeName)
    {
        gotoFieldLayoutSchemes();
        return getDialog().isTextInResponse(fieldSchemeName);
    }

    public boolean customFieldExists(String fieldName)
    {
        gotoCustomFields();
        return getDialog().isTextInResponse(fieldName);
    }

    public boolean subTaskTypeExists(String subTaskType)
    {
        activateSubTasks();
        boolean exists = getDialog().isLinkPresent("del_" + subTaskType);
        deactivateSubTasks();
        return exists;
    }

    public boolean securtiySchemeExists(String securityScheme)
    {
        clickOnAdminPanel("admin.schemes", "security_schemes");
        return getDialog().isTextInResponse(securityScheme);
    }

    public boolean securtiyLevelExists(String securityScheme, String securityLevel)
    {
        clickOnAdminPanel("admin.schemes", "security_scheme");
        clickLinkWithText(securityScheme);
        return getDialog().isLinkPresent("add_" + securityLevel);
    }

    public boolean permissionSchemeExists(String permissionScheme)
    {
        clickOnAdminPanel("admin.schemes", "permission_schemes");
        return getDialog().isLinkPresentWithText(permissionScheme);
    }

    public boolean workflowSchemeExists(String workflowScheme)
    {
        clickOnAdminPanel("admin.schemes", "workflow_schemes");
        return getDialog().isLinkPresentWithText(workflowScheme);
    }

    public boolean workflowExists(String workflow)
    {
        clickOnAdminPanel("admin.globalsettings", "workflows");
        return getDialog().isLinkPresent("steps_" + workflow);
    }

    public boolean linkedStatusExists(String linkedStatus)
    {
        clickOnAdminPanel("admin.issuesettings", "statuses");
        return getDialog().isLinkPresent("del_" + linkedStatus);
    }

    public boolean componentExists(String component, String project)
    {
        final Project projectByName = getProjectByName(project);

        final Component componentByname = getComponentByname(projectByName.key, component);
        return componentByname != null;

    }

    public boolean versionExists(String version, String project)
    {
        final Project projectByName = getProjectByName(project);
        final Version versionByName = getVersionByName(projectByName.key, version);

        return versionByName != null;
    }

    public boolean projectExists(String project)
    {
        final Project projectByName = getProjectByName(project);

        return projectByName != null;
    }

    public void gotoFieldScreens()
    {
        gotoPage("/secure/admin/ViewFieldScreens.jspa");
    }

    public void gotoFieldScreen(String screenName)
    {
        gotoFieldScreens();
        assertTextPresent("View Screens");
        assertLinkPresent("configure_fieldscreen_" + screenName);
        clickLink("configure_fieldscreen_" + screenName);
    }

    public void gotoFieldScreenSchemes()
    {
        gotoPage("/secure/admin/ViewFieldScreenSchemes.jspa");
    }

    public void gotoFieldScreenScheme()
    {
        gotoIssueTypeScreenSchemes();
        clickLink("configure_fieldscreenscheme_" + DEFAULT_SCREEN_SCHEME);
    }

    public void addFieldToFieldScreen(String screenName, String fieldName)
    {
        // Add custom field to the default screen
        log("Adding " + fieldName + " to field screen.");
        backdoor.screens().addFieldToScreen(screenName, fieldName);
    }

    public void addFieldsToFieldScreen(String screenName, String[] fieldNames)
    {
        // Add custom field to the default screen
        log("Adding fields to field screen.");
        gotoFieldScreen(screenName);
        assertTextPresent("Configure Screen");
        for (String fieldName : fieldNames)
        {
            backdoor.screens().addFieldToScreen(screenName, fieldName);
        }
    }

    public void addFieldToFieldScreen(String screenName, String fieldName, String position)
    {
        // Add custom field to the default screen
        backdoor.screens().addFieldToScreen(screenName, fieldName);
    }

    public void removeFieldFromFieldScreen(String screenName, String[] fieldNames)
    {
        log("Removing Fields from field screen.");

        gotoFieldScreen(screenName);
        assertTextPresent("Configure Screen");
        for (String fieldName : fieldNames)
        {
            backdoor.screens().removeFieldFromScreen(screenName, fieldName);
        }
    }

    public String findRowWithName(String fieldTableName, int column, String fieldName)
    {
        try
        {
            WebTable fieldTable = getDialog().getResponse().getTableWithID(fieldTableName);
            //if the table doesn't exist *no* fields are currently configured at all.
            if (fieldTable == null)
            {
                return null;
            }
            // First row is a headings row so skip it
            for (int i = 1; i < fieldTable.getRowCount(); i++)
            {
                String field = fieldTable.getCellAsText(i, column);
                if (field.contains(fieldName))
                {
                    // As we skipped the first row subtract 1
                    return Integer.toString(i - 1);
                }
            }
            return null;
        }
        catch (SAXException e)
        {
            fail("Cannot find table with id '" + FIELD_TABLE_ID + "'.");
            e.printStackTrace();
        }
        return null;
    }

    public void addScreen(String screenName, String screenDescription)
    {
        gotoFieldScreens();
        clickLink("add-field-screen");
        setWorkingForm("field-screen-add");
        setFormElement("fieldScreenName", screenName);
        setFormElement("fieldScreenDescription", screenDescription);
        submit("Add");
    }

    public void copyScreen(String copiedScreenName, String newScreenName, String screenDescription)
    {
        gotoFieldScreens();
        clickLink("copy_fieldscreen_" + copiedScreenName);
        setFormElement("fieldScreenName", newScreenName);
        setFormElement("fieldScreenDescription", screenDescription);
        submit("Copy");
    }

    public void deleteScreen(String screenName)
    {
        gotoFieldScreens();
        clickLink("delete_fieldscreen_" + screenName);
        submit("Delete");
    }

    public void removeAllFieldScreens()
    {
        while (true)
        {
            gotoFieldScreens();
            try
            {
                assertLinkNotPresentWithText("Delete");
                break;
            }
            catch (Throwable t)
            {
                clickLinkWithText("Delete");
                submit("Delete");
            }
        }
    }

    public void removeAllFieldScreenSchemes()
    {
        while (true)
        {
            gotoIssueTypeScreenSchemes();
            try
            {
                assertLinkNotPresentWithText("Delete");
                break;
            }
            catch (Throwable t)
            {
                clickLinkWithText("Delete");
                submit("Delete");
            }
        }
    }

    public void removeAllScreenAssociationsFromDefault()
    {
        while (true)
        {
            gotoViewFieldScreenSchemes();
            clickLink("configure_fieldscreenscheme_" + DEFAULT_SCREEN_SCHEME);
            try
            {
                assertLinkNotPresentWithText("Delete");
                break;
            }
            catch (Throwable t)
            {
                clickLinkWithText("Delete");
            }
        }
    }

    // Professional Helper Functions
    public void gotoFieldScreenScheme(String schemeName)
    {
        gotoViewFieldScreenSchemes();
        clickLink("configure_fieldscreenscheme_" + schemeName);
    }

    private void gotoViewFieldScreenSchemes()
    {
        gotoPage(PAGE_NOT_STANDARD_VIEW_FIELD_SCREEN_SCHEMES);
    }

    public void addFieldScreenScheme(String schemeName, String schemeDescription, String fieldScreenDefault)
    {
        gotoViewFieldScreenSchemes();
        clickLink("add-field-screen-scheme");
        setWorkingForm("field-screen-scheme-add");
        setFormElement("fieldScreenSchemeName", schemeName);
        setFormElement("fieldScreenSchemeDescription", schemeDescription);
        submit("Add");
    }

    public void copyFieldScreenScheme(String copiedSchemeName, String schemeName, String schemeDescription)
    {
        gotoViewFieldScreenSchemes();
        clickLink("copy_fieldscreenscheme_" + copiedSchemeName);
        setFormElement("fieldScreenSchemeName", schemeName);
        setFormElement("fieldScreenSchemeDescription", schemeDescription);
        submit("Copy");
    }

    public void deleteFieldScreenScheme(String schemeName)
    {
        gotoViewFieldScreenSchemes();
        try
        {
            assertLinkPresent("configure_fieldscreenscheme_" + schemeName);
        }
        catch (AssertionError e)
        {
            log("Scheme does not exist");
        }
        clickLink("delete_fieldscreenscheme_" + schemeName);
        submit("Delete");
    }

    public void addIssueOperationToScreenAssociation(String schemeName, String issueOperation, String screenName)
    {
        log("Adding screen " + screenName + " to operation '" + issueOperation + "'.");
        gotoFieldScreenScheme(schemeName);

        clickLink("add-screen-scheme-item");
        selectOption("issueOperationId", issueOperation);
        selectOption("fieldScreenId", screenName);
        submit("Add");
    }

    public void deleteIssueOperationFromScreenAssociation(String schemeName, String issueOperation)
    {
        log("Deleting operation '" + issueOperation + "' from scheme " + schemeName + ".");
        gotoFieldScreenScheme(schemeName);
        try
        {
            clickLink("delete_fieldscreenscheme_" + issueOperation);
        }
        catch (AssertionError e)
        {
            log("Issue Operation not configured");
        }
//        assertLinkNotPresent("delete_fieldscreenscheme_" + issueOperation);
    }

    public void removeAllFieldScreenAssociation(String schemeName)
    {
        while (true)
        {
            gotoFieldScreenScheme(schemeName);
            try
            {
                assertLinkNotPresentWithText("Delete");
                break;
            }
            catch (Throwable t)
            {
                clickLinkWithText("Delete");
            }
        }
    }

    public void removeAllIssueTypeScreenSchemes()
    {
        while (true)
        {
            gotoIssueTypeScreenSchemes();
            try
            {
                assertLinkNotPresentWithText("Delete");
                break;
            }
            catch (Throwable t)
            {
                clickLinkWithText("Delete");
                submit("Delete");
            }
        }
    }

    public void gotoIssueTypeScreenScheme(String schemeName)
    {
        gotoIssueTypeScreenSchemes();
        clickLink("configure_issuetypescreenscheme_" + schemeName);
    }

    public void addIssueTypeFieldScreenScheme(String schemeName, String schemeDescription, String defaultScreenScheme)
    {
        gotoIssueTypeScreenSchemes();
        clickLink("add-issue-type-screen-scheme");
        setFormElement("schemeName", schemeName);
        setFormElement("schemeDescription", schemeDescription);
        selectOption("fieldScreenSchemeId", defaultScreenScheme);
        submit("Add");
    }

    public void deleteIssueTypeFieldScreenScheme(String schemeId)
    {
        gotoIssueTypeScreenSchemes();
        clickLink("delete_issuetypescreenscheme_" + schemeId);
        submit("Delete");
    }

    public void copyIssueTypeFieldScreenSchemeName(String copiedSchemeId, String schemeName, String schemeDescription)
    {
        gotoIssueTypeScreenSchemes();
        clickLink("copy_issuetypescreenscheme_" + copiedSchemeId);
        setFormElement("schemeName", schemeName);
        setFormElement("schemeDescription", schemeDescription);
        submit("Copy");
    }

    protected void gotoIssueTypeScreenSchemes()
    {
        gotoPage(PAGE_ISSUE_TYPE_SCREEN_SCHEMES);
    }

    public void addIssueTypeToScreenAssociation(String issueTypeSchemeId, String issueType, String screenSchemeName)
    {
        gotoIssueTypeScreenScheme(issueTypeSchemeId);
        clickLink("add-issue-type-screen-scheme-configuration-association");
        selectOption("issueTypeId", issueType);
        selectOption("fieldScreenSchemeId", screenSchemeName);
        submit("Add");
    }

    public void associateIssueTypeScreenSchemeToProject(String projectName, String screenScheme)
    {
        Project project = getProjectByName(projectName);
        gotoPage("/secure/project/SelectIssueTypeScreenScheme!default.jspa?projectId=" + project.id);
        selectOption("schemeId", screenScheme);
        submit("Associate");
    }

    public void addTabToScreen(String screenName, String tabName)
    {
        backdoor.screens().addTabToScreen(screenName, tabName);
    }

    public void gotoFieldScreenTab(String screenName, String tabName)
    {
        gotoFieldScreen(screenName);
        try
        {
            assertTextPresent("Add one or more fields to the");
            assertTextPresentBeforeText("<b>" + tabName + "</b>", "tab.");
        }
        catch (AssertionFailedError e)
        {
            clickLinkWithText(tabName);
        }
    }

    public void deleteTabFromScreen(String screenName, String tabName)
    {
        gotoFieldScreenTab(screenName, tabName);
        clickLinkWithText("Delete");
        submit("Delete");
    }

    public void addFieldToFieldScreenTab(String screenName, String tabName, String fieldName, String position)
    {
        backdoor.screens().addFieldToScreenTab(screenName, tabName, fieldName, position);
    }

    public void removeFieldFromFieldScreenTab(String screenName, String tabName, String[] fieldNames)
    {
        gotoFieldScreenTab(screenName, tabName);
        assertTextPresent("Configure Screen");
        for (String fieldName : fieldNames)
        {
            String indexName = findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, fieldName);
            if (indexName != null)
            {
                int index = Integer.parseInt(indexName);
                checkCheckbox("removeField_" + index);
                assertCheckboxSelected("removeField_" + index);
            }
            else
            {
                log("Field " + fieldName + " not present");
            }
        }

        submit("deleteFieldsFromTab");
    }

    public void restoreDefaultDashboard()
    {
        clickLink("home_link");
        clickLinkWithText("Manage Dashboard");
        if (getDialog().isLinkPresentWithText("Restore Defaults"))
        {
            clickLinkWithText("Restore Defaults");
            submit("Restore");
        }
    }

    /**
     * @deprecated use new FuncTestCase way.
     */
    public void startDashboardConfiguration()
    {
        clickLink("home_link");
        if (getDialog().isLinkPresent("configure_on"))
        {
            clickLink("configure_on");
        }

    }

    public void browseToFullConfigure()
    {
        clickLink("home_link");
        clickLinkWithText("Manage Dashboard");
        clickLinkWithText("Full configure");
    }

    /**
     * Adds the portlet with the given name up to the point where the portlet configuration form comes up. Callers need
     * to complete that form to finish adding the portlet.
     *
     * @param portlet name of the portlet as defined in the plugin config.
     */
    public void addPortlet(String portlet)
    {
        startDashboardConfiguration();
        clickLink("home_link");
        clickLinkWithText("Add a new gadget.");
        setFormElement("portletId", "com.atlassian.jira.plugin.system.portlets:" + portlet);
        assertRadioOptionSelected("portletId", "com.atlassian.jira.plugin.system.portlets:" + portlet);
        submit(" Add ");
    }

    public int saveFilter(String filterName, String filterDesc)
    {
        clickLinkWithText("Save");
        setFormElement("filterName", filterName);
        setFormElement("filterDescription", filterDesc);
        submit("saveasfilter_submit");
        gotoManageFilter();
        assertLinkPresentWithText(filterName);
        int filterId = Integer.parseInt(extractFilterId(filterName));
        log("Saved filter: '" + filterName + "' [" + filterId + "]");
        return filterId;
    }

    public int saveFilterAs(String filterName, String copyName, String copyDesc, String saveColumnOrder)
    {
        gotoManageFilter();
        String filterId = extractFilterId(filterName);
        clickLinkWithText(filterName);
        if (!saveColumnOrder.equalsIgnoreCase("ignore"))
        {
            clickLinkWithText("Use your default Column Order");
        }
        clickLinkWithText("Save as");
        setFormElement("filterName", copyName);
        setFormElement("filterDescription", copyDesc);
        if (!saveColumnOrder.equalsIgnoreCase("ignore"))
        {
            setFormElement("saveColumnLayout", String.valueOf(saveColumnOrder));
        }
        else
        {
            assertFormElementNotPresent("saveColumnLayout");
        }
        submit("saveasfilter_submit");
        //dumpResponse();
        gotoManageFilter();
        assertLinkPresentWithText(copyName);
        int copyFilterId = Integer.parseInt(extractFilterId(copyName));
        log("Saved filter: '" + filterName + "' [" + filterId + "] as filter: '" + copyName + "' [" + copyFilterId + "]");
        return copyFilterId;
    }

    public String extractFilterId(String filterName)
    {
        try
        {
            filterName = "\">" + filterName + "</a>";
            String text = getDialog().getResponse().getText();
            int endOfFilterId = text.indexOf(filterName);
            int startOfFilterId = text.substring(0, endOfFilterId).lastIndexOf("requestId=") + "requestId=".length();
            String filterId = text.substring(startOfFilterId, endOfFilterId);
            log("filterId = " + filterId);
            return filterId;
        }
        catch (IOException e)
        {
            fail("Could not retrieve id for filter: '" + filterName + "'");
            return null;
        }
    }

    public void deleteAllFilter()
    {
        gotoManageFilter();

        clickOnMyFiltersTab();

        log("Deleting all filters");
        while (getDialog().isLinkPresentWithText("Delete"))
        {
            clickLinkWithText("Delete");
            submit("Delete");
        }
    }

    private void clickOnMyFiltersTab()
    {
        XPathLocator loc = new XPathLocator(tester, "//ul[@id='filter_type_table']/li/a/strong");
        if (loc.getText().contains("My"))
        {
            getNavigation().clickLinkWithExactText("My");
        }

        // make sure its the selected tab
        loc = new XPathLocator(tester, "//li[@class='active']/a");
        text.assertTextPresent(loc, "My");
    }

    /**
     * Deletes the filter with the given name if it exists. Leaves you on the manage filter page.
     *
     * @param filterName name of the filter to delete.
     */
    public void deleteFilter(String filterName)
    {
        gotoManageFilter();

        clickOnMyFiltersTab();

        if (getDialog().isLinkPresent("delete_" + filterName))
        {
            clickLink("delete_" + filterName);
            submit("Delete");
            assertLinkNotPresent("delete_" + filterName);
        }
    }

    /**
     * @param filterId The id of the filter to load
     * @deprecated please use {@link com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation#loadFilter(long)}
     */
    public void gotoFilterById(long filterId)
    {
        gotoManageFilter();
        clickLink("filterlink_" + filterId);
    }

    public void gotoFilter(String filtername)
    {
        gotoManageFilter();
        clickLinkWithText(filtername);
    }

    /**
     * @deprecated please use {@link com.atlassian.jira.functest.framework.navigation.FilterNavigation#allFilters()}
     */
    private void gotoManageFilter()
    {
        //clickLink("find_link");
        //clickLink("managefilters");

        gotoPage("/secure/ManageFilters.jspa?filterView=search&pressedSearchButton=true&searchName=&searchOwner=&Search=");
    }

    public void assertViewIssueFields()
    {
        assertTextPresent("View Field Configuration");
    }

    public void moveOptionsToPositions(String[] optionValue, String[] optionId, String itemType, Map<String, String> moveToPosition)
    {

        for (String currentPosition : moveToPosition.keySet())
        {
            String newPosition = moveToPosition.get(currentPosition);

            int currIntPos = Integer.parseInt(currentPosition);
            int newIntPos = Integer.parseInt(newPosition);

            //checks and asserts if the current option is before or after the position it is moving to
            if (currIntPos < newIntPos)
            {
                assertTextPresentBeforeText("<b>" + optionValue[currIntPos] + "</b>", "<b>" + optionValue[newIntPos] + "</b>");
            }
            else if (currIntPos > newIntPos)
            {
                assertTextPresentBeforeText("<b>" + optionValue[newIntPos] + "</b>", "<b>" + optionValue[currIntPos] + "</b>");
            }

            //sets the new position for the current option
            log("      Moving item at position " + currIntPos + " to position " + newPosition);
            setFormElement("new" + itemType + "Position_" + optionId[currIntPos], newPosition);
        }

        clickButtonWithValue("Move");

        //completes the Map by filling in missing positions
        for (int i = 1; i < optionValue.length; i++)
        {
            if (!moveToPosition.containsKey(String.valueOf(i)))
            {
                int k = 1;
                while (k <= i && moveToPosition.containsValue(String.valueOf(k)))
                {
                    k++;
                }
                moveToPosition.put(String.valueOf(i), String.valueOf(k));
            }
        }

        //checks if the options moved to its correct positions
        for (String currentOption : moveToPosition.keySet())
        {
            String newCurrentPos = moveToPosition.get(currentOption);
            String newReplacedPos = moveToPosition.get(newCurrentPos);

            int currentOptionInt = Integer.parseInt(currentOption);
            int newCurrentPosInt = Integer.parseInt(newCurrentPos);
            int otherOptionInt = Integer.parseInt(newCurrentPos);
            int newReplacedPosInt = Integer.parseInt(newReplacedPos);

            if (newCurrentPosInt < newReplacedPosInt)
            {
                assertTextPresentBeforeText("<b>" + optionValue[currentOptionInt] + "</b>", "<b>" + optionValue[otherOptionInt] + "</b>");
            }
            else if (newCurrentPosInt > newReplacedPosInt)
            {
                assertTextPresentBeforeText("<b>" + optionValue[otherOptionInt] + "</b>", "<b>" + optionValue[currentOptionInt] + "</b>");
            }
            //else item remained in the same position
        }
    }

    public void checkOrderingUsingArrows(String[] optionValue, String[] optionId)
    {
        //following for loops moves options using the ordering arrows such that by the end of the for loop it returns
        //all the options back into its original position - ie remain in ascending order
        log("Testing reordering using the option ordering arrows");
        log("  checking moveToLast arrows");
        int i;
        for (i = 1; i < optionValue.length; i++)
        {
            clickLink(MOVE_TO_LAST + optionId[i]);
            assertLinkNotPresent(MOVE_DOWN + optionId[i]);
            assertLinkNotPresent(MOVE_TO_LAST + optionId[i]);
        }
        checkItemsAreInAscendingOrder(optionValue);

        log("  checking moveToFirst arrows");
        for (i = optionValue.length - 1; i >= 1; i--)
        {
            clickLink(MOVE_TO_FIRST + optionId[i]);
            assertLinkNotPresent(MOVE_UP + optionId[i]);
            assertLinkNotPresent(MOVE_TO_FIRST + optionId[i]);
        }
        checkItemsAreInAscendingOrder(optionValue);

        log("  checking moveDown arrows");
        for (i = 1; i < optionValue.length - 1; i++)
        {
            clickLink(MOVE_DOWN + optionId[i]);
        }
        checkItemsAreInAscendingOrder(optionValue);

        log("  checking moveUp arrows");
        for (i = optionValue.length - 1; i >= 2; i--)
        {
            clickLink(MOVE_UP + optionId[i]);
        }
        checkItemsAreInAscendingOrder(optionValue);
    }

    public String checkOrderingUsingMoveToPos(String[] optionValue, String[] optionId, String itemType)
    {
        //the following moves options using the 'Move To Position' field.
        log("  Testing reordering using 'Move To Position' field inputs");

        log("    Test moving one item");
        resetInAscendingOrdering(optionId, itemType);
        moveOptionsToPositions(optionValue, optionId, itemType, easyMapBuild("1", "2"));

        log("    Test moving two items");
        resetInAscendingOrdering(optionId, itemType);
        moveOptionsToPositions(optionValue, optionId, itemType, easyMapBuild("1", "3", "2", "4"));

        log("    Test moving three items");
        resetInAscendingOrdering(optionId, itemType);
        moveOptionsToPositions(optionValue, optionId, itemType, easyMapBuild("1", "3", "2", "5", "3", "4"));

        return optionValue[1]; //this is for cascading select - to check ordering works at the lowest level
    }

    public void resetInAscendingOrdering(String[] optionId, String itemType)
    {
        if ("Field".equals(itemType))
        {
            createNewFieldScreen(optionId);
        }
        else
        {
            clickLinkWithText("Sort options alphabetically");
        }
    }

    public void createNewFieldScreen(String[] optionId)
    {
        removeAllFieldScreens();
        addScreen("new screen for reordering tests", "");

        int i = 1;
        while (i < optionId.length)
        {
            setFormElement("fieldId", optionId[i]);
            submit("Add");
            i++;
        }
    }

    public void checkItemsAreInAscendingOrder(String[] optionValue)
    {
        for (int i = 1; i < optionValue.length - 1; i++)
        {
            assertTextPresentBeforeText("<b>" + optionValue[i] + "</b>", "<b>" + optionValue[i + 1] + "</b>");
        }
    }

    @Deprecated
    public static <K, V> Map<K, V> easyMapBuild(K key1, V value1)
    {
        Map<K, V> map = new HashMap<K, V>(1);

        map.put(key1, value1);

        return map;
    }

    @Deprecated
    public static <K, V> Map<K, V> easyMapBuild(K key1, V value1,
            K key2, V value2)
    {
        Map<K, V> map = new HashMap<K, V>(2);

        map.put(key1, value1);
        map.put(key2, value2);

        return map;
    }

    @Deprecated
    public static <K, V> Map<K, V> easyMapBuild(K key1, V value1,
            K key2, V value2,
            K key3, V value3)
    {
        Map<K, V> map = new HashMap<K, V>(3);

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        return map;
    }

    public void viewChangeHistoryOfIssue(String issueKey)
    {
        gotoIssue(issueKey);
        if (getDialog().isLinkPresentWithText(CHANGE_HISTORY))
        {
            //for some bizarre unknown reason, on websphere 5.1 clicking the link doesnt work
            //and tries to view 'page=com.atlassian.jira.plugin.system.issuetabpanels%3Achangehistory-tabpanel'
            //clickLinkWithText(CHANGE_HISTORY);
            gotoPage("/browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel");
            assertLinkNotPresent(CHANGE_HISTORY);
        }
        else
        {
            assertTextPresent(CHANGE_HISTORY);
            //change log is already selected
        }
    }

    public void assertNoChangesForIssue(String issueKey)
    {
        viewChangeHistoryOfIssue(issueKey);
        final CssLocator chLocator = new CssLocator(tester, "div[id^=changehistory-]");
        assertEquals(0, chLocator.getNodes().length);
        assertTextPresent("created issue");
    }

    /**
     * Checkts that the last change history of the issue (with given issueKey) has an entry for the field with the
     * original and new value
     *
     * @param issueKey Which issue to check
     * @param field The issue field as displayed on the change history
     * @param originalValue The expected original value for the change history
     * @param newValue The expected new value for the change history
     * @deprecated please use {@link com.atlassian.jira.functest.framework.assertions.Assertions#assertLastChangeHistoryRecords(String,
     *             com.atlassian.jira.webtests.ztests.workflow.ExpectedChangeHistoryRecord)}
     */
    public void assertLastChangeHistoryIs(String issueKey, String field, String originalValue, String newValue)
    {
        viewChangeHistoryOfIssue(issueKey);

        String text;
        try
        {
            text = getDialog().getResponse().getText();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
            return;
        }

        // find the index of the last change history table
        final String CHANGE_HISTORY_ID_PREFIX = "changehistory_";
        int startOfLastChangeHistoryTableId = text.lastIndexOf("id=\"" + CHANGE_HISTORY_ID_PREFIX);
        int lastChangeHistoryTableId = -1;
        if (startOfLastChangeHistoryTableId != -1)
        {
            startOfLastChangeHistoryTableId += ("id=\"" + CHANGE_HISTORY_ID_PREFIX).length();
            int endOfLastChangeHistoryTableId = text.substring(startOfLastChangeHistoryTableId).indexOf('"') + startOfLastChangeHistoryTableId;
            lastChangeHistoryTableId = Integer.parseInt(text.substring(startOfLastChangeHistoryTableId, endOfLastChangeHistoryTableId));
        }

        if (lastChangeHistoryTableId == -1)
        {
            fail("Could not find the last change history table");
        }

        try
        {
            //check that the change history mapping specified is on any of the rows
            WebTable changeHistoryTable = getDialog().getResponse().getTableWithID(CHANGE_HISTORY_ID_PREFIX + lastChangeHistoryTableId);
            for (int row = 0; row < changeHistoryTable.getRowCount(); row++)
            {
                //if the change item exists, stop
                if (tableCellHasStrictText(changeHistoryTable, row, 0, field) &&
                        tableCellHasStrictText(changeHistoryTable, row, 1, originalValue) &&
                        tableCellHasStrictText(changeHistoryTable, row, 2, newValue))
                {
                    return;
                }
            }
            fail("The last change history for issue: " + issueKey + " did not have the change item: " + field + "[" + originalValue + "][" + newValue + "]");
        }
        catch (SAXException e)
        {
            raiseRuntimeException(e);
        }
    }

    public void assertLastChangeNotMadeToField(String issueKey, String field)
    {
        viewChangeHistoryOfIssue(issueKey);

        text.assertTextNotPresent(new XPathLocator(tester, "//div[@id=\"issue_actions_container\"]/div[last()]"), field);
    }

    public void assertErrorMsgFieldRequired(String fieldId, String project, String fieldDisplayName)
    {
        assertTextPresent("&quot;" + fieldId + "&quot; field is required and the project &quot;" + project + "&quot; does not have any " + fieldDisplayName);
    }

    // Helpfull bulk operations

    /**
     * simulates the clicking on bulk change all issues
     */
    public void bulkChangeIncludeAllPages()
    {
        gotoPage("/views/bulkedit/BulkEdit1!default.jspa?reset=true&tempMax=10000");
    }

    /**
     * simulates the clicking on bulk change current page of issues
     */
    public void bulkChangeIncludeCurrentPage()
    {
        gotoPage("/views/bulkedit/BulkEdit1!default.jspa?reset=true");
    }

    /**
     * Checks if the current step in bulk change is: Choose Issues
     */
    public void isStepChooseIssues()
    {
        assertTextPresent(STEP_PREFIX + STEP_CHOOSE_ISSUES);
        log("Step 1 of 4");
    }

    /**
     * Checks if the current step in bulk change is: Choose Operation
     */
    public void isStepChooseOperation()
    {
        assertTextPresent(STEP_PREFIX + STEP_CHOOSE_OPERATION);
        log("Step 2 of 4");
    }

    /**
     * Checks if the current step in bulk change is: Operation Details
     *
     * @deprecated please use {@link com.atlassian.jira.functest.framework.Workflows#assertStepOperationDetails()}
     */
    public void isStepOperationDetails()
    {
        assertTextPresent(STEP_PREFIX + STEP_OPERATION_DETAILS);
        log("Step 3 of 4");
    }

    /**
     * Checks if the current step in bulk change is: Confirmation.
     */
    public void isStepConfirmation()
    {
        assertTextPresent(STEP_CONFIRMATION);
        log("Step 4 of 4");
    }

    /**
     * checks a checkbox with cbox id, and confirms that is has been checked
     */
    public void selectCheckbox(String cbox)
    {
        assertCheckboxNotSelected(cbox);
        checkCheckbox(cbox);
        assertCheckboxSelected(cbox);
    }

    /**
     * selects the checkbox with id all<br> Used in the Step Choose Issues
     */
    public void bulkChangeChooseIssuesAll()
    {
        isStepChooseIssues();

        getDialog().setWorkingForm("bulkedit");
        String[] paramNames = getDialog().getForm().getParameterNames();
        for (String paramName : paramNames)
        {
            if (paramName.startsWith("bulkedit_"))
            {
                selectCheckbox(paramName);
            }
        }
        clickOnNext();
    }

    /**
     * Chooses the Delete Operation radio button in the Step Choose Operation
     */
    public void bulkChangeChooseOperationDelete(boolean mailServerExists)
    {
        isStepChooseOperation();
        setFormElement(FIELD_OPERATION, RADIO_OPERATION_DELETE);
        assertRadioOptionSelected(FIELD_OPERATION, RADIO_OPERATION_DELETE);
        log("Operation Selected: Delete Issues");
        clickOnNext();

        if (mailServerExists)
        {
            // Do nothing - send mail notification option not needed
            clickOnNext();
        }
    }

    /**
     * Chooses the Edit Operation radio button in the Step Choose Operation
     */
    public void bulkChangeChooseOperationEdit()
    {
        isStepChooseOperation();
        setFormElement(FIELD_OPERATION, RADIO_OPERATION_EDIT);
        assertRadioOptionSelected(FIELD_OPERATION, RADIO_OPERATION_EDIT);
        log("Operation Selected: Edit Issues");
        clickOnNext();
    }

    /**
     * Chooses the Move Operation radio button in the Step Choose Operation
     */
    public void chooseOperationBulkMove()
    {
        isStepChooseOperation();
        setFormElement(FIELD_OPERATION, RADIO_OPERATION_MOVE);
        assertRadioOptionSelected(FIELD_OPERATION, RADIO_OPERATION_MOVE);
        log("Operation Selected: Move Issues");
        clickOnNext();
    }

    /**
     * Chooses the Execute Worfklow Action radio button in the Step Choose Operation
     */
    public void chooseOperationExecuteWorfklowTransition()
    {
        isStepChooseOperation();
        setFormElement(FIELD_OPERATION, RADIO_OPERATION_WORKFLOW);
        assertRadioOptionSelected(FIELD_OPERATION, RADIO_OPERATION_WORKFLOW);
        log("Operation Selected: Transition Issues");
        clickOnNext();
    }

    /**
     * Clicks on the 'Next' button on any of the bulk change steps
     *
     * @deprecated please use {@link com.atlassian.jira.functest.framework.Navigation#clickOnNext()}
     */
    public void clickOnNext()
    {
        submit(BUTTON_NEXT);
        log("Next");
    }

    /**
     * Clicks on the 'Cancel' button on any of the bulk change steps
     */
    public void bulkChangeCancel()
    {
        //seems assertFormElementPresent("Cancel"); needs to be here to find the Cancel button?
        assertions.assertNodeHasText(new CssLocator(tester, "#cancel"), "Cancel");
        clickLink("cancel");
        assertTextPresent(LABEL_ISSUE_NAVIGATOR);
        log("Canceled");
    }

    /**
     * Clicks on the 'Confirm' button on the confirmation steps
     */
    public void bulkChangeConfirm()
    {
        isStepConfirmation();
        submit(BUTTON_CONFIRM);
        log("Confirmed");
    }

    /**
     * For the new framework version of this method, check out {@link com.atlassian.jira.functest.framework.util.env.EnvironmentUtils#getJiraJavaVersion()}
     */
    public float getJiraJavaVersion()
    {
        EnvironmentUtils environmentUtils = new EnvironmentUtils(tester, getEnvironmentData());
        return environmentUtils.getJiraJavaVersion();
    }

    /**
     * For the new framework version of this method, check out {@link com.atlassian.jira.functest.framework.util.env.EnvironmentUtils#isJavaBeforeJdk15()}
     */
    public boolean isBeforeJdk15()
    {
        return getJiraJavaVersion() < JDK_1_5_VERSION;
    }

    /**
     * @deprecated Use {@link Administration#restoreData(String)} instead.
     */
    @Deprecated
    public void restoreData(String fileName)
    {
        administration.restoreData(fileName);
        navigation.gotoDashboard();
    }

    /**
     * This restore uses the full Pico refresh the same as in Production.
     * <p/>
     * ie it does not do a "Quick Import".
     *
     * @param fileName XML Restore file
     * @see #restoreData(String)
     */
    public void restoreDataWithFullRefresh(String fileName)
    {
        restoreData(new File(getEnvironmentData().getXMLDataLocation(), fileName), false);
    }

    protected void copyFileToJiraImportDirectory(File file)
    {
        String filename = file.getName();

        if (!copiedFiles.contains(filename))
        {
            File jiraImportDirectory = new File(administration.getJiraHomeDirectory(), "import");
            try
            {
                FileUtils.copyFileToDirectory(file, jiraImportDirectory);
                copiedFiles.add(filename);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Could not copy file " + file.getAbsolutePath() +
                        " to the import directory in jira home " + jiraImportDirectory, e);
            }
        }
    }

    public void restoreData(File file)
    {
        restoreData(file, true);
    }

    public void restoreData(File file, boolean quickImport)
    {
        getNavigation().gotoAdminSection("system_info");
        final FuncTestTimer timer = TestInformationKit.pullTimer("XML Restore");

        copyFileToJiraImportDirectory(file);

        gotoPage("/secure/admin/XmlRestore!default.jspa");
        setWorkingForm("restore-xml-data-backup");
        setFormElement("filename", file.getName());
        setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        if (quickImport)
        {
            checkCheckbox("quickImport", "true");
        }

        submit();
        waitForRestore();
        try
        {
            assertTextPresent("Your import has been successful");
        }
        catch (AssertionFailedError e)
        {
            //The following are assertions of possible error messages to display the cause of failure to import
            //instead of having to check HTML dump manually. Please add/modify new error messages not included already.
            assertCauseOfError("The xml data you are trying to import seems to be from a newer version of JIRA. This will not work.");
            assertCauseOfError("You must enter the location of an XML file.");
            assertCauseOfError("Could not find file at this location.", file.getName());
            assertCauseOfError("Invalid license key specified.");
            assertCauseOfError("The current license is too old to install this version of JIRA");
            assertCauseOfError("Invalid license type for this edition of JIRA. License should be of type Standard.");
            assertCauseOfError("Invalid license type for this edition of JIRA. License should be of type Professional.");
            assertCauseOfError("Invalid license type for this edition of JIRA. License should be of type Enterprise.");
            assertCauseOfError("You must specify an index for the restore process.");
            assertCauseOfError("Error parsing export file. Your export file is invalid.");
            fail("Your JIRA data failed to restore successfully. See logs for details");
        }
        timer.end();

        getNavigation().disableWebSudo();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        // Debugs the count of the generic values
//        gotoPage("secure/admin/debug/debugGenericValues.jsp?decorator=none");
//        dumpResponse();
//        gotoPage("");

        // Set the baseURL to the correct thing! (AG-641)

        fixBaseURL();
        beginAt("/");
    }

    private void waitForRestore()
    {
        //wait for result page to come up
        String url = tester.getDialog().getResponse().getURL().toExternalForm();
        while (url.contains("importprogress"))
        {
            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
            }
            final String subUrl = url.substring(getEnvironmentData().getBaseUrl().toString().length());
            gotoPage(subUrl);
            url = tester.getDialog().getResponse().getURL().toExternalForm();
        }
    }

    /**
     * Restores the data from the file name without making any english assertions.
     *
     * @param fileName The name of the data file from which to restore the data
     */
    public void restoreI18nData(String fileName)
    {
        restoreI18nData(new File(getEnvironmentData().getXMLDataLocation(), fileName));
    }

    /**
     * Restores the data from the file name without making any english assertions.
     *
     * @param file The data file from which to restore the data
     */
    public void restoreI18nData(File file)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer("XML Restore");

        copyFileToJiraImportDirectory(file);

        gotoPage("/secure/admin/XmlRestore!default.jspa");
        setWorkingForm("jiraform");
        setFormElement("filename", file.getName());
        setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        checkCheckbox("quickImport", "true");
        submit();
        waitForRestore();
        try
        {
            // Check the login again button is there
            assertNotNull(new XPathLocator(tester, "//*[@id=\"login\"]").getNode());
        }
        catch (AssertionFailedError e)
        {
            fail("Your JIRA data failed to restore successfully. See logs for details");
        }
        timer.end();

        getNavigation().disableWebSudo();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);

        // Set the baseURL to the correct thing! (AG-641)
        String baseUrl = getEnvironmentData().getBaseUrl().toString();
        log("Setting baseurl to '" + baseUrl + "'");
        gotoAdmin();
        gotoPage("/secure/admin/jira/EditApplicationProperties!default.jspa");
        tester.setFormElement("baseURL", baseUrl);
        tester.submit();
        beginAt("/");
    }

    private void assertCauseOfError(String errorMessage)
    {
        assertCauseOfError(errorMessage, null);
    }

    /**
     * Check that the errorMessage is not present, if it is present the error message is the cause of the failure.
     * params is just to provide additional info that may be helpful in understanding the cause of the failure. Eg.
     * displaying the import file name when the import fails to find the file
     */
    private void assertCauseOfError(String errorMessage, String params)
    {
        try
        {
            assertTextNotPresent(errorMessage);
        }
        catch (AssertionFailedError e)
        {
            fail("Failed to restore JIRA data. Cause: " + errorMessage + (params != null ? " [" + params + "]" : ""));
        }
    }

    public void restoreDataWithLicense(String fileName, String licenseKey)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer("XML Restore");

        File file = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/" + fileName);
        copyFileToJiraImportDirectory(file);

        gotoPage("/secure/admin/XmlRestore!default.jspa");

        setWorkingForm("jiraform");
        setFormElement("filename", file.getName());
        setFormElement("license", licenseKey);
        checkCheckbox("quickImport", "true");
        submit();
        waitForRestore();
        assertTextPresent("Your import has been successful");
        timer.end();

        getNavigation().disableWebSudo();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);

        fixBaseURL();
        beginAt("/");
    }

    private void fixBaseURL()
    {
        // Set the baseURL to the correct thing! (AG-641)
        String baseUrl = getEnvironmentData().getBaseUrl().toString();
        log("Setting baseurl to '" + baseUrl + "'");
        gotoAdmin();
        tester.clickLink("general_configuration");
        tester.clickLink("edit-app-properties");
        tester.setFormElement("baseURL", baseUrl);
        tester.submit("Update");
    }

    // quick stop watch methods
    public void start()
    {
        stopWatch.start();
    }

    public void split()
    {
        stopWatch.split();
        log("Stop watch split at: " + stopWatch.toSplitString());
    }

    public void stop()
    {
        stopWatch.stop();
        log("Stop watch stopped at: " + stopWatch.toString());
    }

    /**
     * Restores the jira instance to one with no issues. Some projects have been created
     *
     * @deprecated Since 5.0. Use {@link com.atlassian.jira.functest.framework.backdoor.Backdoor#restoreBlankInstance()}
     *             instead.
     */
    @Deprecated
    public void restoreBlankInstance()
    {
        getAdministration().restoreBlankInstance();
    }

    public void enableUnassignedIssues()
    {
        gotoAdmin();
        clickLink("general_configuration");
        tester.clickLink("edit-app-properties");
        checkCheckbox("allowUnassigned", "true");
        submit("Update");
    }

    public void disableUnassignedIssues()
    {
        gotoAdmin();
        clickLink("general_configuration");
        tester.clickLink("edit-app-properties");
        checkCheckbox("allowUnassigned", "false");
        submit("Update");
    }

    public void setBaseUrl()
    {
        setBaseUrl(getEnvironmentData().getBaseUrl().toExternalForm());
    }

    /**
     * @deprecated please use {@link com.atlassian.jira.functest.framework.admin.GeneralConfiguration#setBaseUrl(String)}
     */
    public void setBaseUrl(String baseUrl)
    {
        gotoAdmin();
        clickLink("general_configuration");
        tester.clickLink("edit-app-properties");
        setFormElement("baseURL", baseUrl);
        submit("Update");
    }

    /**
     * Adds a stats portlet with the filter name
     */
    public void addIssueTypeStatsPortlet(String filterName)
    {
        browseToFullConfigure();
        submit("addButton");
        checkCheckbox("portletId", "com.atlassian.jira.plugin.system.portlets:filterstats");
        submit(" Add ");
        selectOption("filterid", filterName);
        selectOption("statistictype", "Issue Type");
        submit("Save");
    }

    public void createProjectCategory(String categoryName, String categoryDescription)
    {
        gotoProjectCategories();

        setFormElement("name", categoryName);
        setFormElement("description", categoryDescription);
        submit("Add");
    }

    public void deleteProjectCategory(String categoryName)
    {
        gotoProjectCategories();

        clickLink("del_" + categoryName);
        submit("Delete");
    }

    public void gotoProjectCategories()
    {
        gotoPage("/secure/admin/projectcategories/ViewProjectCategories!default.jspa");
    }

    public void placeProjectInCategory(String projectName, String categoryName)
    {
        final Project project = getProjectByName(projectName);

        gotoPage("/secure/project/SelectProjectCategory!default.jspa?pid=" + project.id);
        selectOption("pcid", categoryName);
        submit("Select");
    }

    public boolean projectCategoryExists(String categoryName)
    {
        gotoProjectCategories();

        try
        {
            assertTextPresent(categoryName);
        }
        catch (Throwable t)
        {
            return false;
        }
        return true;
    }

    /**
     * Use {@link Navigation#browseProject(String)} instead.
     *
     * @param key The project key.
     */
    @Deprecated
    public void gotoProjectBrowse(String key)
    {
        gotoPage("browse/" + key);
    }

    public void gotoVersionBrowse(String projectKey, String versionName)
    {
        gotoProjectBrowse(projectKey + "?selectedTab=com.atlassian.jira.jira-projects-plugin:versions-panel");
        clickLinkWithText(versionName);
        if (getDialog().isLinkPresentWithText("Select:"))
        {
            clickLinkWithText("Select:");
        }
    }

    public void gotoComponentBrowse(String projectKey, String componentName)
    {
        gotoProjectBrowse(projectKey + "?selectedTab=com.atlassian.jira.jira-projects-plugin:components-panel");
        clickLinkWithText(componentName);
        if (getDialog().isLinkPresentWithText("Select:"))
        {
            clickLinkWithText("Select:");
        }
    }


    /**
     * Goes to the specified issue tab panel for the issue with issueKy directly.
     *
     * @param issueKey issue key of the issue to see
     * @param issueTabName valid issue tab names are: <li>{@link #ISSUE_TAB_ALL} <li>{@link #ISSUE_TAB_COMMENTS}
     * <li>{@link #ISSUE_TAB_WORK_LOG} <li>{@link #ISSUE_TAB_CHANGE_HISTORY}
     */
    public void gotoIssueTabPanel(String issueKey, String issueTabName)
    {
        if (ISSUE_TAB_ALL.equals(issueTabName))
        {
            gotoPage("browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel");
        }
        else if (ISSUE_TAB_COMMENTS.equals(issueTabName))
        {
            gotoPage("browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel");
        }
        else if (ISSUE_TAB_WORK_LOG.equals(issueTabName))
        {
            gotoPage("browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:worklog-tabpanel");
        }
        else if (ISSUE_TAB_CHANGE_HISTORY.equals(issueTabName))
        {
            gotoPage("browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel");
        }
        else
        {
            fail("Invalid issue tab panel specified");
        }
    }

    /**
     * Goes to the specified project tab panel for the project with projectKey directly.
     *
     * @param projectKey project key of the project to see
     * @param projectTabName valid project tab names are: <li>{@link #PROJECT_TAB_OPEN_ISSUES} <li>{@link
     * #PROJECT_TAB_ROAD_MAP} <li>{@link #PROJECT_TAB_CHANGE_LOG}
     */
    public void gotoProjectTabPanel(String projectKey, String projectTabName)
    {
        if (PROJECT_TAB_OPEN_ISSUES.equals(projectTabName))
        {
            gotoPage("browse/" + projectKey + "?selectedTab=com.atlassian.jira.jira-projects-plugin:openissues-panel");
        }
        else if (PROJECT_TAB_ROAD_MAP.equals(projectTabName))
        {
            gotoPage("browse/" + projectKey + "?selectedTab=com.atlassian.jira.jira-projects-plugin:roadmap-panel");
        }
        else if (PROJECT_TAB_CHANGE_LOG.equals(projectTabName))
        {
            gotoPage("browse/" + projectKey + "?selectedTab=com.atlassian.jira.jira-projects-plugin:changelog-panel");
        }
        else if (PROJECT_TAB_VERSIONS.equals(projectTabName))
        {
            gotoPage("browse/" + projectKey + "?selectedTab=com.atlassian.jira.jira-projects-plugin:versions-panel");
        }
        else if (PROJECT_TAB_COMPONENTS.equals(projectTabName))
        {
            gotoPage("browse/" + projectKey + "?selectedTab=com.atlassian.jira.jira-projects-plugin:components-panel");
        }
        else
        {
            fail("Invalid project tab panel specified");
        }
        assertTextPresentBeforeText(projectTabName, projectTabName);
    }

    public void setFieldConfigurationFieldToRenderer(String configuration, String fieldId, String renderer)
    {
        setFieldConfigurationFieldToRenderer(configuration, fieldId, renderer, false);
    }

    public void setFieldConfigurationFieldToRenderer(String configuration, String fieldId, String renderer, boolean assertWarningNotPresent)
    {
        gotoFieldLayoutConfiguration(configuration);
        clickLink("renderer_" + fieldId);
        assertTextPresent("Edit Field Renderer");
        selectOption("selectedRendererType", renderer);
        if (assertWarningNotPresent)
        {
            assertTextNotPresent("Changing the renderer will effect the display of all ");
        }
        submit("Update");
        assertTextPresent("Edit Field Renderer Confirmation");
        assertTextPresent(renderer);
        submit("Update");
        log("Set " + fieldId + " to renderer type " + renderer + " in the " + configuration + " configuration.");
    }

    public void gotoFieldLayoutConfiguration(String configuration)
    {
        gotoFieldLayouts();
        clickLink("configure-" + configuration);
        assertTextPresent(configuration);
    }

    public void gotoPluginsScreen()
    {
        clickOnAdminPanel("admin.system_body", "plugins");
        assertTextPresent("Current Plugins");
    }

    public void gotoFieldLayouts()
    {
        gotoPage(PAGE_ENTERPRISE_FIELD_CONFIGURATIONS);
        assertTextPresent("View Field Configurations");
    }

    public void addEventType(String name, String description, String template)
    {
        gotoAdmin();
        clickLink("eventtypes");
        setFormElement("name", name);
        setFormElement("description", description);
        selectOption("templateId", template);
        submit("Add");
        checkEventTypeDetails(name, description, EVENT_TYPE_INACTIVE_STATUS, template, null, null);

        WebTable fieldTable;
        try
        {
            fieldTable = getDialog().getResponse().getTableWithID(EVENT_TYPE_TABLE);

            String eventTypeCellText = fieldTable.getCellAsText(fieldTable.getRowCount() - 1, EVENT_TYPE_TABLE_NAME_COL);
            assertTrue(eventTypeCellText.contains(name));
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
    }

    public void deleteEventType(String name)
    {
        gotoAdmin();
        clickLink("eventtypes");
        clickLink("del_" + name);
        assertTextPresent("Please confirm that you wish to delete the event: <b>" + name + "</b>.");
        submit("Delete");
        assertTextNotPresent(name);
    }

    public String getEventTypeIDWithName(String name)
    {
        gotoAdmin();
        clickLink("eventtypes");

        clickLink("edit_" + name);

        return getDialog().getFormParameterValue("eventTypeId");
    }

    // Check the event type table details for the specified event type
    public void checkEventTypeDetails(String eventTypeName, String eventTypeDesc, String status, String template, String notificationScheme, String workflow)
    {
        gotoAdmin();
        clickLink("eventtypes");

        WebTable fieldTable;
        try
        {
            fieldTable = getDialog().getResponse().getTableWithID(EVENT_TYPE_TABLE);

            // First row is a headings row so skip it
            for (int i = 1; i < fieldTable.getRowCount(); i++)
            {
                String field = fieldTable.getCellAsText(i, EVENT_TYPE_TABLE_NAME_COL);
                if (field.contains(eventTypeName))
                {
                    int EVENT_TYPE_TABLE_DESC_COL = 1;
                    String eventTypeCellText = fieldTable.getCellAsText(i, EVENT_TYPE_TABLE_DESC_COL);
                    assertTrue(eventTypeCellText.contains(eventTypeDesc));
                    int EVENT_TYPE_TABLE_STATUS_COL = 2;
                    eventTypeCellText = fieldTable.getCellAsText(i, EVENT_TYPE_TABLE_STATUS_COL);
                    assertTrue(eventTypeCellText.contains(status));
                    int EVENT_TYPE_TABLE_TEMPLATE_COL = 3;
                    eventTypeCellText = fieldTable.getCellAsText(i, EVENT_TYPE_TABLE_TEMPLATE_COL);
                    assertTrue(eventTypeCellText.contains(template));

                    if (notificationScheme != null && !notificationScheme.equals(""))
                    {
                        int EVENT_TYPE_TABLE_NOTIFIC_COL = 4;
                        eventTypeCellText = fieldTable.getCellAsText(i, EVENT_TYPE_TABLE_NOTIFIC_COL);
                        assertTrue(eventTypeCellText.contains(notificationScheme));
                    }

                    if (workflow != null && !workflow.equals(""))
                    {
                        int EVENT_TYPE_TABLE_WORKFLOW_COL = 5;
                        eventTypeCellText = fieldTable.getCellAsText(i, EVENT_TYPE_TABLE_WORKFLOW_COL);
                        assertTrue(eventTypeCellText.contains(workflow));
                    }
                }
            }
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
    }

    public void checkNotificationForEvent(String eventTypeName, String notificationType, String template)
    {
        gotoAdmin();
        clickLink("notification_schemes");
        clickLinkWithText("Default Notification Scheme");

        WebTable fieldTable;
        try
        {
            final String NOTIFICATION_SCHEME_TABLE = "notificationSchemeTable";
            fieldTable = getDialog().getResponse().getTableWithID(NOTIFICATION_SCHEME_TABLE);

            // First row is a headings row so skip it
            for (int i = 1; i < fieldTable.getRowCount(); i++)
            {
                int NOTIFICATION_TABLE_NAME_COL = 0;
                String field = fieldTable.getCellAsText(i, NOTIFICATION_TABLE_NAME_COL);
                if (field.contains(eventTypeName))
                {
                    int NOTIFICATION_TABLE_TYPE_COL = 1;
                    TableCell notificationCell = fieldTable.getTableCell(i, NOTIFICATION_TABLE_TYPE_COL);

                    if (notificationType == null)
                    {
                        assertTrue(!notificationCell.asText().contains(notificationType));
                    }
                    else
                    {
                        assertTrue(notificationCell.asText().contains(notificationType));
                    }
                }
            }

            gotoAdmin();
            clickLink("eventtypes");

        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.functest.framework.admin.IssueLinking#disable()} instead
     */
    @Deprecated
    public void disableIssueLinks()
    {
        clickOnAdminPanel("admin.globalsettings", "linking");
        assertTextPresent("Issue Linking");
        assertTextPresent("ON");
        submit("Deactivate");
        assertTextPresent("OFF");
    }

    /**
     * Creates the Cloners link type that JIRA creates between cloned issues. If this link type does not exist the clone
     * of teh issue is not linked to the original issue.
     */
    public void createClonersLinkType()
    {
        clickOnAdminPanel("admin.globalsettings", "linking");
        assertTextPresent("Issue Linking");
        assertTextPresent("ON");

        setFormElement("name", CLONERS_LINK_TYPE_NAME);
        setFormElement("outward", CLONERS_OUTWARD_LINK_NAME);
        setFormElement("inward", CLONERS_INWARD_LINK_NAME);
        submit("Add");
    }

    /**
     * Selects the 'listValue' in the 'field' list and checks that the corresponding 'field' checkbox is selected
     */
    public void setBulkEditFieldTo(String field, String listValue)
    {
        log("Set " + field + " To: \"" + listValue + "\"");
        checkCheckbox("actions", field);
        selectOption(field, listValue);
        assertOptionEquals(field, listValue);
//        setFormElement(field, listValue);
//        assertFormElementEquals(field, listValue);
    }

    /**
     * Chooses the bulk action(s) you wish to perform on the selected issue.<br> if a field is not to be selected place
     * "" in place of it.<br> Used in Operation Details
     *
     * @param fields A map woth field ids as keys and field values (have to be simple Strings) as values.
     */
    public void bulkEditOperationDetailsSetAs(Map<String, String> fields)
    {
        isStepOperationDetails();
        assertFormElementEquals("actions", null);

        for (Map.Entry<String, String> entry : fields.entrySet())
        {
            final String fieldName = entry.getKey();
            final String value = entry.getValue();
            if (FIELD_FIX_VERSIONS.equals(fieldName))
            {
                setBulkEditFieldTo(FIELD_FIX_VERSIONS, value);
            }
            if (FIELD_VERSIONS.equals(fieldName))
            {
                setBulkEditFieldTo(FIELD_VERSIONS, value);
            }
            if (FIELD_COMPONENTS.equals(fieldName))
            {
                setBulkEditFieldTo(FIELD_COMPONENTS, value);
            }
            if (FIELD_ASSIGNEE.equals(fieldName))
            {
                setBulkEditFieldTo(FIELD_ASSIGNEE, value);
            }
            if (FIELD_PRIORITY.equals(fieldName))
            {
                setBulkEditFieldTo(FIELD_PRIORITY, value);
            }
        }

        clickOnNext();
    }

    /**
     * Checks in step Confirmation of edit operation before confirming, whether or not the selected fields have been
     * made<br> DOES NOT goto the issues change log and check that they are changed after confirmation... something to
     * consider testing...
     *
     * @param fields a map with field ids as keys and simple Strings as values.
     */
    public void bulkEditConfirmEdit(Map<String, String> fields)
    {
        isStepConfirmation();

        for (Map.Entry<String, String> entry : fields.entrySet())
        {
            final String fieldName = entry.getKey();
            final String value = entry.getValue();
            if (FIELD_FIX_VERSIONS.equals(fieldName))
            {
                assertTextPresent("Fix Version/s");
                assertTextPresent(value);
            }
            if (FIELD_VERSIONS.equals(fieldName))
            {
                assertTextPresent("Affects Version/s");
                assertTextPresent(value);
            }
            if (FIELD_COMPONENTS.equals(fieldName))
            {
                assertTextPresent("Component/s");
                assertTextPresent(value);
            }
            if (FIELD_ASSIGNEE.equals(fieldName))
            {
                assertTextPresent("Assignee");
                assertTextPresent(value);
            }
            if (FIELD_PRIORITY.equals(fieldName))
            {
                assertTextPresent("Priority");
                assertTextPresent(value);
            }
        }
    }

    public void bulkChangeSelectIssue(String key)
    {
        isStepChooseIssues();
        bulkOperationCheckIssues(Arrays.asList(key));
        clickOnNext();
    }

    public void bulkChangeSelectIssues(Collection keys)
    {
        isStepChooseIssues();
        bulkOperationCheckIssues(keys);
        clickOnNext();
    }

    private void bulkOperationCheckIssues(Collection keys)
    {
        try
        {
            int checkBoxColumn = 0;
            WebTable table = getDialog().getResponse().getTableWithID(ISSUETABLE_ID);
            int keyColumn = -1;
            for (int i = 0; i < table.getColumnCount(); i++)
            {
                String headerCell = table.getCellAsText(ISSUETABLE_HEADER_ROW, i);
                if (headerCell.trim().equals("Key"))
                {
                    keyColumn = i;
                }
            }

            if (keyColumn < 0)
            {
                fail("Could not find column for Key");
            }

            int checkBoxesChecked = 0;
            for (int i = 0; i < table.getRowCount(); i++)
            {
                String key = table.getCellAsText(i, keyColumn);
                if (keys.contains(key.trim()))
                {
                    TableCell checkBoxCell = table.getTableCell(i, checkBoxColumn);
                    String[] elementNames = checkBoxCell.getElementNames();
                    boolean foundCheckbox = false;
                    for (String elementName : elementNames)
                    {
                        if (elementName.startsWith("bulkedit_"))
                        {
                            checkCheckbox(elementName);
                            if (++checkBoxesChecked >= keys.size())
                            {
                                // If we have selected all provided issue keys then there us no need to continue
                                // looking through the table. Return out of the method.
                                return;
                            }
                            else
                            {
                                // Otherwise no need to loop through the nodes. Continue with the next table row.
                                foundCheckbox = true;
                                break;
                            }
                        }
                    }

                    if (!foundCheckbox)
                    {
                        fail("Could not find the check box for issue with key '" + key + "'.");
                    }
                }
            }

        }
        catch (SAXException e)
        {
            e.printStackTrace();
            fail("Error occurred selecting issues.");
        }
    }

    /**
     * @return the id of the link type created.
     */
    public String createIssueLinkType(String name, String outwardLinkName, String inwardLinkName)
    {
        activateIssueLinking();
        setFormElement("name", name);
        setFormElement("outward", outwardLinkName);
        setFormElement("inward", inwardLinkName);
        submit();

        String text;
        String linkTypeId;

        try
        {
            text = getDialog().getResponse().getText();
            //first extract the href link to the link types delete link
            int linkIdLocation = text.indexOf("del_" + name);
            int endOfLinkIdLocation = text.indexOf(">Del", linkIdLocation);
            String deleteLinkLocation = text.substring(linkIdLocation, endOfLinkIdLocation);
            //now extract the actual id only
            linkIdLocation = deleteLinkLocation.indexOf("id=");
            endOfLinkIdLocation = deleteLinkLocation.indexOf("\"", linkIdLocation);
            linkTypeId = deleteLinkLocation.substring(linkIdLocation + "id=".length(), endOfLinkIdLocation);
        }
        catch (Exception e)
        {
            fail("Unable to retrieve Link Id for link type " + name + " : " + e.getMessage());
            return "fail";
        }

        return linkTypeId;
    }

    public void linkIssueWithComment(String currentIssueKey, String link, String destinationIssueKey, String comment, String commentLevel, String expectedText)
    {
        log("Link Issue: test linking an issue");
        activateIssueLinking();

        final StringBuilder url = new StringBuilder().append("/secure/LinkJiraIssue.jspa?atl_token=").append(getPage().getXsrfToken()).
                append("&id=").append(getIssueIdWithIssueKey(currentIssueKey)).append("&linkDesc=").append(link).append("&currentIssueKey=").
                append(currentIssueKey).append("&issueKeys=").append(destinationIssueKey);
        if (comment != null)
        {
            url.append("&comment=").append(comment).append("&commentLevel=").append(commentLevel);

        }
        gotoPage(url.toString());
        assertTextPresent(expectedText);
    }

    public void linkIssueWithComment(String currentIssueKey, String link, String destinationIssueKey, String comment, String commentLevel)
    {
        linkIssueWithComment(currentIssueKey, link, destinationIssueKey, comment, commentLevel, destinationIssueKey);
    }

    public boolean isMailServerExists()
    {
        gotoAdmin();
        clickLink("outgoing_mail");

        try
        {
            assertTextPresent("You do not currently have an SMTP server configured.");
            return false;
        }
        catch (Exception e)
        {
            return true;
        }
    }

    /**
     * Reads the HTML response text and finds the first group match for the given regex
     *
     * @param regex regex
     * @return The first matching group (first pair of parens) or null if there none is found.
     * @throws org.apache.oro.text.regex.MalformedPatternException if regex is malformed
     */
    public String getRegexMatch(String regex) throws MalformedPatternException
    {
        String html = getDialog().getResponseText();
        Perl5Compiler compiler = new Perl5Compiler();
        Pattern pattern = compiler.compile(regex, Perl5Compiler.EXTENDED_MASK | Perl5Compiler.MULTILINE_MASK);
        Perl5Matcher matcher = new Perl5Matcher();
        if (!matcher.contains(html, pattern))
        {
            return null;
        }
        MatchResult mr = matcher.getMatch();
        return mr.group(1);
    }

    /**
     * Detects whether or not the passed regular expression matches the page.
     *
     * @param regex the regular expression to match.
     * @param multiline true iff the regex should match across lines, false otherwise.
     */

    public void assertRegexMatch(String regex, boolean multiline)
    {
        Perl5Compiler compiler = new Perl5Compiler();
        Pattern pattern = null;

        int flags = Perl5Compiler.EXTENDED_MASK;
        if (multiline)
        {
            flags = flags | Perl5Compiler.MULTILINE_MASK;
            log("Asserting regular expression \"" + regex + "\" matches the page.");
        }
        else
        {
            log("Asserting regular expression \"" + regex + "\" matches line on the page.");
        }

        try
        {
            pattern = compiler.compile(regex, flags);
        }
        catch (MalformedPatternException e)
        {
            fail("Regular expression '" + regex + "' is not valid: " + e.getMessage());
        }

        String html = getDialog().getResponseText();
        Perl5Matcher matcher = new Perl5Matcher();

        if (matcher.contains(html, pattern))
        {
            fail("Regular expression '" + regex + "' did not match page.");
        }
    }

    public void gotoProjectRolesScreen()
    {
        gotoAdmin();
        clickLink("project_role_browser");
    }

    /**
     * @deprecated iconUrl is no longer available from web, use {@link
     * #editIssueType(String, String, String, Long)}
     */
    public void editIssueType(String issueTypeId, String name, String description, String iconUrl)
    {
        editIssueType(issueTypeId, name, description, (Long)null );
    }

    /**
     * Modifies the issue type with the given id to have the given properties.
     *
     * @param issueTypeId the id of the issue type to edit.
     * @param name the new name of the issue type.
     * @param description the new description of the issue type
     */
    public void editIssueType(String issueTypeId, String name, String description)
    {
        editIssueType(issueTypeId, name, description, (Long)null);
    }

    /**
     * Modifies the issue type with the given id to have the given properties.
     *
     * @param issueTypeId the id of the issue type to edit.
     * @param name the new name of the issue type.
     * @param description the new description of the issue type
     * @param avatarId the new avatar for issue type
     */
    public void editIssueType(String issueTypeId, String name, String description, Long avatarId)
    {
        gotoPage("/secure/admin/EditIssueType!default.jspa?id=" + issueTypeId);
        setFormElement("name", name);
        setFormElement("description", description);
        if ( null!=avatarId)
        {
            setFormElement("avatarId", String.valueOf(avatarId));
        }

        submit("Update");
    }

    /**
     * @deprecated Use #addIssueType(String, String) - iconUrl no longer available from web.
     */
    @Deprecated
    public String addIssueType(String name, String desc, String iconUrl)
    {
        return addIssueType(name, desc);
    }

    /**
     * Creates a custom issue type with the given properties. Make the name and description unique or things will be...
     * interesting.
     *
     * @param name The name of the issue type.
     * @param desc The description.
     * @return the id of the created issue type.
     */
    public String addIssueType(String name, String desc)
    {
        gotoAdmin();
        clickLink("issue_types");
        clickLink("add-issue-type");
        setFormElement("name", name);
        setFormElement("description", desc);
        submit("Add");
        assertTextPresent(name);
        assertTextPresent(desc);
        String response = getDialog().getResponseText();

        // Issue type with the largest ID is the most recently added.
        String editlinkBase = "EditIssueType!default.jspa?id=";

        int indexOfMatch = 0;
        Long largestIssueTypeId = 0L;

        while (indexOfMatch > -1)
        {
            indexOfMatch = response.indexOf(editlinkBase, indexOfMatch + 1);
            if (indexOfMatch > -1)
            {
                int endOfIdIndex = response.indexOf("\"", indexOfMatch + 1);
                if (endOfIdIndex > -1)
                {
                    Long id = Long.valueOf(response.substring(indexOfMatch + editlinkBase.length(), endOfIdIndex));
                    largestIssueTypeId = Math.max(largestIssueTypeId, id);
                }
            }
        }

        return largestIssueTypeId.toString();
    }

    public final void associateWorkFlowSchemeToProject(String project, String workflow_scheme, Map<String, String> statusMapping)
    {
        administration.project().associateWorkflowScheme(project, workflow_scheme, statusMapping, true);
    }

    /**
     * Turn on/off Project Roles + Groups visibility for comments and worklogs
     *
     * @param enable true = enable, false = disable
     */
    protected void enableCommentGroupVisibility(Boolean enable)
    {
        navigation.gotoAdmin();
        clickLink("general_configuration");
        tester.clickLink("edit-app-properties");
        setFormElement("title", "jWebTest JIRA installation");
        checkCheckbox("groupVisibility", enable.toString());
        submit("Update");
        if (enable)
        {
            assertTextPresent("Groups &amp; Project Roles");
            assertTextNotPresent("Project Roles only");
        }
        else
        {
            assertTextNotPresent("Groups &amp; Project Roles");
            assertTextPresent("Project Roles only");
        }
    }

    /**
     * Adds the given comment to the current issue, making it visible to all who can see the issue.
     *
     * @param comment the comment body.
     * @deprecated Use {@link com.atlassian.jira.functest.framework.navigation.IssueNavigation#addComment(String,
     *             String)} instead.
     */
    @Deprecated
    protected void addCommentOnCurrentIssue(String comment)
    {
        addCommentOnCurrentIssue(comment, null);
    }

    /**
     * Adds a comment on the current issue on the given role level or no level restriction (all users)
     *
     * @param comment the comment body.
     * @param roleLevel role level, null does not select any role level
     * @deprecated Use {@link com.atlassian.jira.functest.framework.navigation.IssueNavigation#addComment(String,
     *             String, String)} instead.
     */
    @Deprecated
    protected void addCommentOnCurrentIssue(String comment, String roleLevel)
    {
        clickLink("footer-comment-button");
        setFormElement("comment", comment);
        if (roleLevel != null)
        {
            selectOption("commentLevel", roleLevel);
        }
        submit();
    }

    public String getTitle(String responseHtml)
    {
        return getTagBody("<title>", "</title>", responseHtml);
    }

    public String getTagBody(String startTag, String endTag, String html)
    {
        int start = html.indexOf(startTag) + startTag.length();
        int end = html.indexOf(endTag, start);
        return html.substring(start, end);
    }

    /**
     * Method that checks if a particular table cell contains the text specified.
     *
     * @return Returns true if the text is contained in the table cell specified.
     */
    protected boolean tableCellHasText(WebTable table, int row, int col, String text)
    {
        log("Checking cell on row [" + row + "] col [" + col + "] for text [" + text + "]");
        String cellContent = table.getCellAsText(row, col);

        final boolean result = cellContent.contains(text);
        if (!result)
        {
            log("Expected '" + text + "' but was not found in '" + cellContent + "'");
        }
        return result;
    }

    protected boolean tableCellDoesNotHaveText(WebTable table, int row, int col, String text)
    {
        log("Checking cell on row [" + row + "] col [" + col + "] for text [" + text + "]");
        String cellContent = table.getCellAsText(row, col);

        final boolean result = !cellContent.contains(text);
        if (!result)
        {
            log("Didn't expect '" + text + "' but was found in '" + cellContent + "'");
        }
        return result;
    }

    /**
     * Same as {@link #tableCellHasText(com.meterware.httpunit.WebTable, int, int, String)} but if the text is an empty
     * string ("") than make sure the table cell trimmed is equal
     *
     * @return Returns true if the text is contained in the table cell specified.
     */
    protected boolean tableCellHasStrictText(WebTable table, int row, int col, String text)
    {
//        log("Checking cell [" + row + ", " + col + "] for strict text [" + text + "]");
        String cellContent = table.getCellAsText(row, col);

        final boolean result;
        if ("".equals(text))
        {
            result = "".equals(cellContent.trim());
        }
        else
        {
            result = cellContent.contains(text);
        }
        if (!result)
        {
//            log("Expected [" + text + "] was not found in cell [" + row + ", " + col + "] instead found [" + cellContent + "]");
        }
        return result;
    }

    /**
     * Checks if a particular table cell contains the link URL specified.
     *
     * @param link URL
     * @return True if the table cell contains the link URL specified.
     */
    protected boolean tableCellHasLinkThatContains(WebTable table, int row, int col, String link)
    {
        if (link == null)
        {
            return tableCellHasNoLinks(table, row, col);
        }
        else
        {
            log("Checking cell on row [" + row + "] col [" + col + "] for link [" + link + "]");
            TableCell tableCell = table.getTableCell(row, col);
            WebLink[] links = tableCell.getLinks();
            if (links != null)
            {
                for (WebLink webLink : links)
                {
                    String urlString = webLink.getURLString();
                    if (urlString != null && urlString.contains(link))
                    {
                        return true;
                    }
                }
            }
            log("Expected '" + link + "' but was not found in '" + table.getCellAsText(row, col) + "'");
            return false;
        }
    }

    protected void assertTableCellHasImage(WebTable table, int row, int col, String stringInImageSource)
    {
        assertTrue("Expected image not found. Please see logs for details.", new ImageCell(stringInImageSource).equals(table, row, col));
    }

    protected void assertTableCellHasNoImage(WebTable table, int row, int col)
    {
        WebImage[] webImages = table.getTableCell(row, col).getImages();
        if (webImages != null && webImages.length > 0)
        {
            fail("An image was found in a cell where it wasn't expected. First image = '" + webImages[0].getSource() + "'.");
        }
    }

    protected boolean tableCellHasNoLinks(WebTable table, int row, int col)
    {
        log("Checking cell on row [" + row + "] col [" + col + "] for no links");
        TableCell tableCell = table.getTableCell(row, col);
        WebLink[] links = tableCell.getLinks();
        boolean result = links == null || links.length == 0;
        if (!result)
        {
            log("Links were not expected but were found in '" + table.getTableCell(row, col).asText() + "'");
        }
        return result;
    }

    /**
     * @see #tableRowEquals(com.meterware.httpunit.WebTable, int, java.util.List)
     */
    private boolean tableRowEquals(WebTable table, int row, Object[] expectedRow)
    {
        return tableRowEquals(table, row, Arrays.asList(expectedRow));
    }

    /**
     * Checks if the row at the specified row number on the table matches the expectedRow. The rows match if all the
     * corresponding columns match.
     * <p/>
     * A column match is determined as follows, if the column of the expectedRow: <li> is null, it will assume the
     * column is correct (ie. ignored) <li> is {@link com.atlassian.jira.webtests.table.LinkCell} then it will use
     * {@link #tableCellHasLinkThatContains(com.meterware.httpunit.WebTable, int, int, String)} <li> else it will use
     * the {@link Object#toString()} and call {@link #tableCellHasStrictText(com.meterware.httpunit.WebTable, int, int,
     * String)}. ie. empty string will check that the trimmed column is also empty.
     *
     * @param table table to check has the expectedRow on row number row
     * @param row row number of the table to compare to the expectedRow (starts from 0)
     * @param expectedRow the row to compare to the tables row
     * @return true if the row at the specified row number equals to the expectedRow
     */
    private boolean tableRowEquals(WebTable table, int row, List expectedRow)
    {
        if (expectedRow.isEmpty())
        {
            log("expected row is empty");
            return false;
        }

        int maxCol = table.getColumnCount();
        for (int col = 0; col < expectedRow.size() && col < maxCol; col++)
        {
            Object expectedCell = expectedRow.get(col);
            if (expectedCell == null)
            {
                //if the expected cell is null, assume it's valid and ignore it
            }
            else if (expectedCell instanceof SimpleCell)
            {
                SimpleCell simpleCell = (SimpleCell) expectedCell;
                if (!simpleCell.equals(table, row, col))
                {
                    String cellContent = simpleCell.getCellAsText(table, row, col);
                    log("table '" + table.getID() + "' row '" + row + "' does not match expected row because cell [" + row + ", " + col + "] = [" + cellContent + "] does not match [" + expectedCell.toString() + "]");
                    return false;
                }
            }
            else if (!tableCellHasStrictText(table, row, col, expectedCell.toString()))
            {
                String cellContent = table.getCellAsText(row, col).trim();
                log("table '" + table.getID() + "' row '" + row + "' does not match expected row because cell [" + row + ", " + col + "] = [" + cellContent + "] does not match [" + expectedCell.toString() + "]");
                return false;
            }
        }
        return true;
    }

    /**
     * Assert that the specified row of the table is equal to the expectedRow
     *
     * @param table table to look up the row
     * @param row the row number of the table to compare
     * @param expectedRow the expected row to match
     * @deprecated please use {@link com.atlassian.jira.functest.framework.assertions.TableAssertions#assertTableRowEquals(com.meterware.httpunit.WebTable,
     *             int, Object[])}
     */
    public void assertTableRowEquals(WebTable table, int row, Object[] expectedRow)
    {
        assertTrue("Expected row '" + prettyParseRow(expectedRow) + "' does not match '" + getTableRowAsList(table, row) + "' (row '" + row + "' of table '" + table.getID() + "')", tableRowEquals(table, row, expectedRow));
    }

    /**
     * Get the specified row from the table as a list of trimmed strings.
     *
     * @param table table to get the row from
     * @param row the row index starting from 0 to extract the row from
     * @return list of trimmed cell values from the table on specified row.
     */
    public List<String> getTableRowAsList(WebTable table, int row)
    {
        List<String> tableRow = new ArrayList<String>();
        int maxCol = table.getColumnCount();
        for (int col = 0; col < maxCol; col++)
        {
            tableRow.add(table.getCellAsText(row, col).trim());
        }
        return tableRow;
    }

    /**
     * Asserts that the table has no row in the table matching the expectedRow
     *
     * @param table table to check the expectedRow does not exist in
     * @param expectedRow the row that should not be in the table
     */
    public void assertTableHasNoMatchingRow(WebTable table, Object[] expectedRow)
    {
        assertTrue("Found a row matching: '" + prettyParseRow(expectedRow) + "' on table '" + table.getID() + "' when unexpected",
                tableIndexOf(table, Arrays.asList(expectedRow)) == -1);
    }

    /**
     * Asserts that the table has no row in the table matching the expectedRow between minRow and the end of the table
     *
     * @param table table to check the expectedRow does not exist in
     * @param minRow the starting row index (inclusive)
     * @param expectedRow the row that should not be in the table
     */
    public void assertTableHasNoMatchingRow(WebTable table, int minRow, Object[] expectedRow)
    {
        assertTableHasNoMatchingRowFromTo(table, minRow, table.getRowCount(), expectedRow);
    }

    /**
     * Asserts that the table has no row in the table matching the expectedRow between minRow and maxRow
     *
     * @param table table to check the expectedRow does not exist in
     * @param minRow the starting row index (inclusive)
     * @param maxRow the ending row index (exclusive)
     * @param expectedRow the row that should not be in the table
     */
    public void assertTableHasNoMatchingRowFromTo(WebTable table, int minRow, int maxRow, Object[] expectedRow)
    {
        if (minRow > maxRow)
        {
            int temp = maxRow;
            maxRow = minRow;
            minRow = temp;
        }
        int rowIndex = tableIndexOf(table, Arrays.asList(expectedRow));
        if (rowIndex != -1)
        {
            if (rowIndex < minRow || rowIndex >= maxRow)
            {
                return;
            }
            fail("Found a row matching: '" + prettyParseRow(expectedRow) + "' on table '" + table.getID() + "' between min index [" + minRow + "] and max index [" + maxRow + "] when unexpected");
        }
    }

    /**
     * Asserts that the table has strictly 'n' number of rows in the table matching the expectedRow. ie. checks there
     * are 'n' and only 'n' number of matching rows.
     *
     * @param table table to check the expectedRow exist in
     * @param expectedRow the row that should be in the table
     * @param n the number of times the expectedRow should appear in the table
     */
    public void assertTableHasOnlyNMatchingRows(WebTable table, int n, Object[] expectedRow)
    {
        if (n > 0)
        {
            int lastMatchingRow = 0;
            List rowList = Arrays.asList(expectedRow);
            for (int c = 0; c < n; ++c)
            {
                lastMatchingRow = tableIndexOf(table, lastMatchingRow, rowList);
                assertTrue("Found '" + c + "' of '" + n + "' rows of '" + rowList + "' on table '" + table.getID() + "'", lastMatchingRow != -1);
                ++lastMatchingRow; //increment last row count so we dont include it in the search
            }
            //assert there are no more matching rows after the last match (remove this to check if there are atleast 'n' rows and not only 'n')
            assertTrue("Found more than '" + n + "' rows of '" + rowList + "' on table '" + table.getID() + "'", tableIndexOf(table, lastMatchingRow, rowList) == -1);
        }
        else //assert that the expected row does not exist.
        {
            assertTableHasNoMatchingRow(table, expectedRow);
        }
    }

    /**
     * Asserts that the table has at least one row matching the expectedRow
     *
     * @param table table to check for the expectedRow
     * @param expectedRow the row to look for
     * @deprecated please use {@link com.atlassian.jira.functest.framework.assertions.TableAssertions#assertTableContainsRow(com.meterware.httpunit.WebTable,
     *             String[])}
     */
    public void assertTableHasMatchingRow(WebTable table, Object[] expectedRow)
    {
        assertTrue("Did not find a row matching: '" + prettyParseRow(expectedRow) + "' on table '" + table.getID() + "'",
                tableIndexOf(table, Arrays.asList(expectedRow)) != -1);
    }

    /**
     * Asserts that the table has atleast one row matching the expectedRow
     *
     * @param table table to check for the expectedRow
     * @param minRow the starting row to look for the expectedRow (inclusive)
     * @param expectedRow the row to look for
     */
    public void assertTableHasMatchingRowFrom(WebTable table, int minRow, Object[] expectedRow)
    {
        log("Asserting table '" + table.getID() + "' has a row from '" + minRow + "' that matches '" + prettyParseRow(expectedRow) + "'");
        String message = "Did not find a row matching: '" + prettyParseRow(expectedRow) + "' on table '" + table.getID()
                + "' from row '" + minRow + "'";
        assertTrue(message, tableIndexOf(table, minRow, table.getRowCount(), Arrays.asList(expectedRow)) != -1);
    }

    /**
     * Asserts that the table has at least one row matching the expectedRow
     *
     * @param table table to check for the expectedRow
     * @param minRow the starting row to look for the expectedRow (inclusive)
     * @param maxRow the last row to look for the expectedRow (exclusive)
     * @param expectedRow the row to look for
     */
    public void assertTableHasMatchingRowFromTo(WebTable table, int minRow, int maxRow, Object[] expectedRow)
    {
        log("Asserting table '" + table.getID() + "' has a row from '" + minRow + "' to '" + maxRow + "' that matches '" + prettyParseRow(expectedRow) + "'");
        String message = "Did not find a row matching: '" + prettyParseRow(expectedRow) + "' on table '"
                + table.getID() + "' from row '" + minRow + "' to '" + maxRow + "'";

        int index = tableIndexOf(table, minRow, maxRow, Arrays.asList(expectedRow));
        assertTrue(message, index != -1);
    }

    public String prettyParseRow(Object[] expectedRow)
    {
        return prettyParseRow(Arrays.asList(expectedRow));
    }

    public String prettyParseRow(List expectedRow)
    {
        StringBuilder sb = new StringBuilder("\n");
        int col = 0;
        for (Iterator iterator = expectedRow.iterator(); iterator.hasNext(); col++)
        {
            Object o = iterator.next();
            sb.append("  Column '").append(col).append("' = ").append(o).append("\n");
        }
        return sb.toString();
    }

    public int tableIndexOf(WebTable table, Object[] expectedRow)
    {
        return tableIndexOf(table, Arrays.asList(expectedRow));
    }

    public int tableIndexOf(WebTable table, int minRow, Object[] expectedRow)
    {
        return tableIndexOf(table, minRow, Arrays.asList(expectedRow));
    }

    public int tableIndexOf(WebTable table, List expectedRow)
    {
        return tableIndexOf(table, 0, table.getRowCount(), expectedRow);
    }

    public int tableIndexOf(WebTable table, int minRow, List expectedRow)
    {
        return tableIndexOf(table, minRow, table.getRowCount(), expectedRow);
    }

    /**
     * Returns the row number (starting from 0) of the first row matching expectedRow on the table. minRow and maxRow
     * limit the range of rows to look on the table. Uses {@link #tableRowEquals(com.meterware.httpunit.WebTable, int,
     * java.util.List)} to determine if the rows match
     *
     * @param table table to look up the expectedRow
     * @param minRow the starting row to look for the expectedRow (inclusive)
     * @param maxRow the last row to look for the expectedRow (exclusive)
     * @param expectedRow the row to look for
     * @return row number of the matching expectedRow, -1 if not found
     */
    public int tableIndexOf(WebTable table, int minRow, int maxRow, List expectedRow)
    {
        log("Scanning table '" + table.getID() + "' from row '" + minRow + "' (inclusive) to row '" + maxRow + "' (exclusive) for '" + expectedRow + "'");
        if (minRow < 0)
        {
            return -1;
        }

        int lastRow = table.getRowCount() < maxRow ? table.getRowCount() : maxRow;
        for (int row = minRow; row < lastRow; row++)
        {
            if (tableRowEquals(table, row, expectedRow))
            {
                //success
                log("Found expected row on row '" + row + "' of table '" + table.getID() + "'");
                return row;
            }
        }
        return -1;
    }

    /**
     * Checks whether the given table has a subtable matching the expectedSubTable.
     *
     * @param table table to check if it has the subtable
     * @param expectedSubTable the subtable to look for
     * @return true if all the rows of the expectedSubTable are part of the table (in same order)
     */
    public boolean tableHasSubTable(WebTable table, Object[][] expectedSubTable)
    {
        int maxRow = table.getRowCount();
        for (int row = 0; row < maxRow; row++)
        {
            if (tableRowEquals(table, row, expectedSubTable[0]))
            {
                boolean isSubTable = true;
                for (int subRow = 1; subRow < maxRow && subRow < expectedSubTable.length && isSubTable; subRow++)
                {
                    if (!tableRowEquals(table, row + subRow, expectedSubTable[subRow]))
                    {
                        isSubTable = false;
                    }
                }
                if (isSubTable)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public void assertTableHasSubTable(WebTable table, Object[][] expectedSubTable)
    {
        StringBuilder sb = new StringBuilder("Table:");
        sb.append(table.getID());
        sb.append(": does not contain expected subTable : \n");
        for (int i = 0; i < expectedSubTable.length; i++)
        {
            Object arr = expectedSubTable[i];
            if (arr != null && arr.getClass().isArray())
            {
                sb.append("[");
                sb.append(i);
                sb.append("] ");
                Object[] innerArr = (Object[]) arr;
                for (Object o : innerArr)
                {
                    sb.append("'");
                    sb.append(String.valueOf(o));
                    sb.append("'");
                    sb.append(",");
                }
                sb.append("\n");
            }
        }
        assertTrue(sb.toString(), tableHasSubTable(table, expectedSubTable));
    }

    /**
     * Takes in a list of strings, iterates over them and asserts that each is present
     *
     * @param iterable of strings
     */
    public void assertTextListPresent(Iterable<String> iterable)
    {
        if (iterable != null)
        {
            for (final String text : iterable)
            {
                assertTextPresent(text);
            }
        }
    }

    /**
     * Takes in a list of strings, iterates over them and asserts that each is *NOT* present
     *
     * @param iterable of strings
     */
    public void assertTextListNotPresent(Iterable<String> iterable)
    {
        if (iterable != null)
        {
            for (final String text : iterable)
            {
                assertTextNotPresent(text);
            }
        }
    }

    /**
     * Use {@link com.atlassian.jira.functest.framework.assertions.CommentAssertions#areVisibleTo(String, String)} and
     * {@link com.atlassian.jira.functest.framework.assertions.CommentAssertions#areNotVisibleTo(String, String)}
     * instead.
     * <p/>
     * Given a username, view the issue with <code>issuekey</code> and assert that a given list of comments are visible
     * in the issue view and another given list of comments are not visible in issue view. NOTE: username must be the
     * same as the password
     *
     * @param usernameAndPassword - must be a valid login username/password combination where username = password
     * @param issueKey - issuekey of issue comments we are checking
     * @param expectedPresentComments - List of comments in the form of strings that should be visible to the user when
     * viewing that issue
     * @param expectedAbsentComments - List of comments in the form of strings that should *NOT* be visible to the user
     * when viewing that issue
     */
    @Deprecated
    public void checkCommentVisibility(String usernameAndPassword, String issueKey, Iterable<String> expectedPresentComments, Iterable<String> expectedAbsentComments)
    {
        logout();
        login(usernameAndPassword, usernameAndPassword);
        gotoIssue(issueKey);
        assertTextListPresent(expectedPresentComments);
        assertTextListNotPresent(expectedAbsentComments);
        // restore the admin login just to be sure
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void toggleExternalUserManagement(boolean enable)
    {
        gotoAdmin();
        clickLink("general_configuration");
        tester.clickLink("edit-app-properties");
        if (enable)
        {
            checkCheckbox("externalUM", "true");
            // JRA-15966. Mode must be private for External User Management
            tester.selectOption("mode", "Private");
        }
        else
        {
            checkCheckbox("externalUM", "false");
        }

        submit("Update");
    }

    /*
    * Tests that the panel shows the correct steps
    */
    protected void assertSubTaskConversionPanelSteps(String key, int currentStep)
    {
        assertTextSequence(new String[] {
                "Convert Issue to Sub-task: " + key,
                "Select Parent and Sub-task Type",
                "Select New Status",
                "Update Fields",
                "Confirmation"
        });

        String[] icons = new String[4];
        for (int i = 1; i <= 4; i++)
        {
            if (i < currentStep)
            {
                icons[i - 1] = "<li class=\"done\">";
            }
            else if (i > currentStep)
            {
                icons[i - 1] = "<li class=\"todo\">";
            }
            else
            {
                icons[i - 1] = "<li class=\"current\">";
            }
        }

        assertTextSequence(icons);

    }


    public void gotoConvertIssue(String issueId)
    {
        gotoPage("/secure/ConvertIssue.jspa?id=" + issueId);
    }

    public void gotoConvertIssueStep2(String issueId, String parent, String issueType)
    {
        gotoPage("/secure/ConvertIssueSetIssueType.jspa?id=" + issueId +
                "&parentIssueKey=" + parent + "&issuetype=" + issueType);
    }

    public void gotoConvertSubTask(String issueId)
    {
        gotoPage("/secure/ConvertSubTask.jspa?id=" + issueId);
    }

    public void gotoConvertSubTaskStep2(String issueId, String issueType)
    {
        gotoPage("/secure/ConvertSubTaskSetIssueType.jspa?id=" + issueId + "&issuetype=" + issueType);
    }

    protected void gotoConvertIssueStep3(String issueId, String parentKey, String issueType, String status)
    {
        gotoPage("/secure/ConvertIssueSetStatus.jspa?id=" + issueId +
                "&parentIssueKey=" + parentKey + "&issuetype=" + issueType + "&targetStatusId=" + status);
    }

    /**
     * Asserts that the link with the given id attribute exists and its href has the urlSubString
     *
     * @param linkId the id attribute.
     * @param urlSubString the expected sub-string of the href of the link.
     */
    public void assertLinkPresentWithSubString(String linkId, String urlSubString)
    {
        try
        {
            WebLink link = getDialog().getResponse().getLinkWithID(linkId);
            assertNotNull(link);
            if (StringUtils.isEmpty(link.getURLString()))
            {
                fail("No URL for link with id [" + linkId + "]");
            }
            boolean foundSubString = link.getURLString().contains(urlSubString);
            assertTrue(link.getURLString() + " does not have substring " + urlSubString, foundSubString);
        }
        catch (SAXException e)
        {
            fail("Error locating weblink with id [" + linkId + "]");
        }
    }

    /**
     * Asserts that the link with the given id attribute exists and its href does NOT have the urlSubString
     *
     * @param linkId the id attribute.
     * @param urlSubString the sub-string that should not appear in the href of the link.
     */
    public void assertLinkPresentWithoutSubString(String linkId, String urlSubString)
    {
        try
        {
            WebLink link = getDialog().getResponse().getLinkWithID(linkId);
            assertNotNull(link);
            if (StringUtils.isEmpty(link.getURLString()))
            {
                fail("No URL for link with id [" + linkId + "]");
            }
            boolean substringNotFound = !link.getURLString().contains(urlSubString);
            assertTrue(link.getURLString() + " does not have substring " + urlSubString, substringNotFound);
        }
        catch (SAXException e)
        {
            fail("Error locating weblink with id [" + linkId + "]");
        }
    }

    /**
     * Asserts that the link with the given id attribute exists and its href ends with the given URL.
     *
     * @param linkId the id attribute.
     * @param urlSuffix the expected suffix of the href of the link.
     */
    public void assertLinkPresentWithURL(String linkId, String urlSuffix)
    {
        try
        {
            WebLink link = getDialog().getResponse().getLinkWithID(linkId);
            assertNotNull(link);
            if (StringUtils.isEmpty(link.getURLString()))
            {
                fail("No URL for link with id [" + linkId + "]");
            }
            boolean foundSuffix = link.getURLString().endsWith(urlSuffix);
            assertTrue(link.getURLString() + " does not have suffix " + urlSuffix, foundSuffix);
        }
        catch (SAXException e)
        {
            fail("Error locating weblink with id [" + linkId + "]");
        }
    }

    /**
     * Asserts that there exists a link on the current page with the given text the url of which has the given suffix.
     *
     * @param linkText the link text.
     * @param urlSuffix the expected url suffix.
     * @deprecated please use {@link com.atlassian.jira.functest.framework.assertions.LinkAssertions#assertLinkLocationEndsWith(String,
     *             String)}
     */
    public void assertLinkWithTextUrlEndsWith(String linkText, String urlSuffix)
    {
        try
        {
            WebLink[] links = getDialog().getResponse().getLinks();
            assertFalse("Expected links!", links.length == 0);
            boolean foundLink = false;
            StringBuilder foundLinksSummary = new StringBuilder();
            int candidateLinks = 0;
            for (WebLink link : links)
            {
                if (link.getURLString() != null && link.asText() != null && link.asText().equals(linkText))
                {
                    candidateLinks++;
                    final String urlString = link.getURLString();
                    foundLinksSummary.append("\n").append(urlString);
                    foundLink = urlString.endsWith(urlSuffix);
                    if (foundLink)
                    {
                        break;
                    }
                }
            }
            if (!foundLink)
            {
                StringBuilder mesg = new StringBuilder();
                mesg.append("Could not find link with text '");
                mesg.append(linkText);
                mesg.append("' that has a url that ends with '");
                mesg.append(urlSuffix);
                mesg.append("'. ");
                if (candidateLinks > 0)
                {
                    mesg.append(candidateLinks);
                    mesg.append(" rejected candidates: [ ");
                    mesg.append(foundLinksSummary);
                    mesg.append("]");
                }
                fail(mesg.toString());
            }
        }
        catch (SAXException e)
        {
            fail("Error locating link with text " + linkText);
        }
    }


    /**
     * Asserts that there exists a link on the current page with the given text the url of which has the given suffix.
     *
     * @param linkText the link text.
     * @param strings the expected portions of the URL in the link.
     */
    public void assertLinkWithTextUrlContains(String linkText, String[] strings)
    {
        try
        {
            WebLink[] links = getDialog().getResponse().getLinks();
            assertFalse("Expected links!", links.length == 0);
            boolean foundLink = false;
            StringBuilder foundLinksSummary = new StringBuilder();
            int candidateLinks = 0;
            for (WebLink link : links)
            {
                if (link.getURLString() != null && link.asText() != null && link.asText().equals(linkText))
                {
                    candidateLinks++;
                    final String urlString = link.getURLString();
                    foundLinksSummary.append("\n").append(urlString);

                    for (String string : strings)
                    {
                        if (!urlString.contains(string))
                        {
                            foundLink = false;
                            break;
                        }
                        else
                        {
                            foundLink = true;
                        }
                    }

                    if (foundLink)
                    {
                        break;
                    }
                }
            }
            if (!foundLink)
            {
                StringBuilder mesg = new StringBuilder();
                mesg.append("Could not find link with text '");
                mesg.append(linkText);
                mesg.append("' that contains all ");

                for (String string : strings)
                {
                    mesg.append("'").append(string).append("' ");
                }

                mesg.append(". ");
                if (candidateLinks > 0)
                {
                    mesg.append(candidateLinks);
                    mesg.append(" rejected candidates: [ ");
                    mesg.append(foundLinksSummary);
                    mesg.append("]");
                }
                fail(mesg.toString());
            }
        }
        catch (SAXException e)
        {
            fail("Error locating link with text " + linkText);
        }
    }

    /**
     * @deprecated please use {@link com.atlassian.jira.functest.framework.assertions.TableAssertions#assertTableCellHasText(com.meterware.httpunit.WebTable,
     *             int, int, String)}
     */
    public void assertTableCellHasText(String tableId, int row, int column, String text)
    {
        try
        {
            WebTable table = getDialog().getResponse().getTableWithID(tableId);
            assertNotNull(table);
            boolean hasText = tableCellHasText(table, row, column, text);
            String actualText = table.getCellAsText(row, column);
            assertTrue("expected to find [" + text + "], somewhere in [" + actualText + "] but obviously couldn't.", hasText);
        }
        catch (SAXException e)
        {
            fail("Error locating table with id [" + tableId + "]");
        }
    }

    /**
     * Asserts that the Cache-control header in the response *is not* set to any one of "no-cache", "no-store" or
     * "must-revalidate". The choice of these 3 headers is directly related to the implementation of
     * com.atlassian.core.filters.AbstractEncodingFilter.setNonCachingHeaders(HttpServletResponse)
     */
    protected void assertResponseCanBeCached()
    {
        final String cacheControl = getDialog().getResponse().getHeaderField("Cache-control");
        final String[] values = new String[] { "no-cache", "no-store" };
        if (cacheControl != null && StringUtils.isNotEmpty(cacheControl))
        {
            // the presence of any of these headers means the response is not cacheable - ensure none of them are present
            for (String value : values)
            {
                assertFalse("Response cannot be cached: found '" + value + "' in Cache-control header", cacheControl.contains(value));
            }
        }
    }

    /**
     * Asserts that the Cache-control header in the response *is* set to any one of "no-cache", "no-store" or
     * "must-revalidate". The choice of these 3 headers is directly related to the implementation of
     * com.atlassian.core.filters.AbstractEncodingFilter.setNonCachingHeaders(HttpServletResponse)
     */
    protected void assertResponseCannotBeCached()
    {
        final String cacheControl = getDialog().getResponse().getHeaderField("Cache-control");
        final String[] values = new String[] { "no-cache", "no-store", "must-revalidate" };
        if (cacheControl != null && StringUtils.isNotEmpty(cacheControl))
        {
            // test for presence of any of the 3 headers - we only require 1 to be set for the response to be not cacheable
            boolean found = false;
            for (String value : values)
            {
                found = found || (cacheControl.contains(value));
            }

            if (!found)
            {
                fail("Cache-control header was set, but was not set to 'no-cache', 'no-store' or 'must-revalidate'");
            }
        }
        else
        {
            fail("No Cache-control header was set in the response");
        }
    }

    protected void gotoDefaultPermissionScheme()
    {
        navigation.gotoAdmin();
        clickLink("permission_schemes");
        clickLinkWithText("Default Permission Scheme");
    }

    /**
     * Grants a {@link com.atlassian.jira.webtests.Permissions} constant to the specified user to the default permission
     * scheme. Only works in enterprise.
     *
     * @param username the user who is
     * @param permission a {@link com.atlassian.jira.webtests.Permissions} constant
     */
    protected void grantPermissionToUserInEnterprise(int permission, String username)
    {
        String permissionKey = getKey(permission);
        gotoDefaultPermissionScheme();
        clickLink("add_perm_" + permissionKey);
        checkCheckbox("type", "user");
        setFormElement("user", username);
        submit(" Add ");
    }

    public void addProjectAdminPermission(int permission, String group)
    {
        String permissionKey = getKey(permission);
        navigation.gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLink("add_perm_" + permissionKey);
        checkCheckbox("type", "group");
        selectOption("group", group);
        submit(" Add ");
    }

    protected void gotoFieldConfigurationDefault()
    {
        gotoAdmin();
        clickLink("field_configuration");
        clickLink("configure-Default Field Configuration");
        assertTextPresent("View Field Configuration");
    }

    protected void assertTableCellContainsFixVersionsLinks(TableCell fixVersionsCell, int expectedLinkCount)
    {
        final WebLink[] links = fixVersionsCell.getLinks();
        assertNotNull(links);
        assertEquals(expectedLinkCount, links.length);
    }

    protected void assertTableCellContainsNoFixVersionsLinks(TableCell fixVersionsCell)
    {
        final WebLink[] links = fixVersionsCell.getLinks();
        assertTrue(links == null || links.length == 0);
    }

    protected void assertTableCellContainsPriorityIcon(TableCell tableCell)
    {
        // assert priority cell contains an icon - always
        final WebImage[] images = tableCell.getImages();
        assertNotNull(images);
        assertEquals(1, images.length);

        WebImage icon = images[0];
        assertTrue(icon.getSource().contains("/images/icons"));
    }

    protected void assertTableCellContainsNoPriorityIcon(TableCell tableCell)
    {
        // assert priority cell contains an icon - always
        final WebImage[] images = tableCell.getImages();
        assertTrue(images == null || images.length == 0);
    }


    /**
     * Simply dumps the web response, not necessarily because and error occurred.
     */
    @SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
    public void dumpResponse()
    {
        dumpResponse(new RuntimeException("HTML Dump Invoked : invoked around"));
    }

    /**
     * Dumps the web response because a Throwable condition exists.
     *
     * @param t the Throwable in question
     */
    public void dumpResponse(Throwable t)
    {
        TestCaseDumpKit.dumpTestInformation(this, new Date(), t);
    }

    /**
     * Logs work on the issue with the given key.
     *
     * @param issueKey the key of the issue to log work on.
     * @param timeLogged formatted time spent e.g. 1h 30m.
     * @param newEstimate formatted new estimate e.g. 1d 2h.
     */
    protected void logWork(String issueKey, String timeLogged, String newEstimate)
    {
        gotoIssue(issueKey);
        logWork(timeLogged, newEstimate);
    }

    /**
     * Logs work on the current issue.
     *
     * @param timeLogged formatted time spent e.g. 1h 30m.
     * @param newEstimate formatted new estimate e.g. 1d 2h.
     */
    private void logWork(String timeLogged, String newEstimate)
    {
        clickLink("log-work");
        setFormElement("timeLogged", timeLogged);
        checkCheckbox("adjustEstimate", "new");
        setFormElement("newEstimate", newEstimate);
        clickButton("log-work-submit");
    }

    /**
     * Sets the estimate on the current issue.
     *
     * @param time formatted time estimate e.g. 3h 30m.
     */
    protected void setEstimate(String time)
    {
        clickLink("edit-issue");
        setFormElement("timetracking", time);
        submit("Update");
    }

    /**
     * Waits until a workflow scheme migration completes now that it runs asynchronously.
     *
     * @param projectName the name of the project to associate the worflow scheme to
     * @param targetWorkflowName the name of the workflow scheme to associate the project to
     */
    protected void waitForSuccessfulWorkflowSchemeMigration(String projectName, String targetWorkflowName)
    {
        final int MAX_ITERATIONS = 100;
        int its = 0;
        while (true)
        {
            its++;
            if (its > MAX_ITERATIONS)
            {
                fail("The Workflow Migration took longer than " + MAX_ITERATIONS + " attempts!  Why?");
            }
            // are we on the "still working" page or the "done" page
            // if its neither then fail
            if (getResponseText().contains("type=\"submit\" name=\"Refresh\""))
            {
                // we are on the "still working page"
                // click on the Refresh Button
                submit("Refresh");
            }
            // its on the done page
            else if (getResponseText().contains("type=\"submit\" name=\"Done\""))
            {
                // we are on the "done" page" but we dont own the task
                validateProgressBarUI("Done");
                submit("Done");
                // now check we are on the project page
                assertTextInElement("project-config-header-name", projectName);
                final WorkflowSchemeData scheme
                        = getBackdoor().workflowSchemes().getWorkflowSchemeByProjectName(projectName);
                assertEquals(scheme.getName(), targetWorkflowName);
                return;
            }
            else if (getResponseText().contains("input type=\"submit\" name=\"Acknowledge\""))
            {
                validateProgressBarUI("Acknowledge");
                // we are on the "Acknowledge" page"
                submit("Acknowledge");

                // now check we are on the project page
                assertTextInElement("project-config-header-name", projectName);
                final WorkflowSchemeData scheme
                        = getBackdoor().workflowSchemes().getWorkflowSchemeByProjectName(projectName);
                assertEquals(scheme.getName(), targetWorkflowName);

                return;
            }
            else if (getDialog().getElement("project-config-header-name") != null)
            {
                // its on the project page for this project (straight thru association)
                return;
            }
            else
            {
                // we are on a page we dont expect
                fail("Page encountered during migration that was not expected : PROJECT:" + projectName + " - WORKFLOW SCHEME NAME" + targetWorkflowName);
            }

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                fail("Test interupted");
            }
        }
    }

    /**
     * This method assums that you have just submitted a long running task and that you need the taskId back.  It does
     * not acknowledge the task in in way
     *
     * @return the id of the submitted task.
     */
    protected long getSubmittedTaskId()
    {
        waitForStableTaskPage();
        return getTaskIdFromProgressBarUI();
    }

    protected void switchToPersonalLicense()
    {
        String licenseTitle = getEdition();
        String licenseKey = LicenseKeys.V2_PERSONAL.getLicenseString();
        switchLicense(licenseKey, "JIRA " + licenseTitle + ": Personal");
    }

    protected void switchLicense(String licenseKey, String licenseDescription)
    {
        gotoAdmin();
        clickLink("license_details");
        setFormElement("license", licenseKey);
        submit("Add");
        assertTextPresent("License Information");
        assertTextPresent(licenseDescription);
    }

    /**
     * This method assumes that you have just submitted a long running task, and you know the task id of it.
     *
     * @param taskId the task to wait for until it  is in acknowledge state
     */
    protected void waitForTaskAcknowledgement(long taskId)
    {
        waitForTaskState(taskId, "Acknowledge");
    }

    /**
     * This method assums that you have just submitted a long running task, and you know the task id of it.
     *
     * @param taskId the task to wait for until it
     */
    private void waitForTaskState(long taskId, String desiredTaskState)
    {
        String taskState;
        do
        {
            taskState = waitForStableTaskPage();
            long taskIdTest = getSubmittedTaskId();
            if (taskId != taskIdTest)
            {
                fail("Expecting taskid <" + taskId + "> but got <" + taskIdTest + ">");
            }
            if (taskState.equals(desiredTaskState))
            {
                return;
            }
            if (taskState.equals("Refresh"))
            {
                // we need to hit refresh other wise we never change state as auto refresh dones not
                // work inside this web unit framework
                submit("Refresh");
            }
        }
        while (!desiredTaskState.equalsIgnoreCase(taskState));
    }

    private String waitForStableTaskPage()
    {
        final int MAX_ITERATIONS = 100;
        int its = 0;

        while (true)
        {
            its++;
            if (its > MAX_ITERATIONS)
            {
                fail("The task took longer than " + MAX_ITERATIONS + " attempts!  Why?");
            }
            // are we on the "still working" page or the "done" page
            // if its neither then fail
            if (getResponseText().contains("type=\"submit\" name=\"Refresh\""))
            {
                return "Refresh";

            }
            // its on the done page
            else if (getResponseText().contains("type=\"submit\" name=\"Done\""))
            {
                // we are on the "done" page" but we dont own the task
                validateProgressBarUI("Done");
                return "Done";
            }
            else if (getResponseText().contains("input type=\"submit\" name=\"Acknowledge\""))
            {
                validateProgressBarUI("Acknowledge");
                return "Acknowledge";
            }

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                fail("Test interupted");
            }
        }
    }

    private long getTaskIdFromProgressBarUI()
    {
        String taskLocatorStr = "<div class=\"pb_border\" id=\"pb_taskid_";
        // find out the task id, its in the progress bar UI
        int startIndex = getResponseText().indexOf(taskLocatorStr);
        if (startIndex == -1)
        {
            fail("Failed to find task progress bar as expected");
        }
        startIndex += taskLocatorStr.length();
        int endIndex = getResponseText().indexOf("\">", startIndex);

        String taskId = getResponseText().substring(startIndex, endIndex);
        return Long.parseLong(taskId);
    }

    /**
     * The button name controls what to check for in terms of the progress bar UI.
     * <p/>
     * - Acknowledge - means you started the task and its finished - Done means that some one else started the task and
     * its finished - Refresh - means its submitted, maybe running and not finished
     *
     * @param desiredTaskState one of the above
     */
    protected void validateProgressBarUI(String desiredTaskState)
    {
        Integer leftPrecentage = null, rightPercentage = null;
        String tmp;

        try
        {
            tmp = getRegexMatch("pb_barlefttd.+style\\s*=\\s*\"[^\"]*width\\s*\\:\\s+(\\d+)%.*\"");
            if (tmp != null)
            {
                leftPrecentage = new Integer(tmp);
            }
        }
        catch (MalformedPatternException e)
        {
            fail("Left table regular expression is invalid.");
        }

        try
        {
            tmp = getRegexMatch("pb_barrighttd.+style\\s*=\\s*\"[^\"]*width\\s*\\:\\s+(\\d+)%.*\"");
            if (tmp != null)
            {
                rightPercentage = new Integer(tmp);
            }
        }
        catch (MalformedPatternException e)
        {
            fail("Right table regular expression is invalid.");
        }


        if ("Acknowledge".equalsIgnoreCase(desiredTaskState) || "Done".equalsIgnoreCase(desiredTaskState))
        {
            // ok we are finished and need acknowledge ment.  So it should have a certain look in the progress bar
            assertTextPresent("Task completed");
            assertTextPresent("Started");
            assertTextPresent("Finished");

            assertNull(rightPercentage);
            // we must be 100% complete to be here!
            assertEquals(100, leftPrecentage.intValue());

            // we can get a bit more specific here.  The user who started the task should be on the page
            if ("Done".equalsIgnoreCase(desiredTaskState))
            {

                // test that the link to another user is there
                assertTextPresent("<span>Started");

                assertRegexMatch("by <a href=\"/.*/secure/admin/user/ViewUser.jspa?name=", false);

                // if you break this test is because your page is not following the Task UI convention of
                // putting a warning at the top of the page.  So I am being strict here as a starting point
                // but we might want to relax this later!
                getAssertions().assertNodeHasText(new CssLocator(tester, ".aui-message.info"), "This task has finished running.");
                getAssertions().assertNodeHasText(new CssLocator(tester, ".aui-message.info"), "who started this task should acknowledge it.");
            }
        }
        else
        {
            assertTextNotPresent("Task completed");
            assertTextNotPresent("Finished");

            //lets make sure the bar is working correctly.
            if (leftPrecentage == null)
            {
                //in this case no progress has been made.
                assertEquals(100, rightPercentage.intValue());
            }
            else if (rightPercentage == null)
            {
                //in the case we have 100% completion but the task is not finished.
                assertEquals(100, leftPrecentage.intValue());
            }
            else
            {
                assertFalse("Task should not be complete", 100 == leftPrecentage.intValue());
                assertFalse("Task should not be complete", 100 == rightPercentage.intValue());
                //in this case we have a regular completion.
                assertEquals(leftPrecentage.intValue(), 100 - rightPercentage.intValue());
            }
        }
    }


    /**
     * Waits for the worflow activation "asynch" screens to finish and then puts it on the ListWorkflow page
     *
     * @param targetWorkflowName the name of the workflow.
     */
    protected void waitForSuccessfulWorkflowActivation(String targetWorkflowName)
    {
        final int MAX_ITERATIONS = 100;
        int its = 0;
        while (true)
        {
            its++;
            if (its > MAX_ITERATIONS)
            {
                fail("The Workflow Migration took longer than " + MAX_ITERATIONS + " attempts!  Why?");
            }
            // are we on the "still working" page or the "done" page
            // if its neither then fail
            if (getResponseText().contains("type=\"submit\" name=\"Refresh\""))
            {
                // we are on the "still working page"
                // click on the Refresh Button
                submit("Refresh");
            }
            else if (getResponseText().contains("type=\"submit\" name=\"Done\""))
            {
                // we are on the "done" page"
                validateProgressBarUI("Done");
                submit("Done");
                return;
            }
            else if (getResponseText().contains("input type=\"submit\" name=\"Acknowledge\""))
            {
                // we are on the "Acknowledge" page"
                validateProgressBarUI("Acknowledge");
                submit("Acknowledge");
                return;
            }
            else
            {
                // we are on a page we dont expect
                fail("Page encountered during migration that was not expected - " + targetWorkflowName);
            }

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                fail("Test interupted");
            }
        }
    }

    protected void associateIssueLevelSecuritySchemeToProject(String projectName, String schemeName)
    {
        final Project project = getProjectByName(projectName);

        gotoPage("/secure/project/SelectProjectScheme!default.jspa?projectId=" + project.id);

        selectOption("newSchemeId", schemeName);
        submit("Next >>");
        submit("Associate");
    }


    protected void gotoPortletConfig()
    {
        clickLinkWithText("Manage Dashboard");
        clickLink("config_0");
    }


    /**
     * Checks for a presence of the part of the link's URL
     *
     * @param linkPart part of the link's URL that needs to be present
     */
    protected void assertHelpLinkWithStringInUrlPresent(String linkPart)
    {
        assertTrue(hasHelpLink(linkPart));
    }

    private boolean hasHelpLink(String linkPart)
    {
        try
        {
            WebLink[] links = getDialog().getResponse().getLinks();
            if (links != null)
            {
                for (WebLink link : links)
                {
                    String urlString = link.getURLString();
                    if (urlString != null && urlString.contains(linkPart))
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        catch (SAXException e)
        {
            return false;
        }
    }

    protected void assertTextNotInColumn(String tableId, int column, String text) throws SAXException
    {
        WebTable table = getDialog().getResponse().getTableWithID(tableId);
        final int count = table.getRowCount();
        for (int i = 0; i < count; i++)
        {
            //check there's no row with the Draft status yet.
            String operationsCell = table.getCellAsText(i, column);
            log("Checking cell on row[" + i + "] col[" + column + "] doesn't have text [" + text + "]");
            assertTrue(!operationsCell.contains(text));
        }
    }

    protected void assertTableCellHasNotText(String tableId, int row, int col, String text)
            throws SAXException
    {
        WebTable table = getDialog().getResponse().getTableWithID(tableId);
        assertTrue(!table.getCellAsText(row, col).contains(text));
    }

    protected String getEdition()
    {
        return "Enterprise";
    }

    protected void assertRedirectAndFollow(final String url, final String redirectRegex)
    {
        try
        {
            getTester().getTestContext().getWebClient().getClientProperties().setAutoRedirect(false);
            gotoPage(url);
            final String redirectTo = getDialog().getResponse().getHeaderField("Location");
            // check redirect url ends wtih the right thing
            final boolean redirectingToCreateIssueStep2 = redirectTo.matches(redirectRegex);
            assertTrue("expected redirect to create issue, location header is: " + redirectTo, redirectingToCreateIssueStep2);
            gotoPage(redirectTo);
        }
        finally
        {
            getTester().getTestContext().getWebClient().getClientProperties().setAutoRedirect(true);
        }
    }

    public void enableRemoteApi()
    {
        gotoAdmin();
        clickLink("general_configuration");
        tester.clickLink("edit-app-properties");
        checkCheckbox("allowRpc", "true");
        submit("Update");
    }

    public void addWorkflowPostfunction(String workflowName, String stepName, String transitionName, String postFunctionName)
    {
        administration.workflows().goTo().workflowSteps(workflowName);
        clickLinkWithText(transitionName);
        clickLinkWithText("Post Functions");
        clickLinkWithText("Add post function", 0);
        selectMultiOptionByValue("type", postFunctionName);

        submit();
    }

    protected void turnOffDangerMode()
    {
        getBackdoor().systemProperties().setProperty("jira.dangermode", "false");
    }

    protected void turnOnDangerMode()
    {
        getBackdoor().systemProperties().setProperty("jira.dangermode", "true");
    }

    public static interface ParameterEnterer
    {
        void enterParameters();
    }

    protected void addWorkflowCondition(String workflowName, String stepName, String condition)
    {
        addWorkflowCondition(workflowName, stepName, condition, null);
    }

    protected void addWorkflowCondition(String workflowName, String stepName, String condition, ParameterEnterer paramEnterer)
    {
        gotoWorkFlow();
        clickLink("steps_live_" + workflowName);
        clickLinkWithText(stepName);
        clickLink("add-workflow-condition");
        checkCheckbox("type", condition);
        submit("Add");

        if (paramEnterer != null)
        {
            paramEnterer.enterParameters();
        }
    }

    protected void addWorkflowValidator(String workflowName, String stepName, String validator)
    {
        addWorkflowValidator(workflowName, stepName, validator, null);
    }

    protected void addWorkflowValidator(String workflowName, String stepName, String validator, ParameterEnterer paramEnterer)
    {
        gotoWorkFlow();
        clickLink("steps_live_" + workflowName);
        clickLinkWithText(stepName);
        clickLinkWithText("Validators");
        clickLink("add_new_validator");
        checkCheckbox("type", validator);
        submit("Add");

        if (paramEnterer != null)
        {
            paramEnterer.enterParameters();
        }
    }

    @Deprecated
    /**
     * Gets the build number from the footer in JIRA
     * @deprecated use {@link com.atlassian.jira.functest.framework.Administration#getBuildNumber()} ()} instead.
     */
    protected String getBuild()
    {
        gotoAdmin();
        final CssLocator poweredByLocator = new CssLocator(tester, "#footer-build-information");
        final String poweredByText = poweredByLocator.getRawText();
        // look for (v4.0-SNAPSHOT#451) in text
        final java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\((.*)\\)");
        final Matcher m = p.matcher(poweredByText);
        if (!m.find())
        {
            fail("Could not find version footer in expected location");
        }
        // given v4.0-SNAPSHOT#451 split on "." and get the last part
        final String versionFooter = m.group(1);
        final String[] footerParts = versionFooter.split("#");
        return footerParts[footerParts.length - 1];
    }

    /**
     * Check whether we are using HSQLDB as the DB -- assumes we are already logged in as the administrator
     */
    public boolean usingHsqlDb() throws IOException
    {
        gotoAdmin();
        clickLink("system_info");
        try
        {
            WebTable systemInfoTable = getDialog().getResponse().getTableWithID("system_info_table");
            return tableIndexOf(systemInfoTable, EasyList.build("Database type", "hsql")) != -1;
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * A shortcut method to allow quick creation of {@link com.atlassian.jira.functest.framework.locator.XPathLocator}s
     *
     * @param xpathExpression the xpath expression
     * @return an XPathLocator
     */
    protected XPathLocator xpath(final String xpathExpression)
    {
        return locator.xpath(xpathExpression);
    }


    public void setUp()
    {
        init();
    }

    public void init()
    {
        DefaultFuncTestHttpUnitOptions.setDefaultOptions();
        // allow people to override these options
        setUpHttpUnitOptions();

        startTime = System.currentTimeMillis();

        webClientListener = new FuncTestWebClientListener();
        JIRAEnvironmentData environmentData = getEnvironmentData();
        WebTesterFactory.setupWebTester(tester, environmentData);
        new JiraSetupInstanceHelper(tester, environmentData).ensureJIRAIsReadyToGo(webClientListener);
        // case the variables to be initialised
        getAdministration();
        getAssertions();
        getNavigation();
        getPage();
    }

    /**
     * Override this to set up any {@link com.meterware.httpunit.HttpUnitOptions} that must be set before the {@link
     * net.sourceforge.jwebunit.WebTester} is used
     */
    protected void setUpHttpUnitOptions()
    {
    }

    public boolean isJiraSetup()
    {
        beginAt("/");

        boolean hasBeenSetUp = (getEnvironmentData().getContext() + "/secure/Dashboard.jspa").equals(getDialog().getResponse().getURL().getPath());
        return (hasBeenSetUp);
    }

    public void tearDown()
    {
        logout();
        super.tearDown();
    }


    @Override
    protected void runTest() throws Throwable
    {
        //
        // make the protected fields available to tests so that they can act like FuncTestCase
        // we have to do this here because the life cycle of JIRAWebTest is not in a good state until
        // setup() has been run!
        //
        initFuncTestCaseLikeProtectedVariables();

        try
        {
            super.runTest();
        }
        catch (Throwable throwable)
        {
            try
            {
                TestCaseDumpKit.dumpTestInformation(this, new Date(), throwable);
            }
            catch (RuntimeException ignored)
            {
            }
            throw throwable;
        }
    }

    /**
     * The outer most edge of a JUnit Test.  All things start and end here.
     *
     * @see junit.framework.TestCase#runBare()
     */
    public void runBare() throws Throwable
    {
        startTime = System.currentTimeMillis();

        HttpUnitOptions.setScriptingEnabled(false);

        //We need to get this through the method because the environment may not have been set (e.g. TPM tests).
        JIRAEnvironmentData data = getEnvironmentData();
        LogOnBothSides.log(backdoor.getTestkit(), TestInformationKit.getStartMsg(this, data.getTenant()));
        try
        {
            super.runBare();
            LogOnBothSides.log(backdoor.getTestkit(), TestInformationKit.getEndMsg(this, data.getTenant(), System.currentTimeMillis() - startTime, webClientListener));
        }
        catch (Throwable throwable)
        {
            LogOnBothSides.log(backdoor.getTestkit(), TestInformationKit.getEndMsg(this, data.getTenant(), System.currentTimeMillis() - startTime, webClientListener, throwable));
            throw throwable;
        }
        finally
        {
            clearTestCaseVariables();
        }
    }

    /**
     * Must be called AFTER Test setup
     */
    private void initFuncTestCaseLikeProtectedVariables()
    {
        // make sure these are initialised
        getNavigation();
        getAdministration();
        getAssertions();
        getEnvironmentData();

        form = new FormImpl(tester);
        page = new HtmlPage(tester);
        text = new TextAssertionsImpl(tester);
        issueTableAssertions = new IssueTableAssertions(getBackdoor());
        parse = new ParserImpl(tester, getEnvironmentData());
        locator = getLocatorFactory();
        log = new FuncTestLoggerImpl(3);
    }

    private LocatorFactory getLocatorFactory()
    {
        if (locator == null)
        {
            return new LocatorFactoryImpl(tester);
        }
        else
        {
            return locator;
        }
    }


    /**
     * JUnit keeps each Testcase in memory for reporting reasons and hence if we dont clear the internal variables we
     * will use  alot of memory and eventually run out!
     */
    private void clearTestCaseVariables()
    {
        tester = null;
        environmentData = null;
        navigation = null;
        form = null;
        page = null;
        parse = null;
        administration = null;
        assertions = null;
        text = null;
        log = null;
        locator = null;
        webClientListener = null;
    }
}
