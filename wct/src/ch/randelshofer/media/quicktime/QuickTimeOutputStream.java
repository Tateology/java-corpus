package ch.randelshofer.media.quicktime;
/**
 * @(#)QuickTimeOutputStream.java  1.0.1  2008-06-18
 *
 * Copyright (c) 2008 Werner Randelshofer
 * Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms.
 */


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 * This class supports writing of images as frames into the video track of
 * a QuickTime movie file.
 * <p>
 * All frames are encoded either using the JPG or the PNG video format.
 * Each frame can have an individual encoding quality and duration.
 * <p>
 * For detailed information about the QuickTime file format see:
 * http://developer.apple.com/documentation/QuickTime/QTFF/
 *
 *
 * @author Werner Randelshofer
 * @version 1.0.1 2008-06-18 WideLeafAtom wrote incorrect header for
 * atoms larger than 4 GB. The default value of time scale is now 600.
 * Renamed method writeFrame to writeFrame. Added writeFrame methods
 * which take a file or an input stream as argument.
 * <br>1.0 Jun 15, 2008 Created.
 */
public class QuickTimeOutputStream {

    /**
     * Output stream of the QuickTimeOutputStream.
     */
    private ImageOutputStream out;

    /**
     * Supported video encodings.
     */
    public static enum VideoFormat {

        JPG, PNG;
    }
    /**
     * Current video formats.
     */
    private VideoFormat videoFormat;
    /**
     * Quality of JPEG encoded video frames.
     */
    private float quality = 0.9f;
    /**
     * Creation time of the movie output stream.
     */
    private Date creationTime;
    /**
     * Width of the video frames. All frames must have the same width.
     * The value -1 is used to mark unspecified width.
     */
    private int imgWidth = -1;
    /**
     * Height of the video frames. All frames must have the same height.
     * The value -1 is used to mark unspecified height.
     */
    private int imgHeight = -1;
    /**
     * The timeScale of the movie.
     */
    private int timeScale = 600;

    /**
     * The states of the movie output stream.
     */
    private static enum States {

        STARTED, FINISHED, CLOSED;
    }
    /**
     * The current state of the movie output stream.
     */
    private States state = States.FINISHED;

    /**
     * QuickTime stores media data in samples.
     * A sample is a single element in a sequence of time-ordered data.
     * Samples are stored in the mdat atom.

     */
    private static class Sample {

        /** Offset of the sample relative to the start of the QuickTime file.
         */
        long offset;
        /** Data length of the sample. */
        long length;
        /**
         * The duration of the sample in time scale units.
         */
        int duration;

        /**
         * Creates a new sample.
         * @param duration
         * @param offset
         * @param length
         */
        public Sample(int duration, long offset, long length) {
            this.duration = duration;
            this.offset = offset;
            this.length = length;
        }
    }
    /**
     * List of video frames.
     */
    private LinkedList<Sample> videoFrames;
    /**
     * This atom holds the movie frames.
     */
    private WideLeafAtom mdatAtom;

    /**
     * Atom base class.
     */
    private abstract class Atom {

        /**
         * The type of the atom.
         */
        protected String type;
        /**
         * The offset of the atom relative to the start of the
         * ImageOutputStream.
         */
        protected long offset;

        /**
         * Creates a new Atom at the current position of the ImageOutputStream.
         * @param type The type of the atom. A string with a length of 4 characters.
         */
        public Atom(String type) throws IOException {
            this.type = type;
            offset = out.getStreamPosition();
        }

        /**
         * Writes the atom to the ImageOutputStream and disposes it.
         */
        public abstract void finish() throws IOException;

        /**
         * Returns the size of the atom including the size of the atom header.
         * @return The size of the atom.
         */
        public abstract long size();
    }

    /**
     * Composite Atom.
     */
    private class CompositeAtom extends Atom {

        private LinkedList<Atom> children;
        private boolean finished;

        /**
         * Creates a new CompositeAtom at the current position of the
         * ImageOutputStream.
         * @param type The type of the atom.
         */
        public CompositeAtom(String type) throws IOException {
            super(type);
            out.writeLong(0); // make room for the atom header
            children = new LinkedList<Atom>();
        }

