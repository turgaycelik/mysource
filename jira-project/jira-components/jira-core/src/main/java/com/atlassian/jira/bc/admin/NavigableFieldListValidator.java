package com.atlassian.jira.bc.admin;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.validation.Failure;
import com.atlassian.validation.Success;
import com.atlassian.validation.Validator;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Validates a list of strings to each be the id of a navigable field that is valid for at least one project.
 *
 * @since v4.4
 */
public final class NavigableFieldListValidator implements Validator
{
    private static final Logger log = Logger.getLogger(NavigableFieldListValidator.class);

    FieldManager fieldManager;

    public NavigableFieldListValidator(FieldManager fieldManager)
    {
        this.fieldManager = fieldManager;
    }

    public NavigableFieldListValidator()
    {
        this(ComponentAccessor.getComponent(FieldManager.class));// injection would be nice
    }

    @Override
    public Result validate(String value)
    {

        String[] fields = value.split(",");
        ArrayList<String> badFields = new ArrayList<String>();
        for (String f : fields)
        {
            String field = f.trim();
            try
            {
                if ("".equals(field) || fieldManager.getNavigableField(field) == null)
                {
                    badFields.add(field);
                    log.debug("Couldn't find requested navigable field '" + field + "'");
                }
            }
            catch (RuntimeException e)
            {
                badFields.add(field);
                log.debug("Couldn't find requested navigable field '" + field + "'");
            }

        }
        if (badFields.isEmpty())
        {
            return new Success(value);
        }
        else
        {
            // TODO i18n
            String mesg = "The following fields are invalid: " + StringUtils.createCommaSeperatedString(badFields);
            return new Failure(mesg, TextUtils.htmlEncode(mesg));
        }
    }


}
