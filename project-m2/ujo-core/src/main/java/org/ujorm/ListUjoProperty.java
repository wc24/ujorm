/*
 *  Copyright 2007-2010 Pavel Ponec
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

package org.ujorm;

import org.ujorm.core.annot.Immutable;

/**
 * A property metadata interface for value type of {@code List<ITEM>}.
 * @deprecated Use the interface {@link ListKey} rather
 * @author Pavel Ponec
 */
@Deprecated
@Immutable
public interface ListUjoProperty<UJO extends Ujo, ITEM> extends ListKey<UJO,ITEM> {


}