package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class UserPickerUser
{
    @XmlElement
    private String name;
    @XmlElement
    private String key;
    @XmlElement
    private String html;
    @XmlElement
    private String displayName;
    @XmlElement
    private URI avatarUrl;

    @SuppressWarnings ({ "UnusedDeclaration", "unused" })
    private UserPickerUser() {}

    public UserPickerUser(String name, String key, String displayName, String html, URI avatarUrl)
    {
        this.name = name;
        this.key = key;
        this.displayName = displayName;
        this.html = html;
        this.avatarUrl = avatarUrl;
    }


    public static final UserPickerUser DOC_EXAMPLE = new UserPickerUser();
    public static final List<UserPickerUser> DOC_EXAMPLE_LIST = new ArrayList<UserPickerUser>();

    static
    {
        DOC_EXAMPLE.name = "fred";
        DOC_EXAMPLE.key = "fred";
        DOC_EXAMPLE.displayName = "Fred Grumble";
        DOC_EXAMPLE.html = "fred@example.com";
        DOC_EXAMPLE.avatarUrl = Examples.jiraURI("secure/useravatar?size=small&ownerId=fred");
        DOC_EXAMPLE_LIST.add(DOC_EXAMPLE);
    }
}
