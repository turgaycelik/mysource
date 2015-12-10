package com.atlassian.jira.config.component;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import org.picocontainer.injectors.ProviderAdapter;

/**
 * @since v6.2
 */
public class I18nHelperProvider extends ProviderAdapter
{
    public I18nHelper provide(I18nHelper.BeanFactory factory, JiraAuthenticationContext authenticationContext)
    {
        //I18nHelper is injected for example in rest resources which are living long enough to invalidate fetched helper
        //therefore we have to return proxy which will hold fetching object from factory straight to the call
        return AbstractDelegatedMethodInterceptor.createProxy(I18nHelper.class, new I18NInterceptor(factory, authenticationContext));
    }


    static class I18NInterceptor extends AbstractDelegatedMethodInterceptor<I18nHelper>
    {
        private final JiraAuthenticationContext authenticationContext;
        private final I18nHelper.BeanFactory factory;

        public I18NInterceptor(I18nHelper.BeanFactory i18nFactory, JiraAuthenticationContext authenticationContext)
        {
            this.authenticationContext = authenticationContext;
            this.factory = i18nFactory;
        }


        @Override
        protected I18nHelper getDelegate()
        {
            return factory.getInstance(authenticationContext.getUser());
        }
    }
}
