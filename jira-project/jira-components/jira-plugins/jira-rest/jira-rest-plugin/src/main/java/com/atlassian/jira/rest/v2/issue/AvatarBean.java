package com.atlassian.jira.rest.v2.issue;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * @since v5.0
 */
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name = "avatar")
public class AvatarBean
{

    /**
     * Avatar bean example used in auto-generated documentation.
     */
    public static final AvatarBean DOC_EXAMPLE;
    public static final AvatarBean DOC_EXAMPLE_2;
    public static final Map<String, List<AvatarBean>> DOC_EXAMPLE_LIST;
    public static final Map<String, List<AvatarBean>> DOC_EXAMPLE_SYSTEM_LIST;

    static
    {
        DOC_EXAMPLE = new AvatarBean(
                "1000",
                "fred",
                true,
                ImmutableMap.of(
                        "16x16", URI.create("http://localhost:8090/jira/secure/useravatar?size=xsmall&avatarId=10040"),
                        "24x24", URI.create("http://localhost:8090/jira/secure/useravatar?size=small&avatarId=10040"),
                        "32x32", URI.create("http://localhost:8090/jira/secure/useravatar?size=medium&avatarId=10040"),
                        "48x48", URI.create("http://localhost:8090/jira/secure/useravatar?avatarId=10040")
                )
        );

        DOC_EXAMPLE_2 = new AvatarBean(
                "1010",
                "andrew",
                false,
                ImmutableMap.of(
                        "16x16", URI.create("http://localhost:8090/jira/secure/useravatar?size=xsmall&avatarId=10080"),
                        "24x24", URI.create("http://localhost:8090/jira/secure/useravatar?size=small&avatarId=10080"),
                        "32x32", URI.create("http://localhost:8090/jira/secure/useravatar?size=medium&avatarId=10080"),
                        "48x48", URI.create("http://localhost:8090/jira/secure/useravatar?avatarId=10080")
                )
        );

        DOC_EXAMPLE_LIST = new HashMap<String, List<AvatarBean>>();
        DOC_EXAMPLE_LIST.put("system", Collections.singletonList(DOC_EXAMPLE));
        DOC_EXAMPLE_LIST.put("custom", Collections.singletonList(DOC_EXAMPLE_2));

        DOC_EXAMPLE_SYSTEM_LIST = new HashMap<String, List<AvatarBean>>();
        DOC_EXAMPLE_SYSTEM_LIST.put("system", Collections.singletonList(DOC_EXAMPLE));
    }

    @XmlElement
    private String id;

    @XmlElement
    private String owner;

    @XmlElement
    private boolean isSystemAvatar;

    @XmlElement
    private boolean isSelected;

    @XmlElement
    Map<String, URI> urls;

    public AvatarBean() {}

    public AvatarBean(final String id, final String owner)
    {
        this(id, owner, false, null);
    }

    public AvatarBean(final String id, final String owner, final boolean isSystemAvatar, final Map<String,URI> urls)
    {
        this.id = id;
        this.owner = owner;
        this.isSystemAvatar = isSystemAvatar;
        this.urls = urls;
        this.isSelected = false;
    }

    public String getId()
    {
        return id;
    }

    public boolean isSelected()
    {
        return isSelected;
    }

    public void setSelected(boolean selected)
    {
        isSelected = selected;
    }

    public boolean getSystemAvatar()
    {
        return isSystemAvatar;
    }

    public String getOwner()
    {
        return owner;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
    }
}
