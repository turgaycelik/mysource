package com.atlassian.jira.plugin.language;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;

/**
 * Interface defining module descriptors for the {@link TranslationTransform} plugin point.
 *
 * {@link TranslationTransform} modules are {@link OrderableModuleDescriptor} so that transforms can be applied in a
 * deterministic order.
 *
 * @since v5.1
 */
public interface TranslationTransformModuleDescriptor extends JiraResourcedModuleDescriptor<TranslationTransform>, OrderableModuleDescriptor
{
}
