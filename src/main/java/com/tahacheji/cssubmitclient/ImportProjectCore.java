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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImportProjectCore {
    public static void startImport(String srcPath, String dstPath) {
        try {
            ZipFile projectArchive = new ZipFile(srcPath);
            FileHeader fileHeader = projectArchive.getFileHeaders().get(0);
            String rootFolder = fileHeader.getFileName().split("/")[0];
            if (Files.exists(Path.of(dstPath + "/" + rootFolder))) {
                throw new ImportProjectException("Project already exists at Destination");
            }
            projectArchive.extractAll(dstPath);
            importEclipseProject(dstPath + "/" + rootFolder, rootFolder);
            RecentProjectsManager.getInstance().setLastProjectCreationLocation(dstPath);
        } catch (ZipException ex) {
            Messages.showErrorDialog("Failed to extract Project Archive: " + ex.getMessage(), "Project Import Failed");
        } catch (ImportProjectException ex) {
            Messages.showErrorDialog(ex.getMessage(), "Project Import Failed");
        }
    }

    private static void importEclipseProject(String projectRoot, String projectName) throws ImportProjectException {
        try {
            createModuleIML(projectRoot, projectName);
            createModuleXML(projectRoot, projectName);
            Project project = ProjectManager.getInstance().loadAndOpenProject(projectRoot);
            ApplicationManager.getApplication().runWriteAction(() -> {
                Sdk[] sdkTable = ProjectJdkTable.getInstance().getAllJdks();
                if (sdkTable.length > 0) ProjectRootManager.getInstance(project).setProjectSdk(sdkTable[0]);
            });
        } catch (Exception e) {
            throw new ImportProjectException(e.getMessage());
        }
    }

    public static void createModuleIML(String projectRoot, String moduleName)
            throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element moduleElement = document.createElement("module");
        moduleElement.setAttribute("type", "JAVA_MODULE");
        moduleElement.setAttribute("version", "4");
        document.appendChild(moduleElement);

        Element componentElement = document.createElement("component");
        componentElement.setAttribute("name", "NewModuleRootManager");
        moduleElement.appendChild(componentElement);

        Element outputElement = document.createElement("output");
        outputElement.setAttribute("url", "file://$MODULE_DIR$/bin");
        componentElement.appendChild(outputElement);
        componentElement.appendChild(document.createElement("exclude-output"));

        Element contentElement = document.createElement("content");
        contentElement.setAttribute("url", "file://$MODULE_DIR$");
        componentElement.appendChild(contentElement);

        Element sourceFolderElement = document.createElement("sourceFolder");
        sourceFolderElement.setAttribute("url", "file://$MODULE_DIR$/src");
        sourceFolderElement.setAttribute("isTestSource", "false");
        contentElement.appendChild(sourceFolderElement);

        Element orderEntry0 = document.createElement("orderEntry");
        orderEntry0.setAttribute("type", "inheritedJdk");
        componentElement.appendChild(orderEntry0);
        Element orderEntry1 = document.createElement("orderEntry");
        orderEntry1.setAttribute("type", "sourceFolder");
        orderEntry1.setAttribute("forTests", "false");
        componentElement.appendChild(orderEntry1);

        Element orderEntry2 = document.createElement("orderEntry");
        orderEntry2.setAttribute("type", "module-library");
        componentElement.appendChild(orderEntry2);
        Element libraryElement = document.createElement("library");
        libraryElement.setAttribute("name", "JUnit4");
        orderEntry2.appendChild(libraryElement);
        Element classesElement = document.createElement("CLASSES");
        libraryElement.appendChild(classesElement);
        Element root1 = document.createElement("root");
        root1.setAttribute("url", "jar://$MAVEN_REPOSITORY$/junit/junit/4.13.1/junit-4.13.1.jar!/");
        classesElement.appendChild(root1);
        Element root2 = document.createElement("root");
        root2.setAttribute("url", "jar://$MAVEN_REPOSITORY$/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar!/");
        classesElement.appendChild(root2);
        libraryElement.appendChild(document.createElement("JAVADOC"));
        libraryElement.appendChild(document.createElement("SOURCES"));

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        File moduleFile = new File(String.format("%s/%s.iml", projectRoot, moduleName));
        moduleFile.getParentFile().mkdirs();
        transformer.transform(new DOMSource(document), new StreamResult(moduleFile));
    }

    private static void createModuleXML(String projectRoot, String moduleName)
            throws TransformerException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.newDocument();
        Element projectElement = document.createElement("project");
        projectElement.setAttribute("version", "4");
        document.appendChild(projectElement);
        Element componentElement = document.createElement("component");
        componentElement.setAttribute("name", "ProjectModuleManager");
        projectElement.appendChild(componentElement);
        Element modulesElement = document.createElement("modules");
        componentElement.appendChild(modulesElement);
        Element moduleElement = document.createElement("module");
        moduleElement.setAttribute("fileurl", String.format("file://$PROJECT_DIR$/%s.iml", moduleName));
        moduleElement.setAttribute("filepath", String.format("$PROJECT_DIR$/%s.iml", moduleName));
        modulesElement.appendChild(moduleElement);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        File modulesFile = new File(projectRoot + "/.idea/modules.xml");
        modulesFile.getParentFile().mkdirs();
        transformer.transform(new DOMSource(document), new StreamResult(modulesFile));
    }
}
