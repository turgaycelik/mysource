AJS.$(function () {

    var $activeWrap,
        $activeArea;

    function setActiveAreaWidth() {

        var activeAreaWidth,
            activeWrapWidth;

        if (!$activeWrap) {
            $activeWrap = AJS.$("div.admin-active-wrap");
            $activeArea = AJS.$("div.admin-active-area");
        }

        activeAreaWidth = $activeArea.prop("scrollWidth");
        activeWrapWidth = $activeWrap.width();

        if (activeAreaWidth >  activeWrapWidth) {
            $activeWrap.width(activeAreaWidth);
        } else {
            $activeWrap.css({
                width: ""
            });
        }
    }

    // Edit project dialog on Projects page
    AJS.$("#project-list .edit-project").each(function () {
        JIRA.createEditProjectDialog(this);
    });
});

JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
    if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
        context.find('.jira-iconpicker-trigger').click(function(e) {
            e.preventDefault();
            var $this = AJS.$(this);
            var url = $this.attr('href');
            var $iconPickerTarget = $this.prev('input.text');
            if ($iconPickerTarget.length) {
                var popup = window.open(url, 'IconPicker',
                        'status=no,resizable=yes,top=100,left=200,width=580,height=650,scrollbars=yes');
                popup.focus();
            }
        });
    }
});

AJS.$(function(){
    // Ensure hitting Enter when focused in one of the <select> elements does the correct action.
    // Need to add hidden fields because when in a dialog the buttons are disabled and the Action is using them
    var editUserGroupsDataSetter = function(e) {
        var $trigger = AJS.$(e.target),
                $form = $trigger.closest("form");
        var action = "";
        if ($trigger.attr('name').toLowerCase().indexOf('join') > -1) {
            action = "join";
        }
        else {
            action = "leave";
        }
        AJS.$('<input type="hidden" />').attr({name: action, value: action}).appendTo($form);
    };
    AJS.$(document.body).on('keypress', "#userGroupPicker select", function(e) {
        if (e.which === 13) {
            editUserGroupsDataSetter.apply(this, arguments);
            var $form = AJS.$(this).closest('form');
            $form.find('.aui-button').prop('disabled', true);
            $form.submit();
        }
    }).on('click', "#userGroupPicker .aui-button", editUserGroupsDataSetter);
});
