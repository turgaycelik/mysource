package com.atlassian.jira.avatar;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Cropps image from given stream and provides object that can write result of cropping as avatar in many sizes. This is
 * interface to component that can be injected.
 *
 * @since v6.3
 */
@ExperimentalApi
public interface CroppingAvatarImageDataProviderFactory
{
    /**
     * Creates avatar image data provider that uses fragment of image.
     *
     * @param uploadedImage original image data stream
     * @param cropping image fragment description (if null some matching to requested size part of image is taken)
     * @return image provider - can be used in {@link TypeAvatarService}, {@link com.atlassian.jira.avatar.AvatarManager}
     */
    @Nonnull
    AvatarImageDataProvider createStreamsFrom(@Nonnull InputStream uploadedImage, @Nullable Selection cropping)
            throws IOException;
}
