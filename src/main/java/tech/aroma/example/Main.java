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
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.ProgrammingLanguage;
import tech.aroma.thrift.Tier;
import tech.aroma.thrift.application.service.ApplicationServiceConstants;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.endpoint.TcpEndpoint;
import tech.aroma.thrift.service.AromaService;
import tech.aroma.thrift.service.ProvisionApplicationRequest;
import tech.aroma.thrift.service.ProvisionApplicationResponse;
import tech.aroma.thrift.service.RegenerateApplicationTokenRequest;
import tech.aroma.thrift.service.RegenerateApplicationTokenResponse;
import tech.aroma.thrift.services.Clients;
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

    private static final String APP_ID = "5b7833c5-d3dc-4b6b-a29c-ba2a9dddba35";
    private static final String APP_TOKEN = "906f040f-54f2-4203-9923-36a6ceabbdd6";

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

//        UserToken userToken = signIn();
////        
//        Application app = createApplication(userToken);
//        LOG.info("Created Application: {}", app);
////        
//        ApplicationToken appToken = getAppTokenFor(userToken, app);
//        LOG.info("Got Application Token: {}", appToken);
    }

    private static void startApp() throws IOException
    {
        LOG.info("Opening port at {}", PORT);
        openPortAt(PORT);
        LOG.info("Opened port at {}", PORT);
        
        EXECUTOR.scheduleAtFixedRate(Main::sendMessage, 2000, 500, TimeUnit.MILLISECONDS);
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
            .titled(title)
            .text(randomMessage)
            .withUrgency(urgency)
            .send();
    }

    private static UserToken signIn() throws TTransportException, TException
    {
        return new UserToken()
            .setTokenId("53fbe000-f8ce-4301-a774-c384561c51b3");
    }

    private static Application createApplication(UserToken token) throws TException
    {
        ProvisionApplicationRequest request = new ProvisionApplicationRequest()
            .setToken(token)
            .setApplicationName("Aroma Example")
            .setProgrammingLanguage(ProgrammingLanguage.JAVA)
            .setTier(Tier.PAID)
            .setApplicationDescription("Example Aroma Application");

        AromaService.Client client = Clients.newAromaServiceClient();
        ProvisionApplicationResponse response = client.provisionApplication(request);

        return response.applicationInfo;
    }

    private static ApplicationToken getAppTokenFor(UserToken userToken, Application app) throws TException
    {
        RegenerateApplicationTokenRequest request = new RegenerateApplicationTokenRequest()
            .setToken(userToken)
            .setApplicationId(app.applicationId);

        AromaService.Client client = Clients.newAromaServiceClient();
        RegenerateApplicationTokenResponse response = client.regenerateToken(request);
        return response.applicationToken;
    }

    private static Application reuseApp()
    {
        return new Application()
            .setApplicationId(APP_ID)
            .setName("Aroma Example");
    }

}
