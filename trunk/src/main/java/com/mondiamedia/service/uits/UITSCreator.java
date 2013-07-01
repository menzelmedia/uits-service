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

import com.mondiamedia.service.uits.crypto.Signer;
import com.mondiamedia.service.uits.mp3.Mp3Stream;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

/**
 *
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class UITSCreator {

    /**
     * 
     * @param dateOfTransaction
     * @param upc
     * @param isrc
     * @param userId
     * @param mp3
     * @param key
     * @return
     * @throws DatatypeConfigurationException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     * @throws JAXBException
     */
    public String renderUITS(Date dateOfTransaction, String upc, String isrc, String userId, Mp3Stream mp3, File key) throws DatatypeConfigurationException, IOException, NoSuchAlgorithmException, InterruptedException, JAXBException {
        //Create Metadata XML Snippet
        MetadataCreator mdc = new MetadataCreator();
        String metadataSnippet = mdc.render(mdc.createMetadata(dateOfTransaction, upc, isrc, userId, mp3));

        //Sign
        Signer signer = new Signer();
        String signature = signer.sign(metadataSnippet.trim(), key);

        //key id
        ResourceBundle bundle = PropertyResourceBundle.getBundle("uits");
        String keyId = bundle.getString("uits.keyid");

        // "render" and glue it all

        StringBuilder buf = new StringBuilder();
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        buf.append("<uits:UITS xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:uits=\"http://www.udirector.net/schemas/2009/uits/1.1\">");
        //buf.append("\n");
        buf.append(metadataSnippet); // MUST be exacty (bytewise) as rendered before, because of this we dont rerender here
        //buf.append("\n");
        buf.append("<signature algorithm=\"RSA2048\" canonicalization=\"none\" keyID=\"");
        buf.append(keyId);
        buf.append("\">");
        buf.append("\n");
        buf.append(signature);
        //buf.append("\n");
        buf.append("</signature>");
        buf.append("\n");
        buf.append("</uits:UITS>");

        return buf.toString();


    }
}
