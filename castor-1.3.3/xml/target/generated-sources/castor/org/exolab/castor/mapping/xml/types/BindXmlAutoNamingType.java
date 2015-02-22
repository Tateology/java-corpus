/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.castor.mapping.xml.types;

/**
 * Enumeration BindXmlAutoNamingType.
 * 
 * @version $Revision$ $Date$
 */
public enum BindXmlAutoNamingType {


      //------------------/
     //- Enum Constants -/
    //------------------/

    /**
     * Constant DERIVEBYCLASS
     */
    DERIVEBYCLASS("deriveByClass"),
    /**
     * Constant DERIVEBYFIELD
     */
    DERIVEBYFIELD("deriveByField");

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
    private static final java.util.Map<java.lang.String, BindXmlAutoNamingType> enumConstants = new java.util.HashMap<java.lang.String, BindXmlAutoNamingType>();


    static {
        for (BindXmlAutoNamingType c: BindXmlAutoNamingType.values()) {
            BindXmlAutoNamingType.enumConstants.put(c.value, c);
        }

    };


      //----------------/
     //- Constructors -/
    //----------------/

    private BindXmlAutoNamingType(final java.lang.String value) {
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
    public static org.exolab.castor.mapping.xml.types.BindXmlAutoNamingType fromValue(
            final java.lang.String value) {
        BindXmlAutoNamingType c = BindXmlAutoNamingType.enumConstants.get(value);
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
