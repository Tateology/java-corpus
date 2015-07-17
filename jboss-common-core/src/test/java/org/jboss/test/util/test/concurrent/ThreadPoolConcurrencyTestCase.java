/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors as indicated
 * by the @authors tag.
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
 */

package org.jboss.test.util.test.concurrent;

import junit.framework.TestCase;

import org.jboss.util.threadpool.BasicThreadPool;
import org.jboss.util.threadpool.BlockingMode;

/**
 * @author baranowb
 *
 */
public class ThreadPoolConcurrencyTestCase extends TestCase{

    public void testConcurrency() throws Exception{
        BasicThreadPool pool = new BasicThreadPool();
        pool.setBlockingMode(BlockingMode.RUN);
        pool.setMaximumQueueSize(20);
        pool.setMaximumPoolSize(1);
        final long startTime = System.currentTimeMillis();
        final long destTime = startTime + 1000*30;
        for(;;){
            pool.run(new Runnable() {

             public void run() {
                
                 
             }
         }, 0, 1000);
            if(System.currentTimeMillis()>destTime){
                break;
            }
        }  
    }
}
