/*
 * Copyright 2016 Aroma Tech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 
package tech.aroma.banana.example;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.client.Banana;
import tech.aroma.banana.client.Urgency;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.StringGenerators;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;

/**
 *
 * @author SirWellington
 */
public class Main 
{
    private final static Logger LOG = LoggerFactory.getLogger(Main.class);
    
    private static final int PORT = 9333;
    
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2);
    
    private static AlchemyGenerator<String> MESSAGES = StringGenerators.stringsFromFixedList("Something really bad has happened",
                                                                                            "New User just signed up",
                                                                                            "User has deleted his account",
                                                                                            "Databse conneciton timed out",
                                                                                            "Could not reach Authentication Service",
                                                                                            "Filesystem is out of disk space",
                                                                                            "Unauthozied Action done by Bob");
    
    private static final AlchemyGenerator<Urgency> URGENCIES = enumValueOf(Urgency.class);
    
    private static final Banana BANANA = Banana.create();
    
    public static void main(String[] args) throws IOException
    {
        LOG.info("Opening port at {}", PORT);
        openPortAt(PORT);
        LOG.info("Opened port at {}", PORT);
        EXECUTOR.scheduleAtFixedRate(Main::sendMessage, 1, 5, TimeUnit.SECONDS);
    }

    private static void openPortAt(int port) throws IOException
    {
        ServerSocket socket = new ServerSocket(port);
        EXECUTOR.submit(() -> socket.accept());
    }
    
    private static void sendMessage()
    {
        LOG.info("Sending Message");
        String message = one(MESSAGES);
        Urgency urgency = one(URGENCIES);
        
        BANANA.begin()
            .message(message)
            .withUrgency(urgency)
            .send();
    }
    
}
