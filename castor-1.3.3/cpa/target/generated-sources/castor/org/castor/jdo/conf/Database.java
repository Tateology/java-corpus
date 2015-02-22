/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.castor.jdo.conf;

/**
 * Class Database.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Database implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _engine.
     */
    private java.lang.String _engine = "generic";

    /**
     * Field _databaseChoice.
     */
    private org.castor.jdo.conf.DatabaseChoice _databaseChoice;

    /**
     * Field _mappingList.
     */
    private java.util.List<org.castor.jdo.conf.Mapping> _mappingList;

    /**
     * Field _packageMappingList.
     */
    private java.util.List<org.castor.jdo.conf.PackageMapping> _packageMappingList;

    /**
     * Field _classMappingList.
     */
    private java.util.List<org.castor.jdo.conf.ClassMapping> _classMappingList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Database() {
        super();
        setEngine("generic");
        this._mappingList = new java.util.ArrayList<org.castor.jdo.conf.Mapping>();
        this._packageMappingList = new java.util.ArrayList<org.castor.jdo.conf.PackageMapping>();
        this._classMappingList = new java.util.ArrayList<org.castor.jdo.conf.ClassMapping>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vClassMapping
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addClassMapping(
            final org.castor.jdo.conf.ClassMapping vClassMapping)
    throws java.lang.IndexOutOfBoundsException {
        this._classMappingList.add(vClassMapping);
    }

    /**
     * 
     * 
     * @param index
     * @param vClassMapping
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addClassMapping(
            final int index,
            final org.castor.jdo.conf.ClassMapping vClassMapping)
    throws java.lang.IndexOutOfBoundsException {
        this._classMappingList.add(index, vClassMapping);
    }

    /**
     * 
     * 
     * @param vMapping
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMapping(
            final org.castor.jdo.conf.Mapping vMapping)
    throws java.lang.IndexOutOfBoundsException {
        this._mappingList.add(vMapping);
    }

    /**
     * 
     * 
     * @param index
     * @param vMapping
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMapping(
            final int index,
            final org.castor.jdo.conf.Mapping vMapping)
    throws java.lang.IndexOutOfBoundsException {
        this._mappingList.add(index, vMapping);
    }

    /**
     * 
     * 
     * @param vPackageMapping
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPackageMapping(
            final org.castor.jdo.conf.PackageMapping vPackageMapping)
    throws java.lang.IndexOutOfBoundsException {
        this._packageMappingList.add(vPackageMapping);
    }

    /**
     * 
     * 
     * @param index
     * @param vPackageMapping
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPackageMapping(
            final int index,
            final org.castor.jdo.conf.PackageMapping vPackageMapping)
    throws java.lang.IndexOutOfBoundsException {
        this._packageMappingList.add(index, vPackageMapping);
    }

    /**
     * Method enumerateClassMapping.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.castor.jdo.conf.ClassMapping> enumerateClassMapping(
    ) {
        return java.util.Collections.enumeration(this._classMappingList);
    }

    /**
     * Method enumerateMapping.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.castor.jdo.conf.Mapping> enumerateMapping(
    ) {
        return java.util.Collections.enumeration(this._mappingList);
    }

    /**
     * Method enumeratePackageMapping.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.castor.jdo.conf.PackageMapping> enumeratePackageMapping(
    ) {
        return java.util.Collections.enumeration(this._packageMappingList);
    }

    /**
     * Method getClassMapping.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the org.castor.jdo.conf.ClassMapping at
     * the given index
     */
    public org.castor.jdo.conf.ClassMapping getClassMapping(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._classMappingList.size()) {
            throw new IndexOutOfBoundsException("getClassMapping: Index value '" + index + "' not in range [0.." + (this._classMappingList.size() - 1) + "]");
        }

        return (org.castor.jdo.conf.ClassMapping) _classMappingList.get(index);
    }

    /**
     * Method getClassMapping.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.castor.jdo.conf.ClassMapping[] getClassMapping(
    ) {
        org.castor.jdo.conf.ClassMapping[] array = new org.castor.jdo.conf.ClassMapping[0];
        return (org.castor.jdo.conf.ClassMapping[]) this._classMappingList.toArray(array);
    }

    /**
     * Method getClassMappingCount.
     * 
     * @return the size of this collection
     */
    public int getClassMappingCount(
    ) {
        return this._classMappingList.size();
    }

    /**
     * Returns the value of field 'databaseChoice'.
     * 
     * @return the value of field 'DatabaseChoice'.
     */
    public org.castor.jdo.conf.DatabaseChoice getDatabaseChoice(
    ) {
        return this._databaseChoice;
    }

    /**
     * Returns the value of field 'engine'.
     * 
     * @return the value of field 'Engine'.
     */
    public java.lang.String getEngine(
    ) {
        return this._engine;
    }

    /**
     * Method getMapping.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the org.castor.jdo.conf.Mapping at the
     * given index
     */
    public org.castor.jdo.conf.Mapping getMapping(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mappingList.size()) {
            throw new IndexOutOfBoundsException("getMapping: Index value '" + index + "' not in range [0.." + (this._mappingList.size() - 1) + "]");
        }

        return (org.castor.jdo.conf.Mapping) _mappingList.get(index);
    }

    /**
     * Method getMapping.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.castor.jdo.conf.Mapping[] getMapping(
    ) {
        org.castor.jdo.conf.Mapping[] array = new org.castor.jdo.conf.Mapping[0];
        return (org.castor.jdo.conf.Mapping[]) this._mappingList.toArray(array);
    }

    /**
     * Method getMappingCount.
     * 
     * @return the size of this collection
     */
    public int getMappingCount(
    ) {
        return this._mappingList.size();
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
     * Method getPackageMapping.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the org.castor.jdo.conf.PackageMapping
     * at the given index
     */
    public org.castor.jdo.conf.PackageMapping getPackageMapping(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._packageMappingList.size()) {
            throw new IndexOutOfBoundsException("getPackageMapping: Index value '" + index + "' not in range [0.." + (this._packageMappingList.size() - 1) + "]");
        }

        return (org.castor.jdo.conf.PackageMapping) _packageMappingList.get(index);
    }

    /**
     * Method getPackageMapping.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.castor.jdo.conf.PackageMapping[] getPackageMapping(
    ) {
        org.castor.jdo.conf.PackageMapping[] array = new org.castor.jdo.conf.PackageMapping[0];
        return (org.castor.jdo.conf.PackageMapping[]) this._packageMappingList.toArray(array);
    }

    /**
     * Method getPackageMappingCount.
     * 
     * @return the size of this collection
     */
    public int getPackageMappingCount(
    ) {
        return this._packageMappingList.size();
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
     * Method iterateClassMapping.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.castor.jdo.conf.ClassMapping> iterateClassMapping(
    ) {
        return this._classMappingList.iterator();
    }

    /**
     * Method iterateMapping.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.castor.jdo.conf.Mapping> iterateMapping(
    ) {
        return this._mappingList.iterator();
    }

    /**
     * Method iteratePackageMapping.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.castor.jdo.conf.PackageMapping> iteratePackageMapping(
    ) {
        return this._packageMappingList.iterator();
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
    public void removeAllClassMapping(
    ) {
        this._classMappingList.clear();
    }

    /**
     */
    public void removeAllMapping(
    ) {
        this._mappingList.clear();
    }

    /**
     */
    public void removeAllPackageMapping(
    ) {
        this._packageMappingList.clear();
    }

    /**
     * Method removeClassMapping.
     * 
     * @param vClassMapping
     * @return true if the object was removed from the collection.
     */
    public boolean removeClassMapping(
            final org.castor.jdo.conf.ClassMapping vClassMapping) {
        boolean removed = _classMappingList.remove(vClassMapping);
        return removed;
    }

    /**
     * Method removeClassMappingAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.castor.jdo.conf.ClassMapping removeClassMappingAt(
            final int index) {
        java.lang.Object obj = this._classMappingList.remove(index);
        return (org.castor.jdo.conf.ClassMapping) obj;
    }

    /**
     * Method removeMapping.
     * 
     * @param vMapping
     * @return true if the object was removed from the collection.
     */
    public boolean removeMapping(
            final org.castor.jdo.conf.Mapping vMapping) {
        boolean removed = _mappingList.remove(vMapping);
        return removed;
    }

    /**
     * Method removeMappingAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.castor.jdo.conf.Mapping removeMappingAt(
            final int index) {
        java.lang.Object obj = this._mappingList.remove(index);
        return (org.castor.jdo.conf.Mapping) obj;
    }

    /**
     * Method removePackageMapping.
     * 
     * @param vPackageMapping
     * @return true if the object was removed from the collection.
     */
    public boolean removePackageMapping(
            final org.castor.jdo.conf.PackageMapping vPackageMapping) {
        boolean removed = _packageMappingList.remove(vPackageMapping);
        return removed;
    }

    /**
     * Method removePackageMappingAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.castor.jdo.conf.PackageMapping removePackageMappingAt(
            final int index) {
        java.lang.Object obj = this._packageMappingList.remove(index);
        return (org.castor.jdo.conf.PackageMapping) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vClassMapping
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setClassMapping(
            final int index,
            final org.castor.jdo.conf.ClassMapping vClassMapping)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._classMappingList.size()) {
            throw new IndexOutOfBoundsException("setClassMapping: Index value '" + index + "' not in range [0.." + (this._classMappingList.size() - 1) + "]");
        }

        this._classMappingList.set(index, vClassMapping);
    }

    /**
     * 
     * 
     * @param vClassMappingArray
     */
    public void setClassMapping(
            final org.castor.jdo.conf.ClassMapping[] vClassMappingArray) {
        //-- copy array
        _classMappingList.clear();

        for (int i = 0; i < vClassMappingArray.length; i++) {
                this._classMappingList.add(vClassMappingArray[i]);
        }
    }

    /**
     * Sets the value of field 'databaseChoice'.
     * 
     * @param databaseChoice the value of field 'databaseChoice'.
     */
    public void setDatabaseChoice(
            final org.castor.jdo.conf.DatabaseChoice databaseChoice) {
        this._databaseChoice = databaseChoice;
    }

    /**
     * Sets the value of field 'engine'.
     * 
     * @param engine the value of field 'engine'.
     */
    public void setEngine(
            final java.lang.String engine) {
        this._engine = engine;
    }

    /**
     * 
     * 
     * @param index
     * @param vMapping
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMapping(
            final int index,
            final org.castor.jdo.conf.Mapping vMapping)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mappingList.size()) {
            throw new IndexOutOfBoundsException("setMapping: Index value '" + index + "' not in range [0.." + (this._mappingList.size() - 1) + "]");
        }

        this._mappingList.set(index, vMapping);
    }

    /**
     * 
     * 
     * @param vMappingArray
     */
    public void setMapping(
            final org.castor.jdo.conf.Mapping[] vMappingArray) {
        //-- copy array
        _mappingList.clear();

        for (int i = 0; i < vMappingArray.length; i++) {
                this._mappingList.add(vMappingArray[i]);
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
     * 
     * 
     * @param index
     * @param vPackageMapping
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPackageMapping(
            final int index,
            final org.castor.jdo.conf.PackageMapping vPackageMapping)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._packageMappingList.size()) {
            throw new IndexOutOfBoundsException("setPackageMapping: Index value '" + index + "' not in range [0.." + (this._packageMappingList.size() - 1) + "]");
        }

        this._packageMappingList.set(index, vPackageMapping);
    }

    /**
     * 
     * 
     * @param vPackageMappingArray
     */
    public void setPackageMapping(
            final org.castor.jdo.conf.PackageMapping[] vPackageMappingArray) {
        //-- copy array
        _packageMappingList.clear();

        for (int i = 0; i < vPackageMappingArray.length; i++) {
                this._packageMappingList.add(vPackageMappingArray[i]);
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
     * @return the unmarshaled org.castor.jdo.conf.Database
     */
    public static org.castor.jdo.conf.Database unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.castor.jdo.conf.Database) org.exolab.castor.xml.Unmarshaller.unmarshal(org.castor.jdo.conf.Database.class, reader);
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
