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

import java.io.StringReader;
import java.text.MessageFormat;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link WhatsAppMessage} class encapsulates the message data and attributes.
 *
 * @author markus7017 - Initial contribution
 */
public class WhatsAppMessage {
    private final WhatsAppLogger logger = new WhatsAppLogger(WhatsAppMessage.class, "Message");

    protected final static String JKEY_TYPE = "type";
    protected final static String JKEY_NUMBER = "number";
    protected final static String JKEY_MESSAGE = "message";
    protected final static String JKEY_URL = "url";
    protected final static String JKEY_PATH = "path";
    protected final static String JKEY_SIZE = "size";
    protected final static String JKEY_CAPTION = "caption";
    protected final static String JKEY_TIMESTAMP = "timestamp";
    protected final static String JKEY_ID = "id";

    public enum WhatsAppMediaType {
        TEXT,
        IMAGE,
        AUDIO,
        VIDEO,
        VCARD
    }

    protected WhatsAppMediaType type = WhatsAppMediaType.TEXT;
    protected String number = "";
    protected String timestamp = "";
    protected String message = "";
    protected String url = "";
    protected String path = "";
    protected String size = "";
    protected String caption = "";
    protected String id = "";

    public WhatsAppMessage() {

    }

    public WhatsAppMessage(String messageString) {
        if (message.startsWith("{", 0) && message.endsWith("}")) {
            // json format
            fromJson(messageString);
        } else {
            if (messageString.contains(":")) { // simple format: number:message
                number = StringUtils.substringBefore(messageString, ":"); // TO-DO: validate that the string is a number
                message = StringUtils.substringAfter(messageString, ":");
            }
        }
    }

    public WhatsAppMessage(WhatsAppMediaType type, String number, String message, String timestamp, String id) {
        this.type = type;
        this.number = number;
        this.timestamp = timestamp;
        this.id = id;
    }

    public WhatsAppMessage(WhatsAppMediaType type, String number, String url, String caption, String path, String size,
            String timestamp, String id) {
        this.type = type;
        this.number = number;
        this.url = url;
        this.caption = caption;
        this.path = path;
        this.size = caption;
        this.timestamp = timestamp;
        this.id = id;
    }

    public WhatsAppMessage fromCliString(String line) throws WhatsAppException {
        String mediaType = StringUtils.substringBetween(line, "[Media Type:", ",");
        if (mediaType != null) {
            // media message
            type = typeFromString(mediaType);
        } else {
            // text message
            type = WhatsAppMediaType.TEXT;
        }
        number = getValue(line, "[", "@");
        timestamp = getValue(line, "(", ")");
        id = getValue(line, ":[", "]");

        switch (type) {
            case TEXT:
                message = getValueAfter(line, "\t").trim();
                break;
            case IMAGE:
            case VIDEO:
            case AUDIO:
                url = getValue(line, "URL:", "]");
                size = getValue(line, "Size:", ",");
                caption = getValueAfter(line, "]");
                break;
            default:
                logger.error("Unsupported Message Type: '{}' (input='{}')", type.toString(), line);
                throw new WhatsAppException(MessageFormat.format("Invalid Media Type:", type.toString()));
        }

        return this;
    }

    @Override
    public String toString() {
        String s = MessageFormat.format("type={0}, number={1}, message=\"{2}\", timestamp={3}, id={4}", type, number,
                message, timestamp, id);
        if (type == WhatsAppMediaType.IMAGE) {
            s = s + MessageFormat.format("url={0}, path={1}, size={2}, caption={3}", url, path, size, caption);
        }
        return s;
    }

    public String toNumberMessage() {
        return number + ":" + message;
    }

    public JsonObject fromJson(String jsonString) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject jMessage = reader.readObject();

        type = typeFromString(getJString(jMessage, JKEY_TYPE, ""));
        number = getJString(jMessage, JKEY_NUMBER, "");
        timestamp = getJString(jMessage, JKEY_TIMESTAMP, "");
        message = getJString(jMessage, JKEY_MESSAGE, "");
        url = getJString(jMessage, JKEY_URL, "");
        path = getJString(jMessage, JKEY_PATH, "");
        size = getJString(jMessage, JKEY_SIZE, "");
        caption = getJString(jMessage, JKEY_CAPTION, "");
        id = getJString(jMessage, JKEY_ID, "");

        return jMessage;
    }

    public String toJson() {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObjectBuilder builder = factory.createObjectBuilder();
        JsonObject jMessage;
        if (type == WhatsAppMediaType.TEXT) {
            // reduced format
            jMessage = builder.add(JKEY_TYPE, type.toString()).add(JKEY_NUMBER, number).add(JKEY_MESSAGE, message)
                    .add(JKEY_TIMESTAMP, timestamp).add(JKEY_ID, id).build();
        } else {
            // full format
            jMessage = builder.add(JKEY_TYPE, type.toString()).add(JKEY_NUMBER, number).add(JKEY_MESSAGE, message)
                    .add(JKEY_URL, url).add(JKEY_PATH, path).add(JKEY_SIZE, size).add(JKEY_CAPTION, caption)
                    .add(JKEY_TIMESTAMP, timestamp).add(JKEY_ID, id).build();
        }
        return jMessage.toString();
    }

    public static WhatsAppMediaType typeFromString(String type) {
        switch (type.toUpperCase()) {
            case "TEXT":
                return WhatsAppMediaType.TEXT;
            case "IMAGE":
                return WhatsAppMediaType.IMAGE;
            case "VIDEO":
                return WhatsAppMediaType.VIDEO;
            case "AUDIO":
                return WhatsAppMediaType.AUDIO;
            case "VCARD":
                return WhatsAppMediaType.VCARD;
            default:
                // logger.error("WhatsAppMessage has invalid type value: {}", type);
                return WhatsAppMediaType.TEXT;
        }
    }

    public WhatsAppMediaType getType() {
        return type;
    }

    public String getNumber() {
        return number;
    }

    public String getMessage() {
        return message;
    }

    public String getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    public String getCaption() {
        return caption;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    private static String getJString(JsonObject json, String key, String defaultString) {
        if (json != null) {
            return json.containsKey(key) ? json.getString(key) : defaultString;
        }
        return defaultString;
    }

    private int getJInt(JsonObject json, String key, int defaultInt) {
        if (json != null) {
            return json.containsKey(key) ? json.getInt(key) : defaultInt;
        }
        return defaultInt;
    }

    private String getValue(String input, String begin, String end) {
        String v = StringUtils.substringBetween(input, begin, end);
        return v != null ? v : "";
    }

    private String getValueAfter(String input, String begin) {
        String v = StringUtils.substringAfterLast(input, begin);
        return v != null ? v : "";
    }
}
