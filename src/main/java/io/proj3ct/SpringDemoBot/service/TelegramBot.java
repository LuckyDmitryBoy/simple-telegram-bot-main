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
    static  final String EXCHANGE ="Курсы валют";
    static final String WEATHER="Прогноз погоды";
    static final String MINSK="/Minsk";
    static final String GOMEL="/Gomel";
    static final String GRODNO="/Grodno";
    static final String BREST="/Brest";
    static final String MOGILEV="/Mogilev";
    static final String VITEBSK="/Vitebsk";
    static final String BOBRUISK="/Bobruisk";
    static final String BARANOVICHI="/Baranovichi";
    static final String BORISOV="/Borisov";
    static final String PINSK="/Pinsk";
    static final String ORSHA="/Orsha";
    static final String MOZYR="/Mozyr";
    static final String NOVOPOLOTSK="/Novopolotsk";
    static final String SOLIGORSK="/Soligorsk";
    static final String LIDA="/Lida";
    static final String MOLODECHNO="/Molodechno";
    static final String POLOTSK="/Polotsk";
    static final String ZHLOBIN="/Zhlobin";
    static final String SVETLOGORSK="/Svetlogorsk";
    static final String RECHITSA="/Rechitsa";
    static final String ZHODINO="/Zhodino";
    static final String SLUTSK="/Slutsk";
    static final String KOBRIN="/Kobrin";
    static final String SLONIM="/Slonim";
    static final String VOLKOVYSK="/Volkovysk";
    static final String KALINKOVICHI="/Kalinkovichi";
    static final String SMORGON="/Smorgon";
    static final String ROGACHEV="/Rogachev";
    static final String OSIPOVICHI="/Osipovichi";
    static final String GORKI="/Gorki";
    static final String BEREZA="/Bereza";
    static final String NOVOGRUDOK="/Novogrudok";
    static final String VILEYKA="/Vileyka";
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
                        weartherForecast(chatId,"Минск");
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
                        currencyExchangeButtons(chatId,"Курс валют:");
                        break;
                    case "Прогноз погоды":
                        weartherForecastButtons(chatId, "Выберете Ваш населённый пункт");
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
            else if(callbackData.equals(MINSK)){
                var message="Minsk";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(GRODNO)){
                var message="Grodno";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(GOMEL)){
                var message="Gomel";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(MOGILEV)){
                var message="Mogilev";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(BREST)){
                var message="Brest";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(VITEBSK)){
                var message="Vitebsk";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(BOBRUISK)){
                var message="Babruysk";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(BARANOVICHI)){
                var message="Baranovichi";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(BORISOV)){
                var message="Borisov";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(PINSK)){
                var message="Pinsk";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(ORSHA)){
                var message="Orsha";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(MOZYR)){
                var message="Mazyr";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(NOVOPOLOTSK)){
                var message="Navapolatsk";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(SOLIGORSK)){
                var message="Salihorsk";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(LIDA)){
                var message="Lida";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(MOLODECHNO)){
                var message="Molodechno";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(POLOTSK)){
                var message="Polatsk";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(ZHLOBIN)){
                var message="Zhlobin";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(SVETLOGORSK)){
                var message="Svyetlahorsk";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(RECHITSA)){
                var message="Rechitsa";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(ZHODINO)){
                var message="Zhodzina";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(SLUTSK)){
                var message="Slutsk";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(KOBRIN)){
                var message="Kobrin";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(SLONIM)){
                var message="Slonim";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(VOLKOVYSK)){
                var message="Vawkavysk";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(KALINKOVICHI)){
                var message="Kalinkavichy";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(SMORGON)){
                var message="Smarhon";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(ROGACHEV)){
                var message="Rahachow";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(OSIPOVICHI)){
                var message="Asipovichy";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(GORKI)){
                var message="Gorki";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(BEREZA)){
                var message="Bereza";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(NOVOGRUDOK)){
                var message="Navahrudak";
                weartherForecast(chatId,message);
            }
            else if(callbackData.equals(VILEYKA)){
                var message="Vileyka";
                weartherForecast(chatId,message);
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
        row.add(WEATHER);
        row.add("joke");
        row.add(EXCHANGE);
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
    private void currencyExchangeButtons(long chatId,String text){
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
    private void weartherForecastButtons(long chatId,String text){
        SendMessage message=new SendMessage();
        message.setText(text);
        message.setChatId(chatId);
        InlineKeyboardMarkup markupInLine=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine=new ArrayList<>();
        List<InlineKeyboardButton> rowInLine=new ArrayList<>();

        var minsk=new InlineKeyboardButton();
        var gomel=new InlineKeyboardButton();
        var grodno=new InlineKeyboardButton();
        minsk.setText("Минск");
        gomel.setText("Гомель");
        grodno.setText("Гродно");
        minsk.setCallbackData(MINSK);
        gomel.setCallbackData(GOMEL);
        grodno.setCallbackData(GRODNO);
        rowInLine.add(minsk);
        rowInLine.add(gomel);
        rowInLine.add(grodno);
        rowsInLine.add(rowInLine);

        rowInLine=new ArrayList<>();

        var brest=new InlineKeyboardButton();
        var vitebsk=new InlineKeyboardButton();
        var mogilev=new InlineKeyboardButton();
        brest.setText("Брест");
        vitebsk.setText("Витебск");
        mogilev.setText("Могилёв");
        brest.setCallbackData(BREST);
        vitebsk.setCallbackData(VITEBSK);
        mogilev.setCallbackData(MOGILEV);
        rowInLine.add(brest);
        rowInLine.add(vitebsk);
        rowInLine.add(mogilev);
        rowsInLine.add(rowInLine);

        rowInLine=new ArrayList<>();

        var bobruisk=new InlineKeyboardButton();
        var baranovichi=new InlineKeyboardButton();
        var borisov=new InlineKeyboardButton();
        bobruisk.setText("Бобруйск");
        baranovichi.setText("Барановичи");
        borisov.setText("Борисов");
        bobruisk.setCallbackData(BOBRUISK);
        baranovichi.setCallbackData(BARANOVICHI);
        borisov.setCallbackData(BORISOV);
        rowInLine.add(bobruisk);
        rowInLine.add(baranovichi);
        rowInLine.add(borisov);
        rowsInLine.add(rowInLine);

        rowInLine=new ArrayList<>();

        var pinsk=new InlineKeyboardButton();
        var orsha=new InlineKeyboardButton();
        var mozyr=new InlineKeyboardButton();
        pinsk.setText("Пинск");
        orsha.setText("Орша");
        mozyr.setText("Мозырь");
        pinsk.setCallbackData(PINSK);
        orsha.setCallbackData(ORSHA);
        mozyr.setCallbackData(MOZYR);
        rowInLine.add(pinsk);
        rowInLine.add(orsha);
        rowInLine.add(mozyr);
        rowsInLine.add(rowInLine);

        rowInLine=new ArrayList<>();

        var novopolotsk=new InlineKeyboardButton();
        var soligorsk=new InlineKeyboardButton();
        var lida=new InlineKeyboardButton();
        novopolotsk.setText("Новополоцк");
        soligorsk.setText("Солигорск");
        lida.setText("Лида");
        novopolotsk.setCallbackData(NOVOPOLOTSK);
        soligorsk.setCallbackData(SOLIGORSK);
        lida.setCallbackData(LIDA);
        rowInLine.add(novopolotsk);
        rowInLine.add(soligorsk);
        rowInLine.add(lida);
        rowsInLine.add(rowInLine);

        rowInLine=new ArrayList<>();

        var molodechno=new InlineKeyboardButton();
        var polotsk=new InlineKeyboardButton();
        var zhlobin=new InlineKeyboardButton();
        molodechno.setText("Молодечно");
        polotsk.setText("Полоцк");
        zhlobin.setText("Жлобин");
        molodechno.setCallbackData(MOLODECHNO);
        polotsk.setCallbackData(POLOTSK);
        zhlobin.setCallbackData(ZHLOBIN);
        rowInLine.add(molodechno);
        rowInLine.add(polotsk);
        rowInLine.add(zhlobin);
        rowsInLine.add(rowInLine);

        rowInLine=new ArrayList<>();

        var svetlogorsk=new InlineKeyboardButton();
        var rechitsa=new InlineKeyboardButton();
        var zhodino=new InlineKeyboardButton();
        svetlogorsk.setText("Светлогорск");
        rechitsa.setText("Речица");
        zhodino.setText("Жодино");
        svetlogorsk.setCallbackData(SVETLOGORSK);
        rechitsa.setCallbackData(RECHITSA);
        zhodino.setCallbackData(ZHODINO);
        rowInLine.add(svetlogorsk);
        rowInLine.add(rechitsa);
        rowInLine.add(zhodino);
        rowsInLine.add(rowInLine);

        rowInLine=new ArrayList<>();

        var slutsk=new InlineKeyboardButton();
        var kobrin=new InlineKeyboardButton();
        var slonim=new InlineKeyboardButton();
        slutsk.setText("Слуцк");
        kobrin.setText("Кобрин");
        slonim.setText("Слоним");
        slutsk.setCallbackData(SLUTSK);
        kobrin.setCallbackData(KOBRIN);
        slonim.setCallbackData(SLONIM);
        rowInLine.add(slutsk);
        rowInLine.add(kobrin);
        rowInLine.add(slonim);
        rowsInLine.add(rowInLine);

        rowInLine=new ArrayList<>();

        var volkovysk=new InlineKeyboardButton();
        var kalinkovichi=new InlineKeyboardButton();
        var smorgon=new InlineKeyboardButton();
        volkovysk.setText("Волковыск");
        kalinkovichi.setText("Калинковичи");
        smorgon.setText("Сморгонь");
        volkovysk.setCallbackData(VOLKOVYSK);
        kalinkovichi.setCallbackData(KALINKOVICHI);
        smorgon.setCallbackData(SMORGON);
        rowInLine.add(volkovysk);
        rowInLine.add(kalinkovichi);
        rowInLine.add(smorgon);
        rowsInLine.add(rowInLine);

        rowInLine=new ArrayList<>();

        var rogachev=new InlineKeyboardButton();
        var osipovichi=new InlineKeyboardButton();
        var gorki=new InlineKeyboardButton();
        rogachev.setText("Рогачёв");
        osipovichi.setText("Осиповичи");
        gorki.setText("Горки");
        rogachev.setCallbackData(ROGACHEV);
        osipovichi.setCallbackData(OSIPOVICHI);
        gorki.setCallbackData(GORKI);
        rowInLine.add(rogachev);
        rowInLine.add(osipovichi);
        rowInLine.add(gorki);
        rowsInLine.add(rowInLine);

        rowInLine=new ArrayList<>();

        var bereza=new InlineKeyboardButton();
        var novogrudok=new InlineKeyboardButton();
        var vileyka=new InlineKeyboardButton();
        bereza.setText("Берёза");
        novogrudok.setText("Новогрудок");
        vileyka.setText("Вилейка");
        bereza.setCallbackData(BEREZA);
        novogrudok.setCallbackData(NOVOGRUDOK);
        vileyka.setCallbackData(VILEYKA);
        rowInLine.add(bereza);
        rowInLine.add(novogrudok);
        rowInLine.add(vileyka);
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
