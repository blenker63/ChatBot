package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @Autowired
    private pro.sky.telegrambot.listener.Scheduled scheduled;
    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message().text().equals("/start")) {
                SendMessage text = new SendMessage(update.message().chat().id(),
                        "Привет " + update.message().from().firstName() + ". Добро пожаловать в чат!");
                SendResponse sendResponse = telegramBot.execute(text);
            } else {
                String offers = update.message().text();
                Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");
                Matcher matcher = pattern.matcher(offers);
                if (matcher.matches()) {
                    LocalDateTime time = LocalDateTime.parse(matcher.group(1),
                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    String answer = matcher.group(3);
                    NotificationTask notificationTask = new NotificationTask();
                    notificationTask.setChatId(update.message().chat().id());
                    notificationTask.setMessageText(answer);
                    notificationTask.setLocalDateTime(time);
                    notificationTaskRepository.save(notificationTask);
                }
            }

            // Process your updates here
        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void run() {
        scheduled.runTask()
                .forEach(f->{
                    SendMessage sendMessage = new SendMessage(f.getChatId(),f.getMessageText());
                    SendResponse sendResponse = telegramBot.execute(sendMessage);
                });
    }
}
