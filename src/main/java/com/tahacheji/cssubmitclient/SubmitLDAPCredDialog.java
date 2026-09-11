/*
 * CS Submit Client
 *
 * Modifications Copyright (C) 2026 Taha Cheji
 * Based on the original "UMD CS Project Integration" plugin
 * Copyright (C) 2024 Atheesh Thirumalairajan.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */

package com.tahacheji.cssubmitclient;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SubmitLDAPCredDialog extends DialogWrapper {
    private JTextField usernameField;
    private JPasswordField passwordField;

    protected SubmitLDAPCredDialog(@Nullable Project project) {
        super(project, true);
        init();
        setTitle("Enter Your UMD Directory ID and Password");
        setSize(550, getSize().height);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        JLabel usernameLabel = new JLabel("Directory ID");
        usernameLabel.setForeground(Color.GRAY);
        panel.add(usernameLabel, gbc);
        gbc.gridy = 1;
        usernameField = new JTextField();
        panel.add(usernameField, gbc);
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(Color.GRAY);
        panel.add(passwordLabel, gbc);
        gbc.gridy = 3;
        passwordField = new JPasswordField();
        panel.add(passwordField, gbc);
        return panel;
    }

    public String getUsername() { return usernameField.getText(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    @Override public boolean isResizable() { return false; }
    @Override protected Action @NotNull [] createActions() { return new Action[]{getOKAction()}; }
}
