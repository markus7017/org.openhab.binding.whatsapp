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
package org.openhab.binding.whatsapp.internal.control;

/**
 * The {@link WhatsAppListener} class defines the interface to pass console events to the thing handler
 *
 * @author Markus Michels - Initial contribution (markus7017)
 */
public interface WhatsAppListener {

    public void onLoginRequired();

    public void onConnected();

    public void onWarning(String message);

    public void onError(String message);

    public void onDisconnect(String cause);

    public void onInboundMessage(WhatsAppMessage waMessage);
}
