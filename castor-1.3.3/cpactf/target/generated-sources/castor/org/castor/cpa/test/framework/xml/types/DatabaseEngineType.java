/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.castor.cpa.test.framework.xml.types;

/**
 * Enumeration DatabaseEngineType.
 * 
 * @version $Revision$ $Date$
 */
public enum DatabaseEngineType {


      //------------------/
     //- Enum Constants -/
    //------------------/

    /**
     * Constant DB2
     */
    DB2("db2"),
    /**
     * Constant DERBY
     */
    DERBY("derby"),
    /**
     * Constant HSQL
     */
    HSQL("hsql"),
    /**
     * Constant MYSQL
     */
    MYSQL("mysql"),
    /**
     * Constant ORACLE
     */
    ORACLE("oracle"),
    /**
     * Constant POINTBASE
     */
    POINTBASE("pointbase"),
    /**
     * Constant POSTGRESQL
     */
    POSTGRESQL("postgresql"),
    /**
     * Constant PROGRESS
     */
    PROGRESS("progress"),
    /**
     * Constant SAPDB
     */
    SAPDB("sapdb"),
    /**
     * Constant SQL_SERVER
     */
    SQL_SERVER("sql-server"),
    /**
     * Constant SYBASE
     */
    SYBASE("sybase");

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
    private static final java.util.Map<java.lang.String, DatabaseEngineType> enumConstants = new java.util.HashMap<java.lang.String, DatabaseEngineType>();


    static {
        for (DatabaseEngineType c: DatabaseEngineType.values()) {
            DatabaseEngineType.enumConstants.put(c.value, c);
        }

    };


      //----------------/
     //- Constructors -/
    //----------------/

    private DatabaseEngineType(final java.lang.String value) {
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
    public static org.castor.cpa.test.framework.xml.types.DatabaseEngineType fromValue(
            final java.lang.String value) {
        DatabaseEngineType c = DatabaseEngineType.enumConstants.get(value);
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
