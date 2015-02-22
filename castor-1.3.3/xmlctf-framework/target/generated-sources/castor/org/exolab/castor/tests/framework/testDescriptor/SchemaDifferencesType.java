/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.castor.tests.framework.testDescriptor;

/**
 * If you expect a non-zero number of differences when comparing
 * schemas,
 *  add one of these elements and provide the FailureStep attribute
 *  to say which step this difference applies to.
 *  
 * 
 * @version $Revision$ $Date$
 */
public class SchemaDifferencesType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
    private int _content;

    /**
     * keeps track of state for field: _content
     */
    private boolean _has_content;

    /**
     * Field _failureStep.
     */
    private org.exolab.castor.tests.framework.testDescriptor.types.FailureStepType _failureStep;


      //----------------/
     //- Constructors -/
    //----------------/

    public SchemaDifferencesType() {
        super();
    }

    public SchemaDifferencesType(final java.lang.String defaultValue) {
        try {
            setContent(new java.lang.Integer(defaultValue).intValue());
         } catch(Exception e) {
            throw new RuntimeException("Unable to cast default value for simple content!");
         } 
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteContent(
    ) {
        this._has_content= false;
    }

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     * 
     * @return the value of field 'Content'.
     */
    public int getContent(
    ) {
        return this._content;
    }

    /**
     * Returns the value of field 'failureStep'.
     * 
     * @return the value of field 'FailureStep'.
     */
    public org.exolab.castor.tests.framework.testDescriptor.types.FailureStepType getFailureStep(
    ) {
        return this._failureStep;
    }

    /**
     * Method hasContent.
     * 
     * @return true if at least one Content has been added
     */
    public boolean hasContent(
    ) {
        return this._has_content;
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
     * Sets the value of field 'content'. The field 'content' has
     * the following description: internal content storage
     * 
     * @param content the value of field 'content'.
     */
    public void setContent(
            final int content) {
        this._content = content;
        this._has_content = true;
    }

    /**
     * Sets the value of field 'failureStep'.
     * 
     * @param failureStep the value of field 'failureStep'.
     */
    public void setFailureStep(
            final org.exolab.castor.tests.framework.testDescriptor.types.FailureStepType failureStep) {
        this._failureStep = failureStep;
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
     * org.exolab.castor.tests.framework.testDescriptor.SchemaDifferencesType
     */
    public static org.exolab.castor.tests.framework.testDescriptor.SchemaDifferencesType unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.exolab.castor.tests.framework.testDescriptor.SchemaDifferencesType) org.exolab.castor.xml.Unmarshaller.unmarshal(org.exolab.castor.tests.framework.testDescriptor.SchemaDifferencesType.class, reader);
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
