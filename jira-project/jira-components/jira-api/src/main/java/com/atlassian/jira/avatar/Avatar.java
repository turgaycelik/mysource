package com.atlassian.jira.avatar;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import com.atlassian.annotations.PublicApi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Represents an icon for a project or some other entity in JIRA.
 *
 * @since v4.0
 */
@PublicApi
public interface Avatar
{
    /**
     * The type of Avatar.
     *
     * @return a non null Avatar.Type.
     */
    @Nonnull
    Avatar.Type getAvatarType();

    /**
     * The base filename to the avatar image file. The actual file name will be modified with the id etc.
     *
     * @return the non null file name.
     */
    @Nonnull
    String getFileName();

    /**
     * The MIME type of the avatar image file.
     *
     * @return the non null file name.
     */
    @Nonnull
    String getContentType();

    /**
     * The database identifier for the Avatar, may be null if it hasn't yet been stored.
     *
     * @return the database id or null.
     */
    Long getId();

    /**
     * A String representation of the identity of the domain object that this avatar is an avatar for!
     * For example, if it is a user avatar, it would be the username (since that is the primary key), for a Project
     * it is the project ID as a String. The meaning of this should be determined by the
     * {@link com.atlassian.jira.avatar.Avatar.Type}.
     *
     * @return the owner id must not be null.
     */
    @Nonnull
    String getOwner();

    /**
     * Indicates whether the Avatar is a system-provided one or if users have defined it.
     *
     * @return true only if the Avatar is a system-provided one.
     */
    boolean isSystemAvatar();

    /**
     * An indicator of the owner type of the avatar. E.g. project, user, group, role etc.
     */
    public static enum Type
    {

        PROJECT("project"), // all we support today
        USER("user"),
        /**
         * @since v6.3
         */
        ISSUETYPE("issuetype");

        private String name;
        private final static Map<String, Type> typesByName;

        static {
            typesByName = createNameToTypeMap();
        }

        private static ImmutableMap<String, Type> createNameToTypeMap()
        {
            final ImmutableMap.Builder<String, Type> typesByNameBuilder = ImmutableMap.builder();
            for( Type type: Type.values()) {
                typesByNameBuilder.put( type.getName(), type );
            }

            return typesByNameBuilder.build();
        }

        private Type(String name)
        {
            this.name = name;
        }

        /**
         * The canonical String representation of the type.
         *
         * @return the name.
         */
        public String getName()
        {
            return name;
        }

        public static Type getByName(final String name)
        {
            return name==null ?
                    null :
                    typesByName.get(name);
        }
    }

    /**
     * The standard sizes for avatars.
     */
    public static enum Size
    {
        /**
         * A small avatar (24x24 pixels). Use when outputting user's names.
         */
        NORMAL("small", 24),

        /**
         * An extra-small avatar (16x16 pixels).
         */
        SMALL("xsmall", 16),

        /**
         * A medium avatar (32x32 pixels). Use in comments and other activity streams.
         */
        MEDIUM("medium", 32),

        /**
         * A large avatar (48x48 pixels).
         */
        LARGE("large", 48, true),

        XLARGE("xlarge", 64),
        XXLARGE("xxlarge", 96),
        XXXLARGE("xxxlarge", 128),
        RETINA_XXLARGE("xxlarge@2x", 192),
        RETINA_XXXLARGE("xxxlarge@2x", 256);

        /**
         * The value to pass back to the server for the size parameter.
         */
        final String param;

        /**
         * The number of pixels.
         */
        final Integer pixels;

        /**
         * Whether this is the default size.
         */
        final boolean isDefault;

        private final static Size largest;
        private final static Size defaultSize;
        private final static List<Size> orderedSizes;
        private final static Map<String, Size> paramToSize;

        static
        {
            Size maxValue = SMALL;
            Size defaultValue = SMALL;
            for (Size imageSize : values())
            {
                if (imageSize.isDefault)
                {
                    defaultValue = imageSize;
                }
                if (imageSize.pixels > maxValue.pixels)
                {
                    maxValue = imageSize;
                }
            }
            largest = maxValue;
            defaultSize = defaultValue;
            orderedSizes = Size.inPixelOrder();
            paramToSize = createParamToSizeMap();
        }

        private static Map<String, Size> createParamToSizeMap()
        {
            final ImmutableMap.Builder<String, Size> paramToSize = ImmutableMap.builder();
            for( Size size : Size.values()) {
                paramToSize.put(size.getParam(), size);
            }

            return paramToSize.build();
        }

        Size(String param, int pixels, boolean isDefault)
        {
            this.param = param;
            this.isDefault = isDefault;
            this.pixels = pixels;
        }

        private Size(String param, int pixels)
        {
            this(param, pixels, false);
        }

        /**
         * In order to cater for future addition of larger sizes this method finds the largest image size.
         * @return The largest Size
         * @see {@link com.atlassian.jira.avatar.AvatarManager.ImageSize#largest()}, they need to be kept in sync.
         */
        public static Size largest()
        {
            return largest;
        }

        /**
         * @return the default size for avatars.
         * @see {@link com.atlassian.jira.avatar.AvatarManager.ImageSize#defaultSize()}, they need to be kept in sync.
         */
        public static Size defaultSize()
        {
            return defaultSize;
        }

        /**
         * @param pixelValue minimum number of pixels tall+wide the avatar size should be
         * @return an avatar {@link Size} that's equal to or larger than the {@link pixelValue},
         *         or {@link null} if there's no size that could cater the value.
         */
        public static Size biggerThan(int pixelValue)
        {
            Size theSize = null;
            for (Size aSize : Size.inPixelOrder())
            {
                if (aSize.pixels >= pixelValue)
                {
                    theSize = aSize;
                    break;
                }
            }
            return theSize;
        }

        /**
         * @param pixelValue minimum number of pixels tall+wide the avatar size should be
         * @return an avatar {@link Size} that's equal to or larger than the {@link pixelValue},
         *         or {@link null} if there's no size that could cater the value.
         */
        public static Size smallerThan(int pixelValue)
        {
            Size theSize = null;
            for (Size aSize : Lists.reverse(Size.inPixelOrder()))
            {
                if (aSize.pixels <= pixelValue)
                {
                    theSize = aSize;
                    break;
                }
            }
            return theSize;
        }

        static List<Size> inPixelOrder()
        {
            if (null != orderedSizes) return orderedSizes;
            List<Size> orderedSizes = Arrays.asList(Size.values());
            Collections.sort(orderedSizes, new Comparator<Size>()
            {
                @Override
                public int compare(final Size o1, final Size o2)
                {
                    if (o1.getPixels() == o2.getPixels()) return 0;
                    return (o1.getPixels() < o2.getPixels()) ? -1 : 1;
                }
            });
            return orderedSizes;
        }

        public int getPixels()
        {
            return pixels;
        }

        public String getParam()
        {
            return param;
        }

        @Override
        public String toString()
        {
            return String.format("<Size [%s], %dx%dpx>", param, pixels, pixels);
        }

        public static Size getSizeFromParam(final String param)
        {
            if ( null == param || !paramToSize.containsKey(param) )
                throw new NoSuchElementException(param);

            return paramToSize.get(param);
        }
    }

    /**
     * These are the filenames of avatars that used to be available as system avatars, but were
     * to be removed from the list of avatar options available to new projects.
     */
    static final List<String> demotedSystemProjectAvatars = Lists.newArrayList(
            "codegeist.png",
            "jm_black.png",
            "jm_brown.png",
            "jm_orange.png",
            "jm_red.png",
            "jm_white.png",
            "jm_yellow.png",
            "monster.png"
    );
}
