/*
 *  Copyright 2007 Paul Ponec
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

package org.ujoframework.extensions;

/**
 * A special interface for a terminology only, an implementation is not reasnonable in the UJO Framework.
 * PropertyTextable is every one object, wich have got implemented method toString() so that the result can be used
 * to restore a new equal object by its a single constructor parameter type of String.
 * <br>This is a "PropertyTextable" test for an <strong>Integer</strong> class:
 * <pre class="pre">
 *  Integer textable1 = <span class="java-keywords">new</span> <span class="java-layer-method">Integer</span>(<span class="java-numeric-literals">7</span>);
 *  Integer textable2 = <span class="java-keywords">new</span> <span class="java-layer-method">Integer</span>(textable1.<span class="java-layer-method">toString()</span>);
 *  <span class="java-keywords">boolean</span> result = textable1.<span class="java-layer-method">equals</span>(textable2);
 * </pre>
 * <br>Some completed PropertyTextable classes from a Java API are
 * <ul>
 *   <li>Boolean</li>
 *   <li>Short</li>
 *   <li>Integer</li>
 *   <li>Long</li>
 *   <li>Float</li>
 *   <li>Double</li>
 *   <li>String</li>
 * </ul>
 * These classes have <strong>NOT</strong> behaviour of PropertyTextable, <br>however UJO Framework supports these types similar like PropertyTextable:
 * <ul>
 *   <li>Byte</li>
 *   <li>Character</li>
 *   <li>java.util.Date</li>
 *   <li>byte[]</li>
 *   <li>char[]</li>
 *   <li>Locale</li>
 *   <li>Color</li>
 *   <li>Dimension</li>
 *   <li>Rectangle</li>
 *   <li>Enum</li>
 *   <li>Class</li>
 *   <li>Charset</li>
 * </ul>
 * 
 * 
 * @author Pavel Ponec
 * @see UjoTextable
 */
public interface PropertyTextable {
    
    /** A result must be acceptable for one constructor parameter (of the same class) to restore an equal object. */
    @Override
    public String toString();
    
}
