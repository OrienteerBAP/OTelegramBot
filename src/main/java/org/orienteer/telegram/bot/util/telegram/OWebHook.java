package org.orienteer.telegram.bot.util.telegram;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.WebhookBot;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Implementation of {@link IOWebHook}
 */
@Singleton
public class OWebHook implements IOWebHook {

    private String keystoreServerFile;
    private String keystoreServerPwd;
    private String internalUrl;
    private HttpServer server;

    @Inject
    private OWebHookRestApi restApi;

    @Override
    public void startServer() throws TelegramApiRequestException {
        if (server == null) {
            ResourceConfig rc = new ResourceConfig();
            rc.register(restApi);
            rc.register(JacksonFeature.class);

            if (keystoreServerFile != null && keystoreServerPwd != null) {
                SSLContextConfigurator sslContext = new SSLContextConfigurator();

                // set up security context
                sslContext.setKeyStoreFile(keystoreServerFile); // contains server keypair
                sslContext.setKeyStorePass(keystoreServerPwd);

                server = GrizzlyHttpServerFactory.createHttpServer(getBaseURI(), rc, true,
                        new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(false));
            } else {
                server = GrizzlyHttpServerFactory.createHttpServer(getBaseURI(), rc);
            }

            try {
                server.start();
            } catch (IOException e) {
                server = null;
                throw new TelegramApiRequestException("Error starting webhook server", e);
            }
        }
    }

    @Override
    public void stopServer() {
        if (isServerStarted()) {
            server.shutdown();
            server = null;
        }
    }

    @Override
    public boolean isServerStarted() {
        return server != null && server.isStarted();
    }

    @Override
    public void unregisterWebHook(WebhookBot bot) {
        restApi.unregisterCallback(bot);
        if (restApi.callbacksSize() == 0) {
            stopServer();
        }
    }

    @Override
    public void registerWebhook(WebhookBot callback) {
        restApi.registerCallback(callback);
    }

    @Override
    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }

    @Override
    public void setKeyStore(String keyStore, String keyStorePassword) throws TelegramApiRequestException {
        this.keystoreServerFile = keyStore;
        this.keystoreServerPwd = keyStorePassword;
        validateServerKeystoreFile(keyStore);
    }

    private URI getBaseURI() {
        return URI.create(internalUrl);
    }

    private void validateServerKeystoreFile(String keyStore) throws TelegramApiRequestException {
        File file = new File(keyStore);
        if (!file.exists() || !file.canRead()) {
            throw new TelegramApiRequestException("Can't find or access server keystore file.");
        }
    }


}
