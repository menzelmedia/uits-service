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

import com.mondiamedia.service.uits.mp3.Mp3Stream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import net.udirector.uits.ObjectFactory;
import net.udirector.uits.UITS;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import junit.framework.TestCase;
import net.udirector.uits.MetadataType;
import org.apache.log4j.Logger;

/**
 *
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class RenderTest extends TestCase {

    Logger logger = Logger.getLogger(this.getClass());

    /**
     * 
     * @throws Exception
     */
    public void testRenderMetadata() throws Exception {



        MetadataCreator creator = new MetadataCreator();
        MetadataType metadata = creator.createMetadata(new Date(), "upc", "isrc", "userid", "Hallo".getBytes("ASCII"));



        String xmlSnippet = creator.render(metadata);
        logger.debug(xmlSnippet);


    }

    /**
     * 
     * @throws Exception
     */
    public void testMetadataFromMp3() throws Exception {
        File file = new File("src/test/sample/vbr.mp3");
        Mp3Stream mp3 = new Mp3Stream(new FileInputStream(file));
        MetadataCreator creator = new MetadataCreator();
        MetadataType metadata = creator.createMetadata(new Date(), "upc", "isrc", "userid", mp3);
        assertNotNull(metadata);

        JAXBContext ctx = JAXBContext.newInstance(MetadataType.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectFactory factory = new ObjectFactory();
        UITS uits = factory.createUITS();
        uits.setMetadata(metadata);
        m.marshal(uits, bout);
        String out = bout.toString();
        logger.debug("My Test");
        logger.debug(out);
    }

    /**
     * 
     */
    public void testNonce() {


        assertEquals(8, Nonce.getNext(8).length());
        assertEquals(40, Nonce.getNext(40).length());
        assertNotSame(Nonce.getNext(8), Nonce.getNext(8));

        logger.debug(Nonce.getNext(8));
        logger.debug(Nonce.getNext(8));
        logger.debug(Nonce.getNext(8));
        logger.debug(Nonce.getNext(8));
        logger.debug(Nonce.getNext(8));


    }
}
