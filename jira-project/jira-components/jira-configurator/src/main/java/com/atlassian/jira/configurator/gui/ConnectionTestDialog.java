package com.atlassian.jira.configurator.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.atlassian.jira.cluster.ClusterSafe;

/**
 * This modal dialog is displayed to the user while the Connection test runs.
 * @since v4.1
 */
public class ConnectionTestDialog extends JDialog
{
    private JButton btnOK;
    private boolean active;
    private JTextArea textArea = new JTextArea();

    public ConnectionTestDialog(final JFrame parent)
    {
        super(parent, "DB Connection Test");
        init(parent);
    }

    @ClusterSafe ("This is not part of the JIRA web app.")
    public synchronized void setActive(boolean active)
    {
        this.active = active;
    }

    @ClusterSafe ("This is not part of the JIRA web app.")
    public synchronized void showIfActive()
    {
        if (active)
        {
            setVisible(true);
        }
        else
        {
            setVisible(true);            
        }
    }

    private void init(JFrame parent)
    {
        setModal(true);
        // Override the Window close event
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent e)
            {
                // Only allow close if the test is finished
                if (btnOK.isEnabled())
                {
                    setVisible(false);
                }
            }
        });

        // Note that this requires that the parent has already realised its size before we construct this Dialog
        setSize(parent.getWidth() - 50, 200);
        setLayout(new BorderLayout());

        textArea.setEditable(false);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.add(textArea);
        add(scrollPane, BorderLayout.CENTER);
        add(newButtonPanel(), BorderLayout.SOUTH);
        setLocationRelativeTo(parent);
    }

    private JPanel newButtonPanel()
    {
        JPanel panel = new JPanel();
        btnOK = new JButton("OK");
        btnOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                setVisible(false);
            }
        });
        btnOK.setEnabled(false);

        panel.add(btnOK);
        return panel;
    }

    public void setText(final String text)
    {
        textArea.setText(text);
    }

    public void addText(String text)
    {
        textArea.setText(textArea.getText() + "\n" + text);
    }

    public void enableCloseButton(final boolean b)
    {
        btnOK.setEnabled(b);
    }
}
