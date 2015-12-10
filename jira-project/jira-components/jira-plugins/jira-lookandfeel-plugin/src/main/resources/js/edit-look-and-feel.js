
(function($) {

    $(function(){
        initEditLookAndFeel();
    });

    function initEditLookAndFeel() {
        initLogoOptions();
        initFaviconOptions();
        initAutoColorMessage();
    }

    function initLogoOptions() {
        var $logoFile = AJS.$("input#logoFile");
        var $logoURL = AJS.$("input#logoUrl");
        $logoFile.change(function() {
            $logoURL.val('');
        });
        $logoURL.change(function() {
            $logoFile.val('');
        });
    }

    function initFaviconOptions() {
        var $faviconFile = AJS.$("input#faviconFile");
        var $faviconURL = AJS.$("input#faviconUrl");
        $faviconFile.change(function() {
            $faviconURL.val('');
        });
        $faviconURL.change(function() {
            $faviconFile.val('');
        });
    }

    function initAutoColorMessage() {
        var $form = $("#upload-logo-form");
        if (!$form.length) {
            return;
        }

        $.ajax({
            type: "GET",
            url: contextPath + "/rest/lookandfeel/1.0/auto/justupdated",
            contentType: "application/json",
            success: function(data) {
                if (data.isJustUpdated) {
                    var token = $form.find("input[name='atl_token']").val() || atl_token();
                    var undoAction = contextPath + "/secure/admin/LookAndFeel!restoreColorScheme.jspa?atl_token=" + token;
                    AJS.messages.info("#upload-logo-form", {
                        body: AJS.I18n.getText("jira.lookandfeel.updatecolors.justupdated", "<a href='" + undoAction + "'>", "</a>"),
                        closeable: false,
                        insert: "prepend"
                    });
                }
            }
        });
    }

})(AJS.$);
