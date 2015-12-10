package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.option.IssueConstantOption;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This describes a simple option.
 *
 * @since v5.0
 */
public class SimpleOptionBean
{
    public String id;
    public String name;
    public String description;
    public String iconUrl;

    public SimpleOptionBean() {}
    
    public static SimpleOptionBean shortBean(final IssueConstantOption issueConstantOption, final UriInfo uriInfo, final String baseUrl)
    {
        final SimpleOptionBean bean = new SimpleOptionBean();
        bean.name = issueConstantOption.getName();
        bean.id = issueConstantOption.getId();
        bean.description = issueConstantOption.getDescription();
        bean.iconUrl = baseUrl + issueConstantOption.getImagePath();

        return bean;
    }

    public static Collection<SimpleOptionBean> asBeans(final Collection<? extends IssueConstantOption> priorities, final UriInfo uriInfo, final String baseUrl)
    {
        final ArrayList<SimpleOptionBean> list = new ArrayList<SimpleOptionBean>();
        for (IssueConstantOption issueConstantOption : priorities)
        {
            list.add(SimpleOptionBean.shortBean(issueConstantOption, uriInfo, baseUrl));
        }
        return list;
    }
    
}
