// Display Toggle for Diagram/Text modes
AJS.$(function($) {
    var MODE_DIAGRAM = 'diagram', MODE_TEXT = 'text', PREFERENCE_NAME = 'workflow-mode';

    function sanitiseMode(mode) {
        return mode === MODE_TEXT ? MODE_TEXT : MODE_DIAGRAM
    }

    function saveMode(mode) {
        AJS.$.ajax({
            url : contextPath + "/rest/api/2/mypreferences?key=" + PREFERENCE_NAME,
            type:  "PUT",
            contentType: "application/json",
            dataType: "json",
            data: sanitiseMode(mode)
        });

    }

    function switchMode(mode){
        var $trigger = $('#workflow-' + mode);
        if (!$trigger.hasClass('active')) {
            $('.workflow-view-toggle').each(function() {
                var $link = $(this);
                if ($trigger.is($link)) {
                    $link.addClass('active');
                } else {
                    $link.removeClass('active');
                }
            });
            $('.workflow-view').addClass('hidden');
            $($trigger.attr('href')).removeClass('hidden').trigger($.Event('show'));
            $(document.documentElement).css('overflow-y', '');
        }
    }

    //If there are toggle links then we are in view mode.
    var $workflowViewToggles = $('.workflow-view-toggle');
    $(document).on("click", ".workflow-view-toggle", function(e) {
        e.preventDefault();
        //only do something if real change
        var $el = $(this), mode;
        if (!$el.hasClass("active")) {
            mode = sanitiseMode($el.data('mode'));
            saveMode(mode);
            switchMode(mode);
        }
    });

    //else edit mode.
    if (!$workflowViewToggles.length) {
        //Save the current mode by looking for the workflow designer.
        saveMode($('#jwd').length > 0 ? MODE_DIAGRAM : MODE_TEXT);
    }

    new JIRA.FormDialog({
        id: 'edit-workflow-dialog',
        trigger: '#edit-workflow-trigger'
    });
});
