package com.ogray.glbot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;

public class AstroBot extends TelegramLongPollingBot {
    HashMap<Long, Talk> talks = new HashMap<Long, Talk>();
    private static final Logger log = LoggerFactory.getLogger(AstroBot.class);

    /**
     * Main entry for incoming message processing
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {

        if(update==null || update.getMessage()==null) {
            log.error("update.getMessage is null");
            return;
        }
        Long chatId = update.getMessage().getChatId();
        Talk talk = talks.get(chatId);
        if( talk==null ) {
            log.info("No talk for chatId="+chatId+", creating new one.");
            talk = new Talk(this, chatId);
            talks.put(chatId, talk);
        }
        talk.onUpdateReceived(update);
    }

    @Override
    public String getBotUsername() {
        return "AstroSolveBot";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    public void sendImage(String chatId, InputFile data, String caption) throws TelegramApiException {
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        sendDocumentRequest.setDocument(data);
        sendDocumentRequest.setCaption(caption);
        execute(sendDocumentRequest);
    }

    public void sendDocUploadingAFile(String chatId, java.io.File save, String caption) throws TelegramApiException {

        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        sendDocumentRequest.setDocument( new InputFile(save));
        sendDocumentRequest.setCaption(caption);
        execute(sendDocumentRequest);
    }
}
