/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.screenshot.applet;

import com.atlassian.jira.web.util.FileNameCharacterCheckerUtil;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Applet for attaching clipboard images to jira issues. On Mac and Windows,
 * the screenshot function copies the screen image to the clipboard which can
 * be read by this applet. Does not work in linux.
 * @author hchirino
 *
 */
public class ScreenshotApplet extends JApplet implements ActionListener
{
    private ImageIcon imageIcon;
    private JLabel photographLabel;
    private URL postURL;
    private String issueId;
    private String screenshotName;
    private String encoding;
    private String pasteButtonText;
    private String fileNameLabelText;
    private String commentLabelText;
    private String attachButtonText;
    private String cancelButtonText;
    private String commentLevelLabelText;
    private String badAppletConfigurationText;
    private String securityProblemText;
    private String allUsersCommentLevelText;
    private String userAgent;

    private char submitKey;
    private char cancelKey;

    private final transient FileNameCharacterCheckerUtil fileNameCharacterCheckerUtil = new FileNameCharacterCheckerUtil();

    private List<String> commentGroupList;
    private List<ProjectRole> commentRoleList;
    private JTextArea comment;
    private JTextField filename;
    public static final String RELOAD_ACTION = "reload";
    public static final String SUBMIT_ACTION = "submit";
    public static final String CANCEL_ACTION = "cancel";
    private JComboBox commentLevelCombo;
    private JLabel errorMessageLabel;
    private String errorMsgFilenameText;
    List<String> headerList;
    private String projectrolesHeaderText;
    private String groupsHeaderText;

