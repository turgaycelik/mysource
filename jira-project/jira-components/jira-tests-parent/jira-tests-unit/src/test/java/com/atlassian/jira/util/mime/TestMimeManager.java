/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util.mime;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import com.atlassian.core.util.ClassLoaderUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestMimeManager
{
    private ByteArrayInputStream bai = new ByteArrayInputStream("image/thisGif gif\ntext/plain xml\napplication/vnd.openxmlformats docx".getBytes());
    // This is derived directly from the mime.types files
    // The application/octet-stream mime types are tested separately
    // The test case checks if the mime type is not equal to "application/octet-stream"
    private String[] allExtensions = {"123","3dml","3ds","3g2","3gp","7z","PNG","aab","aac","aam","aas","abw","ac","acc","ace","acu","acutc","adp","aep","af","afp","ahead","ai","aiff","air","ait","ami","apk","appcache","application","apr","arc","asc","asf","asm","aso","asx","atc","atom","atomcat","atomsvc","atx","au","avi","aw","azf","azs","azw","bcpio","bdf","bdm","bed","bh2","bin","blb","blorb","bmi","bmp","box","boz","btif","bz","bz2","c","c11amc","c11amz","c4d","c4f","c4g","cab","caf","cap","car","cat","cba","cbr","cbt","cc","ccxml","cdbcmsg","cdf","cdkey","cdmia","cdmic","cdmid","cdmio","cdmiq","cdx","cdxml","cdy","cer","cfs","cgm","chat","chm","chrt","cif","cii","cil","cla","class","clkk","clkp","clkt","clkw","clkx","clp","cmc","cmdf","cml","cmp","cmx","cod","com","cpio","cpt","crd","crl","crt","cryptonote","csh","csml","csp","css","csv","cu","curl","cww","cxx","dae","daf","dart","dataless","davmount","dbk","dcr","dcurl","dd2","ddd","deb","der","dfac","dgc","dir","dis","djv","djvu","dll","dmg","dmp","dms","dna","doc","docm","docx","dotm","dotx","dp","dpg","dra","dsc","dssc","dtb","dtd","dts","dtshd","dvb","dvi","dwf","dwg","dxf","dxp","dxr","ecelp4800","ecelp7470","ecelp9600","ecma","edm","edx","efif","ei6","emf","eml","emma","eol","eot","eps","epub","es3","esa","esf","et3","etx","eva","evy","exe","exi","ext","ez","ez2","ez3","f","f4v","f77","fbs","fcdt","fcs","fdf","fe_launch","fg5","fh","fh4","fhc","fig","flac","fli","flo","flv","flw","flx","fly","fm","fnc","for","fpx","frame","fsc","fst","ftc","fti","fvt","fxp","fxpl","fzs","g2w","g3","g3w","gac","gam","gbr","gca","gdl","geo","gex","ggb","ggt","ghf","gif","gim","gml","gmx","gnumeric","gph","gpx","gqf","gqs","gram","gramps","gre","grv","grxml","gsf","gtar","gtm","gtw","gv","gxf","gxt","gz","h261","h263","h264","hal","hbci","hdf","hlp","hpgl","hpid","hps","hqx","htke","htm","html","hvd","hvp","hvs","i2g","icc","ice","icm","ico","ics","ief","ifb","ifm","iges","igl","igm","igs","igx","iif","imp","ims","ink","inkml","install","iota","ipfix","ipk","irm","irp","iso","itp","ivp","ivu","jad","jam","jar","java","jisp","jlt","jnlp","joda","jpe","jpeg","jpg","jpgm","jpgv","jpm","js","json","jsonml","kar","karbon","kfo","kia","kil","kml","kmz","kne","knp","kon","kpr","kpt","kpxx","ksp","ktr","ktx","ktz","kwd","kwt","lasxml","latex","lbd","lbe","les","lha","link66","list3820","listafp","lnk","lostxml","lrm","ltf","lvp","lwp","lzh","m13","m14","m21","m3u","m3u8","m4u","m4v","ma","mads","mag","maker","man","mathml","mb","mbk","mbox","mc1","mcd","mcurl","mdb","mdi","me","mesh","meta4","metalink","mets","mfm","mft","mgp","mgz","mid","midi","mie","mif","mime","mj2","mjp2","mk3d","mka","mks","mkv","mlp","mmd","mmf","mmr","mng","mny","mobi","mods","mov","movie","mp2","mp21","mp3","mp4","mp4a","mp4s","mp4v","mpc","mpe","mpeg","mpg","mpg4","mpga","mpkg","mpm","mpn","mpp","mpy","mqy","mrc","mrcx","ms","mscml","mseed","mseq","msf","msh","msl","msty","mts","mus","musicxml","mvb","mwf","mxf","mxl","mxml","mxs","mxu","n-gage","n3","nb","nbp","nc","ncx","ndl","nfo","ngdat","nitf","nlu","nml","nnd","nns","nnw","npx","nsc","nsf","ntf","nzb","oa2","oa3","oas","obd","obj","oda","odb","odc","odf","odft","odg","odi","odm","odp","ods","odt","oga","ogg","ogv","ogx","omdoc","onetmp","onetoc","onetoc2","opf","opml","oprc","org","osf","osfpvg","otc","otf","otg","oth","oti","otp","ots","ott","oxps","oxt","p","p10","p12","p7b","p7c","p7m","p7r","p7s","p8","pas","patch","paw","pbd","pbm","pcap","pcf","pcl","pclxl","pct","pcurl","pcx","pdb","pdf","pfa","pfb","pfm","pfr","pfx","pgm","pgn","pgp","pic","pki","pkipath","plb","plc","plf","pls","pml","png","pnm","portpkg","potm","potx","ppam","ppd","ppm","ppsm","ppsx","ppt","pptm","pptx","pqa","prc","pre","prf","ps","psb","psd","psf","pskcxml","ptid","pub","pvb","pwn","pya","pyv","qam","qbo","qfx","qps","qt","qwd","qxd","qxt","ra","ram","rar","ras","rcprofile","rdf","rdz","rep","res","rf","rgb","rif","rip","ris","rl","rlc","rld","rm","rmm","rmp","rms","rmvb","rnc","roa","roff","rp","rp9","rpm","rpss","rpst","rq","rs","rsd","rss","rt","rtf","rtx","rv","s","s3m","saf","sbml","sc","scd","scm","scq","scs","scurl","sda","sdc","sdd","sdkd","sdkm","sdp","sdw","see","seed","sema","semd","semf","ser","setpay","setreg","sfd-hdstx","sfs","sfv","sgi","sgl","sgm","sgml","sh","shar","shf","si","sic","sid","sig","sil","silo","sis","sisx","sit","sitx","skd","skp","skt","sl","slc","sldm","sldx","slt","sm","smf","smi","smil","smv","smzip","snf","spc","spf","spl","spot","spp","spq","spx","sql","src","srt","sru","srx","ssdl","sse","ssf","ssml","st","stc","std","stf","sti","stk","stl","str","stw","sub","sus","susp","sv4cpio","sv4crc","svc","svd","svg","svgz","swf","swi","sxc","sxd","sxg","sxi","sxm","sxw","t","t3","taglet","tao","tar","tcap","tcl","teacher","tei","teicorpus","tex","texi","texinfo","tfi","tfm","tga","tgz","thmx","tif","tiff","tmo","torrent","tpl","tpt","tr","tra","trm","tsd","tsv","ttc","ttf","ttl","twd","twds","txd","txf","txt","u32","udeb","ufd","ufdl","ulx","umj","unityweb","uoml","uri","uris","urls","ustar","utz","uu","uva","uvd","uvf","uvg","uvh","uvi","uvm","uvp","uvs","uvt","uvu","uvv","uvva","uvvf","uvvh","uvvi","uvvm","uvvp","uvvs","uvvt","uvvu","uvvv","uvvx","uvvz","uvx","uvz","vcard","vcd","vcf","vcg","vcs","vcx","vis","viv","vob","vor","vrml","vsd","vsf","vss","vst","vtu","vxml","wad","wav","wax","wbmp","wbs","wbxml","wcm","wdp","weba","webm","webp","wg","wgt","wks","wm","wma","wmd","wmf","wml","wmlc","wmls","wmlsc","wmv","wmx","wmz","woff","wpd","wpl","wps","wqd","wri","wrl","wsdl","wspolicy","wtb","wvx","x32","x3d","x3db","x3dbz","x3dv","x3dvz","x3dz","xaml","xap","xar","xbap","xbd","xbm","xdf","xdm","xdp","xdssc","xdw","xenc","xer","xfdf","xfdl","xht","xhtml","xhvml","xif","xlam","xlf","xls","xlsb","xlsm","xlsx","xltm","xltx","xm","xml","xo","xop","xpi","xpl","xpm","xpr","xps","xpw","xpx","xsd","xsl","xslt","xsm","xspf","xul","xvml","xwd","xyz","xz","yang","yin","z1","z2","z3","zaz","zip","zir","zirz","zmm"};
    // Even this is derived from mime.types
    private String[] binExtensions = {"bin", "dms"};

    @Test
    public void testMimeSanitiserChangesGenericMimeTypes()
    {
        MimeManager mimeManager = new MimeManager(bai);
        String genericMimeType = "application/octet-stream";
        String gifFileName = "image.gif";

        assertEquals("image/thisGif", mimeManager.getSanitisedMimeType(genericMimeType, gifFileName));
    }

    @Test
    public void testMimeSanitiserChangesZIPGenericMimeTypes()
    {
        MimeManager mimeManager = new MimeManager(bai);
        String genericMimeType = "application/x-zip-compressed";
        String gifFileName = "doc.docx";

        assertEquals("application/vnd.openxmlformats", mimeManager.getSanitisedMimeType(genericMimeType, gifFileName));
    }

    @Test
    public void testMimeSanitiserIgnoresExistingMimeTypes()
    {
        MimeManager mimeManager = new MimeManager(bai);
        String mimeType = "text/plain";
        String gifFileName = "image.gif";

        assertEquals("text/plain", mimeManager.getSanitisedMimeType(mimeType, gifFileName));
    }

    @Test
    public void testNullConstructor()
    {
        MimeManager mimeManager = new MimeManager(null);
        String mimeType = "text/plain";
        String gifFileName = "image.gif";

        //just check that it runs without exception
        mimeManager.getSanitisedMimeType(mimeType, gifFileName);
    }

    @Test
    public void testAllMimeTypes()
    {
        // While running unit tests the mime.types file defined in atlassian-renderer.jar is getting loaded
        // The mime.types is locally copied to the resources directory of tests, which is loaded here
        // jira-components/jira-tests-parent/jira-tests-unit/src/test/resources/mime.types
        MimeManager mimeManager = new MimeManager(ClassLoaderUtils.getResourceAsStream("mime.types", this.getClass()));
        for(String extn: allExtensions){
            String fileName = "dummy_file." + extn;
            //System.out.println(fileName);
            if(Arrays.asList(binExtensions).contains(extn)){
                assertEquals("application/octet-stream", mimeManager.getSuggestedMimeType (fileName));
            }
            else {
                assertFalse("application/octet-stream".equals(mimeManager.getSuggestedMimeType (fileName)));
            }

        }
    }

    @Test
    public void testEmptyExtension()
    {
        MimeManager mimeManager = new MimeManager(ClassLoaderUtils.getResourceAsStream("mime.types", this.getClass()));
        String fileName = "dummy_file";
        System.out.println(fileName);
        assertEquals("text/html", mimeManager.getSuggestedMimeType (fileName));
    }

    @Test
    public void testJspExtension()
    {
        MimeManager mimeManager = new MimeManager(ClassLoaderUtils.getResourceAsStream("mime.types", this.getClass()));
        String[] fileNames = {"dummy_file.jspa", "dummy_file.jsp"};
        for(String fileName: fileNames) {
            System.out.println(fileName);
            assertEquals("text/html", mimeManager.getSuggestedMimeType (fileName));
        }
    }

    public void atest()
    {
        System.out.println("aaa");
        System.out.println("ClassLoaderUtils.getResource(\"mime.types\", this.getClass()) = " + ClassLoaderUtils.getResource("mime.types", this.getClass()));

        FileTypeMap fileTypeMap = new MimetypesFileTypeMap(ClassLoaderUtils.getResourceAsStream("mime.types", this.getClass()));
        String ft = fileTypeMap.getContentType("abc.xml");
        System.out.println("ft = " + ft);
    }
}
