package com.atlassian.jira.rest.v2.issue.customfield;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.net.URISyntaxException;

/**
* @since v4.4
*/
@XmlRootElement (name="customFieldOption")
public class CustomFieldOptionBean
{
    /**
     * Example representation of an issue type.
     * <pre>
     * {
     *   self: "http://localhost:8090/jira/rest/api/2.0/customFieldOption/3",
     *   iconUrl: "http://localhost:8090/jira/images/icons/issuetypes/task.png",
     *   value: "Blue"
     * }
     * </pre>
     */
    static final CustomFieldOptionBean DOC_EXAMPLE;
    static
    {
        try
        {
            DOC_EXAMPLE = new CustomFieldOptionBean(
                    new URI("http://localhost:8090/jira/rest/api/2.0/customFieldOption/3"),
                    "Blue"
            );
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e); // never happens
        }
    }

    @XmlElement
    private URI self;

    @XmlElement
    private String value;

    /**
     * Non-public constructor used for reflection-based tools.
     */
    private CustomFieldOptionBean() {
        // empty
    }

    public CustomFieldOptionBean(URI selfUri, String value)
    {
        this.self = selfUri;
        this.value = value;
    }
}
