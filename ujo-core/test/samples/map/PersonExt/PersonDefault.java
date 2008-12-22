/*
 * Person.java
 *
 * Created on 9. �erven 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.map.PersonExt;

import org.ujoframework.implementation.map.*;
public class PersonDefault extends MapUjoExt<PersonDefault> {
    
  public static final MapProperty<PersonDefault, String > NAME = newProperty("Name", String.class).setDefault("");
  public static final MapProperty<PersonDefault, Boolean> MALE = newProperty("Male", true);
  public static final MapProperty<PersonDefault, Double > CASH = newProperty("Cash", 0d);
    
  public void addCash(double cash) {
    //double newPrice2 = get(Person.CASH) + cash;
    double newPrice = get(CASH) + cash;
    set(CASH, newPrice);
  }
}
