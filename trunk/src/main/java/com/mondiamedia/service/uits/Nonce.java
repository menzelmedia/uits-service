/*
 * Copyright 2011 Mondia Media GmbH. All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Mondia Media GmbH ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement
 * you entered into with Mondia Media GmbH.

 */
package com.mondiamedia.service.uits;

import java.util.UUID;
import org.apache.commons.codec.binary.Base64;

/**
 * A pseudo generated random alphanumerical Nonce 
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class Nonce {

    //long seed = System.currentTimeMillis();
    private static String getNextUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * generate a new pseudo random nonce not longer than maxLength chars
     * Note that maximum length is limited to 40 or s.t.l.
     * @param maxLength 
     * @return a new nonce
     */
    public static String getNext(int maxLength) {
        Base64 enc = new Base64();

        String base64 = enc.encodeToString(getNextUUID().getBytes());
        if (maxLength < base64.length()) {
            return base64.substring(0, maxLength);
        }
        return base64;

    }
}
