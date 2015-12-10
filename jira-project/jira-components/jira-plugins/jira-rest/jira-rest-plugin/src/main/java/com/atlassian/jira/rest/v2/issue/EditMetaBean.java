package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.Issue;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Bean for the top level of a GET editmeta issue request.
 *
 * @since v5.0
 */
public class EditMetaBean
{
    @XmlAttribute
    @XmlElement
    private Map<String, FieldMetaBean> fields;

    public EditMetaBean() {}

    public EditMetaBean(final Issue issue, final EditMetaFieldBeanBuilder fieldsBuilder)
    {

        this.fields = (issue != null) ? fieldsBuilder.build() : Collections.<String, FieldMetaBean>emptyMap();
    }

    static final EditMetaBean DOC_EXAMPLE;
    static
    {
        DOC_EXAMPLE = new EditMetaBean();
        DOC_EXAMPLE.fields = new HashMap<String, FieldMetaBean>();
        DOC_EXAMPLE.fields.put("summary", FieldMetaBean.DOC_EXAMPLE);
    }

}
