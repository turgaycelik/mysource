package com.atlassian.jira.webtests.ztests.avatar;

import java.util.Map;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.RestFuncTest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;

@WebTest ({ FUNC_TEST, UPGRADE_TASKS })
public class TestAvatarSettingsMigration extends RestFuncTest
{
    static final ImmutableList<String> USERS = ImmutableList.of(
            "user_with_avatar",
            "user_without_avatar"
    );

    static final Map<String, String> GRAVATAR_ON = ImmutableMap.of(
            "user_with_avatar", "http://www.gravatar.com/avatar/3b273c72f359240538caceb63b6384ef?d=mm&s=48",
            "user_without_avatar", "http://www.gravatar.com/avatar/13570ed7405a9e0a95744ea68e55017e?d=mm&s=48"
    );

    static final Map<String, String> GRAVATAR_OFF = ImmutableMap.of(
            "user_with_avatar", "/secure/useravatar?avatarId=10040",
            "user_without_avatar", "/secure/useravatar?avatarId=10062"
    );

    public void testGravatarsRemainOnAfterUpgrade() throws Exception
    {
        backdoor.restoreDataFromResource("avatar/GravatarsOn.xml");
        for (String user : USERS)
        {
            assertEquals(user + " should keep their gravatar after upgrade", GRAVATAR_ON.get(user), backdoor.userProfile().getAvatarUrl(user));
        }
    }

    public void testGravatarsRemainOffAfterUpgradeWhenExplicitlyOff() throws Exception
    {
        backdoor.restoreDataFromResource("avatar/GravatarsOff.xml");

        String context = getEnvironmentData().getContext();
        for (String user : ImmutableList.of("user_with_avatar", "user_without_avatar"))
        {
            assertEquals(user + " should keep their internal avatar after upgrade", context + GRAVATAR_OFF.get(user), backdoor.userProfile().getAvatarUrl(user));
        }
    }

    public void testGravatarsRemainOffAfterUpgradeWhenDefaultedToOff() throws Exception
    {
        backdoor.restoreDataFromResource("avatar/GravatarsDefault.xml");

        String context = getEnvironmentData().getContext();
        for (String user : ImmutableList.of("user_with_avatar", "user_without_avatar"))
        {
            assertEquals(user + " should keep their internal avatar after upgrade", context + GRAVATAR_OFF.get(user), backdoor.userProfile().getAvatarUrl(user));
        }
    }
}
