package it.com.atlassian.jira.plugin.headernav.customcontentlinks.admin;

import com.atlassian.jira.pageobjects.project.AbstractProjectConfigPageTab;
import com.atlassian.jira.pageobjects.util.PollerUtil;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.List;



public class CustomContentLinksConfigPageTab extends AbstractProjectConfigPageTab
{
    private static final String URI_TEMPLATE = "plugins/servlet/custom-content-links-admin?entityKey=%s";
    private final String uri;

    @ElementBy (id = "custom-content-links-admin-content")
    private PageElement customContentLinks;
    private CustomContentLinksForm createLinkForm;

    public CustomContentLinksConfigPageTab(String projectKey)
    {
        this.uri = String.format(URI_TEMPLATE, projectKey);
    }


    @Init
    public void initialise()
    {
        createLinkForm = pageBinder.bind(CustomContentLinksForm.class, By.className("aui-restfultable-create"));
    }


    @Override
    public TimedCondition isAt()
    {
        return customContentLinks.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return uri;
    }

    public List<Link> getLinks() {
        List<PageElement> rows = PollerUtil.findAll("Restfultable did not appear", By.className("aui-restfultable-row"), elementFinder);
        return Lists.transform(rows, new Function<PageElement, Link>()
        {
            @Override
            public Link apply(PageElement input)
            {
                List<PageElement> values = input.findAll(By.className("aui-restfultable-editable"));
                if (values.size() != 2) {
                    throw new RuntimeException("Expecetd 2 aui-restfultable-editable elements in " + input.toString());
                }
                return new Link(values.get(0).getText(), values.get(1).getText());
            }
        });
    }

    public void createLink(String label, String url)
    {
        createLinkForm.fill(label, url);
        createLinkForm.submit();
    }

    public CustomContentLinksForm getCreateLinkForm()
    {
        return createLinkForm;
    }

    public static class Link {
        public Link(String label, String url)
        {
            this.label = label;
            this.url = url;
        }

        public final String label;
        public final String url;
    }
}
