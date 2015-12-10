package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalChangeItem;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.13
 */
public class ChangeItemParserImpl implements ChangeItemParser
{
    private static final String ID = "id";
    private static final String GROUP = "group";
    private static final String FIELD_TYPE = "fieldtype";
    private static final String FIELD = "field";
    private static final String OLD_VALUE = "oldvalue";
    private static final String OLD_STRING = "oldstring";
    private static final String NEW_VALUE = "newvalue";
    private static final String NEW_STRING = "newstring";

    public ExternalChangeItem parse(final Map attributes) throws ParseException
    {
        Null.not("attributes", attributes);
        //<ChangeItem id="10014" group="10006" fieldtype="jira" field="security" oldvalue="10000" oldstring="level1" newvalue="10001" newstring="level2"/>

        final String id = (String) attributes.get(ID);
        final String changeGroupId = (String) attributes.get(GROUP);
        final String fieldType = (String) attributes.get(FIELD_TYPE);
        final String field = (String) attributes.get(FIELD);
        final String oldvalue = (String) attributes.get(OLD_VALUE);
        final String oldstring = (String) attributes.get(OLD_STRING);
        final String newvalue = (String) attributes.get(NEW_VALUE);
        final String newstring = (String) attributes.get(NEW_STRING);

        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("A change item must have an id specified.");
        }
        if (StringUtils.isEmpty(changeGroupId))
        {
            throw new ParseException("Change item '" + id + "' is missing the change group id.");
        }
        if (StringUtils.isEmpty(field))
        {
            throw new ParseException("Change item '" + id + "' is missing the 'field' attribute.");
        }

        return new ExternalChangeItem(id, changeGroupId, fieldType, field, oldvalue, oldstring, newvalue, newstring);
    }

    public EntityRepresentation getEntityRepresentation(final ExternalChangeItem changeItem)
    {
        final Map attributes = new HashMap();
        attributes.put(ID, changeItem.getId());
        attributes.put(GROUP, changeItem.getChangeGroupId());
        attributes.put(FIELD_TYPE, changeItem.getFieldType());
        attributes.put(FIELD, changeItem.getField());
        attributes.put(OLD_VALUE, changeItem.getOldValue());
        attributes.put(OLD_STRING, changeItem.getOldString());
        attributes.put(NEW_VALUE, changeItem.getNewValue());
        attributes.put(NEW_STRING, changeItem.getNewString());

        return new EntityRepresentationImpl(ChangeItemParser.CHANGE_ITEM_ENTITY_NAME, attributes);
    }
}
