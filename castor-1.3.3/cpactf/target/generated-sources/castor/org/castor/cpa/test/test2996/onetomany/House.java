/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.castor.cpa.test.test2996.onetomany;

/**
 * 
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class House implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id.
     */
    private long _id;

    /**
     * keeps track of state for field: _id
     */
    private boolean _has_id;

    /**
     * Field _flatsList.
     */
    private java.util.List<org.castor.cpa.test.test2996.onetomany.Flat> _flatsList;


      //----------------/
     //- Constructors -/
    //----------------/

    public House() {
        super();
        this._flatsList = new java.util.ArrayList<org.castor.cpa.test.test2996.onetomany.Flat>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vFlats
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addFlats(
            final org.castor.cpa.test.test2996.onetomany.Flat vFlats)
    throws java.lang.IndexOutOfBoundsException {
        this._flatsList.add(vFlats);
    }

    /**
     * 
     * 
     * @param index
     * @param vFlats
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addFlats(
            final int index,
            final org.castor.cpa.test.test2996.onetomany.Flat vFlats)
    throws java.lang.IndexOutOfBoundsException {
        this._flatsList.add(index, vFlats);
    }

    /**
     */
    public void deleteId(
    ) {
        this._has_id= false;
    }

    /**
     * Method enumerateFlats.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.castor.cpa.test.test2996.onetomany.Flat> enumerateFlats(
    ) {
        return java.util.Collections.enumeration(this._flatsList);
    }

    /**
     * Method getFlats.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.castor.cpa.test.test2996.onetomany.Flat at the given inde
     */
    public org.castor.cpa.test.test2996.onetomany.Flat getFlats(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._flatsList.size()) {
            throw new IndexOutOfBoundsException("getFlats: Index value '" + index + "' not in range [0.." + (this._flatsList.size() - 1) + "]");
        }

        return (org.castor.cpa.test.test2996.onetomany.Flat) _flatsList.get(index);
    }

    /**
     * Method getFlats.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.castor.cpa.test.test2996.onetomany.Flat[] getFlats(
    ) {
        org.castor.cpa.test.test2996.onetomany.Flat[] array = new org.castor.cpa.test.test2996.onetomany.Flat[0];
        return (org.castor.cpa.test.test2996.onetomany.Flat[]) this._flatsList.toArray(array);
    }

    /**
     * Method getFlatsCount.
     * 
     * @return the size of this collection
     */
    public int getFlatsCount(
    ) {
        return this._flatsList.size();
    }

    /**
     * Returns the value of field 'id'.
     * 
     * @return the value of field 'Id'.
     */
    public long getId(
    ) {
        return this._id;
    }

    /**
     * Method hasId.
     * 
     * @return true if at least one Id has been added
     */
    public boolean hasId(
    ) {
        return this._has_id;
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
     * Method iterateFlats.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.castor.cpa.test.test2996.onetomany.Flat> iterateFlats(
    ) {
        return this._flatsList.iterator();
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
    public void removeAllFlats(
    ) {
        this._flatsList.clear();
    }

    /**
     * Method removeFlats.
     * 
     * @param vFlats
     * @return true if the object was removed from the collection.
     */
    public boolean removeFlats(
            final org.castor.cpa.test.test2996.onetomany.Flat vFlats) {
        boolean removed = _flatsList.remove(vFlats);
        return removed;
    }

    /**
     * Method removeFlatsAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.castor.cpa.test.test2996.onetomany.Flat removeFlatsAt(
            final int index) {
        java.lang.Object obj = this._flatsList.remove(index);
        return (org.castor.cpa.test.test2996.onetomany.Flat) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vFlats
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setFlats(
            final int index,
            final org.castor.cpa.test.test2996.onetomany.Flat vFlats)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._flatsList.size()) {
            throw new IndexOutOfBoundsException("setFlats: Index value '" + index + "' not in range [0.." + (this._flatsList.size() - 1) + "]");
        }

        this._flatsList.set(index, vFlats);
    }

    /**
     * 
     * 
     * @param vFlatsArray
     */
    public void setFlats(
            final org.castor.cpa.test.test2996.onetomany.Flat[] vFlatsArray) {
        //-- copy array
        _flatsList.clear();

        for (int i = 0; i < vFlatsArray.length; i++) {
                this._flatsList.add(vFlatsArray[i]);
        }
    }

    /**
     * Sets the value of field 'id'.
     * 
     * @param id the value of field 'id'.
     */
    public void setId(
            final long id) {
        this._id = id;
        this._has_id = true;
    }

    /**
     * Method unmarshalHouse.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.castor.cpa.test.test2996.onetomany.House
     */
    public static org.castor.cpa.test.test2996.onetomany.House unmarshalHouse(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.castor.cpa.test.test2996.onetomany.House) org.exolab.castor.xml.Unmarshaller.unmarshal(org.castor.cpa.test.test2996.onetomany.House.class, reader);
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
