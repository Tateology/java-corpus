/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.castor.mapping.xml.types;

/**
 * Enumeration SqlDirtyType.
 * 
 * @version $Revision$ $Date$
 */
public enum SqlDirtyType {


      //------------------/
     //- Enum Constants -/
    //------------------/

    /**
     * Constant CHECK
     */
    CHECK("check"),
    /**
     * Constant IGNORE
     */
    IGNORE("ignore");

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
    private static final java.util.Map<java.lang.String, SqlDirtyType> enumConstants = new java.util.HashMap<java.lang.String, SqlDirtyType>();


    static {
        for (SqlDirtyType c: SqlDirtyType.values()) {
            SqlDirtyType.enumConstants.put(c.value, c);
        }

    };


      //----------------/
     //- Constructors -/
    //----------------/

    private SqlDirtyType(final java.lang.String value) {
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
    public static org.exolab.castor.mapping.xml.types.SqlDirtyType fromValue(
            final java.lang.String value) {
        SqlDirtyType c = SqlDirtyType.enumConstants.get(value);
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
