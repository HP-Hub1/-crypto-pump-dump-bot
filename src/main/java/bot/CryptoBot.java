package bot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.UserSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CryptoBot extends TelegramLongPollingBot {

    @Value("${telegram.token}")
    private String botToken;

    @Value("${telegram.username}")
    private String botUsername;

    @Getter
    private final Map<Long, UserSettings> userSettingsMap = new ConcurrentHashMap<>();

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        UserSettings settings = userSettingsMap.computeIfAbsent(chatId, id -> new UserSettings());

        if (text.startsWith("/setpair ")) {
            String pair = text.substring(9).toUpperCase();
            settings.setPair(pair);
            send(chatId, "Пара установлена на " + pair);
        } else if (text.startsWith("/setinterval ")) {
            try {
                int seconds = Integer.parseInt(text.substring(13));
                settings.setIntervalSeconds(seconds);
                send(chatId, "Интервал установлен на " + seconds + " сек.");
            } catch (NumberFormatException e) {
                send(chatId, "Некорректный формат интервала.");
            }
        } else if (text.startsWith("/setthreshold ")) {
            try {
                double threshold = Double.parseDouble(text.substring(14));
                settings.setPumpThresholdPercent(threshold);
                send(chatId, "Порог установлен на " + threshold + "%");
            } catch (NumberFormatException e) {
                send(chatId, "Некорректный формат порога.");
            }
        } else {
            send(chatId, "Доступные команды:\n" +
                    "/setpair BTCUSDT\n" +
                    "/setinterval 300\n" +
                    "/setthreshold 1.5");
        }
    }

    private void send(Long chatId, String text) {
        SendMessage msg = new SendMessage(chatId.toString(), text);
        try {
            execute(msg);
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

}