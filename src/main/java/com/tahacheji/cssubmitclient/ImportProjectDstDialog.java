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

import com.intellij.ide.RecentProjectsManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ImportProjectDstDialog extends DialogWrapper {
    private TextFieldWithBrowseButton dstChooser;

    protected ImportProjectDstDialog(@Nullable Project project) {
        super(project, true);
        init();
        setTitle("Import a UMD CS Project");
        setSize(500, getSize().height);
        setOKButtonText("Import Project");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        dstChooser = new TextFieldWithBrowseButton();
        dstChooser.setText(RecentProjectsManager.getInstance().getLastProjectCreationLocation());
        dstChooser.addBrowseFolderListener("Select Destination Directory", null, null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor());
        JLabel label = new JLabel("Select Destination Directory:");
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(dstChooser, gbc);
        return panel;
    }

    public String getDstPath() { return dstChooser.getText(); }

    @Override
    protected void doOKAction() {
        if (dstChooser.getText().isBlank()) {
            dstChooser.setText(RecentProjectsManager.getInstance().getLastProjectCreationLocation());
        }
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
        dstChooser.setText("");
    }

    @Override public boolean isResizable() { return false; }
    @Override protected Action @NotNull [] createActions() { return new Action[]{getOKAction(), getCancelAction()}; }
}
