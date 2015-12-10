package com.atlassian.jira.pageobjects.pages.admin.licenserole;

import com.atlassian.jira.pageobjects.components.fields.MultiSelect;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.TimedElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openqa.selenium.By;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;

public class LicenseRolePage extends AbstractJiraPage
{
    private final Function<PageElement, Role> TOROLE = new Function<PageElement, Role>()
    {
        @Override
        public Role apply(@Nullable final PageElement input)
        {
            return new Role(input);
        }
    };

    private static final Function<PageElement, String> TOSTRING = new Function<PageElement, String>()
    {
        @Override
        public String apply(final PageElement input)
        {
            return StringUtils.stripToNull(input.getText());
        }
    };

    private static final Predicate<PageElement> ISPRESENT = new Predicate<PageElement>()
    {
        @Override
        public boolean apply(final PageElement input)
        {
            return input.isPresent();
        }
    };

    @ElementBy (cssSelector = "#license-roles table")
    private PageElement roles;

    @Override
    public TimedCondition isAt()
    {
        //Wait until the table finishes loading.
        final TimedElement element = roles.timed();
        return Conditions.and(element.isPresent(), Conditions.not(element.hasClass("loading")));
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/LicenseRoles.jspa";
    }

    public List<Role> roles()
    {
        return copyOf(transform(roles.findAll(By.cssSelector("tbody tr")), TOROLE));
    }

    public Role role(String id)
    {
        final PageElement element = roles.find(By.cssSelector(String.format("tbody tr[data-id='%s']", id)));
        if (!element.isPresent())
        {
            throw new IllegalArgumentException(String.format("Can't find role with id '%s'.", id));
        }
        return new Role(element);
    }

    public class Role
    {
        private final PageElement row;
        private final PageElement name;
        private final PageElement groups;

        private Role(PageElement row)
        {
            if (!row.isPresent())
            {
                throw new IllegalArgumentException("row is not on the page.");
            }

            this.row = row;
            this.name = row.find(By.className("license-role-name"));
            this.groups = row.find(By.className("license-role-groups"));

            if (!Iterables.all(Arrays.asList(this.name, this.groups), ISPRESENT))
            {
                throw new IllegalStateException("Unable to find expected elements in table row.");
            }
        }

        public List<String> getGroups()
        {
            return copyOf(transform(groups.findAll(By.cssSelector(".license-role-group-list li")), TOSTRING));
        }

        public String getName()
        {
            return TOSTRING.apply(name);
        }

        public EditableRole edit()
        {
            final PageElement trigger = groups.find(By.className("aui-restfultable-editable"));
            if (!trigger.isPresent())
            {
                throw new IllegalStateException("Row does not appear to be editable.");
            }
            if (!trigger.isVisible())
            {
                //I can't work out how to make WD work with empty groups. You need to execute a hover to show the trigger
                //but it just doesn't seem to work for me.
                throw new IllegalStateException("Trigger is not currently visible (probably because the row is empty).");
            }

            trigger.click();
            return new EditableRole(row);
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this)
                    .append("groups", groups)
                    .append("name", name)
                    .toString();
        }
    }

    public class EditableRole
    {
        private final PageElement row;
        private final MultiSelect select;

        private EditableRole(PageElement row)
        {
            if (!row.isPresent())
            {
                throw new IllegalArgumentException("row is not on the page.");
            }
            this.row = row;
            this.select = pageBinder.bind(MultiSelect.class, "license-role-groups-select");
        }

        public EditableRole groups(String...groups)
        {
            select.clearAllItems();
            for (String group : groups)
            {
                select.add(group);
            }
            return this;
        }

        public Role save()
        {
            row.find(By.cssSelector("input[type=submit]")).click();
            Poller.waitUntilFalse(row.timed().hasClass("loading"));
            return new Role(row);
        }
    }
}
