package com.atlassian.jira.crowd.embedded;

import java.util.HashSet;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ValidationFailureException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.singleton;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestDefaultJaacsService
{
    private Set<RemoteAddress> addressList1;
    private Set<RemoteAddress> addressList2;
    private RemoteAddress address1;
    private RemoteAddress address2;
    private RemoteAddress address3;
    private RemoteAddress address4;
    private RemoteAddress address5;
    private RemoteAddress address6;

    private MockJiraServiceContext mockServiceContext;
    private User user;
    private I18nHelper i18nHelper;
    private I18nHelper.BeanFactory i18nFactory;


    @Before
    public void setup()
    {
        user = new MockUser("testuser");
        mockServiceContext = new MockJiraServiceContext(user);

        i18nHelper = new MockI18nHelper();
        i18nFactory = createMock(I18nHelper.BeanFactory.class);
        expect(i18nFactory.getInstance(user)).andReturn(i18nHelper).anyTimes();
        replay(i18nFactory);

        address1 = new RemoteAddress("10.10.8.22");
        address2 = new RemoteAddress("10.10.10.11");
        address3 = new RemoteAddress("117.21.21.145");
        address4 = new RemoteAddress("127.0.0.1");
        address5 = new RemoteAddress("[fe80::213:2ff:fe57:43fd]");
        address6 = new RemoteAddress("bad address");

        addressList1 = new HashSet<RemoteAddress>();
        addressList1.add(address1);
        addressList1.add(address2);
        addressList1.add(address3);

        addressList2 = new HashSet<RemoteAddress>();
        addressList2.add(address3);
        addressList2.add(address4);
        addressList2.add(address5);
        addressList2.add(address6);
    }

    @Test
    public void testGetRemoteAddresses() throws Exception
    {

        Application app = EasyMock.createMock(Application.class);
        EasyMock.expect(app.getRemoteAddresses()).andReturn(addressList1);
        ApplicationManager applicationManager = EasyMock.createMock(ApplicationManager.class);
        EasyMock.expect(applicationManager.findById(1)).andReturn(app);
        EasyMock.replay(applicationManager, app);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(applicationManager, null, null);
        Set<RemoteAddress> list = defaultJaacsService.getRemoteAddresses(null, 1);

        assertNotNull(list);
        assertEquals(3, list.size());
        assertTrue(list.contains(address1));
        assertTrue(list.contains(address2));
        assertTrue(list.contains(address3));

        EasyMock.verify(app, applicationManager);
    }

    @Test
    public void testValidateAddRemoteAddress_ip4() throws Exception
    {
        Application app = EasyMock.createMock(Application.class);
        EasyMock.expect(app.getRemoteAddresses()).andReturn(addressList1);
        ApplicationManager applicationManager = EasyMock.createMock(ApplicationManager.class);
        EasyMock.expect(applicationManager.findById(1)).andReturn(app);

        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(applicationManager, app, permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(applicationManager, permissionManager, null);

        assertTrue(defaultJaacsService.validateAddRemoteAddress(mockServiceContext, "10.10.10.10", 1));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testValidateAddRemoteAddress_ip6() throws Exception
    {
        Application app = EasyMock.createMock(Application.class);
        EasyMock.expect(app.getRemoteAddresses()).andReturn(addressList1);
        ApplicationManager applicationManager = EasyMock.createMock(ApplicationManager.class);
        EasyMock.expect(applicationManager.findById(1)).andReturn(app);

        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(applicationManager, app, permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(applicationManager, permissionManager, null);

        assertTrue(defaultJaacsService.validateAddRemoteAddress(mockServiceContext, "[fe80::213:2ff:fe57:43fd]", 1));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testValidateAddRemoteAddress_nullIP() throws Exception
    {
        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(null, permissionManager, null);

        assertFalse(defaultJaacsService.validateAddRemoteAddress(mockServiceContext, null, 1));
        // Check the error
        ErrorCollection errorCollection = mockServiceContext.getErrorCollection();
        assertEquals("An address is required.", errorCollection.getErrors().get("remoteAddresses"));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testValidateAddRemoteAddress_emptyIP() throws Exception
    {
        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(null, permissionManager, null);

        assertFalse(defaultJaacsService.validateAddRemoteAddress(mockServiceContext, "", 1));
        // Check the error
        ErrorCollection errorCollection = mockServiceContext.getErrorCollection();
        assertEquals("An address is required.", errorCollection.getErrors().get("remoteAddresses"));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testValidateAddRemoteAddress_invalidIP() throws Exception
    {
        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(null, permissionManager, null);

        assertFalse(defaultJaacsService.validateAddRemoteAddress(mockServiceContext, "bad address", 1));
        // Check the error
        ErrorCollection errorCollection = mockServiceContext.getErrorCollection();
        assertEquals("'bad address' is not a valid IP address.", errorCollection.getErrors().get("remoteAddresses"));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testValidateAddRemoteAddress_notPermitted() throws Exception
    {
        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);

        EasyMock.replay(permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(null, permissionManager, null);

        assertFalse(defaultJaacsService.validateAddRemoteAddress(mockServiceContext, null, 1));
        // Check the error
        ErrorCollection errorCollection = mockServiceContext.getErrorCollection();
        assertTrue(errorCollection.getErrorMessages().contains("You must be a JIRA Administrator to configure JIRA as a Crowd Server."));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testAddRemoteAddress() throws Exception
    {
        Application app = EasyMock.createMock(Application.class);
        EasyMock.expect(app.getRemoteAddresses()).andReturn(addressList1);
        ApplicationManager applicationManager = EasyMock.createMock(ApplicationManager.class);
        EasyMock.expect(applicationManager.findById(1)).andReturn(app).times(2);
        applicationManager.addRemoteAddress(app, new RemoteAddress("10.10.10.10"));

        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(app, applicationManager, permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(applicationManager, permissionManager, null);

        assertTrue(defaultJaacsService.addRemoteAddress(mockServiceContext, "10.10.10.10", 1));
        EasyMock.verify(app, applicationManager, permissionManager);
    }

    @Test
    public void testAddRemoteAddress_validationFail() throws Exception
    {
        Application app = EasyMock.createMock(Application.class);
        ApplicationManager applicationManager = EasyMock.createMock(ApplicationManager.class);

        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);

        EasyMock.replay(app, applicationManager, permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(applicationManager, permissionManager, null);

        assertFalse(defaultJaacsService.addRemoteAddress(mockServiceContext, "10.10.10.10", 1));
        // Check the error
        ErrorCollection errorCollection = mockServiceContext.getErrorCollection();
        assertTrue(errorCollection.getErrorMessages().contains("You must be a JIRA Administrator to configure JIRA as a Crowd Server."));
        EasyMock.verify(app, applicationManager, permissionManager);
    }

    @Test
    public void testValidateDeleteApplication() throws Exception
    {
        Application app = EasyMock.createMock(Application.class);
        ApplicationManager applicationManager = EasyMock.createMock(ApplicationManager.class);
        EasyMock.expect(applicationManager.findById(1)).andReturn(app);

        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(applicationManager, app, permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(applicationManager, permissionManager, null);

        assertTrue(defaultJaacsService.validateDeleteApplication(mockServiceContext, 1));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testValidateDeleteApplication_invalidApp() throws Exception
    {
        // We can delete invalid addresses.
        Application app = EasyMock.createMock(Application.class);
        ApplicationManager applicationManager = EasyMock.createMock(ApplicationManager.class);
        EasyMock.expect(applicationManager.findById(1)).andThrow(new ApplicationNotFoundException(1L));

        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(applicationManager, app, permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(applicationManager, permissionManager, null);

        assertFalse(defaultJaacsService.validateDeleteApplication(mockServiceContext, 1));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testValidateDeleteRemoteAddress_notPermitted() throws Exception
    {
        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);

        EasyMock.replay(permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(null, permissionManager, null);

        assertFalse(defaultJaacsService.validateDeleteApplication(mockServiceContext, 1));
        // Check the error
        ErrorCollection errorCollection = mockServiceContext.getErrorCollection();
        assertTrue(errorCollection.getErrorMessages().contains("You must be a JIRA Administrator to configure JIRA as a Crowd Server."));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testDeleteApplication() throws Exception
    {
        Application app = EasyMock.createMock(Application.class);
        ApplicationManager applicationManager = EasyMock.createMock(ApplicationManager.class);
        EasyMock.expect(applicationManager.findById(1)).andReturn(app).times(2);
        applicationManager.remove(app);

        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(app, applicationManager, permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(applicationManager, permissionManager, null);

        assertTrue(defaultJaacsService.deleteApplication(mockServiceContext, 1));
        EasyMock.verify(app, applicationManager, permissionManager);
    }

    @Test
    public void testDeleteRemoteAddress_notPermitted() throws Exception
    {
        Application app = EasyMock.createMock(Application.class);
        ApplicationManager applicationManager = EasyMock.createMock(ApplicationManager.class);

        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);

        EasyMock.replay(app, applicationManager, permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(applicationManager, permissionManager, null);

        assertFalse(defaultJaacsService.deleteApplication(mockServiceContext, 1));
        // Check the error
        ErrorCollection errorCollection = mockServiceContext.getErrorCollection();
        assertTrue(errorCollection.getErrorMessages().contains("You must be a JIRA Administrator to configure JIRA as a Crowd Server."));
        EasyMock.verify(app, applicationManager, permissionManager);
    }

    @Test
    public void testValidateResetPassword() throws Exception
    {
        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(null, permissionManager, null);

        assertTrue(defaultJaacsService.validateResetPassword(mockServiceContext, "secret", 1));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testValidateResetPassword_nullPass() throws Exception
    {
        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(null, permissionManager, null);

        assertFalse(defaultJaacsService.validateResetPassword(mockServiceContext, null, 1));
        // Check the error
        ErrorCollection errorCollection = mockServiceContext.getErrorCollection();
        assertEquals("A password is required.", errorCollection.getErrors().get("credential"));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testValidateResetPassword_EmptyPass() throws Exception
    {
        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(null, permissionManager, null);

        assertFalse(defaultJaacsService.validateResetPassword(mockServiceContext, "", 1));
        // Check the error
        ErrorCollection errorCollection = mockServiceContext.getErrorCollection();
        assertEquals("A password is required.", errorCollection.getErrors().get("credential"));
        EasyMock.verify(permissionManager);
    }


    @Test
    public void testValidateResetPassword_notPermitted() throws Exception
    {
        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);

        EasyMock.replay(permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(null, permissionManager, null);

        assertFalse(defaultJaacsService.validateResetPassword(mockServiceContext, null, 1));
        // Check the error
        ErrorCollection errorCollection = mockServiceContext.getErrorCollection();
        assertTrue(errorCollection.getErrorMessages().contains("You must be a JIRA Administrator to configure JIRA as a Crowd Server."));
        EasyMock.verify(permissionManager);
    }

    @Test
    public void testResetPassword() throws Exception
    {
        Application app = EasyMock.createMock(Application.class);
        ApplicationManager applicationManager = EasyMock.createMock(ApplicationManager.class);
        EasyMock.expect(applicationManager.findById(1)).andReturn(app);
        applicationManager.updateCredential(app, new PasswordCredential("secret"));

        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        EasyMock.replay(app, applicationManager, permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(applicationManager, permissionManager, null);

        assertTrue(defaultJaacsService.resetPassword(mockServiceContext, "secret", 1));
        EasyMock.verify(app, applicationManager, permissionManager);
    }

    @Test
    public void testResetPassword_notPermitted() throws Exception
    {
        Application app = EasyMock.createMock(Application.class);
        ApplicationManager applicationManager = EasyMock.createMock(ApplicationManager.class);

        PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);

        EasyMock.replay(app, applicationManager, permissionManager);

        DefaultJaacsService defaultJaacsService = new DefaultJaacsService(applicationManager, permissionManager, null);

        assertFalse(defaultJaacsService.resetPassword(mockServiceContext, "secret", 1));
        EasyMock.verify(app, applicationManager, permissionManager);
    }

    @Test
    public void findAllShouldThrowAValidationExceptionIfUserHasNoAdminAccess() throws Exception
    {
        PermissionManager permissionManager = createMock(PermissionManager.class);
        expect(permissionManager.hasPermission(anyInt(), EasyMock.eq(user))).andReturn(false).anyTimes();
        replay(permissionManager);

        ApplicationManager applicationManager = createMock(ApplicationManager.class);
        replay(applicationManager);

        try
        {
            new DefaultJaacsService(null, permissionManager, i18nFactory).findAll(user);
        }
        catch (ValidationFailureException e)
        {
            assertThat(e.errors().getErrorMessages(), hasItem("admin.jaacs.application.admin.required"));
        }
    }

    @Test
    public void findByIdShouldThrowAValidationExceptionIfUserHasNoAdminAccess() throws Exception
    {
        PermissionManager permissionManager = createMock(PermissionManager.class);
        expect(permissionManager.hasPermission(anyInt(), EasyMock.eq(user))).andReturn(false).anyTimes();
        replay(permissionManager);

        ApplicationManager applicationManager = createMock(ApplicationManager.class);
        replay(applicationManager);

        try
        {
            new DefaultJaacsService(null, permissionManager, i18nFactory).findById(user, 1L);
        }
        catch (ValidationFailureException e)
        {
            assertThat(e.errors().getErrorMessages(), hasItem("admin.jaacs.application.admin.required"));
        }
    }

    @Test
    public void createShouldThrowAValidationExceptionIfUserHasNoAdminAccess() throws Exception
    {
        PermissionManager permissionManager = createMock(PermissionManager.class);
        expect(permissionManager.hasPermission(anyInt(), EasyMock.eq(user))).andReturn(false).anyTimes();
        replay(permissionManager);

        try
        {
            new DefaultJaacsService(null, permissionManager, i18nFactory).create(user, null);
        }
        catch (ValidationFailureException e)
        {
            assertThat(e.errors().getErrorMessages(), hasItem("admin.jaacs.application.admin.required"));
        }
    }

    @Test
    public void updateShouldThrowAValidationExceptionIfUserHasNoAdminAccess() throws Exception
    {
        PermissionManager permissionManager = createMock(PermissionManager.class);
        expect(permissionManager.hasPermission(anyInt(), EasyMock.eq(user))).andReturn(false).anyTimes();
        replay(permissionManager);

        try
        {
            new DefaultJaacsService(null, permissionManager, i18nFactory).update(user, null);
        }
        catch (ValidationFailureException e)
        {
            assertThat(e.errors().getErrorMessages(), hasItem("admin.jaacs.application.admin.required"));
        }
    }

    @Test
    public void validateApplicationShouldCheckNameAndCredentialAndRemoteAddresses() throws Exception
    {
        Application app = createMock(Application.class);
        expect(app.getName()).andReturn("").anyTimes();
        expect(app.getCredential()).andReturn(new PasswordCredential("")).anyTimes();
        expect(app.getRemoteAddresses()).andReturn(singleton(new RemoteAddress("foo"))).anyTimes();
        replay(app);

        try
        {
            new DefaultJaacsService(null, null, i18nFactory).validateApplication(user, app);
            fail("Validation should not pass for app: " + app);
        }
        catch (ValidationFailureException e)
        {
            assertThat(e.errors().getErrors().get("name"), equalTo("admin.jaacs.application.remote.application.name.required"));
            assertThat(e.errors().getErrors().get("credential"), equalTo("admin.jaacs.application.password.empty"));
            assertThat(e.errors().getErrors().get("remoteAddresses"), equalTo("admin.jaacs.application.remote.address.invalid.ip [foo]"));
        }
    }
}
