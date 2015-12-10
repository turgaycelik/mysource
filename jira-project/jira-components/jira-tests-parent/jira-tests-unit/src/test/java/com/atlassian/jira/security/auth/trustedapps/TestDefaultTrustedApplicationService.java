package com.atlassian.jira.security.auth.trustedapps;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultTrustedApplicationService
{
    @Test
    public void testNullManager()
    {
        try
        {
            new DefaultTrustedApplicationService(null, new Checker(true), new MockValidator());
            fail("IAE Expected");
        }
        catch (final IllegalArgumentException e)
        {
            // yay
        }
    }

    @Test
    public void testNullChecker()
    {
        try
        {
            new DefaultTrustedApplicationService(new MockTrustedApplicationManager(),
                (DefaultTrustedApplicationService.PermissionCheck) null, new MockValidator());
            fail("IAE Expected");
        }
        catch (final IllegalArgumentException e)
        {
            // yay
        }
    }

    @Test
    public void testNullValidator()
    {
        try
        {
            new DefaultTrustedApplicationService(new MockTrustedApplicationManager(), new Checker(true), null);
            fail("IAE Expected");
        }
        catch (final IllegalArgumentException e)
        {
            // yay
        }
    }

    @Test
    public void testGetAllAllowed()
    {
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(true), new MockValidator());

        final Context serviceContext = new Context("fred");
        final Set all = service.getAll(serviceContext);
        assertNotNull(all);
        assertEquals(3, all.size());
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        for (final Object anAll : all)
        {
            assertTrue(anAll instanceof TrustedApplicationInfo);
        }
    }

    @Test
    public void testGetAllNotAllowed()
    {
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(false), new MockValidator());

        final Context serviceContext = new Context("fred");
        final Set all = service.getAll(serviceContext);
        assertNotNull(all);
        assertEquals(0, all.size());
        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testGetByIDAllowed()
    {
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(true), new MockValidator());

        final Context serviceContext = new Context("fred");
        final TrustedApplicationInfo info = service.get(serviceContext, 1);
        assertNotNull(info);
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testGetByIDNotAllowed()
    {
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(false), new MockValidator());

        final Context serviceContext = new Context("fred");
        final TrustedApplicationInfo info = service.get(serviceContext, 1);
        assertNull(info);
        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testGetByApplicationIdAllowed()
    {
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(true), new MockValidator());

        final Context serviceContext = new Context("fred");
        final TrustedApplicationInfo info = service.get(serviceContext, "CONF");
        assertNotNull(info);
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testGetByApplicationIdNotAllowed()
    {
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(false), new MockValidator());

        final Context serviceContext = new Context("fred");
        final TrustedApplicationInfo info = service.get(serviceContext, "BAM");
        assertNull(info);
        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testDeleteAllowed()
    {
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(true), new MockValidator());

        final Context serviceContext = new Context("fred");

        assertEquals(3, service.getAll(serviceContext).size());
        assertTrue(service.delete(serviceContext, 1));
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        assertEquals(2, service.getAll(serviceContext).size());
    }

    @Test
    public void testDeleteNotAllowed()
    {
        final AtomicBoolean allow = new AtomicBoolean(false);

        final DefaultTrustedApplicationService.PermissionCheck check = new DefaultTrustedApplicationService.PermissionCheck()
        {
            public boolean check(final User user)
            {
                return allow.get();
            }
        };
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()), check,
            new MockValidator());

        final Context serviceContext = new Context("fred");

        allow.set(true);
        assertEquals(3, service.getAll(serviceContext).size());
        allow.set(false);
        assertFalse(service.delete(serviceContext, 1));
        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
        allow.set(true);
        assertEquals(3, service.getAll(serviceContext).size());
    }

    @Test
    public void testDeleteNonExistentId()
    {
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(true), new MockValidator());

        final Context serviceContext = new Context("fred");

        assertEquals(3, service.getAll(serviceContext).size());
        assertFalse(service.delete(serviceContext, 99));
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        assertEquals(3, service.getAll(serviceContext).size());
    }

    @Test
    public void testStoreAllowed()
    {
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(true), new MockValidator());

        final Context serviceContext = new Context("fred");

        assertEquals(3, service.getAll(serviceContext).size());
        final MockTrustedApplicationInfo oldInfo = new MockTrustedApplicationInfo(0, "FISH", "fisheye", 16000);
        final TrustedApplicationInfo newInfo = service.store(serviceContext, oldInfo);
        assertNotNull(newInfo);
        assertEquals(oldInfo.getID(), newInfo.getID());
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        assertEquals(4, service.getAll(serviceContext).size());
    }

    @Test
    public void testStoreNotAllowed()
    {
        final AtomicBoolean allow = new AtomicBoolean(false);

        final DefaultTrustedApplicationService.PermissionCheck check = new DefaultTrustedApplicationService.PermissionCheck()
        {
            public boolean check(final User user)
            {
                return allow.get();
            }
        };
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()), check,
            new MockValidator());

        final Context serviceContext = new Context("fred");

        allow.set(true);
        assertEquals(3, service.getAll(serviceContext).size());
        allow.set(false);

        final MockTrustedApplicationInfo oldInfo = new MockTrustedApplicationInfo(0, "FISH", "fisheye", 16000);
        final TrustedApplicationInfo newInfo = service.store(serviceContext, oldInfo);
        assertNull(newInfo);
        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());

        allow.set(true);
        assertEquals(3, service.getAll(serviceContext).size());
    }

    @Test
    public void testStoreThrowsIfNotValid()
    {
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(true), new MockValidator(false));

        final Context serviceContext = new Context("fred");

        assertEquals(3, service.getAll(serviceContext).size());
        final MockTrustedApplicationInfo oldInfo = new MockTrustedApplicationInfo(0, "FISH", "fisheye", 16000);
        try
        {
            service.store(serviceContext, oldInfo);
            fail("IllegalArg expected");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        assertEquals(3, service.getAll(serviceContext).size());
    }

    @Test
    public void testValidatorCalledAndReturnsTrue()
    {
        final SimpleTrustedApplication application = new TrustedApplicationBuilder().toSimple();
        final Context serviceContext = new Context("fred");
        final MockValidator validator = new MockValidator()
        {
            @Override
            public boolean validate(final JiraServiceContext context, final I18nHelper helper, final SimpleTrustedApplication app)
            {
                assertSame(application, app);
                assertSame(serviceContext, context);
                return super.validate(context, helper, application);
            }
        };
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(true), validator);

        assertTrue(service.validate(serviceContext, application));
        assertEquals(1, validator.calledCount);
    }

    @Test
    public void testValidatorCalledAndReturnsFalse()
    {
        final SimpleTrustedApplication application = new TrustedApplicationBuilder().toSimple();
        final Context serviceContext = new Context("fred");
        final MockValidator validator = new MockValidator(false)
        {
            @Override
            public boolean validate(final JiraServiceContext context, final I18nHelper helper, final SimpleTrustedApplication app)
            {
                assertSame(application, app);
                assertSame(serviceContext, context);
                return super.validate(context, helper, application);
            }
        };
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(true), validator);

        assertFalse(service.validate(serviceContext, application));
        assertFalse(service.validate(serviceContext, application));
        assertFalse(service.validate(serviceContext, application));
        assertEquals(3, validator.calledCount);
    }

    @Test
    public void testValidatorNotCalledIfNoPermission()
    {
        final MockValidator validator = new MockValidator();
        final TrustedApplicationService service = new DefaultTrustedApplicationService(new MockTrustedApplicationManager(getStandardApps()),
            new Checker(false), validator);

        assertFalse(service.validate(new Context("fred"), new TrustedApplicationBuilder().toSimple()));
        assertEquals(0, validator.calledCount);
    }

    private TrustedApplicationInfo[] getStandardApps()
    {
        return new TrustedApplicationInfo[] {
                new MockTrustedApplicationInfo(1, "CONF", "confluence", 1000),
                new MockTrustedApplicationInfo(2, "BAM", "bamboo", 4000),
                new MockTrustedApplicationInfo(3, "CWD", "crowd", 9000)
        };
    }

    private class Checker implements DefaultTrustedApplicationService.PermissionCheck
    {
        private Checker(final boolean result)
        {
            this.result = result;
        }

        private final boolean result;

        public boolean check(final User user)
        {
            return result;
        }
    }

    private static class Context extends MockJiraServiceContext
    {
        private Context(final String name)
        {
            super(new MockUser(name));
        }
    }
}