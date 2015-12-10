package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;


public class UsersAndGroupsBean
{
    @XmlElement
    private UserPickerResultsBean users;
    @XmlElement
    private GroupSuggestionsBean groups;

    UsersAndGroupsBean()
    {}

    public UsersAndGroupsBean(final UserPickerResultsBean users, final GroupSuggestionsBean groups)
    {
        this.users = users;
        this.groups = groups;
    }

    public UserPickerResultsBean getUsers()
    {
        return users;
    }

    public GroupSuggestionsBean getGroups()
    {
        return groups;
    }

    public static final UsersAndGroupsBean DOC_EXAMPLE = new UsersAndGroupsBean();

    static
    {
        DOC_EXAMPLE.users = UserPickerResultsBean.DOC_EXAMPLE;
        DOC_EXAMPLE.groups = GroupSuggestionsBean.DOC_EXAMPLE;
    }
}



