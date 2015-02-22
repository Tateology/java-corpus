/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.castor.mapping.xml.types;

/**
 * Enumeration FieldMappingCollectionType.
 * 
 * @version $Revision$ $Date$
 */
public enum FieldMappingCollectionType {


      //------------------/
     //- Enum Constants -/
    //------------------/

    /**
     * Constant ARRAY
     */
    ARRAY("array"),
    /**
     * Constant VECTOR
     */
    VECTOR("vector"),
    /**
     * Constant ARRAYLIST
     */
    ARRAYLIST("arraylist"),
    /**
     * Constant PRIORITYQUEUE
     */
    PRIORITYQUEUE("priorityqueue"),
    /**
     * Constant HASHTABLE
     */
    HASHTABLE("hashtable"),
    /**
     * Constant COLLECTION
     */
    COLLECTION("collection"),
    /**
     * Constant SET
     */
    SET("set"),
    /**
     * Constant MAP
     */
    MAP("map"),
    /**
     * Constant ENUMERATE
     */
    ENUMERATE("enumerate"),
    /**
     * Constant SORTEDSET
     */
    SORTEDSET("sortedset"),
    /**
     * Constant ITERATOR
     */
    ITERATOR("iterator"),
    /**
     * Constant SORTEDMAP
     */
    SORTEDMAP("sortedmap");

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field value.
     */
    private final java.lang.String value;

    /**
     * Field enumConstants.
     */
    private static final java.util.Map<java.lang.String, FieldMappingCollectionType> enumConstants = new java.util.HashMap<java.lang.String, FieldMappingCollectionType>();


    static {
        for (FieldMappingCollectionType c: FieldMappingCollectionType.values()) {
            FieldMappingCollectionType.enumConstants.put(c.value, c);
        }

    };


      //----------------/
     //- Constructors -/
    //----------------/

    private FieldMappingCollectionType(final java.lang.String value) {
        this.value = value;
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method fromValue.
     * 
     * @param value
     * @return the constant for this value
     */
    public static org.exolab.castor.mapping.xml.types.FieldMappingCollectionType fromValue(
            final java.lang.String value) {
        FieldMappingCollectionType c = FieldMappingCollectionType.enumConstants.get(value);
        if (c != null) {
            return c;
        }
        throw new IllegalArgumentException(value);
    }

    /**
     * 
     * 
     * @param value
     */
    public void setValue(
            final java.lang.String value) {
    }

    /**
     * Method toString.
     * 
     * @return the value of this constant
     */
    public java.lang.String toString(
    ) {
        return this.value;
    }

    /**
     * Method value.
     * 
     * @return the value of this constant
     */
    public java.lang.String value(
    ) {
        return this.value;
    }

}
