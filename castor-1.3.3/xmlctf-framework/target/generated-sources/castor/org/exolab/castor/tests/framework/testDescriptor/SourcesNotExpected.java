/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.castor.tests.framework.testDescriptor;

/**
 * Class SourcesNotExpected.
 * 
 * @version $Revision$ $Date$
 */
public class SourcesNotExpected implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _sourceNotExpectedList.
     */
    private java.util.List _sourceNotExpectedList;


      //----------------/
     //- Constructors -/
    //----------------/

    public SourcesNotExpected() {
        super();
        this._sourceNotExpectedList = new java.util.ArrayList();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vSourceNotExpected
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSourceNotExpected(
            final java.lang.String vSourceNotExpected)
    throws java.lang.IndexOutOfBoundsException {
        this._sourceNotExpectedList.add(vSourceNotExpected);
    }

    /**
     * 
     * 
     * @param index
     * @param vSourceNotExpected
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSourceNotExpected(
            final int index,
            final java.lang.String vSourceNotExpected)
    throws java.lang.IndexOutOfBoundsException {
        this._sourceNotExpectedList.add(index, vSourceNotExpected);
    }

    /**
     * Method enumerateSourceNotExpected.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration enumerateSourceNotExpected(
    ) {
        return java.util.Collections.enumeration(this._sourceNotExpectedList);
    }

    /**
     * Method getSourceNotExpected.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getSourceNotExpected(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._sourceNotExpectedList.size()) {
            throw new IndexOutOfBoundsException("getSourceNotExpected: Index value '" + index + "' not in range [0.." + (this._sourceNotExpectedList.size() - 1) + "]");
        }

        return (java.lang.String) _sourceNotExpectedList.get(index);
    }

    /**
     * Method getSourceNotExpected.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getSourceNotExpected(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._sourceNotExpectedList.toArray(array);
    }

    /**
     * Method getSourceNotExpectedCount.
     * 
     * @return the size of this collection
     */
    public int getSourceNotExpectedCount(
    ) {
        return this._sourceNotExpectedList.size();
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
     * Method iterateSourceNotExpected.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator iterateSourceNotExpected(
    ) {
        return this._sourceNotExpectedList.iterator();
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
    public void removeAllSourceNotExpected(
    ) {
        this._sourceNotExpectedList.clear();
    }

    /**
     * Method removeSourceNotExpected.
     * 
     * @param vSourceNotExpected
     * @return true if the object was removed from the collection.
     */
    public boolean removeSourceNotExpected(
            final java.lang.String vSourceNotExpected) {
        boolean removed = _sourceNotExpectedList.remove(vSourceNotExpected);
        return removed;
    }

    /**
     * Method removeSourceNotExpectedAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeSourceNotExpectedAt(
            final int index) {
        java.lang.Object obj = this._sourceNotExpectedList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vSourceNotExpected
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSourceNotExpected(
            final int index,
            final java.lang.String vSourceNotExpected)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._sourceNotExpectedList.size()) {
            throw new IndexOutOfBoundsException("setSourceNotExpected: Index value '" + index + "' not in range [0.." + (this._sourceNotExpectedList.size() - 1) + "]");
        }

        this._sourceNotExpectedList.set(index, vSourceNotExpected);
    }

    /**
     * 
     * 
     * @param vSourceNotExpectedArray
     */
    public void setSourceNotExpected(
            final java.lang.String[] vSourceNotExpectedArray) {
        //-- copy array
        _sourceNotExpectedList.clear();

        for (int i = 0; i < vSourceNotExpectedArray.length; i++) {
                this._sourceNotExpectedList.add(vSourceNotExpectedArray[i]);
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
     * org.exolab.castor.tests.framework.testDescriptor.SourcesNotExpected
     */
    public static org.exolab.castor.tests.framework.testDescriptor.SourcesNotExpected unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.exolab.castor.tests.framework.testDescriptor.SourcesNotExpected) org.exolab.castor.xml.Unmarshaller.unmarshal(org.exolab.castor.tests.framework.testDescriptor.SourcesNotExpected.class, reader);
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
