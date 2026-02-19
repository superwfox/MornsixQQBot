package sudark2.Sudark.mornsixQQBot;


import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.get;
import static sudark2.Sudark.mornsixQQBot.OneBotClient.sendP;

public class FileManager {

    static File FileFolder = get().getDataFolder();
    static Set<String> users = new HashSet<>();
    static String QQGroup = "";
    static String ManagerGroup = "";
    static String BotQQ = "3101965697";
    static int[] curfewTime = new int[4];
    static Set<String> Regex = new HashSet<>();
    static Set<String> mice = new HashSet<>();
    static File superUsers = new File(FileFolder, "superUsers.txt");
    static File groupList = new File(FileFolder, "groups.txt");
    static File shutLogs = new File(FileFolder, "shutLogs.csv");
    static File curfewFile = new File(FileFolder, "curfew.txt");
    static File regexFile = new File(FileFolder, "regex.txt");
    static File noticeFile = new File(FileFolder, "notice.txt");
    static File miceFile = new File(FileFolder, "mice.txt");

    public static void initFiles() {

        if (!FileFolder.exists()) FileFolder.mkdirs();

        FileManager fileManager = new FileManager();
        fileManager.checkFile(fileManager.superUsers.getAbsolutePath());
        fileManager.checkFile(fileManager.shutLogs.getAbsolutePath());
        fileManager.checkFile(fileManager.groupList.getAbsolutePath());
        fileManager.checkFile(fileManager.curfewFile.getAbsolutePath());
        fileManager.checkFile(fileManager.regexFile.getAbsolutePath());
        fileManager.checkFile(fileManager.noticeFile.getAbsolutePath());
        fileManager.checkFile(fileManager.miceFile.getAbsolutePath());

        users = readSuperUsers();
        loadGroupList();
        loadCurfew();
        loadRegex();
        loadMice();
    }

    public static Set<String> loadMice() {
        Set<String> initialSet = new HashSet<>();

        try (BufferedReader r = new BufferedReader(new FileReader(miceFile))) {
            String line = r.readLine();
            if (line == null || line.isEmpty()) return initialSet;
            return new HashSet<>(Arrays.asList(line.split("\\|")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return initialSet;
    }

    public static void writeMice(Set<String> ids) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(miceFile))) {
            w.write(String.join("|", ids));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadNotice() {
        try {
            return Files.readString(noticeFile.toPath());
        } catch (IOException e) {
            e.printStackTrace(); // 或者用日志打印
            return "无"; // 文件无法读取时返回空字符串
        }
    }

    public static void writeNotice(String notice) {
        try {
            Files.writeString(noticeFile.toPath(), notice);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Set<String> readSuperUsers() {
        Set<String> initialSet = new HashSet<>();

        try (BufferedReader r = new BufferedReader(new FileReader(superUsers))) {
            String line = r.readLine();
            if (line == null || line.isEmpty()) return initialSet;
            return new HashSet<>(Arrays.asList(line.split("\\|")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return initialSet;
    }

    public static void writeSuperUsers(Set<String> ids) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(superUsers))) {
            w.write(String.join("|", ids));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadGroupList() {
        try (BufferedReader r = new BufferedReader(new FileReader(groupList))) {
            String line = r.readLine();

            if (line == null || line.isEmpty()) {
                users.forEach(id -> sendP(id, "尚未设置群聊"));
                return;
            }

            String[] groups = line.split("\\|");

            if (groups.length < 2) {
                users.forEach(id -> sendP(id, "还有一个群聊需要设置"));
                return;
            }

            QQGroup = groups[0];
            ManagerGroup = groups[1];
        } catch (IOException e) {
            e.printStackTrace();
            users.forEach(id -> sendP(id, "读取群聊失败"));
        }
    }


    public static void loadCurfew() {
        try (BufferedReader r = new BufferedReader(new FileReader(curfewFile))) {
            String line = r.readLine();
            if (line == null || line.isEmpty()) {
                curfewTime[0] = curfewTime[1] = curfewTime[2] = curfewTime[3] = 0;
                return;
            }
            String[] times = line.split(",");
            curfewTime[0] = Integer.parseInt(times[0]);
            curfewTime[1] = Integer.parseInt(times[1]);
            curfewTime[2] = Integer.parseInt(times[2]);
            curfewTime[3] = Integer.parseInt(times[3]);
        } catch (IOException e) {
            e.printStackTrace();
            users.forEach(id -> sendP(id, "读取禁言时间失败"));
        }
    }

    public static void writeCurfew(int[] times) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(curfewFile))) {
            String line = Arrays.stream(times)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(","));
            w.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<String[]> readShutLogs() {
        List<String[]> args = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(shutLogs))) {
            String line = r.readLine();
            while (line != null) {
                args.add(line.split(","));
                line = r.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            users.forEach(id -> sendP(id, "读取禁言记录失败"));
        }
        return args;
    }

    public static void writeShutLogs(List<String[]> args) {
        writeShutLogs(args, 0);
    }

    private static void writeShutLogs(List<String[]> args, int retry) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(shutLogs))) {
            for (String[] arg : args) {
                w.write(String.join(",", arg));
                w.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            users.forEach(id -> sendP(id, "写入禁言记录失败 5秒后重试"));

            if (retry < 5) { // 最大重试 5 次
                int nextRetry = retry + 1;
                CompletableFuture.runAsync(() -> {
                    writeShutLogs(args, nextRetry);
                }, CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS));
            } else {
                System.out.println("写入失败，已达到最大重试次数");
            }
        }
    }


    public static void loadRegex() {
        List<String> regexList = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(regexFile))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.isBlank()) {
                    regexList.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Regex = new HashSet<>(regexList);
    }

    public static void writeRegexList() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(regexFile))) {
            for (String regex : Regex) {
                bw.write(regex);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void writeGroupList(String[] ids) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(groupList))) {
            w.write(String.join("|", ids));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


}
