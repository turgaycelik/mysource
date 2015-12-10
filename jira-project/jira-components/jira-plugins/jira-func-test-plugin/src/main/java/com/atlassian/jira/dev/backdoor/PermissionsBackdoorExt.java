package com.atlassian.jira.dev.backdoor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.fugue.Option;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.security.GlobalPermissionEntry;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.testkit.plugin.PermissionsBackdoor;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import static com.atlassian.jira.testkit.plugin.util.CacheControl.never;

/**
 * Extended PermissionsBackdoor.
 */
@Path ("permissions")
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
@AnonymousAllowed
public class PermissionsBackdoorExt extends PermissionsBackdoor
{
    private final GlobalPermissionManager globalPermissionManager;

    public PermissionsBackdoorExt(GlobalPermissionManager globalPermissionManager)
    {
        super(globalPermissionManager);
        this.globalPermissionManager = globalPermissionManager;
    }

    @GET
    @AnonymousAllowed
    @Path("global/add/key")
    public Response addGlobalPermissionByKey(@QueryParam ("type") String permissionType, @QueryParam ("group") String group)
    {
        GlobalPermissionType globalPermissionType = globalPermissionManager.getGlobalPermission(permissionType).get();
        globalPermissionManager.addPermission(globalPermissionType, group);
        return Response.ok(null).build();
    }


    @GET
    @Path ("global/key")
    public Collection<PermissionBean> getPermissionsByKey(@QueryParam ("type") String permissionType)
    {
        if (permissionType == null) { throw new WebApplicationException(400); }

        return Collections2.transform(globalPermissionManager.getPermissions(GlobalPermissionKey.of(permissionType)), PermissionBean.CONVERT_FN);
    }

    public static class PermissionBean
    {
        static Function<GlobalPermissionEntry, PermissionBean> CONVERT_FN = new Function<GlobalPermissionEntry, PermissionBean>()
        {
            @Override
            public PermissionBean apply(GlobalPermissionEntry globalPermissionEntry)
            {
                PermissionBean permissionBean = new PermissionBean();
                permissionBean.permType = globalPermissionEntry.getPermissionKey();
                permissionBean.group = globalPermissionEntry.getGroup();

                return permissionBean;
            }
        };

        public String permType;
        public String group;
    }

    @GET
    @AnonymousAllowed
    @Path("global/remove/key")
    public Response removeGlobalPermissionByKey(@QueryParam ("type") String permissionType, @QueryParam ("group") String group)
    {
        GlobalPermissionType globalPermissionType = globalPermissionManager.getGlobalPermission(permissionType).get();
        globalPermissionManager.removePermission(globalPermissionType, group);
        return Response.ok(null).build();
    }

    @GET
    @AnonymousAllowed
    @Produces ({ MediaType.APPLICATION_JSON })
    @Path ("global/getgroups/key")
    public Response getGlobalPermissionGroupsByKey(@QueryParam ("type") String permissionType)
    {
        Collection<String> groupNames = new ArrayList<String>();
        Option<GlobalPermissionType> globalPermissionTypeOpt = globalPermissionManager.getGlobalPermission(permissionType);
        if(globalPermissionTypeOpt.isDefined())
        {
            GlobalPermissionType globalPermissionType = globalPermissionTypeOpt.get();
            // Use this method instead getGroupNames as it will not retrun "anyone"
            // group which means null for group name.
            for (GlobalPermissionEntry jiraPermission : globalPermissionManager.getPermissions(globalPermissionType.getGlobalPermissionKey()))
            {
                groupNames.add(jiraPermission.getGroup());
            }
        }
        List<String> str = Lists.newArrayListWithCapacity(groupNames.size());
        str.addAll(groupNames);

        return Response.ok(str).cacheControl(never()).build();
    }
}
