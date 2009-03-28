/*
 * T004a_Test.java
 * JUnit based test
 *
 * Created on 8. �erven 2007, 23:42
 */

package org.ujoframework.implementation.xml.t006_body;

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
public class T004c_Test extends MyTestCase {
    
    public T004c_Test(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(T004c_Test.class);
        return suite;
    }
    
    /**
     * Test of printProperties method, of class org.apache.person.implementation.imlXML.XmlUjo.
     */
    public void testPrintXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        CharArrayWriter writer = new CharArrayWriter(256);
        try {
            AtrPerson person = createPerson();
            // Serialization:
            UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
            
            System.out.println("XML==PERSON:\n" + writer.toString());
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Test of printProperties method, of class org.ujoframework.person.implementation.imlXML.XmlUjo.
     */
    public void testRestoreXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        CharArrayWriter writer = new CharArrayWriter(256);
        //
        AtrPerson person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
        AtrPerson person2 = UjoManagerXML.getInstance().parseXML(is, AtrPerson.class, false);
        
        assertEquals(person, person2);
    }
    
    
    /** Create persons with different times */
    protected AtrPerson createPerson() {
        AtrPerson result = createPersonOne();
        AtrPerson.MALE.setValue(result, null);
        AtrPerson.NAME.setValue(result, null);
        
        if (true) {
            AtrPerson.CHILDS.addItem(result, createPersonOne()); sleep(100);
            AtrPerson.CHILDS.addItem(result, createPersonOne()); sleep(100);
            AtrPerson.CHILDS.addItem(result, createPersonOne()); sleep(10);
        } else {
            AtrPerson.CHILDS.addItem(result, createPersonOne()); sleep(100);
            //AtrPerson.CHILDS.addItem(result, createPersonOne()); sleep(100);
            //AtrPerson.CHILDS.addItem(result, createPersonOne()); sleep(10);
            
        }
        return result;
    }
    
    protected AtrPerson createPersonOne() {
        AtrPerson result = new AtrPerson();
        AtrPerson.NAME.setValue(result, "Pavel");
        AtrPerson.MALE.setValue(result,  true);
        AtrPerson.BIRTH.setValue(result, new Date());
        
        return result;
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}