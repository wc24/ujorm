/*
 * T001a_Test.java
 * JUnit based test
 *
 * Created on 8. �erven 2007, 23:42
 */

package org.ujoframework.implementation.xml.t001_simple;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.util.Date;
import junit.framework.*;
import org.ujoframework.MyTestCase;
import org.ujoframework.core.UjoManagerXML;

/**
 *
 * @author Pavel Ponec
 */
public class T001b_Test extends MyTestCase {
    
    public T001b_Test(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(T001b_Test.class);
        return suite;
    }
    
    public void test_01 () throws Exception {
        restoreXML(" Naz dar \nxxx ", true);
    }
    
    /**
     * Test of printProperties method, of class org.ujoframework.person.implementation.imlXML.XmlUjo.
     */
    public void restoreXML(String name, boolean printText) throws Exception {
        System.out.println( "restoreXML \"" + name + "\": " + suite().toString() );
        CharArrayWriter writer = new CharArrayWriter(256);
        //
        UPerson person = createPerson();
        person.NAME.setValue(person, name);
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        
        if (printText) {
           System.out.println("XML:\n" + writer.toString());
        }
        
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
        UPerson person2 = UjoManagerXML.getInstance().parseXML(is, UPerson.class, false);
        
        assertEquals(person, person2);
    }
    
    
    
    protected UPerson createPerson() {
        UPerson result = new UPerson();
        UPerson.NAME.setValue(result, "Pavel");
        UPerson.MALE.setValue(result,  true);
        UPerson.BIRTH.setValue(result, new Date());
        
        return result;
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
