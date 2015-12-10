package com.atlassian.jira.plugin.userformat;

import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.plugin.userformat.configuration.UserFormatTypeConfiguration;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatModuleDescriptors;
import com.atlassian.jira.user.UserKeyService;
import org.apache.log4j.Logger;

/**
 * Builds instances of {@link UserFormat} for an specified user format type.
 *
 * @since v4.4
 * @see com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor
 */
public class DefaultUserFormats implements UserFormats
{
    private Logger log = Logger.getLogger(DefaultUserFormats.class);
    private final UserFormatTypeConfiguration userFormatTypeConfiguration;
    private final UserFormatModuleDescriptors userFormatModuleDescriptors;
    private final UserKeyService userKeyService;

    public DefaultUserFormats(final UserFormatTypeConfiguration userFormatTypeConfiguration,
            final UserFormatModuleDescriptors userFormatModuleDescriptors, UserKeyService userKeyService)
    {
        this.userFormatTypeConfiguration = userFormatTypeConfiguration;
        this.userFormatModuleDescriptors = userFormatModuleDescriptors;
        this.userKeyService = userKeyService;
    }

    @Override
    public UserFormat forType(final String type)
    {
        if(userFormatTypeConfiguration.containsType(type))
        {
            final String moduleKey = userFormatTypeConfiguration.getUserFormatKeyForType(type);
            if (userFormatModuleDescriptors.withKey(moduleKey) != null)
            {
                return new CachingUserFormat(userFormatModuleDescriptors.withKey(moduleKey).getModule());
            }
            else
            {
                // the module or plugin that contains it might be disabled or in the process of being upgraded.
                log.info(String.format("Disabled user format found mapped to type '%s'. Falling back to system default.", type));
                return new CachingUserFormat(defaultUserFormatForType(type));
            }
        }
        else // A non-configured type
        {
            final UserFormat defaultUserFormatForType = defaultUserFormatForType(type);
            if (defaultUserFormatForType != null)
            {
                log.info(
                        String.format
                                (
                                        "Mapping user format type '%s' to the default format in module with key '%s'",
                                        type, userFormatModuleDescriptors.defaultFor(type).getCompleteKey()
                                )
                );
                userFormatTypeConfiguration.setUserFormatKeyForType(type, userFormatModuleDescriptors.defaultFor(type).getCompleteKey());
            }
            return new CachingUserFormat(defaultUserFormatForType);
        }
    }

    @Override
    public UserFormatter formatter(String type)
    {
        return new UserFormatterImpl(forType(type), userKeyService);
    }

    private UserFormat defaultUserFormatForType(final String type)
    {
        final UserFormatModuleDescriptor defaultDescriptorForType = userFormatModuleDescriptors.defaultFor(type);
        if (defaultDescriptorForType != null)
        {
            return defaultDescriptorForType.getModule();
        }
        else
        {
            log.error(String.format("Falling back to a default user format for type: %s FAILED.", type));
            return null;
        }
    }
}
