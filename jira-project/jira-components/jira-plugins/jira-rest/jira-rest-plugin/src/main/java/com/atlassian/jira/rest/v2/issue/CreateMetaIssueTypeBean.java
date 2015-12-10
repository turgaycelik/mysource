package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.plugins.rest.common.expand.Expandable;
import com.atlassian.plugins.rest.common.expand.SelfExpanding;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.util.Map;

/**
 * Bean to represent issue types in a createmeta issue request.
 *
 * @since v5.0
 */
public class CreateMetaIssueTypeBean extends IssueTypeJsonBean
{
    @XmlAttribute
    private String expand;
    
    @XmlElement
    private Map<String, FieldMetaBean> fields;

    @XmlTransient
    private CreateMetaFieldBeanBuilder fieldsBuilder;

    @XmlTransient
    @Expandable ("fields")
    private SelfExpanding fieldsExpander = new SelfExpanding()
    {
        public void expand()
        {
            fields = fieldsBuilder.build();
        }
    };

    public CreateMetaIssueTypeBean(final String self, final String id, final String name, final String description, final boolean subTask, final String iconUrl, final CreateMetaFieldBeanBuilder fieldsBuilder)
    {
        setSelf(self);
        setId(id);
        setName(name);
        setDescription(description);
        setSubtask(subTask);
        setIconUrl(iconUrl);
        this.fieldsBuilder = fieldsBuilder;
    }

    /**
     * Needed to create REST doco, should not be used in general code.
     *
     * @param fields
     */
    void setFields(final Map<String, FieldMetaBean> fields)
    {
        this.fields = fields;
    }
}
