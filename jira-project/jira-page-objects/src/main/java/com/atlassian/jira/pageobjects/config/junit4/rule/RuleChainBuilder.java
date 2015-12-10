package com.atlassian.jira.pageobjects.config.junit4.rule;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.inject.InjectionContext;
import com.atlassian.pageobjects.util.InjectingTestedProducts;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p/>
 * Builds a rule chain based on a {@link com.atlassian.pageobjects.TestedProduct} instance. The provided tested product
 * must support injection as specified by
 * {@link com.atlassian.pageobjects.util.InjectingTestedProducts#supportsInjection(com.atlassian.pageobjects.TestedProduct)}.
 *
 * <p/>
 * The rules can be passed either by instance, or by rule class, in which case the rule is instantiated by injection -
 * it must be compatible with the JSR-330 injection rules.
 *
 * @since 5.1
 */
public final class RuleChainBuilder
{
    public static RuleChainBuilder forProduct(TestedProduct<?> product)
    {
        return new RuleChainBuilder(product);
    }

    private final InjectionContext injectionContext;
    private RuleChain ruleChain = RuleChain.emptyRuleChain();


    private RuleChainBuilder(TestedProduct<?> product)
    {
        this.injectionContext = InjectingTestedProducts.asInjectionContext(Assertions.notNull("product", product));
    }


    public RuleChainBuilder around(TestRule rule)
    {
        this.ruleChain = ruleChain.around(rule);
        return this;
    }

    public RuleChainBuilder around(Class<? extends TestRule> ruleClass)
    {
        this.ruleChain = ruleChain.around(injectionContext.getInstance(ruleClass));
        return this;
    }

    public RuleChainBuilder when(boolean whether, Conditionally action)
    {
        if (whether)
        {
            action.run(this);
        }
        return this;
    }

    public abstract static class Conditionally {

        public static Conditionally around(final TestRule rule)
        {
            return new Conditionally()
            {
                @Override
                public void run(RuleChainBuilder builder)
                {
                    builder.around(rule);
                }
            };
        }

        public static Conditionally around(final Class<? extends TestRule> ruleClass)
        {
            return new Conditionally()
            {
                @Override
                public void run(RuleChainBuilder builder)
                {
                    builder.around(ruleClass);
                }
            };
        }

        protected abstract void run(RuleChainBuilder builder);

        private Conditionally()
        {
        }
    }

    public RuleChain build()
    {
        return ruleChain;
    }

}
