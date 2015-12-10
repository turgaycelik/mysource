function FilterResultsColumnData (allColumns, currentConfig) {
    this.selectedColumns = [];
    this.allColumns = [];

    // Separate out the columns that are selected from the full list
    this.populateColumns = function (allColumns, currentConfig) {
        this.allColumns = allColumns;
        // Support data generated before column reordering was possible
        currentConfig = currentConfig.replace("--Default--", "issuetype|issuekey|priority|summary");
        var configuredColumns = currentConfig.split("|");

        this.populateSelectedColumns(configuredColumns, allColumns);
    };

    this.populateSelectedColumns = function (configuredColumns, allColumns) {
        var selectedColumns = this.selectedColumns;
        var findColumnIndex = this.findColumnIndex;
        AJS.$(configuredColumns).each(function () {
            var configuredColumn = this;
            var i = findColumnIndex(configuredColumn, allColumns);
            if (i != -1) {
                // If the user had the configuration set to --Default--|issuetype,
                // they would end up with two issuetypes in the selected list, and things break.
                // De-dupe them here and only keep the first, as only the first was shown in the table historically.
                if (findColumnIndex(configuredColumn, selectedColumns) == -1) {
                    selectedColumns.push(allColumns[i]);
                }
            }
        });
    }

    this.getSelectedColumns = function () {
        return this.selectedColumns;
    }

    this.getUnselectedColumns = function () {
        // This array needs to stay in the original order given by the server.
        // If we persist this array, we would lose the order of its items every time
        // an element moves to the other array and back.
        var unselectedColumns = [];
        var findColumnIndex = this.findColumnIndex;
        var selectedColumns = this.selectedColumns;
        AJS.$(allColumns).each(function () {
            if (findColumnIndex(this.value, selectedColumns) == -1) {
                unselectedColumns.push(this);
            }
        });
        return unselectedColumns;
    }

    this.findColumnIndex = function (value, array) {
        for (var i = 0; i < array.length; i++) {
            if (value == array[i].value) {
                return i
            }
        }
        return -1;
    }

    this.selectColumn = function (columnId) {
        // Check it's not already selected (it shouldn't be)
        var selectedIndex = this.findColumnIndex (columnId, this.selectedColumns);
        if (selectedIndex == -1) {
            var allIndex = this.findColumnIndex (columnId, this.allColumns);
            if (allIndex != -1) {
                var column = allColumns[allIndex];
                this.selectedColumns.push(column);
                AJS.$(document).trigger("column-data-item-selected",
                        { dataModel : this, column : allColumns[allIndex]});
            }
        }

    }

    this.unselectColumn = function (columnId) {
        var selectedIndex = this.findColumnIndex (columnId, this.selectedColumns);
        if (selectedIndex != -1) {
            this.selectedColumns.splice(selectedIndex, 1);
            var unselectedColumns = this.getUnselectedColumns();
            var newIndex = this.findColumnIndex (columnId, unselectedColumns);
            AJS.$(document).trigger("column-data-item-unselected",
                    { dataModel : this, column : unselectedColumns[newIndex], index : newIndex});
        }
    }

    this.reorderSelectedColumns = function (newOrder) {
        this.selectedColumns = newOrder;
        AJS.$(document).trigger("column-data-reordered", { dataModel : this });
    }

    this.populateColumns (allColumns, currentConfig);
}
