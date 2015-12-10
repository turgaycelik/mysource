AJS.namespace("AJS.gadget.fields");

AJS.gadget.fields.filterPicker = function(gadget, userpref){
    if (!gadget.projectOrFilterName){
        gadget.projectOrFilterName = gadget.getMsg("gadget.common.filter.none.selected");
    }

    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.filter.label"),
        description:gadget.getMsg("gadget.common.filter.description"),
        id: "proj_filter_picker_" + userpref,
        type: "callbackBuilder",
        callback: function(parentDiv){
            parentDiv.append(
                AJS.$("<input/>").attr({
                    id: "filter_" + userpref + "_id",
                    type: "hidden",
                    name: userpref
                }).val(gadget.getPref(userpref))
            ).append(
                AJS.$("<span/>").attr({id:"filter_" + userpref + "_name"}).addClass("filterpicker-value-name field-value").text(gadget.projectOrFilterName)
            );
            parentDiv.append(
                AJS.$("<div/>").attr("id", "quickfind-container").append(
                    AJS.$("<label/>").addClass("overlabel").attr({
                        "for":"quickfind",
                        id: "quickfind-label"
                    }).text(gadget.getMsg("gadget.common.quick.find"))
                ).append(
                    AJS.$("<input class='text' />").attr("id", "quickfind")
                ).append(
                    AJS.$("<span/>").addClass("inline-error")
                )
            );

            if (gadget.isLocal()){
                parentDiv.append(
                    AJS.$("<a href='#'/>").addClass("advanced-search").attr({
                        id: "filter_" + userpref + "_advance",
                        title: gadget.getMsg("gadget.common.filterid.edit")
                    }).text(gadget.getMsg("gadget.common.advanced.search")).click(function(e){
                        var url = jQuery.ajaxSettings.baseUrl + "/secure/FilterPickerPopup.jspa?showProjects=false&field=" + userpref;
                        var windowVal = "filter_" + userpref + "_window";
                        var prefs = "width=800, height=500, resizable, scrollbars=yes";

                        var newWindow = window.open(url, windowVal, prefs);
                        newWindow.focus();
                        e.preventDefault();
                    })
                );
            }


            AJS.gadget.fields.applyOverLabel("quickfind-label");
            AJS.gadget.fields.autocomplete.Filters({
                fieldID: "quickfind",
                ajaxData: {},
                baseUrl: jQuery.ajaxSettings.baseUrl,
                relatedId: "filter_" + userpref + "_id",
                relatedDisplayId: "filter_" + userpref + "_name",
                filtersLabel: gadget.getMsg("gadget.common.filters"),
                gadget: gadget
            });
        }
    };
};

AJS.gadget.fields.projectPicker = function(gadget, userpref, options){
    if (!gadget.title){
        gadget.title = gadget.getMsg("gadget.common.project.none.selected");
    }

    if(!AJS.$.isArray(options.options)){
        options.options = [options.options];
    }
    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.project.label"),
        description:gadget.getMsg("gadget.common.project.description"),
        type: "select",
        selected: gadget.getPref(userpref),
        options: options.options
    };
};

AJS.gadget.fields.projectsAndCategoriesPicker = function(gadget, userpref, options) {
    if (!options){
        options = {};
    }
    if (!options.projects){
        options.projects = [{label:gadget.getMsg("gadget.common.projects.all"), value: "allprojects"}];
    }

    if(options.projects[0].value !== "allprojects"){
        options.projects = AJS.$.merge([{label:gadget.getMsg("gadget.common.projects.all"), value: "allprojects"}], options.projects);
    }

    if (!options.categories || options.categories.length == 0){
        return {
            userpref: userpref,
            label: gadget.getMsg("gadget.common.projects.label"),
            description:gadget.getMsg("gadget.common.projects.description"),
            type: "multiselect",
            selected: gadget.getPref(userpref),
            options: options.projects,
            value: gadget.getPrefArray(userpref)
        };
    }
    var optionList =  [
        {
            group :
            {
                label: gadget.getMsg("gadget.common.projects"),
                options: options.projects
            }
        },
        {
            group :
            {
                label: gadget.getMsg("gadget.common.categories"),
                options: options.categories
            }
        }
    ];

    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.projects.and.categories.label"),
        description:gadget.getMsg("gadget.common.projects.and.categories.description"),
        type: "multiselect",
        selected: gadget.getPref(userpref),
        options: optionList,
        value: gadget.getPrefArray(userpref)
    };
};

