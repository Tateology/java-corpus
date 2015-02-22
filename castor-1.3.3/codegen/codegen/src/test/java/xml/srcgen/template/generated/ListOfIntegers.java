/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.3-RC1</a>, using an
 * XML Schema.
 * $Id$
 */

package xml.srcgen.template.generated;

/**
 * Class ListOfIntegers.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class ListOfIntegers implements java.io.Serializable {

    /**
     * Field _listTypeList.
     */
    private java.util.Vector<java.lang.Long> _listTypeList;

    public ListOfIntegers() {
        super();
        this._listTypeList = new java.util.Vector<java.lang.Long>();
    }

    /**
     * 
     * 
     * @param vListType
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addListType(final long vListType) throws java.lang.IndexOutOfBoundsException {
        // check for the maximum size
        if (this._listTypeList.size() >= 256) {
            throw new IndexOutOfBoundsException("addListType has a maximum of 256");
        }

        this._listTypeList.addElement(new java.lang.Long(vListType));
    }

    /**
     * 
     * 
     * @param index
     * @param vListType
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addListType(final int index,final long vListType) throws java.lang.IndexOutOfBoundsException {
        // check for the maximum size
        if (this._listTypeList.size() >= 256) {
            throw new IndexOutOfBoundsException("addListType has a maximum of 256");
        }

        this._listTypeList.add(index, new java.lang.Long(vListType));
    }

    /**
     * Method enumerateListType.
     * 
     * @return an Enumeration over all long elements
     */
    public java.util.Enumeration<java.lang.Long> enumerateListType() {
        return this._listTypeList.elements();
    }

    /**
     * Method getListType.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the long at the given index
     */
    public long getListType(final int index) throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._listTypeList.size()) {
            throw new IndexOutOfBoundsException("getListType: Index value '" + index + "' not in range [0.." + (this._listTypeList.size() - 1) + "]");
        }

        return ((java.lang.Long) _listTypeList.get(index)).longValue();
    }

    /**
     * Method getListType.Returns the contents of the collection in
     * an Array.  
     * 
     * @return this collection as an Array
     */
    public long[] getListType() {
        int size = this._listTypeList.size();
        long[] array = new long[size];
        java.util.Iterator iter = _listTypeList.iterator();
        for (int index = 0; index < size; index++) {
            array[index] = ((java.lang.Long) iter.next()).longValue();
        }
        return array;
    }

    /**
     * Method getListTypeCount.
     * 
     * @return the size of this collection
     */
    public int getListTypeCount() {
        return this._listTypeList.size();
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
     */
    public void removeAllListType() {
        this._listTypeList.clear();
    }

    /**
     * Method removeListType.
     * 
     * @param vListType
     * @return true if the object was removed from the collection.
     */
    public boolean removeListType(final long vListType) {
        boolean removed = _listTypeList.remove(new java.lang.Long(vListType));
        return removed;
    }

    /**
     * Method removeListTypeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public long removeListTypeAt(final int index) {
        java.lang.Object obj = this._listTypeList.remove(index);
        return ((java.lang.Long) obj).longValue();
    }

    /**
     * 
     * 
     * @param index
     * @param vListType
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setListType(final int index,final long vListType) throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._listTypeList.size()) {
            throw new IndexOutOfBoundsException("setListType: Index value '" + index + "' not in range [0.." + (this._listTypeList.size() - 1) + "]");
        }

        this._listTypeList.set(index, new java.lang.Long(vListType));
    }

    /**
     * 
     * 
     * @param vListTypeArray
     */
    public void setListType(final long[] vListTypeArray) {
        //-- copy array
        _listTypeList.clear();

        for (int i = 0; i < vListTypeArray.length; i++) {
                this._listTypeList.add(new java.lang.Long(vListTypeArray[i]));
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
     * xml.srcgen.template.generated.ListOfIntegers
     */
    public static xml.srcgen.template.generated.ListOfIntegers unmarshal(final java.io.Reader reader) throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (xml.srcgen.template.generated.ListOfIntegers) org.exolab.castor.xml.Unmarshaller.unmarshal(xml.srcgen.template.generated.ListOfIntegers.class, reader);
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
