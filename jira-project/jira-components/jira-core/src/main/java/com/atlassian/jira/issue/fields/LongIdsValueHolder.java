package com.atlassian.jira.issue.fields;

import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.StringUtils.trim;

/**
 * A type of List&lt;Long> that can be used as an entry in the fieldsvaluemap, but that can keep track of error values
 * (and hence return them in error edithtmls).
 *
 * @since v5.1
 */
public class LongIdsValueHolder extends LinkedList<Long>
{
    public static final String NEW_VALUE_PREFIX = "nv_";

    private final Set<String> valuesToAdd = newLinkedHashSet();
    private final Set<String> badStrings = newLinkedHashSet();

    public static LongIdsValueHolder fromFieldValuesHolder(String id, Map params)
    {

        Object o = params.get(id);
        if (o instanceof LongIdsValueHolder)
        {
            return (LongIdsValueHolder) o;
        }

        if (o instanceof Collection)
        { // we may need to up-cast it to a "better" collection
            LongIdsValueHolder vh = new LongIdsValueHolder(new ArrayList<Long>((Collection) o));
            try
            {
                params.put(vh, id); // put "better" value back
            }
            catch (UnsupportedOperationException e)
            {
                // see JRADEV-13352: sometimes older plugins call OrderableField.updateIssue() (with an immutable
                // map) in order to update a value in an issue. The newer way to do it is via the issue service.
                // but instead of throwing a UOE, let's just capture it and continue on.
                // (the only time we need to mutate the map is to assist with a UI issue, and if someone
                // is calling it programatically, they don't need the functionalty delivered via the mutation.
            }
            return vh;
        }

        return null;
    }

    public LongIdsValueHolder(List<Long> componentIds)
    {
        super(componentIds);
    }

    public LongIdsValueHolder(String[] value)
    {
        this(null == value ? null : Arrays.asList(value));
    }

    public LongIdsValueHolder(Collection<String> value)
    {
        if (value != null && !value.isEmpty())
        {
            for (String aValue : value)
            {
                try
                {
                    this.add(new Long(aValue));
                }
                catch (NumberFormatException e)
                {
                    handleNonNumericValue(aValue);
                }
            }
        }
    }

    public String getInputText()
    {
        final Set<String> allInputStrings = newLinkedHashSet(badStrings);
        return StringUtils.join(allInputStrings, "");
    }

    public Set<String> getValuesToAdd()
    {
        return Collections.unmodifiableSet(valuesToAdd);
    }

    public void addBadId(Long componentId)
    {
        badStrings.add(Long.toString(componentId));
    }

    public void validateIds(Predicate<Long> predicate)
    {
        Iterator<Long> it = iterator();
        while (it.hasNext())
        {
            Long id = it.next();
            if (!predicate.apply(id))
            {
                it.remove();
                addBadId(id);
            }
        }
    }

    private void handleNonNumericValue(final String aValue)
    {
        final String normalizedValue = trim(StringUtils.remove(aValue, NEW_VALUE_PREFIX));
        if (isBlank(normalizedValue))
        {
            return;
        }

        if (startsWith(aValue, NEW_VALUE_PREFIX))
        {
            valuesToAdd.add(normalizedValue);
        }
        else
        {
            badStrings.add(normalizedValue);
        }
    }
}
