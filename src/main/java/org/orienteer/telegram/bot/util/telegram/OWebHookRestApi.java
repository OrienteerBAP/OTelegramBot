package org.orienteer.telegram.bot.util.telegram;

import com.google.inject.Singleton;
import org.apache.wicket.util.io.IClusterable;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiValidationException;
import org.telegram.telegrambots.generics.WebhookBot;
import org.telegram.telegrambots.logging.BotLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST API for handles WebHook requests from Telegram servers
 */
@Singleton
@Path(IOWebHook.REST_URL)
public class OWebHookRestApi implements IClusterable {
    private final ConcurrentHashMap<String, WebhookBot> callbacks;

    public OWebHookRestApi() {
        callbacks = new ConcurrentHashMap<>();
    }

    @POST
    @Path("/{botPath}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateReceived(@PathParam("botPath") String botPath, Update update) {
        if (callbacks.containsKey(botPath)) {
            try {
                BotApiMethod response = callbacks.get(botPath).onWebhookUpdateReceived(update);
                if (response != null) {
                    response.validate();
                }
                return Response.ok(response).build();
            } catch (TelegramApiValidationException e) {
                BotLogger.severe("RESTAPI", e);
                return Response.serverError().build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public void registerCallback(WebhookBot callback) {
        if (!callbacks.containsKey(callback.getBotPath())) {
            callbacks.put(callback.getBotPath(), callback);
        }
    }

    public void unregisterCallback(WebhookBot bot) {
        if (callbacks.containsKey(bot.getBotPath())) {
            callbacks.remove(bot.getBotPath());
        }
    }

    public int callbacksSize() {
        return callbacks.size();
    }

    public void clearCallbacks() {
        callbacks.clear();
    }
}