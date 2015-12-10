(function ($) {

    JIRA.Events.VALIDATE_TIMETRACKING = "validateTimeTracking";

    function toggleTimeTrackingContainer(context, activate) {

        var $logWorkContainer = $(context).find("#worklog-logworkcontainer"),
            $timeTrackingContainer = $(context).find("#worklog-timetrackingcontainer"),
            $logWorkCheckbox = $(context).find("#log-work-activate");

        if (activate) {
            $logWorkContainer.removeClass("hidden");
            $timeTrackingContainer.addClass("hidden");
            $logWorkCheckbox.prop("checked", true);
        } else {
            $logWorkContainer.addClass("hidden");
            $timeTrackingContainer.removeClass("hidden");
            $logWorkCheckbox.prop("checked", false);
        }
    }

    function applyLogworkControls(context) {

        $('#log-work-adjust-estimate-new-value, #log-work-adjust-estimate-manual-value', context).attr('disabled','disabled');

        $('#log-work-adjust-estimate-'+$('input[name=worklog_adjustEstimate]:checked,input[name=adjustEstimate]:checked', context).val()+'-value', context).removeAttr('disabled');
        $('input[name=worklog_adjustEstimate],input[name=adjustEstimate]', context).change(function(){
            $('#log-work-adjust-estimate-new-value,#log-work-adjust-estimate-manual-value', context).attr('disabled','disabled');
            $('#log-work-adjust-estimate-'+$(this).val()+'-value', context).removeAttr('disabled');
        });

        $("#delete-log-work-adjust-estimate-new-value").change(function() {
            $("#delete-log-work-adjust-estimate-new").prop("checked", true);
        });
        $("#delete-log-work-adjust-estimate-manual-value").change(function() {
            $("#delete-log-work-adjust-estimate-manual").prop("checked", true);
        });

        $(context).find("#log-work-activate").change(function() {
            toggleTimeTrackingContainer(context, $(this).is(":checked"));
        });
    }

    // In Quick Edit/Create we need to ensure the container is visible to append error messages
    JIRA.bind(JIRA.Events.VALIDATE_TIMETRACKING, function (e, context) {
        toggleTimeTrackingContainer(context, true);
    });

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            applyLogworkControls(context);
        }
    });

})(AJS.$);