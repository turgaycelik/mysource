package com.atlassian.jira.functest.framework.parser.comment;

/**
 * Simple class to hold the comment as shown on the view issue page.
 *
 * @since v3.13
 */
public class Comment
{
    private String comment;
    private String details;

    public Comment()
    {
    }

    public Comment(final String comment, final String details)
    {
        this.comment = comment;
        this.details = details;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(final String comment)
    {
        this.comment = comment;
    }

    public String getDetails()
    {
        return details;
    }

    public void setDetails(final String details)
    {
        this.details = details;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final Comment comment1 = (Comment) o;

        if (comment != null ? !comment.equals(comment1.comment) : comment1.comment != null)
        {
            return false;
        }
        if (details != null ? !details.equals(comment1.details) : comment1.details != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        return result;
    }
}
