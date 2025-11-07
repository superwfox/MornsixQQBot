package sudark2.Sudark.mornsixQQBot;


import java.io.*;
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
    static int[] curfewTime = new int[4];
    static Set<String> Regex = new HashSet<>();
    static File superUsers = new File(FileFolder, "superUsers.txt");
    static File groupList = new File(FileFolder, "groups.txt");
    static File shutLogs = new File(FileFolder, "shutLogs.csv");
    static File curfewFile = new File(FileFolder, "curfew.txt");
    static File regexFile = new File(FileFolder, "prefix.txt");

    public static void initFiles() {
        FileManager fileManager = new FileManager();
        fileManager.checkFile(fileManager.superUsers.getAbsolutePath());
        fileManager.checkFile(fileManager.shutLogs.getAbsolutePath());
        fileManager.checkFile(fileManager.groupList.getAbsolutePath());
        fileManager.checkFile(fileManager.curfewFile.getAbsolutePath());

        users = readSuperUsers();
        loadGroupList();
        loadCurfew();
        loadRegex();
    }

    public static Set<String> readSuperUsers() {
        Set<String> initialSet = new HashSet<>();
        initialSet.add("2963502563");
        initialSet.add("3364200181");

        try (BufferedReader r = new BufferedReader(new FileReader(superUsers))) {
            String line = r.readLine();
            if (line == null || line.isEmpty()) return new HashSet<>();
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
            String[] groups = line.split("//|");

            if (line == null || line.isEmpty()) {
                users.forEach(id -> sendP(id, "尚未设置群聊"));
                return;
            }
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


    public static List<String> loadRegex() {
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
        return regexList;
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
