package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.ComponentClient;
import com.atlassian.jira.testkit.client.restclient.ComponentIssueCounts;
import com.atlassian.jira.testkit.client.restclient.Errors;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.jira.testkit.client.restclient.User;

import java.net.URI;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Func tests for component resource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestComponentResource extends RestFuncTest
{
    private ComponentClient componentClient;
    private SearchClient searchClient;

    public void testViewComponent() throws Exception
    {
        Component component = componentClient.get("10000");

        assertEquals(getBaseUrlPlus("rest/api/2/component/10000"), component.self);
        assertEquals("New Component 1", component.name);
        assertNull(component.lead);
    }

    public void testViewComponentNotFound() throws Exception
    {
        Response response123 = componentClient.getResponse("123");
        assertEquals(404, response123.statusCode);
        assertTrue(response123.entity.errorMessages.contains("The component with id 123 does not exist."));

        Response responseAbc = componentClient.getResponse("abc");
        assertEquals(404, responseAbc.statusCode);
        assertTrue(responseAbc.entity.errorMessages.contains("The component with id abc does not exist."));
    }

    public void testCreateComponent() throws Exception
    {
        //Create a simple component.
        Component inputComponent = new Component().project("MKY").name("Sausages").description("Lamb and Rosemary").leadUserName("fred").assigneeType(Component.AssigneeType.COMPONENT_LEAD);
        Component newComponent = componentClient.get(componentClient.create(inputComponent).id.toString());
        assertSameComponent(inputComponent, newComponent);
        //Create a simple component, no lead.
        inputComponent = new Component().project("MKY").name("Chops").description("Lamb short loin").assigneeType(Component.AssigneeType.PROJECT_DEFAULT);
        newComponent = componentClient.get(componentClient.create(inputComponent).id.toString());
        assertSameComponent(inputComponent, newComponent);
        //Create a simple component, no description.
        inputComponent = new Component().project("MKY").name("potatoes").leadUserName("fred").assigneeType(Component.AssigneeType.PROJECT_DEFAULT);
        newComponent = componentClient.get(componentClient.create(inputComponent).id.toString());
        assertSameComponent(inputComponent, newComponent);
        //Create a simple component, just a name.
        inputComponent = new Component().project("MKY").name("onions");
        newComponent = componentClient.get(componentClient.create(inputComponent).id.toString());
        Component expectedComponent = new Component().project("MKY").name("onions").assigneeType(Component.AssigneeType.PROJECT_DEFAULT);
        assertSameComponent(expectedComponent, newComponent);
    }

    public void testCreateComponentErrors() throws Exception
    {
        Response response = componentClient.createResponse(new Component());
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("A project id must be specified for this operation."), response.entity);

        // Bad Project
        Component inputComponent = new Component().project("BAD").name("Sausages").description("Lamb and Rosemary").leadUserName("fred").assigneeType(Component.AssigneeType.COMPONENT_LEAD);
        response = componentClient.createResponse(inputComponent);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("No project could be found with key 'BAD'."), response.entity);

        // Name not supplied
        inputComponent = new Component().project("MKY").id(10000l).description("Lamb and Rosemary").leadUserName("fred").assigneeType(Component.AssigneeType.COMPONENT_LEAD);
        response = componentClient.createResponse(inputComponent);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("name", "The component name specified is invalid - cannot be an empty string."), response.entity);

        // Duplicate name
        inputComponent = new Component().project("MKY").id(10000l).name("Sausages").description("Lamb and Rosemary").leadUserName("fred").assigneeType(Component.AssigneeType.COMPONENT_LEAD);
        Component newComponent = componentClient.create(inputComponent);
        inputComponent = new Component().project("MKY").id(10000l).name("Sausages").description("Lamb and Rosemary").leadUserName("fred").assigneeType(Component.AssigneeType.COMPONENT_LEAD);
        response = componentClient.createResponse(inputComponent);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("name", "A component with the name Sausages already exists in this project."), response.entity);

        // Invalid username supplied
        inputComponent = new Component().project("MKY").id(10000l).name("C1").description("Lamb and Rosemary").leadUserName("notfred").assigneeType(Component.AssigneeType.COMPONENT_LEAD);
        response = componentClient.createResponse(inputComponent);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("leadUserName", "The user notfred does not exist.").addError("componentLead", "The user notfred does not exist."), response.entity);

        //No permission.
        inputComponent = new Component().project("MKY").id(10001l).name("Chops").description("Lamb short loin").assigneeType(Component.AssigneeType.PROJECT_DEFAULT);
        response = componentClient.loginAs("fred").createResponse(inputComponent);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("You cannot edit the configuration of this project."), response.entity);

        //No permission to view project (anonymous)
        Response notFoundResp = componentClient.anonymous().createResponse(new Component().project("MKY").name("some name"));
        assertThat(notFoundResp.statusCode, equalTo(NOT_FOUND.getStatusCode()));

        //No permission to configure project (anonymous)
        Response unauthorisedResp = componentClient.anonymous().createResponse(new Component().project("HSP").name("component"));
        assertThat(unauthorisedResp.statusCode, equalTo(UNAUTHORIZED.getStatusCode()));
    }

    public void testEditComponent() throws Exception
    {
        //Update all fields component
        Component expectedComponent = getInitialComponent();
        Component inputComponent = new Component().name("Sausages").description("Lamb and Rosemary").leadUserName("fred").assigneeType(Component.AssigneeType.COMPONENT_LEAD).self(expectedComponent.self);
        expectedComponent.name("Sausages").description("Lamb and Rosemary").leadUserName("fred").assigneeType(Component.AssigneeType.COMPONENT_LEAD);
        verifyEdit(expectedComponent, inputComponent);

        //Now just one field at a time
        inputComponent = new Component().name("Hamburger").self(expectedComponent.self);
        expectedComponent.name("Hamburger");
        verifyEdit(expectedComponent, inputComponent);

        inputComponent = new Component().description("Beef mince to you and me").self(expectedComponent.self);
        expectedComponent.description("Beef mince to you and me");
        verifyEdit(expectedComponent, inputComponent);

        inputComponent = new Component().assigneeType(Component.AssigneeType.COMPONENT_LEAD).self(expectedComponent.self);
        expectedComponent.assigneeType(Component.AssigneeType.COMPONENT_LEAD);
        verifyEdit(expectedComponent, inputComponent);

        inputComponent = new Component().assigneeType(Component.AssigneeType.UNASSIGNED).self(expectedComponent.self);
        expectedComponent.assigneeType(Component.AssigneeType.UNASSIGNED);
        verifyEdit(expectedComponent, inputComponent);

        inputComponent = new Component().leadUserName("admin").self(expectedComponent.self);
        expectedComponent.leadUserName("admin");
        verifyEdit(expectedComponent, inputComponent);

        // verify that sending an empty string for the lead user name results in the lead being unset.
        inputComponent = new Component().leadUserName("").self(expectedComponent.self);
        expectedComponent.leadUserName(null);
        expectedComponent.lead(null);
        verifyEdit(expectedComponent, inputComponent);
    }

    public void testEditComponentErrors() throws Exception
    {
        // Duplicate name
        Component expectedComponent = getInitialComponent();
        Component editComponent = new Component().name("New Component 2").self(expectedComponent.self);
        Response response = componentClient.putResponse(editComponent);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("name", "A component with the name New Component 2 already exists in this project."), response.entity);

        // Name blank
        editComponent = new Component().name("").self(expectedComponent.self);
        response = componentClient.putResponse(editComponent);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("name", "The component name specified is invalid - cannot be an empty string."), response.entity);

        // Invalid username supplied
        editComponent = new Component().leadUserName("notfred").self(expectedComponent.self);
        response = componentClient.putResponse(editComponent);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("leadUserName", "The user notfred does not exist.").addError("componentLead", "The user notfred does not exist."), response.entity);

        //No permission to edit
        editComponent = new Component().name("Not authorised").self(expectedComponent.self);
        response = componentClient.loginAs("fred").putResponse(editComponent);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("The user fred does not have permission to complete this operation."), response.entity);
        
        //No permission to view project (anonymous)
        String compId = administration.project().addComponent("MKY", "Galho", "Galho do macaco", "admin");
        response = componentClient.anonymous().putResponse(compId, new Component().name("Other"));
        assertThat(response.statusCode, equalTo(NOT_FOUND.getStatusCode()));
        assertThat(response.entity.errorMessages, hasItem("The component with id 10001 does not exist."));

        response = componentClient.anonymous().putResponse("gugu", new Component().name("Other"));
        assertThat(response.statusCode, equalTo(NOT_FOUND.getStatusCode()));
        assertThat(response.entity.errorMessages, hasItem("The component with id gugu does not exist."));

        //Can view but no permission to edit project (anonymous)
        response = componentClient.anonymous().putResponse(new Component().name("New Component 1").self(expectedComponent.self));
        assertThat(response.statusCode, equalTo(UNAUTHORIZED.getStatusCode()));
    }

    private void verifyEdit(Component expectedComponent, Component editComponent)
    {
        componentClient.putResponse(editComponent);
        assertSameComponent(expectedComponent, componentClient.get(expectedComponent.id.toString()));
    }

    private Component getInitialComponent()
    {
        // First let's make sure we're in the state we expect
        final String componentID = "10000";
        Component actualComponent = componentClient.get(componentID);
        Component expectedComponent = new Component().self(createSelfLink(Long.valueOf(componentID)))
                .id(10000l)
                .name("New Component 1")
                .assigneeType(Component.AssigneeType.PROJECT_DEFAULT).assignee(createUserAdmin())
                .realAssigneeType(Component.AssigneeType.PROJECT_DEFAULT).realAssignee(createUserAdmin())
                .isAssigneeTypeValid(true);

        assertEquals(expectedComponent, actualComponent);
        return actualComponent;
    }

    private User createUserAdmin()
    {
        final Map<String, String> avatarUrls = MapBuilder.<String, String>newBuilder()
                .add("24x24", getBaseUrlPlus("secure/useravatar?size=small&avatarId=10062"))
                .add("16x16", getBaseUrlPlus("secure/useravatar?size=xsmall&avatarId=10062"))
                .add("32x32", getBaseUrlPlus("secure/useravatar?size=medium&avatarId=10062"))
                .add("48x48", getBaseUrlPlus("secure/useravatar?avatarId=10062"))
// TODO JRADEV-20790 - Re-enable the larger avatar sizes.
//                .add("64x64", getBaseUrlPlus("secure/useravatar?size=xlarge&avatarId=10062"))
//                .add("96x96", getBaseUrlPlus("secure/useravatar?size=xxlarge&avatarId=10062"))
//                .add("128x128", getBaseUrlPlus("secure/useravatar?size=xxxlarge&avatarId=10062"))
//                .add("192x192", getBaseUrlPlus("secure/useravatar?size=xxlarge%402x&avatarId=10062")) // %40 == "@"
//                .add("256x256", getBaseUrlPlus("secure/useravatar?size=xxxlarge%402x&avatarId=10062"))
                .toMap();

        return new User()
                .self(createUserUri("admin"))
                .name("admin")
                .key("admin")
                .displayName("Administrator")
                .active(true)
                .avatarUrls(avatarUrls);
    }

    private URI createUserUri(String name)
    {
        return getRestApiUri(String.format("user?username=%s", name));
    }

    private URI createSelfLink(long id)
    {
        return getRestApiUri("component", String.valueOf(id));
    }

    public void testGetComponentIssueCounts() throws Exception
    {
        ComponentIssueCounts counts = componentClient.getComponentIssueCounts("10000");
        assertEquals(0, counts.issueCount);

        // Add an issue to this component
        String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Issue for voting test");
        navigation.issue().setComponents(issueKey, "New Component 1");
        counts = componentClient.getComponentIssueCounts("10000");
        assertEquals(1, counts.issueCount);

        // Add an issue to another component
        issueKey = navigation.issue().createIssue("homosapien", "Bug", "Issue for voting test");
        navigation.issue().setComponents(issueKey, "New Component 2");
        counts = componentClient.getComponentIssueCounts("10000");
        assertEquals(1, counts.issueCount);
        counts = componentClient.getComponentIssueCounts("10001");
        assertEquals(1, counts.issueCount);

        // And some more issue to another component
        issueKey = navigation.issue().createIssue("homosapien", "Bug", "Issue for voting test");
        navigation.issue().setComponents(issueKey, "New Component 2");
        counts = componentClient.getComponentIssueCounts("10000");
        assertEquals(1, counts.issueCount);
        counts = componentClient.getComponentIssueCounts("10001");
        assertEquals(2, counts.issueCount);

        // Anonymous should work
        counts = componentClient.loginAs("fred").getComponentIssueCounts("10001");
        assertEquals(2, counts.issueCount);
    }

    public void testGetVersionIssueCountsNotFound() throws Exception
    {
        Response resp1 = componentClient.getComponentIssueCountsResponse("1");
        assertEquals(404, resp1.statusCode);
        assertTrue(resp1.entity.errorMessages.contains("The component with id 1 does not exist."));

        Response respZbing = componentClient.getComponentIssueCountsResponse("zbing");
        assertEquals(404, respZbing.statusCode);
        assertTrue(respZbing.entity.errorMessages.contains("The component with id zbing does not exist."));
    }

    public void testDeleteComponent() throws Exception
    {
        // Delete component no swap to components
        String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Issue for voting test");
        navigation.issue().setComponents(issueKey, "New Component 1");
        // Search for component is empty should return one issue
        SearchResult searchResults = searchClient.postSearch(new SearchRequest().jql("component is empty"));
        assertThat(searchResults.total, equalTo(0));
        assertThat(searchResults.issues.size(), equalTo(0));

        componentClient.delete("10000");
        Response resp1 = componentClient.getResponse("10000");
        assertEquals(404, resp1.statusCode);
        assertTrue(resp1.entity.errorMessages.contains("The component with id 10000 does not exist."));
        // Search for component is empty should return one issue
        searchResults = searchClient.postSearch(new SearchRequest().jql("component is empty"));
        assertThat(searchResults.total, equalTo(1));
        assertThat(searchResults.issues.size(), equalTo(1));

        // Delete and move issues
        navigation.issue().setComponents(issueKey, "New Component 2");
        issueKey = navigation.issue().createIssue("homosapien", "Bug", "Issue for voting test");
        navigation.issue().setComponents(issueKey, "New Component 2");

        componentClient.delete("10001", createSelfLink(10002));
        resp1 = componentClient.getResponse("10001");
        assertEquals(404, resp1.statusCode);
        assertTrue(resp1.entity.errorMessages.contains("The component with id 10001 does not exist."));

        // Search for component = "New Component 3" should return one issue
        searchResults = searchClient.postSearch(new SearchRequest().jql("component = \"New Component 3\""));
        assertThat(searchResults.total, equalTo(2));
        assertThat(searchResults.issues.size(), equalTo(2));

    }

    /**
     * This tests the sent and received components are virtually the same although they may hve slightly different shapes.
     * @param expectedComponent The expected component
     * @param actualComponent The actual component
     */
    private void assertSameComponent(Component expectedComponent, Component actualComponent)
    {
        assertNotNull(actualComponent.id);
        assertNotNull(actualComponent.self);

        assertEquals(expectedComponent.name, actualComponent.name);
        assertEquals(expectedComponent.description, actualComponent.description);
        assertEquals(expectedComponent.assigneeType, actualComponent.assigneeType);
        // The component lead may be bean or just a name
        String expectedlead = expectedComponent.lead != null ? expectedComponent.lead.name : expectedComponent.leadUserName;
        String actuallead = actualComponent.lead != null ? actualComponent.lead.name : actualComponent.leadUserName;
        assertEquals(expectedlead, actuallead);
    }


    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        componentClient = new ComponentClient(getEnvironmentData());
        searchClient = new SearchClient(getEnvironmentData());
        administration.restoreData("TestComponentResource.xml");
    }
}
