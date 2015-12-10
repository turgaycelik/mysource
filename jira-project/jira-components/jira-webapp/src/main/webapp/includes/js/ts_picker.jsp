<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties,
                 com.atlassian.jira.config.properties.APKeys,
                 com.atlassian.jira.component.ComponentAccessor"%>
<%@ page import="com.atlassian.jira.config.properties.LookAndFeelBean"%>
<%
    //this file needs to be a jsp so that we can process the look and feel of it.
    ApplicationProperties ap = ComponentAccessor.getApplicationProperties();
    final LookAndFeelBean lookAndFeelBeanTSPicker = LookAndFeelBean.getInstance(ap);
    String topBgColour = lookAndFeelBeanTSPicker.getTopBackgroundColour();
    String topTxtColour = lookAndFeelBeanTSPicker.getTopTxtColour();
%>

// Title: Timestamp picker
// Description: See the demo at url
// URL: http://www.softcomplex.com/products/tigra_calendar/
// Version: 1.0
// Date: 12-05-2001 (mm-dd-yyyy)
// Author: Denis Gritcyuk <denis@softcomplex.com>
// Notes: Permission given to use this script in any kind of applications if
//    header lines are left unchanged. Feel free to contact the author
//    for feature requests and/or donations

function show_calendar(context_path, formName, formObj, element)
{
    show_calendar_func(context_path, 'document.' + formName + '.elements[\'' + element + '\']', formObj.elements[element].value, formObj.elements[element].value);
}

function show_calendar_func(context_path, str_target, str_datetime, str_month)
{
    escapeTarget(str_target);
	var arr_months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
	var week_days = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];
	var n_weekstart = 1; // day week starts from (normally 0 or 1)

	var dt_datetime = (str_datetime == null || str_datetime =="" ?  new Date() : str2dt2(str_datetime));
	var dt_month = (str_month == null || str_month =="" ?  new Date() : str2dt2(str_month));
	var dt_prev_month = new Date(dt_month);
	dt_prev_month.setMonth(dt_month.getMonth()-1);
	if (dt_month.getMonth()%12 != (dt_prev_month.getMonth()+1)%12)
    {
		dt_prev_month.setMonth(dt_month.getMonth());
		dt_prev_month.setDate(0);
	}
	var dt_next_month = new Date(dt_month);
	dt_next_month.setMonth(dt_month.getMonth()+1);
	if ((dt_month.getMonth() + 1)%12 != dt_next_month.getMonth()%12)
		dt_next_month.setDate(0);

	var dt_firstday = new Date(dt_month);
	dt_firstday.setDate(1);
	dt_firstday.setDate(1-(7+dt_firstday.getDay()-n_weekstart)%7);
	var dt_lastday = new Date(dt_next_month);
	dt_lastday.setDate(0);

	// html generation (feel free to tune it for your particular application)
	// print calendar header
	var str_buffer = new String
    (
		"<html>\n"+
		"<head>\n"+
		"	<title>Calendar</title>\n"+
		"</head>\n"+
		"<body bgcolor=\"f0f0f0\" leftmargin=\"0\" topmargin=\"0\" marginwidth=\"0\" marginheight=\"0\">\n"+
		"<table class=\"clsOTable\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">\n"+
		"<tr>\n	<td bgcolor=\"<%= topBgColour %>\" width=1% nowrap>&nbsp;<a href=\"javascript:window.opener.show_calendar_func('" + context_path + "', '"+
		escapeTarget(str_target)+"', '"+ dt2dtstr2(dt_datetime)+"', '"+ dt2dtstr2(dt_prev_month)+"');\">"+
		"<img src=\""+context_path+"/images/icons/prev.gif\" width=\"16\" height=\"16\" border=\"0\""+
		" alt=\"previous month\"></a></td>\n"+
		"	<td bgcolor=\"<%= topBgColour %>\" align=center width=100%>"+
		"<font color=\"<%= topTxtColour %>\" face=\"Verdana, Sans-Serif\" size=\"2\"><b>"
		+arr_months[dt_month.getMonth()]+" "+dt_month.getFullYear()+"</b></font></td>\n"+
		"	<td bgcolor=\"<%= topBgColour %>\"  width=1% align=\"right\" nowrap><a href=\"javascript:window.opener.show_calendar_func('" + context_path + "', '"
		+escapeTarget(str_target)+"', '"+ dt2dtstr2(dt_datetime)+"', '"+ dt2dtstr2(dt_next_month)+"');\">"+
		"<img src=\""+context_path+"/images/icons/next.gif\" width=\"16\" height=\"16\" border=\"0\""+
		" alt=\"next month\"></a>&nbsp;</td>\n</tr>\n" +
		"<tr><td colspan=3 bgcolor=\"#bbbbbb\">\n"+
		"<table cellspacing=\"1\" cellpadding=\"3\" border=\"0\" width=\"100%\">\n"
    );

	var dt_current_day = new Date(dt_firstday);
	// print weekdays titles
	str_buffer += "<tr>\n";
	for (var n=0; n<7; n++)
		str_buffer += "	<td bgcolor=\"#f0f0f0\" align=center>"+
		"<font face=\"Verdana, Sans-Serif\" size=\"2\">"+
		week_days[(n_weekstart+n)%7]+"</font></td>\n";
	// print calendar table
	str_buffer += "</tr>\n";
	while (dt_current_day.getMonth() == dt_month.getMonth() ||
		dt_current_day.getMonth() == dt_firstday.getMonth()) {
		// print row header
		str_buffer += "<tr>\n";
		for (var n_current_wday=0; n_current_wday<7; n_current_wday++) {
				if (dt_current_day.getDate() == dt_datetime.getDate() &&
					dt_current_day.getMonth() == dt_datetime.getMonth())
					// print current date
					str_buffer += "	<td bgcolor=\"#DBEAF5\" align=\"right\">";
				else if (dt_current_day.getDay() == 0 || dt_current_day.getDay() == 6)
					// weekend days
					str_buffer += "	<td bgcolor=\"#fffff0\" align=\"right\">";
				else
					// print working days of current month
					str_buffer += "	<td bgcolor=\"white\" align=\"right\">";

				if (dt_current_day.getMonth() == dt_month.getMonth())
					// print days of current month
					str_buffer += "<a href=\"javascript:window.opener."+str_target+
					".value='"+dt2dtstr2(dt_current_day)+"'; window.close();\">"+
					"<font color=\"black\" face=\"Verdana, Sans-Serif\" size=\"2\">";
				else
					// print days of other months
					str_buffer += "<a href=\"javascript:window.opener."+str_target+
					".value='"+dt2dtstr2(dt_current_day)+"'; window.close();\">"+
					"<font color=\"gray\" face=\"Verdana, Sans-Serif\" size=\"2\">";
				str_buffer += dt_current_day.getDate()+"</font></a></td>\n";
				dt_current_day.setDate(dt_current_day.getDate()+1);
		}
		// print row footer
		str_buffer += "</tr>\n";
	}
	// print calendar footer
	str_buffer +=
        "<tr><td colspan=7 bgcolor=#f0f0f0 align=center><font size=\"1\" face=\"Verdana, Sans-Serif\">\n" +
        "<a href=\"javascript:window.opener."+str_target+
		".value='"+dt2dtstr2(new Date())+"'; window.close();\">Today</a> | \n" +
        "<a href=\"javascript:window.opener."+str_target+".value=''; window.close();\">Clear</a>\n" +
        "</font>\n" +
		"</td></tr></table>\n" +
		"</tr>\n</td>\n</table>\n" +
        "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
        "<tr>\n" +
        "<td height=12 background=\""+context_path+"/images/border/border_bottom.gif\"><img src=\""+context_path+"/images/border/spacer.gif\" width=\"1\" height=\"1\" border=\"0\"></td>\n" +
        "</tr>\n" +
        "</table>\n" +
		"</body>\n" +
		"</html>\n";

	var vWinCal = window.open("", "Calendar",
		"width=220,height=220,status=no,resizable=yes,top=220,left=200");
	vWinCal.opener = self;
	vWinCal.focus();
	var calc_doc = vWinCal.document;
	calc_doc.write (str_buffer);
	calc_doc.close();
}

