AJS.$(function($) {
    $(document).on('simpleClick', '.collapsed-comments', function(e) {
        e.preventDefault();

        var collapsedLink = $(this);
        var collapsedLinkBlock = collapsedLink.closest('.message-container');
        var container = collapsedLink.closest('.issuePanelContainer');
        var module = collapsedLink.closest('.module');
        var numCommentsBefore = collapsedLinkBlock.prevAll('.activity-comment').length;
        var numCollapsed = collapsedLink.find('.show-more-comments').data('collapsed-count');

        showLoading();
        makeRequest();

        function showLoading() {
            collapsedLink.find('.show-more-comments').text(AJS.I18n.getText('common.concepts.loading'));
        }

        function makeRequest() {
            var url = collapsedLink.attr('href');
            JIRA.SmartAjax.makeRequest({
                url: url,
                method: 'GET',
                headers: { "X-PJAX": true } // needed for the ViewIssue action to return only the activity panel
            }).done(showCollapsed);
        }

        function showCollapsed(html) {
            var commentsToShow = $(html).find('.activity-comment').slice(numCommentsBefore, numCommentsBefore + numCollapsed);
            collapsedLinkBlock.replaceWith(commentsToShow);
            JIRA.trace("jira.issue.comment.expanded");
            JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [module, JIRA.CONTENT_ADDED_REASON.panelRefreshed]);

            // Expand the twixi for the first comment
            container.find('.activity-comment:first').removeClass('collapsed').addClass('expanded');
        }
    });
});
