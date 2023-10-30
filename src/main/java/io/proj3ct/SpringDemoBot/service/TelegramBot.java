package io.proj3ct.SpringDemoBot.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.vdurmont.emoji.EmojiParser;
import io.proj3ct.SpringDemoBot.config.BotConfig;
import io.proj3ct.SpringDemoBot.model.Joke;
import io.proj3ct.SpringDemoBot.model.JokeRepository;
import io.proj3ct.SpringDemoBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JokeRepository jokeRepository;
    @Autowired
    private BotConfig config;

    static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";

    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";
    static final String NEXT_JOKE="NEXT_JOKE";
    static final int MAX_JOKE_ID=19;
    static final String ERROR_TEXT = "Error occurred: ";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/joke", "get a joke"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));
        listofCommands.add(new BotCommand("/settings", "set your preferences"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

                switch (messageText) {
                    case "/start":
                        showStart(chatId, update.getMessage().getChat().getFirstName());
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            TypeFactory typeFactory = objectMapper.getTypeFactory();
                            List<Joke> jokeList = objectMapper.readValue(new File("db/stupidstuff.json"),
                                    typeFactory.constructCollectionType(List.class, Joke.class));
                            jokeRepository.saveAll(jokeList);
                        } catch (Exception e) {
                            log.error(Arrays.toString(e.getStackTrace()));
                        }
                        break;
                        case "/joke":
                       var joke=getRandomJoke();
                        joke.ifPresent(randomJoke -> AddButtonAndSendMessage(chatId,randomJoke.getBody()));
                        break;
                    default:

                        prepareAndSendMessage(chatId, "Sorry, command was not recognized");

                }
        }
        else if(update.hasCallbackQuery()){
            String callbackData=update.getCallbackQuery().getData();
            long chatId=update.getCallbackQuery().getMessage().getChatId();
            if(callbackData.equals(NEXT_JOKE)){
                var joke=getRandomJoke();
               // joke.ifPresent(randomJoke -> AddButtonAndSendMessage(chatId,randomJoke.getBody()));
                joke.ifPresent(randomJoke -> addButtonAndEditText(chatId,randomJoke.getBody(),update.getCallbackQuery().getMessage().getMessageId()));

            }
        }

    }
    private Optional<Joke> getRandomJoke(){
        var r=new Random();
        var randomId= r.nextInt(MAX_JOKE_ID)+1;//делаем так, чтобы генерировали значения от 1 до 52
        return jokeRepository.findById(randomId);//ищем шутки под этим айди
    }
    private void AddButtonAndSendMessage(long chatId,String joke){//сразу вызывается этот метод при первом нажатии /joke
        SendMessage message=new SendMessage();
        message.setText(joke);
        message.setChatId(chatId);
        InlineKeyboardMarkup markup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine=new ArrayList<>();
        List<InlineKeyboardButton> rowInLine=new ArrayList<>();
        var inLineKeyBoardButton=new InlineKeyboardButton();
        inLineKeyBoardButton.setCallbackData(NEXT_JOKE);
        inLineKeyBoardButton.setText(EmojiParser.parseToUnicode("next joke "+":rolling_on_the_floor_laughing:"));
        rowInLine.add(inLineKeyBoardButton);
        rowsInLine.add(rowInLine);
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);
        send(message);
    }
    private void addButtonAndEditText(long chatId,String joke,Integer messageId){//методдля обновление шуток в сообщении
        EditMessageText message=new EditMessageText();
        message.setChatId(chatId);
        message.setText(joke);
        message.setMessageId(messageId);

        InlineKeyboardMarkup markup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine=new ArrayList<>();
        List<InlineKeyboardButton> rowInLine=new ArrayList<>();
        var inLineKeyBoardButton=new InlineKeyboardButton();
        inLineKeyBoardButton.setCallbackData(NEXT_JOKE);
        inLineKeyBoardButton.setText(EmojiParser.parseToUnicode("next joke "+":rolling_on_the_floor_laughing:"));
        rowInLine.add(inLineKeyBoardButton);
        rowsInLine.add(rowInLine);
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);
        sendEditMessageText(message);

    }
    private void showStart(long chatId, String name) {


        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you!" + " :blush:");
        log.info("Replied to user " + name);


        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        send(message);
    }


    private void send(SendMessage message){//метод для отсылки шутки в новом сообщении
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }
    private void sendEditMessageText(EditMessageText message){//метод для отсылки шутки в уже существующем сообщении
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        send(message);
    }
}
