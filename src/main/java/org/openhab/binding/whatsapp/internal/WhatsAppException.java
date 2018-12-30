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

/**
 * The {@link WhatsAppException} class a binding specific exception class.
 *
 * @author Markus Michels (markus7017) - Initial contribution
 */
public class WhatsAppException extends Exception {

    private static final long serialVersionUID = 4981375820787134863L;

    /**
     * Constructor. Creates new instance of WhatsAppException
     */
    public WhatsAppException() {
        super();
    }

    /**
     * Constructor. Creates new instance of WhatsAppException
     *
     * @param message the detail message.
     */
    public WhatsAppException(String message) {
        super(message);
    }

    /**
     * Constructor. Creates new instance of WhatsAppException
     *
     * @param cause the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public WhatsAppException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor. Creates new instance of WhatsAppException
     *
     * @param message the detail message.
     * @param cause   the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public WhatsAppException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toString() {
        return getMessage() + " (" + getClass() + ")";
    }

}
