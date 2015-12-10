package com.atlassian.jira.web.action.admin.permission;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.permission.PermissionSchemeManager;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

/**
 * @since v6.1
 */
public class TestAddScheme
{

    @Mock
    @AvailableInContainer
    PermissionSchemeManager permissionSchemeManager;

    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Test
    public void getSchemeManagerShouldReturnPermissionSchemeManager()
    {
        AddScheme addScheme = new AddScheme();
        assertSame(addScheme.getSchemeManager(), permissionSchemeManager);
    }

    @Test
    public void getRedirectUrlShouldReturnRedirectUrlForPermissionSchemes(){
        assertThat(new AddScheme().getRedirectURL(), Matchers.startsWith("ViewPermissionSchemes"));
    }
}
