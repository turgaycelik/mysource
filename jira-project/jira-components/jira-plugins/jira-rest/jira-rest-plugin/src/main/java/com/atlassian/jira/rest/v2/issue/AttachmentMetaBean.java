package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents the enabled attachment capabilities.
 *
 * @since v5.0
 */
public class AttachmentMetaBean
{
    @JsonProperty
    private boolean enabled;

    @JsonProperty
    /** Upload limit in bytes. */
    private Long uploadLimit;

    public static final AttachmentMetaBean DOC_EXAMPLE;

    static
    {
        DOC_EXAMPLE = new AttachmentMetaBean(true, 1000000L);
    }

    public AttachmentMetaBean(boolean enabled, Long uploadLimit)
    {
        this.enabled = enabled;
        this.uploadLimit = uploadLimit;
    }
}
