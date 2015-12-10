package com.atlassian.jira.util.json;

import javax.annotation.Nullable;

/**
 * JSON related utils. Not in JIRA API, as I didn't want to introduce there dependency on Jackson.
 *
 * @since v5.0
 */
public class JsonUtil
{
    /**
     * Util for transforming objects of any type (including arrays, standard primitive wrapper types, beans) into a
     * valid JSON string, which can be directly embedded in &lt;script> section in HTML page. Because parsing of
     * &lt;script> is immediately terminated by web browsers when &lt;/ sequence is found (usually &lt;/script>
     * marks the end of script section, but browsers are lazy) this method also escapes each "&lt;/" into "&lt;\/"
     *
     * @param o object to convert to JSON string, for null you will get "null" string (without qutoes)
     * @return object converted to its JSON representation
     */
    public static String toJsonString(@Nullable Object o)
    {
        try
        {
            return JSONObject.valueToString(o).replace("</", "<\\/");
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }
}
