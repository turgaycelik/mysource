package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.MockUser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestTrustedApplicationSemanticValidator
{

    @Before
    public void setUp()
    {
        new MockComponentWorker().init();
    }

    @Test
    public void testNullInCtor()
    {
        try
        {
            new TrustedApplicationSemanticValidator(null);
            fail("IAE expected");
        }
        catch (final IllegalArgumentException yay)
        {
            // expected
        }
    }

    @Test
    public void testValidateApplicationIDAlreadyExists()
    {
        final TrustedApplicationInfo info = new MockTrustedApplicationInfo(1, "appId", "name", 1000);
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(info));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(0, "appId", "name", 1000));
        final JiraServiceContext context = new JiraServiceContextImpl(new MockUser("name"));
        assertFalse(validator.validate(context, new com.atlassian.jira.mock.i18n.MockI18nHelper(), builder.toSimple()));
        assertEquals(1, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testValidateApplicationIDDoesntAlreadyExist()
    {
        final TrustedApplicationInfo info = new MockTrustedApplicationInfo(1, "anAppId", "name", 1000);
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(info));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(0, "appId", "name", 1000));
        final JiraServiceContext context = new JiraServiceContextImpl(new MockUser("name"));
        assertTrue(validator.validate(context, new com.atlassian.jira.mock.i18n.MockI18nHelper(), builder.toSimple()));
        assertEquals(0, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testValidateApplicationIDCantBeFound()
    {
        final TrustedApplicationInfo info = new MockTrustedApplicationInfo(1, "anAppId", "name", 1000);
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(info));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(2, "appId", "name", 1000));
        final JiraServiceContext context = new JiraServiceContextImpl(new MockUser("name"));
        assertFalse(validator.validate(context, new com.atlassian.jira.mock.i18n.MockI18nHelper(), builder.toSimple()));
        assertEquals(1, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testValidateApplicationIDCorrect()
    {
        final TrustedApplicationInfo info = new MockTrustedApplicationInfo(1, "anAppId", "name", 1000);
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(info));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(1, "anAppId", "new name", 1000));
        final JiraServiceContext context = new JiraServiceContextImpl(new MockUser("name"));
        assertTrue(validator.validate(context, new com.atlassian.jira.mock.i18n.MockI18nHelper(), builder.toSimple()));
        assertEquals(0, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testValidateBlankApplicationID()
    {
        final TrustedApplicationInfo info = new MockTrustedApplicationInfo(1, "anAppId", "name", 1000);
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(info));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(0, "", "name", 1000));
        final JiraServiceContext context = new JiraServiceContextImpl(new MockUser("name"));
        assertTrue(validator.validate(context, new com.atlassian.jira.mock.i18n.MockI18nHelper(), builder.toSimple()));
        assertEquals(0, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testValidateBadPublicKey()
    {
        final TrustedApplicationInfo info = new MockTrustedApplicationInfo(1, "anAppId", "name", 1000);
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(info));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(0, "", "name", 1000, "1.1.1.1", "/some/url/", new KeyFactory.InvalidPublicKey(new IllegalArgumentException())));
        final JiraServiceContext context = new JiraServiceContextImpl(new MockUser("name"));
        assertFalse(validator.validate(context, new com.atlassian.jira.mock.i18n.MockI18nHelper(), builder.toSimple()));
        assertEquals(1, context.getErrorCollection().getErrorMessages().size());
    }
}
