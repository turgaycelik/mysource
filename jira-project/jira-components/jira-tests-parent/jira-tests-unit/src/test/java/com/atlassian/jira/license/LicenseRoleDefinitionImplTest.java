package com.atlassian.jira.license;

import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class LicenseRoleDefinitionImplTest
{
    private final JiraAuthenticationContext ctx =
            MockSimpleAuthenticationContext.createNoopContext(new MockUser("Dr. Who"));

    @Test(expected = IllegalArgumentException.class)
    public void cantBeConstructedWithoutRoleId()
    {
        new LicenseRoleDefinitionImpl(null, "goodArgument", ctx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantBeConstructedWithNullName()
    {
        new LicenseRoleDefinitionImpl(new LicenseRoleId("name"), null, ctx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantBeConstructedWithBlankName()
    {
        new LicenseRoleDefinitionImpl(new LicenseRoleId("name"), " \t\r\n", ctx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantBeConstructedWithNullAuthCtx()
    {
        new LicenseRoleDefinitionImpl(new LicenseRoleId("name"), "name", null);
    }

    @Test
    public void equalsOnlyUsesTheRoleId() throws Exception
    {
        final LicenseRoleId role1 = LicenseRoleId.valueOf("role1");
        final LicenseRoleId role2 = LicenseRoleId.valueOf("role2");

        final LicenseRoleDefinitionImpl def1 = new LicenseRoleDefinitionImpl(role1, "Name 1", ctx);
        final LicenseRoleDefinitionImpl def2 = new LicenseRoleDefinitionImpl(role1, "Name 2", ctx);
        final LicenseRoleDefinitionImpl def3 = new LicenseRoleDefinitionImpl(role2, "Name 1", ctx);

        assertThat(def1, equalTo(def1));
        assertThat(def1, equalTo(def2));
        assertThat(def2, equalTo(def1));
        assertThat(def2, equalTo(def2));

        assertThat(def3, equalTo(def3));
        assertThat(def3, not(equalTo(def1)));
        assertThat(def3, not(equalTo(def2)));
        assertThat(def1, not(equalTo(def3)));
        assertThat(def2, not(equalTo(def3)));
    }

    @Test
    public void hashCodeOnlyUsesTheRoleId() throws Exception
    {
        final LicenseRoleId role1 = LicenseRoleId.valueOf("role1");
        final LicenseRoleDefinitionImpl def1 = new LicenseRoleDefinitionImpl(role1, "Name 1", ctx);
        final LicenseRoleDefinitionImpl def2 = new LicenseRoleDefinitionImpl(role1, "Name 2", ctx);

        assertThat(def1.hashCode(), equalTo(def2.hashCode()));
    }
}