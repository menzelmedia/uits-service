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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class Signer {

    /**
     * <h2>signs the payload with the privateKey in a way specified in the spec pdf by using openssl:</h2>
     * 
     * <ul>
     * <li>Digest SHA256</li>
     * <li>Sign RSA 2048</li>
     * <li>Base64 encode</li>
     * </ul>
     * 
     * <b>openssl must be in PATH or path must be adjusted below or elsewhere</b>
     * @param payload
     * @param privateKey
     * @return string containing the base64 encoded signature (multiline) for usage in the <signature> tag
     * @throws IOException
     * @throws InterruptedException  
     */
    public static String sign(String payload, File privateKey) throws IOException, InterruptedException {

        File temp = File.createTempFile("signature", ".bin");
        temp.deleteOnExit();

        File payloadTemp = File.createTempFile("signature", ".bin");
        payloadTemp.deleteOnExit();


        //Pump payload out in file
        FileWriter writer = null;
        try {
            writer = new FileWriter(payloadTemp);
            writer.append(payload);
            writer.close();
        } catch (IOException ex) {
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        //digest & sign
        Process openssl = Runtime.getRuntime().exec("openssl dgst -sha256 -out " + temp.getAbsolutePath() + " -sign " + privateKey.getAbsolutePath() + " " + payloadTemp.getAbsolutePath());
        openssl.waitFor();
        if (openssl.exitValue() != 0) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            while (openssl.getErrorStream().available() > 0) {
                bout.write(openssl.getErrorStream().read());
            }
            throw new RuntimeException("Execution of openssl failed: " + bout.toString());
        }

        //base64 encode
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Base64OutputStream base64out = new Base64OutputStream(bout, true, 64, "\n".getBytes()); //for openssl compat.

        FileInputStream fin = new FileInputStream(temp);

        IOUtils.copy(fin, base64out);

        base64out.close();
        fin.close();

        return bout.toString();
    }
}
