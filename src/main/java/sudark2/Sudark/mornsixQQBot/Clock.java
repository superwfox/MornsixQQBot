package sudark2.Sudark.mornsixQQBot;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static sudark2.Sudark.mornsixQQBot.FileManager.curfewTime;
import static sudark2.Sudark.mornsixQQBot.OneBotClient.cancelB;
import static sudark2.Sudark.mornsixQQBot.OneBotClient.setB;

public class Clock {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void start() {
        long delay = computeInitialDelay();
        scheduler.scheduleAtFixedRate(Clock::tick, delay, 60, TimeUnit.SECONDS);
    }

    private static long computeInitialDelay() {
        LocalDateTime now = LocalDateTime.now();
        return 60 - now.getSecond();
    }

    private static void tick() {
        if (curfewTime[0] == curfewTime[2] && curfewTime[1] == curfewTime[3]) return;

        int hour = LocalDateTime.now().getHour();
        int min = LocalDateTime.now().getMinute();

        if (hour == curfewTime[0] && min == curfewTime[1]) {
            setB();
        }
        if (hour == curfewTime[2] && min == curfewTime[3]) {
            cancelB();
        }
    }
}
