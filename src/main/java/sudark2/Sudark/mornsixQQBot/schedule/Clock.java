package sudark2.Sudark.mornsixQQBot.schedule;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static sudark2.Sudark.mornsixQQBot.FileManager.curfewTime;
import static sudark2.Sudark.mornsixQQBot.FileManager.loadNotice;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.*;

public class Clock {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static int emailCheckCounter = 0;

    public static void start() {
        long delay = computeInitialDelay();
        scheduler.scheduleAtFixedRate(Clock::tick, delay, 60, TimeUnit.SECONDS);
    }

    private static long computeInitialDelay() {
        LocalDateTime now = LocalDateTime.now();
        return 60 - now.getSecond();
    }

    private static void tick() {
        CompletableFuture.runAsync(sudark2.Sudark.mornsixQQBot.BiliDataSniffer.BiliChecker::check);

        emailCheckCounter++;
        if (emailCheckCounter >= 5) {
            emailCheckCounter = 0;
            CompletableFuture.runAsync(sudark2.Sudark.mornsixQQBot.EmailRelated.EmailChecker::checkEmails);
        }

        if (curfewTime[0] == curfewTime[2] && curfewTime[1] == curfewTime[3])
            return;

        int hour = LocalDateTime.now().getHour();
        int min = LocalDateTime.now().getMinute();

        if (hour == 19 && min == 0 && LocalDateTime.now().getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            broadNotice(loadNotice());
        }

        if (hour == curfewTime[0] && min == curfewTime[1]) {
            setB();
        }
        if (hour == curfewTime[2] && min == curfewTime[3]) {
            cancelB();
        }
    }
}
