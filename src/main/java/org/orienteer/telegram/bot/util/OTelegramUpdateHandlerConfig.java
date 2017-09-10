package org.orienteer.telegram.bot.util;

import org.apache.wicket.util.io.IClusterable;

/**
 * Class which contains information for {@link org.telegram.telegrambots.generics.LongPollingBot} and {@link org.telegram.telegrambots.generics.WebhookBot}
 */
public class OTelegramUpdateHandlerConfig implements IClusterable{
    private final String username;
    private final String token;
    private final String externalUrl;
    private final String internalUrl;
    private final String pathToPublicKey;
    private final String pathToCertificateStore;
    private final String certificateStorePassword;
    private final long userSession;
    private final int port;

    public OTelegramUpdateHandlerConfig(String username, String token, long userSession) {
        this(username, token, null, null, -1, userSession, null, null, null);
    }

    public OTelegramUpdateHandlerConfig(String username, String token, String externalUrl, String internalUrl, int port, long userSession) {
        this(username, token, externalUrl, internalUrl, port, userSession, null, null, null);
    }

    public OTelegramUpdateHandlerConfig(String username, String token, String externalUrl, String internalUrl, int port, long userSession,
                                        String pathToPublicKey, String pathToCertificateStore, String password) {
        this.username = username;
        this.token = token;
        this.externalUrl = externalUrl;
        this.internalUrl = internalUrl;
        this.pathToPublicKey = pathToPublicKey;
        this.pathToCertificateStore = pathToCertificateStore;
        this.certificateStorePassword = password;
        this.userSession = userSession;
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public String getInternalUrl() {
        return internalUrl;
    }

    public String getPathToPublicKey() {
        return pathToPublicKey;
    }

    public String getPathToCertificateStore() {
        return pathToCertificateStore;
    }

    public String getCertificateStorePassword() {
        return certificateStorePassword;
    }

    public long getUserSession() {
        return userSession;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "OTelegramUpdateHandlerConfig{" +
                "username='" + username + '\'' +
                ", token='" + token + '\'' +
                ", externalUrl='" + externalUrl + '\'' +
                ", internalUrl='" + internalUrl + '\'' +
                ", pathToPublicKey='" + pathToPublicKey + '\'' +
                ", pathToCertificateStore='" + pathToCertificateStore + '\'' +
                ", certificateStorePassword='" + certificateStorePassword + '\'' +
                ", userSession=" + userSession +
                ", port=" + port +
                '}';
    }
}
