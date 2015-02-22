/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.castor.mapping.xml.types;

/**
 * Enumeration BindXmlNodeType.
 * 
 * @version $Revision$ $Date$
 */
public enum BindXmlNodeType {


      //------------------/
     //- Enum Constants -/
    //------------------/

    /**
     * Constant ATTRIBUTE
     */
    ATTRIBUTE("attribute"),
    /**
     * Constant ELEMENT
     */
    ELEMENT("element"),
    /**
     * Constant NAMESPACE
     */
    NAMESPACE("namespace"),
    /**
     * Constant TEXT
     */
    TEXT("text");

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
    private static final java.util.Map<java.lang.String, BindXmlNodeType> enumConstants = new java.util.HashMap<java.lang.String, BindXmlNodeType>();


    static {
        for (BindXmlNodeType c: BindXmlNodeType.values()) {
            BindXmlNodeType.enumConstants.put(c.value, c);
        }

    };


      //----------------/
     //- Constructors -/
    //----------------/

    private BindXmlNodeType(final java.lang.String value) {
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
    public static org.exolab.castor.mapping.xml.types.BindXmlNodeType fromValue(
            final java.lang.String value) {
        BindXmlNodeType c = BindXmlNodeType.enumConstants.get(value);
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
