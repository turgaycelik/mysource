package com.atlassian.jira.rest.bind;

import com.atlassian.jira.rest.Dates;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

/**
 * Adapter for marshalling and unmarshalling <b>calendar dates</b>. This class must <b>not</b> be used to marshall dates
 * that correspond to a specific instant (that is, a date/time object). For date/time objects, the {@link
 * DateTimeAdapter} should be used instead.
 * <p/>
 * The wire format used is <code>'{@value Dates#DATE_FORMAT}'</code>.
 *
 * @see Dates
 * @since v4.4
 */
public class DateAdapter extends XmlAdapter<String, Date>
{
    @Override
    public Date unmarshal(String text) throws Exception
    {
        return Dates.fromDateString(text);
    }

    @Override
    public String marshal(Date date) throws Exception
    {
        return Dates.asDateString(date);
    }
}
