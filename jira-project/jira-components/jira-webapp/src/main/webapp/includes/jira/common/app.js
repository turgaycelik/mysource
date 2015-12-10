;(function() {
    var $ = require('jquery');
    var params = require('aui/params');
    var InlineDialog = require('aui/inline-dialog');
    var AuiTabs = require('aui/tabs');
    var ToggleBlock = require('jira/toggleblock/toggle-block');
    var describeBrowser = require('jira/ajs/browser/describe-browser');
    var Issue = require('jira/issue');
    var Messages = require('jira/message');
    var Cookie = require('jira/data/cookie');
    var Forms = require('jira/util/forms');
    var Meta = require('jira/util/data/meta');
    var Events = require('jira/util/events');
    var Types = require('jira/util/events/types');
    var Navigator = require('jira/util/navigator');

    // Cache a jQuery reference of $document
    var $document = $(document);

    // Preparing all over labels
    function initOverlabels() {
        $("label.overlabel").overlabel();
    }

    /*
     Sets the width of the issue navigator results wrapper.
     Keeps the right hand page elements within the browser view when the results table is wider than the browser view.
     Also fixes rendering issue with IE8 (JRA-18224)
     */
    function initIssueNavContainment() {
        var $issueNav = $("div.results"),
                $issueNavWrapWidth = $issueNav.width();
        $issueNav.bind("resultsWidthChanged", function () {
            var $issueNavWrap = $(this);

            $issueNavWrap.css("width", 100 / $issueNavWrapWidth * ($issueNavWrapWidth - (parseInt($(document.documentElement).prop("scrollWidth"), 10) - $(window).width())) + "%");
        });
        $(window).resize(function () {
            $issueNav.trigger("resultsWidthChanged");
        });
        $issueNav.trigger("resultsWidthChanged");

        $("#issuenav").bind("contractBlock expandBlock", function () {
            $(".results").trigger("resultsWidthChanged");
        });

    }

    // For switching tabs on field screens (edit, transition, create)
    function initFieldTabs() {
        $(".fieldTabs li").click(function(e){
            e.preventDefault();
            e.stopPropagation();
            var $this = $(this);
            if (!$this.hasClass("active")){
                $(".fieldTabs li.active").removeClass("active");
                $this.addClass("active");
                $(".fieldTabArea.active").removeClass("active");
                $("#" + $this.attr("rel")).addClass("active");
            }
        });
    }

    // Toggle form accessKeys. JRA-16102
    function initHandleAccessKeys() {
        $("form").handleAccessKeys();
        $document.bind("dialogContentReady", function () {
            $("form", this.$content).handleAccessKeys({
                selective: false // replace all access keys, not just ones in this form
            });
        });
    }

    // Hide all inline dialogs if we press escape (JRADEV-5811)
    function initHandleInlineLayerHide() {
        $document.keydown(function(e) {
            if (InlineDialog.current && e.which === 27 && !$(e.target).is(":input")) {
                InlineDialog.current.hide();
            }
        });
    }

    function initToggleBlocks() {
        new ToggleBlock({
            blockSelector: ".twixi-block",
            storageCollectionName: "twixi"
        })
        .addCallback("toggle", function () {
            Issue.getStalker().trigger("stalkerHeightUpdated");
        })
        // Comments
        .addTrigger(".action-head .action-details", "click");

        // Becuse these are inverted need to switch expanded/collapsed calsses.  Yes, I know it confusing.
        new ToggleBlock({
            blockSelector: ".twixi-block-inverted",
            collapsedClass: "expanded",
            expandedClass: "collapsed",
            storageCollectionName: "inverted-twixi"
        })
        .addCallback("toggle", function () {
            Issue.getStalker().trigger("stalkerHeightUpdated");
        });


        // Collapsing for the Simple Section
        // Default state is collapsed, so need to reverse the classes
        // Except for the text area, which is the oposite
        new ToggleBlock({
            blockSelector: "#issue-filter .toggle-wrap:not(#navigator-filter-subheading-textsearch-group)",
            triggerSelector: ".toggle-trigger",
            collapsedClass: "expanded",
            expandedClass: "collapsed",
            storageCollectionName: "navSimpleSearch"
        });
        new ToggleBlock({
            blockSelector: "#navigator-filter-subheading-textsearch-group",
            triggerSelector: ".toggle-trigger",
            storageCollectionName: "navSimpleSearchText"
        });


        // Collapsing for the Advanced section
        new ToggleBlock({
            blockSelector: "#queryBoxTable.toggle-wrap",
            triggerSelector: ".toggle-trigger",
            storageCollectionName: "navAdvanced"
        });

        // Generic twixi block
        new ToggleBlock({
            blockSelector: ".twixi-block",
            triggerSelector: ".twixi-trigger",
            storageCollectionName: "twixi"
        });

        new ToggleBlock({
            blockSelector: "#issuenav",
            triggerSelector: "a.toggle-lhc",
            collapsedClass: "lhc-collapsed",
            storageCollectionName: "lhc-state",
            autoFocusTrigger: false
        });

        // If a section has an error contained in it, it should be shown
        $(".error", "#issue-filter").parents(".toggle-wrap").removeClass("collapsed").addClass("expanded");

        $("fieldset.content-toggle input[type='radio']").live("change", function () {
            var $this = $(this);
            $this.closest(".content-toggle").find("input[type='radio']").each(function () {
                var $this = $(this);
                $("#" + $this.attr("name") + "-" + $this.val() + "-content").addClass("hidden");
            });
            $("#" + $this.attr("name") + "-" + $this.val() + "-content").removeClass("hidden");
        });
    }

    /* Logwork radio behaviour to disable/enable corresponding text inputs */
    function initLogWork() {
        $('#log-work-adjust-estimate-new-value,#log-work-adjust-estimate-manual-value').attr('disabled','disabled');
        $('#log-work-adjust-estimate-'+$('input[name=worklog_adjustEstimate]:checked,input[name=adjustEstimate]:checked').val()+'-value').removeAttr('disabled');
        $('input[name=worklog_adjustEstimate],input[name=adjustEstimate]').change(function(){
            $('#log-work-adjust-estimate-new-value,#log-work-adjust-estimate-manual-value').attr('disabled','disabled');
            $('#log-work-adjust-estimate-'+$(this).val()+'-value').removeAttr('disabled');
        });
    }

    // Make sure that we display one of the panels on page load (if there is a selected radio).
    function initLogin() {
        var radio = $('input:checked');
        if (radio.length !== 0) {
            if (radio.attr('id') === 'forgot-login-rb-forgot-password') {
                $('#username,#password').addClass('hidden');
                $('#password').removeClass('hidden');
            }
            else if (radio.attr('id') === 'forgot-login-rb-forgot-username') {
                $('#username,#password').addClass('hidden');
                $('#username').removeClass('hidden');
            }
        }

        // Swap the panels if one of the radio's is selected
        $('#forgot-login-rb-forgot-password').change(function() {
            $('#username,#password').addClass('hidden');
            $('#password').removeClass('hidden');
        });
        $('#forgot-login-rb-forgot-username').change(function() {
            $('#username,#password').addClass('hidden');
            $('#username').removeClass('hidden');
        });
    }

    /* File input field-group repeaters */
    function initFileRadio() {
        $("input.upfile").each(function() {
            var input = $(this),
                container = input.closest(".field-group");
            input.change(function () {
                if (input.val().length > 0) {
                    container.next('.field-group').removeClass('hidden');
                }
            });
        });
    }

    /**
     * Ctrl-Enter should work for text areas
     */
    function initHandleEnterInTextarea() {
        $document.on("keypress", "textarea", Forms.submitOnCtrlEnter);
        // For the JQL text box - we want to submit on Enter.
        // This is used for the old issue naviator (i.e. not Kickass) only.
        $("#jqltext").keypress(Forms.submitOnEnter);
    }

    /**
     * Warn if using an unsupported browser
     */
    function initUnsupportedBrowserWarning() {
        var $warning = $("#browser-warning");
        $(".icon-close",$warning).click(function () {
            $warning.slideUp("fast");
            Cookie.save("UNSUPPORTED_BROWSER_WARNING", "handled");
        });
    }

    /**
     * Make normal forms (non-ajax) still conform to the api we have for dialog forms etc. Disabling submission
     * by preventing default on before submit event.
     */
    function initHandleFormSubmit() {
        $("form").submit(function(e) {
            var event = new $.Event("before-submit");
            $(this).trigger(event);
            if (event.isDefaultPrevented()) {
                e.preventDefault();
            }
        });
    }

    /**
     * Textareas that expand on input.
     */
    function initExpandOnInput() {
        var selector = '#comment, #environment, #description',
            maxTextareaHeight = 200;

        $document.bind('tabSelect', function (e, data) {
            data.pane.find(selector).expandOnInput();
        });

        $(selector).expandOnInput(maxTextareaHeight);

        $document.bind('dialogContentReady', function (e, dialog) {
            dialog.get$popupContent()
                    .bind('tabSelect', function (e, data) {
                        data.pane.find(selector).expandOnInput(maxTextareaHeight);
                    })
                    .find(selector)
                    .expandOnInput(maxTextareaHeight);
        });

        // Bind to the event triggered by toggling the wiki markup preview.
        $document.bind('showWikiInput', function (e, $container) {
            $container.find(selector).expandOnInput();
        });
    }

    function initAuiTabHandling() {
        // Ensure tabs are initiated in dialogs. Used in Quick Edit/Quick Create
        Events.bind("dialogContentReady", function () {
            AuiTabs.setup();
        });
    }

    // We want people to cancel forms like they used to when cancel was a button.
    // JRADEV-1823 - Alt-` does not work in IE
    function initCancelFormHandling() {
        var $auiForm = $("form.aui");
        var $cancel = $("a.cancel", $auiForm);
        if (Navigator.isIE() && Navigator.majorVersion() < 12 && $cancel.attr("accessKey")) {
            $cancel.focus(function(e){
                if (e.altKey) {
                    //simulate a click (for the dirty form filter) then follow the link!
                    $(this).mousedown();
                    window.location.href = $cancel.attr("href");
                }
            });
        }
    }

    // Initialise the bulk edit screen to make checkboxes autoselect on :input change events.
    function initBulkEditCheckboxes() {
        var checkRow = function(input){
            $(input).closest(".availableActionRow").find("td:first :checkbox").attr('checked', true);
        };
        var $rows = $("#availableActionsTable tr.availableActionRow");
        $rows.children("td:last-child").find(":input").change(function(e){
            checkRow(this);
        });
    }

    function initPerformanceMonitor() {
        if(params.showmonitor) {
            var $div = $("<div class='perf-monitor'/>");
            var slowRequest = params["jira.request.server.time"] > 2000,
                    tooManySql = params.jiraSQLstatements > 50;
            if(slowRequest) {
                $div.addClass("tooslow");
            }
            if(tooManySql) {
                $div.addClass("toomanysql");
            }

            $("#header-top").append($div);


            InlineDialog($div, "perf-monitor-dialog",
                    function($contents, control, show) {
                        var timingInfo = "<div>Page render time <strong>" + params["jira.request.server.time"] + " ms</strong>";
                        if(params.jiraSQLstatements) {
                            timingInfo += " / SQL <strong>" + params.jiraSQLstatements + "@" + params.jiraSQLtime + " ms</strong></br>";
                            timingInfo += "<a target=\"_blank\" href=" + contextPath + "/sqldata.jsp?requestId=" + params["jira.request.id"] + ">More...</a>";
                        } else {
                            timingInfo += " / No SQL statments";
                        }
                        timingInfo +="</div>";
                        $contents.empty().append(timingInfo);
                        show();
                    });
        }
    }

    function initShareItem($ctx) {
        $(".shared-item-trigger", $ctx).each(function() {
            var target = $(this).attr('href');
            InlineDialog(this, target.substring(1), function(contents, trigger, showPopup){
                contents.html($(target).html());
                showPopup();
            }, { width: 240 });
        });
    }

    function initClickables() {
        $(".clickable").click(function() {
            window.location.href = $(this).find("a").attr("href");
        });
    }

    function initProjectsList($ctx) {
        $(".projects-list-trigger", $ctx).each(function() {
            var $trigger = $(this);
            $trigger.click(false);
            var target = $trigger.attr('href');
            InlineDialog(this, target.substring(1), function(contents, trigger, showPopup) {
                contents.html($(target).html());
                showPopup();
            }, {
                onHover: true,
                hideDelay: 500,
                width: 240
            });
        });
    }

    function initHelpLinks() {
        $document.on("click", "[data-helplink=local]", function(e) {
            var url = this.getAttribute('href');
            var child = window.open(url, 'jiraLocalHelp', 'resizable, scrollbars=yes');
            child.focus();
            e.preventDefault();
            return false;
        });
    }

    // document ready
    $(function () {
        initToggleBlocks();
        initOverlabels();
        initIssueNavContainment();
        initFieldTabs();
        initHandleAccessKeys();
        initLogWork();
        initLogin();
        initFileRadio();
        initHandleEnterInTextarea();
        initUnsupportedBrowserWarning();
        initHandleFormSubmit();
        initExpandOnInput();
        initCancelFormHandling();
        initBulkEditCheckboxes();
        initHandleInlineLayerHide();
        initPerformanceMonitor();
        initClickables();
        initHelpLinks();
        initProjectsList();
    });

    // Run straight away
    describeBrowser(); // Add classNames describing the browser, i.e name and version, to html tag.
    initAuiTabHandling();

    Events.bind(Types.NEW_CONTENT_ADDED, function (e, $ctx) {
        initShareItem($ctx);
    });

    Events.bind(Types.NEW_CONTENT_ADDED, function(event, $context) {
        // Show a success message after saving a search filter.
        $context.find("#filter-edit").on("submit", function(event) {
            if (!event.isDefaultPrevented()) {
                var filterName = AJS.escapeHtml(this.elements.filterName.value);
                Messages.showMsgOnReload(AJS.I18n.getText("editfilter.save.success", filterName), {
                    type: "SUCCESS",
                    closeable: true,
                    target: "body:not(:has(#filter-edit))" // Only show the success message when redirected.
                });
            }
        });

        // Show a success message after adding a subscription.
        $context.find('#filter-subscription').on("submit", function(event) {
            if (!event.isDefaultPrevented()) {
                var recipient = $('select[name="groupName"]').val() || Meta.get('remote-user-fullname');
                Messages.showMsgOnReload(AJS.I18n.getText("subscriptions.add.success", AJS.escapeHtml(recipient)), {
                    type: "SUCCESS",
                    closeable: true,
                    target: "body:not(:has(#filter-subscription))" // Only show the success message when redirected.
                });
            }
        });
    });
})();
