/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.3-RC1</a>, using an
 * XML Schema.
 * $Id$
 */

package org.exolab.castor.builder.appInfo.generated.complexContent;

/**
 * 
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Insurant extends org.exolab.castor.builder.appInfo.generated.complexContent.Person 
implements java.io.Serializable
{

    /**
     * Field _policyNumber.
     */
    private long _policyNumber;

    /**
     * keeps track of state for field: _policyNumber
     */
    private boolean _has_policyNumber;

    public Insurant() {
        super();
    }

    /**
     */
    public void deletePolicyNumber() {
        this._has_policyNumber= false;
    }

    /**
     * Returns the value of field 'policyNumber'.
     * 
     * @return the value of field 'PolicyNumber'.
     */
    public long getPolicyNumber() {
        return this._policyNumber;
    }

    /**
     * Method hasPolicyNumber.
     * 
     * @return true if at least one PolicyNumber has been added
     */
    public boolean hasPolicyNumber() {
        return this._has_policyNumber;
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
     * Sets the value of field 'policyNumber'.
     * 
     * @param policyNumber the value of field 'policyNumber'.
     */
    public void setPolicyNumber(final long policyNumber) {
        this._policyNumber = policyNumber;
        this._has_policyNumber = true;
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
     * org.exolab.castor.builder.appInfo.generated.complexContent.Insurant
     */
    public static org.exolab.castor.builder.appInfo.generated.complexContent.Insurant unmarshal(final java.io.Reader reader) throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.exolab.castor.builder.appInfo.generated.complexContent.Insurant) org.exolab.castor.xml.Unmarshaller.unmarshal(org.exolab.castor.builder.appInfo.generated.complexContent.Insurant.class, reader);
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
