package com.atlassian.jira.hints;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public final class HintImpl implements Hint
{
    private final String tooltip;

    private final String text;

    public HintImpl(final String text, final String tooltip)
    {
        notNull("text", text);
        notNull("tooltip", tooltip);

        this.text = text;
        this.tooltip = tooltip;
    }

    @Override
    public String getText()
    {
        return text;
    }

    @Override
    public String getTooltip()
    {
        return tooltip;
    }

    @Override
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

        final HintImpl hint = (HintImpl) o;

        if (!text.equals(hint.text))
        {
            return false;
        }
        if (!tooltip.equals(hint.tooltip))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = tooltip.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }
}
