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
public class Person implements java.io.Serializable {

    /**
     * Field _ssn.
     */
    private long _ssn;

    /**
     * keeps track of state for field: _ssn
     */
    private boolean _has_ssn;

    /**
     * Field _firstName.
     */
    private java.lang.String _firstName;

    /**
     * Field _lastName.
     */
    private java.lang.String _lastName;

    /**
     * Field _birthdate.
     */
    private org.exolab.castor.types.Date _birthdate;

    public Person() {
        super();
    }

    /**
     */
    public void deleteSsn() {
        this._has_ssn= false;
    }

    /**
     * Returns the value of field 'birthdate'.
     * 
     * @return the value of field 'Birthdate'.
     */
    public org.exolab.castor.types.Date getBirthdate() {
        return this._birthdate;
    }

    /**
     * Returns the value of field 'firstName'.
     * 
     * @return the value of field 'FirstName'.
     */
    public java.lang.String getFirstName() {
        return this._firstName;
    }

    /**
     * Returns the value of field 'lastName'.
     * 
     * @return the value of field 'LastName'.
     */
    public java.lang.String getLastName() {
        return this._lastName;
    }

    /**
     * Returns the value of field 'ssn'.
     * 
     * @return the value of field 'Ssn'.
     */
    public long getSsn() {
        return this._ssn;
    }

    /**
     * Method hasSsn.
     * 
     * @return true if at least one Ssn has been added
     */
    public boolean hasSsn() {
        return this._has_ssn;
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
     * Sets the value of field 'birthdate'.
     * 
     * @param birthdate the value of field 'birthdate'.
     */
    public void setBirthdate(final org.exolab.castor.types.Date birthdate) {
        this._birthdate = birthdate;
    }

    /**
     * Sets the value of field 'firstName'.
     * 
     * @param firstName the value of field 'firstName'.
     */
    public void setFirstName(final java.lang.String firstName) {
        this._firstName = firstName;
    }

    /**
     * Sets the value of field 'lastName'.
     * 
     * @param lastName the value of field 'lastName'.
     */
    public void setLastName(final java.lang.String lastName) {
        this._lastName = lastName;
    }

    /**
     * Sets the value of field 'ssn'.
     * 
     * @param ssn the value of field 'ssn'.
     */
    public void setSsn(final long ssn) {
        this._ssn = ssn;
        this._has_ssn = true;
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
     * org.exolab.castor.builder.appInfo.generated.complexContent.Person
     */
    public static org.exolab.castor.builder.appInfo.generated.complexContent.Person unmarshal(final java.io.Reader reader) throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.exolab.castor.builder.appInfo.generated.complexContent.Person) org.exolab.castor.xml.Unmarshaller.unmarshal(org.exolab.castor.builder.appInfo.generated.complexContent.Person.class, reader);
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
