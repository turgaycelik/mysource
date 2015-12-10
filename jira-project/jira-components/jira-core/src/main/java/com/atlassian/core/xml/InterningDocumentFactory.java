package com.atlassian.core.xml;

import com.atlassian.jira.util.dbc.Assertions;
import org.dom4j.DocumentFactory;
import org.dom4j.Text;
import org.dom4j.tree.DefaultText;

import javax.annotation.Nonnull;

/**
 * Interns all strings rather than just QNames
 *
 * @since v6.1
 */
public class InterningDocumentFactory extends DocumentFactory
{
    @Override
    public Text createText(@Nonnull final String text)
    {
        Assertions.notNull("text", text);
        return new DefaultText(text.intern());
    }
}
