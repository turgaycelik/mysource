// ** I18N

// Calendar RU language
// Translation: Sly Golovanov, http://golovanov.net, <sly@golovanov.net>
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = new Array
("\u0411\u041d\u042f\u0419\u041f\u0415\u042f\u0415\u041c\u042d\u0415",
 "\u041e\u041d\u041c\u0415\u0414\u0415\u041a\u042d\u041c\u0425\u0419",
 "\u0411\u0420\u041d\u041f\u041c\u0425\u0419",
 "\u042f\u041f\u0415\u0414\u042e",
 "\u0412\u0415\u0420\u0411\u0415\u041f\u0426",
 "\u041e\u042a\u0420\u041c\u0425\u0416\u042e",
 "\u042f\u0421\u0410\u0410\u041d\u0420\u042e",
 "\u0411\u041d\u042f\u0419\u041f\u0415\u042f\u0415\u041c\u042d\u0415");

// Please note that the following array of short day names (and the same goes
// for short month names, _SMN) isn't absolutely necessary.  We give it here
// for exemplification on how one can customize the short day names, but if
// they are simply the first N letters of the full name you can simply say:
//
//   Calendar._SDN_len = N; // short day name length
//   Calendar._SMN_len = N; // short month name length
//
// If N = 3 then this is not needed either since we assume a value of 3 if not
// present, to be compatible with translation files that were written before
// this feature.

// short day names
Calendar._SDN = new Array
("\u0411\u042f\u0419",
 "\u041e\u041d\u041c",
 "\u0411\u0420\u041f",
 "\u042f\u041f\u0414",
 "\u0412\u0415\u0420",
 "\u041e\u042a\u0420",
 "\u042f\u0421\u0410",
 "\u0411\u042f\u0419");

// full month names
Calendar._MN = new Array
("\u042a\u041c\u0411\u042e\u041f\u042d",
 "\u0422\u0415\u0411\u041f\u042e\u041a\u042d",
 "\u041b\u042e\u041f\u0420",
 "\u042e\u041e\u041f\u0415\u041a\u042d",
 "\u041b\u042e\u0418",
 "\u0425\u0427\u041c\u042d",
 "\u0425\u0427\u041a\u042d",
 "\u042e\u0411\u0426\u0421\u042f\u0420",
 "\u042f\u0415\u041c\u0420\u042a\u0410\u041f\u042d",
 "\u041d\u0419\u0420\u042a\u0410\u041f\u042d",
 "\u041c\u041d\u042a\u0410\u041f\u042d",
 "\u0414\u0415\u0419\u042e\u0410\u041f\u042d");

// short month names
Calendar._SMN = new Array
("\u042a\u041c\u0411",
 "\u0422\u0415\u0411",
 "\u041b\u042e\u041f",
 "\u042e\u041e\u041f",
 "\u041b\u042e\u0418",
 "\u0425\u0427\u041c",
 "\u0425\u0427\u041a",
 "\u042e\u0411\u0426",
 "\u042f\u0415\u041c",
 "\u041d\u0419\u0420",
 "\u041c\u041d\u042a",
 "\u0414\u0415\u0419");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "\u043d \u0419\u042e\u041a\u0415\u041c\u0414\u042e\u041f\u0415...";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" + // don't translate this this ;-)
