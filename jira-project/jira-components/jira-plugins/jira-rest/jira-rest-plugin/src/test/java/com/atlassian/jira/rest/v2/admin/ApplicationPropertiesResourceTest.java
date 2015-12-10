package com.atlassian.jira.rest.v2.admin;

import com.atlassian.jira.bc.admin.ApplicationPropertiesService;
import com.atlassian.jira.bc.admin.ApplicationProperty;
import com.atlassian.jira.bc.admin.ApplicationPropertyMetadata;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.validation.Success;
import com.atlassian.validation.Validated;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpStatus;

import javax.ws.rs.core.Response;
import java.util.ArrayList;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

/**
 * Test for {@link ApplicationPropertiesResource}.
 *
 * @since v4.4
 */
public class ApplicationPropertiesResourceTest extends TestCase
{
    public void testGetApplicationProperty()
    {
        JiraAuthenticationContext authContext = createMock(JiraAuthenticationContext.class);
        MockUser user = new MockUser("user");
        expect(authContext.getLoggedInUser()).andReturn(user).anyTimes();
        expect(authContext.getI18nHelper()).andReturn(new MockI18nHelper()).anyTimes();

        PermissionManager permManager = createMock(PermissionManager.class);
        expect(permManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andReturn(true);

        ApplicationPropertiesService service = createMock(ApplicationPropertiesService.class);
        ApplicationProperty foobar = applicationProperty("foo", "bar");
        expect(service.getApplicationProperty("foo")).andReturn(foobar);

        replay(authContext, permManager, service);

        ApplicationPropertiesResource resource = new ApplicationPropertiesResource(authContext, permManager, service);
        Response response = resource.getProperty("foo","SYSADMIN_ONLY",null);

        assertEquals(HttpStatus.SC_OK, response.getStatus());
        ApplicationPropertiesResource.Property property = (ApplicationPropertiesResource.Property) response.getEntity();
        assertEquals("foo", property.getKey());
        assertEquals("bar", property.getValue());
        verify(authContext, permManager, service);
    }

    public void testGetApplicationProperies()
    {
        JiraAuthenticationContext authContext = createMock(JiraAuthenticationContext.class);
        MockUser user = new MockUser("user");
        expect(authContext.getLoggedInUser()).andReturn(user).anyTimes();
        expect(authContext.getI18nHelper()).andReturn(new MockI18nHelper()).anyTimes();

        PermissionManager permManager = createMock(PermissionManager.class);

        ApplicationPropertiesService service = createMock(ApplicationPropertiesService.class);
        ArrayList<ApplicationProperty> props = new ArrayList<ApplicationProperty>();
        props.add(applicationProperty("foo", "bar"));
        props.add(applicationProperty("fred", "wilma"));
        props.add(applicationProperty("barney", "betty"));
        expect(service.hasPermissionForLevel("SYSADMIN_ONLY")).andReturn(true);
        expect(service.getEditableApplicationProperties("SYSADMIN_ONLY",null)).andReturn(props);

        replay(authContext, permManager, service);

        ApplicationPropertiesResource resource = new ApplicationPropertiesResource(authContext, permManager, service);
        Response response = resource.getProperty(null,"SYSADMIN_ONLY",null);

        assertEquals(HttpStatus.SC_OK, response.getStatus());
        ArrayList<ApplicationPropertiesResource.Property> properties
                = (ArrayList<ApplicationPropertiesResource.Property>) response.getEntity();
        assertEquals("foo", properties.get(0).getKey());
        assertEquals("bar", properties.get(0).getValue());
        assertEquals("fred", properties.get(1).getKey());
        assertEquals("wilma", properties.get(1).getValue());
        assertEquals("barney", properties.get(2).getKey());
        assertEquals("betty", properties.get(2).getValue());
        verify(authContext, permManager, service);
    }

    public void testSetApplicationProperty()
    {
        JiraAuthenticationContext authContext = createMock(JiraAuthenticationContext.class);
        MockUser user = new MockUser("user");
        expect(authContext.getLoggedInUser()).andReturn(user).anyTimes();
        expect(authContext.getI18nHelper()).andReturn(new MockI18nHelper()).anyTimes();

        PermissionManager permManager = createMock(PermissionManager.class);
        expect(permManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andReturn(true);

        ApplicationPropertiesService service = createMock(ApplicationPropertiesService.class);
        ApplicationProperty updatedProp = applicationProperty("foo", "baz");
        Validated<ApplicationProperty> updated = new Validated<ApplicationProperty>(new Success("baz"), updatedProp);
        expect(service.getApplicationProperty("foo")).andReturn(updatedProp);
        expect(service.setApplicationProperty("foo", "baz")).andReturn(updated);

        replay(authContext, permManager, service);

        ApplicationPropertiesResource resource = new ApplicationPropertiesResource(authContext, permManager, service);
        Response response = resource.setProperty("foo", "baz");

        assertEquals(HttpStatus.SC_OK, response.getStatus());
        ApplicationPropertiesResource.Property property = (ApplicationPropertiesResource.Property) response.getEntity();
        assertEquals("foo", property.getKey());
        assertEquals("baz", property.getValue());
        verify(authContext, permManager, service);
    }

    private static ApplicationProperty applicationProperty(String key, String value)
    {
        ApplicationPropertyMetadata md = new ApplicationPropertyMetadata.Builder()
                .key(key)
                .type("string")
                .defaultValue(key + "default")
                .sysAdminEditable(true)
                .requiresRestart(false)
                .build();
        return new ApplicationProperty(md, value);
    }


}
