// ** I18N

// Calendar EN language
// Author: Mihai Bazon, <mihai_bazon@yahoo.com>
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// Update for JIRA 3.2.2 by Andrzej Michalec (andrzej_michalec@o2.pl)
// To generate 'calendar-pl.js' file use following command 
// native2ascii -encoding UTF-8 calendar-pl-utf8.js calendar-pl.js

// full day names
Calendar._DN = new Array
("Niedziela",
 "Poniedzia\u0142ek",
 "Wtorek",
 "\u015aroda",
 "Czwartek",
 "Pi\u0105tek",
 "Sobota",
 "Niedziela");

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
("Nie",
 "Pn",
 "Wt",
 "\u015ar",
 "Cz",
 "Pt",
 "So",
 "Nie");

// First day of the week. "0" means display Sunday first, "1" means display
// Monday first, etc.
Calendar._FD = 0;

// full month names
Calendar._MN = new Array
("Stycze\u0144",
 "Luty",
 "Marzec",
 "Kwiecie\u0144",
 "Maj",
 "Czerwiec",
 "Lipiec",
 "Sierpie\u0144",
 "Wrzesie\u0144",
 "Pa\u017adziernik",
 "Listopad",
 "Grudzie\u0144");

// short month names
Calendar._SMN = new Array
("sty",
 "lut",
 "mar",
 "kwi",
 "maj",
 "cze",
 "lip",
 "sie",
 "wrz",
 "pa\u017a",
 "lis",
 "gru");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "O kalendarzu";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" + // don't translate this this ;-)
"Aby pobra\u0107 najnowsz\u0105 wersj\u0119, odwied\u017a: http://www.dynarch.com/projects/calendar/\n" +
"Dost\u0119pny na licencji GNU LGPL. Zobacz szczeg\u00f3\u0142y na http://gnu.org/licenses/lgpl.html." +
"\n\n" +
"Wyb\u00f3r daty:\n" +
"- U\u017cyj przycisk\u00f3w \xab, \xbb by wybra\u0107 rok\n" +
"- U\u017cyj przycisk\u00f3w " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " by wybra\u0107 miesi\u0105c\n" +
"- Przytrzymaj klawisz myszy nad jednym z powy\u017cszych przycisk\u00f3w dla szybszego wyboru.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Wyb\u00f3r czasu:\n" +
"- Kliknij na jednym z p\u00f3l czasu by zwi\u0119kszy\u0107 jego warto\u015b\u0107\n" +
"- lub kliknij trzymaj\u0105c Shift by zmiejszy\u0107 jego warto\u015b\u0107\n" +
"- lub kliknij i przeci\u0105gnij dla szybszego wyboru.";

Calendar._TT["PREV_YEAR"] = "Poprzedni rok (przytrzymaj dla menu)";
Calendar._TT["PREV_MONTH"] = "Poprzedni miesi\u0105c (przytrzymaj dla menu)";
Calendar._TT["GO_TODAY"] = "Id\u017a do dzisiejszej daty";
Calendar._TT["NEXT_MONTH"] = "Nast\u0119pny miesi\u0105c (przytrzymaj dla menu)";
Calendar._TT["NEXT_YEAR"] = "Nast\u0119pny rok (przytrzymaj dla menu)";
Calendar._TT["SEL_DATE"] = "Wybierz dat\u0119";
Calendar._TT["DRAG_TO_MOVE"] = "Przeci\u0105gnij by przesun\u0105\u0107";
Calendar._TT["PART_TODAY"] = " (dzisiaj)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "%s jako pierwszy dzie\u0144 tygodnia";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "Zamknij";
Calendar._TT["TODAY"] = "Dzisiaj";
Calendar._TT["TIME_PART"] = "(Shift-)Klik lub przeci\u0105gnij by zmieni\u0107 warto\u015b\u0107";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%A, %e %B";

Calendar._TT["WK"] = "tydz.";
Calendar._TT["TIME"] = "Czas:";
