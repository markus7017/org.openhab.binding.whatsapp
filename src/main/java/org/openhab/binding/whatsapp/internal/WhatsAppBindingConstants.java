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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WhatsAppBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author markus7017 - Initial contribution
 */
@NonNullByDefault
public class WhatsAppBindingConstants {

    private static final String BINDING_ID = "whatsapp";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CHANNEL = new ThingTypeUID(BINDING_ID, "hub");

    // List of all Channel ids
    public static final String CHGROUP_TEXTMESSAGE = "textMessages";
    public static final String CHANNEL_MSGOUT = "messageOut";
    public static final String CHANNEL_MSGIN = "messageIn";
    public static final String CHGROUP_MEDIAMESSAGE = "mediaMessages";
    public static final String CHANNEL_MEDIAIN = "mediaIn";
    public static final String CHANNEL_MEDIAOUT = "mediaIn";
}
