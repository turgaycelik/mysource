package com.atlassian.jira.rest.v2.issue;

import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Models the operations bar of a view issue page.  It contains a link group for issue operations (including workflow
 * transitions) and issue tools (sharing and issue views).
 * <p/>
 * Any other web-items that are in the <pre>view.issue.opsbar</pre> or <pre>jira.issue.tools</pre> web-sections
 * will also be added to these link groups.
 *
 * @since v5.0
 */
@SuppressWarnings ({ "FieldCanBeLocal", "UnusedDeclaration" })
@JsonIgnoreProperties (ignoreUnknown = true)
public class OpsbarBean
{
    @JsonProperty
    private List<LinkGroupBean> linkGroups = new ArrayList<LinkGroupBean>();

    @SuppressWarnings ({ "UnusedDeclaration" })
    OpsbarBean() {}

    public OpsbarBean(List<LinkGroupBean> linkGroups)
    {
        this.linkGroups.addAll(linkGroups);
    }

    public List<LinkGroupBean> getLinkGroups()
    {
        return linkGroups;
    }

    static final OpsbarBean DOC_EXAMPLE = new OpsbarBean(Lists.newArrayList(LinkGroupBean.DOC_EXAMPLE, LinkGroupBean.RECURSIVE_DOC_EXAMPLE));
}
