package com.atlassian.jira.rest.v2.issue;


import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkTypeJsonBean;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @since v4.3
 */
@XmlRootElement (name = "issueLinkTypes")
public class IssueLinkTypesBean
{
    @XmlElement (name = "issueLinkTypes")
    private List<IssueLinkTypeJsonBean> issueIssueLinkTypes;

    static final IssueLinkTypesBean DOC_EXAMPLE;

    static
    {
        DOC_EXAMPLE = new IssueLinkTypesBean(Lists.newArrayList(ResourceExamples.ISSUE_LINK_TYPE_EXAMPLE, ResourceExamples.ISSUE_LINK_TYPE_EXAMPLE_2));
    }

    public IssueLinkTypesBean(List<IssueLinkTypeJsonBean> issueIssueLinkTypes)
    {
        this.issueIssueLinkTypes = issueIssueLinkTypes;
    }

    public IssueLinkTypesBean() { }

    public static IssueLinkTypesBean create(List<IssueLinkTypeJsonBean> issueLinkTypeList)
    {
        return new IssueLinkTypesBean(issueLinkTypeList);
    }
}
