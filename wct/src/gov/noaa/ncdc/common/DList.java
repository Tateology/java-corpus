package gov.noaa.ncdc.common;

// From: http://forums.crm.saeven.net/blog.php?b=1&goto=next
// with listeners, event handling, etc... added by Steve Ansari
//package com.saeven.widgets;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.jdesktop.swingx.JXList;


public class DList extends JXList {
//public class DList extends JList {
    private static final long serialVersionUID = 1L;
    
    public  static DataFlavor   DList_Flavor     = new DataFlavor( DListData.class, "DListData" );
    private static DataFlavor[] supportedFlavors = { DList_Flavor };
    
    private Vector<DragListener> listeners = new Vector<DragListener>();
    
    public DList(){
        super();
        setTransferHandler( new ReorderHandler() );
        setDragEnabled( true );
        setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    }
    
    public DList( DefaultListModel m ){
        this();
        setModel( m );            
    }
    
    public void addDragListener(DragListener l) {
    	listeners.add(l);
    }
    public void removeDragListener(DragListener l) {
    	listeners.remove(l);
    }
    
    public void dropComplete(){
    	for (DragListener d : listeners) {
    		d.dropComplete();
    	}
    }
    
    private class ReorderHandler extends TransferHandler {
        private static final long serialVersionUID = 1L;
        
        @Override
        public boolean importData(TransferSupport support) {
            
            // this is the index of the element onto which the dragged element, is dropped
            final int dropIndex = DList.this.locationToIndex( getDropLocation().getDropPoint() );
        
            try{
                Object [] draggedData        = ((DListData)support.getTransferable().getTransferData( DList_Flavor )).data;
                final DList dragList        = ((DListData)support.getTransferable().getTransferData( DList_Flavor )).parent;
                
//                System.out.println(dragList.getModel().getClass());
                
                DefaultListModel dragModel    = (DefaultListModel)dragList.getModel();
                DefaultListModel dropModel    = (DefaultListModel)DList.this.getModel();
                
                final Object leadItem     = dropIndex >= 0 ? dropModel.elementAt( dropIndex ) : null;
                final int dataLength     = draggedData.length;            

                // make sure that the lead item, is not in the dragged data
                if( leadItem != null )
                    for( int i = 0 ; i < dataLength ; i++ )
                        if( draggedData[i].equals( leadItem ) )
                            return false;                
                
                int dragLeadIndex        = -1;
                final boolean localDrop    = dropModel.contains( draggedData[0] );
                
                if( localDrop ) {
                    dragLeadIndex    = dropModel.indexOf( draggedData[0] );
                }
                
                for( int i = 0 ; i < dataLength ; i++ ) {
                    dragModel.removeElement( draggedData[i] );
                }
                        
                if( localDrop ){
                    final int adjustedLeadIndex = dropModel.indexOf( leadItem );
                    final int insertionAdjustment = dragLeadIndex <= adjustedLeadIndex ? 1 : 0;
                        
                    System.out.println(adjustedLeadIndex+","+insertionAdjustment);
                    
                    final int [] indices = new int[dataLength];
                    for( int i = 0 ; i < dataLength ; i++ ){
                        dropModel.insertElementAt( draggedData[i], adjustedLeadIndex + insertionAdjustment + i );
                        indices[i] = adjustedLeadIndex + insertionAdjustment + i;
                    }
                    
                    SwingUtilities.invokeLater( new Runnable(){
//                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            DList.this.clearSelection();        
                            DList.this.setSelectedIndices( indices );
                            dropComplete();
                        }
                    });
                }
                else{
                    final int [] indices = new int[dataLength];
                    for( int i = 0 ; i < dataLength ; i++ ){
                        dropModel.insertElementAt( draggedData[i], dropIndex + 1 );
                        indices[i] = dropIndex + 1 + i;
                    }
                    
                    SwingUtilities.invokeLater( new Runnable(){
//                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            DList.this.clearSelection();        
                            DList.this.setSelectedIndices( indices );
                            dragList.clearSelection();
                            dropComplete();
                        }
                    });
                }
            }
            catch( Exception x ){
                x.printStackTrace();
            }            
            return false;
        }    
        
        public int getSourceActions( JComponent c ){
            return TransferHandler.MOVE;
        }
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            return new DListData( DList.this, DList.this.getSelectedValues() );
        }
        
        @Override
        public boolean canImport(TransferSupport support) {
            if( !support.isDrop() || !support.isDataFlavorSupported( DList_Flavor ) )
                return false;
            
                        
            return true;
        }
        
        @Override
        public Icon getVisualRepresentation(Transferable t) {
            // TODO Auto-generated method stub
            return super.getVisualRepresentation(t);
        }        
    }
    
    private class DListData implements Transferable {
        
        private Object[]     data;
        private DList        parent;
        
        protected DListData( DList p, Object[] d ){
            parent    = p;
            data     = d;
        }
        
//        @Override
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if ( flavor.equals( DList_Flavor ) )
                return DListData.this;
            else
                return null;
        }
        
//        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return supportedFlavors;
        }
        
//        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            // TODO Auto-generated method stub
            return true;
        }
    }
    
    public class DragEvent {
    	private boolean localDrag = true;
    	private int sourceIndex;
    	private int insertionIndex;

    	public DragEvent(boolean localDrag, int sourceIndex, int insertionIndex) {
    		this.localDrag = localDrag;
    		this.sourceIndex = sourceIndex;
    		this.insertionIndex = insertionIndex;
    	}
    	
		public int getInsertionIndex() {
			return insertionIndex;
		}

		public void setInsertionIndex(int insertionIndex) {
			this.insertionIndex = insertionIndex;
		}

		public boolean isLocalDrag() {
			return localDrag;
		}

		public void setLocalDrag(boolean localDrag) {
			this.localDrag = localDrag;
		}

		public int getSourceIndex() {
			return sourceIndex;
		}

		public void setSourceIndex(int sourceIndex) {
			this.sourceIndex = sourceIndex;
		}
    	
    }
}