/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A simple class store methods we want to expose to velocity templates
 */
public class JiraVelocityHelper
{
    private static final Logger log = LoggerFactory.getLogger(JiraVelocityHelper.class);

    private static final String lSep = JiraSystemProperties.getInstance().getProperty("line.separator");
    private static final int CHANGELOG_WHITESPACE_INDENT = 4; // Changelogs will be indented by this amount of space

    private final FieldManager fieldManager;

    public JiraVelocityHelper(final FieldManager fieldManager)
    {
        this.fieldManager = fieldManager;
    }

    @HtmlSafe
    public String urlencode(final String s) throws UnsupportedEncodingException
    {
        return URLEncoder.encode(s, ComponentAccessor.getApplicationProperties().getEncoding());
    }

    public String quote(final String in)
    {
        return indentTextBlock(in, "> ", true);
    }

    public String quoteExceptFirst(final String in)
    {
        return indentTextBlock(in, "> ", false);
    }

    public String indentTextBlock(final String in, final String indentStr, final boolean quoteFirst)
    {
        if ((in == null) || (in.length() == 0))
        {
            return "";
        }
        final StringBuilder out = new StringBuilder(in.length() * 2);
        final StringTokenizer tok = new StringTokenizer(in, "\n\r");
        boolean first = true;
        while (tok.hasMoreElements())
        {
            final String line = (String) tok.nextElement();
            if (first)
            {
                out.append((quoteFirst ? indentStr : "") + line);
                first = false;
            }
            else
            {
                out.append(indentStr + line);
            }
            if (tok.hasMoreElements())
            {
                out.append(lSep);
            }
        }
        return out.toString();
    }

    /** Returns Internationalized human-readable name of a field, or the key (usually also readable) if none was found. */
    public String getFieldName(final String fieldKey, final I18nHelper i18n)
    {
        final Field field = fieldManager.getField(fieldKey);
        if (field == null)
        {
            return fieldKey;
        }
        return i18n.getText(field.getNameKey());
    }

    public String getFieldName(final GenericValue changeItem, final I18nHelper i18n)
    {
        final String field = changeItem.getString("field");
        if (changeItem.getString("fieldtype").toLowerCase().equals("custom"))
        {
            return field;
        }
        else
        {
            final String fieldKey = StringUtils.replaceAll(field, " ", "").toLowerCase();
            return i18n.getText("issue.field." + fieldKey);
        }
    }

    /** For 'Move Issue' events; returns the issue's old key, extracted from the changelog. */
    public String getOldKey(final GenericValue changelog) throws GenericEntityException
    {
        final List<GenericValue> changeItems = changelog.getRelated("ChildChangeItem");
        for (final GenericValue changeItem : changeItems)
        {
            final String fieldKey = changeItem.getString("field");
            if ("Key".equals(fieldKey))
            {
                return changeItem.getString("oldstring");
            }
        }
        return "";
    }

    /** For 'Move Issue' events; returns the issue's new key, extracted from the changelog. */
    public String getNewKey(final GenericValue changelog) throws GenericEntityException
    {
        final List<GenericValue> changeItems = changelog.getRelated("ChildChangeItem");
        for (final GenericValue changeItem : changeItems)
        {
            final String fieldKey = changeItem.getString("field");
            if ("Key".equals(fieldKey))
            {
                return changeItem.getString("newstring");
            }
        }
        return "";
    }

    public String printChangelog(final GenericValue changelog, final I18nHelper i18n, final Collection<String> ignoredFields) throws GenericEntityException
    {
        return printChangelog(changelog, i18n, ignoredFields, true);
    }

