package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 *
 *
 * @since v5.1
 */
public class ConfigurePortalPages extends AbstractJiraPage
{
    public enum Tab {
        MY("my", "owned");

        private String tabId;
        private String tableId;

        Tab(String tabId, String tableId) {
            this.tableId = tableId;
            this.tabId = tabId;
        }
    }

    @ElementBy (id="create_page")
    private PageElement createNew;

    @Override
    public TimedCondition isAt()
    {
        return createNew.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/ConfigurePortalPages!default.jspa";
    }

    public List<Dashboard> getDashboards(Tab which) {
        driver.findElement(By.id(which.tabId + "-dash-tab")).click();
        driver.waitUntilElementIsVisible(By.id("pp_" + which.tableId));

        final List<Dashboard> results = Lists.newArrayList();
        for(WebElement we : driver.findElements(By.cssSelector("table.aui tbody tr")))  {
            String id = we.getAttribute("id").replace("pp_", "");
            String name = we.findElement(By.cssSelector("div.favourite-item a")).getText();
            results.add(new Dashboard(name, id, !we.findElements(By.cssSelector("a.fav-link.enabled")).isEmpty()));
            if (StringUtils.defaultString(we.getAttribute("class")).contains("last-row")) {
                break;
            }
        }
        return results;
    }

    public static class Dashboard {
        private final String name;
        private final String id;
        private final boolean favourite;

        public Dashboard(String name, String id) {
            this(name, id, false);
        }

        public Dashboard(String name, String id, boolean favourite) {
            this.name = name;
            this.id = id;
            this.favourite = favourite;
        }

        public String getName()
        {
            return name;
        }

        public String getId()
        {
            return id;
        }

        public boolean isFavourite()
        {
            return favourite;
        }

        @Override
        public boolean equals(Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
