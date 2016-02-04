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
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.client.Banana;
import tech.aroma.banana.client.Urgency;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.ProgrammingLanguage;
import tech.aroma.banana.thrift.Role;
import tech.aroma.banana.thrift.Tier;
import tech.aroma.banana.thrift.application.service.ApplicationServiceConstants;
import tech.aroma.banana.thrift.authentication.ApplicationToken;
import tech.aroma.banana.thrift.authentication.UserToken;
import tech.aroma.banana.thrift.endpoint.TcpEndpoint;
import tech.aroma.banana.thrift.service.BananaService;
import tech.aroma.banana.thrift.service.GetMessagesRequest;
import tech.aroma.banana.thrift.service.GetMessagesResponse;
import tech.aroma.banana.thrift.service.ProvisionApplicationRequest;
import tech.aroma.banana.thrift.service.ProvisionApplicationResponse;
import tech.aroma.banana.thrift.service.RegenerateApplicationTokenRequest;
import tech.aroma.banana.thrift.service.RegenerateApplicationTokenResponse;
import tech.aroma.banana.thrift.service.SignInRequest;
import tech.aroma.banana.thrift.service.SignInResponse;
import tech.aroma.banana.thrift.service.SignUpRequest;
import tech.aroma.banana.thrift.service.SignUpResponse;
import tech.aroma.banana.thrift.services.Clients;
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

    private static final String APP_ID = "37ec7417-7c6d-4359-b463-2f4d129a752f";
    private static final String APP_TOKEN = "1ed567e3-9264-4358-8c22-30deb02d3cab";

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
        "User Bought Item");

    private static final AlchemyGenerator<Urgency> URGENCIES = enumValueOf(Urgency.class);

    private static final TcpEndpoint ENDPOINT = ApplicationServiceConstants.BETA_ENDPOINT;

    private static final Banana BANANA = Banana.newBuilder()
        .withEndpoint(ENDPOINT.hostname, ENDPOINT.port)
        .withApplicationToken(APP_TOKEN)
        .withAsyncExecutorService(Executors.newSingleThreadExecutor())
        .build();

    public static void main(String[] args) throws IOException, TException
    {
        startApp();

//        UserToken userToken = signIn();
//        LOG.info("Created account and got user token: {}", userToken);
//        
//        Application app = reuseApp();//createApplication(userToken);
//        LOG.info("Created Application: {}", app);
//        
//        ApplicationToken appToken = getAppTokenFor(userToken, app);
//        LOG.info("Got Application Token: {}", appToken);
    }

    private static void startApp() throws IOException
    {
        LOG.info("Opening port at {}", PORT);
        openPortAt(PORT);

        LOG.info("Opened port at {}", PORT);
        EXECUTOR.scheduleAtFixedRate(Main::sendMessage, 1, 1, TimeUnit.SECONDS);
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

        BANANA.begin()
            .titled(title)
            .text(randomMessage)
            .withUrgency(urgency)
            .send();
    }

    private static UserToken createAccount() throws TTransportException, TException
    {
        SignUpRequest request = new SignUpRequest()
            .setEmail("jwellington.moreno@gmail.com")
            .setFirstName("Juan")
            .setMiddleName("Wellington")
            .setLastName("Moreno")
            .setMainRole(Role.DEVELOPER)
            .setUsername("sirwellington");

        BananaService.Client client = Clients.newBananaServiceClient();
        SignUpResponse response = client.signUp(request);

        return response.getUserToken();
    }

    private static UserToken signIn() throws TTransportException, TException
    {
        SignInRequest request = new SignInRequest()
            .setEmailAddress("jwellington.moreno@gmail.com");

        BananaService.Client client = Clients.newBananaServiceClient();

        SignInResponse response = client.signIn(request);
        return response.getUserToken();
    }

    private static Application createApplication(UserToken token) throws TException
    {
        ProvisionApplicationRequest request = new ProvisionApplicationRequest()
            .setToken(token)
            .setApplicationName("Banana Example")
            .setProgrammingLanguage(ProgrammingLanguage.JAVA)
            .setTier(Tier.PAID)
            .setApplicationDescription("Example Banana Application");

        BananaService.Client client = Clients.newBananaServiceClient();
        ProvisionApplicationResponse response = client.provisionApplication(request);

        return response.applicationInfo;
    }

    private static ApplicationToken getAppTokenFor(UserToken userToken, Application app) throws TException
    {
        RegenerateApplicationTokenRequest request = new RegenerateApplicationTokenRequest()
            .setToken(userToken)
            .setApplicationId(app.applicationId);

        BananaService.Client client = Clients.newBananaServiceClient();
        RegenerateApplicationTokenResponse response = client.regenerateToken(request);
        return response.applicationToken;
    }

    private static Application reuseApp()
    {
        return new Application()
            .setApplicationId(APP_ID)
            .setName("Banana Example");
    }

    private static void getMessages() throws TTransportException, TException
    {
        UserToken userToken = signIn();

        BananaService.Client client = Clients.newBananaServiceClient();

        GetMessagesRequest request = new GetMessagesRequest()
            .setApplicationId(APP_ID)
            .setLimit(1000)
            .setToken(userToken);

        GetMessagesResponse response = client.getMessages(request);
        LOG.info("Messages: {}", response.messages);
    }
}
