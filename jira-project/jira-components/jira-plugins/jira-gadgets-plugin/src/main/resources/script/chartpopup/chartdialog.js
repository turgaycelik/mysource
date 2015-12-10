/**
 * @module jira.plugin.charting
 * @author Scott Harwood
 * @since 4.0
 * @requires jQuery > v1.6, jQuery.aop
 */

 /*JSLINT options*/
 /*jslint white: true, browser: true, onevar: true, plusplus: true, regexp: true, eqeqeq: true */
 /*global window, document, jQuery, dhtmlHistory, jira*/

AJS.namespace("jira.plugin.charting");

// singleton
jira.plugin.charting = function () {
    var dialog, /* {Object} jquery object */
    DIALOG_ID = "charts-dialog",
    CHARTS_CATEGORY = "Charts",
    CHART_HEIGHT = 522,
    saveFormLoaded = false,
    idToUriMap = {},
    dialogThrobber = AJS.$('<span class="icon throbber"></span>'),

    getPostData = function(formData) {
        var postData = {};
        jQuery.each(formData, function(i, field) {
            postData[field.name] = field.value;
        });
        var filterId = getFilterId();
        if(filterId) {
            postData.filterId = filterId;
        } else {
            postData.jql = getFilterJql() || "";
        }
        var chartPanelId = dialog.getPage(0).curtab;
        postData.gadgetUri = idToUriMap[chartPanelId];
        var userPrefs = jira.plugin.chartingstore.get(chartPanelId);
        if(userPrefs) {
            var prefsMap = {};
            for (var key in userPrefs) {
                prefsMap[key] = userPrefs[key];
            }
            postData.userPrefs = prefsMap;
        }

        return postData;
    },

    loadSaveToDashboardForm = function() {
        if(!saveFormLoaded) {
            var saveFormUrl = contextPath + "/rest/gadget/1.0/chart/add";
            var filterId = getFilterId();
            if(filterId) {
                saveFormUrl = saveFormUrl + "?filterId=" + encodeURIComponent(filterId);
            } else {
                saveFormUrl = saveFormUrl + "?jql=" + encodeURIComponent(getFilterJql());
            }

            var saveFormPanel = jQuery("#saveToDashboardPanel");
            saveFormPanel.empty();
            jQuery.ajax({
                url: saveFormUrl,
                success: function(data) {
                    saveFormPanel.html(data);
                    saveFormLoaded = true;
                    var chartForm = saveFormPanel.find("form#savetodashboardform");
                    var saveButton = saveFormPanel.find("#save-btn1");
                    chartForm.submit(function(e) {
                        e.preventDefault();
                        chartForm.find(".error").remove();
                        chartForm.find(".form-errors p").remove();
                        chartForm.find(".form-errors").hide();
                        saveButton.attr("disabled", "true");

                        var postData = getPostData(chartForm.serializeArray());
                        jQuery.ajax({
                            url: chartForm.attr("action"),
                            contentType:'application/json',
                            dataType: "text",
                            data: JSON.stringify(postData),
                            type:'POST',
                            success: function (data) {
                                chartForm.trigger("fakesubmit");
                                saveButton.removeAttr("disabled", "false");
                                window.location = data;
                            },
                            error: function(XMLHttpRequest, textStatus, errorThrown) {
                                saveButton.removeAttr("disabled", "false");
                                if(XMLHttpRequest.status === 400) {
                                    var errorCollection = eval("(" + XMLHttpRequest.responseText + ")");
                                    if(errorCollection.generic.length > 0) {
                                        jQuery(errorCollection.generic).each(function(i, item) {
                                        chartForm.find(".form-errors").append("<p>" + item + "</p>");
                                        });
                                        chartForm.find(".form-errors").show();
                                    }
                                    if(errorCollection.fields.length > 0) {
                                        jQuery(errorCollection.fields).each(function(i, item) {
                                            chartForm.find("#" + item.field).after("<div class=\"error\">" + item.error + "</div>");
                                        });
                                    }
                                }
                            }
                        });

                    });
                    saveFormPanel.find("#cancel-btn1").click(function(e) {
                        dialog.prevPage();
                        saveButton.removeAttr("disabled", "false");
                        chartForm.find(".form-errors").hide();
                        chartForm.find(".error").remove();
                        e.preventDefault();
                    });
                }
            });
        }
    },

    /**
     * Appends loading indicator (throbber) to all ajax requests. Indicator, controlled via css, appears in the top
     * right corner of the dialog.
     *
     * @method addThrobber
     * @private
     */
    addThrobber = function () {
        dialog.popup.element.find(".dialog-button-panel").prepend(dialogThrobber);
        jQuery().ajaxSend(function(a,xhr) {
            if (dialog.popup.element.is(":visible")) {
                jQuery(xhr).throbber({target: dialogThrobber});
            }
        });
    },

    /**
     * Handles failed ajax requests. Will subsitute the page body with the 500 error that occured.
     *
     * @method addErrorHandler
     * @private
     */
    addErrorHandler = function () {
        jQuery().ajaxError(function(e,XMLHttpRequest,ajaxOptions){
            if (XMLHttpRequest.status == 500) {
                dialog.hide();
                jQuery("body").html(XMLHttpRequest.responseText);
            }
        });
    },

    /**
     * ESC hides the dialog
     */
    keypressListener = function(e) {       
        if (e.keyCode === 27) {
            dialog.hide();
        }
    },

    /**
     * Creates dialog box. Adds global functionality including error handling and ajax throbber indicator.
     *
     * @method createDialog
     * @private
     * @param {String, Number} width - width of dialog box
     * @param {String, Number} height - height of dialog box
     */
    createDialog = function (width, height, data) {
        dialog = new AJS.Dialog(width, height, DIALOG_ID);
        dialog.addHeader(AJS.I18n.getText('navigator.results.currentview.gadgets'));
        resetPanels(data);
        dialog.addButton(AJS.I18n.getText('portletSearchRequestView.save.to.dashboard'),
                function(dialog) {
                    loadSaveToDashboardForm();
                    dialog.nextPage();
                },
                "save-to-dashboard");
        dialog.addCancel(AJS.I18n.getText('common.words.cancel'), function(dialog) {
            dialog.hide();
        });
        dialog.addPage();
        dialog.page[1].addPanel("saveToDashBoard", "<div id=\"saveToDashboardPanel\"</div>");
        dialog.page[1].addHeader(AJS.I18n.getText('portletSearchRequestView.save.to.dashboard.title'));
        addThrobber();
        addErrorHandler();
        AJS.$(document).keydown(keypressListener);
    },

    /**
     * Resets the dialog to the original state when it pops up.
     *
     * @method resetDialog
     * @param {Number} initialPanel - the panel index to go to after resetting the dialog
     * @private
     */
    resetDialog = function(initialPanel) {
        if(dialog) {
            saveFormLoaded = false;
            jQuery("form[name=chartpopup]").show();
            jQuery("input[name=filterName]").val("");
            dialog.gotoPage(0);
            for(var i = 0; dialog.getPanel(i); i++) {
                var panel = dialog.getPanel(i);
                panel.loaded = false;
            }
            dialog.gotoPanel(initialPanel);
        }
    },

    /**
     * Given a directory JSON object this method figures out all the chart gadgets to display in the popup.
     *
     * @method createDialog
     * @private
     * @param {json} data -
     */
    loadChartGadgets = function(data) {
        // Fill items on the right
        var chartGadgets = [], filterResultsGadget, uri = parseUri(window.location);
        var baseUrl = uri.protocol + "://" + uri.authority;
        jQuery(data.gadgets).each(function(i, gadget) {
            //JRADEV-4419: Only add gadgets that are hosted locally to the dialog!
            if(gadget.gadgetSpecUri.indexOf("http") !== 0 || gadget.gadgetSpecUri.indexOf(baseUrl) === 0) {
                jQuery([gadget.categories]).each(function(i, catKey) {
                    var g = {title:gadget.title, specUri:gadget.gadgetSpecUri};
                    // we want to stick the Filter Results gadget at the front of the list of gadgets, so keep a reference to it
                    if (g.specUri.indexOf("filter-results") > -1) {
                        filterResultsGadget = g;
                    }
                    if(catKey.constructor==Array) { // if there are multiple categories for this gadget, loop through all of them
                        for(var j=0; j<catKey.length; ++j) {
                            // don't add the Filter Results gadget to the array - we'll do it later
                            if(catKey[j] === CHARTS_CATEGORY && filterResultsGadget != g) {
                                chartGadgets.push(g);
                            }
                        }
                    }
                    else {
                        // don't add the Filter Results gadget to the array - we'll do it later
                        if(catKey === CHARTS_CATEGORY && filterResultsGadget != g) {
                            chartGadgets.push(g);
                        }
                    }
                });
            }
        });

        chartGadgets.sort(function(one, two) { return (one.title > two.title ? 1 : -1); });
        // add the Filter Results gadget to the start
        if (filterResultsGadget) {
            chartGadgets.unshift(filterResultsGadget);
        }
        return chartGadgets;
    },

    /**
     * Renders the contents of a particular panel for a particular chart.
     *
     * @method renderPanelContents
     * @private
     * @param {Object} panel - the panel to render
     * @param {Object} data - json object with the directory contents
     */
    renderPanelContents = function(panel, data) {
        panel.body.html(data);
        panel.loaded = true;
    },

    /**
     * Sets up all the panels when the dialog loads
     *
     * @method resetPanels
     * @private
     * @param {Object} data - json object of the directory contents
     */
    resetPanels = function(data) {
        var chartGadgets = loadChartGadgets(data);
        dialog.gotoPage(0);
        jQuery(chartGadgets).each(function(i, gadget) {
            dialog.page[0].addPanel(gadget.title, "<div class=\"loadingPanel\">&nbsp;</div>");
            var panel = dialog.getPanel(i);
            if(panel) {
                panel.loaded = false;
                idToUriMap[i] = gadget.specUri;
                panel.body.show = function (panel, show) {
                    return function() {
                        if(!panel.loaded) {
                            var rendererUrl = contextPath + "/rest/gadget/1.0/chart/render?id=" + i + "&gadgetUri=" + encodeURIComponent(gadget.specUri);
                            var filterId = getFilterId();
                            if(filterId) {
                                rendererUrl = rendererUrl + "&filterId=" + encodeURIComponent(filterId);
                            } else {
                                rendererUrl = rendererUrl + "&jql=" + encodeURIComponent(getFilterJql());
                            }

                            jQuery.ajax({
                                url: rendererUrl,
                                success: function(data) {
                                    renderPanelContents(panel, data);
                                }
                            });
                        }
                        show.call(panel.body);
                    };
                }(panel, panel.body.show);
            }
        });
    },

    /**
     * Creates a new dialog (if necessary and displays it)
     *
     * @method launchDialog
     * @private
     * @param {String} url - the url to get the direcotry JSON object from
     * @param {String, Number} width - width of dialog box
     * @param {String, Number} height - height of dialog box
     * @param {String, Number} initialPanel - the first panel to load
     */
    launchDialog = function (url, width, height, initialPanel) {
        if(dialog) {
            //this ensures that all the panels will get loaded from the server again.
            resetDialog(initialPanel);
            dialog.show();
        } else {
            jQuery(jQuery.ajax({
                url: url,
                dataType: 'json',
                success: function (data) {
                    if (!dialog) {
                        createDialog(width, height, data);
                    }
                    dialog.show();
                    dialog.gotoPage(0);
                    dialog.gotoPanel(initialPanel);
                }
            })).throbber({target: jQuery("#throbber-space")});
        }
    },

    getFilterId = function()
    {
        var filterId = AJS.Meta.get('filter-id');

        if (filterId && filterId >= 0) {
            return filterId;
        }

        return null;
    },

    getFilterJql = function()
    {
        return AJS.Meta.get('filter-jql');
    };

    return function () {
        // if they clicked on Charts we default to the first chart i.e. Panel 1
        jQuery("body").delegate("#charts", "click", function(e) {
            AJS.populateParameters();
            launchDialog(contextPath + "/rest/config/1.0/directory.json", 800, CHART_HEIGHT, 1);
            e.preventDefault();
        });
        // if they clicked on On Dashboard we default to the Filter Results gadget i.e. Panel 0
        jQuery("body").delegate("#onDashboard", "click", function(e) {
            AJS.populateParameters();
            launchDialog(contextPath + "/rest/config/1.0/directory.json", 800, CHART_HEIGHT, 0);
            e.preventDefault();
        });
    };
}();

// wait for page to fully load to make sure we have all the elements we need
jQuery(document).ready(jira.plugin.charting);

