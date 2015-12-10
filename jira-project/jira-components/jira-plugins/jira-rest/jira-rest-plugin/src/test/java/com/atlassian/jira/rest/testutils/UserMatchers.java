package com.atlassian.jira.rest.testutils;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class UserMatchers
{

    public static class IsApplicationUserWithName extends TypeSafeMatcher<ApplicationUser>
    {
        private final String name;

        public IsApplicationUserWithName(final String name)
        {
            this.name = name;
        }

        @Override
        protected boolean matchesSafely(final ApplicationUser user)
        {
            return user != null && StringUtils.equals(name, user.getName());
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("name is not " + name);
        }
    }

    public static class IsApplicationUserWithKey extends TypeSafeMatcher<ApplicationUser>
    {
        private final String key;

        public IsApplicationUserWithKey(final String key)
        {
            this.key= key;
        }

        @Override
        protected boolean matchesSafely(final ApplicationUser user)
        {
            return StringUtils.equals(key, user.getKey());
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("key is not " + key);
        }
    }

    public static class IsUserWithName extends TypeSafeMatcher<User>
    {
        private final String name;

        public IsUserWithName(final String name)
        {
            this.name = name;
        }

        @Override
        protected boolean matchesSafely(final User user)
        {
            return StringUtils.equals(name, user.getName());
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("name is not " + name);
        }
    }

}
