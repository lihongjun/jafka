/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sohu.jafka.network;


import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.sohu.jafka.utils.Closer;

/**
 * @author adyliu (imxylz@gmail.com)
 * @since 2012-4-6
 */
public abstract class AbstractServerThread implements Runnable {

    private Selector selector;
    protected final CountDownLatch startupLatch = new CountDownLatch(1);
    protected final CountDownLatch shutdownLatch = new CountDownLatch(1);
    protected final AtomicBoolean alive = new AtomicBoolean(false);
    
    final protected Logger logger = Logger.getLogger(getClass());
    /**
     * @return the selector
     */
    public Selector getSelector() {
        if (selector == null) {
            try {
                selector = Selector.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return selector;
    }
    
    protected void closeSelector() {
        Closer.closeQuietly(selector,logger);
    }
    
    public void shutdown() throws InterruptedException {
        alive.set(false);
        selector.wakeup();
        shutdownLatch.await();
    }
    
    protected void startupComplete() {
        alive.set(true);
        startupLatch.countDown();
    }
    
    protected void shutdownComplete() {
        shutdownLatch.countDown();
    }

    protected boolean isRunning() {
        return alive.get();
    }
    
    public void awaitStartup() throws InterruptedException {
        startupLatch.await();
    }
}