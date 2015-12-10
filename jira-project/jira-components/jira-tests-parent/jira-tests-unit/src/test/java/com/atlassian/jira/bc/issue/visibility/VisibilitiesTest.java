package com.atlassian.jira.bc.issue.visibility;

import com.atlassian.jira.issue.fields.rest.json.beans.VisibilityJsonBean;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

public class VisibilitiesTest
{

    @Test
    public void testCreatePublicVisibility() throws Exception
    {
        Visibility visibility = Visibilities.fromGroupAndRoleId((String) null, null);

        assertThat(visibility, Matchers.<Visibility>equalTo(PublicVisibility.INSTANCE));
    }

    @Test
    public void testCreateVisibilityForRole() throws Exception
    {
        Long roleLevelId = 123L;
        Visibility visibility = Visibilities.fromGroupAndRoleId(null, roleLevelId);

        assertThat(visibility, Matchers.<Visibility>equalTo(new RoleVisibility(roleLevelId)));
    }

    @Test
    public void testCreateVisibilityForGroup() throws Exception
    {
        String group = "group";
        Visibility visibility = Visibilities.fromGroupAndRoleId(group, null);

        assertThat(visibility, Matchers.<Visibility>equalTo(new GroupVisibility(group)));
    }

    @Test
    public void testCreateVisibilityDefinedForGroupAndRole() throws Exception
    {
        Visibility visibility = Visibilities.fromGroupAndRoleId("group", 123L);

        assertThat(visibility, Matchers.<Visibility>equalTo(new InvalidVisibility("service.error.visibility")));
    }

    @Test
    public void testCreateGroupVisibilityFromJsonBean() throws Exception
    {
        String group = "group";
        VisibilityJsonBean visibilityJsonBean = new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.group, group);
        Visibility visibility = Visibilities.fromVisibilityBean(visibilityJsonBean, null);

        assertThat(visibility, Matchers.<Visibility>equalTo(new GroupVisibility(group)));
    }

    @Test
    public void testCreateRoleVisibilityFromJsonBean() throws Exception
    {
        Long roleLevelId = 123L;
        String roleLevel = "administrators";
        ProjectRoleManager projectRoleManager = mock(ProjectRoleManager.class);
        ProjectRole projectRole = new ProjectRoleImpl(roleLevelId, roleLevel, "");
        when(projectRoleManager.getProjectRole(eq(roleLevel))).thenReturn(projectRole);
        VisibilityJsonBean visibilityJsonBean = new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.role, roleLevel);
        Visibility visibility = Visibilities.fromVisibilityBean(visibilityJsonBean, projectRoleManager);

        assertThat(visibility, Matchers.<Visibility>equalTo(new RoleVisibility(roleLevelId)));
    }

    @Test
    public void testCreateRoleVisibilityFromJsonBeanWhenRoleDoesNotExist() throws Exception
    {
        String roleLevel = "administrators";
        ProjectRoleManager projectRoleManager = mock(ProjectRoleManager.class);
        VisibilityJsonBean visibilityJsonBean = new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.role, roleLevel);
        Visibility visibility = Visibilities.fromVisibilityBean(visibilityJsonBean, projectRoleManager);

        assertThat(visibility, Matchers.<Visibility>equalTo(new InvalidVisibility("service.error.roledoesnotexist", roleLevel)));
    }

    @Test
    public void testCreateVisibilityWhenRoleIdIsValidString() throws Exception
    {
        Long roleLevelId = 123L;
        Visibility visibility = Visibilities.fromGroupAndStrRoleId(null, roleLevelId.toString());

        assertThat(visibility, Matchers.<Visibility>equalTo(new RoleVisibility(roleLevelId)));
    }

    @Test
    public void testCreateVisibilityWhenRoleIdIsNotNumber() throws Exception
    {
        Visibility visibility = Visibilities.fromGroupAndStrRoleId(null, "abs");

        assertThat(visibility, Matchers.<Visibility>equalTo(new InvalidVisibility("service.error.roleidnotnumber")));
    }

    @Test
    public void testCreateVisibilityWhenRoleIdEmptyString() throws Exception
    {
        Visibility visibility = Visibilities.fromGroupAndStrRoleId(null, "");

        assertThat(visibility, Matchers.<Visibility>equalTo(PublicVisibility.INSTANCE));
    }
}