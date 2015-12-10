package com.atlassian.jira.webtests.ztests.admin.trustedapps;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebResponse;
import net.sourceforge.jwebunit.TestContext;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Works as a client, tests that requests can be made securely.
 *
 * @since v3.12
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestTrustedApplicationClientVersion0 extends JIRAWebTest
{
    static
    {
        try
        {
            Security.addProvider((Provider) Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider").newInstance());
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (ClassNotFoundException ignoreHopeFullyRunningUnderJava5WhichSupportsRSA)
        {
            // ignore
        }
    }

    private static final class TrustedAppHeader
    {
        /**
         * Header parameter name for trusted application ID
         */
        static final String APP_ID = "X-Seraph-Trusted-App-ID";

        /**
         * Header parameter name for trusted application certificate
         */
        static final String CERTIFICATE = "X-Seraph-Trusted-App-Cert";

        /**
         * Response header parameter that will contain trusted application error message if it fails
         */
        static final String ERROR = "X-Seraph-Trusted-App-Error";

        /**
         * Response header parameter that indicates the status of the request (ERROR or OK)
         */
        static final String STATUS = "X-Seraph-Trusted-App-Status";

        /**
         * Header parameter name for the secret key, used to encrypt the certificate.
         */
        static final String SECRET_KEY = "X-Seraph-Trusted-App-Key";
    }

    private static final class Status
    {
        /**
         * Status of ERROR
         */
        static final String ERROR = "ERROR";

        /**
         * Status of OK
         */
        static final String OK = "OK";
    }

    private static final String ID = "TestTrustedApplicationClient.id";
    private static final String REQUEST_STRING = "/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml";

    public TestTrustedApplicationClientVersion0(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestTrustedAppsWithIssues.xml");

        gotoAdmin();
        gotoPage("/secure/admin/trustedapps/ViewTrustedApplications.jspa");
        File file = new File(getEnvironmentData().getXMLDataLocation(), "/trustedapp/");
        setFormElement("trustedAppBaseUrl", file.toURI().toASCIIString());
        submit("Send Request");

        setWorkingForm("jiraform");
        setFormElement("name", "TestTrustedApplicationClient");
        setFormElement("timeout", "500");
        submit("Add");
        logout();
    }

    /* Call all tests from one method, so that setUp isn't run every time */
    public void testAll() throws Exception
    {
        _testClientNotLoggedIn();
        _testUnknownClientLoggedInFred();
        _testClientLoggedInUnknown();
        _testClientLoggedInFred();
        _testClientLoggedInAdmin();
        _testInvalidCertData();
        _testInvalidCertSecretKey();
        _testCertificateExpires();
        _testUrlNotMatched();
    }

    /* test that the RSS feed contains no entries (not logged in at all) */
    public void _testClientNotLoggedIn() throws Exception
    {
        logout();
        GetMethodWebRequest request = new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + REQUEST_STRING);

        new Token().populateRequest("nothing", request);

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
        assertResponseContainsBadProtocolError(response);
    }

    /* test that the request returns an error due to an invalid user being sent */
    public void _testClientLoggedInUnknown() throws Exception
    {
        logout();
        GetMethodWebRequest request = new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + REQUEST_STRING);

        new Token().populateRequest("unknown-user", request);

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
        assertResponseContainsBadProtocolError(response);

    }

    /* test that the RSS feed contains one item (logged in as fred) */
    public void _testClientLoggedInFred() throws Exception
    {
        logout();
        GetMethodWebRequest request = new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + REQUEST_STRING);

        final Token token = new Token();
        token.populateRequest("FreD", request);

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);

        assertResponseContainsError(response);
        // valid user gets no items due to invalid signature
        assertResponseContainsNoItems(response);
        assertResponseContainsBadProtocolError(response);
    }

    /* test that the RSS feed contains two items (logged in as admin) */
    public void _testClientLoggedInAdmin() throws Exception
    {
        logout();
        GetMethodWebRequest request = new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + REQUEST_STRING);

        final Token token = new Token();
        token.populateRequest("aDmIn", request);

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);

        assertResponseContainsError(response);
        // valid user gets no items due to invalid signature
        assertResponseContainsNoItems(response);
        assertResponseContainsBadProtocolError(response);
    }

    /* test that the request returns an error due to an unregistered client making the request */
    public void _testUnknownClientLoggedInFred() throws Exception
    {
        logout();
        GetMethodWebRequest request = new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + REQUEST_STRING);

        new Token().populateRequest(FRED_USERNAME, request);
        request.setHeaderField(TrustedAppHeader.APP_ID, ID + ".unknown");

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
        assertResponseContainsBadProtocolError(response);
    }

    /* test that the request returns an error due to the certificate sent being invalid (encrypted data) */
    public void _testInvalidCertData() throws Exception
    {
        logout();
        GetMethodWebRequest request = new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + REQUEST_STRING);

        new Token().populateRequest("blah", request);
        request.setHeaderField(TrustedAppHeader.CERTIFICATE, ID); // just a random text simulating invalid cert

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
        assertResponseContainsBadProtocolError(response);
    }

    /* test that the request returns an error due to the certificate sent being invalid */
    public void _testInvalidCertSecretKey() throws Exception
    {
        logout();
        GetMethodWebRequest request = new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + REQUEST_STRING);

        new Token().populateRequest(FRED_USERNAME, request);
        request.setHeaderField(TrustedAppHeader.SECRET_KEY, ID); // just a random text simulating invalid cert

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
        assertResponseContainsBadProtocolError(response);
    }

    /*test that the request returns an error due to request taking too long */
    public void _testCertificateExpires() throws Exception
    {
        logout();
        GetMethodWebRequest request = new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + REQUEST_STRING);

        new Token().populateRequest(FRED_USERNAME, request);

        // wait until timeout has elapsed
        Thread.sleep(550);

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertResponseContainsError(response);
        assertResponseContainsNoItems(response);
        assertResponseContainsBadProtocolError(response);
    }

    /* test that the request returns an error due to the URL requested not being part of the matching URLs */
    public void _testUrlNotMatched() throws Exception
    {
        logout();
        GetMethodWebRequest request = new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + "/browse/MKY-1");

        new Token().populateRequest(FRED_USERNAME, request);

        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();
        final WebResponse response = webClient.sendRequest(request);
        assertNotNull(response);
        assertTrue(response.getText(), response.getText().indexOf("You must log in to access this page.") >= 0);
    }

    private void assertResponseContainsError(WebResponse response)
    {
        assertNotNull(response);
        assertNotNull(response.getHeaderField(TrustedAppHeader.STATUS));
        assertEquals(Status.ERROR, response.getHeaderField(TrustedAppHeader.STATUS));
        assertNotNull(response.getHeaderField(TrustedAppHeader.ERROR));
    }

    private void assertResponseContainsBadProtocolError(WebResponse response)
    {
        assertEquals("BAD_PROTOCOL_VERSION; Unsupported protocol version: {0}. required {1}; [\"0\",\"2\"]", response.getHeaderField(TrustedAppHeader.ERROR));
    }

    private void assertResponseContainsNoItems(WebResponse response) throws SAXException
    {
        assertNotNull(response);
        final Document doc = response.getDOM();
        final NodeList nodes = doc.getElementsByTagName("item");
        assertEquals(0, nodes.getLength());
    }

    private static class Token
    {
        /**
         * make sure this data is in sync with the file in xml/admim/appTrustCertificate.
         * The PublicKey in there and the algorithm must match this algorithm/private key.
         */
        private static final class KeyData
        {
            private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCySptbugHAzWUJY3ALWhuSCPhVXnwbUBfsRExYQitBCVny4V1DcU2SAx22bH9dSM0X7NdMObF74r+Wd77QoPAtaySqFLqCeRCbFmhHgVSi+pGeCipTpueefSkz2AX8Aj+9x27tqjBsX1LtNWVLDsinEhBWN68R+iEOmf/6jGWObQIDAQAB";
            private static final String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALJKm1u6AcDNZQljcAtaG5II+FVefBtQF+xETFhCK0EJWfLhXUNxTZIDHbZsf11IzRfs10w5sXviv5Z3vtCg8C1rJKoUuoJ5EJsWaEeBVKL6kZ4KKlOm5559KTPYBfwCP73Hbu2qMGxfUu01ZUsOyKcSEFY3rxH6IQ6Z//qMZY5tAgMBAAECgYB4QXJAkFmWXfOEPZnZTlHCUmKN0kkLcx5vsjF8ZkUefNw6wl9Rmh6kGY30+YF+vhf3xzwAoflggjSPnP0LY0Ibf0XxMcNjR1zBsl9X7gKfXghIunS6gbcwrEwBNc5GR4zkYjYaZQ4zVvm3oMS2glV9NlXAUl41VL2XAQC/ENwbUQJBAOdoAz4hZGgke9AxoKLZh215gY+PLXqVLlWf14Ypk70Efk/bVvF10EsAOuAm9queCyr0qNf/vgHrm4HHXwJz4SsCQQDFPXir5qs+Kf2Y0KQ+WO5IRaNmrOlNvWDqJP/tDGfF/TYo6nSI0dGtWNfwZyDB47PbUq3zxCHYjExBJ9vQNZLHAkEA4JlCtHYCl1X52jug1w7c9DN/vc/Q626J909aB3ypSUdoNagFPf0EexcxDcijmDSgUEQA8Qzm5cRBPfg9Tgsc2wJBAIKbiv2hmEFowtHfTvMuJlNbMbF6zF67CaLib0oEDe+QFb4QSqyS69py20MItytM4btYy3GArbzcYl4+y5La9t8CQE2BkMV3MLcpAKjxtK5SYwCyLT591k35isGxmIlSQBQbDmGP9L5ZeXmVGVxRCGbBQjCzeoafPvUZo65kaRQHUJc=";
            private static final String ALGORITHM = "RSA";
            private static final String BOUNCY_CASTLE_PROVIDER = "BC";
            private static final String STREAM_CIPHER = "RC4";
            private static final String ASYM_CIPHER = "RSA/NONE/NoPadding";
            private static final String CHARSET_NAME = "UTF-8";
        }

        final PrivateKey privateKey;
        final PublicKey publicKey;
        final SecretKey secretKey;

        String unencoded;

        Token()
        {
            try
            {
                KeyFactory keyFactory = KeyFactory.getInstance(KeyData.ALGORITHM, KeyData.BOUNCY_CASTLE_PROVIDER);
                privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(KeyData.PRIVATE_KEY.getBytes())));
                publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(KeyData.PUBLIC_KEY.getBytes())));

                secretKey = new SecretKeyGenerator().generateKey();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        String getSecretKey()
        {
            try
            {
                Cipher cipher = Cipher.getInstance(KeyData.ASYM_CIPHER, KeyData.BOUNCY_CASTLE_PROVIDER);
                cipher.init(Cipher.ENCRYPT_MODE, privateKey);
                return new String(Base64.encodeBase64(cipher.doFinal(secretKey.getEncoded())));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        String getCert(String userName)
        {
            try
            {
                Cipher cipher = Cipher.getInstance(KeyData.STREAM_CIPHER, KeyData.BOUNCY_CASTLE_PROVIDER);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);

                StringWriter writer = new StringWriter();
                writer.write(String.valueOf(System.currentTimeMillis()));
                writer.write('\n');
                writer.write(userName);
                writer.flush();
                unencoded = writer.toString();
                byte[] encryptedData = cipher.doFinal(unencoded.getBytes(KeyData.CHARSET_NAME));
                return new String(Base64.encodeBase64(encryptedData));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        void populateRequest(String userName, GetMethodWebRequest request)
        {
            request.setHeaderField(TrustedAppHeader.APP_ID, ID);
            request.setHeaderField(TrustedAppHeader.SECRET_KEY, getSecretKey());
            request.setHeaderField(TrustedAppHeader.CERTIFICATE, getCert(userName));
        }

        public String toString()
        {
            return unencoded;
        }

        class SecretKeyGenerator
        {
            SecretKey generateKey()
            {
                SecretKey result = generateSecretKey();
                while (!isValid(result))
                {
                    result = generateSecretKey();
                }
                return result;
            }

            private boolean isValid(SecretKey secretKey)
            {
                final byte[] encoded = secretKey.getEncoded();
                return (encoded.length == 16) && (encoded[0] != 0);
            }

            private SecretKey generateSecretKey()
            {
                try
                {
                    return KeyGenerator.getInstance(KeyData.STREAM_CIPHER, KeyData.BOUNCY_CASTLE_PROVIDER).generateKey();
                }
                catch (NoSuchAlgorithmException e)
                {
                    throw new RuntimeException(e);
                }
                catch (NoSuchProviderException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}