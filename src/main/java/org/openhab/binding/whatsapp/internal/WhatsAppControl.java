/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.whatsapp.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link WhatsAppControl} class provides functions to control the yowsup instance running as a sub process
 *
 * @author Markus Michels (markus7017) - Initial contribution
 */

public class WhatsAppControl {
    private final WhatsAppLogger logger = new WhatsAppLogger(WhatsAppControl.class, "Control");
    private final WhatsAppHandler handler;
    private final WhatsAppConfiguration config;

    private String cliVersion = "";
    private String progVersion = "";
    private Process yowsupProcess = null;

    private BufferedReader input; // will be stdout from the yowsup-cli process
    private OutputStreamWriter output; // will become stdin for the yowsup-cli process, needs to be unbuffered!!

    int exitCode = -1; // exit code of console process

    public WhatsAppControl(WhatsAppHandler handler, WhatsAppConfiguration config) throws WhatsAppException {
        this.config = config;
        this.handler = handler;
    }

    void start() throws IOException, WhatsAppException {
        try {
            String errorMessage = "";
            logger.info("Originating number = '{}'", config.originatingNumber);
            if (config.apiPassword.isEmpty()) {
                throw new WhatsAppException("API password not set, check thing configuration");
            }
            if (config.cliPath.isEmpty()) {
                throw new WhatsAppException("cli-path not set, check thing configuration");
            }

            File f = new File(config.cliPath);
            if (!f.exists() || f.isDirectory()) {
                // do something
                throw new WhatsAppException(MessageFormat.format(
                        "Configured cli-path '{0}' does not exist or is not accessable, check thing configuration",
                        config.cliPath));
            }
            String installDir = System.getProperty("user.home") + "/.yowsup";
            logger.debug("yowsup installation directory: {}", installDir);
            f = new File(installDir);
            if (!f.exists() || !f.isDirectory()) {
                // do something
                throw new WhatsAppException(
                        "It seems that the yowsup data base is not initialized (/root/.yowsup was not found");
            }
            String keyDir = installDir + "/" + config.originatingNumber;
            f = new File(keyDir);
            if (!f.exists() || !f.isDirectory()) {
                // do something
                errorMessage = MessageFormat.format("KeyDB for this orginigating number was not found ({0}", keyDir);
                throw new WhatsAppException(errorMessage);

            }

            // start yowsup console
            // yowsup-cli demos -l "<originatingNumber>:<password from registration>" -y

            String[] args = new String[5];
            args[0] = config.cliPath;
            args[1] = "demos";
            args[2] = "-l";
            args[3] = config.originatingNumber + ":" + config.apiPassword;
            args[4] = "-y";

            logger.debug("Start console: {} {} {} {} {}", args[0], args[1], args[2], args[3], args[4]);
            ProcessBuilder pb = new ProcessBuilder(args[0], args[1], args[2], args[3], args[4]);
            pb.redirectErrorStream(true);
            pb.redirectInput(Redirect.PIPE);
            pb.redirectOutput(Redirect.PIPE);
            Map<String, String> env = pb.environment();
            env.put("PYTHONUNBUFFERED", "no"); // disable stream buffering for Python

            // Start yowsup cli interface, should never terminate
            yowsupProcess = pb.start();
            input = new BufferedReader(new InputStreamReader(yowsupProcess.getInputStream()));
            output = new OutputStreamWriter(yowsupProcess.getOutputStream());
            if (yowsupProcess.isAlive()) {
                logger.debug("Console started");
            }

            new Thread() {
                @Override
                public void run() {
                    logger.debug("Console Reader started");
                    String line = null;
                    try {
                        while ((line = input.readLine()) != null) {
                            try {
                                if (line.length() == 0) {
                                    continue;
                                }
                                logger.info("Console: '{}'", line);

                                // yowsup-cli v2.0.15
                                // Using yowsup v2.5.7
                                if (line.contains("yowsup      v")) {
                                    progVersion = StringUtils.substringAfter(line, "yowsup      v");
                                    logger.debug("Running yowsup version: }≠", cliVersion);
                                    continue;
                                }
                                if (line.contains("yowsup-cli")) {
                                    cliVersion = StringUtils.substringAfter(line, "yowsup-cli ");
                                    logger.debug("Running yowsup-cli version: }≠", cliVersion);
                                    continue;
                                }

                                if (line.contains("Error: Not connected")
                                        || line.contains("Type /help for available commands")) {
                                    handler.onLoginRequired();
                                    continue;
                                }

                                if (line.contains("Auth: Logged in!") || line.contains("[connected]:")) {
                                    handler.onConnected();
                                    continue;
                                }

                                if (line.contains("Login Failed, reason:")) {
                                    handler.onDisconnect(line);
                                    continue;
                                }

                                if (line.contains("@s.whatsapp.net(")) {
                                    // inbound message
                                    WhatsAppMessage waMessage = new WhatsAppMessage();
                                    waMessage.fromCliString(line);
                                    handler.onInboundMessage(waMessage);
                                    continue;
                                }

                                if (line.contains("[offline]")) {
                                    handler.onDisconnect(StringUtils.substringAfter(":", line));
                                    continue;
                                }

                                if (line.contains("WARNING:")) {
                                    handler.onWarning(line);
                                    continue;
                                }
                                if (line.contains("ERROR:")) {
                                    handler.onError(line);
                                    continue;
                                }
                            } catch (Exception e) {
                                logger.error("Unable to process cli line: '{}' - {} ({})", line, e.getMessage(),
                                        e.getClass());

                            }

                        }

                        exitCode = yowsupProcess.exitValue();
                        logger.error("Console was terminated, rc={}", exitCode);
                    } catch (Exception e) {
                        logger.error(MessageFormat.format("Unable to start: {0} ({1}", e.getMessage(), e.getClass()));
                    }
                }
            }.start();
        } catch (Exception e) {
            throw new WhatsAppException(
                    MessageFormat.format("Unable to start the Hub: {0} ({1})", e.getMessage(), e.getClass()));
        }
    }

