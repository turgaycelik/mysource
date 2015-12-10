package com.atlassian.jira.jelly.tag;

import com.atlassian.jira.util.ErrorCollection;
import org.apache.commons.jelly.JellyTagException;

public class JellyUtils
{
    public static void processErrorCollection(ErrorCollection errorCollection) throws JellyTagException
    {
        if (errorCollection != null && errorCollection.hasAnyErrors())
        {
            StringBuilder errors = new StringBuilder("The following problems were found:\n");

            for (final String s : errorCollection.getErrorMessages())
            {
                errors.append(s).append('\n');
            }

            for (String key : errorCollection.getErrors().keySet())
            {
                errors.append(key).append(':').append(' ').append(errorCollection.getErrors().get(key));
            }

            throw new JellyTagException(errors.toString());
        }
    }
}
