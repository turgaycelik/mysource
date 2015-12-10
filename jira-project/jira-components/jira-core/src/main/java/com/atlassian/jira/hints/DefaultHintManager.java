package com.atlassian.jira.hints;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLabel;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.plugin.web.api.DynamicWebInterfaceManager;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.model.WebLabel;

import org.apache.log4j.Logger;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the {@link HintManager}.
 *
 * @since v4.2
 */
public class DefaultHintManager implements HintManager
{
    private static final Logger log = Logger.getLogger(DefaultHintManager.class);

    public static final String ALL_HINTS_SECTION = "jira.hints/all";
    public static final String HINTS_PREFIX = "jira.hints/";
    private final Random random = new Random();//SecureRandomFactory.newInsecureRandom();
    private final DynamicWebInterfaceManager webInterfaceManager;

    public DefaultHintManager(final DynamicWebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    public Hint getRandomHint(final User user, final JiraHelper jiraHelper)
    {
        return getRandomHint(getAllHints(user, jiraHelper));
    }

    public List<Hint> getAllHints(final User user, final JiraHelper jiraHelper)
    {
        return getHintsForSection(user, jiraHelper, ALL_HINTS_SECTION);
    }

    public Hint getHintForContext(final User remoteUser, final JiraHelper jiraHelper, final Context context)
    {
        notNull("context", context);
        return getRandomHint(getHintsForSection(remoteUser, jiraHelper, HINTS_PREFIX + context.toString()));
    }

    private Hint getRandomHint(final List<Hint> hints)
    {
        if (hints.isEmpty())
        {
            return null;
        }
        int randomPosition = random.nextInt(hints.size());
        return hints.get(randomPosition);
    }

    private List<Hint> getHintsForSection(final User user, final JiraHelper helper, final String section)
    {
        notNull("helper", helper);
        final Map<String,Object> context = helper.getContextParams();
        context.put("user", user);
        final Iterable<WebItem> items = webInterfaceManager.getDisplayableWebItems(section, context);
        return CollectionUtil.transform(items, new Function<WebItem, Hint>(){
            public Hint get(final WebItem input)
            {
                final Option<String> label = option(input.getLabel());
                if(label.isEmpty())
                {
                    log.warn("Hint web item with key '" + input.getCompleteKey() + "' does not define a label");
                }
                return new HintImpl(label.getOrElse(""), input.getTitle());
            }
        });
    }

    private String getText(final WebLabel input, User user, JiraHelper helper)
    {
        if(input == null)
        {
            return "";
        }
        return ((JiraWebLabel)input).getDisplayableLabel(user, helper);
    }
}