    void login() throws WhatsAppException {
        logger.info("Logging in to WhatsApp service.");
        sendCommand("/L");
    }

    void sendMessage(WhatsAppMessage waMessage) throws WhatsAppException {
        logger.info("Send message: {}", waMessage.toString());
        // Send text message: /message send <number> <content>
        sendCommand(MessageFormat.format("/message send {0} \"{1}\"", waMessage.getNumber(), waMessage.getMessage()));
    }

    void sendMedia(WhatsAppMessage waMessage) throws WhatsAppException {
        logger.info("Send media: {}", waMessage.toString());
        String caption = !waMessage.getCaption().isEmpty() ? " \"" + waMessage.getCaption() + "\"" : "";
        switch (waMessage.getType()) {
            case TEXT:
                // Send text message:
                sendMessage(waMessage);
                break;
            case IMAGE:
                // Send image: /image send <number> <path> [caption]
                sendCommand(MessageFormat.format("/image send {0} \"{1}\" \"{2}\"", waMessage.getNumber(),
                        waMessage.getPath(), caption));
                break;
            case AUDIO:
                // Send audio file: /audio send <number> <path>
                sendCommand(MessageFormat.format("/audio send {0} \"{1}\" \"{2}\"", waMessage.getNumber(),
                        waMessage.getPath(), caption));
                break;
            case VIDEO:
                // Send video file: /video send <number> <path> [caption]
                sendCommand(MessageFormat.format("/video send {0} \"{1}\" \"{2}\"", waMessage.getNumber(),
                        waMessage.getPath(), caption));
                break;
            case DOCUMENT:
                sendCommand(
                        MessageFormat.format("/document send {0} \"{1}\"", waMessage.getNumber(), waMessage.getPath()));
                break;
            case VCARD:
                // /vcard <number> <vcardName> <contactName> <contactNumber>
                // sendCommand(MessageFormat.format("/vcard {0} \"{1}\" \"{2}\" \"{3}\"", waMessage.getNumber(),));
                break;
            case LOCATION:
                // Send location: /location <number> <latitude> <longitude> [name] [address] [url]
                break;
        }
    }

    void disconnect() throws WhatsAppException {
        logger.info("Disconnecting from WhatsApp service.");
        sendCommand("/disconnect");
    }

    private void sendCommand(String command) throws WhatsAppException {
        try {
            synchronized (output) {
                output.write(command + "\r\n");
                output.flush();
            }
        } catch (Exception e) {
            throw new WhatsAppException("Unable to send console command!", e);
        }

    }

    void stop() throws IOException {
        logger.info("Stopping Console");
        yowsupProcess.destroy();
        if (yowsupProcess.isAlive()) {
            logger.debug("Console still running");
        }
    }

    void dispose() {
        try {
            stop();
        } catch (IOException e) {
        }
    }
}
