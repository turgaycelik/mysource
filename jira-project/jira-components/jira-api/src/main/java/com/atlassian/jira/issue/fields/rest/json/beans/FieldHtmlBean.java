package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.annotations.ExperimentalApi;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents a field to be used in a clientside HTML app.  In order to render this field we need to know the
 * field's id, i18nized label, if it's required and the field's edit HTML (rendered by the field itself).
 *
 * @since 5.0.3
 */
@ExperimentalApi
@XmlRootElement (name = "field")
public class FieldHtmlBean
{
    @XmlElement (name = "id")
    private String id;
    @XmlElement (name = "label")
    private String label;
    @XmlElement (name = "required")
    private boolean required;
    @XmlElement (name = "editHtml")
    private String editHtml;
    @XmlElement (name = "tab")
    private FieldTab tab;

    private FieldHtmlBean() {}

    public FieldHtmlBean(final String id, final String label, final boolean required, final String editHtml, final FieldTab tab)
    {
        this.id = notNull("id", id);
        this.label = label;
        this.required = required;
        this.editHtml = editHtml;
        this.tab = tab;
    }

    public String getId()
    {
        return id;
    }

    public String getLabel()
    {
        return label;
    }

    public boolean isRequired()
    {
        return required;
    }

    public String getEditHtml()
    {
        return editHtml;
    }

    public FieldTab getTab()
    {
        return tab;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final FieldHtmlBean that = (FieldHtmlBean) o;

        if (required != that.required) { return false; }
        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (label != null ? !label.equals(that.label) : that.label != null) { return false; }
        if (tab != null ? !tab.equals(that.tab) : that.tab != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (required ? 1 : 0);
        result = 31 * result + (tab != null ? tab.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("id", id).
                append("label", label).
                append("required", required).
                append("editHtml", editHtml).
                append("tab", tab).
                toString();
    }
}
