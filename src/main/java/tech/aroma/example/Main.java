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

package tech.aroma.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.aroma.thrift.application.service.ApplicationServiceConstants;
import tech.aroma.thrift.endpoint.TcpEndpoint;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.StringGenerators;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;

/**
 *
 * @author SirWellington
 */
public class Main
{

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final int PORT = 9333;

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2);

    private static final String APP_TOKEN = "fc1d0e37-2061-458f-8488-855ceeca0289";

    private static final AlchemyGenerator<String> TITLES = StringGenerators.stringsFromFixedList(
        "App Crashed",
        "App Launched",
        "App Updated",
        "Network Issue",
        "New User",
        "Build Failed",
        "Token Expired",
        "Disk Full",
        "User Deleted Account",
        "Invalid Credit Card Used",
        "Database Time Out",
        "Could not reach Authentication Service",
        "Could not reach Twilio",
        "Unauthorized Action",
        "Service Redeployed",
        "Database Query Failed",
        "Device Lost Connection",
        "Battery Low",
        "Alarm Triggered",
        "User Bought Item");

    private static final AlchemyGenerator<Urgency> URGENCIES = enumValueOf(Urgency.class);

    private static final TcpEndpoint ENDPOINT = ApplicationServiceConstants.BETA_ENDPOINT;

    private static final Aroma AROMA = Aroma.newBuilder()
        .withEndpoint(ENDPOINT.hostname, ENDPOINT.port)
        .withApplicationToken(APP_TOKEN)
        .withAsyncExecutorService(Executors.newSingleThreadExecutor())
        .build();

    public static void main(String[] args) throws IOException, TException
    {
        startApp();
    }

    private static void startApp() throws IOException
    {
        LOG.info("Opening port at {}", PORT);
       
        openPortAt(PORT);
       
        LOG.info("Opened port at {}", PORT);
        
        EXECUTOR.scheduleAtFixedRate(Main::sendMessage, 2000, 2000, TimeUnit.MILLISECONDS);
    }

    private static void openPortAt(int port) throws IOException
    {
        ServerSocket socket = new ServerSocket(port);
        EXECUTOR.submit(() -> socket.accept());
    }

    private static void sendMessage()
    {
        LOG.info("Sending Message");
        String title = one(TITLES);
        String randomMessage = one(alphabeticString(100));
        Urgency urgency = one(URGENCIES);

        AROMA.begin()
            .titled("Yo!!!")
            .text(randomMessage)
            .withUrgency(urgency)
            .send();
    }

}
