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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class ImportProjectAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        descriptor.setTitle("Import a UMD CS Project");
        descriptor.setDescription("Choose your project's downloaded ZIP file");
        descriptor.withFileFilter(file -> file.getName().toLowerCase().endsWith(".zip") && !file.isDirectory());

        VirtualFile selectedFile = FileChooser.chooseFile(descriptor, project, null);
        if (selectedFile == null) return;

        ImportProjectDstDialog dstDialog = new ImportProjectDstDialog(project);
        if (dstDialog.showAndGet() && !dstDialog.getDstPath().isBlank()) {
            ImportProjectCore.startImport(selectedFile.getPath(), dstDialog.getDstPath());
        }
    }
}
