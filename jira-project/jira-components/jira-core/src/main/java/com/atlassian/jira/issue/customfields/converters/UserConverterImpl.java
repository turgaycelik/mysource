package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringUtils;

public class UserConverterImpl implements UserConverter
{
    private final UserManager userManager;
    private final I18nHelper i18nHelper;

    public UserConverterImpl(UserManager userManager, I18nHelper i18nHelper)
    {
        this.userManager = userManager;
        this.i18nHelper = i18nHelper;
    }

    @Override
    public String getString(User user)
    {
        if (user == null)
        {
            return "";
        }
        return user.getName();
    }

    @Override
    public String getHttpParameterValue(ApplicationUser user)
    {
        if (user == null)
        {
            return "";
        }
        return user.getName();
    }

    @Override
    public String getDbString(ApplicationUser user)
    {
        if (user == null)
        {
            return "";
        }
        return user.getKey();
    }

    @Override
    public User getUser(String stringValue) throws FieldValidationException
    {
        if (StringUtils.isBlank(stringValue))
        {
            return null;
        }
        User user = userManager.getUser(stringValue);
        if (user == null)
        {
            // For backward compatibility
            throw new FieldValidationException(i18nHelper.getText("user.picker.errors.usernotfound", stringValue));
        }
        return user;
    }

    @Override
    public User getUserEvenWhenUnknown(String stringValue) throws FieldValidationException
    {
        if (StringUtils.isBlank(stringValue))
        {
            return null;
        }
        return userManager.getUserEvenWhenUnknown(stringValue);
    }

    @Override
    public ApplicationUser getUserFromHttpParameterWithValidation(String stringValue) throws FieldValidationException
    {
        if (StringUtils.isBlank(stringValue))
        {
            return null;
        }
        ApplicationUser user = userManager.getUserByName(stringValue);
        if (user == null)
        {
            // Validation
            throw new FieldValidationException(i18nHelper.getText("user.picker.errors.usernotfound", stringValue));
        }
        return user;
    }

    @Override
    public ApplicationUser getUserFromDbString(String stringValue)
    {
        if (StringUtils.isBlank(stringValue))
        {
            return null;
        }
        return userManager.getUserByKeyEvenWhenUnknown(stringValue);
    }

    @Override
    public User getUserObject(String stringValue) throws FieldValidationException
    {
        return getUser(stringValue);
    }
}
