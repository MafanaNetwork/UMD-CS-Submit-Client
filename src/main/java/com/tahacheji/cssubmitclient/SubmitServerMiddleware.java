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

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Submission implementation modernized by Taha Cheji for current UMD CAS auth
 * and current IntelliJ builds. This class is based on the older submission
 * middleware from Atheesh Thirumalairajan's original plugin.
 */
public class SubmitServerMiddleware {
    private final Project project;
    private final HashMap<String, String> config;
    private final ProgressIndicator progressIndicator;
    private final HttpClient httpClient;

    public SubmitServerMiddleware(Project project, ProgressIndicator progressIndicator) throws SubmitProjectException {
        this.project = project;
        this.config = new HashMap<>();
        this.progressIndicator = progressIndicator;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        parseSubmitServerConfig();
    }

    public void initiateSubmission() throws SubmitProjectException {
        progressIndicator.setText("Authenticating with the submit server");
        String authenticationType = config.get("authentication.type");
        if ("ldap".equalsIgnoreCase(authenticationType)) {
            negotiateCredentialsLDAP();
        } else if ("cas".equalsIgnoreCase(authenticationType) || "openid".equalsIgnoreCase(authenticationType)) {
            negotiateCredentialsCAS();
        } else {
            throw new SubmitProjectException("Authentication Type not Supported: " + authenticationType);
        }

        progressIndicator.setText("Archiving and uploading files to the submit server");
        Path archivePath = Path.of(project.getBasePath(), "submit.zip");
        try {
            File dst = archivePath.toFile();
            ZipFile archiveFile = new ZipFile(dst);
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setCompressionMethod(CompressionMethod.STORE);
            archiveFile.addFolder(new File(project.getBasePath() + "/src"), zipParameters);
            archiveFile.setComment("ZipFile for submission: CS Submit Client by Taha Cheji");

            String boundary = "----UMDSubmitBoundary" + UUID.randomUUID().toString().replace("-", "");
            HttpRequest.BodyPublisher body = buildMultipartBody(dst, boundary);
            HttpRequest request = HttpRequest.newBuilder(URI.create(config.get("submitURL")))
                    .timeout(Duration.ofSeconds(90))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("User-Agent", "CS-Submit-Client/1.3.0")
                    .POST(body)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String responseString = response.body() == null ? "" : response.body();
            Thread.sleep(250);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new SubmitProjectException("Project Upload Failed (HTTP " + response.statusCode() + "): " + responseString);
            }
            if (!responseString.startsWith("Successful")) {
                throw new SubmitProjectException("Project Upload Failed: " + responseString);
            }
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    null, responseString, "Project Successfully Submitted", JOptionPane.INFORMATION_MESSAGE));
        } catch (SubmitProjectException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SubmitProjectException("Failed to archive or upload project files: " + describeException(ex));
        } finally {
            try { Files.deleteIfExists(archivePath); }
            catch (Exception ex) { System.err.println("Failed to clean generated archive."); }
        }
    }

    private HttpRequest.BodyPublisher buildMultipartBody(File archive, String boundary) throws IOException {
        List<byte[]> body = new ArrayList<>();
        body.add(("--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"submittedFiles\"; filename=\"submit.zip\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.add(Files.readAllBytes(archive.toPath()));
        body.add("\r\n".getBytes(StandardCharsets.UTF_8));

        for (Map.Entry<String, String> entry : config.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if ("submitURL".equals(key) || "password".equals(key) || value == null) continue;
            String safeKey = key.replace("\"", "");
            body.add(("--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"" + safeKey + "\"\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n\r\n" +
                    value + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
        body.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(body);
    }

    private void negotiateCredentialsLDAP() throws SubmitProjectException {
        try {
            final String[] credentials = new String[2];
            SwingUtilities.invokeAndWait(() -> {
                JTextField user = new JTextField(24);
                JPasswordField pass = new JPasswordField(24);
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(new JLabel("LDAP username:"));
                panel.add(user);
                panel.add(Box.createVerticalStrut(8));
                panel.add(new JLabel("Password:"));
                panel.add(pass);
                int result = JOptionPane.showConfirmDialog(null, panel, "UMD Submit Server Login",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    credentials[0] = user.getText();
                    credentials[1] = new String(pass.getPassword());
                }
            });
            if (credentials[0] == null || credentials[1] == null) {
                throw new SubmitProjectException("Authentication cancelled");
            }
            config.put("loginName", credentials[0]);
            config.put("password", credentials[1]);

            String[] params = {"loginName", "password", "courseKey", "projectNumber"};
            StringBuilder form = new StringBuilder();
            for (String param : params) {
                if (form.length() > 0) form.append('&');
                form.append(urlEncode(param)).append('=').append(urlEncode(config.get(param)));
            }

            HttpRequest request = HttpRequest.newBuilder(URI.create(config.get("baseURL") + "/eclipse/NegotiateOneTimePassword"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", "CS-Submit-Client/1.3.0")
                    .POST(HttpRequest.BodyPublishers.ofString(form.toString(), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String responseString = response.body() == null ? "" : response.body();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new SubmitProjectException("Authentication server returned HTTP " + response.statusCode());
            }
            if (responseString.startsWith("failed") || responseString.length() < 5) {
                throw new SubmitProjectException("Invalid Username or Password");
            }
            parseSubmitServerConfig(Arrays.asList(responseString.split("\\n")));
            config.remove("password");
            Thread.sleep(250);
        } catch (SubmitProjectException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SubmitProjectException("Authentication with Server Failed: " + describeException(ex));
        }
    }

    private void negotiateCredentialsCAS() throws SubmitProjectException {
        try {
            String courseKey = config.get("courseKey");
            String projectNumber = config.get("projectNumber");
            String baseURL = config.get("baseURL");
            if (courseKey == null || projectNumber == null || baseURL == null) {
                throw new SubmitProjectException("Missing courseKey, projectNumber, or baseURL in .submit");
            }

            String authUrl = baseURL + "/view/submitStatus.jsp?courseKey=" + urlEncode(courseKey)
                    + "&projectNumber=" + urlEncode(projectNumber);
            boolean browserOpened = false;
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(authUrl));
                    browserOpened = true;
                }
            } catch (Exception ignored) {
                browserOpened = false;
            }

            final String[] verification = new String[1];
            final boolean opened = browserOpened;
            SwingUtilities.invokeAndWait(() -> {
                JTextField field = new JTextField(42);
                String prefix = opened
                        ? "Your browser was opened to the UMD Submit Server.\n"
                        : "Open this URL in your browser:\n" + authUrl + "\n\n";
                JTextArea instructions = new JTextArea(prefix +
                        "Sign in with UMD CAS if asked. The Submit Server will show\n" +
                        "submission verification information. Copy the entire value\n" +
                        "from the browser and paste it below.");
                instructions.setEditable(false);
                instructions.setOpaque(false);
                instructions.setLineWrap(true);
                instructions.setWrapStyleWord(true);
                instructions.setColumns(48);
                instructions.setRows(opened ? 4 : 7);

                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(instructions);
                panel.add(Box.createVerticalStrut(10));
                panel.add(new JLabel("Submission verification information:"));
                panel.add(field);

                int result = JOptionPane.showConfirmDialog(null, panel, "UMD CAS Authentication",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) verification[0] = field.getText();
            });

            if (verification[0] == null) throw new SubmitProjectException("CAS authentication cancelled");
            String[] parsed = parseBrowserVerification(verification[0].trim());
            config.put("classAccount", parsed[0]);
            config.put("oneTimePassword", parsed[1]);
            Thread.sleep(250);
        } catch (SubmitProjectException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SubmitProjectException("CAS authentication failed: " + describeException(ex));
        }
    }

    private String[] parseBrowserVerification(String verification) throws SubmitProjectException {
        if (verification == null || verification.length() <= 2) {
            throw new SubmitProjectException("The submission verification value is empty or too short.");
        }
        try {
            int suppliedChecksum = Integer.parseInt(verification.substring(verification.length() - 1), 16);
            String payload = verification.substring(0, verification.length() - 1);
            int expectedChecksum = payload.hashCode() & 0xF;
            if (suppliedChecksum != expectedChecksum) {
                throw new SubmitProjectException("The submission verification value did not pass its checksum. Copy the entire value from the browser and try again.");
            }
            String[] values = payload.split(";", 2);
            if (values.length != 2 || values[0].isBlank() || values[1].isBlank()) {
                throw new SubmitProjectException("The submission verification value has an unexpected format.");
            }
            return values;
        } catch (NumberFormatException ex) {
            throw new SubmitProjectException("The submission verification value has an unexpected format. Copy the entire value from the browser and try again.");
        }
    }

    private void parseSubmitServerConfig() throws SubmitProjectException {
        progressIndicator.setText("Reading configuration files");
        try {
            List<String> lines = Files.readAllLines(Path.of(project.getBasePath() + "/.submit"), StandardCharsets.UTF_8);
            parseSubmitServerConfig(lines);
            config.put("submitClientVersion", "0.3.1");
            config.put("submitClientTool", "EclipsePlugin");
        } catch (IOException ex) {
            throw new SubmitProjectException("Unable to access the Submit Server configuration for this project.");
        }
    }

    private void parseSubmitServerConfig(List<String> lines) throws SubmitProjectException {
        try {
            for (String line : lines) {
                if (!line.startsWith("#")) {
                    String[] params = line.split("=", 2);
                    if (params.length == 2) config.put(params[0].trim(), params[1].trim());
                }
            }
            Thread.sleep(500);
        } catch (Exception ex) {
            throw new SubmitProjectException("Failed to parse submit server configuration.");
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String describeException(Exception ex) {
        String msg = ex.getMessage();
        return ex.getClass().getSimpleName() + ((msg == null || msg.isBlank()) ? "" : ": " + msg);
    }
}
