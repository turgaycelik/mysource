package com.atlassian.jira.event.type;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

/**
 * This class describes the type of event.
 */
@PublicApi
public class EventType
{
    // JIRA Issue Event Type IDs
    // NOTE: If you're adding a new event type you need to create an upgrade task that will add it to EventType table.
    // Order of Events (in relation to each other for display) is defined in EventTypeOrderTransformer 
    public static final Long ISSUE_CREATED_ID = 1L;
    public static final Long ISSUE_UPDATED_ID = 2L;
    public static final Long ISSUE_ASSIGNED_ID = 3L;
    public static final Long ISSUE_RESOLVED_ID = 4L;
    public static final Long ISSUE_CLOSED_ID = 5L;
    public static final Long ISSUE_COMMENTED_ID = 6L;
    public static final Long ISSUE_REOPENED_ID = 7L;
    public static final Long ISSUE_DELETED_ID = 8L;
    public static final Long ISSUE_MOVED_ID = 9L;
    public static final Long ISSUE_WORKLOGGED_ID = 10L;
    public static final Long ISSUE_WORKSTARTED_ID = 11L;
    public static final Long ISSUE_WORKSTOPPED_ID = 12L;
    public static final Long ISSUE_GENERICEVENT_ID = 13L;
    public static final Long ISSUE_COMMENT_EDITED_ID = 14L;
    public static final Long ISSUE_WORKLOG_UPDATED_ID = 15L;
    public static final Long ISSUE_WORKLOG_DELETED_ID = 16L;
    public static final Long ISSUE_COMMENT_DELETED_ID = 17L;

    public static final String JIRA_SYSTEM_EVENT_TYPE = "jira.system.event.type";
    public static final String EVENT_TYPE = "EventType";

    private final Long id;
    private final String name;
    private final String description;
    // The type of the event type  - system or custom
    private final String type;
    // The default template Id to be associated with this event type
    private Long templateId;

    /**
     * Create an Event Type.
     *
     * @param name        name of this event type
     * @param description description of this event type
     * @param templateId  the default template for his event type
     */
    public EventType(String name, String description, Long templateId)
    {
        this(null, name, description, templateId);
    }

    /**
     * Create an Event Type.
     *
     * @param id          id of this event type
     * @param name        name of this event type
     * @param description description of this event type
     * @param templateId  the default template for his event type
     */
    public EventType(Long id, String name, String description, Long templateId)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        // The default template id for this event type
        this.templateId = templateId;
        this.type = null;
    }

    /**
     * Create an Event Type.
     * The given generic value needs to have the following attributes set:
     * <ul>
     * <li>id (Long)</li>
     * <li>name (String)</li>
     * <li>description (String)</li>
     * <li>type (String)</li>
     * <li>templateId (Long)</li>
     * </ul>
     *
     * @param eventTypeGV generic value
     */
    public EventType(GenericValue eventTypeGV)
    {
        this.id = eventTypeGV.getLong("id");
        this.name = eventTypeGV.getString("name");
        this.description = eventTypeGV.getString("description");
        this.type = eventTypeGV.getString("type");
        this.templateId = eventTypeGV.getLong("templateId");
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getType()
    {
        return type;
    }

    /**
     * Retrieve the default template id associated with this event type. <br/> If the event type is not associated with
     * a default template, return the event type id as the template id. In this case, the event type id should match
     * with a suitable template for this event. <br/> Refer to email-template-id-mappings.xml and
     * upgrade-system-event-types.xml.
     *
     * @return the default template id associated with this event type. The event type id is returned if no template id
     *         has been selected.
     */
    public Long getTemplateId()
    {
        return templateId == null ? id : templateId;
    }

    /**
     * Set the default template id to be associated with this event type.
     *
     * @param templateId template id
     */
    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public boolean isSystemEventType()
    {
        return type != null && type.equals(JIRA_SYSTEM_EVENT_TYPE);
    }

    public String getNameKey()
    {
        return "event.type." + getName().replace(" ", "").toLowerCase() + ".name";
    }

    public String getDescKey()
    {
        return "event.type." + getName().replace(" ", "").toLowerCase() + ".desc";
    }

    /**
     * Allows i18n keys to be specified in the properties file to allow translation of the event type name.
     *
     * @param remoteUser current user
     * @return String   a i18n name or the original event type name
     */
    public String getTranslatedName(User remoteUser)
    {
        String translatedName = getI18nBean(remoteUser).getText(getNameKey());

        return (TextUtils.stringSet(translatedName) && !translatedName.equals(getNameKey())) ? translatedName : name;
    }

    /**
     * Allows i18n keys to be specified in the properties file to allow translation of the event type description.
     *
     * @param remoteUser current user
     * @return String   an i18n description or the original event type description
     */
    public String getTranslatedDesc(User remoteUser)
    {
        String translatedDesc = getI18nBean(remoteUser).getText(getDescKey());

        return (TextUtils.stringSet(translatedDesc) && !translatedDesc.equals(getDescKey())) ? translatedDesc : description;
    }

    private I18nHelper getI18nBean(User remoteUser)
    {
        return ComponentAccessor.getI18nHelperFactory().getInstance(remoteUser);
    }

}
