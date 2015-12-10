function columnGadgetFieldType (gadget, name, availableColumns)
{
    var dataModel = new FilterResultsColumnData (availableColumns, gadget.getPref(name));
    var columnPicker = new ColumnPicker (gadget, name, dataModel);

    AJS.$(document).bind("column-data-item-selected", function () {
        gadget.resize();
    });
    AJS.$(document).bind("column-data-item-unselected", function () {
        gadget.resize();
    });

    return {
        id: "columnNames",
        label: gadget.getMsg("gadget.issuetable.common.fields.to.display"),
        type: "callbackBuilder",
        callback: function(parentDiv) {
            columnPicker.initalize(parentDiv);
        }
    };
};

function ColumnPicker (gadget, fieldName, dataModel) {
    this.fieldName = fieldName;
    this.dataModel = dataModel;

    /* This is the function that inserts the column picker control into the gadget's edit panel */
    this.initalize = function (parentDiv) {
        var tableElement = AJS.$("<table/>").attr ({
            id: "column-picker-restful-table"
        }).appendTo(parentDiv);
        var tableHelpTextElement = AJS.$("<div/>").attr ({
            id: "column-picker-table-helpText",
            'class': "description"
        }).appendTo(parentDiv);

        AJS.$("<p/>").appendTo(parentDiv);

        // Populate the select element before adding it to the DOM for better performance
        var selectElement = AJS.$("<select/>").attr ({
            id: "column-picker-select",
            'class': "select"
        });
        var addButton = AJS.$("<input/>").attr ({
            id: "column-picker-add",
            type: "button",
            value: gadget.getMsg("gadget.issuetable.common.fields.add"),
            'class': "button"
        });

        createSelectElement (selectElement, addButton, dataModel);
        var selectWrapper = AJS.$("<div></div>").addClass("column-picker-select-wrapper").appendTo(parentDiv);
        selectElement.appendTo(selectWrapper);
        addButton.appendTo(selectWrapper);

        var selectHelpTextElement = AJS.$("<div/>").attr ({
            id: "column-picker-select-helpText",
            'class': "description"
        }).appendTo(parentDiv);
        var submitElement = AJS.$("<div/>").attr ({
            id: "column-picker-hidden-submit",
            'class': "hidden"
        }).appendTo(parentDiv);


        createRestfulTable(tableElement,
                [{
                    id: "label",
                    header: "Name",
                    allowEdit: false
                }],
                this.dataModel.getSelectedColumns()
        );
        createSubmitElement (submitElement, fieldName, dataModel);
        createHelpText (tableHelpTextElement, selectHelpTextElement);
    };

    function createRestfulTable(table, columns, entries) {
        var reorderable = function (rowClass) {
            return rowClass.extend({
                initialize: function (options) {
                    rowClass.prototype.initialize.call(this, AJS.$.extend({}, options, { allowReorder:true }));
                }
            })
        };

        var restyled = function (rowClass) {
            //noinspection JSUnusedGlobalSymbols
            return rowClass.extend({
                renderOperations: function () {
                    var instance = this;
                    return AJS.$('<a id="column-picker-delete-' + AJS.escapeHtml(instance.model.attributes.value) + '" title="' + gadget.getMsg("gadget.issuetable.common.column.delete")
                            + '"><span>" + gadget.getMsg("gadget.issuetable.common.column.delete") + "</span></a>')
                        .addClass(this.classNames.DELETE)
                        .addClass("icon-delete")
                        .addClass("icon")
                        .click(function () {
                            instance.destroy();
                        });
                    },
                events: {
                    "click .aui-restfultable-editable" : "edit",
                    "mouseover" : 'handleMouseOver',
                    "mouseout" : 'handleMouseOut'
                },
                /* These should ideally be handled in CSS but IE8 and IE9 are not respecting the :hover state */
                handleMouseOver: function (ev) {
                     AJS.$(this.el.children).each( function () { this.bgColor = "#f0f0f0"; } );
                },
                handleMouseOut: function (ev) {
                     AJS.$(this.el.children).each( function () { this.bgColor = ""; } );
                }
            });
        };

        var makeSortable = function (instance) {
            // This is straight copied and pasted from restfultable.js,
            // but modified to avoid making an AJAX call
            // TODO: file an issue to make this pluggable

            // Add allowance for another cell to the thead
            instance.$theadRow.prepend("<th />");

            // Allow drag and drop reordering of rows
            instance.$tbody.sortable({
                handle: "." + instance.classNames.DRAG_HANDLE,
                helper: function(e, elt) {
                    var helper =  elt.clone(true).addClass(instance.classNames.MOVEABLE);
                    helper.children().each(function (i) {
                        AJS.$(this).width(elt.children().eq(i).width());
                    });
                    return helper;
                },
                start: function (event, ui) {
                    var $this = ui.placeholder.find("td");
                    // Make sure that when we start dragging widths do not change
                    ui.item
                            .addClass(instance.classNames.MOVEABLE)
                            .children().each(function (i) {
                                AJS.$(this).width($this.eq(i).width());
                            });

                    // Add a <td> to the placeholder <tr> to inherit CSS styles.
                    ui.placeholder
                            .html('<td colspan="' + instance.getColumnCount() + '">&nbsp;</td>')
                            .css("visibility", "visible");

                    // Stop hover effects etc from occuring as we move the mouse (while dragging) over other rows
                    instance.getRowFromElement(ui.item[0]).trigger(instance._event.MODAL);
                },
                stop: function (event, ui) {
                    ui.item
                            .removeClass(instance.classNames.MOVEABLE)
                            .children().attr("style", "");

                    ui.placeholder.removeClass(instance.classNames.ROW);

                    // Return table to a normal state
                    instance.getRowFromElement(ui.item[0]).trigger(instance._event.MODELESS);
                },
                update: function (event, ui) {
                    var row = instance.getRowFromElement(ui.item[0]);

                    if (row) {
                        AJS.triggerEvtForInst(instance._event.REORDER_SUCCESS, instance, []);
                    }
                },
                axis: "y",
                delay: 0,
                containment: "document",
                cursor: "move",
                scroll: true,
                zIndex: 8000
            });

            // Prevent text selection while reordering.
            instance.$tbody.bind("selectstart mousedown", function (event) {
                return !AJS.$(event.target).is("." + instance.classNames.DRAG_HANDLE);
            });
        };

        var restfulTable = new AJS.RestfulTable({
            el: table,
            allowCreate: false,
            allowReorder: false, // NOTE: this is overridden at the row level via the "reorderable" decorator
            columns: columns,
            resources: {
                all: function (populate) {
                    populate(entries);
                }
            },
            model: Backbone.Model.extend({
                sync: function (method, model, success) {
                    success(model.toJSON());
                }
            }),
            views:{
                row: restyled(reorderable(AJS.RestfulTable.Row)),
                editRow: reorderable(AJS.RestfulTable.EditRow)
            }
        });
        makeSortable(restfulTable);
        restfulTable.isRefreshingTable = false;

        // Events triggered when new fields are added or removed from the table
        AJS.$(document).bind("column-data-item-selected", refreshTableData);
        AJS.$(document).bind("column-data-item-unselected", refreshTableData);

        function refreshTableData (document, data) {
            restfulTable.isRefreshingTable = true;
            AJS.$(restfulTable.getRows()).each(function () {
                restfulTable.removeRow(this);
            });
            restfulTable.populate(data.dataModel.getSelectedColumns());
            restfulTable.isRefreshingTable = false;
        }

        // Event triggered when items are reordered in the table.
        AJS.$(restfulTable).bind(AJS.RestfulTable.Events.REORDER_SUCCESS, function (event) {
            var selectedColumns = [];
            AJS.$(event.target.getRows()).each(function () {
                selectedColumns.push(
                        {
                            label: this.model.attributes.label,
                            value: this.model.attributes.value
                        });
            });
            dataModel.reorderSelectedColumns(selectedColumns);
        });

        // Event triggered when items are removed from the table.
        AJS.$(restfulTable).bind(AJS.RestfulTable.Events.ROW_REMOVED, function (event, row) {
            // Don't update the data model if we're processing a change triggered by a change in the data model
            if (!restfulTable.isRefreshingTable) {
                dataModel.unselectColumn(row.model.attributes.value);
            }
        });
    }

    function createSelectElement (selectElement, addButton, dataModel) {
        populateSelectData (selectElement, dataModel);

        // Notify the data model when an item is selected, by clicking the button or pressing ENTER on the list
        addButton.click(addSelectedItem);
        selectElement.keyup(function (event) {
            if (event.which == 13) {
                addSelectedItem();
            }
        })

        function addSelectedItem() {
            var newValue = selectElement.val();
            if (newValue != null && newValue != "") {
                dataModel.selectColumn(newValue);
            }
        }

        // Event triggered when a field is selected, and should disappear from this list.
        AJS.$(document).bind("column-data-item-selected", function (document, data) {
            selectElement.children("[value=" + data.column.value + "]").remove();
        });

        // Event triggered when a field is unselected, and should reappear in this list in the right place.
        AJS.$(document).bind("column-data-item-unselected", function (document, data) {
            selectElement.children(":eq(" + data.index + ")").after(
                AJS.$("<option>").attr("value", data.column.value).text(data.column.label));
        });
    }

    function populateSelectData (selectElement, dataModel) {
        var unselectedColumns = dataModel.getUnselectedColumns();
        var fields = [];
        selectElement.append(AJS.$("<option></option>",
                { value: "",
                  text: gadget.getMsg("gadget.issuetable.common.column.picker.prompt") }));
        AJS.$(unselectedColumns).each(function () {
            fields.push('<option value="'+AJS.escapeHtml(this.value)+'">'+AJS.escapeHtml(this.label)+'</option>');
        });
        selectElement.append(AJS.$(fields.join("")));
    }

    // This is a set of hidden fields that provides the value submitted back to the server when "Save" is clicked.
    function createSubmitElement (submitElement, fieldName, dataModel) {
        synchronizeSubmitData (submitElement, fieldName, dataModel);

        // An item has been selected, add it to the end of the list.
        AJS.$(document).bind("column-data-item-selected", function (document, data) {
            submitElement.append(
                    AJS.$("<input>").attr("type", "hidden").attr("name", fieldName).attr("value", data.column.value));
        });

        // An item has been unselected, remove it from the list.
        AJS.$(document).bind("column-data-item-unselected", function (document, data) {
            submitElement.children("[value=" + data.column.value + "]").remove();
        });

        // The items have been reordered, re-generate the list.
        AJS.$(document).bind("column-data-reordered", function (document, data) {
            synchronizeSubmitData (submitElement, fieldName, data.dataModel);
        });
    }

    function synchronizeSubmitData (submitElement, fieldName, dataModel) {
        submitElement.empty();
        AJS.$.each(dataModel.getSelectedColumns(), function () {
           submitElement.append(AJS.$("<input>").attr("type", "hidden").attr("name", fieldName).attr("value", this.value));
        });
    }

    function createHelpText (tableHelpTextElement, selectHelpTextElement) {
        tableHelpTextElement.text(gadget.getMsg("gadget.issuetable.common.column.reorder.instructions"));
        selectHelpTextElement.text(gadget.getMsg("gadget.issuetable.common.column.picker.instructions"));
    }
}
