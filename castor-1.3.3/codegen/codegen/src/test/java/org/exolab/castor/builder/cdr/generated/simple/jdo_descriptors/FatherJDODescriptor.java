/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.3-RC1</a>, using an
 * XML Schema.
 * $Id$
 */

package org.exolab.castor.builder.cdr.generated.simple.jdo_descriptors;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.lang.reflect.Method;
import org.castor.core.exception.IllegalClassDescriptorInitialization;
import org.castor.jdo.engine.SQLTypeInfos;
import org.exolab.castor.builder.cdr.generated.simple.Father;
import org.exolab.castor.jdo.engine.nature.ClassDescriptorJDONature;
import org.exolab.castor.jdo.engine.nature.FieldDescriptorJDONature;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.mapping.FieldDescriptor;
import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.mapping.loader.FieldDescriptorImpl;
import org.exolab.castor.mapping.loader.FieldHandlerImpl;
import org.exolab.castor.mapping.loader.TypeInfo;
import org.exolab.castor.mapping.xml.ClassChoice;
import org.exolab.castor.mapping.xml.ClassMapping;
import org.exolab.castor.mapping.xml.FieldMapping;
import org.exolab.castor.mapping.xml.MapTo;
import org.exolab.castor.mapping.xml.Sql;
import org.exolab.castor.mapping.xml.types.ClassMappingAccessType;
import org.exolab.castor.mapping.xml.types.FieldMappingCollectionType;

