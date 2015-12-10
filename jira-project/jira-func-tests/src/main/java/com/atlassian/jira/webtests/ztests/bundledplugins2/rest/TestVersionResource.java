package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Errors;
import com.atlassian.jira.testkit.client.restclient.ProjectClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.testkit.client.restclient.VersionClient;
import com.atlassian.jira.testkit.client.restclient.VersionIssueCounts;
import com.atlassian.jira.testkit.client.restclient.VersionMove;
import com.atlassian.jira.testkit.client.restclient.VersionUnresolvedIssueCount;
import org.joda.time.LocalDate;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Func test for VersionResource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestVersionResource extends RestFuncTest
{
    private VersionClient versionClient;
    private ProjectClient projectClient;
    private SearchClient searchClient;



    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        versionClient = new VersionClient(getEnvironmentData());
        projectClient = new ProjectClient(getEnvironmentData());
        searchClient = new SearchClient(getEnvironmentData());

        administration.restoreData("TestVersionResource.xml");
    }

    @Override
    protected void tearDownTest()
    {
        versionClient = null;
        projectClient = null;
        searchClient = null;
        super.tearDownTest();
    }



    public void testEditDescription() throws Exception
    {
        final Version expectedVersion = getInitialVersion();

        final Version editVersion = new Version().description("my new description").self(expectedVersion.self);
        expectedVersion.description = editVersion.description;
        expectedVersion.projectId = 10000l;

        verifyEdit(expectedVersion, editVersion);
    }

    private void verifyEdit(Version expectedVersion, Version editVersion)
    {
        versionClient.putResponse(editVersion);
        assertThat(versionClient.get(getVersionId(expectedVersion)), equalTo(expectedVersion));
    }

    public void testEditName() throws Exception
    {
        final Version expectedVersion = getInitialVersion();

        final Version editVersion = new Version().name("Super Version").self(expectedVersion.self);
        expectedVersion.name = editVersion.name;

        verifyEdit(expectedVersion, editVersion);
    }

    public void testEditNameErrors() throws Exception
    {
        Version editVersion = new Version().name("").self(getInitialVersion().self);
        Response response = versionClient.putResponse(editVersion);
        assertThat(response.statusCode, equalTo(BAD_REQUEST.getStatusCode()));
        assertThat(response.entity, equalTo(new Errors().addError("name", "You must specify a valid version name")));

        editVersion = new Version().name("New Version 4").self(getInitialVersion().self);
        response = versionClient.putResponse(editVersion);
        assertThat(response.statusCode, equalTo(BAD_REQUEST.getStatusCode()));
        assertThat(response.entity, equalTo(new Errors().addError("name", "A version with this name already exists in this project.")));
    }

    private Version getInitialVersion()
    {
        // First let's make sure we're in the state we expect
        final String versionID = "10000";
        final Version actualVersion = versionClient.get(versionID);
        final Version expectedVersion = new Version().self(createSelfLink(Long.valueOf(versionID)))
                .archived(false).released(false).name("New Version 1")
                .description("Test Version Description 1")
                .id(10000L)
                .projectId(10000L);

        assertThat(actualVersion, equalTo(expectedVersion));
        return actualVersion;
    }

    private String getVersionId(final Version version)
    {
        final String[] selfParts = version.self.split("/");
        return selfParts[selfParts.length - 1];
    }

    public void testArchiveUnarchive() throws Exception
    {
        final Version expectedVersion = getInitialVersion();

        Version editVersion = new Version().archived(true).self(expectedVersion.self);
        expectedVersion.archived = editVersion.archived;

        verifyEdit(expectedVersion, editVersion);

        editVersion = new Version().archived(false).self(expectedVersion.self);
        expectedVersion.archived = editVersion.archived;

        verifyEdit(expectedVersion, editVersion);
    }

    public void testReleaseUnrelease() throws Exception
    {
        final Version expectedVersion = getInitialVersion();

        final LocalDate releaseDate = new LocalDate().plusDays(1);
        Version editVersion = new Version().released(true).releaseDate(releaseDate).self(expectedVersion.self);
        expectedVersion.released = editVersion.released;
        expectedVersion.releaseDate = editVersion.releaseDate;
        expectedVersion.userReleaseDate = releaseDate.toString("dd/MMM/yy");
        expectedVersion.projectId = 10000l;
        verifyEdit(expectedVersion, editVersion);

        editVersion = new Version().released(false).self(expectedVersion.self);
        expectedVersion.released = editVersion.released;
        expectedVersion.overdue = false;

        verifyEdit(expectedVersion, editVersion);
    }

    public void testEditStartDate() throws Exception
    {
        final Version expectedVersion = getInitialVersion();

        // Test setting the date via canonical form
        final LocalDate startDate = LocalDate.fromDateFields(new Date(0));
        Version editVersion = new Version().startDate(startDate).self(expectedVersion.self);

        expectedVersion.startDate = editVersion.startDate;
        expectedVersion.userStartDate = startDate.toString("dd/MMM/yy");
        verifyEdit(expectedVersion, editVersion);

        // Test setting the date via display form
        final LocalDate aDate = new LocalDate().plusDays(1);
        editVersion = new Version().userStartDate(aDate.toString("dd/MMM/yy")).self(expectedVersion.self);

        expectedVersion.startDate = aDate;
        expectedVersion.userStartDate = aDate.toString("dd/MMM/yy");
        verifyEdit(expectedVersion, editVersion);

        // Test setting the date to null via user format  -  Can't use this framework to test setting the canonical form to null
        editVersion = new Version().userStartDate("").self(expectedVersion.self);

        expectedVersion.startDate = null;
        expectedVersion.userStartDate = null;
        expectedVersion.overdue = null;
        verifyEdit(expectedVersion, editVersion);
    }

    public void testEditReleaseDate() throws Exception
    {
        final Version expectedVersion = getInitialVersion();

        // Test setting the date via canonical form
        final LocalDate releaseDate = LocalDate.fromDateFields(new Date(0));
        Version editVersion = new Version().releaseDate(releaseDate).self(expectedVersion.self);

        expectedVersion.releaseDate = editVersion.releaseDate;
        expectedVersion.userReleaseDate = releaseDate.toString("dd/MMM/yy");
        expectedVersion.overdue = true;
        verifyEdit(expectedVersion, editVersion);


        // Test setting the date via display form
        final LocalDate aDate = new LocalDate().plusDays(1);
        editVersion = new Version().userReleaseDate(aDate.toString("dd/MMM/yy")).self(expectedVersion.self);

        expectedVersion.releaseDate = aDate;
        expectedVersion.userReleaseDate = aDate.toString("dd/MMM/yy");
        expectedVersion.overdue = false;
        verifyEdit(expectedVersion, editVersion);

        // Test setting the date to null via user format  -  Can't use this framework to test setting the canonical form to null
        editVersion = new Version().userReleaseDate("").self(expectedVersion.self);

        expectedVersion.releaseDate = null;
        expectedVersion.userReleaseDate = null;
        expectedVersion.overdue = null;
        verifyEdit(expectedVersion, editVersion);

    }

    public void testVersionReleaseDateShouldBeInSystemTimeZone() throws Exception
    {
        final String versionId = "10011";
        final String releaseDate = "01/Mar/11";

        // Admin & Fred should always see the same date, independent of their time zone preference
        assertReleaseDateEqualTo(versionId, releaseDate);

        final String newReleaseDate = "03/Mar/11";
        final Version updatedVersion = versionClient.loginAs(FRED_USERNAME).get(versionId).releaseDate(newReleaseDate, null);

        // if Bob sets the date, then Bob, Admin, and Fred should see the same date
        final Response r = versionClient.loginAs(BOB_USERNAME).putResponse(updatedVersion);
        assertThat(String.valueOf(r.entity), r.statusCode, equalTo(200));
        assertReleaseDateEqualTo(versionId, newReleaseDate);
    }

    public void testViewVersion() throws Exception
    {
        Version actualVersion = versionClient.get("10000");
        Version expectedVersion = new Version().self(createSelfLink(10000))
                .archived(false).released(false).name("New Version 1")
                .description("Test Version Description 1")
                .id(10000L).projectId(10000l);

        assertThat(expectedVersion, equalTo(actualVersion));

        actualVersion = versionClient.get("10001");
        expectedVersion = new Version().self(createSelfLink(10001))
                .archived(false).released(false).name("New Version 4")
                .description("Test Version Description 4")
                .id(10001L).projectId(10000l);

        assertThat(expectedVersion, equalTo(actualVersion));

        actualVersion = versionClient.get("10002");
        expectedVersion = new Version().self(createSelfLink(10002))
                .archived(false).released(false).name("New Version 5")
                .description("Test Version Description 5")
                .id(10002L).projectId(10000l);

        assertThat(expectedVersion, equalTo(actualVersion));

        actualVersion = versionClient.get("10002");
        expectedVersion = new Version().self(createSelfLink(10002))
                .archived(false).released(false).name("New Version 5")
                .description("Test Version Description 5")
                .id(10002L).projectId(10000l);

        assertThat(expectedVersion, equalTo(actualVersion));

        actualVersion = versionClient.get("10010");
        expectedVersion = new Version().self(createSelfLink(10010))
                .archived(false).released(false).name("One")
                .id(10010L).projectId(10001l);

        assertThat(expectedVersion, equalTo(actualVersion));

        actualVersion = versionClient.get("10011");
        expectedVersion = new Version().self(createSelfLink(10011)).name("Two").description("Two")
                .releaseDate("01/Mar/11").archived(false).released(false).overdue(true).id(10011L).projectId(10001l);

        assertThat(expectedVersion, equalTo(actualVersion));

        actualVersion = versionClient.get("10012");
        expectedVersion = new Version().self(createSelfLink(10012)).name("Three").description("Three")
                .releaseDate("09/Mar/11").archived(false).released(true).id(10012L).projectId(10001l);

        assertThat(expectedVersion, equalTo(actualVersion));

        actualVersion = versionClient.get("10013");
        expectedVersion = new Version().self(createSelfLink(10013)).name("Four").archived(true).released(false).id(10013L).projectId(10001l);

        assertThat(expectedVersion, equalTo(actualVersion));

        actualVersion = versionClient.get("10014");
        expectedVersion = new Version().self(createSelfLink(10014)).name("Five").description("Five")
                .archived(true).released(true).id(10014L).projectId(10001l);

        assertThat(expectedVersion, equalTo(actualVersion));

        //The admin user can't actually browse this project. Make sure they can still see the version because of permission.
        actualVersion = versionClient.get("10110");
        expectedVersion = new Version().self(createSelfLink(10110)).name("Hidden")
                .archived(false).released(false).id(10110L).projectId(10010l);

        assertThat(expectedVersion, equalTo(actualVersion));

        //Can we access projects as a project admin.
        actualVersion = versionClient.loginAs(FRED_USERNAME).get("10110");
        expectedVersion = new Version().self(createSelfLink(10110)).name("Hidden")
                .archived(false).released(false).id(10110L).projectId(10010l);

        assertThat(expectedVersion, equalTo(actualVersion));
    }

    public void testViewVersionNotFound() throws Exception
    {
        final Response resp1 = versionClient.getResponse("1");
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Could not find version for id '1'"));

        final Response respZbing = versionClient.getResponse("zbing");
        assertThat(respZbing.statusCode, equalTo(404));
        assertTrue(respZbing.entity.errorMessages.contains("Could not find version for id 'zbing'"));
    }

    public void testCreateVersionErrors() throws Exception
    {
        //No project specified.
        Response response = versionClient.createResponse(new Version());
        assertThat(response.statusCode, equalTo(BAD_REQUEST.getStatusCode()));
        assertThat(response.entity, equalTo(new Errors().addError("Project must be specified to create a version.")));

        //Two Release Dates.
        response = versionClient.createResponse(new Version().releaseDate("1/Jan/2000").project("IGNORED"));
        assertThat(response.statusCode, equalTo(BAD_REQUEST.getStatusCode()));
        assertThat(response.entity, equalTo(new Errors().addError("Only one of 'releaseDate' and 'userReleaseDate' can be specified when creating a version.")));

        //Bad project specified
        response = versionClient.createResponse(new Version().project("BAD"));
        assertThat(response.statusCode, equalTo(NOT_FOUND.getStatusCode()));
        assertThat(response.entity, equalTo(new Errors().addError("Project with key 'BAD' either does not exist or you do not have permission to create versions in it.")));

        //Project exists but the user does not have admin permissions.
        response = versionClient.loginAs(FRED_USERNAME).createResponse(new Version().project("MKY"));
        assertThat(response.statusCode, equalTo(NOT_FOUND.getStatusCode()));
        assertThat(response.entity, equalTo(new Errors().addError("Project with key 'MKY' either does not exist or you do not have permission to create versions in it.")));

        //Don't specify a project name.
        response = versionClient.loginAs("admin").createResponse(new Version().project("MKY"));
        assertThat(response.statusCode, equalTo(BAD_REQUEST.getStatusCode()));
        assertThat(response.entity, equalTo(new Errors().addError("name", "You must specify a valid version name")));
    }

    public void testCreateVersion() throws Exception
    {
        final LocalDate tenDaysAgo = new LocalDate().dayOfYear().addToCopy(-10);

        //Create a simple version.
        Version inputVersion = new Version().project("MKY").name("New Version");
        Version newVersion = versionClient.create(inputVersion);
        assertThat(newVersion, equalTo(addNewAttributes(inputVersion, newVersion).projectId(10001l)));

        //Create a version with description.
        inputVersion = new Version().project("MKY").name("New Version2").description("Description 2");
        newVersion = versionClient.create(inputVersion);
        assertThat(newVersion, equalTo(addNewAttributes(inputVersion, newVersion).projectId(10001l)));

        //Create a version with a release date as string.
        inputVersion = new Version().project("MKY").name("New Version3").description("Description 3").releaseDate("12/Oct/81", null);
        newVersion = versionClient.create(inputVersion);
        assertThat(newVersion, equalTo(addNewAttributes(inputVersion, newVersion).overdue(true).releaseDate("12/Oct/81").projectId(10001l)));

        //Create a version with release date as a version.
        inputVersion = new Version().project("MKY").name("New Version4").description("Description 4").releaseDate(null, tenDaysAgo);
        newVersion = versionClient.create(inputVersion);
        assertThat(newVersion, equalTo(addNewAttributes(inputVersion, newVersion).overdue(true).releaseDate(tenDaysAgo).projectId(10001l).userReleaseDate(tenDaysAgo.toString("dd/MMM/yy"))));

        final LocalDate tenDaysFromNow = tenDaysAgo.dayOfYear().addToCopy(20);

        //We can't browse this project but we should be able to create issues as the system admin.
        inputVersion = new Version().project("HIDDEN").name("Hidden1").releaseDate(null, tenDaysFromNow);
        newVersion = versionClient.create(inputVersion);
        assertThat(newVersion, equalTo(addNewAttributes(inputVersion, newVersion).releaseDate(tenDaysFromNow).overdue(false).userReleaseDate(tenDaysFromNow.toString("dd/MMM/yy")).projectId(10010l)));

        //Make sure we can create a version as fred who has Project Admin permission.
        inputVersion = new Version().project("HIDDEN").name("Hidden2");
        newVersion = versionClient.loginAs(FRED_USERNAME).create(inputVersion);
        assertThat(newVersion, equalTo(addNewAttributes(inputVersion, newVersion).projectId(10010l)));
    }

    public void testMoveVersion() throws Exception
    {
        // Get the versions for the project  before we start just to be sure what is going on..
        assertVersionSequence("MKY", "Five", "One", "Two", "Three", "Four");

        // move the "Five" down.
        VersionMove vm = new VersionMove().position("Later");
        versionClient.move("10014", vm);
        assertVersionSequence("MKY", "One", "Five", "Two", "Three", "Four");

        // move the "Five" down.
        vm = new VersionMove().position("Later");
        versionClient.move("10014", vm);
        assertVersionSequence("MKY", "One", "Two", "Five", "Three", "Four");

        // move the "Two" up.
        vm = new VersionMove().position("Earlier");
        versionClient.move("10011", vm);
        assertVersionSequence("MKY", "Two", "One", "Five", "Three", "Four");

        // move the "Four" up.
        vm = new VersionMove().position("Earlier");
        versionClient.move("10013", vm);
        assertVersionSequence("MKY", "Two", "One", "Five", "Four", "Three");
        
        // move the "Two" up. Already at the top so no change
        vm = new VersionMove().position("Earlier");
        versionClient.move("10011", vm);
        assertVersionSequence("MKY", "Two", "One", "Five", "Four", "Three");

        // move the "Three" down. Already at the bottom so no change
        vm = new VersionMove().position("Later");
        versionClient.move("10012", vm);
        assertVersionSequence("MKY", "Two", "One", "Five", "Four", "Three");

        // move the "Four" to top.
        vm = new VersionMove().position("First");
        versionClient.move("10013", vm);
        assertVersionSequence("MKY", "Four", "Two", "One", "Five", "Three");

        // move the "Four" to top.
        vm = new VersionMove().position("First");
        versionClient.move("10013", vm);
        assertVersionSequence("MKY", "Four", "Two", "One", "Five", "Three");

        // move the "Two" to the bottom.
        vm = new VersionMove().position("Last");
        versionClient.move("10011", vm);
        assertVersionSequence("MKY", "Four", "One", "Five", "Three", "Two");

        // move the "Two" to the bottom.
        vm = new VersionMove().position("Last");
        versionClient.move("10011", vm);
        assertVersionSequence("MKY", "Four", "One", "Five", "Three", "Two");

        // all into order by putting one to the top then the rest after it starting with "Five"
        vm = new VersionMove().position("First");
        versionClient.move("10010", vm);
        vm = new VersionMove().after(createSelfLink(10010));
        versionClient.move("10014", vm);
        vm = new VersionMove().after(createSelfLink(10010));
        versionClient.move("10013", vm);
        vm = new VersionMove().after(createSelfLink(10010));
        versionClient.move("10012", vm);
        vm = new VersionMove().after(createSelfLink(10010));
        versionClient.move("10011", vm);
        assertVersionSequence("MKY", "One", "Two", "Three", "Four", "Five");
    }

    public void testMoveVersionErrors() throws Exception
    {
        // Version not found.
        VersionMove vm = new VersionMove().position("Later");
        Response response = versionClient.moveResponse("99914", vm);
        assertThat(response.statusCode, equalTo(NOT_FOUND.getStatusCode()));
        assertThat(response.entity, equalTo(new Errors().addError("Could not find version for id '99914'")));

        // Move after target not found.
        vm = new VersionMove().after(createSelfLink(19910));
        response = versionClient.moveResponse("10014", vm);
        assertThat(response.statusCode, equalTo(NOT_FOUND.getStatusCode()));
        assertThat(response.entity, equalTo(new Errors().addError("Could not find version with id '19910' for project 'MKY'")));

        // Move after target wrong project.
        vm = new VersionMove().after(createSelfLink(10000));
        response = versionClient.moveResponse("10014", vm);
        assertThat(response.statusCode, equalTo(NOT_FOUND.getStatusCode()));
        assertThat(response.entity, equalTo(new Errors().addError("Could not find version with id '10000' for project 'MKY'")));

//        // Move to a bad place
//        vm = new VersionMove().position("middle");
//        response = versionClient.moveResponse("10014", vm);
//        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
//        assertEquals(new Errors().addError("Could not find version with id '10000' for project 'MKY'"), response.entity);
    }

    public void testDeleteVersion() throws Exception
    {
        // Delete version no swap to versions
        versionClient.delete("10010");
        Response resp1 = versionClient.getResponse("10010");
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Could not find version for id '10010'"));

        // Delete and move fix and affected versions
        final String issueKey = navigation.issue().createIssue("monkey", "Bug", "Issue for voting test");
        navigation.issue().setFixVersions(issueKey, "Two");
        navigation.issue().setAffectsVersions(issueKey, "Two");

        versionClient.delete("10011", createSelfLink(10013), createSelfLink(10014));
        resp1 = versionClient.getResponse("10011");
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Could not find version for id '10011'"));

        // Search for fixVersion = "Four" should return one issue
        final SearchResult fixFourResults = searchClient.postSearch(new SearchRequest().jql("fixVersion = Four"));
        assertThat(fixFourResults.total, equalTo(1));
        assertThat(fixFourResults.issues.size(), equalTo(1));

        // Search for fixVersion = "Five" should return one issue
        SearchResult affectedFive = searchClient.postSearch(new SearchRequest().jql("affectedVersion = Five"));
        assertThat(affectedFive.total, equalTo(1));
        assertThat(affectedFive.issues.size(), equalTo(1));

        // Delete version and clear issue links
        versionClient.delete("10013");
        resp1 = versionClient.getResponse("10013");
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Could not find version for id '10013'"));

        // Search for fixVersion = "Four" should return no issues
        Response<SearchResult> result = searchClient.postSearchResponse(new SearchRequest().jql("fixVersion = Four"));
        assertThat(result.statusCode, equalTo(400));
        assertTrue(result.entity.errorMessages.contains("The value 'Four' does not exist for the field 'fixVersion'."));

        // Search for fixVersion = "Five" should return one issue
        affectedFive = searchClient.postSearch(new SearchRequest().jql("affectedVersion = Five"));
        assertThat(affectedFive.total, equalTo(1));
        assertThat(affectedFive.issues.size(), equalTo(1));

        // Delete version and clear issue links
        versionClient.delete("10014");
        resp1 = versionClient.getResponse("10014");
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Could not find version for id '10014'"));

        // Search for fixVersion = "Four" should return no issues
        result = searchClient.postSearchResponse(new SearchRequest().jql("fixVersion = Four"));
        assertThat(result.statusCode, equalTo(400));
        assertTrue(result.entity.errorMessages.contains("The value 'Four' does not exist for the field 'fixVersion'."));

        // Search for fixVersion = "Five" should return no issues
        result = searchClient.postSearchResponse(new SearchRequest().jql("affectedVersion = Five"));
        assertThat(result.statusCode, equalTo(400));
        assertTrue(result.entity.errorMessages.contains("The value 'Five' does not exist for the field 'affectedVersion'."));

    }

    public void testDeleteVersionErrorConditions() throws Exception
    {
        // Delete version no swap to versions
        Response resp1 = versionClient.delete("99010");
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Could not find version for id '99010'"));

        // Delete version no swap to versions
        resp1 = versionClient.loginAs(FRED_USERNAME).delete("10010");
        assertThat(resp1.statusCode, equalTo(401));
        assertTrue(resp1.entity.errorMessages.contains("The user fred does not have permission to complete this operation."));

        // Delete and move fix and affected versions
        final String issueKey = navigation.issue().createIssue("monkey", "Bug", "Issue for voting test");
        navigation.issue().setFixVersions(issueKey, "Two");
        navigation.issue().setAffectsVersions(issueKey, "Two");

        resp1 = versionClient.loginAs("admin").delete("10011", createSelfLink(10011), createSelfLink(10014));
        assertThat(resp1.statusCode, equalTo(400));
        assertTrue(resp1.entity.errorMessages.contains("You cannot move the issues to the version being deleted."));

        resp1 = versionClient.loginAs("admin").delete("10011", createSelfLink(10012), createSelfLink(10011));
        assertThat(resp1.statusCode, equalTo(400));
        assertTrue(resp1.entity.errorMessages.contains("You cannot move the issues to the version being deleted."));

        resp1 = versionClient.loginAs("admin").delete("10011", createSelfLink(10011), createSelfLink(23014));
        assertThat(resp1.statusCode, equalTo(400));
        assertTrue(resp1.entity.errorMessages.contains("The affects version with id 23014 does not exist."));

        resp1 = versionClient.loginAs("admin").delete("10011", createSelfLink(23011), createSelfLink(10014));
        assertThat(resp1.statusCode, equalTo(400));
        assertTrue(resp1.entity.errorMessages.contains("The fix version with id 23011 does not exist."));
    }

    public void testGetVersionIssueCounts() throws Exception
    {
        VersionIssueCounts counts = versionClient.getVersionIssueCounts("10000");
        assertThat(counts.issuesFixedCount, equalTo(0L));
        assertThat(counts.issuesAffectedCount, equalTo(0L));

        // Add an issue to this version
        String issueKey = navigation.issue().createIssue("monkey", "Bug", "Issue for voting test");
        navigation.issue().setFixVersions(issueKey, "Two");
        navigation.issue().setAffectsVersions(issueKey, "Two");
        counts = versionClient.getVersionIssueCounts("10011");
        assertThat(counts.issuesFixedCount, equalTo(1L));
        assertThat(counts.issuesAffectedCount, equalTo(1L));

        // Add an issue to this version
        issueKey = navigation.issue().createIssue("monkey", "Bug", "Issue for voting test");
        navigation.issue().setFixVersions(issueKey, "Two");
        counts = versionClient.getVersionIssueCounts("10011");
        assertThat(counts.issuesFixedCount, equalTo(2L));
        assertThat(counts.issuesAffectedCount, equalTo(1L));

        // Add an issue to this version
        issueKey = navigation.issue().createIssue("monkey", "Bug", "Issue for voting test");
        navigation.issue().setFixVersions(issueKey, "Two");
        navigation.issue().setAffectsVersions(issueKey, "Three");
        counts = versionClient.getVersionIssueCounts("10011");
        assertThat(counts.issuesFixedCount, equalTo(3L));
        assertThat(counts.issuesAffectedCount, equalTo(1L));
        counts = versionClient.getVersionIssueCounts("10012");
        assertThat(counts.issuesFixedCount, equalTo(0L));
        assertThat(counts.issuesAffectedCount, equalTo(1L));
    }

    public void testGetVersionIssueCountsNotFound() throws Exception
    {
        final Response resp1 = versionClient.getVersionIssueCountsResponse("1");
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Could not find version for id '1'"));

        final Response respZbing = versionClient.getVersionIssueCountsResponse("zbing");
        assertThat(respZbing.statusCode, equalTo(404));
        assertTrue(respZbing.entity.errorMessages.contains("Could not find version for id 'zbing'"));
    }

    public void testGetVersionUnresolvedIssueCount() throws Exception
    {
        VersionUnresolvedIssueCount counts = versionClient.getVersionUnresolvedIssueCount("10000");
        assertThat(counts.issuesUnresolvedCount, equalTo(0L));

        // Add an issue to this version
        String issueKey = navigation.issue().createIssue("monkey", "Bug", "Issue for voting test");
        navigation.issue().setFixVersions(issueKey, "Two");
        counts = versionClient.getVersionUnresolvedIssueCount("10011");
        assertThat(counts.issuesUnresolvedCount, equalTo(1L));

        // Add an issue to this version
        issueKey = navigation.issue().createIssue("monkey", "Bug", "Issue for voting test");
        navigation.issue().setFixVersions(issueKey, "Two");
        counts = versionClient.getVersionUnresolvedIssueCount("10011");
        assertThat(counts.issuesUnresolvedCount, equalTo(2L));

        // Add an issue to this version
        issueKey = navigation.issue().createIssue("monkey", "Bug", "Issue for voting test");
        navigation.issue().setFixVersions(issueKey, "Two");
        counts = versionClient.getVersionUnresolvedIssueCount("10011");
        assertThat(counts.issuesUnresolvedCount, equalTo(3L));
        counts = versionClient.getVersionUnresolvedIssueCount("10012");
        assertThat(counts.issuesUnresolvedCount, equalTo(0L));
    }

    public void testGetVersionUnresolvedIssueCountNotFound() throws Exception
    {
        final Response resp1 = versionClient.getVersionUnresolvedIssueCountResponse("1");
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Could not find version for id '1'"));

        final Response respZbing = versionClient.getVersionUnresolvedIssueCountResponse("zbing");
        assertThat(respZbing.statusCode, equalTo(404));
        assertTrue(respZbing.entity.errorMessages.contains("Could not find version for id 'zbing'"));
    }

    private void assertVersionSequence(String project, String... names)
    {
        final List<Version> versions = projectClient.getVersions(project);
        assertThat(versions.size(), equalTo(names.length));
        assertVersionSequence(versions, names);
    }

    private void assertVersionSequence(List<Version> versions, String[] names)
    {
        for (int i=0; i<names.length; i++)
        {
            final String name = names[i];
            assertThat(versions.get(i).name, equalTo(name));
        }
    }


    private Version addNewAttributes(Version version, Version newVersion)
    {
        return version.self(newVersion.self).archived(false).released(false).project(null).id(newVersion.id);
    }

    private URI createSelfLink(long id)
    {
        return getRestApiUri("version", String.valueOf(id));
    }

    private void assertReleaseDateEqualTo(String versionId, String expectedDate)
    {
        final List<Version> versions = Arrays.asList(
                versionClient.loginAs(BOB_USERNAME).get(versionId),
                versionClient.loginAs(ADMIN_USERNAME).get(versionId),
                versionClient.loginAs(FRED_USERNAME).get(versionId)
        );

        for (Version version : versions)
        {
            assertThat(String.format("Release date is wrong in %s", version), version.userReleaseDate, equalTo(expectedDate));
        }
    }
}
