/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml;

import java.util.ArrayList;
import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.map.MapUjo;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class XmlUjoRoot_2 extends MapUjo  {
    
    public static final UjoProperty<XmlUjoRoot_2,Object[]>  PRO_P4 = newProperty("ARRAY_OBJ", Object[].class);
    public static final UjoProperty<XmlUjoRoot_2,ArrayList> PRO_P5 = newProperty("LIST", ArrayList.class);
    
    
}
