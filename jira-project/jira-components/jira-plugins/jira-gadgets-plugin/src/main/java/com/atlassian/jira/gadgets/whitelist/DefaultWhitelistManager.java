package com.atlassian.jira.gadgets.whitelist;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.event.AddGadgetEvent;
import com.atlassian.gadgets.event.AddGadgetFeedEvent;
import com.atlassian.jira.bc.whitelist.InternalWhitelistManager;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.plugins.whitelist.ImmutableWhitelistRule;
import com.atlassian.plugins.whitelist.LegacyWhitelistRule;
import com.atlassian.plugins.whitelist.OutboundWhitelist;
import com.atlassian.plugins.whitelist.WhitelistRule;
import com.atlassian.plugins.whitelist.WhitelistType;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation that delegates to the common whitelist
 *
 * @since v6.1
 */
public class DefaultWhitelistManager implements InternalWhitelistManager, InitializingBean, DisposableBean
{
    private final EventPublisher eventPublisher;
    private final com.atlassian.plugins.whitelist.WhitelistService whitelistService;
    private final OutboundWhitelist whitelist;

    public DefaultWhitelistManager(final EventPublisher eventPublisher, final com.atlassian.plugins.whitelist.WhitelistService whitelistService, final OutboundWhitelist whitelist)
    {
        this.eventPublisher = eventPublisher;
        this.whitelistService = whitelistService;
        this.whitelist = whitelist;
    }

    @Override
    public List<String> getRules()
    {
        //when the whitelist is disabled, there shouldn't be any rules!
        if (isDisabled())
        {
            return Collections.emptyList();
        }

        return getLegacyWhitelistRules();
    }

    @Override
    public boolean isDisabled()
    {
        return !whitelistService.isWhitelistEnabled();
    }

    @Override
    public List<String> updateRules(final List<String> newRules, final boolean disabled)
    {
        /**
         * The old api didn't enforce any permission checks. To avoid breaking existing code, every call will pass the
         * permission check.
         */
        try
        {
            ImportUtils.setSubvertSecurityScheme(true);
            return updateWhitelist(newRules, disabled);
        }
        finally
        {
            ImportUtils.setSubvertSecurityScheme(false);
        }
    }

    private List<String> updateWhitelist(List<String> newRules, boolean disabled)
    {
        if (disabled)
        {
            whitelistService.disableWhitelist();
        }
        else
        {
            whitelistService.enableWhitelist();
        }

        for (WhitelistRule whitelistRule : whitelistService.getAll())
        {
            final Integer whitelistRuleId = whitelistRule.getId();
            if (whitelistRuleId != null && whitelistRule.getType() != WhitelistType.APPLICATION_LINK) {
                whitelistService.remove(whitelistRuleId);
            }
        }

        for (String rule : newRules)
        {
            whitelistService.add(new LegacyWhitelistRule(rule));
        }

        return getRules();
    }

    @Override
    public boolean isAllowed(final URI uri)
    {
        return whitelist.isAllowed(uri);
    }

    @Override
    public void afterPropertiesSet()
    {
        eventPublisher.register(this);
    }

    @Override
    public void destroy()
    {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onAddGadget(final AddGadgetEvent addGadgetEvent)
    {
        addWhitelistEntry(addGadgetEvent.getGadgetUri());
    }

    @EventListener
    public void onAddGadgetFeed(final AddGadgetFeedEvent addGadgetFeedEvent)
    {
        addWhitelistEntry(addGadgetFeedEvent.getFeedUri());
    }

    private void addWhitelistEntry(final URI uri)
    {
        if (!whitelist.isAllowed(uri))
        {
            String newRule = uri.getScheme() + "://" + uri.getAuthority() + "/*";
            addWhitelistRule(newRule);
        }
    }

    private void addWhitelistRule(String newRule)
    {
        try
        {
            ImportUtils.setSubvertSecurityScheme(true);
            whitelistService.add(ImmutableWhitelistRule.builder().expression(newRule).type(WhitelistType.WILDCARD_EXPRESSION).build());
        }
        finally
        {
            ImportUtils.setSubvertSecurityScheme(false);
        }
    }

    private List<String> getLegacyWhitelistRules()
    {
        final Collection<String> transformedList = Collections2.transform(whitelistService.getAll(), toLegacyWhitelistRule());
        return ImmutableList.copyOf(Collections2.filter(transformedList, Predicates.notNull()));
    }

    private Function<WhitelistRule, String> toLegacyWhitelistRule()
    {
        return new Function<WhitelistRule, String>()
        {
            @Override
            public String apply(final WhitelistRule input)
            {
                if (input.getType() == WhitelistType.EXACT_URL)
                {
                    return "=" + input.getExpression();
                }
                else if (input.getType() == WhitelistType.REGULAR_EXPRESSION)
                {
                    return "/" + input.getExpression();
                }
                else if (input.getType() == WhitelistType.WILDCARD_EXPRESSION)
                {
                    return input.getExpression();
                }
                else
                {
                    return null;
                }
            }
        };
    }
}
