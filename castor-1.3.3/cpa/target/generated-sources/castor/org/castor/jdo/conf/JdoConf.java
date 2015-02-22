/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.castor.jdo.conf;

/**
 * Class JdoConf.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class JdoConf implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name = "jdo-conf";

    /**
     * Field _databaseList.
     */
    private java.util.List<org.castor.jdo.conf.Database> _databaseList;

    /**
     * Field _transactionDemarcation.
     */
    private org.castor.jdo.conf.TransactionDemarcation _transactionDemarcation;


      //----------------/
     //- Constructors -/
    //----------------/

    public JdoConf() {
        super();
        setName("jdo-conf");
        this._databaseList = new java.util.ArrayList<org.castor.jdo.conf.Database>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vDatabase
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDatabase(
            final org.castor.jdo.conf.Database vDatabase)
    throws java.lang.IndexOutOfBoundsException {
        this._databaseList.add(vDatabase);
    }

    /**
     * 
     * 
     * @param index
     * @param vDatabase
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDatabase(
            final int index,
            final org.castor.jdo.conf.Database vDatabase)
    throws java.lang.IndexOutOfBoundsException {
        this._databaseList.add(index, vDatabase);
    }

    /**
     * Method enumerateDatabase.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.castor.jdo.conf.Database> enumerateDatabase(
    ) {
        return java.util.Collections.enumeration(this._databaseList);
    }

    /**
     * Method getDatabase.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the org.castor.jdo.conf.Database at the
     * given index
     */
    public org.castor.jdo.conf.Database getDatabase(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._databaseList.size()) {
            throw new IndexOutOfBoundsException("getDatabase: Index value '" + index + "' not in range [0.." + (this._databaseList.size() - 1) + "]");
        }

        return (org.castor.jdo.conf.Database) _databaseList.get(index);
    }

    /**
     * Method getDatabase.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.castor.jdo.conf.Database[] getDatabase(
    ) {
        org.castor.jdo.conf.Database[] array = new org.castor.jdo.conf.Database[0];
        return (org.castor.jdo.conf.Database[]) this._databaseList.toArray(array);
    }

    /**
     * Method getDatabaseCount.
     * 
     * @return the size of this collection
     */
    public int getDatabaseCount(
    ) {
        return this._databaseList.size();
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name;
    }

    /**
     * Returns the value of field 'transactionDemarcation'.
     * 
     * @return the value of field 'TransactionDemarcation'.
     */
    public org.castor.jdo.conf.TransactionDemarcation getTransactionDemarcation(
    ) {
        return this._transactionDemarcation;
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
     * Method iterateDatabase.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.castor.jdo.conf.Database> iterateDatabase(
    ) {
        return this._databaseList.iterator();
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
    public void removeAllDatabase(
    ) {
        this._databaseList.clear();
    }

    /**
     * Method removeDatabase.
     * 
     * @param vDatabase
     * @return true if the object was removed from the collection.
     */
    public boolean removeDatabase(
            final org.castor.jdo.conf.Database vDatabase) {
        boolean removed = _databaseList.remove(vDatabase);
        return removed;
    }

    /**
     * Method removeDatabaseAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.castor.jdo.conf.Database removeDatabaseAt(
            final int index) {
        java.lang.Object obj = this._databaseList.remove(index);
        return (org.castor.jdo.conf.Database) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vDatabase
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setDatabase(
            final int index,
            final org.castor.jdo.conf.Database vDatabase)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._databaseList.size()) {
            throw new IndexOutOfBoundsException("setDatabase: Index value '" + index + "' not in range [0.." + (this._databaseList.size() - 1) + "]");
        }

        this._databaseList.set(index, vDatabase);
    }

    /**
     * 
     * 
     * @param vDatabaseArray
     */
    public void setDatabase(
            final org.castor.jdo.conf.Database[] vDatabaseArray) {
        //-- copy array
        _databaseList.clear();

        for (int i = 0; i < vDatabaseArray.length; i++) {
                this._databaseList.add(vDatabaseArray[i]);
        }
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'transactionDemarcation'.
     * 
     * @param transactionDemarcation the value of field
     * 'transactionDemarcation'.
     */
    public void setTransactionDemarcation(
            final org.castor.jdo.conf.TransactionDemarcation transactionDemarcation) {
        this._transactionDemarcation = transactionDemarcation;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.castor.jdo.conf.JdoConf
     */
    public static org.castor.jdo.conf.JdoConf unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.castor.jdo.conf.JdoConf) org.exolab.castor.xml.Unmarshaller.unmarshal(org.castor.jdo.conf.JdoConf.class, reader);
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
