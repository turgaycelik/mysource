<%--// Provide hover and click effect to entire table rows.
// Usage:
// <table class="grid">
//   <tr href="somelink.jsp" onmouseover="rowHover(this)">
//   ...--%>

<script language="JavaScript" type="text/javascript">

function rowHover(row)
{
    row.oldClassName = row.className;
    row.className = 'rowHover';
    row.onmouseout = function()
    {
        this.className = this.oldClassName;
    }
}

function setCheckboxes()
{
    var value = document.selectorform.all.checked;
    var numelements = document.selectorform.elements.length;
    var item;
    for (var i=0 ; i < numelements ; i++)
    {
        item = document.selectorform.elements[i];
        item.checked = value;
    }
}

function selectUsers(openerElId, inputSelector)
{
    addUsersToText(openerElId, inputSelector);

    window.close();
}

function addUsersToText(openerElId, inputSelector)
{
    var openerEl = opener.document.getElementById(openerElId);
    var numelements = document.selectorform.elements.length;
    var item;

    var selectedUsersString = createPreviouslySelectedString(AJS.$('input[name="previouslySelected"]').val(), inputSelector);
    selectedUsersString = selectedUsersString.substring(1, selectedUsersString.length - 1);
    var selectedUsers = selectedUsersString.split(";");

    for (var x in selectedUsers) {
        addSingleUserToText(decodeUserName(selectedUsers[x]), openerEl);
    }

    var userList = [];
    AJS.$.each(openerEl.value.split(","), function() {
        var username = trim(this);
        if (username && username !== "") {
            userList.push(username);
        }
    });
    userList.sort();
    openerEl.value = userList.join(", ");

}

function addSingleUserToText(user, openerEl)
{
    var userList = openerEl.value.split(",");
    for (var i = 0 ; i < userList.length ; i++) {
        if (user == trim(userList[i])) {
            return;
        }
    }
    if (openerEl.value =="") {
        openerEl.value = user;
    } else {
        openerEl.value = openerEl.value + ", " + user;
    }
}

function trim(str)
{
   return str.replace(/^\s*|\s*$/g,"");
}
// Multi-Select - Shift Click
var recordedCheckBoxIndex;

function processCBClick(myEvent, myCheckBox)
{
    if (myCheckBox.checked)
    {
        if (myEvent.shiftKey)
        {
            selectMultiple(myCheckBox);
        }
        else
        {
            recordCheckBox(myCheckBox);
            // setRowClass(findCheckBox(myCheckBox), true);
        }
    }
    else
    {
        recordedCheckBoxIndex = null;
        // setRowClass(findCheckBox(myCheckBox), false);
    }
}

function recordCheckBox(checkBoxToRecord)
{
    recordedCheckBoxIndex = findCheckBox(checkBoxToRecord);
}

function selectMultiple(myCheckBox)
{
    if (recordedCheckBoxIndex == null)
        return;

    var currentCheckBox = findCheckBox(myCheckBox);

    var lastSelected = selectNeededCheckBoxes(myCheckBox, Math.min(currentCheckBox,recordedCheckBoxIndex),
                                      Math.max(currentCheckBox,recordedCheckBoxIndex),
                                      true);

    recordedCheckBoxIndex = lastSelected;
}


function findCheckBox(myCheckBox)
{
    for(var i = 0; i < myCheckBox.form.elements[myCheckBox.name].length; i++)
    {
        if (myCheckBox.form.elements[myCheckBox.name][i].value == myCheckBox.value)
        {
            return i;
        }
    }
}

function selectNeededCheckBoxes(myCheckBox, from, to, value)
{
    var j;
    for (j = from; j <= to; j++)
    {
        myCheckBox.form.elements[myCheckBox.name][j].checked = value;
        //setRowClass(j, value);
    }

    return j;
}

function setRowClass(index, value)
{
    if (document.all)
    {
        if (value)
        {
            document.all['row' + index].className='rowSelected';
        }
        else
        {
            document.all['row' + index].className='rowUnselected';
        }
    }
    else if (!document.all && document.getElementById)
    {
        if (value)
        {
            document.getElementById('row' + index).className='rowSelected';
        }
        else
        {
            document.getElementById('row' + index).className='rowUnselected';
        }
    }
}

function toggleCheckBox(event, checkboxId)
{
    checkbox = document.getElementById(checkboxId);
    if (checkbox == null)
        checkbox = document.all[checkboxId];

    checkbox.checked = !checkbox.checked;
    processCBClick(event, checkbox);
    return false;
}

function moveToPage(start, inputSelector)
{
    var form = AJS.$('form.aui');
    var startField = form.find('input[name="start"]');
    startField.val(start);

    // check we are in multi-select
    if (AJS.$('.selectorform').length > 0) {
        var prevSelected = form.find('input[name="previouslySelected"]');
        prevSelected.val(createPreviouslySelectedString(prevSelected.val(), inputSelector));
    }

    form.submit();
}

function createPreviouslySelectedString(currentSelected, inputSelector)
{
    var selected = currentSelected,
        selector = "input:checkbox[name=userchecks]";
    if (inputSelector && inputSelector !== "") {
        selector = inputSelector;
    }
    var checkboxes = AJS.$(document.selectorform).find(selector);
    checkboxes.each(function() {
        var item = this;
        if (item != null) {
            var userName = encodeUserName(item.value);
            selected = removeUserFromSelected(selected, userName);
            if (item.checked) {
                if (selected.length == 0) {
                    selected += ';';
                }
                selected += userName;
                selected += ';';
            }
        }
    });
    return selected;
}

function removeUserFromSelected(selectedString, encodedUserName)
{
    return selectedString.replace(";" + encodedUserName + ";", ";");
}

function encodeUserName(userName)
{
    return userName.replace(/;/, "%59");
}

function decodeUserName(userName)
{
    return userName.replace(/%59/, ";");
}

</script>
