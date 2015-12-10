package com.atlassian.jira.mail;

import com.atlassian.diff.DiffViewBean;
import com.atlassian.jira.web.action.util.DiffViewRenderer;
import com.opensymphony.util.TextUtils;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.4
 */
public class DiffUtils
{
    private final DiffViewRenderer diffViewRenderer;

    public DiffUtils(DiffViewRenderer diffViewRenderer)
    {
        this.diffViewRenderer = diffViewRenderer;
    }

    public String diff(String oldText, String oldStyle, String newText, String newStyle)
    {
        final DiffViewBean diff = DiffViewBean.createWordLevelDiff(oldText, newText);
        return diffViewRenderer.getUnifiedHtml(diff, oldStyle, newStyle);
    }
}
