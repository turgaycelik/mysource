package com.atlassian.jira.pageobjects.global;

import com.atlassian.pageobjects.elements.PageElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

/**
 * @since v4.4
 */
public class SoyUserProfileLink implements UserLink
{
    private final User user;
    private final boolean isHover;

    private SoyUserProfileLink(User user, boolean isHover)
    {
        this.user = user;
        this.isHover = isHover;
    }

    public static SoyUserProfileLink parse(PageElement element)
    {
        final PageElement a = element.find(By.cssSelector(".user-hover"));
        if (a.isPresent())
        {
            String userName =  a.getAttribute("rel");
            String fullName = StringUtils.stripToNull(a.getText());

            return new SoyUserProfileLink(new User(userName, fullName), true);
        }
        else
        {
            PageElement anonymous = element.find(By.cssSelector("span.user-anonymous"));
            if (anonymous.isPresent())
            {
                return new SoyUserProfileLink(User.ANONYMOUS, false);
            }
            else
            {
                PageElement name = element.find(By.cssSelector("span.user-name"));
                if (name.isPresent())
                {
                    return new SoyUserProfileLink(new User(StringUtils.stripToNull(name.getText()), null), false);
                }
                else
                {
                    return null;
                }
            }
        }
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public boolean isHoverLink()
    {
        return isHover;
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
