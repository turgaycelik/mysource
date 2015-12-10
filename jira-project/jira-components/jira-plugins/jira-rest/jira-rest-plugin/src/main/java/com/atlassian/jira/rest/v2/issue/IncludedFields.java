package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.rest.api.util.StringList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Encapsualtes the parsing and querying of the "fields=" query parameter.
 *
 * @since v5.0
 */
public class IncludedFields
{
    private final boolean includeAll;
    private final boolean includeNavigable;
    private final Set<String> include = new HashSet<String>();
    private final Set<String> exclude = new HashSet<String>();

    IncludedFields(boolean includeAllByDefault, boolean includeNavigableByDefault, List<StringList> fields)
    {
        boolean includeAll = false;
        boolean includeNavigable = false;
        if (fields != null) {
            for (String id : StringList.joinLists(fields).asList())
            {
                if ("*all".equals(id))
                {
                    includeAllByDefault = true;
                    includeAll = true;
                    includeNavigable = true;
                }
                else if ("*navigable".equals(id))
                {
                    includeAllByDefault = false;
                    includeNavigableByDefault = true;
                    includeNavigable = true;
                }
                else if (id.startsWith("-"))
                {
                    id = id.substring(1);
                    exclude.add(id);
                }
                else {
                    include.add(id);
                }
            }
        }

        if (include.isEmpty())
        {
            // nothing specifically included, honour the default includes
            this.includeAll = includeAllByDefault;
            this.includeNavigable = includeNavigableByDefault;
        }
        else
        {
            // they specifically mentioned an field, don't use any default includes unless they explicitly asked for it
            this.includeAll = includeAll;
            this.includeNavigable = includeNavigable;
        }

    }

    public static IncludedFields includeAllByDefault(List<StringList> fields)
    {
        return new IncludedFields(true, true, fields);
    }
    public static IncludedFields includeNavigableByDefault(List<StringList> fields)
    {
        return new IncludedFields(false, true, fields);
    }

    public boolean included(Field field)
    {
        return included(field.getId(), field instanceof NavigableField);
    }

    public boolean included(String fieldId, boolean navigable)
    {
        if (exclude.contains(fieldId))
        {
            return false;
        }
        if (includeAll || (navigable && includeNavigable)) {
            return true;
        }
        return include.contains(fieldId);
    }

}
