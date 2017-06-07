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
import java.util.concurrent.*;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.aroma.client.Priority;
import tech.aroma.thrift.application.service.ApplicationServiceConstants;
import tech.aroma.thrift.endpoint.TcpEndpoint;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.StringGenerators;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;

/**
 * @author SirWellington
 */
public class Main
{

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final int PORT = 9333;

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2);

    private static final String APP_TOKEN = "ec033ff6-af81-4678-8a79-a4c4aa8a37fc";

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

    private static final AlchemyGenerator<Priority> PRIORITIES = enumValueOf(Priority.class);

    private static final TcpEndpoint LOCAL = new TcpEndpoint().setHostname("localhost").setPort(7002);
    private static final TcpEndpoint PRODUCTION = ApplicationServiceConstants.PRODUCTION_ENDPOINT;
    private static final TcpEndpoint ENDPOINT = PRODUCTION;

    private static final Aroma AROMA = Aroma.Builder
            .create()
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
        openPortAt(PORT);

        EXECUTOR.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                sendMessage();
            }
        }, 2000, 2000, TimeUnit.MILLISECONDS);
    }

    private static void openPortAt(int port) throws IOException
    {
        LOG.info("Opening port at {}", port);

        final ServerSocket socket = new ServerSocket(port);

        EXECUTOR.submit(new Callable<Void>()
        {
            @Override
            public Void call() throws IOException
            {
                socket.accept();
                return null;
            }
        });

        LOG.info("Opened port at {}", port);

    }

    private static void sendMessage()
    {
        LOG.info("Sending Message");
        String title = one(TITLES);
        String randomMessage = one(alphabeticStrings(100));
        Priority urgency = one(PRIORITIES);

        AROMA.begin()
             .titled(title)
             .withBody(randomMessage)
             .withPriority(urgency)
             .send();
    }

}
