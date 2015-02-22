/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.castor.jdo.conf;

/**
 * Class TransactionDemarcation.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class TransactionDemarcation implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _mode.
     */
    private java.lang.String _mode;

    /**
     * Field _transactionManager.
     */
    private org.castor.jdo.conf.TransactionManager _transactionManager;


      //----------------/
     //- Constructors -/
    //----------------/

    public TransactionDemarcation() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'mode'.
     * 
     * @return the value of field 'Mode'.
     */
    public java.lang.String getMode(
    ) {
        return this._mode;
    }

    /**
     * Returns the value of field 'transactionManager'.
     * 
     * @return the value of field 'TransactionManager'.
     */
    public org.castor.jdo.conf.TransactionManager getTransactionManager(
    ) {
        return this._transactionManager;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid(
    ) {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(
            final java.io.Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Marshaller.marshal(this, handler);
    }

    /**
     * Sets the value of field 'mode'.
     * 
     * @param mode the value of field 'mode'.
     */
    public void setMode(
            final java.lang.String mode) {
        this._mode = mode;
    }

    /**
     * Sets the value of field 'transactionManager'.
     * 
     * @param transactionManager the value of field
     * 'transactionManager'.
     */
    public void setTransactionManager(
            final org.castor.jdo.conf.TransactionManager transactionManager) {
        this._transactionManager = transactionManager;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.castor.jdo.conf.TransactionDemarcation
     */
    public static org.castor.jdo.conf.TransactionDemarcation unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.castor.jdo.conf.TransactionDemarcation) org.exolab.castor.xml.Unmarshaller.unmarshal(org.castor.jdo.conf.TransactionDemarcation.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
