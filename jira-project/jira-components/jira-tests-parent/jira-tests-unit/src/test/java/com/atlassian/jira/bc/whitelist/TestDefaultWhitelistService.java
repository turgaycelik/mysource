package com.atlassian.jira.bc.whitelist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultWhitelistService
{
    private User admin;
    private PermissionManager permissionManager;
    private I18nHelper.BeanFactory beanFactory;

    @Before
    public void setUp() throws Exception
    {
        admin = new MockUser("admin");

        permissionManager = createMock(PermissionManager.class);
        beanFactory = createMock(I18nHelper.BeanFactory.class);
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void testCheckInvalidPermissions()
    {
        expect(permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (com.atlassian.crowd.embedded.api.User) admin)).andReturn(true);

        replay(permissionManager, beanFactory);

        DefaultWhitelistService whitelistService = new DefaultWhitelistService(permissionManager, null, beanFactory);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(whitelistService.checkInvalidPermissions(new JiraServiceContextImpl(admin, errorCollection)));
        assertFalse(errorCollection.hasAnyErrors());

        verify(permissionManager, beanFactory);
    }

    @Test
    public void testCheckInvalidPermissionsNonSysadmin()
    {
        expect(permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (com.atlassian.crowd.embedded.api.User) admin)).andReturn(false);
        expect(beanFactory.getInstance((com.atlassian.crowd.embedded.api.User)admin)).andReturn(new MockI18nHelper());

        replay(permissionManager, beanFactory);

        DefaultWhitelistService whitelistService = new DefaultWhitelistService(permissionManager, null, beanFactory);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        assertTrue(whitelistService.checkInvalidPermissions(new JiraServiceContextImpl(admin, errorCollection)));
        assertTrue(errorCollection.hasAnyErrors());

        verify(permissionManager, beanFactory);
    }

    @Test
    public void testMethodsCheckPermissions()
    {
        final DefaultWhitelistService whitelistService = new DefaultWhitelistService(null, null, null)
        {
            @Override
            boolean checkInvalidPermissions(JiraServiceContext context)
            {
                context.getErrorCollection().addErrorMessage("FAILED PERMISSION CHECK!");
                return true;
            }
        };

        WhitelistService.WhitelistResult result = whitelistService.getRules(getContext());
        assertFalse(result.isValid());

        final WhitelistService.WhitelistUpdateValidationResult validationResult = whitelistService.validateUpdateRules(getContext(), Collections.<String>emptyList(), false);
        assertFalse(validationResult.isValid());

        try
        {
            whitelistService.updateRules(validationResult);
            fail("should have thrown exception!");
        }
        catch (Exception e)
        {
            //yay!
        }
    }

    @Test
    public void testValidPatterns()
    {
        expect(permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (com.atlassian.crowd.embedded.api.User) admin)).andReturn(true);

        replay(permissionManager);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        JiraServiceContext context = new JiraServiceContextImpl(admin, errorCollection);

        WhitelistService whitelistService = new DefaultWhitelistService(permissionManager, null, null);

        List<String> rules = new ArrayList<String>();
        rules.add("a series of innocent looking words");
        rules.add("/a [f]ancy reg(e|e)x");
        rules.add("a [f]ancy reg(e|e)x looking thing that actually has to be escaped");
        rules.add("=http://www.zombo.com");
        rules.add("http[s://hosting.gmodules.com/ig/gadgets/file/112581010116074801021/hamster.xml");
        rules.add("/http://hosting.gmodules.com/ig/gadgets/file/112581010116074801021/hamster.xml");

        WhitelistService.WhitelistUpdateValidationResult whitelistUpdateValidationResult;
        whitelistUpdateValidationResult= whitelistService.validateUpdateRules(context, rules, false);

        assertTrue(whitelistUpdateValidationResult.isValid());
        assertFalse(errorCollection.hasAnyErrors());

        verify(permissionManager);
    }

    @Test
    public void testInvalidPatterns ()
    {
        expect(permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (com.atlassian.crowd.embedded.api.User) admin)).andReturn(true);
        expect(beanFactory.getInstance((com.atlassian.crowd.embedded.api.User)admin)).andReturn(new MockI18nHelper());

        replay(permissionManager, beanFactory);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        JiraServiceContext context = new JiraServiceContextImpl(admin, errorCollection);

        WhitelistService whitelistService = new DefaultWhitelistService(permissionManager, null, beanFactory);

        List<String> rules = new ArrayList<String>();
        rules.add("/http[s://hosting.gmodules.com/ig/gadgets/file/112581010116074801021/hamster.xml");

        WhitelistService.WhitelistUpdateValidationResult whitelistUpdateValidationResult;
        whitelistUpdateValidationResult= whitelistService.validateUpdateRules(context, rules, false);

        assertFalse(whitelistUpdateValidationResult.isValid());
        assertTrue(errorCollection.hasAnyErrors());

        verify(permissionManager, beanFactory);
    }

    private JiraServiceContext getContext()
    {
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        return new JiraServiceContextImpl(admin, errorCollection);
    }
}