        public void add(Atom child) throws IOException {
            if (children.size() > 0) {
                children.getLast().finish();
            }
            children.add(child);
        }

        /**
         * Writes the atom and all its children to the ImageOutputStream
         * and disposes of all resources held by the atom.
         * @throws java.io.IOException
         */
        @Override
                public void finish() throws IOException {
            if (!finished) {
                if (size() > 0xffffffffL) {
                    throw new IOException("CompositeAtom \"" + type + "\" is too large: " + size());
                }

                long pointer = out.getStreamPosition();
                out.seek(offset);

                AtomDataOutputStream headerData = new AtomDataOutputStream(new FilterImageOutputStream(out));
                headerData.writeInt((int) size());
                headerData.writeType(type);
                for (Atom child : children) {
                    child.finish();
                }
                out.seek(pointer);
                finished = true;
            }
        }

        @Override
                public long size() {
            long length = 8;
            for (Atom child : children) {
                length += child.size();
            }
            return length;
        }
    }

    /**
     * Leaf Atom.
     */
    private class LeafAtom extends Atom {

        private AtomDataOutputStream data;
        private boolean finished;

        /**
         * Creates a new LeafAtom at the current position of the
         * ImageOutputStream.
         * @param type The type of the atom.
         */
        public LeafAtom(String name) throws IOException {
            super(name);
            out.writeLong(0); // make room for the atom header
            data = new AtomDataOutputStream(new FilterImageOutputStream(out));
        }