    public String printChangelog(final GenericValue changelog, final I18nHelper i18n, final Collection<String> ignoredFields, final boolean ignoreStatus) throws GenericEntityException
    {
        final List<String> ignoredFieldList = new ArrayList<String>(ignoredFields);
        if (ignoreStatus)
        {
            ignoredFieldList.add("status");
        }
        final List<GenericValue> changeItems = changelog.getRelated("ChildChangeItem");
        final int maxFieldLength = getMaxFieldLength(changeItems, i18n);
        final StringBuilder result = new StringBuilder();
        String fieldKey = "";
        String prevFieldKey = "undefined";
        for (final GenericValue changeItem : changeItems)
        {
            prevFieldKey = fieldKey;
            fieldKey = changeItem.getString("field");
            String oldStr = changeItem.getString("oldstring");
            String newStr = changeItem.getString("newstring");

            // JRA-13353: When the Comment field is included in the Changelog here, it is being deleted as there are
            // separate events for comments being created and updated. In this case, the original comment text is stored
            // in the "oldvalue" field and not "oldstring"
            boolean isCommentDeleted = false;
            if ("Comment".equals(fieldKey))
            {
                isCommentDeleted = true;
                oldStr = changeItem.getString("oldvalue");
                newStr = changeItem.getString("newvalue");
            }

            // Sort out the actual display values for old & new strings

            oldStr = getPrettyFieldString(fieldKey, oldStr, i18n);
            newStr = getPrettyFieldString(fieldKey, newStr, i18n);

            String fieldValue;
            if (ignoredFieldList.contains(fieldKey))
            {
                continue;
            }
            if ((oldStr != null) && (newStr != null))
            {
                if (multiLine(newStr))
                {
                    fieldValue = lSep + newStr + lSep + lSep + "  " + i18n.getText("template.changelog.was") + ":" + (multiLine(oldStr) ? lSep : "") + oldStr + lSep;
                }
                else
                {
                    fieldValue = newStr + "  (" + i18n.getText("template.changelog.was") + ": " + oldStr + ")";
                }
            }
            else if (oldStr != null)
            {
                if (isCommentDeleted)
                {
                    fieldValue = i18n.getText("template.changelog.was.deleted") + "\n\n(" + i18n.getText("template.changelog.was") + ": " + oldStr + ")";
                }
                else
                {
                    fieldValue = "    (" + i18n.getText("template.changelog.was") + ": " + oldStr + ")";
                }
            }
            else if (newStr != null)
            {
                fieldValue = (multiLine(newStr) ? lSep : "") + newStr;
            }
            else
            {
                fieldValue = i18n.getText("template.changelog.was.deleted");
            }
            final String fieldName = getFieldName(changeItem, i18n);
            //We only want to print the fieldname on the first row of a multi-value change
            if (!prevFieldKey.equals(fieldKey))
            {
                for (int i = (0 - CHANGELOG_WHITESPACE_INDENT); i < (maxFieldLength - fieldName.length()); i++)
                {
                    result.append(" ");
                }
                result.append(fieldName).append(": ");
            }
            else
            {
                for (int i = (0 - CHANGELOG_WHITESPACE_INDENT); i < (maxFieldLength); i++)
                {
                    result.append(" ");
                }
                result.append("  "); // for the ': '
            }
            result.append(fieldValue).append(lSep);
        }
        return result.toString();
    }

