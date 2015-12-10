package com.atlassian.jira.functest.matcher;

import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @since v6.1
 */
public class IssueLinksTypeMatcher extends BaseMatcher<IssuelinksType>
{
    public static IssueLinksTypeMatcher issueLinkType(final String name, final String inward, final String outward)
    {
        return new IssueLinksTypeMatcher(name, inward, outward);
    }

    private final String name;
    private final String inward;
    private final String outward;

    private IssueLinksTypeMatcher(final String name, final String inward, final String outward)
    {

        this.name = name;
        this.inward = inward;
        this.outward = outward;
    }

    @Override
    public boolean matches(final Object item)
    {
        return item instanceof IssuelinksType && ((IssuelinksType) item).getName().equals(name) && ((IssuelinksType) item).getInward().equals(inward) && ((IssuelinksType) item).getOutward().equals(outward);
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText(new IssuelinksType(null, null, name, inward, outward).toString().replaceAll("null", "<any>"));
    }
}