package com.atlassian.jira.issue.link;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.ofbiz.OfBizValueWrapper;

@PublicApi
public interface IssueLinkType extends OfBizValueWrapper, Comparable<IssueLinkType>
{
    public static final String NAME_FIELD_NAME = "linkname";
    public static final String OUTWARD_FIELD_NAME = "outward";
    public static final String INWARD_FIELD_NAME = "inward";
    public static final String STYLE_FIELD_NAME = "style";

    /**
     * @return the id of this IssueLinkType
     */
    public Long getId();

    /**
     * @return the name of this IssueLinkType
     */
    public String getName();

    /**
     * @return the outward name of this IssueLinkType
     */
    public String getOutward();

    /**
     * @return the inward name of this IssueLinkType
     */
    public String getInward();

    /**
     * @return the style name of this IssueLinkType
     */
    public String getStyle();

    /**
     * @return true if the link type is a subtask link
     */
    public boolean isSubTaskLinkType();

    /**
     * Checks if this link type is a System Link type.
     * <p>
     * System link types are used by JIRA to denote a special relationship between issues.
     * For example, a sub-task is linked to its parent issue using a link that is of a system link type.
     *
     * @return true if this is a System Link Type
     */
    public boolean isSystemLinkType();
}
