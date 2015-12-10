package com.atlassian.jira.webtests.ztests.admin.trustedapps;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.security.auth.trustedapps.BouncyCastleEncryptionProvider;
import com.atlassian.security.auth.trustedapps.EncryptedCertificate;
import com.atlassian.security.auth.trustedapps.EncryptionProvider;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;
import com.atlassian.security.auth.trustedapps.request.TrustedRequest;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebResponse;
import net.sourceforge.jwebunit.TestContext;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

/**
 * Test using Trusted Apps v2 and v3 Protocols.
 *
 * @since v6.2
 */
@WebTest ({ Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestTrustedApplicationClient extends FuncTestCase
{
    private static final String ID = "TestTrustedApplicationClient.id";
    private static final String REQUEST_STRING = "/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml";
    private static final String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALJKm1u6AcDNZQljcAtaG5II+FVefBtQF+xETFhCK0EJWfLhXUNxTZIDHbZsf11IzRfs10w5sXviv5Z3vtCg8C1rJKoUuoJ5EJsWaEeBVKL6kZ4KKlOm5559KTPYBfwCP73Hbu2qMGxfUu01ZUsOyKcSEFY3rxH6IQ6Z//qMZY5tAgMBAAECgYB4QXJAkFmWXfOEPZnZTlHCUmKN0kkLcx5vsjF8ZkUefNw6wl9Rmh6kGY30+YF+vhf3xzwAoflggjSPnP0LY0Ibf0XxMcNjR1zBsl9X7gKfXghIunS6gbcwrEwBNc5GR4zkYjYaZQ4zVvm3oMS2glV9NlXAUl41VL2XAQC/ENwbUQJBAOdoAz4hZGgke9AxoKLZh215gY+PLXqVLlWf14Ypk70Efk/bVvF10EsAOuAm9queCyr0qNf/vgHrm4HHXwJz4SsCQQDFPXir5qs+Kf2Y0KQ+WO5IRaNmrOlNvWDqJP/tDGfF/TYo6nSI0dGtWNfwZyDB47PbUq3zxCHYjExBJ9vQNZLHAkEA4JlCtHYCl1X52jug1w7c9DN/vc/Q626J909aB3ypSUdoNagFPf0EexcxDcijmDSgUEQA8Qzm5cRBPfg9Tgsc2wJBAIKbiv2hmEFowtHfTvMuJlNbMbF6zF67CaLib0oEDe+QFb4QSqyS69py20MItytM4btYy3GArbzcYl4+y5La9t8CQE2BkMV3MLcpAKjxtK5SYwCyLT591k35isGxmIlSQBQbDmGP9L5ZeXmVGVxRCGbBQjCzeoafPvUZo65kaRQHUJc=";
    private String localProtocolVersion;
    private String remoteProtocolVersion;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        // record existing confg
        try
        {
            localProtocolVersion = System.getProperty(TrustedApplicationUtils.Constant.PROTOCOL_VERSION_KEY);
            remoteProtocolVersion = backdoor.systemProperties().getProperty(TrustedApplicationUtils.Constant.PROTOCOL_VERSION_KEY);
        }
        catch(Exception ex)
        {
            // do nothing
        }

        administration.restoreData("TestTrustedAppsWithIssues.xml");
        navigation.gotoAdmin();
        navigation.gotoPage("/secure/admin/trustedapps/ViewTrustedApplications.jspa");
        File file = new File(getEnvironmentData().getXMLDataLocation(), "/trustedapp/");
        tester.setFormElement("trustedAppBaseUrl", file.toURI().toASCIIString());
        tester.submit("Send Request");
        tester.setWorkingForm("jiraform");
        tester.setFormElement("name", "TestTrustedApplicationClient");
        tester.setFormElement("timeout", "500");
        tester.submit("Add");
        navigation.logout();
    }

    @Override
    protected void tearDownTest()
    {
        // revert to pre-existing config
        if(this.localProtocolVersion != null)
        {
            System.setProperty(TrustedApplicationUtils.Constant.PROTOCOL_VERSION_KEY, this.localProtocolVersion);
        }
        else
        {
            System.clearProperty(TrustedApplicationUtils.Constant.PROTOCOL_VERSION_KEY);
        }

        if(this.remoteProtocolVersion!= null)
        {
            backdoor.systemProperties().setProperty(TrustedApplicationUtils.Constant.PROTOCOL_VERSION_KEY, this.remoteProtocolVersion);
        }
    }

    public void testAllWithV2TrustedProtocol() throws Exception
    {
        backdoor.systemProperties().setProperty(TrustedApplicationUtils.Constant.PROTOCOL_VERSION_KEY, TrustedApplicationUtils.Constant.VERSION_TWO.toString());
        System.setProperty(TrustedApplicationUtils.Constant.PROTOCOL_VERSION_KEY,TrustedApplicationUtils.Constant.VERSION_TWO.toString());

        _testClientNotLoggedIn();
        _testUnknownClientLoggedInFred();
        _testClientLoggedInUnknown();
        _testClientLoggedInFred();
        _testClientLoggedInAdmin();
        _testInvalidCertData();
        _testInvalidCertSecretKey();
        _testUrlNotMatched();
    }

    public void testAllWithV3TrustedProtocol() throws Exception
    {
        backdoor.systemProperties().setProperty(TrustedApplicationUtils.Constant.PROTOCOL_VERSION_KEY, TrustedApplicationUtils.Constant.VERSION_THREE.toString());
        System.setProperty(TrustedApplicationUtils.Constant.PROTOCOL_VERSION_KEY, TrustedApplicationUtils.Constant.VERSION_THREE.toString());

        _testClientNotLoggedIn();
        _testUnknownClientLoggedInFred();
        _testClientLoggedInUnknown();
        _testClientLoggedInFred();
        _testClientLoggedInAdmin();
        _testInvalidCertData();
        _testInvalidCertSecretKeyWithV3Protocol();
        _testUrlNotMatched();
    }

    public void _testClientNotLoggedIn() throws Exception
    {
        GetMethodWebRequest request = getGetMethodWebRequest("nothing");

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
    }

    /* test that the request returns an error due to an invalid user being sent */
    public void _testClientLoggedInUnknown() throws Exception
    {
        GetMethodWebRequest request = getGetMethodWebRequest("unknown-user", getEnvironmentData().getBaseUrl() + REQUEST_STRING);

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
    }

    /* test that the RSS feed contains one item (logged in as fred) */
    public void _testClientLoggedInFred() throws Exception
    {
        GetMethodWebRequest request = getGetMethodWebRequest("FreD");

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsNoErrors(response);
        Document doc = response.getDOM();

        NodeList nodes = doc.getElementsByTagName("item");
        assertEquals(1, nodes.getLength());
        Node title = ((Element) nodes.item(0)).getElementsByTagName("title").item(0);
        final String text = title.getFirstChild().getNodeValue();
        assertNotNull(text);
        assertTrue(text.indexOf("HSP-1") >= 0);
        assertTrue(text.indexOf("A bug anyone can see") >= 0);
    }

    /* test that the RSS feed contains two items (logged in as admin) */
    public void _testClientLoggedInAdmin() throws Exception
    {
        GetMethodWebRequest request = getGetMethodWebRequest("aDmIn");

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsNoErrors(response);
        final Document doc = response.getDOM();

        final NodeList nodes = doc.getElementsByTagName("item");
        assertEquals(2, nodes.getLength());
        Node title = ((Element) nodes.item(0)).getElementsByTagName("title").item(0);
        String text = title.getFirstChild().getNodeValue();
        assertNotNull(text);
        assertTrue(text, text.indexOf("MKY-1") >= 0);
        assertTrue(text, text.indexOf("A bug only admin can see") >= 0);

        title = ((Element) nodes.item(1)).getElementsByTagName("title").item(0);
        text = title.getFirstChild().getNodeValue();
        assertNotNull(text);
        assertTrue(text, text.indexOf("HSP-1") >= 0);
        assertTrue(text, text.indexOf("A bug anyone can see") >= 0);
    }

    /* test that the request returns an error due to an unregistered client making the request */
    public void _testUnknownClientLoggedInFred() throws Exception
    {
        GetMethodWebRequest request = getGetMethodWebRequest(FRED_USERNAME);
        request.setHeaderField(TrustedApplicationUtils.Header.Request.ID, ID + ".unknown");

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
    }

    /* test that the request returns an error due to the certificate sent being invalid (encrypted data) */
    public void _testInvalidCertData() throws Exception
    {
        GetMethodWebRequest request = getGetMethodWebRequest("blah");
        request.setHeaderField(TrustedApplicationUtils.Header.Request.CERTIFICATE, ID); // just a random text simulating invalid cert

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
    }

    /* test that the request returns an error due to the certificate sent being invalid */
    public void _testInvalidCertSecretKey() throws Exception
    {
        GetMethodWebRequest request = getGetMethodWebRequest(FRED_USERNAME);
        request.setHeaderField(TrustedApplicationUtils.Header.Request.SECRET_KEY, ID); // just a random text simulating invalid cert

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
    }

    /* test that the request returns an error due to the certificate sent being invalid */
    public void _testInvalidCertSecretKeyWithV3Protocol() throws Exception
    {
        GetMethodWebRequest request = getGetMethodWebRequest(FRED_USERNAME);
        request.setHeaderField(TrustedApplicationUtils.Header.Request.SECRET_KEY, ID); // just a random text simulating invalid cert

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);

        // V3 protocol does not require secret key.
        assertResponseContainsNoErrors(response);
        final Document doc = response.getDOM();

        NodeList nodes = doc.getElementsByTagName("item");
        assertEquals(1, nodes.getLength());
        Node title = ((Element) nodes.item(0)).getElementsByTagName("title").item(0);
        final String text = title.getFirstChild().getNodeValue();
        assertNotNull(text);
        assertTrue(text.indexOf("HSP-1") >= 0);
        assertTrue(text.indexOf("A bug anyone can see") >= 0);
    }

    /*test that the request returns an error due to request taking too long */
    public void _testCertificateExpires() throws Exception
    {
        GetMethodWebRequest request = getGetMethodWebRequest(FRED_USERNAME);

        // wait until timeout has elapsed
        Thread.sleep(550);

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
    }

    /* test that the request returns an error due to the URL requested not being part of the matching URLs */
    public void _testUrlNotMatched() throws Exception
    {
        GetMethodWebRequest request = getGetMethodWebRequest(FRED_USERNAME, getEnvironmentData().getBaseUrl() + "/browse/MKY-1");

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertNotNull(response);
        assertTrue(response.getText(), response.getText().indexOf("You must log in to access this page.") >= 0);
    }

    private GetMethodWebRequest getGetMethodWebRequest(String username)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException
    {
        String url = getEnvironmentData().getBaseUrl() + REQUEST_STRING;
        return this.getGetMethodWebRequest(username, url);
    }
    private GetMethodWebRequest getGetMethodWebRequest(String username, String url)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException
    {

        GetMethodWebRequest request = new GetMethodWebRequest(url);
        EncryptionProvider encryptionProvider = new BouncyCastleEncryptionProvider();
        EncryptedCertificate encryptedCertificate = encryptionProvider.createEncryptedCertificate(username, encryptionProvider.toPrivateKey(Base64.decodeBase64(PRIVATE_KEY.getBytes())), ID, url);
        populateRequest(encryptedCertificate, request);
        return request;
    }

    private void assertResponseContainsError(WebResponse response)
    {
        assertNotNull(response);
        assertNotNull(response.getHeaderField(TrustedApplicationUtils.Header.Response.STATUS));
        assertEquals("ERROR", response.getHeaderField(TrustedApplicationUtils.Header.Response.STATUS));
        assertNotNull(response.getHeaderField(TrustedApplicationUtils.Header.Response.ERROR));
    }

    private void assertResponseContainsNoItems(WebResponse response) throws SAXException
    {
        assertNotNull(response);
        final Document doc = response.getDOM();
        final NodeList nodes = doc.getElementsByTagName("item");
        assertEquals(0, nodes.getLength());
    }

    private void assertResponseContainsNoErrors(WebResponse response)
    {
        assertNotNull(response);
        assertNotNull(response.getHeaderField(TrustedApplicationUtils.Header.Response.STATUS));
        assertEquals("OK", response.getHeaderField(TrustedApplicationUtils.Header.Response.STATUS));
        assertNull(response.getHeaderField(TrustedApplicationUtils.Header.Response.ERROR));
    }

    private void populateRequest(final EncryptedCertificate certificate, final GetMethodWebRequest request)
    {
        TrustedRequest trustedRequest = new TrustedRequest()
        {
            @Override
            public void addRequestParameter(String name, String value)
            {
                // GetMethodWebRequest does not accept null values for headers.
                if(value != null)
                {
                    request.setHeaderField(name, value);
                }
            }
        };
        TrustedApplicationUtils.addRequestParameters(certificate, trustedRequest);
    }
}
