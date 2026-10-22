package dev.yenot;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javazoom.jl.player.Player;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private ScheduledExecutorService scheduler;
    private List<ScheduledTrack> schedule;

    public Main() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        loadSchedule();
    }

    // 1. Читаем расписание из JSON
    private void loadSchedule() {
        try (InputStream is = getClass().getResourceAsStream("/schedule.json")) {
            if (is == null) {
                System.err.println("❌ Файл schedule.json не найден!");
                return;
            }

            // Gson превращает JSON в список объектов Java
            Gson gson = new Gson();
            //Type listType = new TypeToken<List<ScheduledTrack>>(){}.getType();
            //schedule = gson.fromJson(new InputStreamReader(is), listType);

            // 1. Читаем JSON в массив (Gson это умеет без TypeToken)
            ScheduledTrack[] array = gson.fromJson(new InputStreamReader(is), ScheduledTrack[].class);
            // 2. Превращаем массив в список (стандартная функция Java)
            schedule = Arrays.asList(array);


            System.out.println("✅ Загружено расписание: " + schedule.size() + " треков.");
        } catch (Exception e) {
            System.err.println("❌ Ошибка чтения JSON: " + e.getMessage());
        }
    }

    public void start() {
        System.out.println("🎵 Приложение запущено! Ожидаю времени воспроизведения...");
        System.out.println("Нажмите Enter, чтобы остановить программу.\n");

        // Запускаем проверку каждую минуту
        scheduler.scheduleAtFixedRate(this::checkAndPlay, 0, 1, TimeUnit.MINUTES);
    }

    // 2. Метод, который вызывается каждую минуту
    private void checkAndPlay() {
        int currentHour = LocalTime.now().getHour();
        int currentMinute = LocalTime.now().getMinute();

        // 1. Создаём список, куда будем складывать все подходящие треки
        List<ScheduledTrack> tracksToPlay = new ArrayList<>();

        // 2. Перебираем расписание
        for (ScheduledTrack t : schedule){
            if(t.hour() == currentHour && t.minute() == currentMinute){
                tracksToPlay.add(t);
            }
        }

        // 3. Если список не пустой, играем все треки по очереди
        if(!tracksToPlay.isEmpty()){
            System.out.println("⏰ Время пришло! Найдено треков для воспроизведения: " + tracksToPlay.size());

            for (ScheduledTrack track : tracksToPlay){
                System.out.println("▶ Играю: " + track.fileName());
                playAudio(track.fileName());
            }

            System.out.println("✅ Все запланированные треки на это время сыграны.\n");
        }


    }

    // 3. Метод воспроизведения
    private void playAudio(String fileName) {
        String resourcePath = "/audio/" + fileName;

        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("❌ Аудиофайл не найден: " + resourcePath);
                return;
            }
            Player player = new Player(is);
            player.play();
            System.out.println("✅ Воспроизведение завершено: " + fileName + "\n");
        } catch (Exception e) {
            System.err.println("❌ Ошибка воспроизведения: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.start();

        // Держим программу запущенной
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }

        app.scheduler.shutdownNow();
        System.out.println("🛑 Программа завершена.");
    }
}
