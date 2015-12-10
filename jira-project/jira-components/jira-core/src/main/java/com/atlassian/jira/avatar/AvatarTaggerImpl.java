package com.atlassian.jira.avatar;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.NoSuchElementException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.atlassian.jira.config.util.JiraHome;

import static javax.imageio.ImageTypeSpecifier.createFromBufferedImageType;
import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * Saves an image as a png with metadata signifying this image is a JIRA Avatar (used by the email handler to decide
 * whether or not to attach an image)
 *
 * @since v6.1
 */
public class
        AvatarTaggerImpl implements AvatarTagger
{
    public static final String FORMAT_NAME = "javax_imageio_png_1.0";
    private static final String AVATAR_DIRECTORY = "data/avatars";
    private static final String TAGGED_AVATAR_FILE_SUFFIX = "jrvtg.png";
    private final JiraHome jiraHome;

    public AvatarTaggerImpl(JiraHome jiraHome)
    {
        this.jiraHome = jiraHome;
    }

    public File getAvatarBaseDirectory()
    {
        return new File(jiraHome.getHome(), AVATAR_DIRECTORY);
    }

    @Override
    public String tagAvatar(long id, String filename) throws IOException
    {
        final File base = getAvatarBaseDirectory();
        for (AvatarManager.ImageSize size : AvatarManager.ImageSize.values())
        {
            final String sizeFlag = size.getFilenameFlag();
            final File avatarFileInstance = new File(base, id + "_" + sizeFlag + filename);
            if (avatarFileInstance.exists())
            {
                tagAvatarFile(avatarFileInstance, toTaggedName(avatarFileInstance.getAbsolutePath()));
            }
        }

        // Don't delete the old files until we're done, in case we can't tag all versions of the files
        try
        {
            for (AvatarManager.ImageSize size : AvatarManager.ImageSize.values())
            {
                final String sizeFlag = size.getFilenameFlag();
                File oldAvatarFile = new File(base, id + "_" + sizeFlag + filename);
                if (oldAvatarFile.exists())
                {
                    oldAvatarFile.delete();
                }
            }
        }
        catch (SecurityException ignored) { }

        return toTaggedName(filename);
    }

    private static String toTaggedName(String filename)
    {
        return removeExtension(filename) + TAGGED_AVATAR_FILE_SUFFIX;
    }

    // Package-private for testing / bundled resource tagging
    static void tagAvatarFile(File file) throws IOException
    {
        tagAvatarFile(file, file.getAbsolutePath());
    }

    private static void tagAvatarFile(File file, String newName) throws IOException
    {
        final File outFile = new File(newName);
        BufferedImage in = ImageIO.read(file);
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        IIOMetadata metadata = metadata(writer, writeParam);

        writer.setOutput(new FileImageOutputStream(outFile));
        writer.write(metadata, new IIOImage(in, null, metadata), writeParam);
    }

    public void saveTaggedAvatar(RenderedImage in, String format, OutputStream targetStream) throws IOException
    {
        final ImageWriter writer;
        try
        {
            writer = ImageIO.getImageWritersByFormatName(format).next();
        }
        catch (NoSuchElementException x)
        {
            throw new IllegalArgumentException("format: '" + format + "'");
        }

        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        IIOMetadata metadata = metadata(writer, writeParam);

        ImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(targetStream);
        writer.setOutput(imageOutputStream);
        writer.write(metadata, new IIOImage(in, null, metadata), writeParam);
    }

    @Override
    public void saveTaggedAvatar(RenderedImage in, String name, File file) throws IOException
    {
        final FileImageOutputStream stream = new FileImageOutputStream(file);
        try
        {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            IIOMetadata metadata = metadata(writer, writeParam);

            writer.setOutput(stream);
            writer.write(metadata, new IIOImage(in, null, metadata), writeParam);
        }
        finally
        {
            stream.close();
        }
    }

    private static IIOMetadata metadata(ImageWriter writer, ImageWriteParam writeParam) throws IIOInvalidTreeException
    {
        ImageTypeSpecifier typeSpecifier = createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

        // Create metadata
        IIOMetadataNode root = new IIOMetadataNode(FORMAT_NAME);
        IIOMetadataNode text = new IIOMetadataNode("tEXt");
        IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry");
        textEntry.setAttribute("keyword", JIRA_SYSTEM_IMAGE_TYPE);
        textEntry.setAttribute("value", AVATAR_SYSTEM_IMAGE_TYPE);
        textEntry.setAttribute("encoding", "UTF-8");
        textEntry.setAttribute("language", "EN");
        textEntry.setAttribute("compression", "none");

        text.appendChild(textEntry);
        root.appendChild(text);
        metadata.mergeTree(FORMAT_NAME, root);
        return metadata;
    }
}
