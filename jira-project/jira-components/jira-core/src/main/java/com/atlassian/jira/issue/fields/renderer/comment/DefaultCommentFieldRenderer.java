package com.atlassian.jira.issue.fields.renderer.comment;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Options;
import com.atlassian.jira.plugin.comment.CommentFieldRendererModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.CommentHelper;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.descriptors.WeightedDescriptorComparator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Ordering;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.transform;

public class DefaultCommentFieldRenderer implements CommentFieldRenderer
{
    private final Ordering<CommentFieldRendererModuleDescriptor> commentFieldRendererModuleDescriptorOrdering;
    private final PluginAccessor pluginAccessor;

    public DefaultCommentFieldRenderer(final PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
        this.commentFieldRendererModuleDescriptorOrdering = Ordering.from(new Comparator<CommentFieldRendererModuleDescriptor>()
        {
            WeightedDescriptorComparator comparator = new WeightedDescriptorComparator();

            @Override
            public int compare(final CommentFieldRendererModuleDescriptor moduleDescriptor1, final CommentFieldRendererModuleDescriptor moduleDescriptor2)
            {
                final int compareResult = comparator.compare(moduleDescriptor2, moduleDescriptor1);
                return compareResult != 0 ? compareResult : moduleDescriptor1.getCompleteKey().compareToIgnoreCase(moduleDescriptor2.getCompleteKey());
            }
        });
    }

    @Override
    public String getIssuePageEditHtml(final Map<String, Object> context, final CommentHelper commentHelper)
    {
        return renderCommentFields(new DescriptorToHtmlFunction()
        {
            @Override
            public Option<String> apply(final CommentFieldRendererModuleDescriptor moduleDescriptor)
            {
                return moduleDescriptor.getIssuePageEditHtml(context);
            }
        }, commentHelper);
    }

    @Override
    public String getIssuePageViewHtml(final Map<String, Object> context, final CommentHelper commentHelper)
    {
        return renderCommentFields(new DescriptorToHtmlFunction()
        {
            @Override
            public Option<String> apply(final CommentFieldRendererModuleDescriptor moduleDescriptor)
            {
                return moduleDescriptor.getIssuePageViewHtml(context);
            }
        }, commentHelper);
    }

    @Override
    public String getFieldEditHtml(final Map<String, Object> context, final CommentHelper commentHelper)
    {
        return renderCommentFields(new DescriptorToHtmlFunction()
        {
            @Override
            public Option<String> apply(final CommentFieldRendererModuleDescriptor moduleDescriptor)
            {
                return moduleDescriptor.getFieldEditHtml(context);
            }
        }, commentHelper);
    }

    @Override
    public String getFieldViewHtml(final Map<String, Object> context, final CommentHelper commentHelper)
    {
        return renderCommentFields(new DescriptorToHtmlFunction()
        {
            @Override
            public Option<String> apply(final CommentFieldRendererModuleDescriptor moduleDescriptor)
            {
                return moduleDescriptor.getFieldViewHtml(context);
            }
        }, commentHelper);
    }

    private String renderCommentFields(final DescriptorToHtmlFunction descriptorToHtmlFunction, final CommentHelper commentHelper)
    {
        Iterable<CommentFieldRendererModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(CommentFieldRendererModuleDescriptor.class);

        List<CommentFieldRendererModuleDescriptor> descriptorsOrderedByWeight = commentFieldRendererModuleDescriptorOrdering.sortedCopy(descriptors);

        // Complicated? Not really. We get all renders for which want to be displayed (according to the condition).
        // Then, transform all this to optional html (there might be no definition of field-view-resource, when we would get none).
        // Finally, pick the first non empty html.
        final Option<String> commentFieldToRender = getFirst(Options.filterNone(transform(filter(descriptorsOrderedByWeight, SafePluginPointAccess.safe(new Predicate<CommentFieldRendererModuleDescriptor>()
        {
            @Override
            public boolean apply(final CommentFieldRendererModuleDescriptor moduleDescriptor) {
                return moduleDescriptor.getCondition() == null || moduleDescriptor.getCondition().shouldDisplay(commentHelper.getContextParams());
            }
        })), SafePluginPointAccess.safe(descriptorToHtmlFunction))), Option.<String>none());

        return commentFieldToRender.getOrError(new Supplier<String>()
        {
            @Override
            public String get()
            {
                throw new IllegalStateException("There should be at least one comment field renderer, which can render the comments");
            }
        });
    }

    private interface DescriptorToHtmlFunction extends Function<CommentFieldRendererModuleDescriptor, Option<String>> {}

}
