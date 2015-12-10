package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 *
 * @since v5.1
 */
public class ManageFiltersPage extends AbstractJiraPage
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

    @ElementBy (id="fav-filters-tab")
    private PageElement favFiltersTab;

    @Override
    public TimedCondition isAt()
    {
        return favFiltersTab.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/ManageFilters.jspa";
    }

    public List<SearchRequest> getSearchRequests(Tab which) {
        driver.findElement(By.id(which.tabId + "-filters-tab")).click();
        driver.waitUntilElementIsVisible(By.id("mf_" + which.tableId));

        final List<SearchRequest> results = Lists.newArrayList();
        for(WebElement we : driver.findElements(By.cssSelector("table.aui tbody tr")))  {
            String id = we.getAttribute("id").replace("mf_", "");
            WebElement nameLink = Iterables.get(we.findElements(By.cssSelector("div.favourite-item a")), 0, null);
            if (nameLink != null) {
                results.add(new SearchRequest(nameLink.getText(), id, !we.findElements(By.cssSelector("a.fav-link.enabled")).isEmpty()));
            }
            if (StringUtils.defaultString(we.getAttribute("class")).contains("last-row")) {
                break;
            }
        }
        return results;
    }

    public static class SearchRequest {
        private final String name;
        private final String id;
        private final boolean favourite;

        public SearchRequest(String name, String id) {
            this(name, id, false);
        }

        public SearchRequest(String name, String id, boolean favourite) {
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
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
