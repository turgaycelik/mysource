package com.atlassian.jira.upgrade.tasks;

import java.util.Arrays;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizPropertyEntryStore;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.propertyset.OfBizPropertyEntryStore;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.collect.CollectionBuilder.list;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUpgradeTask_Build608
{
    @Rule
    public MockitoContainer container = MockitoMocksInContainer.rule(this);

    @SuppressWarnings("unused")
    @AvailableInContainer
    private OfBizPropertyEntryStore ofBizPropertyEntryStore = new MockOfBizPropertyEntryStore();


    @Test
    public void testMetaData()
    {
        final UpgradeTask_Build608 upgradeTask = new UpgradeTask_Build608(null, null, null, null);
        assertEquals("608", upgradeTask.getBuildNumber());
        assertEquals("Updating system user avatars.", upgradeTask.getShortDescription());
    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        final ApplicationProperties mockApplicationProperties = mock(ApplicationProperties.class);
        final AvatarManager mockAvatarManager = mock(AvatarManager.class);
        final OfBizDelegator mockOfBizDelegator = mock(OfBizDelegator.class);
        final UserPropertyManager mockUserPropertyManager = mock(UserPropertyManager.class);

        final Avatar mockAvatar = mock(Avatar.class);
        final Avatar mockCreatedAvatar = mock(Avatar.class);
        final Avatar mockAnonymousAvatar = mock(Avatar.class);
        when(mockCreatedAvatar.getId()).thenReturn(9999L);
        when(mockAvatar.getId()).thenReturn(12323L);
        when(mockAnonymousAvatar.getId()).thenReturn(7777L);
        when(mockAvatarManager.getAllSystemAvatars(Avatar.Type.USER)).thenReturn(list(mockAvatar));

        for (int i = 1; i <= 22; i++)
        {
            when(mockAvatarManager.create(AvatarImpl.createSystemAvatar("Avatar-" + i + ".png", "image/png", Avatar.Type.USER))).thenReturn(mockCreatedAvatar);
        }

        when(mockAvatarManager.create(AvatarImpl.createSystemAvatar("Avatar-default.png", "image/png", Avatar.Type.USER))).thenReturn(mockCreatedAvatar);
        mockApplicationProperties.setString("jira.avatar.user.default.id", "9999");

        when(mockAvatarManager.create(AvatarImpl.createSystemAvatar("Avatar-unknown.png", "image/png", Avatar.Type.USER))).thenReturn(mockAnonymousAvatar);
        mockApplicationProperties.setString("jira.avatar.user.anonymous.id", "7777");

        final EntityCondition entityCondition = new MockEntityConditionList(Arrays.asList(new MockEntityExpr("propertyKey", EntityOperator.EQUALS, "user.avatar.id"),
                new MockEntityExpr("propertyValue", EntityOperator.IN, list(12323L))), EntityOperator.AND);
        final GenericValue mockResultGv = new MockGenericValue("OSUserPropertySetNumberView", MapBuilder.singletonMap("id", 45678L));
        when(mockOfBizDelegator.findByCondition("OSUserPropertySetNumberView", entityCondition, list("id"))).thenReturn(list(mockResultGv));
        when(mockOfBizDelegator.bulkUpdateByPrimaryKey("OSPropertyNumber", MapBuilder.singletonMap("value", 9999L), list(45678L))).thenReturn(1);

        when(mockAvatarManager.delete(12323L)).thenReturn(true);

        final UpgradeTask_Build608 upgradeTask = new UpgradeTask_Build608(mockAvatarManager, mockApplicationProperties, mockOfBizDelegator, mockUserPropertyManager);

        upgradeTask.doUpgrade(false);
    }
}
