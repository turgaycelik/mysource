package com.atlassian.jira.rest.v2.issue;

import java.util.List;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarPickerHelperImpl;
import com.atlassian.jira.user.ApplicationUser;

import com.google.common.collect.Lists;

import static com.atlassian.jira.rest.v2.avatar.AvatarUrls.getAvatarURLs;

/**
 * @since v5.0
 */
public class AvatarBeanFactory
{
    /**
     * Create a list of AvatarBeans given the passed List of Avatar domain objects.
     *
     * @return the List of AvatarBeans from the passed List of Avatar domain objects.
     */
    public static List<AvatarBean> createAvatarBeans(final List<Avatar> avatars, final ApplicationUser avatarUser)
    {
        List<AvatarBean> beans = Lists.newArrayList();
        for (Avatar avatar : avatars)
        {
            beans.add(new AvatarBean(avatar.getId().toString(), avatar.getOwner(), avatar.isSystemAvatar(), getAvatarURLs(avatarUser, avatar)));
        }

        return beans;
    }

    /**
     * Creates an AvatarBean from Avatar domain object
     *
     * @param avatar - domain object
     * @return avatar bean
     */
    public static AvatarBean createAvatarBean(final Avatar avatar)
    {
        return new AvatarBean(avatar.getId().toString(), avatar.getOwner());
    }

    /**
     * Gets cropping instructions for temporary avatar
     *
     * @param temporaryAvatarBean temporary avatar representation
     * @return cropping instructions for temporary avatar
     */
    public static AvatarCroppingBean createTemporaryAvatarCroppingInstructions(final AvatarPickerHelperImpl.TemporaryAvatarBean temporaryAvatarBean)
    {
        return new AvatarCroppingBean(temporaryAvatarBean.getUrl(), temporaryAvatarBean.getCropperWidth(),
                temporaryAvatarBean.getCropperOffsetY(), temporaryAvatarBean.getCropperOffsetX(),
                temporaryAvatarBean.isCroppingNeeded());
    }

}
