package com.atlassian.jira.functest.framework.navigation.issue;

/**
 * Represents the attachments block on the view issue page.
 *
 * @since v4.2
 */
public interface AttachmentsBlock
{
    /**
     * Sorts the file attachments list on the view issue page for an specific issue.
     *
     * @param key The {@link com.atlassian.jira.functest.framework.navigation.issue.AttachmentsBlock.Sort.Key} to use to
     * sort the list.
     * @param direction The {@link com.atlassian.jira.functest.framework.navigation.issue.AttachmentsBlock.Sort.Direction}
     * to use to sort the list.
     */
    public void sort(AttachmentsBlock.Sort.Key key, AttachmentsBlock.Sort.Direction direction);

    /**
     * Navigates to the Manage Attachments page.
     *
     * @return An {@link com.atlassian.jira.functest.framework.navigation.issue.AttachmentManagement} object to interact
     *         with the Manage Attachments page.
     */
    AttachmentManagement manage();

    ImageAttachmentsGallery gallery();

    FileAttachmentsList list();

    interface Sort
    {
        /**
         * Represents a key used to sort the attachments list on the view issue page.
         */
        enum Key
        {
            NAME("attachment-sort-key-name"), DATE("attachment-sort-key-date");

            private final String linkId;

            Key(final String linkId)
            {
                this.linkId = linkId;
            }

            public String getLinkId()
            {
                return linkId;
            }
        }

        /**
         * Represents a sort direction used when sorting the attachments list on the view issue page.
         */
        enum Direction
        {
            ASCENDING("attachment-sort-direction-asc"), DESCENDING("attachment-sort-direction-desc");

            private String linkId;

            Direction(final String linkId)
            {
                this.linkId = linkId;
            }

            public String getLinkId()
            {
                return linkId;
            }
        }
    }
}
