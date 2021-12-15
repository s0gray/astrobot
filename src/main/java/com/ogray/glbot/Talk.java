package com.ogray.glbot;

import com.ogray.glbot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;


public class Talk {
    private static final Logger log = LoggerFactory.getLogger(Talk.class);

    AstroBot bot;
    Long chatId;

    String rootDataFolder = "data/";


    enum BotState {
        IDLE,
        SET_SOURCE_SIZE, SET_SOURCE_TYPE, SET_IMAGE_SIZE_PX, SET_IMAGE_SIZE_RE, SET_NG, SET_M0, SET_GAMMA, SET_SIGMAC, INTRO
    }

    BotState state = BotState.IDLE;
    void setState(BotState s) {
        log.info("setState "+s+" "+this);
        this.state = s;
    }

    /**
     * Constructor
     * @param bot
     * @param chatId
     */
    public Talk(AstroBot bot, Long chatId) {
        this.bot = bot;
        this.chatId = chatId;

    }

    public void onUpdateReceived(Update update) {
        String input = update.getMessage().getText();
        log.info("+onMessage, state="+state+",["+input+"]");
        try {

            if(update.getMessage().getDocument()==null) {
                sendResponse2(update, "drop-files");
                return;
            }

            log.info("received mime-type: " + update.getMessage().getDocument().getMimeType());
          /*  if ("image/fits".compareTo(update.getMessage().getDocument().getMimeType()) != 0) {
                sendResponse2(update, "only-fits");
                return;
            }*/

            log.info("File name: " + update.getMessage().getDocument().getFileName());
            log.info("File  UniqueId: " + update.getMessage().getDocument().getFileUniqueId());

            String dstFolder = rootDataFolder + chatId;
            String finalFileName = null;
            if(!Utils.isFileFolderExists(dstFolder)) {
                log.info("Creating data folder " + dstFolder);
                Utils.mkdir(dstFolder);
            }

            if (!update.getMessage().hasDocument()) {
                sendResponse2(update, "drop-files");
                return;
            }

            String doc_id = update.getMessage().getDocument().getFileId();
            String doc_name = update.getMessage().getDocument().getFileName();
            String doc_mine = update.getMessage().getDocument().getMimeType();
            int doc_size = update.getMessage().getDocument().getFileSize();
            String getID = String.valueOf(update.getMessage().getFrom().getId());

            Document document = new Document();
            document.setMimeType(doc_mine);
            document.setFileName(doc_name);
            document.setFileSize(doc_size);
            document.setFileId(doc_id);

            GetFile getFile = new GetFile();
            getFile.setFileId(document.getFileId());
            try {
                org.telegram.telegrambots.meta.api.objects.File file = bot.execute(getFile);
                finalFileName = dstFolder +"/"+doc_name;
                bot.downloadFile(file, new File(finalFileName));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            log.info("Ready file name: "+ finalFileName);

            String cmd = "astap -f "+finalFileName+" -r 50 -d /home/ssa/astap/db";
            log.info("shell ["+cmd+"]");
            try {
                Process process = Runtime.getRuntime()
                        .exec(String.format(cmd));

              //  StreamGobbler streamGobbler =
              //          new StreamGobbler(process.getInputStream(), System.out::println);
             //   Executors.newSingleThreadExecutor().submit(streamGobbler);
                int exitCode = process.waitFor();
                log.info("exitCode " + exitCode);

                // find .wcs file
                String wcs = Utils.makeWcsFile(finalFileName);
                log.info("looking for .wcs file " + wcs);

                if(Utils.isFileFolderExists(wcs)) {
                    log.info("WCS found!");
                    sendResponse2(update, "ok-solved");
                    bot.sendDocUploadingAFile(""+chatId, new File(wcs), "info");
                } else {
                    log.info("WCS not found!");
                    sendResponse2(update, "not-solved");
                    return;
                }

            } catch (IOException e) {
               log.error("error "+ e.getMessage());
            } catch (InterruptedException e) {
                log.error("error "+ e.getMessage());

            }

            //    Long userId = update.getMessage().getFrom().getId();

            /* switch(input) {
                case "/render":
                    boss.render();
                    byte[] jpg = boss.getMap().field.getJPG();

                    bot.sendImage("" +chatId,
                            new InputFile( new ByteArrayInputStream(jpg), "image.jpg"), "image");

                    return;

                case "/setsigmac":
                    sendResponse2(update, "enter-value");
                    this.setState(BotState.SET_SIGMAC);
                    return;
            }*/

            switch(state) {

                default:
                    setState(BotState.INTRO);
                    sendResponse2(update, "intro");
                    return;

            }
          // sendResponse(update, "Hello !");


        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private int getIntValue(String input) {
        return Integer.parseInt(input);
    }

    private float getFloatValue(String input) throws NumberFormatException {
        return Float.parseFloat(input);
    }


    private int getIndexFromYes(String input) {
        String sub = input.substring( 1, input.length()-3);
   //     log.info("sub = "+sub);
        return Integer.parseInt(sub);
    }
    private int getIndexFromNo(String input) {
        String sub = input.substring( 1, input.length()-2);
    //    log.info("sub = "+sub);
        return Integer.parseInt(sub);
    }

    // is '/1YES
    private static boolean isYes(String input) {
        if(input.length()<4) return false;
        String sub = input.substring( input.length()-3);
        log.info("sub=["+sub+"]");
        if("YES".compareTo(sub)==0) return true;
        return false;
    }
    private static boolean isNo(String input) {
        if(input.length()<3) return false;
        String sub = input.substring( input.length()-2);
        log.info("sub=["+sub+"]");
        if("NO".compareTo(sub)==0) return true;
        return false;
    }


    void sendResponse(Update update, String text) throws TelegramApiException {
        SendMessage sendMessage = makeResponse(update);
        sendMessage.setText(text);
        bot.execute(sendMessage);
    }

    /**
     * Send response using key in .properties
     * @param update
     * @param key
     * @throws TelegramApiException
     */
    void sendResponse2(Update update, String key) throws TelegramApiException {
        SendMessage sendMessage = makeResponse(update);
        sendMessage.setText( Utils.getString(key) );
        bot.execute(sendMessage);
    }

    SendMessage makeResponse(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        return sendMessage;
    }


}
