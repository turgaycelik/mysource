package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class TestUpgradeTask_Build554
{
    @Test
    public void testDoUpgrade() throws Exception
    {
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);
        final AvatarManager mockAvatarManager = createMock(AvatarManager.class);

        final Avatar mockAvatar = createMock(Avatar.class);
        final Avatar mockCreatedAvatar = createMock(Avatar.class);
        expect(mockCreatedAvatar.getId()).andReturn(9999L);
        expect(mockAvatar.getId()).andReturn(12323L);
        expect(mockAvatarManager.getAllSystemAvatars(Avatar.Type.USER)).andReturn(CollectionBuilder.list(mockAvatar));
        expect(mockAvatarManager.delete(12323L)).andReturn(true);

        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("angel.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("businessman.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("businessman2.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("devil.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("doctor.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("dude1.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("dude2.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("dude3.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("dude4.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("dude5.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("ghost.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("security_agent.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("user1.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("user2.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("user3.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("user_headphones.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("userprofile_silhouette.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);

        mockApplicationProperties.setString("jira.avatar.user.default.id", 9999L + "");
        replay(mockApplicationProperties, mockAvatarManager, mockAvatar, mockCreatedAvatar);

        UpgradeTask_Build554 upgradeTask = new UpgradeTask_Build554(mockAvatarManager, mockApplicationProperties);

        upgradeTask.doUpgrade(false);

        verify(mockApplicationProperties, mockAvatarManager,mockAvatar, mockCreatedAvatar);
    }

    @Test
    public void testMetaData()
    {
        UpgradeTask_Build554 upgradeTask = new UpgradeTask_Build554(null, null);
        assertEquals("554", upgradeTask.getBuildNumber());
        assertEquals("Creates system user avatars.", upgradeTask.getShortDescription());
    }
}
