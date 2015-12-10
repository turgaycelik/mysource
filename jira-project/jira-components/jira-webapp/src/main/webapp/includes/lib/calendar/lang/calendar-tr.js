//////////////////////////////////////////////////////////////////////////////////////////////
//	Turkish Translation by Nuri AKMAN
//	Location: Ankara/TURKEY
//	e-mail	: nuriakman@hotmail.com
//	Date	: April, 9 2003
//
//	Note: if Turkish Characters does not shown on you screen
//		  please include falowing line your html code:
//
//		  <meta http-equiv="Content-Type" content="text/html; charset=windows-1254">
//
//////////////////////////////////////////////////////////////////////////////////////////////

// ** I18N
Calendar._DN = new Array
("Pazar",
 "Pazartesi",
 "Sal\u0131",
 "\u00c7ar\u015famba",
 "Per\u015fembe",
 "Cuma",
 "Cumartesi",
 "Pazar");

Calendar._SDN = new Array
("Paz",
 "Pzt",
 "Sal",
 "\u00c7ar",
 "Per",
 "Cum",
 "Cts",
 "Paz");

Calendar._MN = new Array
("Ocak",
 "\u015eubat",
 "Mart",
 "Nisan",
 "May\u0131s",
 "Haziran",
 "Temmuz",
 "A\u011fustos",
 "Eyl\u00fcl",
 "Ekim",
 "Kas\u0131m",
 "Aral\u0131k");

Calendar._SMN = new Array
("Oca",
 "\u015Eub",
 "Mar",
 "Nis",
 "May",
 "Haz",
 "Tem",
 "A\u011Fu",
 "Eyl",
 "Eki",
 "Kas",
 "Ara");

// tooltips
Calendar._TT = {};
Calendar._TT["TOGGLE"] = "Haftan\u0131n ilk g\u00fcn\u00fcn\u00fc kayd\u0131r";
Calendar._TT["PREV_YEAR"] = "\u00d6nceki Y\u0131l (Men\u00fc i\u00e7in bas\u0131l\u0131 tutunuz)";
Calendar._TT["PREV_MONTH"] = "\u00d6nceki Ay (Men\u00fc i\u00e7in bas\u0131l\u0131 tutunuz)";
Calendar._TT["GO_TODAY"] = "Bug\u00fcn'e git";
Calendar._TT["NEXT_MONTH"] = "Sonraki Ay (Men\u00fc i\u00e7in bas\u0131l\u0131 tutunuz)";
Calendar._TT["NEXT_YEAR"] = "Sonraki Y\u0131l (Men\u00fc i\u00e7in bas\u0131l\u0131 tutunuz)";
Calendar._TT["SEL_DATE"] = "Tarih se\u00e7iniz";
Calendar._TT["DRAG_TO_MOVE"] = "Ta\u015f\u0131mak i\u00e7in s\u00fcr\u00fckleyiniz";
Calendar._TT["PART_TODAY"] = " (bug\u00fcn)";
Calendar._TT["MON_FIRST"] = "Takvim Pazartesi g\u00fcn\u00fcnden ba\u015flas\u0131n";
Calendar._TT["SUN_FIRST"] = "Takvim Pazar g\u00fcn\u00fcnden ba\u015flas\u0131n";
Calendar._TT["CLOSE"] = "Kapat";
Calendar._TT["TODAY"] = "Bug\u00fcn";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Display %s first";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "dd-mm-y";
Calendar._TT["TT_DATE_FORMAT"] = "d MM y, DD";

Calendar._TT["WK"] = "Hafta";

// local AM/PM designators
Calendar._TT["AM"] = "AM";
Calendar._TT["PM"] = "PM";
Calendar._TT["am"] = "am";
Calendar._TT["pm"] = "pm";
