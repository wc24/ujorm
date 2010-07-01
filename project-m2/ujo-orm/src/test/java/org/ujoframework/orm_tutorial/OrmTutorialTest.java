/*
 *  Copyright 2009-2010 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujoframework.orm_tutorial;

import java.sql.Connection;
import junit.framework.TestCase;
import org.ujoframework.orm_tutorial.sample.SampleORM;


/**
 * Tutorial test
 * @author Pavel Ponec
 */
public class OrmTutorialTest extends TestCase {

    private static Connection conn = null;
    private static boolean noRunned = true;
    private static boolean noRunnedX = true;
    
    public OrmTutorialTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return OrmTutorialTest.class;
    }

    // ----------------------------------------------------

//    /**
//     * Test of getItemCount method, of class AbstractPropertyList.
//     */
//    public void testInit() throws Exception {
//
//        if (conn==null) {
//            Class.forName(new H2Dialect().getJdbcDriver());
//            conn = java.sql.DriverManager.getConnection
//                ( "jdbc:h2:mem:db1"
//                , "sa"
//                , ""
//                );
//            assertNotNull(conn);
//        }
//    }


    /**
     * Test of getItemCount method, of class AbstractPropertyList.
     */
    public void testTutorial() {

        if (noRunned) {
            noRunned = true;
            SampleORM.main(new String[]{});
        }
    }

    /**
     * Test of getItemCount method, of class AbstractPropertyList.
     */
//    public void testTutorialExtended() {
//
//        if (noRunnedX) {
//            noRunnedX = true;
//            org.ujoframework.orm_tutorial.xtended.XSampleORM.main(new String[]{});
//        }
//    }


    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}