package com.atlassian.jira.plugin.index;

import java.util.Map;

import com.atlassian.fugue.Iterables;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.index.ChangeHistorySearchExtractor;
import com.atlassian.jira.index.CommentSearchExtractor;
import com.atlassian.jira.index.EntitySearchExtractor;
import com.atlassian.jira.index.IssueSearchExtractor;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.module.ModuleFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

/**
 * Descriptor of modules which can add additional fields to indexes of Issue, ChangeHistory and Comments.
 *
 * @since v6.2
 */
public class EntitySearchExtractorModuleDescriptorImpl extends AbstractJiraModuleDescriptor<EntitySearchExtractor<?>>
{
    private static final Map<Class<? extends EntitySearchExtractor>, RegistrationStrategy<?>> SUPPORTED_EXTRACTOR_TYPES = ImmutableMap.<Class<? extends EntitySearchExtractor>, RegistrationStrategy<?>>of(
        CommentSearchExtractor.class, new CommentSearchExtractorRegistrationStrategy(),
        ChangeHistorySearchExtractor.class, new ChangeHistorySearchExtractorRegistrationStrategy(),
        IssueSearchExtractor.class, new IssueSearchExtractorRegistrationStrategy()
    );

    private RegistrationStrategy<?> registrationStrategy;

    public EntitySearchExtractorModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    public void enabled()
    {
        super.enabled();
        registrationStrategy = registrationStrategy();
        registrationStrategy.register(getModule());
    }

    @Override
    public void disabled()
    {
        super.disabled();
        registrationStrategy.unregister(getModule());
    }

    public RegistrationStrategy<?> getRegistrationStrategy()
    {
        return registrationStrategy;
    }

    private <E> RegistrationStrategy<E> registrationStrategy()
    {
        return Iterables.findFirst(SUPPORTED_EXTRACTOR_TYPES.entrySet(), new Predicate<Map.Entry<Class<? extends EntitySearchExtractor>, RegistrationStrategy<?>>>()
        {
            @Override
            public boolean apply(final Map.Entry<Class<? extends EntitySearchExtractor>, RegistrationStrategy<?>> entry)
            {
                return entry.getKey().isAssignableFrom(getModuleClass());
            }
        }).fold(new Supplier<RegistrationStrategy<E>>()
        {
            @Override
            public RegistrationStrategy<E> get()
            {
                throw new PluginException("Class " + getModuleClassName() + " is not implementing extractor for comment, issue or change history");
            }
        }, new Function<Map.Entry<Class<? extends EntitySearchExtractor>, RegistrationStrategy<?>>, RegistrationStrategy<E>>()
        {
            @Override
            public RegistrationStrategy<E> apply(final Map.Entry<Class<? extends EntitySearchExtractor>, RegistrationStrategy<?>> entry)
            {
                // noinspection unchecked
                return (RegistrationStrategy<E>) entry.getValue();
            }
        });
    }

    interface RegistrationStrategy<E>
    {
        void register(EntitySearchExtractor<?> extractor);

        void unregister(EntitySearchExtractor<?> extractor);

        Class<E> getEntityType();
    }

    private abstract static class AbstractRegistrationStrategy<E, S extends EntitySearchExtractor<E>> implements RegistrationStrategy<E>
    {
        private final Class<S> searchExtractorType;
        private final Class<E> entityType;

        protected AbstractRegistrationStrategy(Class<S> searchExtractorType, Class<E> entityType)
        {
            this.searchExtractorType = searchExtractorType;
            this.entityType = entityType;
        }

        @Override
        public void register(final EntitySearchExtractor<?> extractor)
        {
            if (searchExtractorType.isAssignableFrom(extractor.getClass()))
            {
                ComponentAccessor.getComponent(SearchExtractorRegistrationManager.class).register(searchExtractorType.cast(extractor), entityType);
            }
        }

        @Override
        public void unregister(final EntitySearchExtractor<?> extractor)
        {
            if (searchExtractorType.isAssignableFrom(extractor.getClass()))
            {
                ComponentAccessor.getComponent(SearchExtractorRegistrationManager.class).unregister(searchExtractorType.cast(extractor), entityType);
            }
        }

        @Override
        public Class<E> getEntityType()
        {
            return entityType;
        }
    }

    private static class CommentSearchExtractorRegistrationStrategy extends AbstractRegistrationStrategy<Comment, CommentSearchExtractor>
    {
        protected CommentSearchExtractorRegistrationStrategy()
        {
            super(CommentSearchExtractor.class, Comment.class);
        }
    }

    private static class ChangeHistorySearchExtractorRegistrationStrategy extends AbstractRegistrationStrategy<ChangeHistoryGroup, ChangeHistorySearchExtractor>
    {
        protected ChangeHistorySearchExtractorRegistrationStrategy()
        {
            super(ChangeHistorySearchExtractor.class, ChangeHistoryGroup.class);
        }
    }


    private static class IssueSearchExtractorRegistrationStrategy extends AbstractRegistrationStrategy<Issue, IssueSearchExtractor>
    {
        protected IssueSearchExtractorRegistrationStrategy()
        {
            super(IssueSearchExtractor.class, Issue.class);
        }
    }
}