    @Override
    public void init()
    {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        {
            final JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.setBackground(Color.white);
            panel.setOpaque(true);
            contentPane.add(panel, BorderLayout.CENTER);
            contentPane = panel;
        }

        if (!parseParameters())
        {
            final JLabel errorLabel = new JLabel(badAppletConfigurationText);
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            contentPane.add(errorLabel, BorderLayout.CENTER);
            return;
        }

        if (!isSecurityOk())
        {
            final JLabel errorLabel = new JLabel(securityProblemText);
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            contentPane.add(errorLabel, BorderLayout.CENTER);
            return;
        }

        {
            //A label for displaying error messages
            errorMessageLabel = new JLabel();
            errorMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            errorMessageLabel.setBackground(Color.white);
            errorMessageLabel.setForeground(Color.red);
            errorMessageLabel.setOpaque(true);
            contentPane.add(errorMessageLabel, BorderLayout.NORTH);
        }

        // Initialize the comment level header list
        headerList = Arrays.asList(new String[] { projectrolesHeaderText, groupsHeaderText });

        {
            //A label for displaying the photographs.
            photographLabel = new JLabel();
            photographLabel.setHorizontalAlignment(SwingConstants.CENTER);
            photographLabel.setBackground(Color.white);
            photographLabel.setOpaque(true);
            photographLabel.setBorder(BorderFactory.createLineBorder(Color.black));
            contentPane.add(new JScrollPane(photographLabel), BorderLayout.CENTER);
        }

        contentPane = getContentPane();
        final JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        panel.setOpaque(true);
        contentPane.add(panel, BorderLayout.SOUTH);
        contentPane = panel;

        {
            contentPane.setLayout(new GridBagLayout());

            {
                final JPanel reloadPanel = new JPanel();
                reloadPanel.setLayout(new GridBagLayout());
                reloadPanel.setBackground(Color.white);
                reloadPanel.setOpaque(true);
                contentPane.add(reloadPanel, new GBC(0, 0));

                {
                    final JButton reloadButton = new JButton(pasteButtonText);
                    reloadButton.setVerticalTextPosition(SwingConstants.CENTER);
                    reloadButton.setHorizontalTextPosition(SwingConstants.LEFT);
                    reloadButton.setMnemonic(KeyEvent.VK_PASTE);
                    reloadButton.setActionCommand(RELOAD_ACTION);
                    reloadButton.addActionListener(this);
                    reloadButton.setHorizontalAlignment(SwingConstants.LEFT);
                    reloadPanel.add(reloadButton, new GBC(0, 0).setFill(GridBagConstraints.NONE).setInsets(5, 5, 5, 5));
                }
            }
            {
                final JPanel textPanel = new JPanel();
                textPanel.setBackground(Color.white);
                textPanel.setOpaque(true);
                textPanel.setLayout(new GridBagLayout());
                contentPane.add(textPanel, new GBC(0, 1).setFill(GridBagConstraints.BOTH).setWeight(1.0, 0.5));

                {
                    final JTextPane text = new JTextPane();
                    text.setBackground(Color.white);
                    text.setEditable(false);
                    final Style style = text.addStyle("right-align", null);
                    StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
                    text.setParagraphAttributes(style, false);
                    text.repaint();
                    text.setFont(new Font("dialog", Font.BOLD, 12));
                    text.setText(fileNameLabelText);
                    textPanel.add(text, new GBC(0, 0).setAnchor(GridBagConstraints.NORTHEAST).setInsets(5, 5, 5, 5).setFill(
                        GridBagConstraints.HORIZONTAL));

                    filename = new JTextField(screenshotName);

                    textPanel.add(filename, new GBC(1, 0).setFill(GridBagConstraints.HORIZONTAL).setInsets(5, 5, 5, 5));
                }
                {
                    final JTextPane text = new JTextPane();
                    text.setBackground(Color.white);
                    text.setEditable(false);
                    final Style style = text.addStyle("right-align", null);
                    StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
                    text.setParagraphAttributes(style, false);
                    text.repaint();
                    text.setFont(new Font("dialog", Font.BOLD, 12));
                    text.setText(commentLabelText);

                    textPanel.add(text, new GBC(0, 2).setAnchor(GridBagConstraints.NORTHEAST).setInsets(5, 5, 5, 5).setFill(
                        GridBagConstraints.HORIZONTAL));

                    comment = new JTextArea(8, 40);
                    comment.setWrapStyleWord(true);
                    comment.setLineWrap(true);

                    // Create a scroll pane so that the text area can scroll
                    final JScrollPane scrollPane = new JScrollPane(comment, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));

                    textPanel.add(scrollPane, new GBC(1, 2).setFill(GridBagConstraints.BOTH).setInsets(5, 5, 5, 5).setWeight(0.5, 0.5));
                }

                final boolean hasGroups = !commentGroupList.isEmpty();
                final boolean hasProjectRoles = !commentRoleList.isEmpty();
                if (hasGroups || hasProjectRoles)
                {
                    final JTextPane text = new JTextPane();
                    text.setBackground(Color.white);
                    text.setEditable(false);
                    final Style style = text.addStyle("right-align", null);
                    StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
                    text.setParagraphAttributes(style, false);
                    text.repaint();
                    text.setFont(new Font("dialog", Font.BOLD, 12));
                    text.setText(commentLevelLabelText);
                    textPanel.add(text, new GBC(0, 3).setAnchor(GridBagConstraints.NORTHEAST).setInsets(5, 5, 5, 5).setFill(
                        GridBagConstraints.HORIZONTAL).setWeight(0.125, 0.0));
                }

                // Setup a single select box that acts like a select with option groups
                commentLevelCombo = new OptionGroupComboBox(headerList);
                // Add an All Users Level
                commentLevelCombo.insertItemAt(allUsersCommentLevelText, 0);
                // Make the all users level the default selection
                commentLevelCombo.setSelectedIndex(0);
                // Provide a renderer that allows us to make header items unselectable
                commentLevelCombo.setRenderer(new OptionGroupListCellRenderer(headerList));
                // Add the select to the main panel
                textPanel.add(commentLevelCombo, new GBC(1, 3).setAnchor(GridBagConstraints.WEST).setInsets(5, 5, 5, 5));

                // Add the project role options if any exist
                if (hasProjectRoles)
                {
                    commentLevelCombo.addItem("Project Roles");
                    for (final Object item : commentRoleList)
                    {
                        commentLevelCombo.addItem(item);
                    }
                }

                // Add the group options if any exist
                if (hasGroups)
                {
                    commentLevelCombo.addItem("Groups");
                    for (final String item : commentGroupList)
                    {
                        commentLevelCombo.addItem(item);
                    }
                }
            }
            {
                final JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new GridBagLayout());
                buttonPanel.setBackground(Color.white);
                buttonPanel.setOpaque(true);

                contentPane.add(buttonPanel, new GBC(0, 2).setAnchor(GridBagConstraints.CENTER).setInsets(5, 5, 5, 5));

                {
                    final JButton submitButton = new JButton(attachButtonText);
                    submitButton.setVerticalTextPosition(SwingConstants.CENTER);
                    submitButton.setHorizontalTextPosition(SwingConstants.LEFT);
                    submitButton.setActionCommand(SUBMIT_ACTION);
                    submitButton.setMnemonic(submitKey);
                    submitButton.addActionListener(this);
                    buttonPanel.add(submitButton, new GBC(0, 0).setInsets(0, 2, 0, 2));
                }
                {
                    final JButton cancelButton = new JButton(cancelButtonText);
                    cancelButton.setVerticalTextPosition(SwingConstants.CENTER);
                    cancelButton.setHorizontalTextPosition(SwingConstants.RIGHT);
                    cancelButton.setActionCommand(CANCEL_ACTION);
                    cancelButton.setMnemonic(cancelKey);
                    cancelButton.addActionListener(this);
                    buttonPanel.add(cancelButton, new GBC(1, 0).setInsets(0, 2, 0, 2));
                }

                // If we're macs then enable the ctrl key
                if (isMacOSX())
                {
                    final KeyStroke ctrlBackQuote = KeyStroke.getKeyStroke(cancelKey, InputEvent.CTRL_MASK);
                    panel.registerKeyboardAction(this, CANCEL_ACTION, ctrlBackQuote, JComponent.WHEN_IN_FOCUSED_WINDOW);

                    final KeyStroke ctrlS = KeyStroke.getKeyStroke(submitKey, InputEvent.CTRL_MASK);
                    panel.registerKeyboardAction(this, SUBMIT_ACTION, ctrlS, JComponent.WHEN_IN_FOCUSED_WINDOW);
                }
                else
                {
                    // Also add the Alt+` listener since cancel doesn't seem to work consistently
                    final KeyStroke ctrlBackQuote = KeyStroke.getKeyStroke(cancelKey, InputEvent.ALT_MASK);
                    panel.registerKeyboardAction(this, CANCEL_ACTION, ctrlBackQuote, JComponent.WHEN_IN_FOCUSED_WINDOW);
                }
            }
        }
    }

    /**
     * Method parseParameters.
     *
     * @return boolean
     */
    private boolean parseParameters()
    {
        pasteButtonText = getParameter("paste.text", "Paste");
        fileNameLabelText = getParameter("filename.text", "File name: ");
        screenshotName = getParameter("screenshotname", "screenshot-1");
        fileNameLabelText = appendLabelEnding(fileNameLabelText);
        commentLabelText = getParameter("comment.text", "Comment: ");
        commentLabelText = appendLabelEnding(commentLabelText);
        commentLevelLabelText = getParameter("comment.level.text", " Comment Viewable By: ");
        commentLevelLabelText = appendLabelEnding(commentLevelLabelText);
        attachButtonText = getParameter("attach.text", "Attach");
        cancelButtonText = getParameter("cancel.text", "Cancel");
        badAppletConfigurationText = getParameter("badconfiguration.text", "Bad Applet Configuration.");
        securityProblemText = getParameter("security.text",
            "Applet security not setup correctly.  You must accept this applet's certificate for it to run.");
        allUsersCommentLevelText = getParameter("allusers.text", "All Users");
        errorMsgFilenameText = getParameter("errormsg.filename.text", "ERROR: The filename must not contain the characters: ").trim();
        groupsHeaderText = getParameter("groups.text", "Groups");
        projectrolesHeaderText = getParameter("projectroles.text", "Project Roles");
        userAgent = getParameter("useragent"); // Can be null

        // Keys
        submitKey = getParameter("submit.access.key", "S").charAt(0);
        cancelKey = getParameter("cancel.access.key", "`").charAt(0);

        final String post = getParameter("post");
        if (post == null)
        {
            return false;
        }
        try
        {
            final URL base = getDocumentBase();
            postURL = new URL(base, post);
        }
        catch (final MalformedURLException e)
        {
            return false;
        }

        issueId = getParameter("issue");
        if (issueId == null)
        {
            return false;
        }

        final List<String> al = new ArrayList<String>();
        for (int i = 0; true; i++)
        {
            final String x = getParameter("usergroup." + i);
            if (x == null)
            {
                break;
            }
            al.add(x);
        }
        al.toArray(new String[al.size()]);

        encoding = getParameter("encoding");
        if (encoding == null)
        {
            // Default to UTF-8
            encoding = "UTF-8";
        }

        // Parse and build both lists
        commentGroupList = buildViewableByList("comment.group.name.");
        commentRoleList = buildProjectRoleList(buildViewableByList("comment.role."));

        return true;
    }

    /**
     * Builds a List[ScreenshotApplet.ProjectRole] by iterating through the given List[String].
     * @param paramList list of applet parameter values
     * @return list of project roles
     */
    private List<ProjectRole> buildProjectRoleList(final List<String> paramList)
    {
        final List<ProjectRole> roles = new ArrayList<ProjectRole>(paramList.size());
        for (final String value : paramList)
        {
            try
            {
                roles.add(ProjectRole.valueOf(value));
            }
            catch (final IllegalArgumentException e)
            {
                System.err.println("Parameter value '" + value + "' for project role ignored");
            }
            catch (final Exception e)
            {
                System.err.println("Exception caught while creating project role levels: " + e.getMessage());
            }
        }
        return roles;
    }

    private List<String> buildViewableByList(final String viewableListKey)
    {
        final List<String> commentList = new ArrayList<String>();
        for (int i = 0; true; i++)
        {
            final String name = getParameter(viewableListKey + i);
            if ((name == null) || (name.trim().length() == 0))
            {
                break;
            }
            else
            {
                // This is a hack to make it look indented as if it is part of the option group header
                commentList.add("    " + name);
            }
        }
        return commentList;
    }

    private String getParameter(final String key, final String defaultValue)
    {
        final String value = getParameter(key);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return value;
        }
    }

    private String appendLabelEnding(final String s)
    {
        return (s.endsWith(" ")) ? s : s + ((s.endsWith(":")) ? " " : ": ");
    }

    private boolean isSecurityOk()
    {
        try
        {
            Toolkit.getDefaultToolkit().getSystemClipboard();
            return true;
        }
        catch (final AccessControlException e)
        {
            return false;
        }
    }

    public void reload()
    {
        Image image = null;
        try
        {
            final Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if ((t != null) && t.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image")))
            {
                image = (Image) t.getTransferData(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));
            }
            else if (isMacOSX())
            {
                image = getMacImage(t);
            }
        }
        catch (final UnsupportedFlavorException e)
        {
            System.out.println("Unsupported image flavor: " + e);
            e.printStackTrace();
        }
        catch (final IOException e)
        {
            System.out.println("IOException getting clipboard contents: " + e);
            e.printStackTrace();
        }

        if (image == null)
        {
            photographLabel.setIcon(null);
            photographLabel.setText("");
            photographLabel.setPreferredSize(photographLabel.getMinimumSize());
            imageIcon = null;
        }
        else
        {
            imageIcon = new ImageIcon(image);
            photographLabel.setIcon(imageIcon);
            photographLabel.setText(null);
            photographLabel.setPreferredSize(photographLabel.getMinimumSize());
        }
    }

    private boolean isMacOSX()
    {
        final String osname = System.getProperty("os.name");
        final boolean isWin = osname.startsWith("Windows");
        return !isWin && osname.startsWith("Mac");
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent e)
    {
        if (e.getActionCommand().equals(RELOAD_ACTION))
        {
            filename.setBackground(Color.white);
            errorMessageLabel.setText("");
            reload();
        }

        if (e.getActionCommand().equals(SUBMIT_ACTION))
        {
            if (imageIcon == null)
            {
                return;
            }

            if (isFilenameValid())
            {
                postImage();
                runJavascript(SUBMIT_ACTION);
            }
            else
            {
                errorMessageLabel.setText(errorMsgFilenameText + "  " + fileNameCharacterCheckerUtil.getPrintableInvalidCharacters());
                filename.setBackground(Color.red);
            }
        }

        if (e.getActionCommand().equals(CANCEL_ACTION))
        {
            runJavascript(CANCEL_ACTION);
        }
    }

    private boolean isFilenameValid()
    {
        return fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars(filename.getText()) == null;
    }

    /**
     * Method postImage.
     */
    private void postImage()
    {
        try
        {
            MultiPartForm mpf;
            {
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                final BufferedImage image = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
                final Graphics2D g = image.createGraphics();
                g.drawImage(imageIcon.getImage(), 0, 0, this);
                
                writeJpegImage(os, image);


                mpf = new MultiPartForm(encoding);
                mpf.addPart("id", null, null, issueId.getBytes(encoding));
                mpf.addPart("comment", null, null, comment.getText().getBytes(encoding));

                // Don't submit the comment level params if there has been none selected
                if (commentLevelCombo.getSelectedItem().equals(allUsersCommentLevelText))
                {}
                else
                {
                    // We need to distinguish between a group and a role level comment param
                    final Object commentLevel = commentLevelCombo.getSelectedItem();

                    if (commentLevel instanceof ProjectRole)
                    {
                        final String roleCommentLevelId = "role:" + ((ProjectRole) commentLevel).getId().trim();
                        mpf.addPart("commentLevel", null, null, roleCommentLevelId.getBytes(encoding));
                        System.out.println("roleLevel = " + roleCommentLevelId);
                    }
                    else
                    {
                        final String groupCommentLevel = "group:" + ((String) commentLevel).trim();
                        System.out.println("groupLevel = " + groupCommentLevel);
                        mpf.addPart("commentLevel", null, null, groupCommentLevel.getBytes(encoding));
                    }
                }

                String completeFileName = filename.getText();
                if (!completeFileName.endsWith(".jpg"))
                {
                    completeFileName += ".jpg";
                }

                mpf.addPart("filename.1", completeFileName, "image/jpeg", os.toByteArray());
            }

            final byte[] data = mpf.toByteArray();

            System.out.println("Submitting to: " + postURL);

            final URLConnection connection = postURL.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", "multipart/form-data; boundary=" + mpf.getBoundary());
            //JRA-24166: Screenshot applet now tells the server to ignore XSRF check in case the token
            //           is wrong. Which seems to be possible for multiple reasons.
            connection.setRequestProperty("X-Atlassian-Token", "no-check");

            if ((userAgent == null) || userAgent.equals(""))
            {
                System.out.println("WARNING: User-Agent unknown, user will be logged out");
            }
            else
            {
                connection.setRequestProperty("User-Agent", userAgent);
            }

            final OutputStream os = connection.getOutputStream();
            os.write(data);
            os.close();

            BufferedReader in;
            try
            {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            catch (final IOException ex)
            {
                in = null;
            }

            if (in != null)
            {
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    System.out.println(inputLine);
                }

                in.close();
            }
        }
        catch (final IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private void writeJpegImage(final ByteArrayOutputStream os, final BufferedImage bufferedImage) throws IOException
    {
        final ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(os);
        final IIOImage ioImage = new IIOImage(bufferedImage, null, null);
        final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        final ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(0.90f);
        writer.setOutput(imageOutputStream);
        writer.write(null, ioImage, iwp);
    }

    private void runJavascript(final String action)
    {
        try
        {
            String urlString = "javascript:";
            if (action.equals(CANCEL_ACTION))
            {
                urlString += "cancel()";
            }
            else
            {
                urlString += "submit()";
            }

            getAppletContext().showDocument(new URL(urlString));
        }
        catch (final MalformedURLException e)
        {
            System.out.println("JavaScript invocation is not supported.");
        }
    }

    private static class GBC extends GridBagConstraints
    {

        static final long serialVersionUID = 6663429585863688980L;

        /**
         * Constructs a GBC with a given gridx and gridy position and
         * all other grid bag constraint values set to the default.
         *
         * @param gridx the gridx position
         * @param gridy the gridy position
         */
        public GBC(final int gridx, final int gridy)
        {
            this.gridx = gridx;
            this.gridy = gridy;
        }

        /**
         * Sets the cell spans.
         *
         * @param gridwidth  the cell span in x-direction
         * @param gridheight the cell span in y-direction
         * @return this object for further modification
         */
        public GBC setSpan(final int gridwidth, final int gridheight)
        {
            this.gridwidth = gridwidth;
            this.gridheight = gridheight;
            return this;
        }

        /**
         * Sets the anchor.
         *
         * @param anchor the anchor value
         * @return this object for further modification
         */
        public GBC setAnchor(final int anchor)
        {
            this.anchor = anchor;
            return this;
        }

        /**
         * Sets the fill direction.
         *
         * @param fill the fill direction
         * @return this object for further modification
         */
        public GBC setFill(final int fill)
        {
            this.fill = fill;
            return this;
        }

        /**
         * Sets the cell weights.
         *
         * @param weightx the cell weight in x-direction
         * @param weighty the cell weight in y-direction
         * @return this object for further modification
         */
        public GBC setWeight(final double weightx, final double weighty)
        {
            this.weightx = weightx;
            this.weighty = weighty;
            return this;
        }

        /**
         * Sets the insets of this cell.
         *
         * @param distance the spacing to use in all directions
         * @return this object for further modification
         */
        public GBC setInsets(final int distance)
        {
            insets = new java.awt.Insets(distance, distance, distance, distance);
            return this;
        }

        /**
         * Sets the insets of this cell.
         *
         * @param top    the spacing to use on top
         * @param left   the spacing to use to the left
         * @param bottom the spacing to use on the bottom
         * @param right  the spacing to use to the right
         * @return this object for further modification
         */
        public GBC setInsets(final int top, final int left, final int bottom, final int right)
        {
            insets = new java.awt.Insets(top, left, bottom, right);
            return this;
        }

        /**
         * Sets the internal padding
         *
         * @param ipadx the internal padding in x-direction
         * @param ipady the internal padding in y-direction
         * @return this object for further modification
         */
        public GBC setIpad(final int ipadx, final int ipady)
        {
            this.ipadx = ipadx;
            this.ipady = ipady;
            return this;
        }
    }

    // ------------------------------------------------------------------------
    // Code below copied verbatim from http://rsb.info.nih.gov/ij/plugins/download/System_Clipboard.java
    // Part of the public domain 'ImageJ' library

    // Mac OS X's data transfer handling is horribly broken... we
    // need to use the "image/x-pict" MIME type and then Quicktime
    // for Java in order to obtain image data without problems.
    Image getMacImage(final Transferable t)
    {
        if (!isQTJavaInstalled())
        {
            throw new RuntimeException("QuickTime for Java is not installed");
        }
        Image img = null;
        //IJ.log("getMacImage: "+t); IJ.wait(2000);
        final DataFlavor[] d = t.getTransferDataFlavors();
        if ((d == null) || (d.length == 0))
        {
            return null;
        }
        //IJ.log(d[0]+": "+d[0]); IJ.wait(2000);
        try
        {
            final Object is = t.getTransferData(d[0]);
            if ((is == null) || !(is instanceof InputStream))
            {
                throw new RuntimeException("Clipboad does not appear to contain an image");
            }
            img = getImageFromPictStream((InputStream) is);
        }
        catch (final Exception e)
        {}
        return img;
    }

    // Converts a PICT to an AWT image using QuickTime for Java.
    // This code was contributed by Gord Peters.
    Image getImageFromPictStream(final InputStream is)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // We need to strip the header from the data because a PICT file
            // has a 512 byte header and then the data, but in our case we only
            // need the data. --GP
            final byte[] header = new byte[512];
            final byte[] buf = new byte[4096];
            int retval;
            baos.write(header, 0, 512);
            while ((retval = is.read(buf, 0, 4096)) > 0)
            {
                baos.write(buf, 0, retval);
            }
            baos.close();
            final int size = baos.size();
            //IJ.log("size: "+size); IJ.wait(2000);
            if (size <= 0)
            {
                return null;
            }
            final byte[] imgBytes = baos.toByteArray();
            // Again with the uglyness.  Here we need to use the Quicktime
            // for Java code in order to create an Image object from
            // the PICT data we received on the clipboard.  However, in
            // order to get this to compile on other platforms, we use
            // the Java reflection API.
            //
            // For reference, here is the equivalent code without
            // reflection:
            //
            //
            // if (QTSession.isInitialized() == false) {
            //     QTSession.open();
            // }
            // QTHandle handle= new QTHandle(imgBytes);
            // GraphicsImporter gi=
            //     new GraphicsImporter(QTUtils.toOSType("PICT"));
            // gi.setDataHandle(handle);
            // QDRect qdRect= gi.getNaturalBounds();
            // GraphicsImporterDrawer gid= new GraphicsImporterDrawer(gi);
            // QTImageProducer qip= new QTImageProducer(gid,
            //                          new Dimension(qdRect.getWidth(),
            //                                        qdRect.getHeight()));
            // return(Toolkit.getDefaultToolkit().createImage(qip));
            //
            // --GP
            //IJ.log("quicktime.QTSession");
            Class<?> c = Class.forName("quicktime.QTSession");
            Method m = c.getMethod("isInitialized");
            final Boolean b = (Boolean) m.invoke(null);
            if (!b.booleanValue())
            {
                m = c.getMethod("open");
                m.invoke(null);
            }
            c = Class.forName("quicktime.util.QTHandle");
            Constructor<?> con = c.getConstructor(imgBytes.getClass());
            final Object handle = con.newInstance(new Object[] { imgBytes });
            final String s = "PICT";
            c = Class.forName("quicktime.util.QTUtils");
            m = c.getMethod("toOSType", s.getClass());
            final Integer type = (Integer) m.invoke(null, s);
            c = Class.forName("quicktime.std.image.GraphicsImporter");
            con = c.getConstructor(Integer.TYPE);
            final Object importer = con.newInstance(type);
            m = c.getMethod("setDataHandle", Class.forName("quicktime.util." + "QTHandleRef"));
            m.invoke(importer, handle);
            m = c.getMethod("getNaturalBounds");
            final Object rect = m.invoke(importer);
            c = Class.forName("quicktime.app.view.GraphicsImporterDrawer");
            con = c.getConstructor(importer.getClass());
            final Object iDrawer = con.newInstance(importer);
            m = rect.getClass().getMethod("getWidth");
            final Integer width = (Integer) m.invoke(rect);
            m = rect.getClass().getMethod("getHeight");
            final Integer height = (Integer) m.invoke(rect);
            final Dimension d = new Dimension(width.intValue(), height.intValue());
            c = Class.forName("quicktime.app.view.QTImageProducer");
            con = c.getConstructor(iDrawer.getClass(), d.getClass());
            final Object producer = con.newInstance(iDrawer, d);
            if (producer instanceof ImageProducer)
            {
                return (Toolkit.getDefaultToolkit().createImage((ImageProducer) producer));
            }
        }
        catch (final RuntimeException re)
        {
            System.out.println("Runtime Exception: " + re.getMessage());
            re.printStackTrace();
            throw re;
        }
        catch (final Exception e)
        {
            System.out.println("QuickTime for Java error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("QuickTime for java error: " + e);
        }
        return null;
    }

    // Retuns true if QuickTime for Java is installed.
    // This code was contributed by Gord Peters.
    boolean isQTJavaInstalled()
    {
        boolean isInstalled;
        try
        {
            Class.forName("quicktime.QTSession");
            isInstalled = true;
        }
        catch (final Exception e)
        {
            isInstalled = false;
        }
        return isInstalled;
    }

    /**
     * This class is used for retrieving the role level applet parameters, displaying their names in the combo box and
     * sending the selected role level ID back to the server.
     */
    private static class ProjectRole
    {
        private final String id;
        private final String name;

        /**
         * Parses the given string and returns a new instance of this class. The String is split by '|' character,
         * left side is the role level ID that will be sent to the server, right side is the role level name that will
         * be displayed in the combo box.
         *
         * @param idname string to parse
         * @return new instance of this class
         * @throws IllegalArgumentException if invalid string is passed in
         */
        public static ProjectRole valueOf(final String idname) throws IllegalArgumentException
        {
            final int delim = idname.indexOf('|');

            if (delim > 0)
            {
                // This a hack to get the value indented so it looks like it is part of the option group header
                return new ProjectRole(idname.substring(0, delim), "    " + idname.substring(delim + 1));
            }
            else
            {
                throw new IllegalArgumentException("Invalid ID and name");
            }
        }

        /**
         * Creates a new instance of this class if both ID and name are not null.
         * @param id ID
         * @param name name
         * @throws IllegalArgumentException if either ID or name is null
         */
        public ProjectRole(final String id, final String name) throws IllegalArgumentException
        {
            if (id == null)
            {
                throw new IllegalArgumentException("ID can not be null");
            }
            if (name == null)
            {
                throw new IllegalArgumentException("Name can not be null");
            }
            this.id = id;
            this.name = name;
        }

        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        /**
         * Returns the role level name
         * @return the role level name
         */
        public String toString()
        {
            return name;
        }
    }

    private static final class OptionGroupListCellRenderer extends DefaultListCellRenderer
    {
        private final List<String> optionGroupHeaders;

        public OptionGroupListCellRenderer(final List<String> optionGroupHeaders)
        {
            this.optionGroupHeaders = optionGroupHeaders;
        }

        public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
        {
            final boolean enabled = !optionGroupHeaders.contains(value);
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setEnabled(enabled);
            return this;
        }
    }

    private static final class OptionGroupComboBox extends JComboBox
    {
        private final List<String> optionGroupHeaders;

        public OptionGroupComboBox(final List<String> optionGroupHeaders)
        {
            this.optionGroupHeaders = optionGroupHeaders;
        }

        public OptionGroupComboBox(final List<String> optionGroupHeaders, final Object[] objects)
        {
            super(objects);
            this.optionGroupHeaders = optionGroupHeaders;
        }

        public void setSelectedIndex(final int anIndex)
        {
            if (!optionGroupHeaders.contains(getItemAt(anIndex)))
            {
                super.setSelectedIndex(anIndex);
            }
        }
    }

}
