package com.atlassian.jira.functest.framework.parser.comment;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses comments off the view issue page.
 *
 * @since v3.13
 */
public class CommentParser
{
    public static List<Comment> getComments(final WebTester tester)
    {
        final List<Comment> commentList = new ArrayList<Comment>();
        final XPathLocator xPathLocator = new XPathLocator(tester, "//div[contains(@class, 'actionContainer')]");
        final Node[] nodes = xPathLocator.getNodes();
        for (Node actionContainerNode : nodes)
        {
            final String details = getTextFromSubNode(actionContainerNode, ".//div[@class='action-details']");
            final String links = getTextFromSubNode(actionContainerNode, ".//div[@class='action-links']");
            //check if the action really is a comment and that it's not the hidden div of the comment (the hidden div
            //will not contain the 'Permlink' link).
            if (details.indexOf("added a comment") != -1 && links.indexOf("Permalink") != -1)
            {
                Comment comment = new Comment();
                comment.setDetails(details);
                comment.setComment(getTextFromSubNode(actionContainerNode, "div[contains(@class, 'action-body')]"));
                commentList.add(comment);
            }
        }

        return commentList;
    }

    private static String getTextFromSubNode(final Node node, final String xpath)
    {
        final String text = new XPathLocator(node, xpath).getText();
        if (text == null)
        {
            return null;
        }
        return text.trim();
    }
}
