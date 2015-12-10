package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.screen.FieldScreenTab;

import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings ({ "UnusedDeclaration" })
public class ScreenableTabBean
{
    public static final Collection<ScreenableTabBean> DOC_EXAMPLE_LIST;
    public static final ScreenableTabBean DOC_EXAMPLE;
    public static final ScreenableTabBean DOC_EXAMPLE_2;
    static
    {
        DOC_EXAMPLE = new ScreenableTabBean();
        DOC_EXAMPLE.id = 10000L;
        DOC_EXAMPLE.name = "Fields Tab";

        DOC_EXAMPLE_2 = new ScreenableTabBean();
        DOC_EXAMPLE_2.id = 10000L;
        DOC_EXAMPLE_2.name = "My Custom Tab";

        DOC_EXAMPLE_LIST = Arrays.asList(DOC_EXAMPLE, DOC_EXAMPLE_2);
    }

    @XmlElement
    public Long id;

    @XmlElement
    public String name;

    public ScreenableTabBean() {}

    public ScreenableTabBean(FieldScreenTab tab)
    {
        name = tab.getName();
        id = tab.getId();
    }
}