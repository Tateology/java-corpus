/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.castor.xml.schema.annotations.jdo;

/**
 * Class PkType.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class PkType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _keyList.
     */
    private java.util.List<java.lang.String> _keyList;


      //----------------/
     //- Constructors -/
    //----------------/

    public PkType() {
        super();
        this._keyList = new java.util.ArrayList<java.lang.String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vKey
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addKey(
            final java.lang.String vKey)
    throws java.lang.IndexOutOfBoundsException {
        this._keyList.add(vKey);
    }

    /**
     * 
     * 
     * @param index
     * @param vKey
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addKey(
            final int index,
            final java.lang.String vKey)
    throws java.lang.IndexOutOfBoundsException {
        this._keyList.add(index, vKey);
    }

    /**
     * Method enumerateKey.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends java.lang.String> enumerateKey(
    ) {
        return java.util.Collections.enumeration(this._keyList);
    }

    /**
     * Method getKey.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getKey(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._keyList.size()) {
            throw new IndexOutOfBoundsException("getKey: Index value '" + index + "' not in range [0.." + (this._keyList.size() - 1) + "]");
        }

        return (java.lang.String) _keyList.get(index);
    }

    /**
     * Method getKey.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getKey(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._keyList.toArray(array);
    }

    /**
     * Method getKeyCount.
     * 
     * @return the size of this collection
     */
    public int getKeyCount(
    ) {
        return this._keyList.size();
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
     * Method iterateKey.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends java.lang.String> iterateKey(
    ) {
        return this._keyList.iterator();
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
     */
    public void removeAllKey(
    ) {
        this._keyList.clear();
    }

    /**
     * Method removeKey.
     * 
     * @param vKey
     * @return true if the object was removed from the collection.
     */
    public boolean removeKey(
            final java.lang.String vKey) {
        boolean removed = _keyList.remove(vKey);
        return removed;
    }

    /**
     * Method removeKeyAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeKeyAt(
            final int index) {
        java.lang.Object obj = this._keyList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vKey
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setKey(
            final int index,
            final java.lang.String vKey)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._keyList.size()) {
            throw new IndexOutOfBoundsException("setKey: Index value '" + index + "' not in range [0.." + (this._keyList.size() - 1) + "]");
        }

        this._keyList.set(index, vKey);
    }

    /**
     * 
     * 
     * @param vKeyArray
     */
    public void setKey(
            final java.lang.String[] vKeyArray) {
        //-- copy array
        _keyList.clear();

        for (int i = 0; i < vKeyArray.length; i++) {
                this._keyList.add(vKeyArray[i]);
        }
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
     * org.exolab.castor.xml.schema.annotations.jdo.PkType
     */
    public static org.exolab.castor.xml.schema.annotations.jdo.PkType unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.exolab.castor.xml.schema.annotations.jdo.PkType) org.exolab.castor.xml.Unmarshaller.unmarshal(org.exolab.castor.xml.schema.annotations.jdo.PkType.class, reader);
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
