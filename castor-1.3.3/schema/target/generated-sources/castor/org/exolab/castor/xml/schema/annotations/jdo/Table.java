/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.castor.xml.schema.annotations.jdo;

/**
 * Element 'table' is used to specify the table where the
 *  Object will be saved.
 *  
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Table implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Attribute 'name' is used to specify the name of
     *  the table.
     *  
     */
    private java.lang.String _name;

    /**
     * Field _accessMode.
     */
    private types.TableAccessModeType _accessMode = types.TableAccessModeType.fromValue("shared");

    /**
     * Attribute 'detachable' is used to indicate that the 
     *  domain entity generated will support Castor JDO
     *  long transactions out of the box. 
     *  
     */
    private boolean _detachable = false;

    /**
     * keeps track of state for field: _detachable
     */
    private boolean _has_detachable;

    /**
     * Field _primaryKey.
     */
    private org.exolab.castor.xml.schema.annotations.jdo.PrimaryKey _primaryKey;


      //----------------/
     //- Constructors -/
    //----------------/

    public Table() {
        super();
        setAccessMode(types.TableAccessModeType.fromValue("shared"));
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteDetachable(
    ) {
        this._has_detachable= false;
    }

    /**
     * Returns the value of field 'accessMode'.
     * 
     * @return the value of field 'AccessMode'.
     */
    public types.TableAccessModeType getAccessMode(
    ) {
        return this._accessMode;
    }

    /**
     * Returns the value of field 'detachable'. The field
     * 'detachable' has the following description: Attribute
     * 'detachable' is used to indicate that the 
     *  domain entity generated will support Castor JDO
     *  long transactions out of the box. 
     *  
     * 
     * @return the value of field 'Detachable'.
     */
    public boolean getDetachable(
    ) {
        return this._detachable;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the
     * following description: Attribute 'name' is used to specify
     * the name of
     *  the table.
     *  
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name;
    }

    /**
     * Returns the value of field 'primaryKey'.
     * 
     * @return the value of field 'PrimaryKey'.
     */
    public org.exolab.castor.xml.schema.annotations.jdo.PrimaryKey getPrimaryKey(
    ) {
        return this._primaryKey;
    }

    /**
     * Method hasDetachable.
     * 
     * @return true if at least one Detachable has been added
     */
    public boolean hasDetachable(
    ) {
        return this._has_detachable;
    }

    /**
     * Returns the value of field 'detachable'. The field
     * 'detachable' has the following description: Attribute
     * 'detachable' is used to indicate that the 
     *  domain entity generated will support Castor JDO
     *  long transactions out of the box. 
     *  
     * 
     * @return the value of field 'Detachable'.
     */
    public boolean isDetachable(
    ) {
        return this._detachable;
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
     * Sets the value of field 'accessMode'.
     * 
     * @param accessMode the value of field 'accessMode'.
     */
    public void setAccessMode(
            final types.TableAccessModeType accessMode) {
        this._accessMode = accessMode;
    }

    /**
     * Sets the value of field 'detachable'. The field 'detachable'
     * has the following description: Attribute 'detachable' is
     * used to indicate that the 
     *  domain entity generated will support Castor JDO
     *  long transactions out of the box. 
     *  
     * 
     * @param detachable the value of field 'detachable'.
     */
    public void setDetachable(
            final boolean detachable) {
        this._detachable = detachable;
        this._has_detachable = true;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: Attribute 'name' is used to specify
     * the name of
     *  the table.
     *  
     * 
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'primaryKey'.
     * 
     * @param primaryKey the value of field 'primaryKey'.
     */
    public void setPrimaryKey(
            final org.exolab.castor.xml.schema.annotations.jdo.PrimaryKey primaryKey) {
        this._primaryKey = primaryKey;
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
     * org.exolab.castor.xml.schema.annotations.jdo.Table
     */
    public static org.exolab.castor.xml.schema.annotations.jdo.Table unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.exolab.castor.xml.schema.annotations.jdo.Table) org.exolab.castor.xml.Unmarshaller.unmarshal(org.exolab.castor.xml.schema.annotations.jdo.Table.class, reader);
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