// datetime parsing and formatting routimes. modify them if you wish other datetime format
function month2int (str_month)
{
    switch (str_month) {
        case "Jan":
            return 0;
        break;
        case "Feb":
            return 1;
        break;
        case "Mar":
            return 2;
        break;
        case "Apr":
            return 3;
        break;
        case "May":
            return 4;
        break;
        case "Jun":
            return 5;
        break;
        case "Jul":
            return 6;
        break;
        case "Aug":
            return 7;
        break;
        case "Sep":
            return 8;
        break;
        case "Oct":
            return 9;
        break;
        case "Nov":
            return 10;
        break;
        case "Dec":
            return 11;
        break;
    }
}

function str2dt2 (str_datetime)
{
	var re_date = /^(\d+)\-(...)\-(\d+)$/;
	if (!re_date.exec(str_datetime))
		return alert("Invalid Datetime format: "+ str_datetime);
	return (new Date (RegExp.$3, month2int(RegExp.$2), RegExp.$1));
}
function dt2dtstr2 (dt_datetime)
{
    var monthsShort = ["Jan", "Feb", "Mar", "Apr",
                       "May", "Jun", "Jul", "Aug", "Sep",
                       "Oct", "Nov", "Dec"];
	return (new String (dt_datetime.getDate()+"-"+monthsShort[(dt_datetime.getMonth())]+"-"+dt_datetime.getFullYear()));
}
function escapeTarget(srcTarget)
{
    var splitUp = srcTarget.split("'");
    var returnString = "";
    for (var i=0;i<splitUp.length;i++)
    {
        returnString += splitUp[i] + "\\'";
    }
    return returnString.substr(0,returnString.length-2);
}
