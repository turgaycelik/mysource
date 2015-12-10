package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;

public class AddFieldBean
{
    static final AddFieldBean DOC_EXAMPLE;

    static
    {
        DOC_EXAMPLE = new AddFieldBean();
        DOC_EXAMPLE.fieldId = "summary";
    }

    // need this property when adding fields because if we set id on backbone models, it doesn't think it is new.
    @XmlElement
    public String fieldId;

    public AddFieldBean() {}

    public AddFieldBean(String fieldId)
    {
        this.fieldId = fieldId;
    }

    public String getFieldId()
    {
        return fieldId;
    }
}