        public AtomDataOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("Atom is finished");
            }
            return data;
        }

        /**
         * Returns the offset of this atom to the beginning of the random access file
         * @return
         */
        @SuppressWarnings("unused")
                public long getOffset() {
            return offset;
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                long sizeBefore = size();

                if (size() > 0xffffffffL) {
                    throw new IOException("LeafAtom \"" + type + "\" is too large: " + size());
                }

                long pointer = out.getStreamPosition();
                out.seek(offset);

                AtomDataOutputStream headerData = new AtomDataOutputStream(new FilterImageOutputStream(out));
                headerData.writeUInt(size());
                headerData.writeType(type);
                out.seek(pointer);
                finished = true;
                long sizeAfter = size();
                if (sizeBefore != sizeAfter) {
                    System.err.println("size mismatch " + sizeBefore + ".." + sizeAfter);
                }
            }
        }

        @Override
        public long size() {
            return 8 + data.size();
        }
    }

    /**
     * Wide Leaf Atom can grow larger then 4 gigabytes.
     */
    private class WideLeafAtom extends Atom {

        private AtomDataOutputStream data;
        private boolean finished;

        /**
         * Creates a new LeafAtom at the current position of the
         * ImageOutputStream.
         * @param type The type of the atom.
         */
        public WideLeafAtom(String name) throws IOException {
            super(name);
            out.writeLong(0); // make room for the atom header
            out.writeLong(0); // make room for the atom header
            data = new AtomDataOutputStream(new FilterImageOutputStream(out));
        }

        public AtomDataOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("Atom is finished");
            }
            return data;
        }

        /**
         * Returns the offset of this atom to the beginning of the random access file
         * @return
         */
        @SuppressWarnings("unused")
                public long getOffset() {
            return offset;
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                long pointer = out.getStreamPosition();
                out.seek(offset);

                AtomDataOutputStream headerData = new AtomDataOutputStream(new FilterImageOutputStream(out));

                if (size() <= 0xffffffffL) {
                    headerData.writeUInt(8);
                    headerData.writeType("wide");
                    headerData.writeUInt(size());
                    headerData.writeType(type);
                } else {
                    headerData.writeInt(1); // special value for extended size atoms
                    headerData.writeType(type);
                    headerData.writeLong(size());
                }

                out.seek(pointer);
                finished = true;
            }
        }

        @Override
        public long size() {
            long size = 8 + data.size();
            return (size > 0xffffffffL) ? size + 8 : size;
        }
    }

    /**
     * Creates a new output stream with the specified image videoFormat and
     * framerate.
     *
     * @param file the output file
     * @param videoFormat the video videoFormat "JPG" or "PNG".
     * @param framerate the number of videoFrames per section
     * @exception IllegalArgumentException if videoFormat is null or if
     * framerate is <= 0
     */
    public QuickTimeOutputStream(File file, VideoFormat format) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        out = new FileImageOutputStream(file);

        if (format == null) {
            throw new IllegalArgumentException("format must not be null");
        }

        this.videoFormat = format;

        this.videoFrames = new LinkedList<Sample>();
    }

    /**
     * Sets the time scale for this media, that is, the number of time units
     * that pass per second in its time coordinate system.
     * <p>
     * The default value is 600.
     *
     * @param newValue
     */
    public void setTimeScale(int newValue) {
        this.timeScale = newValue;
    }

    /**
     * Returns the time scale of this media.
     *
     * @return time scale
     */
    public int getTimeScale() {
        return timeScale;
    }

    /**
     * Sets the compression quality of the video track.
     * A value of 0 stands for "high compression is important" a value of
     * 1 for "high image quality is important".
     * <p>
     * Changing this value affects frames which are subsequently written
     * to the QuickTimeOutputStream. Frames which have already been written
     * are not changed.
     * <p>
     * This value has no effect on videos encoded with the PNG format.
     * <p>
     * The default value is 0.9.
     *
     * @param newValue
     */
    public void setVideoCompressionQuality(float newValue) {
        this.quality = newValue;
    }

    /**
     * Returns the video compression quality.
     *
     * @return video compression quality
     */
    public float getVideoCompressionQuality() {
        return quality;
    }

    /**
     * Sets the dimension of the video track.
     * <p>
     * You need to explicitly set the dimension, if you add all frames from
     * files or input streams.
     * <p>
     * If you add frames from buffered images, then QuickTimeOutputStream
     * can determine the video dimension from the image width and height.
     *
     * @param width
     * @param height
     */
    public void setVideoDimension(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("widt and height must be greater zero.");
        }
        this.imgWidth = width;
        this.imgHeight = height;
    }

    /**
     * Sets the state of the QuickTimeOutpuStream to started.
     * <p>
     * If the state is changed by this method, the prolog is
     * written.
     */
    private void ensureStarted() throws IOException {
        if (state != States.STARTED) {
            creationTime = new Date();
            writeProlog();
            mdatAtom = new WideLeafAtom("mdat");
            state = States.STARTED;
        }
    }

    /**
     * Writes a frame to the video track.
     * <p>
     * If the dimension of the video track has not been specified yet, it
     * is derived from the first buffered image added to the QuickTimeOutputStream.
     *
     * @param image The frame image.
     * @param duration The duration of the frame in time scale units.
     *
     * @throws IllegalArgumentException if the duration is less than 1, or
     * if the dimension of the frame does not match the dimension of the video
     * track.
     * @throws IOException if writing the image failed.
     */
    public void writeFrame(BufferedImage image, int duration) throws IOException {
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be greater 0");
        }
        ensureOpen();
        ensureStarted();

        // Get the dimensions of the first image
        if (imgWidth == -1) {
            imgWidth = image.getWidth();
            imgHeight = image.getHeight();
        } else {
            // The dimension of the image must match the dimension of the video track
            if (imgWidth != image.getWidth() || imgHeight != image.getHeight()) {
                throw new IllegalArgumentException("Dimensions of image[" + videoFrames.size() +
                        "] (width=" + image.getWidth() + ", height=" + image.getHeight() +
                        ") differs from image[0] (width=" +
                        imgWidth + ", height=" + imgHeight);
            }
        }

        long offset = out.getStreamPosition();

        MemoryCacheImageOutputStream imgOut = new MemoryCacheImageOutputStream(mdatAtom.getOutputStream());
        ImageWriter iw;
        ImageWriteParam iwParam;
        switch (videoFormat) {
            case JPG:
                iw = (ImageWriter) ImageIO.getImageWritersByMIMEType("image/jpeg").next();
                iwParam = iw.getDefaultWriteParam();
                iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwParam.setCompressionQuality(quality);
                break;
            case PNG:
            default:
                iw = (ImageWriter) ImageIO.getImageWritersByMIMEType("image/png").next();
                iwParam = iw.getDefaultWriteParam();
                break;
        }
        iw.setOutput(imgOut);
        IIOImage img = new IIOImage(image, null, null);
        iw.write(null, img, iwParam);
        iw.dispose();
        long length = out.getStreamPosition() - offset;

        videoFrames.add(new Sample(duration, offset, length));
    }

    /**
     * Writes a frame from a file to the video track.
     * <p>
     * This method does not inspect the contents of the file.
     * Its your responsibility to only add JPG files if you have chosen
     * the JPEG video format, and only PNG files if you have chosen the PNG
     * video format.
     * <p>
     * If you add all frames from files or from input streams, then you
     * have to explicitly set the dimension of the video track before you
     * call finish() or close().
     *
     * @param file The file which holds the image data.
     * @param duration The duration of the frame in time scale units.
     *
     * @throws IllegalStateException if the duration is less than 1.
     * @throws IOException if writing the image failed.
     */
    public void writeFrame(File file, int duration) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            writeFrame(in, duration);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Writes a frame to the video track.
     * <p>
     * This method does not inspect the contents of the file.
     * Its your responsibility to only add JPG files if you have chosen
     * the JPEG video format, and only PNG files if you have chosen the PNG
     * video format.
     * <p>
     * If you add all frames from files or from input streams, then you
     * have to explicitly set the dimension of the video track before you
     * call finish() or close().
     *
     * @param in The input stream which holds the image data.
     * @param duration The duration of the frame in time scale units.
     *
     * @throws IllegalArgumentException if the duration is less than 1.
     * @throws IOException if writing the image failed.
     */
    public void writeFrame(InputStream in, int duration) throws IOException {
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be greater 0");
        }
        ensureOpen();
        ensureStarted();

        long offset = out.getStreamPosition();
        OutputStream mdatOut = mdatAtom.getOutputStream();
        byte[] buf = new byte[512];
        int len;
        while ((len = in.read(buf)) != -1) {
            mdatOut.write(buf, 0, len);
        }
        long length = out.getStreamPosition() - offset;
        videoFrames.add(new Sample(duration, offset, length));
    }

    /**
     * Closes the movie file as well as the stream being filtered.
     *
     * @exception IOException if an I/O error has occurred
     */
    public void close() throws IOException {
        if (state == States.STARTED) {
            finish();
        }
        if (state != States.CLOSED) {
            out.close();
            state = States.CLOSED;
        }
    }

    /**
     * Finishes writing the contents of the QuickTime output stream without closing
     * the underlying stream. Use this method when applying multiple filters
     * in succession to the same output stream.
     *
     * @exception IllegalStateException if the dimension of the video track
     * has not been specified or determined yet.
     * @exception IOException if an I/O exception has occurred
     */
    public void finish() throws IOException {
        ensureOpen();
        if (state != States.FINISHED) {
            if (imgWidth == -1 || imgHeight == -1) {
                throw new IllegalStateException("image width and height must be specified");
            }

            mdatAtom.finish();
            writeEpilog();
            state = States.FINISHED;
            imgWidth = imgHeight = -1;
        }
    }

    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (state == States.CLOSED) {
            throw new IOException("Stream closed");
        }
    }

    private void writeProlog() throws IOException {
        /* File type atom
         *
        typedef struct {
        magic brand;
        bcd4 versionYear;
        bcd2 versionMonth;
        bcd2 versionMinor;
        magic[4] compatibleBrands;
        } ftypAtom;
         */
        LeafAtom ftypAtom = new LeafAtom("ftyp");
        AtomDataOutputStream d = ftypAtom.getOutputStream();
        d.writeType("qt  "); // brand
        d.writeBCD4(2005); // versionYear
        d.writeBCD2(3); // versionMonth
        d.writeBCD2(0); // versionMinor
        d.writeType("qt  "); // compatibleBrands
        d.writeInt(0); // compatibleBrands (0 is used to denote no value)
        d.writeInt(0); // compatibleBrands (0 is used to denote no value)
        d.writeInt(0); // compatibleBrands (0 is used to denote no value)
        ftypAtom.finish();
    }

    private void writeEpilog() throws IOException {
        Date modificationTime = new Date();
        int duration = 0;
        for (Sample s : videoFrames) {
            duration += s.duration;
        }

        LeafAtom leaf;

        /* Movie Atom ========= */
        CompositeAtom moovAtom = new CompositeAtom("moov");

        /* Movie Header Atom -------------
         * typedef struct {
        byte version;
        byte[3] flags;
        mactimestamp creationTime;
        mactimestamp modificationTime;
        int timeScale;
        int duration;
        int preferredRate;
        short preferredVolume;
        byte[10] reserved;
        int[9] matrix;
        int previewTime;
        int previewDuration;
        int posterTime;
        int selectionTime;
        int selectionDuration;
        int currentTime;
        int nextTrackId;
        } movieHeaderAtom;
         */
        leaf = new LeafAtom("mvhd");
        moovAtom.add(leaf);
        AtomDataOutputStream d = leaf.getOutputStream();
        d.writeByte(0); // version
        d.writeByte(0); // flags[0]
        d.writeByte(0); // flags[1]
        d.writeByte(0); // flags[2]
        d.writeMacTimestamp(creationTime); // creationTime
        d.writeMacTimestamp(modificationTime); // modificationTime
        d.writeInt(timeScale); // timeScale
        d.writeInt(duration); // duration
        d.writeFixed16D16(1d); // preferredRate
        d.writeShort(256); // preferredVolume
        d.write(new byte[10]); // reserved;
        d.writeFixed16D16(1f); // matrix[0]
        d.writeFixed16D16(0f); // matrix[1]
        d.writeFixed2D30(0f); // matrix[2]
        d.writeFixed16D16(0f); // matrix[3]
        d.writeFixed16D16(1f); // matrix[4]
        d.writeFixed2D30(0); // matrix[5]
        d.writeFixed16D16(0); // matrix[6]
        d.writeFixed16D16(0); // matrix[7]
        d.writeFixed2D30(1f); // matrix[8]
        d.writeInt(0); // previewTime
        d.writeInt(0); // previewDuration
        d.writeInt(0); // posterTime
        d.writeInt(0); // selectionTime
        d.writeInt(0); // selectionDuration
        d.writeInt(0); // currentTime;
        d.writeInt(2); // nextTrackId

        /* Track Atom ======== */
        CompositeAtom trakAtom = new CompositeAtom("trak");
        moovAtom.add(trakAtom);

        /* Track Header Atom -----------
        typedef struct {
        byte version;
        byte flag0;
        byte flag1;
        byte set TrackHeaderFlags flag2;
        mactimestamp creationTime;
        mactimestamp modificationTime;
        int trackId;
        byte[4] reserved;
        int duration;
        byte[8] reserved;
        short layer;
        short alternateGroup;
        short volume;
        byte[2] reserved;
        int[9] matrix;
        int trackWidth;
        int trackHeight;
        } trackHeaderAtom;     */
        leaf = new LeafAtom("tkhd");
        trakAtom.add(leaf);
        d = leaf.getOutputStream();
        d.write(0); // version
        d.write(0); // flag 0
        d.write(0); // flag 1
        d.write(0xf); // flag2
        d.writeMacTimestamp(creationTime); // creationTime
        d.writeMacTimestamp(modificationTime); // modificationTime
        d.writeInt(1); // trackId
        d.writeInt(0); // reserved;
        d.writeInt(duration); // duration
        d.writeLong(0); // reserved
        d.writeShort(0); // layer;
        d.writeShort(0); // alternate group
        d.writeShort(0); // volume
        d.writeShort(0); // reserved
        d.writeFixed16D16(1f); // matrix[0]
        d.writeFixed16D16(0f); // matrix[1]
        d.writeFixed2D30(0f); // matrix[2]
        d.writeFixed16D16(0f); // matrix[3]
        d.writeFixed16D16(1f); // matrix[4]
        d.writeFixed2D30(0); // matrix[5]
        d.writeFixed16D16(0); // matrix[6]
        d.writeFixed16D16(0); // matrix[7]
        d.writeFixed2D30(1f); // matrix[8]
        d.writeFixed16D16(imgWidth); // width
        d.writeFixed16D16(imgHeight); // height

        /* Media Atom ========= */
        CompositeAtom mdiaAtom = new CompositeAtom("mdia");
        trakAtom.add(mdiaAtom);

        /* Media Header atom -------
        typedef struct {
        byte version;
        byte[3] flags;
        mactimestamp creationTime;
        mactimestamp modificationTime;
        int timeScale;
        int duration;
        short language;
        short quality;
        } mediaHeaderAtom;*/
        leaf = new LeafAtom("mdhd");
        mdiaAtom.add(leaf);
        d = leaf.getOutputStream();
        d.write(0); // version
        d.write(0); // flag 0
        d.write(0); // flag 1
        d.write(0); // flag2
        d.writeMacTimestamp(creationTime); // creationTime
        d.writeMacTimestamp(modificationTime); // modificationTime
        d.writeInt(timeScale); // timeScale
        d.writeInt(duration); // duration
        d.writeShort(0); // language;
        d.writeShort(0); // quality

        /** Media Handler Atom ------- */
        leaf = new LeafAtom("hdlr");
        mdiaAtom.add(leaf);
        /*typedef struct {
        byte version;
        byte[3] flags;
        magic componentType;
        magic componentSubtype;
        magic componentManufacturer;
        int componentFlags;
        int componentFlagsMask;
        cstring componentName;
        } handlerReferenceAtom;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        d.write(0); // flag 0
        d.write(0); // flag 1
        d.write(0); // flag2
        d.writeType("mhlr"); // componentType
        d.writeType("vide"); // componentSubtype
        d.writeInt(0); // componentManufacturer
        d.writeInt(0); // componentFlags
        d.writeInt(0); // componentFlagsMask
        d.write(0); // componentName (empty string)

        /* Media Information atom ========= */
        CompositeAtom minfAtom = new CompositeAtom("minf");
        mdiaAtom.add(minfAtom);

        /* Video media information atom -------- */
        leaf = new LeafAtom("vmhd");
        minfAtom.add(leaf);
        /*typedef struct {
        byte version;
        byte flag1;
        byte flag2;
        byte set vmhdFlags flag3;
        short graphicsMode;
        ushort[3] opcolor;
        } videoMediaInformationHeaderAtom;*/
        d = leaf.getOutputStream();
        d.write(0); // version
        d.write(0); // flag 1
        d.write(0); // flag 2
        d.write(0x1); // flag 3
        d.writeShort(0x40); // graphicsMode (0x40 = ditherCopy)
        d.writeUShort(0); // opcolor0
        d.writeUShort(0); // opcolor1
        d.writeUShort(0); // opcolor2

        /* Handle reference atom -------- */
        leaf = new LeafAtom("hdlr");
        minfAtom.add(leaf);
        /*typedef struct {
        byte version;
        byte[3] flags;
        magic componentType;
        magic componentSubtype;
        magic componentManufacturer;
        int componentFlags;
        int componentFlagsMask;
        cstring componentName;
        } handlerReferenceAtom;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        d.write(0); // flag 0
        d.write(0); // flag 1
        d.write(0); // flag2
        d.writeType("dhlr"); // componentType
        d.writeType("alis"); // componentSubtype
        d.writeInt(0); // componentManufacturer
        d.writeInt(0); // componentFlags
        d.writeInt(0); // componentFlagsMask
        d.write(0); // componentName (empty string)

        /* Data information atom ===== */
        CompositeAtom dinfAtom = new CompositeAtom("dinf");
        minfAtom.add(dinfAtom);

        /* Data reference atom ----- */
        leaf = new LeafAtom("dref");
        dinfAtom.add(leaf);
        /*typedef struct {
        ubyte version;
        ubyte[3] flags;
        int numberOfEntries;
        dataReferenceEntry dataReference[numberOfEntries];
        } dataReferenceAtom;

        set {
        dataRefSelfReference=1 // I am not shure if this is the correct value for this flag
        } drefEntryFlags;

        typedef struct {
        int size;
        magic type;
        byte version;
        ubyte flag1;
        ubyte flag2;
        ubyte set drefEntryFlags flag3;
        byte[size - 12] data;
        } dataReferenceEntry;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        d.write(0); // flag 1
        d.write(0); // flag 2
        d.write(0); // flag 3
        d.writeInt(1); // numberOfEntires
        d.writeInt(12); // dataReference.size
        d.writeType("alis"); // dataReference.type
        d.write(0); // dataReference.version
        d.write(0); // dataReference.flag1
        d.write(0); // dataReference.flag2
        d.write(0x1); // dataReference.flag3

        /* Sample Table atom ========= */
        CompositeAtom stblAtom = new CompositeAtom("stbl");
        minfAtom.add(stblAtom);

        /* Sample Description atom ------- */
        leaf = new LeafAtom("stsd");
        stblAtom.add(leaf);
        /*
        typedef struct {
        byte version;
        byte[3] flags;
        int numberOfEntries;
        sampleDescriptionEntry sampleDescriptionTable[numberOfEntries];
        } sampleDescriptionAtom;

        typedef struct {
        int size;
        magic type;
        byte[6] reserved; // six bytes that must be zero
        short dataReferenceIndex; // A 16-bit integer that contains the index of the data reference to use to retrieve data associated with samples that use this sample description. Data references are stored in data reference atoms.
        byte[size - 16] data;
        } sampleDescriptionEntry;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        d.write(0); // flag 1
        d.write(0); // flag 2
        d.write(0); // flag 3
        d.writeInt(1); // number of Entries
        d.writeInt(86); // sampleDescriptionTable[0].size
        switch (videoFormat) {
            case JPG:
                d.writeType("jpeg"); // sampleDescriptionTable[0].type
                d.write(new byte[6]); // sampleDescriptionTable[0].reserved
                d.writeShort(1); // sampleDescriptionTable[0].dataReferenceIndex
                d.writeShort(1); // sampleDescriptionTable.data ?
                d.writeShort(1); // sampleDescriptionTable.data ?
                d.writeType("appl"); // sampleDescriptionTable.manufacturer ?
                d.writeInt(0);  // ?
                d.writeInt(512); // ?
                d.writeUShort(imgWidth); // image width ?
                d.writeUShort(imgHeight); // image height?
                d.writeFixed16D16(72.0); // dpi horizontal?
                d.writeFixed16D16(72.0); // dpi vertical?
                d.writeInt(0); // ?
                d.writeShort(1); // ?
                d.writePString("Photo - JPEG"); // ?
                d.write(0); // padding ?
                d.writeInt(0); // ?
                d.writeInt(0); // ?
                d.writeInt(0); // ?
                d.writeInt(0); // ?
                d.writeInt(0x18); // ?
                d.writeUShort(0xffff); // ?
                break;
            case PNG:
                d.writeType("png "); // sampleDescriptionTable[0].type
                d.write(new byte[6]); // sampleDescriptionTable[0].reserved
                d.writeShort(1); // sampleDescriptionTable[0].dataReferenceIndex
                d.writeShort(1); // sampleDescriptionTable.data ?
                d.writeShort(1); // sampleDescriptionTable.data ?
                d.writeType("java"); // sampleDescriptionTable.manufacturer ?
                d.writeInt(0);  // ?
                d.writeInt(512); // ?
                d.writeUShort(imgWidth); // image width ?
                d.writeUShort(imgHeight); // image height?
                d.writeFixed16D16(72.0); // dpi horizontal?
                d.writeFixed16D16(72.0); // dpi vertical?
                d.writeInt(0); // ?
                d.writeShort(1); // ?
                d.writePString("PNG"); //
                d.writeShort(0); // padding ?
                d.writeInt(0); // ?
                d.writeInt(0); // ?
                d.writeInt(0); // ?
                d.writeInt(0); // ?
                d.writeInt(0); // ?
                d.writeInt(0); // ?
                d.writeInt(0x20); // ?
                d.writeUShort(0xffff); // ?
                break;
        }

        /* Time to Sample atom ---- */
        leaf = new LeafAtom("stts");
        stblAtom.add(leaf);
        /*
        typedef struct {
        byte version;
        byte[3] flags;
        int numberOfEntries;
        timeToSampleTable timeToSampleTable[numberOfEntries];
        } timeToSampleAtom;

        typedef struct {
        int sampleCount;
        int sampleDuration;
        } timeToSampleTable;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        d.write(0); // flag 1
        d.write(0); // flag 2
        d.write(0); // flag 3
        // count runs of video frame durations
        int runCount = 1;
        int prevDuration = videoFrames.get(0).duration;
        for (Sample s : videoFrames) {
            if (s.duration != prevDuration) {
                runCount++;
                prevDuration = s.duration;
            }
        }
        d.writeInt(runCount); // numberOfEntries
        int runLength = 0;
        prevDuration = videoFrames.get(0).duration;
        for (Sample s : videoFrames) {
            if (s.duration != prevDuration) {
                if (runLength > 0) {
                    d.writeInt(runLength); // timeToSampleTable[0].sampleCount
                    d.writeInt(prevDuration); // timeToSampleTable[0].sampleDuration
                }
                prevDuration = s.duration;
                runLength = 1;
            } else {
                runLength++;
            }
        }
        if (runLength > 0) {
            d.writeInt(runLength); // timeToSampleTable[0].sampleCount
            d.writeInt(prevDuration); // timeToSampleTable[0].sampleDuration
        }
        /* sample to chunk atom -------- */
        leaf = new LeafAtom("stsc");
        stblAtom.add(leaf);
        /*
        typedef struct {
        byte version;
        byte[3] flags;
        int numberOfEntries;
        sampleToChunkTable sampleToChunkTable[numberOfEntries];
        } sampleToChunkAtom;

        typedef struct {
        int firstChunk;
        int samplesPerChunk;
        int sampleDescription;
        } sampleToChunkTable;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        d.write(0); // flag 1
        d.write(0); // flag 2
        d.write(0); // flag 3
        d.writeInt(1); // number of entries
        d.writeInt(1); // first chunk
        d.writeInt(1); // samples per chunk
        d.writeInt(1); // sample description

        /* sample size atom -------- */
        leaf = new LeafAtom("stsz");
        stblAtom.add(leaf);
        /*
        typedef struct {
        byte version;
        byte[3] flags;
        int sampleSize;
        int numberOfEntries;
        sampleSizeTable sampleSizeTable[numberOfEntries];
        } sampleSizeAtom;

        typedef struct {
        int size;
        } sampleSizeTable;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        d.write(0); // flag 1
        d.write(0); // flag 2
        d.write(0); // flag 3
        d.writeUInt(0); // sample size
        d.writeUInt(videoFrames.size()); // number of entries
        for (Sample s : videoFrames) {
            d.writeUInt(s.length); // sample size
        }
        //
        if (videoFrames.getLast().offset <= 0xffffffffL) {
            /* chunk offset atom -------- */
            leaf = new LeafAtom("stco");
            stblAtom.add(leaf);
            /*
            typedef struct {
            byte version;
            byte[3] flags;
            int numberOfEntries;
            chunkOffsetTable chunkOffsetTable[numberOfEntries];
            } chunkOffsetAtom;

            typedef struct {
            int offset;
            } chunkOffsetTable;
             */
            d = leaf.getOutputStream();
            d.write(0); // version
            d.write(0); // flag 1
            d.write(0); // flag 2
            d.write(0); // flag 3
            d.writeUInt(videoFrames.size()); // number of entries
            for (Sample s : videoFrames) {
                d.writeUInt(s.offset); // offset
            }
        } else {
            /* long chunk offset atom -------- */
            leaf = new LeafAtom("co64");
            stblAtom.add(leaf);
            /*
            typedef struct {
            byte version;
            byte[3] flags;
            int numberOfEntries;
            chunkOffsetTable chunkOffset64Table[numberOfEntries];
            } chunkOffset64Atom;

            typedef struct {
            long offset;
            } chunkOffset64Table;
             */
            d = leaf.getOutputStream();
            d.write(0); // version
            d.write(0); // flag 1
            d.write(0); // flag 2
            d.write(0); // flag 3
            d.writeUInt(videoFrames.size()); // number of entries
            for (Sample s : videoFrames) {
                d.writeLong(s.offset); // offset
            }
        }
        //
        moovAtom.finish();
    }
}
