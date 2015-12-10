package com.atlassian.jira.issue.util.transformers;

import com.atlassian.jira.issue.Issue;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.StringValueTransformer;

public class JiraTransformers
{
    /**
     * Transformer that turns {@link com.atlassian.jira.issue.Issue} objects into their Long ID.
     */
    public static final Transformer ISSUE_ID = new Transformer()
    {
        public Object transform(Object input)
        {
            return ((Issue) input).getId();
        }
    };

    /**
     * Transforms nulls into "-1"
     */
    public static final Transformer NULL_SWAP = new Transformer()
    {
        public Object transform(Object input)
        {
            if (input == null)
            {
                return "-1";
            }
            else
            {
                return input;
            }
        }
    };

    /**
     * Transforms objects into their String.valueOf and nulls into "-1"
     */
    public static final Transformer NULL_SWAP_STRING = ChainedTransformer.getInstance(NULL_SWAP, StringValueTransformer.getInstance());

}
