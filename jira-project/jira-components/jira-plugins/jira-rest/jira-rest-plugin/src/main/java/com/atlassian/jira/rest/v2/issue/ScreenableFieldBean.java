package com.atlassian.jira.rest.v2.issue;

import com.atlassian.core.util.collection.EasyList;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class ScreenableFieldBean
{
    @XmlElement
    private String id;
    @XmlElement
    private String name;

    final static ScreenableFieldBean DOC_EXAMPLE;
    final static ScreenableFieldBean DOC_EXAMPLE_2;
    final static List<ScreenableFieldBean> DOC_EXAMPLE_LIST;
    static
    {
        DOC_EXAMPLE = new ScreenableFieldBean();
        DOC_EXAMPLE.id = "summary";
        DOC_EXAMPLE.name = "Summary";

        DOC_EXAMPLE_2 = new ScreenableFieldBean();
        DOC_EXAMPLE_2.id = "description";
        DOC_EXAMPLE_2.name = "Description";

        DOC_EXAMPLE_LIST = EasyList.build(DOC_EXAMPLE, DOC_EXAMPLE_2);
    }

    public ScreenableFieldBean()
    {
        // tooling
    }

    public ScreenableFieldBean(String id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

}
