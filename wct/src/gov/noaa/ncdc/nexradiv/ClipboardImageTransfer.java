package gov.noaa.ncdc.nexradiv;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;

/**
 * Originally from Subversion repository for 'capturedit' software on Sourceforge:
 * http://capturedit.svn.sourceforge.net/viewvc/*checkout* /capturedit/trunk/src/com/glubco/capturedit/ImageWriters/ClipBoardWriter.java?revision=3
 */
public class ClipboardImageTransfer {

    public void copyImageToClipboard(Image image) {
        // Work around a Sun bug that causes a hang in "sun.awt.image.ImageRepresentation.reconstruct".
        new javax.swing.ImageIcon(image); // Force load.
        BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        newImage.createGraphics().drawImage(image, 0, 0, null);
        image = newImage;
        ImageSelection imageSelection = new ImageSelection(image);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.getSystemClipboard().setContents(imageSelection, null);
    }

    public static class ImageSelection implements Transferable {
        private Image image;

        public ImageSelection(Image image) {
            this.image = image;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!flavor.equals(DataFlavor.imageFlavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DataFlavor.imageFlavor);
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{
                    DataFlavor.imageFlavor
            };
        }
    }
}

