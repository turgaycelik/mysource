package com.atlassian.jira.sharing;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Some utility functions for dealing with {@link com.atlassian.jira.sharing.SharePermission} instances.
 * <p>
 * TODO move to UI package
 * 
 * @since v3.13
 */
@PublicApi
public class SharePermissionUtils
{
    private static final String TYPE_KEY = "type";
    private static final String PARAM1_KEY = "param1";
    private static final String PARAM2_KEY = "param2";

    /**
     * Convert the passed permission into a JSON object.
     * 
     * @param permission the permission to convert.
     * @return A JSON object representing the permission.
     * @throws JSONException if an error occurs while creating the JSON object.
     */
    public static JSONObject toJson(final SharePermission permission) throws JSONException
    {
        notNull("permission", permission);
        final JSONObject object = new JSONObject();
        object.put(SharePermissionUtils.TYPE_KEY, permission.getType());
        if (permission.getParam1() != null)
        {
            object.put(SharePermissionUtils.PARAM1_KEY, permission.getParam1());

            if (permission.getParam2() != null)
            {
                object.put(SharePermissionUtils.PARAM2_KEY, permission.getParam2());
            }
        }
        return object;
    }

    /**
     * Converts the passed SharePermissions into a JSON array.
     * 
     * @param permissions the permissions to convert.
     * @return the JSON array.
     * @throws JSONException if an error occurs while creating the JSONArray.
     */
    public static JSONArray toJsonArray(final Collection<SharePermission> permissions) throws JSONException
    {
        notNull("permission", permissions);
        final JSONArray array = new JSONArray();
        for (final SharePermission sharePermission : permissions)
        {
            array.put(SharePermissionUtils.toJson(sharePermission));
        }
        return array;
    }

    /**
     * Return a SharePermission from a JSON object.
     * 
     * @param json the JSON object to convert.
     * @return the converted SharePermission.
     * @throws JSONException if an error occurs while converting the object.
     */
    public static SharePermission fromJsonObject(final JSONObject json) throws JSONException
    {
        notNull("json", json);
        if (!json.has(SharePermissionUtils.TYPE_KEY))
        {
            throw new JSONException("Invalid JSON SharePermission: No type passed.");
        }
        final ShareType.Name type;
        try
        {
            type = new ShareType.Name(json.getString(SharePermissionUtils.TYPE_KEY));
        }
        catch (final IllegalArgumentException e)
        {
            throw new JSONException(e);
        }

        String param1 = null;
        String param2 = null;
        if (json.has(SharePermissionUtils.PARAM1_KEY))
        {
            param1 = json.getString(SharePermissionUtils.PARAM1_KEY);

            if (StringUtils.isBlank(param1))
            {
                throw new JSONException("Invalid JSON SharePermission: 'param1' cannot be blank.");
            }

            if (json.has(SharePermissionUtils.PARAM2_KEY))
            {
                param2 = json.getString(SharePermissionUtils.PARAM2_KEY);
                if (StringUtils.isBlank(param2))
                {
                    throw new JSONException("Invalid JSON SharePermission: 'param2' cannot be blank.");
                }
            }
        }
        else if (json.has(SharePermissionUtils.PARAM2_KEY))
        {
            throw new JSONException("Invalid JSON SharePermission: 'param2' passed without 'param1'.");
        }
        return new SharePermissionImpl(type, param1, param2);
    }

    /**
     * Create a SharePermission from the passed JSON string.
     * 
     * @param jsonString the JSON string make a SharePermission from.
     * @return the new SharePermission.
     * @throws JSONException if an error occurs while converting the object.
     */
    public static SharePermission fromJsonObjectString(final String jsonString) throws JSONException
    {
        notNull("jsonString", jsonString);
        if (StringUtils.isBlank(jsonString))
        {
            return null;
        }
        return SharePermissionUtils.fromJsonObject(new JSONObject(jsonString));
    }

    /**
     * Convert the passed array into SharePermissions.
     * 
     * @param array the JSON array to convert.
     * @return the converted SharePermission.
     * @throws JSONException if an error occurs during convertion.
     */
    public static SharePermissions fromJsonArray(final JSONArray array) throws JSONException
    {
        notNull("array", array);
        final Set<SharePermission> permissions = new HashSet<SharePermission>(array.length());
        for (int i = 0; i < array.length(); i++)
        {
            permissions.add(SharePermissionUtils.fromJsonObject(array.getJSONObject(i)));
        }
        return new SharePermissions(permissions);
    }

    public static SharePermissions fromJsonArrayString(final String jsonString) throws JSONException
    {
        notNull("jsonString", jsonString);

        if (StringUtils.isBlank(jsonString))
        {
            return SharePermissions.PRIVATE;
        }
        else
        {
            return SharePermissionUtils.fromJsonArray(new JSONArray(jsonString));
        }
    }

    // /CLOVER:OFF
    private SharePermissionUtils()
    {
    // make sure this object is not created.
    }
    // /CLOVER:ON
}
