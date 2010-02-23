/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.bean;

import org.ujoframework.Ujo;
import org.ujoframework.core.UjoManager;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.map.MapUjo;

public class Person extends MapUjo {
    
    public static final Property<Person, String>  NAME = newProperty("Name", String.class);
    public static final Property<Person, Boolean> MALE = newProperty("Male", Boolean.class);
    public static final Property<Person, Integer> AGE  = newProperty("Age" , Integer.class);

    // ---- A STANDARD BEAN IMPLEMENTATION ----
    
    private String  name;
    private Boolean male;
    private Integer age ;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getMale() {
        return male;
    }

    public void setMale(Boolean male) {
        this.male = male;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
    
    
    
    // ---- EXTENDED ----
    
    /** Equals */
    @Override
    public boolean equals(Object obj) {
        return UjoManager.getInstance().equalsUjo(this, (Ujo)obj );
    }
    
    /** Equals */
    @Override
    public Object clone(int depth, Object context) {
        return UjoManager.getInstance().clone(this, depth, context);
    }
    
    /**
     * Creates a new instance of UsePerson
     */
    public static Person createPerson() {
        
        Person person = new Person();
        
        // Writing:
        Person.NAME.setValue(person, "Pavel Ponec");
        Person.MALE.setValue(person, true);
        Person.AGE.setValue(person, 31);
        
        // Reading:
        String  name = Person.NAME.getValue(person);
        Boolean male = Person.MALE.getValue(person);
        Integer age  = Person.AGE .getValue(person);
        
        return person;
        
    }    
    
}