/**
 * Class FatherJDODescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class FatherJDODescriptor extends org.exolab.castor.mapping.loader.ClassDescriptorImpl {

    public FatherJDODescriptor() {
        super();
        ClassMapping mapping = new ClassMapping();
        ClassChoice choice = new ClassChoice();
        MapTo mapTo = new MapTo();

        addNature(ClassDescriptorJDONature.class.getName());
        ClassDescriptorJDONature jdoNature = new ClassDescriptorJDONature(this);
        jdoNature.setTableName("Father");
        setJavaClass(Father.class);
        jdoNature.setAccessMode(AccessMode.valueOf("shared"));
        jdoNature.addCacheParam("name", "org.exolab.castor.builder.cdr.generated.simple.Father");

        mapping.setAccess(ClassMappingAccessType.fromValue("shared"));
        mapping.setAutoComplete(true);
        mapping.setName("org.exolab.castor.builder.cdr.generated.simple.Father");
        mapping.setClassChoice(choice);
        mapTo.setTable("Father");
        mapping.setMapTo(mapTo);
        setMapping(mapping);

        //ssnr field
        String ssnrFieldName = "ssnr";
        FieldDescriptorImpl ssnrFieldDescr;
        FieldMapping ssnrFM = new FieldMapping();
        TypeInfo ssnrType = new TypeInfo(java.lang.Long.class);
        // Set columns required (= not null)
        ssnrType.setRequired(true);

        FieldHandler ssnrHandler;
        try {
            Method ssnrGetMethod = Father.class.getMethod("getSsnr", null);
            Method ssnrSetMethod = Father.class.getMethod("setSsnr", new Class[]{
                long.class});

            ssnrHandler = new FieldHandlerImpl(ssnrFieldName, null, null,
                ssnrGetMethod, ssnrSetMethod, ssnrType);

        } catch (SecurityException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (MappingException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (NoSuchMethodException e1) {
            throw new RuntimeException(e1.getMessage());
        }
        // Instantiate ssnr field descriptor
        ssnrFieldDescr = new FieldDescriptorImpl(ssnrFieldName, ssnrType,ssnrHandler, false);
        ssnrFieldDescr.addNature(FieldDescriptorJDONature.class.getName());
        FieldDescriptorJDONature ssnrFieldJdoNature = new FieldDescriptorJDONature(ssnrFieldDescr);
        ssnrFieldJdoNature.setSQLName(new String[] { "ssnr" });
        ssnrFieldJdoNature.setSQLType(new int[] {SQLTypeInfos.javaType2sqlTypeNum(java.lang.Long.class) });
        ssnrFieldJdoNature.setManyTable(null);
        ssnrFieldJdoNature.setManyKey(new String[] {});
        ssnrFieldJdoNature.setDirtyCheck(false);
        ssnrFieldJdoNature.setReadOnly(false);

        ssnrFieldDescr.setDirect(false);
        ssnrFieldDescr.setRequired(true);
        ssnrFieldDescr.setSetMethod("setSsnr");
        ssnrFieldDescr.setGetMethod("getSsnr");

        ssnrFieldDescr.setContainingClassDescriptor(this);
        ssnrFieldDescr.setIdentity(true);
        ssnrFM.setIdentity(true);
        ssnrFM.setDirect(false);
        ssnrFM.setName("ssnr");
        ssnrFM.setRequired(true);
        ssnrFM.setSetMethod("setSsnr");
        ssnrFM.setGetMethod("getSsnr");
        Sql ssnrSql = new Sql();
        ssnrSql.addName("ssnr");
        ssnrSql.setType("integer");
        ssnrFM.setSql(ssnrSql);
        ssnrFM.setType("long");
        choice.addFieldMapping(ssnrFM);

        //firstName field
        String firstNameFieldName = "firstName";
        FieldDescriptorImpl firstNameFieldDescr;
        FieldMapping firstNameFM = new FieldMapping();
        TypeInfo firstNameType = new TypeInfo(java.lang.String.class);
        // Set columns required (= not null)
        firstNameType.setRequired(true);

        FieldHandler firstNameHandler;
        try {
            Method firstNameGetMethod = Father.class.getMethod("getFirstName", null);
            Method firstNameSetMethod = Father.class.getMethod("setFirstName", new Class[]{
                java.lang.String.class});

            firstNameHandler = new FieldHandlerImpl(firstNameFieldName, null, null,
                firstNameGetMethod, firstNameSetMethod, firstNameType);

        } catch (SecurityException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (MappingException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (NoSuchMethodException e1) {
            throw new RuntimeException(e1.getMessage());
        }
        // Instantiate firstName field descriptor
        firstNameFieldDescr = new FieldDescriptorImpl(firstNameFieldName, firstNameType,firstNameHandler, false);
        firstNameFieldDescr.addNature(FieldDescriptorJDONature.class.getName());
        FieldDescriptorJDONature firstNameFieldJdoNature = new FieldDescriptorJDONature(firstNameFieldDescr);
        firstNameFieldJdoNature.setSQLName(new String[] { "firstName" });
        firstNameFieldJdoNature.setSQLType(new int[] {SQLTypeInfos.javaType2sqlTypeNum(java.lang.String.class) });
        firstNameFieldJdoNature.setManyTable(null);
        firstNameFieldJdoNature.setManyKey(new String[] {});
        firstNameFieldJdoNature.setDirtyCheck(false);
        firstNameFieldJdoNature.setReadOnly(false);

        firstNameFieldDescr.setDirect(false);
        firstNameFieldDescr.setRequired(true);
        firstNameFieldDescr.setSetMethod("setFirstName");
        firstNameFieldDescr.setGetMethod("getFirstName");

        firstNameFieldDescr.setContainingClassDescriptor(this);
        firstNameFieldDescr.setIdentity(false);
        firstNameFM.setIdentity(false);
        firstNameFM.setDirect(false);
        firstNameFM.setName("firstName");
        firstNameFM.setRequired(true);
        firstNameFM.setSetMethod("setFirstName");
        firstNameFM.setGetMethod("getFirstName");
        Sql firstNameSql = new Sql();
        firstNameSql.addName("firstName");
        firstNameSql.setType("varchar");
        firstNameFM.setSql(firstNameSql);
        firstNameFM.setType("java.lang.String");
        choice.addFieldMapping(firstNameFM);

        //lastName field
        String lastNameFieldName = "lastName";
        FieldDescriptorImpl lastNameFieldDescr;
        FieldMapping lastNameFM = new FieldMapping();
        TypeInfo lastNameType = new TypeInfo(java.lang.String.class);
        // Set columns required (= not null)
        lastNameType.setRequired(true);

        FieldHandler lastNameHandler;
        try {
            Method lastNameGetMethod = Father.class.getMethod("getLastName", null);
            Method lastNameSetMethod = Father.class.getMethod("setLastName", new Class[]{
                java.lang.String.class});

            lastNameHandler = new FieldHandlerImpl(lastNameFieldName, null, null,
                lastNameGetMethod, lastNameSetMethod, lastNameType);

        } catch (SecurityException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (MappingException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (NoSuchMethodException e1) {
            throw new RuntimeException(e1.getMessage());
        }
        // Instantiate lastName field descriptor
        lastNameFieldDescr = new FieldDescriptorImpl(lastNameFieldName, lastNameType,lastNameHandler, false);
        lastNameFieldDescr.addNature(FieldDescriptorJDONature.class.getName());
        FieldDescriptorJDONature lastNameFieldJdoNature = new FieldDescriptorJDONature(lastNameFieldDescr);
        lastNameFieldJdoNature.setSQLName(new String[] { "lastName" });
        lastNameFieldJdoNature.setSQLType(new int[] {SQLTypeInfos.javaType2sqlTypeNum(java.lang.String.class) });
        lastNameFieldJdoNature.setManyTable(null);
        lastNameFieldJdoNature.setManyKey(new String[] {});
        lastNameFieldJdoNature.setDirtyCheck(false);
        lastNameFieldJdoNature.setReadOnly(false);

        lastNameFieldDescr.setDirect(false);
        lastNameFieldDescr.setRequired(true);
        lastNameFieldDescr.setSetMethod("setLastName");
        lastNameFieldDescr.setGetMethod("getLastName");

        lastNameFieldDescr.setContainingClassDescriptor(this);
        lastNameFieldDescr.setIdentity(false);
        lastNameFM.setIdentity(false);
        lastNameFM.setDirect(false);
        lastNameFM.setName("lastName");
        lastNameFM.setRequired(true);
        lastNameFM.setSetMethod("setLastName");
        lastNameFM.setGetMethod("getLastName");
        Sql lastNameSql = new Sql();
        lastNameSql.addName("lastName");
        lastNameSql.setType("varchar");
        lastNameFM.setSql(lastNameSql);
        lastNameFM.setType("java.lang.String");
        choice.addFieldMapping(lastNameFM);

        setFields(new FieldDescriptor[] {firstNameFieldDescr,lastNameFieldDescr});
        setIdentities(new FieldDescriptor[] {ssnrFieldDescr});
    }

}
