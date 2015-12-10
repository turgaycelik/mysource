package com.atlassian.jira.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class can be used to "parse" values from a map of parameters.  This is really intended to be used in {@link
 * webwork.action.Action} code that needs to read input parameters from {@link webwork.action.ActionContext#getParameters()}
 *
 */
public class ParameterUtils
{
    private static final Logger log = Logger.getLogger(ParameterUtils.class);

    /**
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatterFactory} instead. Since v5.2.
     */
    public static Date getDateParam(Map params, String s, Locale locale) throws DateTooEarlyException
    {
        String paramValue = getStringParam(params, s);

        return parseDate(paramValue, locale);

    }

    /**
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatterFactory} instead. Since v5.2.
     */
    public static Date parseDate(String paramValue, Locale locale)
    {
        if (StringUtils.isBlank(paramValue))
        {
            return null;
        }

        try
        {
            Date date = ComponentAccessor.getComponent(OutlookDateManager.class).getOutlookDate(locale).parseDatePicker(paramValue);
            if (date.getTime() < 0) // this is an invalid date
            {
                throw new DateTooEarlyException();
            }
            else
            {
                return date;
            }
        }
        catch (ParseException e)
        {
            log.warn("Could not parse: " + paramValue + " into a date");
        }

        return null;
    }

    public static User getUserParam(Map params, String s)
    {
        String paramValue = getStringParam(params, s);

        if (StringUtils.isBlank(paramValue))
        {
            return null;
        }

        return UserUtils.getUser(paramValue);
    }

    /**
     * Create a List from the parameter with the specified key.
     *
     * @return null if the object is not a String array, or has no elements<br> otherwise it returns a List containing
     *         all elements of the String array, with the Strings over value ("-1") turned into null's. (Strings with
     *         value "" are ignored).
     */
    public static List getListParam(Map params, String key)
    {
        Object o = params.get(key);

        if (o instanceof String[])
        {
            String[] oArray = (String[]) o;

            if (oArray.length > 0)
            {
                List oList = new ArrayList();
                for (String s : oArray)
                {
                    if (StringUtils.isBlank(s))
                    {
                        continue;
                    }
                    else if (s.equalsIgnoreCase("-1"))
                    {
                        oList.add(null);
                    }
                    else
                    {
                        oList.add(s);
                    }
                }

                return oList;
            }
        }

        return null;
    }

    public static List getListParamKeepMinusOnes(Map params, String key)
    {
        Object o = params.get(key);

        if (o instanceof String[])
        {
            String[] oArray = (String[]) o;

            if (oArray.length > 0)
            {
                return Lists.newArrayList(oArray);
            }
        }
        else if (o instanceof String)
        {
            return Lists.newArrayList(o);
        }

        return null;
    }

    /**
     * Make a list of Strings into a list of Longs
     */
    public static List makeListLong(List list)
    {
        List newList = null;
        if (list != null)
        {
            newList = new ArrayList(list.size());
            for (Object o : list)
            {
                if (o == null)
                {
                    newList.add(null);
                }
                else
                {
                    try
                    {
                        newList.add(new Long((String) o));
                    }
                    catch (NumberFormatException e)
                    {
                        log.warn("Could not convert to long: " + o);
                    }
                }
            }
        }
        return newList;
    }

    public static String getStringParam(Map params, String key)
    {
        Object o = params.get(key);

        if (o instanceof String)
        {
            return (String) o;
        }
        else if (o instanceof String[])
        {
            return ((String[]) o)[0];
        }
        else if (o instanceof Collection)
        {
            return (String) ((Collection) o).iterator().next();
        }
        else if (o == null)
        {
            return null;
        }
        else
        {
            return o.toString();
        }
    }

    /**
     * Returns the value of the specified parameter name as a String[]
     *
     * @param params the map of parameters
     * @param paramName the name of the parameter
     * @return a String[] of values or null if there are no parameters
     */
    public static String[] getStringArrayParam(Map params, String paramName)
    {
        Object o = params.get(paramName);

        if (o instanceof String)
        {
            return new String[] { (String) o };
        }
        else if (o instanceof String[])
        {
            return (String[]) o;
        }
        else if (o instanceof Collection)
        {
            Collection c = (Collection) o;
            String[] sa = new String[c.size()];
            int i = 0;
            for (Object obj : c)
            {
                sa[i++] = obj == null ? null : String.valueOf(obj);
            }
            return sa;
        }
        else if (o == null)
        {
            return null;
        }
        else
        {
            return new String[] { o.toString() };
        }
    }

    /**
     * Searches through the Map (params) for the given targetKey and targetValue extracting the index (i) and uses this
     * to extract the corresponding index value with the desiredKey. if there is no match, the first value or null is
     * returned
     *
     * @return desiredValue - corresponding value to desiredKey with the same index as the targetKey and targetValue, or
     *         the first value if there is only one, otherwise null
     */
    public static String getStringParam(Map params, String targetKey, String targetValue, String desiredKey)
    {
        Object targetO = params.get(targetKey);
        Object desiredO = params.get(desiredKey);

        if (desiredO instanceof String)
        {
            return ((String) desiredO);
        }
        else if (targetO instanceof String[] && desiredO instanceof String[])
        {
            for (int i = 0; i < ((String[]) targetO).length; i++)
            {
                if (((String[]) targetO)[i].equals(targetValue) == true)
                {
                    return ((String[]) desiredO)[i];
                }
            }
            return ((String[]) desiredO)[0];
        }
        else if (desiredO == null)
        {
            return null;
        }
        else
        {
            return desiredO.toString();
        }
    }

    /**
     * Checks if the given key, value pair exists in the given params Map
     *
     * @param params the map of web parameters
     * @param key the name of the parameter to check
     * @param value the value to check for
     * @return true of this key/value pair exists
     */
    public static boolean paramContains(Map params, String key, String value)
    {
        if (getStringParam(params, key, value, key) != null)
        //must check the output is the given value since it may have returned a junk value
        {
            return getStringParam(params, key, value, key).equals(value);
        }
        else
        {
            return false;
        }
    }

    public static Long getLongParam(Map params, String key)
    {
        String s = getStringParam(params, key);

        if (StringUtils.isBlank(s))
        {
            return null;
        }

        try
        {
            return new Long(s);
        }
        catch (NumberFormatException nfe)
        {
            log.warn("NumberFormatException looking for param: " + key);
        }

        // exception or the long value was <= 0
        return null;
    }

    /**
     * Gets a int value from the map and uses the defaultValue if the value can be converted.
     *
     * @param mapOfParameters the map of parameters
     * @param paramName the parameter name to use
     * @param defaultValue the default value in case things cant be converted
     * @return the converted value or the defaultValue if it cant be converted
     */
    public static int getIntParam(Map/*<String,String[]>*/ mapOfParameters, String paramName, int defaultValue)
    {
        String s = getStringParam(mapOfParameters, paramName);

        if (StringUtils.isBlank(s))
        {
            return defaultValue;
        }

        try
        {
            return Integer.valueOf(s);
        }
        catch (NumberFormatException nfe)
        {
            return defaultValue;
        }
    }

    /**
     * Returns a boolean value from the map of parameters.  if it does no exist in the map, then false is returned
     *
     * @param mapOfParameters the map of parameters
     * @param paramName the parameter name to use
     * @return true if the value converted to true or false otherwise
     */
    public static boolean getBooleanParam(Map/*<String,String[]>*/ mapOfParameters, String paramName)
    {
        String s = getStringParam(mapOfParameters, paramName);
        return Boolean.valueOf(s);
    }


    public static Double getDoubleParam(Map params, String key)
    {
        String s = getStringParam(params, key);

        if (StringUtils.isBlank(s))
        {
            return null;
        }

        try
        {
            return new Double(s);
        }
        catch (NumberFormatException nfe)
        {
            log.warn("NumberFormatException looking for param: " + key);
        }

        // exception or the long value was <= 0
        return null;
    }

    /**
     * Convert value to -1 if it is null
     */
    private static Object swapNull(Object o)
    {
        if (o == null)
        {
            return "-1";
        }
        return o;
    }

    /**
     * Convert all null values in a collection into "-1"s.
     */
    public static Collection swapNulls(Collection col)
    {
        if (col == null)
        {
            return null;
        }

        List result = new ArrayList(col.size());

        for (Object aCol : col)
        {
            result.add(swapNull(aCol));
        }

        return result;
    }

    /**
     * Given an array of Strings, return a list of Longs representing the IDs
     */
    public static List<Long> getLongListFromStringArray(String[] ar)
    {
        if (ar == null || ar.length == 0)
        {
            return Collections.emptyList();
        }

        List<Long> result = new ArrayList<Long>(ar.length);

        for (String anAr : ar)
        {
            if (!StringUtils.isBlank(anAr))
            {
                result.add(new Long(anAr));
            }
        }

        return result;
    }

    public static List<String> getListFromStringArray(String[] ar)
    {
        if (ar == null || ar.length == 0)
        {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>(ar.length);

        for (String anAr : ar)
        {
            if (!StringUtils.isBlank(anAr))
            {
                result.add(anAr);
            }
        }

        return result;
    }

    /**
     * Given a collection of entities, return an array of Strings representing the IDs
     */
    public static String[] getStringArrayFromList(Collection entities)
    {
        String[] ar = new String[entities.size()];

        int i = 0;
        for (Iterator iterator = entities.iterator(); iterator.hasNext(); i++)
        {
            GenericValue genericValue = (GenericValue) iterator.next();
            ar[i] = genericValue.getString("id");
        }
        return ar;
    }

    public static String makeCommaSeparated(Long[] longs)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < longs.length; i++)
        {
            result.append(longs[i]);
            if (i < longs.length - 1)
            {
                result.append(",");
            }
        }
        return result.toString();
    }

    /**
     * @deprecated Only used in deprecated method. Since v5.2.
     */
    public static class DateTooEarlyException extends RuntimeException
    {
        public DateTooEarlyException()
        {
            super();
        }

        public DateTooEarlyException(String message)
        {
            super(message);
        }
    }
}
