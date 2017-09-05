package org.orienteer.telegram.bot.handler;

/**
 * Contains config for {@link OTelegramWebHookHandler}
 */
public class WebHookHandlerConfig {
    public final String username;
    public final String token;
    public final String externalWebHookUrl;
    public final String internalWebHookUrl;
    public final String pathToCertificatePublicKey;
    public final String pathToCertificateStore;
    public final String certificateStorePassword;
    public final long userSession;


    public WebHookHandlerConfig(String username, String token, String webHookHost, long port, long userSession, String pathToCertificatePublicKey, String pathToCertificateStore, String password) {
        this.username = username;
        this.token = token;
        this.externalWebHookUrl = webHookHost + ":" + port;
        this.internalWebHookUrl = webHookHost + ":" + port;
        this.pathToCertificatePublicKey = pathToCertificatePublicKey;
        this.pathToCertificateStore = pathToCertificateStore;
        this.certificateStorePassword = password;
        this.userSession = userSession;
    }

}
