/*
 * Copyright 2011 Wensheng Dou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: LockAction.java 8994 2011-08-01 23:40:59Z rjoachim $
 */
package org.exolab.castor.persist;

/**
 * LockAction is enum which show the action of lock.
 * 
 * @author <a href="mailto:wsdou55 AT gmail DOT com">Wensheng Dou</a>
 * @version $Revision: 8994 $ $Date: 2011-08-02 01:40:59 +0200 (Di, 02 Aug 2011) $
 */
public enum LockAction {
    /**
     * Action Read.
     */
    READ, 
    
    /**
     * Action Write.
     */
    WRITE, 
    
    /**
     * Action Create.
     */
    CREATE, 
    
    /**
     * Action Update.
     */
    UPDATE
}
