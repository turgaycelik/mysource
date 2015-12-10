// ** I18N

// Calendar SK language
// Author: Peter Valach (pvalach@gmx.net)
// Encoding: utf-8
// Last update: 2003/10/29
// Distributed under the same terms as the calendar itself.

// full day names
Calendar._DN = new Array
("Nede\u013ea",
 "Pondelok",
 "Utorok",
 "Streda",
 "\u0160tvrtok",
 "Piatok",
 "Sobota",
 "Nede\u013ea");

// short day names
Calendar._SDN = new Array
("Ned",
 "Pon",
 "Uto",
 "Str",
 "\u0160tv",
 "Pia",
 "Sob",
 "Ned");

// full month names
Calendar._MN = new Array
("janu\u00e1r",
 "febru\u00e1r",
 "marec",
 "apr\u00edl",
 "m\u00e1j",
 "j\u00fan",
 "j\u00fal",
 "august",
 "september",
 "okt\u00f3ber",
 "november",
 "december");

// short month names
Calendar._SMN = new Array
("jan",
 "feb",
 "mar",
 "apr",
 "m\u00e1j",
 "j\u00fan",
 "j\u00fal",
 "aug",
 "sep",
 "okt",
 "nov",
 "dec");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "O kalend\u00e1ri";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" +
"Posledn\u00fa verziu n\u00e1jdete na: http://www.dynarch.com/projects/calendar/\n" +
"Distribuovan\u00e9 pod GNU LGPL.  Vid http://gnu.org/licenses/lgpl.html pre detaily." +
"\n\n" +
"V\u00fdber d\u00e1tumu:\n" +
"- Pou\u017eite tlacidl\u00e1 \xab, \xbb pre v\u00fdber roku\n" +
"- Pou\u017eite tlacidl\u00e1 " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " pre v\u00fdber mesiaca\n" +
"- Ak ktor\u00e9kolvek z t\u00fdchto tlacidiel podr\u017e\u00edte dlh\u0161ie, zobraz\u00ed sa r\u00fdchly v\u00fdber.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"V\u00fdber casu:\n" +
"- Kliknutie na niektorAs polo\u017eku casu ju zv\u00fd\u0161i\n" +
"- Shift-klik ju zn\u00ed\u017ei\n" +
"- Ak podr\u017e\u00edte tlac\u00edtko stlacen\u00e9, posAsvan\u00edm men\u00edte hodnotu.";

Calendar._TT["PREV_YEAR"] = "Predo\u0161l\u00fd rok (podr\u017ete pre menu)";
Calendar._TT["PREV_MONTH"] = "Predo\u0161l\u00fd mesiac (podr\u017ete pre menu)";
Calendar._TT["GO_TODAY"] = "Prejst na dne\u0161ok";
Calendar._TT["NEXT_MONTH"] = "Nasl. mesiac (podr\u017ete pre menu)";
Calendar._TT["NEXT_YEAR"] = "Nasl. rok (podr\u017ete pre menu)";
Calendar._TT["SEL_DATE"] = "Zvolte d\u00e1tum";
Calendar._TT["DRAG_TO_MOVE"] = "Podr\u017ean\u00edm tlac\u00edtka zmen\u00edte polohu";
Calendar._TT["PART_TODAY"] = " (dnes)";
Calendar._TT["MON_FIRST"] = "Zobrazit pondelok ako prv\u00fd";
Calendar._TT["SUN_FIRST"] = "Zobrazit nedelu ako prv\u00fa";
Calendar._TT["CLOSE"] = "Zavriet";
Calendar._TT["TODAY"] = "Dnes";
Calendar._TT["TIME_PART"] = "(Shift-)klik/tahanie zmen\u00ed hodnotu";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Zobraz %s prv\u00fd";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "$d. %m. %Y";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %e. %b";

Calendar._TT["WK"] = "t\u00fd\u017e";
Calendar._TT["TIME"] = "\u010cas:";
