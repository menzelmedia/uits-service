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
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.udirector.uits.MetadataType;
import net.udirector.uits.ObjectFactory;
import com.mondiamedia.service.uits.mp3.Mp3Hasher;
import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.namespace.QName;

/**
 *
 * A Renderer for the Metadata part of the UITS XML
 * 
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class MetadataCreator {

    private String distributorId = "MondiaMedia";

    /**
     * returns a JAXB MetadataType representation with all values filled in, which can be used to render/marshall the UITS xml
     * @param dateOfTransaction
     * @param upc
     * @param isrc
     * @param userId
     * @param sha256
     * @return metadata part of the UITS xml as jaxb 
     * @throws DatatypeConfigurationException if calendarparsing is b0rken -- should not happen
     */
    public MetadataType createMetadata(Date dateOfTransaction, String upc, String isrc, String userId, byte[] sha256) throws DatatypeConfigurationException {
        ObjectFactory factory = new ObjectFactory();
        MetadataType metadata = factory.createMetadataType();

        metadata.setNonce(Nonce.getNext(8));

        MetadataType.Time time = factory.createMetadataTypeTime();
        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
        gcal.setTime(dateOfTransaction);
        XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        time.setValue(cal);
        metadata.setTime(time);

        MetadataType.Distributor distributor = factory.createMetadataTypeDistributor();
        distributor.setValue(getDistributorId());
        metadata.setDistributor(distributor);

        MetadataType.ProductID pid = factory.createMetadataTypeProductID();
        pid.setType("UPC");
        pid.setCompleted(Boolean.FALSE);  //FIXME: check album buys!?
        pid.setValue(upc);
        metadata.setProductID(pid);

        MetadataType.AssetID aid = factory.createMetadataTypeAssetID();
        aid.setType("ISRC");
        aid.setValue(isrc);
        metadata.setAssetID(aid);

        MetadataType.UID uid = factory.createMetadataTypeUID();
        uid.setValue(userId);
        uid.setVersion(1);
        metadata.getTIDOrUID().add(uid);

        MetadataType.Media media = factory.createMetadataTypeMedia();
        media.setAlgorithm("SHA256");
        media.setValue(sha256);
        metadata.setMedia(media);

        return metadata;
    }

    /**
     * returns a JAXB MetadataType representation with all values filled in, which can be used to render/marshall the UITS xml
     * @param dateOfTransaction
     * @param upc
     * @param isrc
     * @param userId
     * @param mp3 a mp3stream from which the sha256 will be rendered
     * @return metadata part of the UITS xml as jaxb 
     * @throws DatatypeConfigurationException if calendarparsing is b0rken -- should not happen
     * @throws IOException 
     * @throws NoSuchAlgorithmException  
     */
    public MetadataType createMetadata(Date dateOfTransaction, String upc, String isrc, String userId, Mp3Stream mp3) throws DatatypeConfigurationException, IOException, NoSuchAlgorithmException {
        byte[] sha256 = Mp3Hasher.hashAudioFrames(mp3, "SHA-256");
        return createMetadata(dateOfTransaction, upc, isrc, userId, sha256);
    }

    /**
     * 
     * @param metadata
     * @return
     * @throws JAXBException
     * @throws PropertyException
     */
    public String render(MetadataType metadata) throws JAXBException, PropertyException {
        JAXBContext ctx = JAXBContext.newInstance(MetadataType.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_FRAGMENT, true);


        //Hack to get that non-root element marshalled

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        JAXBElement<MetadataType> wrappedArticle = new JAXBElement<MetadataType>(
                new QName("", "metadata"),
                MetadataType.class,
                metadata);

        m.marshal(wrappedArticle, bout);

        String str = bout.toString();
        //Hack to get rid of that namespace, which is sometimes(?) rendered in
        //FIXME: There must be a better way to omit that fucking ns

        Pattern p = Pattern.compile("\\ xmlns:[^\\>]+");
        Matcher matcher = p.matcher(str);

        return matcher.replaceFirst("");
    }

    /**
     * @return the distributorId
     */
    public String getDistributorId() {
        return distributorId;
    }

    /**
     * @param distributorId the distributorId to set
     */
    public void setDistributorId(String distributorId) {
        this.distributorId = distributorId;
    }
}
