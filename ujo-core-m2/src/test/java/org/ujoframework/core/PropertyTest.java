/*
 * UjoManagerTest.java
 * JUnit based test
 *
 * Created on 27. June 2007, 19:21
 */

package org.ujoframework.core;

import java.util.ArrayList;
import org.ujoframework.MyTestCase;
import org.ujoframework.extensions.PersonExt;
import static org.ujoframework.extensions.PersonExt.*;

/**
 *
 * @author Pavel Ponec
 */
public class PropertyTest extends MyTestCase {
    
    public PropertyTest(String testName) {
        super(testName);
    }
    
    private static Class suite() {
        return PropertyTest.class;
    }
    
    /**
     * Test of encodeBytes method, of class org.ujoframework.core.UjoManager.
     */
    public void testCopy() {

        PersonExt from = new PersonExt(1);
        PersonExt to = new PersonExt(2);

        PersonExt.ID.copy(from, to);
        assertSame(from.get(ID), to.get(ID));

        // ---

        from.set(PERS, new ArrayList<PersonExt>());
        PERS.copy(from, to);
        assertSame(from.get(PERS), to.get(PERS));
    }
    
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
    
}