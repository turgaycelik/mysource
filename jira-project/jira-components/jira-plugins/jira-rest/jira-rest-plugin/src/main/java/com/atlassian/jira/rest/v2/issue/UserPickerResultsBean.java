package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class UserPickerResultsBean
{

    /**
     * A version bean instance used for auto-generated documentation.
     */
    static final UserPickerResultsBean DOC_EXAMPLE;
    static
    {
        UserPickerResultsBean bean = new UserPickerResultsBean();
        bean.header = "Showing 20 of 25 matching groups";
        bean.total = 25;
        bean.users = UserPickerUser.DOC_EXAMPLE_LIST;
        DOC_EXAMPLE = bean;
    }

    @XmlElement
    private List<UserPickerUser> users;

    @XmlElement
    private Integer total;

    @XmlElement
    private String header;

    @SuppressWarnings ({ "UnusedDeclaration", "unused" })
    private UserPickerResultsBean() {}

    public UserPickerResultsBean(List<UserPickerUser> users, String header, Integer total)
    {
        this.users = users;
        this.header = header;
        this.total = total;
    }
}
