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

    private static final String APP_ID = "0d5e293c-cd82-4d29-b0e3-375ee05a9b39";
    private static final String APP_TOKEN = "b6f5bb7b-fb49-4845-932d-cbb259810d39";
    
    private static final AlchemyGenerator<String> TITLES = StringGenerators.stringsFromFixedList(
        "Network Meltdown",
        "New User",
        "User has deleted his account",
        "Databse Connection Timed Out",
        "Could not reach Authentication Service",
        "Filesystem is out of disk space",
        "Unauthozied Action");

    private static final AlchemyGenerator<Urgency> URGENCIES = enumValueOf(Urgency.class);

    private static final TcpEndpoint ENDPOINT = ApplicationServiceConstants.BETA_ENDPOINT;

    private static final Banana BANANA = Banana.newBuilder()
        .withEndpoint(ENDPOINT.hostname, ENDPOINT.port)
        .withApplicationToken(APP_TOKEN)
        .withAsyncExecutorService(Executors.newSingleThreadExecutor())
        .build();

    public static void main(String[] args) throws IOException, TException
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

}