AJS.gadget.fields.projectsOrCategoriesPicker = function(gadget, userpref, options) {
    if (!options){
        options = {};
    }
    if (!options.projects || options.projects.length == 0){
        options.projects = [{label:gadget.getMsg("gadget.common.projects.all"), value: "allprojects"}];
    }

    if(options.projects[0].value !== "allprojects"){
        options.projects = AJS.$.merge([{label:gadget.getMsg("gadget.common.projects.all"), value: "allprojects"}], options.projects);
    }

    if (!options.categories || options.categories.length == 0){
        return {
            userpref: userpref,
            label: gadget.getMsg("gadget.common.projects.label"),
            description:gadget.getMsg("gadget.common.projects.description"),
            type: "multiselect",
            selected: gadget.getPref(userpref),
            options: options.projects,
            value: gadget.getPrefArray(userpref)
        };
    }

    var setOptions = function(optionList, selected){
        var selectList = AJS.$("#proj_cat_picker_" + userpref + " select");
        selectList.empty();
        AJS.$(optionList).each(function(){
            selectList.append(
                AJS.$("<option/>").attr("value", this.value).text(this.label)
            );
        });

        selectList.val(selected);
        var selectedOptions = selectList.val();
        if (selectedOptions){
            AJS.$("#" + userpref).val(selectedOptions.join("|"));
        } else {
            AJS.$("#" + userpref).val("");
        }

    };

    if(options.categories[0].value !== "catallCategories"){
        options.categories = AJS.$.merge([{label:gadget.getMsg("gadget.common.categories.all"), value: "catallCategories"}], options.categories);
    }

    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.projects.and.categories.label"),
        description:gadget.getMsg("gadget.common.projects.and.categories.description"),
        id: "proj_cat_picker_" + userpref,
        type: "callbackBuilder",
        callback: function(parentDiv){

            parentDiv
                .append(
                    AJS.$('<div class="radio"/>')
                        .append(AJS.$('<input type="radio" id="catOrProj_' + userpref + '_proj" name="catOrProj_' + userpref + '" value="projects" class="radio proj-cat-option" />'))
                        .append(AJS.$('<label for="catOrProj_' + userpref + '_proj" />').text(gadget.getMsg("gadget.common.projects"))))
                .append(
                    AJS.$('<div class="radio"/>')
                    .append(AJS.$('<input type="radio" id="catOrProj_' + userpref + '_cat" name="catOrProj_' + userpref + '" value="categories" class="radio proj-cat-option" />'))
                    .append(AJS.$('<label for="catOrProj_' + userpref + '_cat" />').text(gadget.getMsg("gadget.common.categories"))))
                .append(AJS.$('<select style="margin-top: 5px;" />').attr({multiple:"multiple"}).addClass("select multi-select"))
                .append(AJS.$("<input/>").attr({id: userpref, name: userpref, type:"hidden", value:gadget.getPref(userpref)})
            );

            var prefs =  gadget.getPrefArray(userpref);

            var radioButtons = AJS.$("#proj_cat_picker_" + userpref + " input[type='radio']");
            var selectList = AJS.$("#proj_cat_picker_" + userpref + " select");
            if (prefs && prefs.length > 0){
                var first = prefs[0];
                if (/^cat/.test(first)){
                    radioButtons.val(["categories"]);
                    setOptions(options.categories, prefs);
                } else {
                    radioButtons.val(["projects"]);
                    setOptions(options.projects, prefs);

                }
            } else {
                radioButtons.val(["projects"]);
                setOptions(options.projects);
            }

            var selectedTypes = {};
            radioButtons.click(function(e){
                var type = AJS.$(this).attr("checked", "checked").val();
                if (type === "projects"){
                    selectedTypes.categories = selectList.val();
                    setOptions(options.projects, selectedTypes.projects);
                    gadget.resize();
                } else {
                    selectedTypes.projects = selectList.val();
                    setOptions(options.categories, selectedTypes.categories);
                    gadget.resize();
                }
            });
            var hiddenPref = AJS.$("#" + userpref);
            selectList.change(function(){
                var selectedOptions = selectList.val();
                if (selectedOptions){
                    hiddenPref.val(selectedOptions.join("|"));
                } else {
                    hiddenPref.val("");
                }
            });

        }
    };


};

