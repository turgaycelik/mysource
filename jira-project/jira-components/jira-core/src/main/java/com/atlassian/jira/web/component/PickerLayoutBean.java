package com.atlassian.jira.web.component;

public class PickerLayoutBean
{
    private String currentHeadingKey;
    private String removeAction;
    private String removePrefix;
    private String removeButtonKey;
    private String noKey;

    private String addHeadingKey;
    private String addAction;
    private String addDescKey;
    private String addDescKey2;
    private String addButtonKey;

    private String nameKey;
    private String displayNameKey;

    private boolean isUserLayoutBean;
    private String pickerAction;
    private String pickerName;
    private String pickerId;
    private String pickerTitle;


    public PickerLayoutBean(final String i18nPrefix, String removeUsersPrefix, String removeUsersAction, String addUserAction, boolean isUserLayoutBean, String pickerAction, String  pickerName, String pickerId)
    {
        this.removeAction = removeUsersAction;
        this.removePrefix = removeUsersPrefix;
        this.addAction = addUserAction;
        this.isUserLayoutBean = isUserLayoutBean;
        this.pickerAction = pickerAction;
        this.pickerName = pickerName;
        this.pickerId = pickerId;
        setupI18n(i18nPrefix);
    }

    private void setupI18n(String i18nPrefix)
    {
        this.currentHeadingKey = i18nPrefix + ".watchers.list";
        this.removeButtonKey = i18nPrefix + ".remove";
        this.noKey = i18nPrefix + ".nowatchers";
        this.addHeadingKey = i18nPrefix + ".add.watcher";
        this.addDescKey = i18nPrefix + ".add.details";
        this.addDescKey2 = i18nPrefix + ".add.desc";
        this.addButtonKey = i18nPrefix + ".add";
        this.nameKey = i18nPrefix + ".name";
        this.displayNameKey = i18nPrefix + ".display.name";
        this.pickerTitle = i18nPrefix + ".picker.title";
    }

    public String getCurrentHeadingKey()
    {
        return currentHeadingKey;
    }

    public String getRemoveAction()
    {
        return removeAction;
    }

    public String getRemovePrefix()
    {
        return removePrefix;
    }

    public String getRemoveButtonKey()
    {
        return removeButtonKey;
    }

    public String getNoKey()
    {
        return noKey;
    }

    public String getAddHeadingKey()
    {
        return addHeadingKey;
    }

    public String getAddAction()
    {
        return addAction;
    }

    public String getAddDescKey()
    {
        return addDescKey;
    }

    public String getAddDescKey2()
    {
        return addDescKey2;
    }

    public String getAddButtonKey()
    {
        return addButtonKey;
    }

    public String getNameKey()
    {
        return nameKey;
    }

    public String getDisplayNameKey()
    {
        return displayNameKey;
    }

    public boolean isUserLayoutBean()
    {
        return isUserLayoutBean;
    }

    public String getPickerAction()
    {
        return pickerAction;
    }

    public String getPickerName()
    {
        return pickerName;
    }

    public String getPickerId()
    {
        return pickerId;
    }

    public String getPickerTitle()
    {
        return pickerTitle;
    }
}
