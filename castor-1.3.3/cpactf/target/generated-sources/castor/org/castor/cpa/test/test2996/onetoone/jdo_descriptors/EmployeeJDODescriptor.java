/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.castor.cpa.test.test2996.onetoone.jdo_descriptors;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.lang.reflect.Method;
import org.castor.core.exception.IllegalClassDescriptorInitialization;
import org.castor.cpa.test.test2996.onetoone.Employee;
import org.castor.jdo.engine.SQLTypeInfos;
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
 * Class EmployeeJDODescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class EmployeeJDODescriptor extends org.exolab.castor.mapping.loader.ClassDescriptorImpl {


      //----------------/
     //- Constructors -/
    //----------------/

    public EmployeeJDODescriptor() {
        super();
        ClassMapping mapping = new ClassMapping();
        ClassChoice choice = new ClassChoice();
        MapTo mapTo = new MapTo();

        addNature(ClassDescriptorJDONature.class.getName());
        ClassDescriptorJDONature jdoNature = new ClassDescriptorJDONature(this);
        jdoNature.setTableName("test2996_onetoone_employee");
        setJavaClass(Employee.class);
        jdoNature.setAccessMode(AccessMode.valueOf("shared"));
        jdoNature.addCacheParam("name", "org.castor.cpa.test.test2996.onetoone.Employee");

        mapping.setAccess(ClassMappingAccessType.fromValue("shared"));
        mapping.setAutoComplete(true);
        mapping.setName("org.castor.cpa.test.test2996.onetoone.Employee");
        mapping.setClassChoice(choice);
        mapTo.setTable("test2996_onetoone_employee");
        mapping.setMapTo(mapTo);
        setMapping(mapping);

        //id field
        String idFieldName = "id";
        FieldDescriptorImpl idFieldDescr;
        FieldMapping idFM = new FieldMapping();
        TypeInfo idType = new TypeInfo(java.lang.Long.class);
        // Set columns required (= not null)
        idType.setRequired(true);

        FieldHandler idHandler;
        try {
            Method idGetMethod = Employee.class.getMethod("getId", null);
            Method idSetMethod = Employee.class.getMethod("setId", new Class[]{
                long.class});

            idHandler = new FieldHandlerImpl(idFieldName, null, null,
                idGetMethod, idSetMethod, idType);

        } catch (SecurityException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (MappingException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (NoSuchMethodException e1) {
            throw new RuntimeException(e1.getMessage());
        }
        // Instantiate id field descriptor
        idFieldDescr = new FieldDescriptorImpl(idFieldName, idType,idHandler, false);
        idFieldDescr.addNature(FieldDescriptorJDONature.class.getName());
        FieldDescriptorJDONature idFieldJdoNature = new FieldDescriptorJDONature(idFieldDescr);
        idFieldJdoNature.setSQLName(new String[] { "id" });
        idFieldJdoNature.setSQLType(new int[] {SQLTypeInfos.javaType2sqlTypeNum(java.lang.Long.class) });
        idFieldJdoNature.setManyTable(null);
        idFieldJdoNature.setManyKey(new String[] {});
        idFieldJdoNature.setDirtyCheck(false);
        idFieldJdoNature.setReadOnly(false);

        idFieldDescr.setContainingClassDescriptor(this);
        idFieldDescr.setIdentity(true);
        idFM.setIdentity(true);
        idFM.setDirect(false);
        idFM.setName("id");
        idFM.setRequired(true);
        idFM.setSetMethod("setId");
        idFM.setGetMethod("getId");
        Sql idSql = new Sql();
        idSql.addName("id");
        idSql.setType("integer");
        idFM.setSql(idSql);
        idFM.setType("long");
        choice.addFieldMapping(idFM);

        //address field
        String addressFieldName = "address";
        String addressSqlName = "address_id";
        FieldDescriptorImpl addressFieldDescr;
        FieldMapping addressFM = new FieldMapping();
        TypeInfo addressType = new TypeInfo(org.castor.cpa.test.test2996.onetoone.Address.class);
        // Set columns required (= not null)
        addressType.setRequired(true);

        FieldHandler addressHandler;
        try {
            Method addressGetMethod = Employee.class.getMethod("getAddress", null);
            Method addressSetMethod = Employee.class.getMethod("setAddress", new Class[]{
                org.castor.cpa.test.test2996.onetoone.Address.class});

            addressHandler = new FieldHandlerImpl(addressFieldName, null, null,
                addressGetMethod, addressSetMethod, addressType);

        } catch (SecurityException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (MappingException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (NoSuchMethodException e1) {
            throw new RuntimeException(e1.getMessage());
        }
        // Instantiate address field descriptor
        addressFieldDescr = new FieldDescriptorImpl(addressFieldName, addressType,addressHandler, false);
        addressFieldDescr.addNature(FieldDescriptorJDONature.class.getName());
        FieldDescriptorJDONature addressFieldJdoNature = new FieldDescriptorJDONature(addressFieldDescr);
        addressFieldJdoNature.setSQLName(new String[] { addressSqlName });
        addressFieldJdoNature.setSQLType(new int[] {SQLTypeInfos.javaType2sqlTypeNum(org.castor.cpa.test.test2996.onetoone.Address.class) });
        addressFieldJdoNature.setManyKey(new String[] { addressSqlName });
        addressFieldJdoNature.setDirtyCheck(false);
        addressFieldJdoNature.setReadOnly(false);

        addressFieldDescr.setContainingClassDescriptor(this);
        addressFieldDescr.setClassDescriptor(new AddressJDODescriptor());
        addressFM.setIdentity(false);
        addressFM.setDirect(false);
        addressFM.setName("address");
        addressFM.setRequired(true);
        addressFM.setSetMethod("setAddress");
        addressFM.setGetMethod("getAddress");
        Sql addressSql = new Sql();
        addressSql.addName("address_id");
        addressSql.setManyKey(new String[] {"address_id"});
        addressFM.setSql(addressSql);
        addressFM.setType("org.castor.cpa.test.test2996.onetoone.Address");
        choice.addFieldMapping(addressFM);

        setFields(new FieldDescriptor[] {addressFieldDescr});
        setIdentities(new FieldDescriptor[] {idFieldDescr});
    }

}
