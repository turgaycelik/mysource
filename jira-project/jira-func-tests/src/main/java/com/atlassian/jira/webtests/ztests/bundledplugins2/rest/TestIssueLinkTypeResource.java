package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.IssueLinkType;
import com.atlassian.jira.testkit.client.restclient.IssueLinkTypeClient;
import com.atlassian.jira.testkit.client.restclient.IssueLinkTypes;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueLinkTypeResource extends RestFuncTest
{
    private IssueLinkTypeClient issueLinkTypeClient;
    @Rule public ExpectedException expectedException = ExpectedException.none();

    public void testGetAllIssueLinkTypes() throws Exception
    {
        final IssueLinkTypes issueLinkTypes = issueLinkTypeClient.getIssueLinkTypes();
        final List<IssueLinkType> list = issueLinkTypes.issueLinkTypes;
        assertEquals(2, list.size());
        IssueLinkType type = list.get(0);
        assertEquals(type.name, "Blocks");
        assertEquals("Blocks", type.outward);
        assertEquals("Blocked by", type.inward);
        assertEquals(new Long(10100).intValue(), type.id.intValue());
        assertEquals(getBaseUrlPlus("rest/api/2/issueLinkType/10100") ,type.self.toString());
        type = list.get(1);
        assertEquals(type.name, "Duplicate");
        assertEquals("Duplicates", type.outward);
        assertEquals("Duplicated by", type.inward);
        assertEquals(new Long(10000).intValue(), type.id.intValue());
        assertEquals(getBaseUrlPlus("rest/api/2/issueLinkType/10000") ,type.self.toString());
    }

    public void testGetAllIssueLinkTypesReturns404WhenIssueLinkingDisabled() throws Exception
    {
        oldway_consider_porting.deactivateIssueLinking();
        assertEquals(404, issueLinkTypeClient.getResponseForAllLinkTypes().statusCode);
    }

    public void testGetAllIssueLinkTypesAnonymousUserAllowed() throws Exception
    {
        final IssueLinkTypes issueLinkTypes = issueLinkTypeClient.anonymous().getIssueLinkTypes();
        final List<IssueLinkType> list = issueLinkTypes.issueLinkTypes;
        assertEquals(2, list.size());
    }

    public void testGetIssueLinkTypeReturns404WhenIssueLinkingDisabled() throws Exception
    {
        oldway_consider_porting.deactivateIssueLinking();
        assertEquals(404, issueLinkTypeClient.getResponseForLinkType("10000").statusCode);
    }

    public void testCreateIssueLinkType() throws Exception
    {
        IssueLinkType linkType = issueLinkTypeClient.createIssueLinkType("New Thing", "inbound", "outbound");
        assertThat(linkType.name, is("New Thing"));
        assertThat(linkType.inward, is("inbound"));
        assertThat(linkType.outward, is("outbound"));
    }

    public void testDeleteIssueLinkType() throws Exception
    {
        IssueLinkType linkType = issueLinkTypeClient.createIssueLinkType("New Thing", "inbound", "outbound");
        Response response = issueLinkTypeClient.deleteIssueLinkType(linkType.id.toString());
        assertThat(response.statusCode, is(204));

        try
        {
            issueLinkTypeClient.getIssueLinkType(linkType.id.toString());
            fail();
        }
        catch (UniformInterfaceException e)
        {
            assertThat(e.getResponse().getClientResponseStatus(), is(ClientResponse.Status.NOT_FOUND));
        }
    }

    public void testGetIssueLinkType() throws Exception
    {
        final IssueLinkType type = issueLinkTypeClient.anonymous().getIssueLinkType("10000");
        assertEquals(type.name, "Duplicate");
        assertEquals("Duplicates", type.outward);
        assertEquals("Duplicated by", type.inward);
        assertEquals(new Long(10000).intValue(), type.id.intValue());
    }

    public void testGetIssueLinkTypeIssueLinkTypeNotFound() throws Exception
    {
        final Response response = issueLinkTypeClient.getResponseForLinkType("10012");
        assertEquals(404, response.statusCode);
        assertEquals("No issue link type with id '10012' found.", response.entity.errorMessages.get(0));
    }



    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueLinkTypeClient = new IssueLinkTypeClient(getEnvironmentData());
        administration.restoreData("TestIssueLinkType.xml");
    }
}
