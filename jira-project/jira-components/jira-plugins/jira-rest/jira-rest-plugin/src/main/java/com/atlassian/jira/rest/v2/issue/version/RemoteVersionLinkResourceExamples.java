package com.atlassian.jira.rest.v2.issue.version;

import java.net.URI;
import java.util.List;

import com.atlassian.jira.rest.v2.entity.RemoteEntityLinkJsonBean;
import com.atlassian.jira.rest.v2.entity.RemoteEntityLinksJsonBean;

import com.google.common.collect.ImmutableList;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Example JSON payloads for remote version link use cases.
 *
 * @since JIRA REST v6.5.1 (JIRA v6.1.1)
 */
public class RemoteVersionLinkResourceExamples
{
    public static final CreateOrUpdateExampleJsonBean CREATE_OR_UPDATE = new CreateOrUpdateExampleJsonBean();

    public static final RemoteEntityLinkJsonBean LINK1 = new RemoteEntityLinkJsonBean()
            .self(URI.create("http://www.example.com/version/10000/SomeGlobalId"))
            .name("Version 1")
            .link("{ \"globalId\": \"SomeGlobalId\", \"myCustomLinkProperty\": true, \"colors\": [ \"red\", \"green\", \"blue\" ]}");
    public static final RemoteEntityLinkJsonBean LINK2 = new RemoteEntityLinkJsonBean()
            .self(URI.create("http://www.example.com/version/10000/AnotherGlobalId"))
            .name("Version 1")
            .link("{ \"globalId\": \"AnotherGlobalId\", \"myCustomLinkProperty\": false, \"colors\": [ \"cyan\", \"magenta\", \"yellow\" ]}");
    public static final RemoteEntityLinkJsonBean LINK3 = new RemoteEntityLinkJsonBean()
            .self(URI.create("http://www.example.com/version/10101/SomeGlobalId"))
            .name("Version 2")
            .link("{ \"globalId\": \"SomeGlobalId\" }");

    // Note: Ordered by version ID
    public static final RemoteEntityLinksJsonBean LINKS_BY_GLOBAL_ID = new RemoteEntityLinksJsonBean()
            .links(list(LINK1, LINK3));

    // Note: Ordered by global ID
    public static final RemoteEntityLinksJsonBean LINKS_BY_VERSION_ID = new RemoteEntityLinksJsonBean()
            .links(list(LINK2, LINK1));

    private static List<RemoteEntityLinkJsonBean> list(RemoteEntityLinkJsonBean... links)
    {
        return ImmutableList.copyOf(links);
    }


    public static class CreateOrUpdateExampleJsonBean
    {
        CreateOrUpdateExampleJsonBean() {}

        @JsonProperty
        private final String globalId = "SomeGlobalId";

        @JsonProperty
        private final boolean myCustomLinkProperty = true;

        @JsonProperty
        private final List<String> colors = ImmutableList.of("red", "green", "blue");

        @JsonProperty
        private final List<String> notes = ImmutableList.of(
                "Remote version links may take any well-formed JSON shape that is desired,",
                "provided that they fit within the maximum buffer size allowed,",
                "which is currently 32,768 characters." );
    }
}
