/*
Renders an element visible to the user
*/
function hideCronEdit(elementid)
{
    document.getElementById(elementid).style.display = 'none';
}

/*
Renders an element invisible to the user
*/
function showCronEdit(elementid)
{
    document.getElementById(elementid).style.display = '';
}

function toggleFrequencyControl(paramPrefix, setOriginal)
{
    var select = document.getElementById(paramPrefix + "interval");
    if(select.value == 0)
    {
        switchToOnce(paramPrefix, setOriginal);
    }
    else
    {
        switchToMany(paramPrefix, setOriginal);
    }
}

/*
Toggles the frequency controls to match 'once per day' mode
*/
function switchToOnce(paramPrefix, setOriginal)
{
    //make sure the frequency select is set correctly
    //set state
    hideCronEdit(paramPrefix + "runMany");
    showCronEdit(paramPrefix + "runOnce");
    if (setOriginal)
    {
        timesOnce[paramPrefix] = true;
    }
}

/*
Toggles the frequency controls to match 'many per day' mode
*/
function switchToMany(paramPrefix, setOriginal)
{
    //set state
    hideCronEdit(paramPrefix + "runOnce");
    showCronEdit(paramPrefix + "runMany");
    if (setOriginal)
    {
        timesOnce[paramPrefix] = false;
    }
}

function switchToDaysOfMonth(paramPrefix)
{
    hideCronEdit(paramPrefix + 'daysOfWeek');
    showCronEdit(paramPrefix + 'daysOfMonth');
    showCronEdit(paramPrefix + 'freqDiv');
    hideCronEdit(paramPrefix + 'innerFreqDiv');
    hideCronEdit(paramPrefix + 'advanced');
    switchToOnce(paramPrefix, false);
}

function switchToDaysOfWeek(paramPrefix)
{
    showCronEdit(paramPrefix + 'daysOfWeek');
    hideCronEdit(paramPrefix + 'daysOfMonth');
    showCronEdit(paramPrefix + 'freqDiv');
    showCronEdit(paramPrefix + 'innerFreqDiv');
    hideCronEdit(paramPrefix + 'advanced');
    switchToOriginal(paramPrefix);
}

function switchToDaily(paramPrefix)
{
    hideCronEdit(paramPrefix + 'daysOfWeek');
    hideCronEdit(paramPrefix + 'daysOfMonth');
    showCronEdit(paramPrefix + 'freqDiv');
    showCronEdit(paramPrefix + 'innerFreqDiv');
    hideCronEdit(paramPrefix + 'advanced');
    switchToOriginal(paramPrefix);
}

function switchToAdvanced(paramPrefix)
{
    hideCronEdit(paramPrefix + 'daysOfWeek');
    hideCronEdit(paramPrefix + 'daysOfMonth');
    hideCronEdit(paramPrefix + "runOnce");
    hideCronEdit(paramPrefix + "runMany");
    hideCronEdit(paramPrefix + 'freqDiv');
    showCronEdit(paramPrefix + 'advanced');

}

function switchToOriginal(paramPrefix)
{
    if (timesOnce[paramPrefix])
    {
        switchToOnce(paramPrefix, false);
    }
    else
    {
        switchToMany(paramPrefix, false);
    }
}