AJS.gadget.fields.projectOrFilterPicker = function(gadget, userpref){
    if (!gadget.projectOrFilterName){
        gadget.projectOrFilterName = gadget.getMsg("gadget.common.filterid.none.selected");
    }


    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.filterid.label"),
        description:gadget.getMsg("gadget.common.filterid.description"),
        id: "proj_filter_picker_" + userpref,
        type: "callbackBuilder",
        callback: function(parentDiv){
            parentDiv.append(
                AJS.$("<input/>").attr({
                    id: "filter_" + userpref + "_id",
                    type: "hidden",
                    name: userpref
                }).val(gadget.getPref(userpref))
            ).append(
                AJS.$("<span/>").attr({id:"filter_" + userpref + "_name"}).addClass("filterpicker-value-name field-value").text(gadget.projectOrFilterName)
            );
            parentDiv.append(
                AJS.$("<div/>").attr("id", "quickfind-container").append(
                    AJS.$("<label/>").addClass("overlabel").attr({
                        "for":"quickfind",
                        id: "quickfind-label"
                    }).text(gadget.getMsg("gadget.common.quick.find"))
                ).append(
                    AJS.$("<input class='text' />").attr("id", "quickfind")
                ).append(
                    AJS.$("<span/>").addClass("inline-error")
                )                    
            );
            if (gadget.isLocal()){
                parentDiv.append(
                    AJS.$("<a href='#'/>").addClass("advanced-search").attr({
                        id: "filter_" + userpref + "_advance",
                        title: gadget.getMsg("gadget.common.filterid.edit")
                    }).text(gadget.getMsg("gadget.common.advanced.search")).click(function(e){
                        var url = jQuery.ajaxSettings.baseUrl + "/secure/FilterPickerPopup.jspa?showProjects=true&field=" + userpref;
                        var windowVal = "filter_" + userpref + "_window";
                        var prefs = "width=800, height=500, resizable, scrollbars=yes";

                        var newWindow = window.open(url, windowVal, prefs);
                        newWindow.focus();
                        e.preventDefault();
                    })
                );
            }


            AJS.gadget.fields.applyOverLabel("quickfind-label");
            AJS.gadget.fields.autocomplete.ProjectsAndFilters({
                fieldID: "quickfind",
                ajaxData: {},
                baseUrl: jQuery.ajaxSettings.baseUrl,
                relatedId: "filter_" + userpref + "_id",
                relatedDisplayId: "filter_" + userpref + "_name",
                gadget: gadget,
                filtersLabel: gadget.getMsg("gadget.common.filters"),
                projectsLabel: gadget.getMsg("gadget.common.projects")
            });
        }
    };
};

AJS.gadget.fields.period = function(gadget, userpref){
    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.period.name.label"),
        description:gadget.getMsg("gadget.common.period.name.description"),
        type: "select",
        selected: gadget.getPref(userpref),
        options:[
            {
                label:gadget.getMsg("gadget.common.period.hourly"),
                value:"hourly"
            },
            {
                label:gadget.getMsg("gadget.common.period.daily"),
                value:"daily"
            },
            {
                label:gadget.getMsg("gadget.common.period.weekly"),
                value:"weekly"
            },
            {
                label:gadget.getMsg("gadget.common.period.monthly"),
                value:"monthly"
            },
            {
                label:gadget.getMsg("gadget.common.period.quarterly"),
                value:"quarterly"
            },
            {
                label:gadget.getMsg("gadget.common.period.yearly"),
                value:"yearly"
            }
        ]
    };
};

AJS.gadget.fields.days = function(gadget, userpref, optMsgKeys){
    return {
        userpref: userpref,
        label: gadget.getMsg(optMsgKeys && optMsgKeys.label ? optMsgKeys.label : "gadget.common.days.label"),
        description: gadget.getMsg(optMsgKeys && optMsgKeys.description ? optMsgKeys.description : "gadget.common.days.description"),
        type: "text",
        value: gadget.getPref(userpref)
    };
};

AJS.gadget.fields.numberToShow = function(gadget, userpref){
    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.num.label"),
        description:gadget.getMsg("gadget.common.num.description"),
        type: "text",
        value: gadget.getPref(userpref)
    };
};

