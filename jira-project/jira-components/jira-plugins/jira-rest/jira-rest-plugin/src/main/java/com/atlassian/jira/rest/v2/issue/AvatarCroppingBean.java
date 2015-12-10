package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement
public class AvatarCroppingBean
{

    /**
     * Avatar bean example used in auto-generated documentation.
     */
    public static final AvatarCroppingBean DOC_EXAMPLE;
    static
    {
        AvatarCroppingBean avatarCroppingBean = new AvatarCroppingBean();
        avatarCroppingBean.url = "http://example.com/jira/secure/temporaryavatar?cropped=true";
        avatarCroppingBean.needsCropping = true;
        avatarCroppingBean.cropperOffsetY = 50;
        avatarCroppingBean.cropperOffsetX = 50;
        avatarCroppingBean.cropperWidth = 120;

        DOC_EXAMPLE = avatarCroppingBean;
    }

    public static final AvatarCroppingBean DOC_EDIT_EXAMPLE;
    static
    {
        AvatarCroppingBean avatarCroppingBean = new AvatarCroppingBean();
        avatarCroppingBean.cropperOffsetY = 50;
        avatarCroppingBean.cropperOffsetX = 50;
        avatarCroppingBean.cropperWidth = 120;

        DOC_EDIT_EXAMPLE = avatarCroppingBean;
    }

    @XmlElement
    private int cropperWidth;

    @XmlElement
    private int cropperOffsetX;

    @XmlElement
    private int cropperOffsetY;

    @XmlElement
    private String url;

    @XmlElement
    private boolean needsCropping;

    public AvatarCroppingBean()
    {
    }

    public AvatarCroppingBean(String url, int cropperWidth, int cropperOffsetY, int cropperOffsetX, boolean needsCropping)
    {
        this.url = url;
        this.cropperWidth = cropperWidth;
        this.cropperOffsetY = cropperOffsetY;
        this.cropperOffsetX = cropperOffsetX;
        this.needsCropping = needsCropping;
    }

    public boolean isNeedsCropping()
    {
        return needsCropping;
    }

    public String getUrl()
    {
        return url;
    }

    public int getCropperWidth()
    {
        return cropperWidth;
    }

    public void setCropperWidth(int cropperWidth)
    {
        this.cropperWidth = cropperWidth;
    }

    public int getCropperOffsetX()
    {
        return cropperOffsetX;
    }

    public void setCropperOffsetX(int cropperOffsetX)
    {
        this.cropperOffsetX = cropperOffsetX;
    }

    public int getCropperOffsetY()
    {
        return cropperOffsetY;
    }

    public void setCropperOffsetY(int cropperOffsetY)
    {
        this.cropperOffsetY = cropperOffsetY;
    }
}
