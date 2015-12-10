package it.com.atlassian.jira.plugin.headernav.customcontentlinks.admin;

import java.util.List;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.util.PollerUtil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.openqa.selenium.By;

import static junit.framework.Assert.assertEquals;

public class ProjectShortcutsDialog
{
    @Inject
    private PageElementFinder elementFinder;

    public ProjectShortcutsDialog()
    {
    }

    public List<Link> getRelatedLinks() {
        return Lists.transform(getLinkLists().get(1).findAll(By.tagName("a")), new Function<PageElement, Link>()
        {
            @Override
            public Link apply(PageElement input)
            {
                return new Link(input.getText(), input.getAttribute("href"));
            }
        });
    }

    private List<PageElement> getLinkLists() {
        List<PageElement> linkLists = PollerUtil.findAll("Waiting for project shortcuts dialog to load links", By.className("projectshortcut-links"), elementFinder);
        assertEquals(2, linkLists.size());
        return linkLists;
    }

    public static class Link {
        public Link(String title, String url)
        {
            this.title = title;
            this.url = url;
        }

        public String title;
        public String url;

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            Link link = (Link) o;

            if (!title.equals(link.title)) { return false; }
            if (!url.equals(link.url)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = title.hashCode();
            result = 31 * result + url.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return "Link{" +
                    "title='" + title + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}