AJS.gadget.fields.cumulative = function(gadget, userpref){
    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.cumulative.label"),
        description:gadget.getMsg("gadget.common.cumulative.description"),
        type: "select",
        selected: gadget.getPref(userpref),
        options:[
            {
                label:gadget.getMsg("gadget.common.yes"),
                value:"true"
            },
            {
                label:gadget.getMsg("gadget.common.no"),
                value:"false"
            }
        ]
    };
};

AJS.gadget.fields.nowConfigured = function(){
    return {
        userpref: "isConfigured",
        type: "hidden",
        value: "true"
    };
};

AJS.gadget.fields.applyOverLabel = function(overLabelId){

    AJS.$("#" + overLabelId).each(function (){

        var label = AJS.$(this)
                .removeClass("overlabel")
                .addClass("overlabel-apply")
                .addClass("show").click(function(){
                    AJS.$("#" + AJS.$(this).attr("for")).focus()
                });

        var field = AJS.$("#" + label.attr("for"))
            .focus(function(){
                label.removeClass("show").hide();
            }).blur(function(){
                if (AJS.$(this).val() === ""){
                    label.addClass("show").show();
                }
            });

        if (field.val() !== ""){
            label.removeClass("show").hide();
        }
    });
};

AJS.namespace("AJS.gadget.fields.autocomplete");

/**
 * Project And Fitler autocomplete picker
 */
AJS.gadget.fields.autocomplete.ProjectsAndFilters = function(options) {

    // prototypial inheritance (http://javascript.crockford.com/prototypal.html)
    var that = begetObject(JIRA.RESTAutoComplete);

    that.getAjaxParams = function(){
        return {
            url: options.baseUrl + "/rest/gadget/1.0/pickers/projectsAndFilters",
            data: {
                fieldName: options.fieldID
            },
            dataType: "json",
            type: "GET",
            global:false,
            error: function(XMLHttpRequest, textStatus, errorThrown){
                if (XMLHttpRequest.data){
                    var errorCollection = XMLHttpRequest.data.errors;
                    if (errorCollection){
                        AJS.$(errorCollection).each(function(){
                            var parent = AJS.$("#" + this.field).parent();
                            parent.find("span.inline-error").text(options.gadget.getMsg(this.error));
                        });
                    }
                }
            }
        };
    };

    that.completeField = function(value) {
        AJS.$("#" + options.relatedId).val(value.id);
        AJS.$("#" + options.relatedDisplayId).addClass("success").text(value.name);
        AJS.$("#" + options.fieldID).val("");

    };

    /**
     * Create html elements from JSON object
     * @method renderSuggestions
     * @param {Object} response - JSON object
     * @returns {Array} Multidimensional array, one column being the html element and the other being its
     * corressponding complete value.
     */
    that.renderSuggestions = function(response) {


        var resultsContainer, suggestionNodes = [];

        this.responseContainer.addClass("aui-list");


        // remove previous results
        this.clearResponseContainer();

        var parent = AJS.$("#" + options.fieldID).parent();
        parent.find("span.inline-error").text("");

        if (response && response.projects && response.projects.length > 0) {


            AJS.$("<h5/>").text(options.projectsLabel).appendTo(this.responseContainer);
            resultsContainer = AJS.$("<ul class='aui-list-section aui-first aui-last'/>").appendTo(this.responseContainer);

            jQuery(response.projects).each(function() {
                if (!this.isModified){
                    this.isModified = true;
                    this.id = "project-" + this.id;                    
                }
                // add html element and corresponding complete value  to sugestionNodes Array
                suggestionNodes.push([jQuery("<li class='aui-list-item'/>").attr("id", this.id + "_" + options.fieldID +  "_listitem").append(
                    AJS.$("<a href='#' class='aui-list-item-link aui-indented-link' />")
                            .click(function (e) {
                                e.preventDefault();
                            })
                            .html(this.html)
                ).appendTo(resultsContainer), this]);

            });
        }

        if (response && response.filters && response.filters.length > 0) {


            if (resultsContainer) {
                resultsContainer.removeClass("aui-last");
            }

            AJS.$("<h5/>").text(options.filtersLabel).appendTo(this.responseContainer);
            resultsContainer = AJS.$("<ul class='aui-list-section aui-first aui-last' />").appendTo(this.responseContainer);

            jQuery(response.filters).each(function() {
                if (!this.isModified){
                    this.isModified = true;
                    this.id = "filter-" + this.id;
                }

                var item = jQuery("<li class='aui-list-item'/>").attr(
                    {
                        id: this.id +"_" + options.fieldID + "_listitem"
                    }
                );


                var link = AJS.$("<a href='#' class='aui-list-item-link  aui-indented-link' />").append(
                    AJS.$("<span/>").addClass("filter-name").html(this.nameHtml)
                )
                .click(function (e) {
                    e.preventDefault();
                })                        
                .appendTo(item);

                if (this.descHtml){
                    link.append(
                        AJS.$("<span/>").addClass("filter-desc").html(" - " + this.descHtml)
                    );
                }

                item.attr("title", link.text());
                
                // add html element and corresponding complete value  to sugestionNodes Array
                suggestionNodes.push([item.appendTo(resultsContainer), this]);

            });
        }

        if (suggestionNodes.length > 0) {
            this.responseContainer.removeClass("no-results");
            that.addSuggestionControls(suggestionNodes);
        } else {
            this.responseContainer.addClass("no-results");
        }

        return suggestionNodes;

    };

    options.maxHeight = 200;

    // Use autocomplete only once the field has atleast 2 characters
    options.minQueryLength = 1;

    // wait 1/4 of after someone starts typing before going to server
    options.queryDelay = 0.25;

    that.init(options);

    return that;

};

