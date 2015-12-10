package com.atlassian.jira.entity;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.user.ApplicationUserEntity;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v6.0
 */
public class ApplicationUserEntityFactory extends AbstractEntityFactory<ApplicationUserEntity>
{
    public static final String ID = "id";
    public static final String USER_KEY = "userKey";
    public static final String LOWER_USER_NAME = "lowerUserName";

    @Override
    public String getEntityName()
    {
        return "ApplicationUser";
    }

    @Override
    public ApplicationUserEntity build(GenericValue genericValue)
    {
        final Long id = genericValue.getLong(ID);
        final String key = genericValue.getString(USER_KEY);
        final String username = genericValue.getString(LOWER_USER_NAME);
        return new ApplicationUserEntity(id, key, username);
    }

    @Override
    public FieldMap fieldMapFrom(ApplicationUserEntity value)
    {
        return FieldMap.build(ID, value.getId())
                .add(USER_KEY, value.getKey())
                .add(LOWER_USER_NAME, value.getUsername());
    }
}
