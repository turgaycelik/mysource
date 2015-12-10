package com.atlassian.velocity;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import com.atlassian.util.concurrent.Supplier;

import java.text.AttributedCharacterIterator;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * For performance reasons, we lazily create the date format.
 * <p>
 * These are created every single time any velocity is rendered,
 * so we need to make sure its construction is as cheap as
 * possible, but creating a SimpleDateFormat actually acquires
 * a global lock on the date time format symbols table, which
 * has been seen to cause significant contention and therefore
 * blocking.
 */
class DelegateDateFormat extends DateFormat
{
    private final Supplier<DateFormat> delegate;

    DelegateDateFormat(final Supplier<DateFormat> delegate)
    {
        this.delegate = notNull("delegate", delegate);
    }

    /*
     * @see java.text.DateFormat#format(java.util.Date, java.lang.StringBuffer, java.text.FieldPosition)
     */
    @Override
    public StringBuffer format(final Date date, final StringBuffer toAppendTo, final FieldPosition fieldPosition)
    {
        return delegate.get().format(date, toAppendTo, fieldPosition);
    }

    /*
     * @see java.text.Format#formatToCharacterIterator(java.lang.Object)
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(final Object obj)
    {
        return delegate.get().formatToCharacterIterator(obj);
    }

    /*
     * @see java.text.DateFormat#getCalendar()
     */
    @Override
    public Calendar getCalendar()
    {
        return delegate.get().getCalendar();
    }

    /*
     * @see java.text.DateFormat#getNumberFormat()
     */
    @Override
    public NumberFormat getNumberFormat()
    {
        return delegate.get().getNumberFormat();
    }

    /*
     * @see java.text.DateFormat#getTimeZone()
     */
    @Override
    public TimeZone getTimeZone()
    {
        return delegate.get().getTimeZone();
    }

    /*
     * @see java.text.DateFormat#isLenient()
     */
    @Override
    public boolean isLenient()
    {
        return delegate.get().isLenient();
    }

    /*
     * @see java.text.DateFormat#parse(java.lang.String, java.text.ParsePosition)
     */
    @Override
    public Date parse(final String source, final ParsePosition pos)
    {
        return delegate.get().parse(source, pos);
    }

    /*
     * @see java.text.DateFormat#parse(java.lang.String)
     */
    @Override
    public Date parse(final String source) throws ParseException
    {
        return delegate.get().parse(source);
    }

    /*
     * @see java.text.DateFormat#parseObject(java.lang.String, java.text.ParsePosition)
     */
    @Override
    public Object parseObject(final String source, final ParsePosition pos)
    {
        return delegate.get().parseObject(source, pos);
    }

    /*
     * @see java.text.Format#parseObject(java.lang.String)
     */
    @Override
    public Object parseObject(final String source) throws ParseException
    {
        return delegate.get().parseObject(source);
    }

    /*
     * @see java.text.DateFormat#setCalendar(java.util.Calendar)
     */
    @Override
    public void setCalendar(final Calendar newCalendar)
    {
        delegate.get().setCalendar(newCalendar);
    }

    /*
     * @see java.text.DateFormat#setLenient(boolean)
     */
    @Override
    public void setLenient(final boolean lenient)
    {
        delegate.get().setLenient(lenient);
    }

    /*
     * @see java.text.DateFormat#setNumberFormat(java.text.NumberFormat)
     */
    @Override
    public void setNumberFormat(final NumberFormat newNumberFormat)
    {
        delegate.get().setNumberFormat(newNumberFormat);
    }

    /*
     * @see java.text.DateFormat#setTimeZone(java.util.TimeZone)
     */
    @Override
    public void setTimeZone(final TimeZone zone)
    {
        delegate.get().setTimeZone(zone);
    }
}