/**
 * Filter autocomplete picker
 */
AJS.gadget.fields.autocomplete.Filters = function(options) {

    // prototypial inheritance (http://javascript.crockford.com/prototypal.html)
    var that = begetObject(JIRA.RESTAutoComplete);

    that.getAjaxParams = function(){
        return {
            url: options.baseUrl + "/rest/gadget/1.0/pickers/filters",
            data: {
                fieldName: options.fieldID
            },
            dataType: "json",
            type: "GET",
            global:false,
            error: function(XMLHttpRequest, textStatus, errorThrown){
                if (XMLHttpRequest.data){
                    var errorCollection = XMLHttpRequest.data.errors;
                    if (errorCollection){
                        AJS.$(errorCollection).each(function(){
                            var parent = AJS.$("#" + this.field).parent();
                            parent.find("span.inline-error").text(options.gadget.getMsg(this.error));
                        });
                    }
                }
            }

        };
    };

    that.completeField = function(value) {
        AJS.$("#" + options.relatedId).val(value.id);
        AJS.$("#" + options.relatedDisplayId).addClass("success").text(value.name);
        AJS.$("#" + options.fieldID).val("");
    };

    /**
     * Create html elements from JSON object
     * @method renderSuggestions
     * @param {Object} response - JSON object
     * @returns {Array} Multidimensional array, one column being the html element and the other being its
     * corressponding complete value.
     */
    that.renderSuggestions = function(response) {


        var resultsContainer, suggestionNodes = [];

        this.responseContainer.addClass("aui-list");

        // remove previous results
        this.clearResponseContainer();

        var parent = AJS.$("#" + options.fieldID).parent();
        parent.find("span.inline-error").text("");

        if (response && response.filters && response.filters.length > 0) {


            resultsContainer = AJS.$("<ul class='aui-list-section aui-first aui-last'/>").appendTo(this.responseContainer);

            jQuery(response.filters).each(function() {
                if (!this.isModified){
                    this.isModified = true;
                    this.id = "filter-" + this.id;
                }

                var item = jQuery("<li class='aui-list-item' />").attr("id", this.id +"_" + options.fieldID + "_listitem");
                var link = AJS.$("<a href='#' class='aui-list-item-link' />").append(
                        AJS.$("<span />").addClass("filter-name").html(this.nameHtml)
                    )
                    .click(function (e) {
                        e.preventDefault();
                    })
                    .appendTo(item);

                if (this.descHtml){
                    link.append(
                        AJS.$("<span />").addClass("filter-desc").html(this.descHtml)
                    );
                }

                item.attr("title", link.text());

                // add html element and corresponding complete value  to sugestionNodes Array
                suggestionNodes.push([item.appendTo(resultsContainer), this]);

            });
        }

        if (suggestionNodes.length > 0) {
            this.responseContainer.removeClass("no-results");
            that.addSuggestionControls(suggestionNodes);
        } else {
            this.responseContainer.addClass("no-results");
        }

        return suggestionNodes;

    };

    // Use autocomplete only once the field has atleast 2 characters
    options.minQueryLength = 1;

    options.maxHeight = 200;

    // wait 1/4 of after someone starts typing before going to server
    options.queryDelay = 0.25;

    that.init(options);

    return that;

};
