package com.atlassian.jira.rest.v2.issue;

import com.atlassian.plugins.rest.common.expand.Expandable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Bean for the top level of a createmeta issue request.
 *
 * @since v5.0
 */
public class TransitionsMetaBean
{
    @XmlAttribute
    private String expand;

    @XmlElement
    @Expandable
    private List<TransitionBean> transitions;

    public TransitionsMetaBean(final List<TransitionBean> transitions)
    {
        this.transitions = (transitions != null) ? transitions : Collections.<TransitionBean>emptyList();
    }

    static final TransitionsMetaBean DOC_EXAMPLE;
    static
    {
        List<TransitionBean> transitions = new ArrayList<TransitionBean>();
        transitions.add(TransitionBean.DOC_EXAMPLE);
        transitions.add(TransitionBean.DOC_EXAMPLE_2);

        DOC_EXAMPLE = new TransitionsMetaBean(transitions);
    }

}
