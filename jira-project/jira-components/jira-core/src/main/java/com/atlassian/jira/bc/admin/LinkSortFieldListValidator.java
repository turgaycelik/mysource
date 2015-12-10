package com.atlassian.jira.bc.admin;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.issue.link.LinkCollectionImpl;
import com.atlassian.validation.Failure;
import com.atlassian.validation.Success;
import com.atlassian.validation.Validator;
import com.google.common.base.Supplier;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Validates a comma separated list of fields to ensure they are only the fields that may be used for specifying the
 * issue link sort order.
 *
 * @since v4.4
 */
public final class LinkSortFieldListValidator implements Validator
{

    private Supplier<Collection<String>> linkSortFields;

    public LinkSortFieldListValidator()
    {
        this(new Supplier<Collection<String>>(){
            @Override
            public Collection<String> get()
            {
                return LinkCollectionImpl.getSortableFields();
            }
        });
    }

    LinkSortFieldListValidator(Supplier<Collection<String>> linkSortFields)
    {
        this.linkSortFields = linkSortFields;
    }

    public Validator.Result validate(String value)
    {
        String[] fields = value.split(",");
        ArrayList<String> badFields = new ArrayList<String>();
        Collection<String> allowableFields = linkSortFields.get();
        for (String f : fields)
        {
            String field = f.trim();
            if (!allowableFields.contains(field))
            {
                badFields.add(field);
            }
        }
        if (badFields.isEmpty())
        {
            return new Success(value);
        }
        else
        {
            // TODO i18n
            String csv = StringUtils.createCommaSeperatedString(allowableFields);
            return new Failure("Only the following fields are allowed: " + csv);
        }
    }

}
