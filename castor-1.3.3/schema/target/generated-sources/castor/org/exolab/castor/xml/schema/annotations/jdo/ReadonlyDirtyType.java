/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.castor.xml.schema.annotations.jdo;

/**
 * Class ReadonlyDirtyType.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class ReadonlyDirtyType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Attribute 'read-only' is used to set off changing
     *  the column. If true, no update can be performed.
     *  
     */
    private boolean _readOnly = false;

    /**
     * keeps track of state for field: _readOnly
     */
    private boolean _has_readOnly;

    /**
     * If the value is 'true', the field will not be
     *  checked against the database for modification.
     *  
     */
    private boolean _dirty = false;

    /**
     * keeps track of state for field: _dirty
     */
    private boolean _has_dirty;


      //----------------/
     //- Constructors -/
    //----------------/

    public ReadonlyDirtyType() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteDirty(
    ) {
        this._has_dirty= false;
    }

    /**
     */
    public void deleteReadOnly(
    ) {
        this._has_readOnly= false;
    }

    /**
     * Returns the value of field 'dirty'. The field 'dirty' has
     * the following description: If the value is 'true', the field
     * will not be
     *  checked against the database for modification.
     *  
     * 
     * @return the value of field 'Dirty'.
     */
    public boolean getDirty(
    ) {
        return this._dirty;
    }

    /**
     * Returns the value of field 'readOnly'. The field 'readOnly'
     * has the following description: Attribute 'read-only' is used
     * to set off changing
     *  the column. If true, no update can be performed.
     *  
     * 
     * @return the value of field 'ReadOnly'.
     */
    public boolean getReadOnly(
    ) {
        return this._readOnly;
    }

    /**
     * Method hasDirty.
     * 
     * @return true if at least one Dirty has been added
     */
    public boolean hasDirty(
    ) {
        return this._has_dirty;
    }

    /**
     * Method hasReadOnly.
     * 
     * @return true if at least one ReadOnly has been added
     */
    public boolean hasReadOnly(
    ) {
        return this._has_readOnly;
    }

    /**
     * Returns the value of field 'dirty'. The field 'dirty' has
     * the following description: If the value is 'true', the field
     * will not be
     *  checked against the database for modification.
     *  
     * 
     * @return the value of field 'Dirty'.
     */
    public boolean isDirty(
    ) {
        return this._dirty;
    }

    /**
     * Returns the value of field 'readOnly'. The field 'readOnly'
     * has the following description: Attribute 'read-only' is used
     * to set off changing
     *  the column. If true, no update can be performed.
     *  
     * 
     * @return the value of field 'ReadOnly'.
     */
    public boolean isReadOnly(
    ) {
        return this._readOnly;
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
     * Sets the value of field 'dirty'. The field 'dirty' has the
     * following description: If the value is 'true', the field
     * will not be
     *  checked against the database for modification.
     *  
     * 
     * @param dirty the value of field 'dirty'.
     */
    public void setDirty(
            final boolean dirty) {
        this._dirty = dirty;
        this._has_dirty = true;
    }

    /**
     * Sets the value of field 'readOnly'. The field 'readOnly' has
     * the following description: Attribute 'read-only' is used to
     * set off changing
     *  the column. If true, no update can be performed.
     *  
     * 
     * @param readOnly the value of field 'readOnly'.
     */
    public void setReadOnly(
            final boolean readOnly) {
        this._readOnly = readOnly;
        this._has_readOnly = true;
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
     * org.exolab.castor.xml.schema.annotations.jdo.ReadonlyDirtyTyp
     */
    public static org.exolab.castor.xml.schema.annotations.jdo.ReadonlyDirtyType unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.exolab.castor.xml.schema.annotations.jdo.ReadonlyDirtyType) org.exolab.castor.xml.Unmarshaller.unmarshal(org.exolab.castor.xml.schema.annotations.jdo.ReadonlyDirtyType.class, reader);
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
