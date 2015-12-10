package com.atlassian.jira.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Simple unit test for {@link UriValidator }.
 *
 * @since v4.2
 */
public class TestUriValidator
{
    private final String CANONICAL_URI =  "http://localhost:8090/jira";
    private final String RELATIVE_URL = "./Action.jspa?selectedId=4";
    private final String BROWSE_URL = "/browse/HSP-3";
    private final String HTTP_URL = "http://xyz.com/";
    private final String HTTPS_URL = "https://xyz.com/";
    private final String RELATIVE_NORMALIZED_URL = "Action.jspa?selectedId=4";
    private final String JAVASCRIPT_ENCODED_URI = "javascript%3Aalert%28%27owned%27%29;";
    private final String JAVASCRIPT_URI = "javascript:alert('owned');";
    private final String ENCODING_URL = "%6aavascript:alert('foo')";
    private final String BADSCHEME_URL = "badscheme://foo";
    private static final String XSS_ALERT_RAW = "\"><script>alert('owned')</script>";
    private static final String XSS_ALERT_ENCODED = "%22&gt;&lt;script&gt;alert(%27xss exploit%27)&lt;/script&gt;";

    private UriValidator uriValidator;



    @Before
    public void setUp()
    {
        uriValidator=new UriValidator("UTF-8");
    }


    @Test
    public void testJavascriptInjection()
    {
        assertNull("javascript returns null", uriValidator.getSafeUri(CANONICAL_URI, JAVASCRIPT_URI));
      }

      @Test
      public void testEncodedJavascriptInjection()
    {
        assertNull("encoded javascript returns null\"", uriValidator.getSafeUri(CANONICAL_URI, JAVASCRIPT_ENCODED_URI));
    }

    @Test
    public void testRelativeURL()
    {
        assertEquals("relative uri", RELATIVE_URL, uriValidator.getSafeUri(CANONICAL_URI, RELATIVE_URL));
    }

    @Test
    public void testHttpScheme()
    {
        assertEquals("absolute uri returned", HTTP_URL,uriValidator.getSafeUri(CANONICAL_URI, HTTP_URL));
    }

    @Test
    public void testHttpsScheme()
    {
        assertEquals("absolute uri returned", HTTPS_URL,uriValidator.getSafeUri(CANONICAL_URI, HTTPS_URL));
    }

    @Test
    public void testBrowseURL()
    {
        assertEquals("browse uri", BROWSE_URL , uriValidator.getSafeUri(CANONICAL_URI, BROWSE_URL));
    }

    @Test
    public void testEncodingInjection()
    {
        assertNull("sneaky encoding returns null", uriValidator.getSafeUri(CANONICAL_URI, ENCODING_URL));
    }

    @Test
    public void testBadSchemeInjection()
    {
        assertEquals("bad scheme returns drivel", BADSCHEME_URL, uriValidator.getSafeUri(CANONICAL_URI, BADSCHEME_URL));
    }

    @Test
    public void testXSSInjection()
    {
        assertNull("xss scheme returns null",  uriValidator.getSafeUri(CANONICAL_URI, XSS_ALERT_RAW));
    }

    @Test
    public void testXSSEncodedInjection()
    {
        assertNull("xss encoded scheme returns null",  uriValidator.getSafeUri(CANONICAL_URI, XSS_ALERT_ENCODED));
    }

}
