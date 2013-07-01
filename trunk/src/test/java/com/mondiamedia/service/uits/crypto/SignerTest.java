/*
 * Copyright 2011 Mondia Media GmbH. All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Mondia Media GmbH ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement
 * you entered into with Mondia Media GmbH.

 */
package com.mondiamedia.service.uits.crypto;

import java.io.File;
import java.net.URL;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

/**
 *
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class SignerTest extends TestCase {

    private Logger logger = Logger.getLogger(SignerTest.class);

    // Does not work on hudson!
    /**
     * 
     * @throws Exception
     */
    public void testSign() throws Exception {
        File openssl1 = new File("/home/matze"); //For the freaks...
        File openssl2 = new File("C:\\Program Files (x86)\\GnuWin32\\bin\\openssl.exe"); //For the non freaks... :)

        if (openssl1.exists() || openssl2.exists()) {
            internalTestSign();
        } else {
            logger.error("seems to run on hudson sign tests skipped");
        }
    }

    /**
     * 
     * @throws Exception
     */
    public void internalTestSign() throws Exception {
        Signer signer = new Signer();
        URL keyRes = getClass().getResource("/keys/mondiamedia.pem");

        assertNotNull(keyRes);

        logger.debug(keyRes.getFile());

        File key = new File(keyRes.getFile());
        assertTrue(key.exists());
        assertTrue(key.canRead());

        String signed = signer.sign("Hallo Welt", key);
        assertNotNull(signed);
        logger.debug("\nSIGNED BASE64\n" + signed);

        assertEquals(
                "ToHa2mjtnVLyZNdVkNOXOFz5xy/wUcZrFsrsfp7fKpACTfafSrzhKvb2qn7DGC4f\n"
                + "HZB65iAehMOGnEGGI6BIWOesiCV2RXGOASBTg1VPD8DcSMw2VaAanGaS2REL68gr\n"
                + "vMwC0XUJfb5oS0HTXSdAv/kWTBhuChsJRLlJKpls8/YuSuvNytlocc+cEHZFTQ/K\n"
                + "0DS7nWkZdPzV+hl1OGqZRUGdhdSVrLeNJm1TDk0roBr55IcZ4KaihdDGRoy5MIf0\n"
                + "lkFttsxSqWbQbXEfo7sh3Lo7p6EDJ0TB5zhPSUH6eFzq1YtQwqsQ0OdoY5gzvrtX\n"
                + "X4v2SaWnDJjc3yQ0WJzrrA==\n",
                signed);
    }
}
