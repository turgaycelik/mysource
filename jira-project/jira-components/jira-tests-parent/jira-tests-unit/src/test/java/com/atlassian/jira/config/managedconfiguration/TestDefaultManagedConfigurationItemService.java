package com.atlassian.jira.config.managedconfiguration;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.plugin.PluginAccessor;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestDefaultManagedConfigurationItemService
{
    @Mock
    ManagedConfigurationItemStore managedConfigurationItemStore;
    @Mock PermissionManager permissionManager;
    @Mock PluginAccessor pluginAccessor;

    @Test
    public void testDoesUserHavePermission()
    {
        // anonymous user should never have permission
        assertThat(service().doesUserHavePermission(null, new ManagedConfigurationItemBuilder().setManaged(false).build()), Matchers.equalTo(false));
        assertThat(service().doesUserHavePermission(null, createEntity(ConfigurationItemAccessLevel.ADMIN)), Matchers.equalTo(false));
        assertThat(service().doesUserHavePermission(null, createEntity(ConfigurationItemAccessLevel.SYS_ADMIN)), Matchers.equalTo(false));
        assertThat(service().doesUserHavePermission(null, createEntity(ConfigurationItemAccessLevel.LOCKED)), Matchers.equalTo(false));
        
        // set up admin / sysadmin users
        User admin = new MockUser("admin"); 
        User sysadmin = new MockUser("sysadmin");
        
        when(permissionManager.hasPermission(Permissions.ADMINISTER, admin))
                .thenReturn(true);
        when(permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, admin))
                .thenReturn(false);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, sysadmin))
                .thenReturn(true);
        when(permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, sysadmin))
                .thenReturn(true);

        // if an item is not managed, then any logged in user will have permission
        assertThat(service().doesUserHavePermission(admin, new ManagedConfigurationItemBuilder().setManaged(false).build()), Matchers.equalTo(true));

        // check permissions
        assertThat(service().doesUserHavePermission(admin, createEntity(ConfigurationItemAccessLevel.ADMIN)), Matchers.equalTo(true));
        assertThat(service().doesUserHavePermission(admin, createEntity(ConfigurationItemAccessLevel.SYS_ADMIN)), Matchers.equalTo(false));
        assertThat(service().doesUserHavePermission(admin, createEntity(ConfigurationItemAccessLevel.LOCKED)), Matchers.equalTo(false));
        
        assertThat(service().doesUserHavePermission(sysadmin, createEntity(ConfigurationItemAccessLevel.ADMIN)), Matchers.equalTo(true));
        assertThat(service().doesUserHavePermission(sysadmin, createEntity(ConfigurationItemAccessLevel.SYS_ADMIN)), Matchers.equalTo(true));
        assertThat(service().doesUserHavePermission(sysadmin, createEntity(ConfigurationItemAccessLevel.LOCKED)), Matchers.equalTo(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoesUserHavePermissionIAE()
    {
        service().doesUserHavePermission(new MockUser("admin"), (ManagedConfigurationItem) null);
    }

    private ManagedConfigurationItem createEntity(final ConfigurationItemAccessLevel configurationItemAccessLevel)
    {
        return new ManagedConfigurationItemBuilder()
                    .setConfigurationItemAccessLevel(configurationItemAccessLevel)
                    .setManaged(true)
                    .build();
    }

    private DefaultManagedConfigurationItemService service()
    {
        return new DefaultManagedConfigurationItemService(managedConfigurationItemStore, permissionManager, pluginAccessor);
    }
}
