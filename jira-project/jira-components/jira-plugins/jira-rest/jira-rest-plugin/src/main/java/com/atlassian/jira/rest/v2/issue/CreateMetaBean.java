package com.atlassian.jira.rest.v2.issue;

import com.atlassian.plugins.rest.common.expand.Expandable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.List;

/**
 * Bean for the top level of a createmeta issue request.
 *
 * @since v5.0
 */
public class CreateMetaBean
{
    @XmlAttribute
    private String expand;
    
    @XmlElement
    @Expandable
    private List<CreateMetaProjectBean> projects;

    public CreateMetaBean(final List<CreateMetaProjectBean> projects)
    {
        this.projects = (projects != null) ? projects : Collections.<CreateMetaProjectBean>emptyList();
    }
}
