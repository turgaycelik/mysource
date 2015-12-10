package com.atlassian.jira.avatar;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Test;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils.substringBeforeLast;
import static com.atlassian.jira.avatar.AvatarTaggerImpl.tagAvatarFile;
import static com.google.common.collect.Lists.newArrayList;
import static electric.util.file.FileUtil.listFiles;
import static org.apache.commons.lang3.Validate.notNull;
import static org.junit.Assert.fail;

/**
 * This test was written for JRA-25705 and checks that all bundled png images (avatars, icons etc.) contain metadata
 * identifying them as being JIRA system images (a text key-value pair something like "jira-system-image-type:avatar").
 * This is required so that if these are embedded in a notificaton email and then come back in a reply to
 * the notification, JIRA email handlers will know not to attach them to issues.
 *
 * @since v6.1
 */
public class TestResourceTagging
{
    public static final String IMAGE_FORMAT = "javax_imageio_png_1.0";
    private static final String JIRA_SYSTEM_IMAGE_TYPE = "jira-system-image-type";
    private static final String AVATARS_PATH = "jira-components/jira-core/src/main/resources/avatars";
    private static final String MAIL_PATH = "jira-components/jira-webapp/src/main/webapp/images/mail";
    private static final String ICONS_PATH = "jira-components/jira-webapp/src/main/webapp/images/icons";

    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            for (String filename : untaggedDescendantsOf(AVATARS_PATH))
            {
                tagAvatarFile(new File(filename));
            }
            for (String filename : untaggedDescendantsOf(MAIL_PATH))
            {
                tagAvatarFile(new File(filename));
            }
            for (String filename : untaggedDescendantsOf(ICONS_PATH))
            {
                tagAvatarFile(new File(filename));
            }

            return;
        }

        File baseFile = new File(args[0]);

        if (baseFile.isDirectory())
        {
            final RegexFileFilter pngFileFilter = new RegexFileFilter(".*\\.png", IOCase.INSENSITIVE);
            for (File file : listFiles(baseFile, true, new OrFileFilter(DirectoryFileFilter.INSTANCE, pngFileFilter)))
            {
                if (!file.isDirectory() && !containsJiraMetadata(new FileImageInputStream(file)))
                {
                    tagAvatarFile(file);
                }
            }
        }
        else if (baseFile.getName().endsWith(".png"))
        {
            tagAvatarFile(baseFile);
        }
        else
        {
            throw new IllegalArgumentException("Filename provided must be either a directory or a .png");
        }
    }

    @Test
    public void testAvatarsAreTagged() throws URISyntaxException, IOException
    {
        reportUntaggedResources(untaggedDescendantsOf(AVATARS_PATH));
    }

    @Test
    public void testMailSpecificImagesAreTagged() throws URISyntaxException, IOException
    {
        reportUntaggedResources(untaggedDescendantsOf(MAIL_PATH));
    }

    @Test
    public void testIconResourcesAreTagged() throws URISyntaxException, IOException
    {
        reportUntaggedResources(untaggedDescendantsOf(ICONS_PATH));
    }

    private static void reportUntaggedResources(Collection<String> untagged)
    {
        if (untagged.isEmpty())
        {
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("The following untagged png resources were found:").append("\n");
        for (String item : untagged)
        {
            report.append("\n").append(item);
        }
        report.append("\n\nPlease tag these using the main() method of this test class.");
        report.append("\n\nSee JIRA-25705 for more context.");
        fail(report.toString());
    }

    private static List<String> untaggedDescendantsOf(String subPath) throws IOException
    {
        File jiraSourceTreeRoot = new File("");
        final String rootNotFound = "Could not find JIRA source tree root anywhere above working directory";
        while (!isJiraSourcetreeRoot(jiraSourceTreeRoot))
        {
            jiraSourceTreeRoot = notNull(parentOf(jiraSourceTreeRoot), rootNotFound);
        }

        File parentDirectory = new File(jiraSourceTreeRoot, subPath.replaceAll("/", File.separator));
        ArrayList<String> namesOfUntaggedFiles = newArrayList();

        final RegexFileFilter isPng = new RegexFileFilter(".*\\.png", IOCase.INSENSITIVE);
        final IOFileFilter isDirectory = DirectoryFileFilter.DIRECTORY;
        for (File file : notNull(listFiles(parentDirectory, true, new OrFileFilter(isPng, isDirectory))))
        {
            if (!file.isDirectory() && !containsJiraMetadata(new FileImageInputStream(file)))
            {
                namesOfUntaggedFiles.add(file.getPath());
            }
        }
        return namesOfUntaggedFiles;
    }

    private static File parentOf(File jiraSourceTreeRoot)
    {
        return new File(substringBeforeLast(jiraSourceTreeRoot.getAbsolutePath(), File.separator));
    }

    // Checks a couple of folder names to determine whether we are in the JIRA source tree root directory
    private static boolean isJiraSourcetreeRoot(File jiraSourcetreeRoot)
    {
        boolean componentsDirPresent = false, distributionDirPresent = false;

        final File[] childFiles = jiraSourcetreeRoot.listFiles();
        if (childFiles == null)
        {
            return false;
        }
        for (File file : childFiles)
        {
            componentsDirPresent |= "jira-components".equalsIgnoreCase(file.getName());
            distributionDirPresent |= "jira-distribution".equalsIgnoreCase(file.getName());
        }
        return componentsDirPresent && distributionDirPresent;
    }

    // Code adapted from com.atlassian.jira.plugins.mail.handlers.GeneratedAttachmentRecogniser in jira mail plugin
    private static boolean containsJiraMetadata(ImageInputStream avatarStream) throws IOException
    {
        ImageReader imageReader = ImageIO.getImageReadersByFormatName("png").next();
        imageReader.setInput(avatarStream);
        Node metadataTree = imageReader.getImageMetadata(0).getAsTree(IMAGE_FORMAT);
        if (metadataTree == null)
        {
            return false;
        }
        NodeList metadataNodes = metadataTree.getChildNodes();
        for (int i1 = 0; i1 < metadataNodes.getLength(); i1++)
        {
            final Node item = metadataNodes.item(i1);
            if (item == null || !"tEXt".equals(item.getNodeName()))
            {
                continue;
            }
            final NodeList entries = item.getChildNodes();
            for (int i2 = 0; i2 < entries.getLength(); i2++)
            {
                Node entry = entries.item(i2);
                if (entry == null || !"tEXtEntry".equals(entry.getNodeName()))
                {
                    continue;
                }

                final NamedNodeMap attributes = entry.getAttributes();
                if (attributes == null)
                {
                    continue;
                }
                final Node keyword = attributes.getNamedItem("keyword");
                if (keyword != null && JIRA_SYSTEM_IMAGE_TYPE.equals(keyword.getNodeValue()))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
