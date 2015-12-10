package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.MockUser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestTrustedApplicationSyntacticValidator
{
    class Fields
    {
        static final String BASE_URL = "trustedAppBaseUrl";
        static final String ENDPOINT = "trustedAppEndpoint";
        static final String NAME = "name";
        static final String TIMEOUT = "timeout";
        static final String APPLICATION_ID = "applicationId";
        static final String IP_MATCH = "ipMatch";
        static final String URL_MATCH = "urlMatch";
    }

    @Before
    public void setup()
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void testIAEIfNullContext()
    {
        final TrustedApplicationSyntacticValidator validator = new TrustedApplicationSyntacticValidator();
        try
        {
            validator.validate(null, new MockI18nHelper(), "http://jira.atlassian.com");
            fail("IAE Expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNPEIfNullI18nHelperInRequestValidate()
    {
        final TrustedApplicationSyntacticValidator validator = new TrustedApplicationSyntacticValidator();
        try
        {
            validator.validate(getContext(), null, "http://jira.atlassian.com");
            fail("IAE Expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNPEIfNullI18nHelperInEditValidate()
    {
        final TrustedApplicationValidator validator = new TrustedApplicationSyntacticValidator();
        try
        {
            validator.validate(getContext(), null, new TrustedApplicationBuilder().toSimple());
            fail("IAE Expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testFailIfNullUrl()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationSyntacticValidator validator = new TrustedApplicationSyntacticValidator();
        assertFalse(validator.validate(context, new MockI18nHelper(), (String) null));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        final String msg = (String) context.getErrorCollection().getErrors().get(Fields.BASE_URL);
        assertNotNull(msg);
    }

    @Test
    public void testFailIfEmptyUrl()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationSyntacticValidator validator = new TrustedApplicationSyntacticValidator();
        assertFalse(validator.validate(context, new MockI18nHelper(), ""));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        final String msg = (String) context.getErrorCollection().getErrors().get(Fields.BASE_URL);
        assertNotNull(msg);
    }

    @Test
    public void testFailIfUrlSchemeUnknown()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationSyntacticValidator validator = new TrustedApplicationSyntacticValidator();
        assertFalse(validator.validate(context, new MockI18nHelper(), "htt3p://some.url"));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        final String msg = (String) context.getErrorCollection().getErrors().get(Fields.BASE_URL);
        assertNotNull(msg);
        assertTrue(msg, msg.toLowerCase().indexOf("unknown protocol") > -1);
    }

    @Test
    public void testFailIfExtraWhitespace()
    {
        JiraServiceContext context = getContext();
        final TrustedApplicationSyntacticValidator validator = new TrustedApplicationSyntacticValidator();
        assertFalse(validator.validate(context, new MockI18nHelper(), "  http://some.url"));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        String msg = (String) context.getErrorCollection().getErrors().get(Fields.BASE_URL);
        assertNotNull(msg);
        assertTrue(msg, msg.indexOf("Illegal character in scheme name") > -1);

        context = getContext();
        assertFalse(validator.validate(context, new MockI18nHelper(), "http://some.url   "));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        msg = (String) context.getErrorCollection().getErrors().get(Fields.BASE_URL);
        assertNotNull(msg);
        assertTrue(msg, msg.indexOf("Illegal character in authority") > -1);
    }

    @Test
    public void testPassIfUrlSchemeKnown()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationSyntacticValidator validator = new TrustedApplicationSyntacticValidator();
        assertTrue(validator.validate(context, new MockI18nHelper(), "http://some.url"));
        assertFalse(context.getErrorCollection().hasAnyErrors());
        assertEquals(0, context.getErrorCollection().getErrors().size());
        assertEquals(0, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testFailIfEmptyApplicationID()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationValidator validator = new TrustedApplicationSyntacticValidator();
        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setName("application.name").setPublicKey("public key").setTimeout(1000).setIpMatch("127.0.0.1").setUrlMatch("/some/url");
        assertFalse(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertNotNull(context.getErrorCollection().getErrors().get(Fields.APPLICATION_ID));
    }

    @Test
    public void testFailIfEmptyName()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationValidator validator = new TrustedApplicationSyntacticValidator();
        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setApplicationId("application.Id").setPublicKey("public key").setTimeout(1000).setIpMatch("127.0.0.1").setUrlMatch("/some/url");
        assertFalse(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertNotNull(context.getErrorCollection().getErrors().get(Fields.NAME));
    }

    @Test
    public void testFailIfZeroTimeout()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationValidator validator = new TrustedApplicationSyntacticValidator();
        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setApplicationId("applicationId").setName("name").setPublicKey("public key").setIpMatch("127.0.0.1").setUrlMatch("/some/url");
        assertFalse(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertNotNull(context.getErrorCollection().getErrors().get(Fields.TIMEOUT));
    }

    @Test
    public void testFailIfNoPublicKey()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationValidator validator = new TrustedApplicationSyntacticValidator();
        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setApplicationId("applicationId").setName("name").setTimeout(1234).setIpMatch("127.0.0.1").setUrlMatch("/some/url");
        assertFalse(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(0, context.getErrorCollection().getErrors().size());
        assertEquals(1, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testFailIfIllegalIpMatch()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationValidator validator = new TrustedApplicationSyntacticValidator();
        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setApplicationId("applicationId").setName("name").setTimeout(1234).setPublicKey("ssomekey").setIpMatch("bad").setUrlMatch("/some/url");
        assertFalse(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertNotNull(context.getErrorCollection().getErrors().get(Fields.IP_MATCH));
    }

    @Test
    public void testFailIfEmptyIpMatch()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationValidator validator = new TrustedApplicationSyntacticValidator();
        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setApplicationId("applicationId").setName("name").setTimeout(1234).setPublicKey("ssomekey").setIpMatch("").setUrlMatch("/some/url");
        assertFalse(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertNotNull(context.getErrorCollection().getErrors().get(Fields.IP_MATCH));
    }

    @Test
    public void testDontFailIfGoodIpMatch()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationValidator validator = new TrustedApplicationSyntacticValidator();
        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setApplicationId("applicationId").setName("name").setTimeout(1234).setPublicKey("ssomekey").setIpMatch(
            "192.168.0.12\n192.168.0.13\n192.168.0.*").setUrlMatch("/some/url");
        assertTrue(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertFalse(context.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testFailIfEmptyUrlMatch()
    {
        final JiraServiceContext context = getContext();
        final TrustedApplicationValidator validator = new TrustedApplicationSyntacticValidator();
        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setApplicationId("applicationId").setName("name").setTimeout(1234).setPublicKey("ssomekey").setIpMatch("127.0.0.1").setUrlMatch("");
        assertFalse(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertNotNull(context.getErrorCollection().getErrors().get(Fields.URL_MATCH));
    }

    private JiraServiceContextImpl getContext()
    {
        return new JiraServiceContextImpl(new MockUser("name"));
    }
}
