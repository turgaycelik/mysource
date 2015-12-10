package com.atlassian.jira.avatar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.atlassian.jira.config.util.JiraHome;

import com.google.common.collect.Lists;

public class AvatarImageDataStorage
{
    static final String AVATAR_DIRECTORY = "data/avatars";
    private final JiraHome jiraHome;

    public AvatarImageDataStorage(final JiraHome jiraHome) {this.jiraHome = jiraHome;}

    public String getNextFilenameStub()
    {
        return UUID.randomUUID().toString();
    }


    public void storeAvatarFiles(Avatar avatar, AvatarImageDataProvider imageDataProvider) throws IOException
    {
        List<File> createFiles = Lists.newArrayList();
        try
        {
            for (Avatar.Size size : Avatar.Size.values())
            {
                final AvatarManager.ImageSize imageSize = AvatarManager.ImageSize.fromSize(size);
                File file = createAvatarFile(avatar, imageSize.getFilenameFlag());
                createFiles.add(file);

                FileOutputStream output = new FileOutputStream(file);
                try
                {
                    imageDataProvider.storeImage(size, output);
                }
                finally
                {
                    output.close();
                }
            }
        }
        catch (Exception x)
        {
            for (File createFile : createFiles)
            {
                createFile.delete();
            }

            if (x instanceof IOException)
            { throw (IOException) x; }
            else if (x instanceof RuntimeException)
            { throw (RuntimeException) x; }
            else
            { throw new RuntimeException(x); }
        }
    }

    public File createAvatarFile(Avatar avatar, String flag) throws IOException
    {
        final File base = getAvatarBaseDirectory();
        createDirectoryIfAbsent(base);
        return new File(base, avatar.getId() + "_" + flag + avatar.getFileName());
    }

    private File getAvatarBaseDirectory()
    {
        return new File(jiraHome.getHome(), AVATAR_DIRECTORY);
    }

    private void createDirectoryIfAbsent(final File dir) throws IOException
    {
        if (!dir.exists() && !dir.mkdirs())
        {
            throw new IOException("Avatars directory is absent and I'm unable to create it. '" + dir.getAbsolutePath() + "'");
        }
        if (!dir.isDirectory())
        {
            throw new IllegalStateException("Avatars directory cannot be created due to an existing file. '" + dir.getAbsolutePath() + "'");
        }
    }

}