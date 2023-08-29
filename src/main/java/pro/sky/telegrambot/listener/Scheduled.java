package pro.sky.telegrambot.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Scheduled {
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    public List<NotificationTask> runTask(){
        return  notificationTaskRepository.findAll().stream()
                .filter(f->f.getLocalDateTime().equals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)))
                .collect(Collectors.toList());
    }
}
