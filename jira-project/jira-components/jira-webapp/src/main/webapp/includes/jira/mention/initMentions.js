define('jira/mention/init-mentions', [
    'jira/mention/mention',
    'jquery'
], function(
    Mention,
    $
) {
    var mentionsCtr = Mention;
    var mentionsController;

    function initMentions() {
        if (!mentionsController) {
            mentionsController = new mentionsCtr();
        }
        mentionsController.textarea(this);
    }

    $(document).delegate(".mentionable", "focus", initMentions);
    $(".mentionable").each(initMentions);
});

AJS.$(function() {
    // Initialize mentions
    require('jira/mention/init-mentions');
});