"For latest version visit: http://www.dynarch.com/projects/calendar/\n" +
"Distributed under GNU LGPL.  See http://gnu.org/licenses/lgpl.html for details." +
"\n\n" +
"\u0439\u042e\u0419 \u0411\u0428\u0410\u041f\u042e\u0420\u042d \u0414\u042e\u0420\u0421:\n" +
"- \u043e\u041f\u0425 \u041e\u041d\u041b\u041d\u042b\u0425 \u0419\u041c\u041d\u041e\u041d\u0419 \xab, \xbb \u041b\u041d\u0424\u041c\u041d \u0411\u0428\u0410\u041f\u042e\u0420\u042d \u0426\u041d\u0414\n" +
"- \u043e\u041f\u0425 \u041e\u041d\u041b\u041d\u042b\u0425 \u0419\u041c\u041d\u041e\u041d\u0419 " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " \u041b\u041d\u0424\u041c\u041d \u0411\u0428\u0410\u041f\u042e\u0420\u042d \u041b\u0415\u042f\u042a\u0416\n" +
"- \u043e\u041d\u0414\u0415\u041f\u0424\u0425\u0420\u0415 \u0429\u0420\u0425 \u0419\u041c\u041d\u041e\u0419\u0425 \u041c\u042e\u0424\u042e\u0420\u0428\u041b\u0425, \u0412\u0420\u041d\u0410\u0428 \u041e\u041d\u042a\u0411\u0425\u041a\u041d\u042f\u042d \u041b\u0415\u041c\u0427 \u0410\u0428\u042f\u0420\u041f\u041d\u0426\u041d \u0411\u0428\u0410\u041d\u041f\u042e.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"\u0439\u042e\u0419 \u0411\u0428\u0410\u041f\u042e\u0420\u042d \u0411\u041f\u0415\u041b\u042a:\n" +
"- \u043e\u041f\u0425 \u0419\u041a\u0425\u0419\u0415 \u041c\u042e \u0412\u042e\u042f\u042e\u0423 \u0425\u041a\u0425 \u041b\u0425\u041c\u0421\u0420\u042e\u0423 \u041d\u041c\u0425 \u0421\u0411\u0415\u041a\u0425\u0412\u0425\u0411\u042e\u0427\u0420\u042f\u042a\n" +
"- \u041e\u041f\u0425 \u0419\u041a\u0425\u0419\u0415 \u042f \u041c\u042e\u0424\u042e\u0420\u041d\u0418 \u0419\u041a\u042e\u0411\u0425\u042c\u0415\u0418 Shift \u041d\u041c\u0425 \u0421\u041b\u0415\u041c\u042d\u042c\u042e\u0427\u0420\u042f\u042a\n" +
"- \u0415\u042f\u041a\u0425 \u041c\u042e\u0424\u042e\u0420\u042d \u0425 \u0414\u0411\u0425\u0426\u042e\u0420\u042d \u041b\u0428\u042c\u0419\u041d\u0418 \u0411\u041a\u0415\u0411\u041d/\u0411\u041e\u041f\u042e\u0411\u041d, \u041d\u041c\u0425 \u0410\u0421\u0414\u0421\u0420 \u041b\u0415\u041c\u042a\u0420\u042d\u042f\u042a \u0410\u0428\u042f\u0420\u041f\u0415\u0415.";

Calendar._TT["PREV_YEAR"] = "\u043c\u042e \u0426\u041d\u0414 \u041c\u042e\u0413\u042e\u0414 (\u0421\u0414\u0415\u041f\u0424\u0425\u0411\u042e\u0420\u042d \u0414\u041a\u042a \u041b\u0415\u041c\u0427)";
Calendar._TT["PREV_MONTH"] = "\u043c\u042e \u041b\u0415\u042f\u042a\u0416 \u041c\u042e\u0413\u042e\u0414 (\u0421\u0414\u0415\u041f\u0424\u0425\u0411\u042e\u0420\u042d \u0414\u041a\u042a \u041b\u0415\u041c\u0427)";
Calendar._TT["GO_TODAY"] = "\u044f\u0415\u0426\u041d\u0414\u041c\u042a";
Calendar._TT["NEXT_MONTH"] = "\u043c\u042e \u041b\u0415\u042f\u042a\u0416 \u0411\u041e\u0415\u041f\u0415\u0414 (\u0421\u0414\u0415\u041f\u0424\u0425\u0411\u042e\u0420\u042d \u0414\u041a\u042a \u041b\u0415\u041c\u0427)";
Calendar._TT["NEXT_YEAR"] = "\u043c\u042e \u0426\u041d\u0414 \u0411\u041e\u0415\u041f\u0415\u0414 (\u0421\u0414\u0415\u041f\u0424\u0425\u0411\u042e\u0420\u042d \u0414\u041a\u042a \u041b\u0415\u041c\u0427)";
Calendar._TT["SEL_DATE"] = "\u0431\u0428\u0410\u0415\u041f\u0425\u0420\u0415 \u0414\u042e\u0420\u0421";
Calendar._TT["DRAG_TO_MOVE"] = "\u043e\u0415\u041f\u0415\u0420\u042e\u042f\u0419\u0425\u0411\u042e\u0418\u0420\u0415 \u041b\u0428\u042c\u0419\u041d\u0418";
Calendar._TT["PART_TODAY"] = " (\u042f\u0415\u0426\u041d\u0414\u041c\u042a)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "\u043e\u0415\u041f\u0411\u0428\u0418 \u0414\u0415\u041c\u042d \u041c\u0415\u0414\u0415\u041a\u0425 \u0410\u0421\u0414\u0415\u0420 %s";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "\u0433\u042e\u0419\u041f\u0428\u0420\u042d";
Calendar._TT["TODAY"] = "\u044f\u0415\u0426\u041d\u0414\u041c\u042a";
Calendar._TT["TIME_PART"] = "(Shift-)\u0419\u041a\u0425\u0419 \u0425\u041a\u0425 \u041c\u042e\u0424\u042e\u0420\u042d \u0425 \u0414\u0411\u0425\u0426\u042e\u0420\u042d";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%e %b, %a";

Calendar._TT["WK"] = "\u041c\u0415\u0414";
Calendar._TT["TIME"] = "\u0431\u041f\u0415\u041b\u042a:";

// local AM/PM designators
Calendar._TT["AM"] = "AM";
Calendar._TT["PM"] = "PM";
Calendar._TT["am"] = "am";
Calendar._TT["pm"] = "pm";
