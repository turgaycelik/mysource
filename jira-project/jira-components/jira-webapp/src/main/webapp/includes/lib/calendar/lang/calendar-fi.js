// ** I18N

// Calendar FI language (Finnish, Suomi)
// Author: Jarno K\u00e4yhk\u00f6, <gambler@phnet.fi>
// Encoding: UTF-8
// Distributed under the same terms as the calendar itself.

Calendar._SMN_len = 6;
// full day names
Calendar._DN = new Array
("Sunnuntai",
 "Maanantai",
 "Tiistai",
 "Keskiviikko",
 "Torstai",
 "Perjantai",
 "Lauantai",
 "Sunnuntai");

// short day names
Calendar._SDN = new Array
("Su",
 "Ma",
 "Ti",
 "Ke",
 "To",
 "Pe",
 "La",
 "Su");

// full month names
Calendar._MN = new Array
("Tammikuu",
 "Helmikuu",
 "Maaliskuu",
 "Huhtikuu",
 "Toukokuu",
 "Kes\u00e4kuu",
 "Hein\u00e4kuu",
 "Elokuu",
 "Syyskuu",
 "Lokakuu",
 "Marraskuu",
 "Joulukuu");

// short month names
Calendar._SMN = new Array
("Tammi",
 "Helmi",
 "Maalis",
 "Huhti",
 "Touko",
 "Kes\u00e4",
 "Hein\u00e4",
 "Elo",
 "Syys",
 "Loka",
 "Marras",
 "Joulu");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Tietoja kalenterista";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" + // don't translate this this ;-)
"Uusin versio osoitteessa: http://www.dynarch.com/projects/calendar/\n" +
"Julkaistu GNU LGPL lisenssin alaisuudessa. Lis\u00e4tietoja osoitteessa http://gnu.org/licenses/lgpl.html" +
"\n\n" +
"P\u00e4iv\u00e4m\u00e4\u00e4r\u00e4 valinta:\n" +
"- K\u00e4yt\u00e4 \xab, \xbb painikkeita valitaksesi vuosi\n" +
"- K\u00e4yt\u00e4 " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " painikkeita valitaksesi kuukausi\n" +
"- Pit\u00e4m\u00e4ll\u00e4 hiiren painiketta mink\u00e4 tahansa yll\u00e4 olevan painikkeen kohdalla, saat n\u00e4kyviin valikon nopeampaan siirtymiseen.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Ajan valinta:\n" +
"- Klikkaa kellonajan numeroita lis\u00e4t\u00e4ksesi aikaa\n" +
"- tai pit\u00e4m\u00e4ll\u00e4 Shift-n\u00e4pp\u00e4int\u00e4 pohjassa saat aikaa taaksep\u00e4in\n" +
"- tai klikkaa ja pid\u00e4 hiiren painike pohjassa sek\u00e4 liikuta hiirt\u00e4 muuttaaksesi aikaa nopeasti eteen- ja taaksep\u00e4in.";

Calendar._TT["PREV_YEAR"] = "Edell. vuosi (paina hetki, n\u00e4et valikon)";
Calendar._TT["PREV_MONTH"] = "Edell. kuukausi (paina hetki, n\u00e4et valikon)";
Calendar._TT["GO_TODAY"] = "Siirry t\u00e4h\u00e4n p\u00e4iv\u00e4\u00e4n";
Calendar._TT["NEXT_MONTH"] = "Seur. kuukausi (paina hetki, n\u00e4et valikon)";
Calendar._TT["NEXT_YEAR"] = "Seur. vuosi (paina hetki, n\u00e4et valikon)";
Calendar._TT["SEL_DATE"] = "Valitse p\u00e4iv\u00e4m\u00e4\u00e4r\u00e4";
Calendar._TT["DRAG_TO_MOVE"] = "Siirr\u00e4 kalenterin paikkaa";
Calendar._TT["PART_TODAY"] = " (t\u00e4n\u00e4\u00e4n)";
Calendar._TT["MON_FIRST"] = "N\u00e4yt\u00e4 maanantai ensimm\u00e4isen\u00e4";
Calendar._TT["SUN_FIRST"] = "N\u00e4yt\u00e4 sunnuntai ensimm\u00e4isen\u00e4";
Calendar._TT["CLOSE"] = "Sulje";
Calendar._TT["TODAY"] = "T\u00e4n\u00e4\u00e4n";
Calendar._TT["TIME_PART"] = "(Shift-) Klikkaa tai liikuta muuttaaksesi aikaa";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Display %s first";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%d.%m.%Y";
Calendar._TT["TT_DATE_FORMAT"] = "%d.%m.%Y";

Calendar._TT["WK"] = "Vko";

// local AM/PM designators
Calendar._TT["AM"] = "AM";
Calendar._TT["PM"] = "PM";
Calendar._TT["am"] = "am";
Calendar._TT["pm"] = "pm";
