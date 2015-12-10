package com.atlassian.configurable;


/**
 * Contains a list of possible Object Configuration Types that can be used.
 */
public class ObjectConfigurationTypes
{
    public static final int TYPE_UNKNOWN = -1;
    public static final int STRING = 0;
    public static final int LONG = 1;
    public static final int SELECT = 2;
    public static final int HIDDEN = 3;
    public static final int DATE = 4;
    public static final int USER = 5;
    public static final int GROUP = 6;
    public static final int TEXT = 7;
    public static final int MULTISELECT = 8;
    public static final int CHECKBOX = 9;
    public static final int CASCADINGSELECT = 10;
    public static final int FILTERPICKER = 11; //hacky
    public static final int FILTERPROJECTPICKER = 12; //hackier

    /**
     * Takes a string representation of a type and retrieves it statis identifier.
     *
     * @param typeStr Type in string format e.g. String
     * @return constant value for typeStr or -1 if unknown.
     */
    public static int getType(String typeStr)
    {
        if (typeStr.toLowerCase().startsWith("string"))
        {
            return STRING;
        }
        else if (typeStr.toLowerCase().startsWith("long"))
        {
            return LONG;
        }
        else if (typeStr.toLowerCase().startsWith("select"))
        {
            return SELECT;
        }
        else if (typeStr.toLowerCase().startsWith("hidden"))
        {
            return HIDDEN;
        }
        else if (typeStr.toLowerCase().startsWith("date"))
        {
            return DATE;
        }
        else if (typeStr.toLowerCase().startsWith("user"))
        {
            return USER;
        }
        else if (typeStr.toLowerCase().startsWith("group"))
        {
            return GROUP;
        }
        else if (typeStr.toLowerCase().startsWith("text"))
        {
            return TEXT;
        }
        else if (typeStr.toLowerCase().startsWith("multiselect"))
        {
            return MULTISELECT;
        }
        else if (typeStr.toLowerCase().startsWith("checkbox"))
        {
            return CHECKBOX;
        }
        else if (typeStr.toLowerCase().startsWith("cascadingselect"))
        {
            return CASCADINGSELECT;
        }
        else if (typeStr.toLowerCase().startsWith("filterpicker"))
        {
            return FILTERPICKER;
        }
        else if (typeStr.toLowerCase().startsWith("filterprojectpicker"))
        {
            return FILTERPROJECTPICKER;
        }
        else
        {
            return TYPE_UNKNOWN;
        }
    }
}
