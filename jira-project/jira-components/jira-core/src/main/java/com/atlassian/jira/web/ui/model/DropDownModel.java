package com.atlassian.jira.web.ui.model;

import java.util.List;
import java.util.Set;

/**
 * An interface to model a drop down menu, so we can pump it into a UI component easier
 *
 * @since v4.4.1
 */
public interface DropDownModel
{
    /**
     * @return the top level text of the drop down menu
     */
    String getTopText();


    /**
     * @return the count of items inside any sections
     */
    int getTotalItems();

    /**
     * @return a list fo menu sections
     */
    List<DropDownSection> getSections();

    interface DropDownSection
    {
        /**
         * @return a list fo menu items inside the section
         */
        List<DropDownItem> getItems();
    }


    interface DropDownItem
    {
        /**
         * @return the text of the menu item
         */
        String getText();

        /**
         * @param name  the name of the attribute
         * @return an attribute with a given name
         */
        String getAttr(String name);

        /**
         * This is useful to do 1 time read of all the attributes such that the act of reading one removes it.  It
         * allows you to place say class=xxx somewhere in the markup and then iterator thought the rest of the
         * attributes and not repeat say class=xxx
         *
         * @param name the name of the attribute
         * @return an attribute with a given name and removes it from the underlying map
         */
        public String getAttrAndRemove(String name);

        /**
         * @return the list of attributes associated with item
         */
        Set<String> getAttrs();

    }

}
