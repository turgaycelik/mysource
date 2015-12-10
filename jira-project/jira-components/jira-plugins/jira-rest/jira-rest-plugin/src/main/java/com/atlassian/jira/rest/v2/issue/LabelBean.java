package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.label.Label;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

/**
* @since v4.2
*/
@XmlRootElement (name="label")
public class LabelBean
{
    // At the moment labels are repesented simply as strings, which makes this entire class a bit
    // superfluous. If we expand labels into something more interesting then they would have a self-link
    // and possibly more things in the standard short/full variants.
    public static Collection<String> asStrings(final Collection<Label> labels)
    {
        final ArrayList<String> list = new ArrayList<String>();
        for (Label label : labels)
        {
            list.add(label.getLabel());
        }
        return list;
    }

    public LabelBean() {}
}
