package com.atlassian.jira.functest.matcher;

import com.atlassian.jira.functest.framework.backdoor.LicenseRoleControl;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Locale;
import java.util.Set;

public class LicenseRoleBeanMatcher extends TypeSafeDiagnosingMatcher<LicenseRoleControl.LicenseRoleBean>
{
    private final String id;
    private final String name;
    private Set<String> groups = Sets.newHashSet();

    public static LicenseRoleBeanMatcher forBusinessUser()
    {
        return new LicenseRoleBeanMatcher("businessuser", "Business User");
    }

    public LicenseRoleBeanMatcher(String id, String name)
    {
        this.id = id.toLowerCase(Locale.ENGLISH);
        this.name = name;
    }

    @Override
    protected boolean matchesSafely(final LicenseRoleControl.LicenseRoleBean item, final Description mismatchDescription)
    {
        if (Objects.equal(name, item.getName())
                && groups.containsAll(item.getGroups())
                && Objects.equal(id, item.getId()))
        {
            return true;
        }
        else
        {
            mismatchDescription.appendValue(String.format("[name: %s, groups: %s, id: %s]",
                    item.getName(), item.getGroups(), item.getId()));

            return false;
        }
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText(String.format("[name: %s, groups: %s, id: %s]", name, groups, id));
    }

    public LicenseRoleBeanMatcher setGroups(final String...groups)
    {
        this.groups = Sets.newHashSet(groups);
        return this;
    }
}
