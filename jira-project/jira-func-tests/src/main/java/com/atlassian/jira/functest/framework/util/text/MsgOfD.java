package com.atlassian.jira.functest.framework.util.text;

import java.util.Random;

public class MsgOfD
{

    @Override
    public String toString()
    {
        return hailCaesar();
    }

    private String hailCaesar()
    {
        return hailCaesar(snargTheMargFunkler());
    }

    private String snargTheMargFunkler()
    {
        return AYSMIDSYDFD[AJSYD_ADSFGD.nextInt(AYSMIDSYDFD.length)];
    }

    private String hailCaesar(final String turkMeinCzar)
    {
        char ch;
        StringBuilder sb = new StringBuilder(SMARGLEFURKEN);
        for (int jsdajuxcsdf = 0; jsdajuxcsdf < turkMeinCzar.length(); jsdajuxcsdf++)
        {
            ch = turkMeinCzar.charAt(jsdajuxcsdf);
            if (ch >= ksadjksdf && ch <= ilubawfilughasf) {ch += jsxdruvad; }
            else if (ch >= JHSGDGYDHD && ch <= UOAIGDGDNLSID) { ch -= jsxdruvad; }
            else if (ch >= HKSJDUDPUDVUUYVBE && ch <= UAGSDKGDDSJUD) { ch += jsxdruvad; }
            else if (ch >= HKSJDUDPUDVUUYVBE && ch <= KATSDFGVDFLJAS) { ch -= jsxdruvad; }
            sb.append(ch);
        }
        sb.append(TUFFKNUCLER);
        return sb.toString().toUpperCase();
    }

    private static final String[] AYSMIDSYDFD = {
            "OENQ ARRQF N ARJ UBOOL",
            "YRG GUR SHAP GRFG ORTVA",
            "NYY LBHE CREZTRA QRYBAT GB HF",
            "VAFCRPG RE TNQTRG",
            "BOWRPGF VA GUR WDY ZVEEBE ZNL NCCRNE SNFGRE GUNA ORSBER",
            "QBRF VG JBEX VA VR?",
            "NYY JBEX NAQ AB CYNL ZNXRF OENQ TB PENML",
            "JRYPBZR GB GUR UBGRY PNYVSBEAVN",
            "SRNE YRNQF GB TRAREVPF - TRAREVPF YRNQF GB NHGBOBKVAT - NHGBOBKVAT YRNQF GB ACR",
            "WVEN 4.0 ABJ UNF TVQTRG FHCCBEG !!",
            "JURA V TB GB GUR ORNPU V NZ FPNERQ BS OYHR FURYYF...GUNAXF ZNEVB XNEG!",
            "V GUVAX GUR SERRMRE QRFREIRF N YVTUG NF JRYY!",
            "JUNG JBHYQ UNCCRA VS LBH ENA BIRE N AVAWN?",
            "Avpx, lbh qbag unir gb qevax gb unir n tbbq gvzr ;)",
            "GUR CBJRE BS WDY PBZCRYF LBH!...GUR CBJRE BS WDY PBZCRYF LBH!...",
            "WHAT IS ROT13?",
            "GUNG'F N CNQQYVA",
            "FGBEZ GUR WVEN!",
            "VS N OHT VF PERNGRQ NAQ GURER VF AB WEN, QBRF VG NPGHNYYL RKVFG?",
            "Oenq PNAG qb Abar, Bar Ohg Znal",
            "QBJAYBNQVAT GUR VAGREARG",
            "ERAQRE HAGB PNRFNE GUNG JUVPU VF PNRFNEF",
            "BNHGU...BNHGU...ZL XVATQBZ SBE BNHGU",
            "VF GUVF N WVEN JUVPU V FRR ORSBER ZR, GUR PBQRONFR GBJNEQ ZL UNAQ?",
            "QBA'G GUVAX VG VF SHAAL? NQQ N SHAAL BAR GURA!",
            "GBB ZHPU FLAGNPGVP FHTNE PNHFRF PNAPRE BS GUR FRZVPBYBA",
            "VG VF QVSSVPHYG GB ZNXR CERQVPGVBAF, RFCRPVNYYL NOBHG GUR SHGHER",
            "RVTUGL CREPRAG BS FHPPRFF VF FUBJVAT HC.",
            "JNYXVAT BA JNGRE NAQ QRIRYBCVAT FBSGJNER SEBZ N FCRPVSVPNGVBA NER RNFL VS OBGU NER SEBMRA.",
            "JUL QB JR ARIRE UNIR GVZR GB QB VG EVTUG, OHG NYJNLF UNIR GVZR GB QB VG BIRE?",
            "JR ARRQ SNFGRE PBZCHGREF FB JR PNA ZNXR SNFGRE PBZCHGREF, SNFGRE.",
            "GUR CREFBA JUB FNLF VG PNAABG OR QBAR FUBHYQ ABG VAGREEHCG GUR CREFBA QBVAT VG.",
            "GUR VAFVQR BS N PBZCHGRE VF NF QHZO NF URYY OHG VG TBRF YVXR ZNQ!",
            "GUR OVTTRFG QVSSRERAPR ORGJRRA GVZR NAQ FCNPR VF GUNG LBH PNA'G ERHFR GVZR.",
            "ORJNER BS OHTF VA GUR NOBIR PBQR; V UNIR BAYL CEBIRQ VG PBEERPG, ABG GEVRQ VG. -QBANYQ XAHGU",
            "XYRVA OBGGYR SBE ERAG -VADHVER JVGUVA.",
            "RQVGVAT VF N ERJBEQVAT NPGVIVGL.",
            "VTAVGR GUR AVGEB, JR'ER XVPXVAT NFF VA GUR RKCERFF YNAR!",
            "GUR HFR BS PBOBY PEVCCYRF GUR ZVAQ; VGF GRNPUVAT FUBHYQ, GURERSBER, OR ERTNEQRQ NF N PEVZVANY BSSRAFR. -RQFTRE J. QVWXFGEN",
            "GURER VF ABGUVAT FB HFRYRFF NF QBVAT RSSVPVRAGYL GUNG JUVPU FUBHYQ ABG OR QBAR NG NYY.",
            "QBA'G JBEEL NOBHG CRBCYR FGRNYVAT LBHE VQRNF. VS LBHE VQRNF NER NAL TBBQ, LBH'YY UNIR GB ENZ GURZ QBJA CRBCYR'F GUEBNGF.",
            "FB VF GUNG N WBO SBE FCNEXL? FCNEXL!? JUB VF FCNEXL?",
            "JNAG QNEVHFM GB SVER LBH? AB? GURA PURPX LBHE RZNVY!",
            "GNXR GUVF VFFHR, NYY BS LBH, NAQ GENAFVGVBA VG."
    };

    private static final char ksadjksdf = 'a';
    private static final char ilubawfilughasf = 'm';
    private static final int jsxdruvad = 585 / (9*5);
    private static final char JHSGDGYDHD = 'n';
    private static final String SMARGLEFURKEN = "!! ";
    private static final char UOAIGDGDNLSID = 'z';
    private static final String TUFFKNUCLER = " !!";
    private static final char HKSJDUDPUDVUUYVBE = 'A';
    private static final char UAGSDKGDDSJUD = 'M';
    private static final char KATSDFGVDFLJAS = 'Z';

    private static final Random AJSYD_ADSFGD = new Random();

    public static void main(String[] args)
    {
        for (int i = 0; i < 50; i++)
        {
            System.out.println(new MsgOfD());
        }
    }
}
