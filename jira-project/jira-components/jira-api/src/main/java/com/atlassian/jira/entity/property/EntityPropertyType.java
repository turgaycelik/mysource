package com.atlassian.jira.entity.property;

import com.atlassian.annotations.ExperimentalApi;

import java.util.HashMap;
import java.util.Map;

/**
 * Type of the entity property.
 *
 * @since v6.2
 */
@ExperimentalApi
public class EntityPropertyType
{
    public static final EntityPropertyType REMOTE_VERSION_LINK= new EntityPropertyType("RemoteVersionLink", "issue.remoteissuelink", "REMOTEVERLINKPROP", "issue.remoteissuelink");
    public static final EntityPropertyType ISSUE_PROPERTY = new EntityPropertyType("IssueProperty", "common.concepts.issue", "ISSUEPROP", "issue.property");
    public static final EntityPropertyType PROJECT_PROPERTY = new EntityPropertyType("ProjectProperty", "common.concepts.project", "PROJECTPROP", "project.property");
    public static final EntityPropertyType COMMENT_PROPERTY = new EntityPropertyType("CommentProperty", "common.concepts.comment", "COMMENTPROP", "comment.property");
    public static final EntityPropertyType CHANGE_HISTORY_PROPERTY = new EntityPropertyType("ChangeHistoryProperty", "common.concepts.changehistory", "CHANGEHISTORYPROP", "changehistory.property");

    private final String dbEntityName;
    private final String i18nKeyForEntityName;
    private final String indexPrefix;
    private final String jqlName;
    private static final Map<String,EntityPropertyType> jqlClauseToProperty = new HashMap<String, EntityPropertyType>();

    static
    {
        jqlClauseToProperty.put(ISSUE_PROPERTY.getJqlName(),ISSUE_PROPERTY);
    }

    public EntityPropertyType(final String dbEntityName, final String i18nKeyForEntityName, final String indexPrefix, final String jqlName)
    {
        this.dbEntityName = dbEntityName;
        this.i18nKeyForEntityName = i18nKeyForEntityName;
        this.indexPrefix = indexPrefix;
        this.jqlName = jqlName;
    }

    public String getDbEntityName()
    {
        return dbEntityName;
    }

    public String getI18nKeyForEntityName()
    {
        return i18nKeyForEntityName;
    }

    public String getJqlName()
    {
        return jqlName;
    }

    public String getIndexPrefix()
    {
        return indexPrefix;
    }

    public static boolean isJqlClause(final String clauseName)
    {
        return clauseName!=null && jqlClauseToProperty.containsKey(clauseName);
    }
    public static EntityPropertyType getEntityPropertyTypeForClause(final String clauseName)
    {
        return jqlClauseToProperty.get(clauseName);
    }
}