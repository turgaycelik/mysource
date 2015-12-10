package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;
import com.atlassian.plugins.rest.common.expand.Expandable;
import com.atlassian.plugins.rest.common.expand.SelfExpanding;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.Map;

/**
* @since v4.2
*/
@SuppressWarnings ({ "FieldCanBeLocal", "UnusedDeclaration" })
@XmlRootElement
@JsonIgnoreProperties (ignoreUnknown = true)
public class TransitionBean
{
    @XmlElement
    private String id;

    @XmlElement
    private String name;

    @XmlElement
    private StatusJsonBean to;

    @XmlElement
    private Map<String, FieldMetaBean> fields;

    @XmlTransient
    private TransitionMetaFieldBeanBuilder fieldsBuilder;

    @XmlTransient
    @Expandable ("fields")
    private SelfExpanding fieldsExpander = new SelfExpanding()
    {
        public void expand()
        {
            fields = fieldsBuilder.build();
        }
    };

    @XmlElement
    private String expand;

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public TransitionBean(String id)
    {
        this.id = id;
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    TransitionBean() {}
    public TransitionBean(final String id, final String name, TransitionMetaFieldBeanBuilder fieldsBuilder, final StatusJsonBean to)
    {
        this.id = id;
        this.name = name;
        this.fieldsBuilder = fieldsBuilder;
        this.to = to;
    }

    static final TransitionBean DOC_EXAMPLE;
    static final TransitionBean DOC_EXAMPLE_2;
    static
    {
        DOC_EXAMPLE = new TransitionBean();
        DOC_EXAMPLE.id = "2";
        DOC_EXAMPLE.name = "Close Issue";
        DOC_EXAMPLE.to = StatusBeanExample.DOC_EXAMPLE;
        DOC_EXAMPLE.fields = new HashMap<String, FieldMetaBean>();
        DOC_EXAMPLE.fields.put("summary", FieldMetaBean.DOC_EXAMPLE);
        DOC_EXAMPLE_2 = new TransitionBean();
        DOC_EXAMPLE_2.id = "711";
        DOC_EXAMPLE_2.name = "QA Review";
        DOC_EXAMPLE_2.to = StatusBeanExample.DOC_EXAMPLE_2;
        DOC_EXAMPLE_2.fields = new HashMap<String, FieldMetaBean>();
        DOC_EXAMPLE_2.fields.put("summary", FieldMetaBean.DOC_EXAMPLE);
        DOC_EXAMPLE_2.fields.put("colour", FieldMetaBean.DOC_EXAMPLE);
    }

    /**
     * This subclass is needed to keep JAXB from freaking out.
     */
    @XmlRootElement (name = "transitions")
    static class SerializableHashMap<K, V> extends HashMap<K, V>
    {
        // empty
    }
}
