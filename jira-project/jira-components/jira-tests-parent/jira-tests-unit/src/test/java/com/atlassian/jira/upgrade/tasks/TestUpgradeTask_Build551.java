package com.atlassian.jira.upgrade.tasks;

import java.util.Collections;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import junit.framework.TestCase;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestUpgradeTask_Build551 extends TestCase
{

    @Test
    public void testUpgrade() throws Exception
    {
        final FieldScreenTab mockTab = createMock(FieldScreenTab.class);
        final FieldScreen mockScreen = createMock(FieldScreen.class);
        final FieldScreenScheme mockDefaultScheme = createMock(FieldScreenScheme.class);
        final FieldScreenSchemeManager mockFieldScreenSchemeManager = createMock(FieldScreenSchemeManager.class);
        expect(mockFieldScreenSchemeManager.getFieldScreenSchemes()).andReturn(CollectionBuilder.list(mockDefaultScheme));
        expect(mockDefaultScheme.getId()).andReturn(1L).anyTimes();
        expect(mockDefaultScheme.getFieldScreen(null)).andReturn(mockScreen);
        expect(mockScreen.getTabs()).andReturn(Collections.singletonList(mockTab));
        expect(mockScreen.getTab(0)).andReturn(mockTab);
        expect(mockTab.getFieldScreenLayoutItem("labels")).andReturn(null);
        mockTab.addFieldScreenLayoutItem("labels");

        replay(mockTab, mockScreen, mockDefaultScheme, mockFieldScreenSchemeManager);

        final UpgradeTask_Build551 upgradeTask = new UpgradeTask_Build551(mockFieldScreenSchemeManager);
        upgradeTask.doUpgrade(false);

        verify(mockTab, mockScreen, mockDefaultScheme, mockFieldScreenSchemeManager);
    }

    @Test
    public void testUpgradeAlreadyConfigured() throws Exception
    {
        final FieldScreenLayoutItem fieldScreenLayoutItem = createMock(FieldScreenLayoutItem.class);
        final FieldScreenTab mockTab = createMock(FieldScreenTab.class);
        final FieldScreen mockScreen = createMock(FieldScreen.class);
        final FieldScreenScheme mockDefaultScheme = createMock(FieldScreenScheme.class);
        final FieldScreenSchemeManager mockFieldScreenSchemeManager = createMock(FieldScreenSchemeManager.class);
        expect(mockFieldScreenSchemeManager.getFieldScreenSchemes()).andReturn(CollectionBuilder.list(mockDefaultScheme));
        expect(mockDefaultScheme.getId()).andReturn(1L).anyTimes();
        expect(mockDefaultScheme.getFieldScreen(null)).andReturn(mockScreen);
        expect(mockScreen.getTabs()).andReturn(Collections.singletonList(mockTab));
        expect(mockScreen.getTab(0)).andReturn(mockTab);
        expect(mockTab.getFieldScreenLayoutItem("labels")).andReturn(fieldScreenLayoutItem);

        replay(mockTab, mockScreen, mockDefaultScheme, mockFieldScreenSchemeManager);

        final UpgradeTask_Build551 upgradeTask = new UpgradeTask_Build551(mockFieldScreenSchemeManager);
        upgradeTask.doUpgrade(false);

        verify(mockTab, mockScreen, mockDefaultScheme, mockFieldScreenSchemeManager);
    }

    @Test
    public void testUpgradeNoDefaultScheme() throws Exception
    {
        final FieldScreenScheme mockScheme = createMock(FieldScreenScheme.class);
        final FieldScreenSchemeManager mockFieldScreenSchemeManager = createMock(FieldScreenSchemeManager.class);
        expect(mockFieldScreenSchemeManager.getFieldScreenSchemes()).andReturn(CollectionBuilder.list(mockScheme));
        expect(mockScheme.getId()).andReturn(10001L).anyTimes();
        replay(mockScheme, mockFieldScreenSchemeManager);

        final UpgradeTask_Build551 upgradeTask = new UpgradeTask_Build551(mockFieldScreenSchemeManager);
        upgradeTask.doUpgrade(false);

        verify(mockScheme, mockFieldScreenSchemeManager);
    }

    @Test
    public void testUpgradeNoTabOnDefaultScreen() throws Exception
    {
        final FieldScreenTab mockTab = createMock(FieldScreenTab.class);
        final FieldScreen mockScreen = createMock(FieldScreen.class);
        final FieldScreenScheme mockDefaultScheme = createMock(FieldScreenScheme.class);
        final FieldScreenSchemeManager mockFieldScreenSchemeManager = createMock(FieldScreenSchemeManager.class);
        expect(mockFieldScreenSchemeManager.getFieldScreenSchemes()).andReturn(CollectionBuilder.list(mockDefaultScheme));
        expect(mockDefaultScheme.getId()).andReturn(1L).anyTimes();
        expect(mockDefaultScheme.getFieldScreen(null)).andReturn(mockScreen);
        expect(mockScreen.getTabs()).andReturn(Collections.<FieldScreenTab>emptyList());

        replay(mockTab, mockScreen, mockDefaultScheme, mockFieldScreenSchemeManager);

        final UpgradeTask_Build551 upgradeTask = new UpgradeTask_Build551(mockFieldScreenSchemeManager);
        upgradeTask.doUpgrade(false);

        verify(mockTab, mockScreen, mockDefaultScheme, mockFieldScreenSchemeManager);
    }

    @Test
    public void testMetaData()
    {
        final UpgradeTask_Build551 upgradeTask = new UpgradeTask_Build551(null);
        assertEquals("551", upgradeTask.getBuildNumber());
        assertEquals("Adds the new Labels system field to the Default Screen of the default field configuration.", upgradeTask.getShortDescription());
    }

}
