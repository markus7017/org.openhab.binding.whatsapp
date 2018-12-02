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

import static org.openhab.binding.whatsapp.internal.WhatsAppBindingConstants.*;

import java.text.MessageFormat;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;

/**
 * The {@link WhatsAppHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author markus7017 - Initial contribution
 */
public class WhatsAppHandler extends BaseThingHandler implements WhatsAppListener {
    private final WhatsAppLogger logger = new WhatsAppLogger(WhatsAppHandler.class, "Handler");
    private WhatsAppConfiguration config = new WhatsAppConfiguration();
    private WhatsAppControl control = null;

    public WhatsAppHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                control.login();
            }

            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            } else {
                // command

                String ch = channelUID.getId();
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_MSGOUT:
                        logger.info("Send message '{}'", command.toString());
                        if (command instanceof StringType) {
                            control.sendMessage(new WhatsAppMessage(command.toString()));
                        }
                        break;
                    case CHANNEL_MEDIAOUT:
                        logger.info("Send message '{}'", command.toString());
                        if (command instanceof StringType) {
                            control.sendMedia(new WhatsAppMessage(command.toString()));
                        }
                        break;
                    default:
                        logger.error("Channel command '{}' for unknown channel '{}'!", command.toString(),
                                channelUID.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Unable to perform channel operation '{}' on channel '{}': {} ({})", command.toString(),
                    channelUID.getId(), e.getMessage(), e.getClass());
        }
    }

    @Override
    public void initialize() {

        String errorMessage = "";
        try {
            // logger.debug("Start initializing!");
            config = getConfigAs(WhatsAppConfiguration.class);

            // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
            // the framework is then able to reuse the resources from the thing handler initialization.
            // we set this upfront to reliably check status updates in unit tests.
            updateStatus(ThingStatus.UNKNOWN);

            // Example for background initialization:
            scheduler.execute(() -> {
                String startError = "";
                try {
                    // background task with long running initialization
                    control = new WhatsAppControl(this, config);
                    control.start();
                    logger.debug("Hub initialized.");
                } catch (Exception e) {
                    startError = MessageFormat.format("exception: {0} ({1})", e.getMessage(), e.getClass());
                    updateThingState(ThingStatus.OFFLINE, "Initialization failed: " + startError);
                }
            });

            logger.debug("Initializing...");
        } catch (Exception e) {
            errorMessage = MessageFormat.format("Exception: {} ({})", e.getMessage(), e.getClass());
            updateThingState(ThingStatus.OFFLINE, errorMessage);
        }
    }

    @Override
    public void onLoginRequired() {
        try {
            control.login();
        } catch (Exception e) {
            logger.error("Unable to login to WhatsApp service: {} ({})", e.getMessage(), e.getClass());
        }
    }

    @Override
    public void onConnected() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.info("Connected to WhatsApp service");
            updateThingState(ThingStatus.ONLINE, "");
        }
    }

    @Override
    public void onInboundMessage(WhatsAppMessage waMessage) {
        logger.info("Received Message: {}", waMessage.toString());
        switch (waMessage.type) {
            case TEXT:
                // simple format
                updateState(CHGROUP_TEXTMESSAGE + "#" + CHANNEL_MSGIN, new StringType(waMessage.toNumberMessage()));
                // JSON format
                updateState(CHGROUP_TEXTMESSAGE + "#" + CHANNEL_MEDIAIN, new StringType(waMessage.toJson()));
                break;
            case IMAGE:
            case VIDEO:
            case AUDIO:
            case DOCUMENT:
                // JSON format
                updateState(CHGROUP_TEXTMESSAGE + "#" + CHANNEL_MEDIAIN, new StringType(waMessage.toJson()));
                break;
            default:
                logger.error("Unable to handle inbound message: type '{}'", waMessage.type);
        }
    }

    @Override
    public void onDisconnect(String cause) {
        if (this.getThing().getStatus() == ThingStatus.ONLINE) {
            logger.info("Disconnected from WhatsApp service");
            updateThingState(ThingStatus.OFFLINE, cause);
        } else {
            logger.debug("Disconnect reported while already in offline mode, cause={})", cause);
        }
    }

    @Override
    public void onWarning(String message) {
        logger.info("{}", message);
    }

    @Override
    public void onError(String message) {
        logger.error("{}", message);
    }

    /**
     * Set thing status (ONLINE/OFFLINE)
     *
     * @param newStatus    ThingStatus.*
     * @param errorMessage Additional information on COMMUNICATION_ERROR
     */
    public void updateThingState(ThingStatus newStatus, String errorMessage) {
        ThingStatus status = this.getThing().getStatus();
        if (status != newStatus) {
            if (newStatus == ThingStatus.INITIALIZING) {
                logger.error("Invalid new thing state: ", newStatus.toString());
            }
            if ((newStatus == ThingStatus.ONLINE) || errorMessage.isEmpty()) {
                updateStatus(newStatus);
            } else {
                logger.error("Error: {}, switch Thing offline", errorMessage);
                updateStatus(newStatus, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            }
        }
    } // setThingState

    @Override
    public void dispose() {
        logger.debug("Shutdown Hub");
        try {
            if (control != null) {
                control.disconnect();
                control = null; // invokes control.dispose();
            }
        } catch (Exception e) {
            logger.debug("Exception on dispose(): {} ({})", e.getMessage(), e.getClass());
        }
    }
}
