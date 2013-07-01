/*
 * Copyright 2011 Mondia Media GmbH. All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Mondia Media GmbH ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement
 * you entered into with Mondia Media GmbH.

 */
package com.mondiamedia.service.uits.mp3;

/**
 * A low level mp3 frame
 * 
 * see http://upload.wikimedia.org/wikipedia/commons/0/01/Mp3filestructure.svg
 * 
 * 
 * On construction it parses the first 4 bytes of header info
 * 
 * Provides all kind of useful infos after creation. And encapsulates the binary data.
 * 
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class Frame {

    private byte[] data;
    private int version; //1-3  ///almost always 1
    private int layer; //1-3  ///almost always 3
    private int bitrate;
    private int samplingrate;
    private boolean padding;

    /**
     * constructs a new Frame by binary raw data. Parses header etcpp.
     * Input must be a binary byte array containing a complete mp3 frame (needs to be seperated elsewhere), including the initial 11bit syncword
     * @param data -- binary array containing the raw mp3 frame
     * @throws IllegalArgumentException if data is no mp3 frame
     */
    public Frame(final byte[] data) {
        if (data[0] != (byte) 0xff || (data[1] & Integer.parseInt("11100000", 2)) != Integer.parseInt("11100000", 2)) //11 bits of sync
        {
            throw new IllegalArgumentException("No Frame Sync word");
        }

        setVersion(data[1]);
        setLayer(data[1]);
        setBitrate(data[2]);
        setSamplingrate(data[2]);
        setPadding(data[2]);
        this.data = data;
    }

    private void setVersion(byte b) {
        int mask = Integer.parseInt("00011000", 2);

        version = 4 - ((b & mask) >> 3);
    }

    private void setLayer(byte b) {
        int mask = Integer.parseInt("00000110", 2);

        layer = 4 - ((b & mask) >> 1);
    }

    private void setBitrate(byte b) {
        int mask = Integer.parseInt("11110000", 2);

        int rate = (b & mask) >> 4;
        switch (rate) {
            case 0x1:
                bitrate = 32;
                break;
            case 0x2:
                bitrate = 40;
                break;
            case 0x3:
                bitrate = 48;
                break;
            case 0x4:
                bitrate = 56;
                break;
            case 0x5:
                bitrate = 64;
                break;
            case 0x6:
                bitrate = 80;
                break;
            case 0x7:
                bitrate = 96;
                break;
            case 0x8:
                bitrate = 112;
                break;
            case 0x9:
                bitrate = 128;
                break;
            case 0xa:
                bitrate = 160;
                break;
            case 0xb:
                bitrate = 192;
                break;
            case 0xc:
                bitrate = 224;
                break;
            case 0xd:
                bitrate = 256;
                break;
            case 0xe:
                bitrate = 320;
                break;
            case 0xf:
                bitrate = -1; //"bad"
                break;

        }

    }

    private void setSamplingrate(byte b) {
        int mask = Integer.parseInt("00001100", 2);

        int rate = (b & mask) >> 2;
        switch (rate) {
            case 0:
                samplingrate = 44100;
                break;
            case 1:
                samplingrate = 48000;
                break;
            case 2:
                samplingrate = 32000;
                break;
            case 3:
                samplingrate = -1; //reserved
                break;
        }
    }

    private void setPadding(byte b) {
        int mask = Integer.parseInt("00000010", 2);

        int v = (b & mask) >> 1;
        padding = v == 1;
    }

    /**
     * calculates the framesize depending on the header information and the official formulas
     * 
     * From: http://www.multiweb.cz/twoinches/mp3inside.htm
     * @return the calculated header size in bytes
     */
    public int calculateFrameLength() {
        int pad = isPadding() ? 1 : 0;
        //double a = 144.0d * bitrate / (double) samplingrate;
        if (getLayer() == 1) {
            return (int) Math.floor((12.0d * getBitrate() * 1000 / (double) getSamplingrate()) + pad) * 4;
        } else {
            return (int) Math.floor((144.0d * getBitrate() * 1000 / (double) getSamplingrate()) + pad);
        }
    }

    /**
     * 
     * @return a nice String represention with all infos
     */
    @Override
    public String toString() {
        return "Mp3 header: V" + getVersion() + " Layer " + getLayer() + " " + getBitrate() + " kbit , samplingrate " + getSamplingrate() + " , padding: " + isPadding() + "  data: " + getData().length + " bytes (calculated: " + calculateFrameLength() + " bytes) , isInfoTag: " + isInfoTag();
    }

    boolean isValid() {
        return getLayer() < 4 && getVersion() != 3 && getSamplingrate() > 0 && getBitrate() > 0;
    }

    /**
     * is this Frame a info tag and no audio frame?!
     * see http://gabriel.mp3-tech.org/mp3infotag.html for details
     * @return true if Infotag
     */
    boolean isInfoTag() {
        String asString = new String(getData());
        return asString.contains("Xing") || asString.contains("Info");
    }

    /**
     * The raw Binary Data of this mp3 frame
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * MPEG Version (usually 1)
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * MPEG Layer (usually 3 (III))
     * @return the layer
     */
    public int getLayer() {
        return layer;
    }

    /**
     * Bitrate in kbps (32-320)
     * -1 if not parsable or illegal
     * @return the bitrate
     */
    public int getBitrate() {
        return bitrate;
    }

    /**
     * The Samplingrate in Hz
     * (eg 44100)
     * @return the samplingrate
     */
    public int getSamplingrate() {
        return samplingrate;
    }

    /**
     * is this a padding frame? (1 extra byte)
     * @return the padding
     */
    public boolean isPadding() {
        return padding;
    }
}
