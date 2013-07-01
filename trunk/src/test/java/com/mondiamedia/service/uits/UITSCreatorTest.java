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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Date;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import junit.framework.TestCase;
import net.udirector.uits.UITS;
import org.apache.log4j.Logger;

/**
 *
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class UITSCreatorTest extends TestCase {

    Logger logger = Logger.getLogger(this.getClass());

    /**
     * 
     */
    public UITSCreatorTest() {
    }

    /**
     * 
     * @throws Exception
     */
    public void testRenderUITS() throws Exception {
        File file = new File("src/test/sample/vbr-tagged.mp3");
        Mp3Stream mp3 = new Mp3Stream(new FileInputStream(file));

        URL keyRes = getClass().getResource("/keys/mondiamedia.pem");
        File key = new File(keyRes.getFile()); //XXX: needs to be a tempfile in Live Usage (e.g. when put in jar)
        UITSCreator creator = new UITSCreator();
        String xml = creator.renderUITS(new Date(), "upc", "isrc", "userid", mp3, key);

        assertNotNull(xml);

        logger.debug(xml);
        //Validate 

        JAXBContext ctx = JAXBContext.newInstance(UITS.class);
        Unmarshaller m = ctx.createUnmarshaller();

        ByteArrayInputStream bin = new ByteArrayInputStream(xml.getBytes());

        UITS uits = (UITS) m.unmarshal(bin);

        assertNotNull(uits);
        
        File outfile = new File("payload.txt");
        if(outfile.exists())
            outfile.delete();
        
        FileOutputStream fout = new FileOutputStream(outfile);
        fout.write(xml.getBytes());
        fout.close();
        
                
    }
}
