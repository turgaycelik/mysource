package com.atlassian.jira.rest.v2.admin.licenserole;

import com.atlassian.jira.license.LicenseRole;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Set;

/**
 * Representation of a {@link com.atlassian.jira.license.LicenseRole} in REST.
 *
 * @since v6.3
 */
public class LicenseRoleBean
{
    static final Function<LicenseRole, LicenseRoleBean> TO_BEAN = new Function<LicenseRole, LicenseRoleBean>()
    {
        @Override
        public LicenseRoleBean apply(final LicenseRole input)
        {
            return new LicenseRoleBean(input);
        }
    };

    @JsonProperty
    private String id;

    @JsonProperty
    private Set<String> groups;

    @JsonProperty
    private String name;

    public LicenseRoleBean(LicenseRole role)
    {
        this.id = role.getId().getName();
        this.groups = role.getGroups();
        this.name = role.getName();
    }

    //Needed for Jackson.
    public LicenseRoleBean()
    {
    }

    LicenseRoleBean(String id, String name, Set<String> groups)
    {
        this.id = id;
        this.groups = groups;
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public Set<String> getGroups()
    {
        return groups;
    }

    public String getName()
    {
        return name;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public void setGroups(final Set<String> groups)
    {
        this.groups = groups;
    }

    public void setName(final String name)
    {
        this.name = name;
    }
}
