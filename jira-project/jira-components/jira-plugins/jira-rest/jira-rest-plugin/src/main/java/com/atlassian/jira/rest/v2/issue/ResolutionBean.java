package com.atlassian.jira.rest.v2.issue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.resolution.Resolution;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
* @since v4.2
*/
@XmlRootElement (name="resolution")
public class ResolutionBean
{
    static final ResolutionBean DOC_EXAMPLE;
    static final ResolutionBean DOC_EXAMPLE_2;
    static final List<ResolutionBean> DOC_EXAMPLE_LIST;
    static
    {
        ResolutionBean resolution = new ResolutionBean();
        resolution.self = Examples.restURI("resolution/1");
        resolution.name = "Fixed";
        resolution.description = "A fix for this issue is checked into the tree and tested.";
        resolution.iconUrl = Examples.jiraURI("images/icons/statuses/resolved.png").toString();
        
        DOC_EXAMPLE = resolution;

        resolution = new ResolutionBean();
        resolution.self = Examples.restURI("resolution/3");
        resolution.name = "Works as designed";
        resolution.description = "This is what it is supposed to do.";

        DOC_EXAMPLE_2 = resolution;

        DOC_EXAMPLE_LIST = EasyList.build(DOC_EXAMPLE, DOC_EXAMPLE_2);
    }

    @XmlElement
    private URI self;

    @XmlElement
    private String description;

    @XmlElement
    private String iconUrl;

    @XmlElement
    private String name;

    @XmlElement
    private String id;

    public ResolutionBean() {}

    public static ResolutionBean shortBean(final Resolution resolution, final UriInfo uriInfo)
    {
        final ResolutionBean bean = new ResolutionBean();

        bean.self = uriInfo.getBaseUriBuilder().path(ResolutionResource.class).path(resolution.getId()).build();
        bean.name = resolution.getNameTranslation();
        bean.id = resolution.getId();

        return bean;
    }

    public static ResolutionBean fullBean(final Resolution resolution, final UriInfo uriInfo)
    {
        final ResolutionBean bean = shortBean(resolution, uriInfo);

        bean.description = resolution.getDescTranslation();
        // Icon URL is not currently used for Resolutions
        // bean.iconUrl = resolution.getIconUrl();

        return bean;
    }

    public static Collection<ResolutionBean> asBeans(final Collection<? extends Resolution> priorities, final UriInfo uriInfo, final String baseUrl)
    {
        final ArrayList<ResolutionBean> list = new ArrayList<ResolutionBean>();
        for (Resolution Resolution : priorities)
        {
            list.add(ResolutionBean.shortBean(Resolution, uriInfo));
        }
        return list;
    }
}
