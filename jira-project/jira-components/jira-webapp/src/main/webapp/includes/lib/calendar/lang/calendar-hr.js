/* Croatian language file for the DHTML Calendar version 0.9.2
* Author Krunoslav Zubrinic <krunoslav.zubrinic@vip.hr>, June 2003.
* Feel free to use this script under the terms of the GNU Lesser General
* Public License, as long as you do not remove or alter this notice.
*/
Calendar._DN = new Array
("Nedjelja",
 "Ponedjeljak",
 "Utorak",
 "Srijeda",
 "\u010cetvrtak",
 "Petak",
 "Subota",
 "Nedjelja");
Calendar._MN = new Array
("Sije\u010danj",
 "Velja\u010da",
 "O\u017eujak",
 "Travanj",
 "Svibanj",
 "Lipanj",
 "Srpanj",
 "Kolovoz",
 "Rujan",
 "Listopad",
 "Studeni",
 "Prosinac");

// short month names
Calendar._SMN = new Array
("sij",
"vel",
"o\u017eu",
"tra",
"svi",
"lip",
"srp",
"kol",
"ruj",
"lis",
"stu",
"pro");

// tooltips
Calendar._TT = {};
Calendar._TT["TOGGLE"] = "Promjeni dan s kojim po\u010dinje tjedan";
Calendar._TT["PREV_YEAR"] = "Prethodna godina (dugi pritisak za meni)";
Calendar._TT["PREV_MONTH"] = "Prethodni mjesec (dugi pritisak za meni)";
Calendar._TT["GO_TODAY"] = "Idi na teku\u0107i dan";
Calendar._TT["NEXT_MONTH"] = "Slijede\u0107i mjesec (dugi pritisak za meni)";
Calendar._TT["NEXT_YEAR"] = "Slijede\u0107a godina (dugi pritisak za meni)";
Calendar._TT["SEL_DATE"] = "Izaberite datum";
Calendar._TT["DRAG_TO_MOVE"] = "Pritisni i povuci za promjenu pozicije";
Calendar._TT["PART_TODAY"] = " (today)";
Calendar._TT["MON_FIRST"] = "Prika\u017ei ponedjeljak kao prvi dan";
Calendar._TT["SUN_FIRST"] = "Prika\u017ei nedjelju kao prvi dan";
Calendar._TT["CLOSE"] = "Zatvori";
Calendar._TT["TODAY"] = "Danas";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Display %s first";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "dd-mm-y";
Calendar._TT["TT_DATE_FORMAT"] = "DD, dd.mm.y";

Calendar._TT["WK"] = "Tje";

// local AM/PM designators
Calendar._TT["AM"] = "AM";
Calendar._TT["PM"] = "PM";
Calendar._TT["am"] = "am";
Calendar._TT["pm"] = "pm";
