package io.proj3ct.SpringDemoBot.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.vdurmont.emoji.EmojiParser;
import io.proj3ct.SpringDemoBot.config.BotConfig;
import io.proj3ct.SpringDemoBot.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
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

    static final String USD = "/usd";
    static final String EUR = "/eur";
    static final String RUB = "/rub";
    static final String PLN = "/pln";
    static final String UAH = "/uah";
    static final String GBP = "/qbr";
    static final String CNY = "/cny";
    static final String KZT = "/kzt";
    static final String TRY = "/try";
    static  final String exchange="Курсы валют";
    static final String NEXT_JOKE="NEXT_JOKE";
    static final int MAX_JOKE_ID=19;
    static final String ERROR_TEXT = "Error occurred: ";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/joke", "get a joke"));
        listofCommands.add(new BotCommand("/weather","get a weather forecast"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));
        listofCommands.add(new BotCommand("/settings", "set your preferences"));
        listofCommands.add(new BotCommand("/usd","get a actual exchange rates"));
        listofCommands.add(new BotCommand("/eur","get a actual exchange rates"));
        listofCommands.add(new BotCommand("/rub","get a actual exchange rates"));
        listofCommands.add(new BotCommand("/pln","get a actual exchange rates"));
        listofCommands.add(new BotCommand("/uah","get a actual exchange rates"));
        listofCommands.add(new BotCommand("/gbp","get a actual exchange rates"));
        listofCommands.add(new BotCommand("/cny","get a actual exchange rates"));
        listofCommands.add(new BotCommand("/kzt","get a actual exchange rates"));
        listofCommands.add(new BotCommand("/try","get a actual exchange rates"));
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
                        registerUser(update.getMessage());
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
                    case "/help":
                        sendMessage(chatId,HELP_TEXT);
                        break;
                        case "/joke":
                       var joke=getRandomJoke();
                        joke.ifPresent(randomJoke -> AddButtonAndSendMessage(chatId,randomJoke.getBody()));
                        break;
                    case "/weather":
                        weartherForecast(chatId,"Minsk");
                        break;
                    case "/usd":
                        currencyExchange(chatId,"USD");
                        break;
                    case "/eur":
                        currencyExchange(chatId,"EUR");
                        break;
                    case "/rub":
                        currencyExchange(chatId,"RUB");
                        break;
                    case "/pln":
                        currencyExchange(chatId,"PLN");
                        break;
                    case "/uah":
                        currencyExchange(chatId,"UAH");
                        break;
                    case "/gbp":
                        currencyExchange(chatId,"GBP");
                        break;
                    case "/cny":
                        currencyExchange(chatId,"CNY");
                        break;
                    case "/kzt":
                        currencyExchange(chatId,"KZT");
                        break;
                    case "/try":
                        currencyExchange(chatId,"TRY");
                        break;
                    case "Курсы валют":
                        currencyExchange1(chatId,"Курс валют:");
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
            else if(callbackData.equals(USD)){
                var message="USD";
                currencyExchange(chatId,message);
            }
            else if(callbackData.equals(EUR)){
                var message="EUR";
                currencyExchange(chatId,message);
            }
            else if(callbackData.equals(RUB)){
                var message="RUB";
                currencyExchange(chatId,message);
            }
            else if(callbackData.equals(PLN)){
                var message="PLN";
                currencyExchange(chatId,message);
            }
            else if(callbackData.equals(UAH)){
                var message="UAH";
                currencyExchange(chatId,message);
            }
            else if(callbackData.equals(GBP)){
                var message="GBP";
                currencyExchange(chatId,message);
            }
            else if(callbackData.equals(CNY)){
                var message="CNY";
                currencyExchange(chatId,message);
            }
            else if(callbackData.equals(KZT)){
                var message="KZT";
                currencyExchange(chatId,message);
            }
            else if(callbackData.equals(TRY)){
                var message="TRY";
                currencyExchange(chatId,message);
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
        ReplyKeyboardMarkup keyboardMarkup=new ReplyKeyboardMarkup();//создали экранную клавиатуру
        List<KeyboardRow> keyboardRowList=new ArrayList<>();//создаем и добавляем иконки меню экранной клавиатуры
        KeyboardRow row=new KeyboardRow();//создали ряд
        row.add("weather");
        row.add("joke");
        row.add(exchange);
        keyboardRowList.add(row);//добавили ряд
        row=new KeyboardRow();
        row.add("register");
        row.add("my data");
        row.add("delete my data");
        keyboardRowList.add(row);
        keyboardMarkup.setKeyboard(keyboardRowList);
        message.setReplyMarkup(keyboardMarkup);
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
    private void currencyExchange(long chatId,String messageText){ //метод для обмена валют
        CurrencyModel currencyModel = new CurrencyModel();
        String currency = "";
        try {
            currency = CurrencyService.getCurrencyRate(messageText, currencyModel);

        } catch (IOException e) {
            sendMessage(chatId, "We have not found such a currency." + "\n" +
                    "Enter the currency whose official exchange rate" + "\n" +
                    "you want to know in relation to BYN." + "\n" +
                    "For example: USD");
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse date");
        }
         sendMessage(chatId, currency);
    }
    private void currencyExchange1(long chatId,String text){
        SendMessage message=new SendMessage();
        message.setText(text);
        message.setChatId(chatId);
        InlineKeyboardMarkup markupInLine=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine=new ArrayList<>();
        List<InlineKeyboardButton> rowInLine=new ArrayList<>();
        var usd=new InlineKeyboardButton();
        var eur=new InlineKeyboardButton();
        var rub=new InlineKeyboardButton();
        usd.setText("USD");
        usd.setCallbackData(USD);
        eur.setText("EUR");
        eur.setCallbackData(EUR);
        rub.setText("RUB");
        rub.setCallbackData(RUB);
        rowInLine.add(usd);
        rowInLine.add(eur);
        rowInLine.add(rub);
        rowsInLine.add(rowInLine);
        rowInLine=new ArrayList<>();
        var pln=new InlineKeyboardButton();
        var uah=new InlineKeyboardButton();
        var gbp=new InlineKeyboardButton();
        pln.setText("PLN");
        pln.setCallbackData(PLN);
        uah.setText("UAH");
        uah.setCallbackData(UAH);
        gbp.setText("GBP");
        gbp.setCallbackData(GBP);
        rowInLine.add(pln);
        rowInLine.add(uah);
        rowInLine.add(gbp);
        rowsInLine.add(rowInLine);
        rowInLine=new ArrayList<>();
        var cny=new InlineKeyboardButton();
        var kzt=new InlineKeyboardButton();
        var tru=new InlineKeyboardButton();
        cny.setText("CNY");
        cny.setCallbackData(CNY);
        kzt.setText("KZT");
        kzt.setCallbackData(KZT);
        tru.setText("TRY");
        tru.setCallbackData(TRY);
        rowInLine.add(cny);
        rowInLine.add(kzt);
        rowInLine.add(tru);
        rowsInLine.add(rowInLine);
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);
        send(message);
    }
    private void weartherForecast(long chatId,String messageText){ //метод для прогноза погоды
        WeartherModel weartherModel = new WeartherModel();
        String wearther = "";
        try {
            wearther = WeartherService.getWearther(messageText,weartherModel);

        } catch (IOException e) {
            sendMessage(chatId, "We have not found such a city." + "\n" +
                    "Enter the city" + "\n" +
                    "For example: Minsk");
        }
        sendMessage(chatId, wearther);
    }
    private void registerUser(Message msg){//метод для регистрации пользователей+ создание кнопок для данной колонки
        if(userRepository.findById(msg.getChatId()).isEmpty()){//проверка существует ли этотпользователь
            var chatId=msg.getChatId();
            var chat=msg.getChat();
            User user=new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setBio(chat.getBio());
            user.setDescription(chat.getDescription());
            user.setPinnedMessage(String.valueOf(chat.getPinnedMessage()));
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            log.info("user Saved "+user);
        }

    }
}
