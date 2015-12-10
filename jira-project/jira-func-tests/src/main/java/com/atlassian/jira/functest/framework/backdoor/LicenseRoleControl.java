package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;

public class LicenseRoleControl extends RestApiClient<LicenseRoleControl>
{
    private static final String BUSINESS_USER = "businessuser";

    public LicenseRoleControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public List<LicenseRoleBean> getRoles()
    {
        return createLicenseRoleResource().get(LicenseRoleBean.LIST);
    }

    public Map<String, LicenseRoleBean> getRolesMap()
    {
        return Maps.uniqueIndex(getRoles(), LicenseRoleBean.GET_ID);
    }

    public Response<List<LicenseRoleBean>> getRolesResponse()
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createLicenseRoleResource().get(ClientResponse.class);
            }
        }, LicenseRoleBean.LIST);
    }

    public LicenseRoleBean getRole(String role)
    {
        return createLicenseRoleResource().path(role).get(LicenseRoleBean.class);
    }

    public LicenseRoleBean getBusinessUser()
    {
        return getRole(BUSINESS_USER);
    }

    public Response<LicenseRoleBean> getRoleResponse(final String role)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createLicenseRoleResource().path(role).get(ClientResponse.class);
            }
        }, LicenseRoleBean.class);
    }

    public LicenseRoleBean putRole(String role, String... groups)
    {
        return createLicenseRoleResource().path(role)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(LicenseRoleBean.class, new LicenseRoleBean(groups));
    }

    public Response<LicenseRoleBean> putRoleResponse(final String role, final String... groups)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createLicenseRoleResource().path(role)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .put(ClientResponse.class, new LicenseRoleBean(groups));
            }
        }, LicenseRoleBean.class);
    }

    public LicenseRoleBean putBusinessUser(String...groups)
    {
        return putRole(BUSINESS_USER, groups);
    }

    private WebResource createLicenseRoleResource()
    {
        return createResource().path("licenserole");
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class LicenseRoleBean
    {
        private static final GenericType<List<LicenseRoleBean>> LIST = new GenericType<List<LicenseRoleBean>>(){};

        private static final Function<LicenseRoleBean, String> GET_ID = new Function<LicenseRoleBean, String>()
        {
            @Override
            public String apply(final LicenseRoleBean input)
            {
                return input.id;
            }
        };

        @JsonProperty
        private String name;

        @JsonProperty
        private String id;

        @JsonProperty
        private List<String> groups;

        public LicenseRoleBean()
        {
            groups = Lists.newArrayList();
        }

        private LicenseRoleBean(String...groups)
        {
            this.groups = Arrays.asList(groups);
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this)
                    .append("name", name)
                    .append("id", id)
                    .append("groups", groups)
                    .toString();
        }

        public String getName()
        {
            return name;
        }

        public String getId()
        {
            return id;
        }

        public List<String> getGroups()
        {
            return groups;
        }
    }
}
