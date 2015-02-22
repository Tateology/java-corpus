/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.3-RC1</a>, using an
 * XML Schema.
 * $Id$
 */

package org.exolab.castor.builder.appInfo.generated.simple;

/**
 * Docu of Father Class.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Father implements java.io.Serializable {

    /**
     * The Social Security Number.
     *  
     */
    private long _ssnr;

    /**
     * keeps track of state for field: _ssnr
     */
    private boolean _has_ssnr;

    /**
     * The first name of the Father.
     *  
     */
    private java.lang.String _firstName;

    /**
     * The Last Name of the Father.
     *  
     */
    private java.lang.String _lastName;

    public Father() {
        super();
    }

    /**
     */
    public void deleteSsnr() {
        this._has_ssnr= false;
    }

    /**
     * Returns the value of field 'firstName'. The field
     * 'firstName' has the following description: The first name of
     * the Father.
     *  
     * 
     * @return the value of field 'FirstName'.
     */
    public java.lang.String getFirstName() {
        return this._firstName;
    }

    /**
     * Returns the value of field 'lastName'. The field 'lastName'
     * has the following description: The Last Name of the Father.
     *  
     * 
     * @return the value of field 'LastName'.
     */
    public java.lang.String getLastName() {
        return this._lastName;
    }

    /**
     * Returns the value of field 'ssnr'. The field 'ssnr' has the
     * following description: The Social Security Number.
     *  
     * 
     * @return the value of field 'Ssnr'.
     */
    public long getSsnr() {
        return this._ssnr;
    }

    /**
     * Method hasSsnr.
     * 
     * @return true if at least one Ssnr has been added
     */
    public boolean hasSsnr() {
        return this._has_ssnr;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
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
    public void marshal(final java.io.Writer out) throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
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
    public void marshal(final org.xml.sax.ContentHandler handler) throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Marshaller.marshal(this, handler);
    }

    /**
     * Sets the value of field 'firstName'. The field 'firstName'
     * has the following description: The first name of the Father.
     *  
     * 
     * @param firstName the value of field 'firstName'.
     */
    public void setFirstName(final java.lang.String firstName) {
        this._firstName = firstName;
    }

    /**
     * Sets the value of field 'lastName'. The field 'lastName' has
     * the following description: The Last Name of the Father.
     *  
     * 
     * @param lastName the value of field 'lastName'.
     */
    public void setLastName(final java.lang.String lastName) {
        this._lastName = lastName;
    }

    /**
     * Sets the value of field 'ssnr'. The field 'ssnr' has the
     * following description: The Social Security Number.
     *  
     * 
     * @param ssnr the value of field 'ssnr'.
     */
    public void setSsnr(final long ssnr) {
        this._ssnr = ssnr;
        this._has_ssnr = true;
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
     * org.exolab.castor.builder.appInfo.generated.simple.Father
     */
    public static org.exolab.castor.builder.appInfo.generated.simple.Father unmarshal(final java.io.Reader reader) throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.exolab.castor.builder.appInfo.generated.simple.Father) org.exolab.castor.xml.Unmarshaller.unmarshal(org.exolab.castor.builder.appInfo.generated.simple.Father.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate() throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