    public boolean wasDeleted(final GenericValue changelog, final String fieldKey, final I18nHelper i18n) throws GenericEntityException
    {
        if (changelog != null)
        {
            final List<GenericValue> changeItems = changelog.getRelated("ChildChangeItem");
            for (final GenericValue changeItem : changeItems)
            {
                final String field = changeItem.getString("field");
                if (field.equals(fieldKey))
                {
                    final String oldStr = getPrettyFieldString(fieldKey, changeItem.getString("oldstring"), i18n);
                    final String newStr = getPrettyFieldString(fieldKey, changeItem.getString("newstring"), i18n);

                    if ((oldStr == null) && (newStr == null))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getPrettyFieldString(final String fieldKey, String str, final I18nHelper i18n)
    {
        if (fieldManager.isNavigableField(fieldKey))
        {
            final NavigableField field = fieldManager.getNavigableField(fieldKey);
            if (field != null)
            {
                str = field.prettyPrintChangeHistory(str, i18n);
            }
        }
        else
        {
            log.debug("FieldKey {} is an invalid field. No translation of change history will occur", fieldKey);
        }
        
        return str;
    }

    public String getPrettyFieldString(final String fieldKey, final String str, final I18nHelper i18n, final String defaultIfNull)
    {
        return getPrettyFieldString(fieldKey, str, i18n) != null ? getPrettyFieldString(fieldKey, str, i18n) : defaultIfNull;
    }

    public String indentToChangelog(final String fieldName, final String fieldValue, final GenericValue changelog, final I18nHelper i18n) throws GenericEntityException
    {
        final StringBuilder result = new StringBuilder();
        int maxFieldLength;
        if (changelog != null)
        {
            final List<GenericValue> changeItems = changelog.getRelated("ChildChangeItem");
            maxFieldLength = getMaxFieldLength(changeItems, i18n);
        }
        else
        {
            maxFieldLength = 10;
        }
        for (int i = (0 - CHANGELOG_WHITESPACE_INDENT); i < (maxFieldLength - fieldName.length()); i++)
        {
            result.append(" ");
        }
        result.append(fieldName).append(": ").append(fieldValue).append(lSep);
        return result.toString();
    }

    public String indentToChangelogNoLineSep(final String fieldName, final String fieldValue, final I18nHelper i18n, final List<String> allFieldNames) throws GenericEntityException
    {
        final StringBuilder result = new StringBuilder();
        int indent;
        if (allFieldNames != null)
        {
            indent = getMaxFieldLength(allFieldNames);
        }
        else
        {
            indent = 10;
        }

        for (int i = (0 - CHANGELOG_WHITESPACE_INDENT); i < (indent - fieldName.length()); i++)
        {
            result.append(" ");
        }
        result.append(fieldName).append(": ").append(fieldValue);
        return result.toString();
    }

    /** Each ChangeItem consists of a field and a value; here, return the longest human-readable field name. */
    private int getMaxFieldLength(final List<GenericValue> changeItems, final I18nHelper i18n)
    {
        return getMaxFieldLength(Transformed.list(changeItems, new Function<GenericValue, String>()
        {
            public String get(final GenericValue changeItem)
            {
                return getFieldName(changeItem, i18n);
            }
        }));
    }

    private int getMaxFieldLength(final List<String> fieldNames)
    {
        int maxFieldLength = 0;
        for (final String fieldName : fieldNames)
        {
            if (fieldName.length() > maxFieldLength)
            {
                maxFieldLength = fieldName.length();
            }
        }
        return maxFieldLength;
    }

    private boolean multiLine(final String newStr)
    {
        return (newStr.indexOf('\r') != -1) || (newStr.indexOf('\n') != -1);
    }

    /**
     * Creates a new, mutable, empty map for use in velocity
     */
    public Map newMap()
    {
        return new LinkedHashMap();
    }

    /**
     * Creates a new, mutable, map for use in velocity. The map will contain one entry (k1, v1).
     */
    public Map newMap(Object k1, Object v1)
    {
        Map map = new LinkedHashMap();
        map.put(k1, v1);
        return map;
    }

    /**
     * Creates a new, mutable, map for use in velocity. The map will contain two entries (k1, v1) and (k2, v2).
     */
    public Map newMap(Object k1, Object v1, Object k2, Object v2)
    {
        Map map = new LinkedHashMap();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public String removeHtmlBreaks(String str)
    {
        str = StringUtils.replaceAll(str, "<br>", "");
        str = StringUtils.replaceAll(str, "<br/>", "");
        str = StringUtils.replaceAll(str, "<br />", "");
        str = StringUtils.replaceAll(str, "<p>", "");
        str = StringUtils.replaceAll(str, "</p>", "");
        return str;
    }

    public String removeHtmlTags(final String str)
    {
        // It might be a good future improvement to only remove tags for block level elements.
        return RegexpUtils.replaceAll(str, "<[^>]*>", " ");
    }
}
