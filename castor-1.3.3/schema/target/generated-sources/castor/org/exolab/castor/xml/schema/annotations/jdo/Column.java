/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.castor.xml.schema.annotations.jdo;

/**
 * Element 'column' is used to specify the column where the
 *  property of an object will be saved.
 *  
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Column extends org.exolab.castor.xml.schema.annotations.jdo.ReadonlyDirtyType 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Attribute 'name' is used to specify the
     *  name of the column.
     *  
     */
    private java.lang.String _name;

    /**
     * Attribute 'type' is used to specify the
     *  JDO-type of the column.
     *  
     */
    private java.lang.String _type;

    /**
     * Specifies if this field accepts NULL
     *  values or not.
     *  
     */
    private boolean _acceptNull = true;

    /**
     * keeps track of state for field: _acceptNull
     */
    private boolean _has_acceptNull;


      //----------------/
     //- Constructors -/
    //----------------/

    public Column() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteAcceptNull(
    ) {
        this._has_acceptNull= false;
    }

    /**
     * Returns the value of field 'acceptNull'. The field
     * 'acceptNull' has the following description: Specifies if
     * this field accepts NULL
     *  values or not.
     *  
     * 
     * @return the value of field 'AcceptNull'.
     */
    public boolean getAcceptNull(
    ) {
        return this._acceptNull;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the
     * following description: Attribute 'name' is used to specify
     * the
     *  name of the column.
     *  
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name;
    }

    /**
     * Returns the value of field 'type'. The field 'type' has the
     * following description: Attribute 'type' is used to specify
     * the
     *  JDO-type of the column.
     *  
     * 
     * @return the value of field 'Type'.
     */
    public java.lang.String getType(
    ) {
        return this._type;
    }

    /**
     * Method hasAcceptNull.
     * 
     * @return true if at least one AcceptNull has been added
     */
    public boolean hasAcceptNull(
    ) {
        return this._has_acceptNull;
    }

    /**
     * Returns the value of field 'acceptNull'. The field
     * 'acceptNull' has the following description: Specifies if
     * this field accepts NULL
     *  values or not.
     *  
     * 
     * @return the value of field 'AcceptNull'.
     */
    public boolean isAcceptNull(
    ) {
        return this._acceptNull;
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
     * Sets the value of field 'acceptNull'. The field 'acceptNull'
     * has the following description: Specifies if this field
     * accepts NULL
     *  values or not.
     *  
     * 
     * @param acceptNull the value of field 'acceptNull'.
     */
    public void setAcceptNull(
            final boolean acceptNull) {
        this._acceptNull = acceptNull;
        this._has_acceptNull = true;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: Attribute 'name' is used to specify
     * the
     *  name of the column.
     *  
     * 
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'type'. The field 'type' has the
     * following description: Attribute 'type' is used to specify
     * the
     *  JDO-type of the column.
     *  
     * 
     * @param type the value of field 'type'.
     */
    public void setType(
            final java.lang.String type) {
        this._type = type;
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
     * org.exolab.castor.xml.schema.annotations.jdo.Column
     */
    public static org.exolab.castor.xml.schema.annotations.jdo.Column unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.exolab.castor.xml.schema.annotations.jdo.Column) org.exolab.castor.xml.Unmarshaller.unmarshal(org.exolab.castor.xml.schema.annotations.jdo.Column.class, reader);
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
