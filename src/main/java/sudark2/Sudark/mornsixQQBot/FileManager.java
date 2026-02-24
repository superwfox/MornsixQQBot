package sudark2.Sudark.mornsixQQBot;

import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.get;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.sendP;

public class FileManager {

    static File FileFolder = get().getDataFolder();
    public static Set<String> users = new HashSet<>();
    public static String QQGroup = "";
    public static String ManagerGroup = "";
    public static String MsgStoreGroup = "1064467046";
    public static String BotQQ = "3101965697";
    public static int[] curfewTime = new int[4];
    public static Set<String> Regex = new HashSet<>();
    public static Set<String> mice = new HashSet<>();
    public static Set<String> biliUids = new HashSet<>();
    static File superUsers = new File(FileFolder, "superUsers.txt");
    static File groupList = new File(FileFolder, "groups.txt");
    public static File shutLogs = new File(FileFolder, "shutLogs.csv");
    static File curfewFile = new File(FileFolder, "curfew.txt");
    static File regexFile = new File(FileFolder, "regex.txt");
    static File noticeFile = new File(FileFolder, "notice.txt");
    static File miceFile = new File(FileFolder, "mice.txt");
    static File biliUidsFile = new File(FileFolder, "biliUids.txt");

    public static void initFiles() {
        if (!FileFolder.exists())
            FileFolder.mkdirs();

        checkFile(superUsers);
        checkFile(shutLogs);
        checkFile(groupList);
        checkFile(curfewFile);
        checkFile(regexFile);
        checkFile(noticeFile);
        checkFile(miceFile);
        checkFile(biliUidsFile);

        users = readSuperUsers();
        loadGroupList();
        loadCurfew();
        loadRegex();
        mice = loadMice();
        biliUids = loadBiliUids();
    }

    public static Set<String> loadMice() {
        return readPipeSet(miceFile, "§7读取黑名单失败");
    }

    public static void writeMice(Set<String> ids) {
        writePipeSet(miceFile, ids, "§7写入黑名单失败");
    }

    public static Set<String> loadBiliUids() {
        return readPipeSet(biliUidsFile, "§7读取B站UID列表失败");
    }

    public static void writeBiliUids() {
        writePipeSet(biliUidsFile, biliUids, "§7写入B站UID列表失败");
    }

    public static String loadNotice() {
        try {
            return Files.readString(noticeFile.toPath());
        } catch (IOException e) {
            warn("§7读取公告失败");
            return "无";
        }
    }

    public static void writeNotice(String notice) {
        try {
            Files.writeString(noticeFile.toPath(), notice);
        } catch (IOException e) {
            warn("§7写入公告失败");
        }
    }

    public static Set<String> readSuperUsers() {
        return readPipeSet(superUsers, "§7读取管理员失败");
    }

    public static void writeSuperUsers(Set<String> ids) {
        writePipeSet(superUsers, ids, "§7写入管理员失败");
    }

    public static void loadGroupList() {
        try (BufferedReader r = new BufferedReader(new FileReader(groupList))) {
            String line = r.readLine();
            if (line == null || line.isEmpty()) {
                notifyAdmins("§7尚未设置群聊");
                return;
            }

            String[] groups = line.split("\\|");
            if (groups.length < 2) {
                notifyAdmins("§7还有一个群聊需要设置");
                return;
            }

            QQGroup = groups[0];
            ManagerGroup = groups[1];
        } catch (IOException e) {
            warn("§7读取群聊失败");
            notifyAdmins("§7读取群聊失败");
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
            if (times.length < 4) {
                notifyAdmins("§7读取宵禁时间失败");
                return;
            }

            curfewTime[0] = Integer.parseInt(times[0]);
            curfewTime[1] = Integer.parseInt(times[1]);
            curfewTime[2] = Integer.parseInt(times[2]);
            curfewTime[3] = Integer.parseInt(times[3]);
        } catch (IOException | NumberFormatException e) {
            warn("§7读取宵禁时间失败");
            notifyAdmins("§7读取宵禁时间失败");
        }
    }

    public static void writeCurfew(int[] times) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(curfewFile))) {
            String line = Arrays.stream(times).mapToObj(String::valueOf).collect(Collectors.joining(","));
            w.write(line);
        } catch (IOException e) {
            warn("§7写入宵禁时间失败");
        }
    }

    public static List<String[]> readShutLogs() {
        List<String[]> args = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(shutLogs))) {
            String line = r.readLine();
            while (line != null) {
                args.add(line.split(",", 6));
                line = r.readLine();
            }
        } catch (IOException e) {
            warn("§7读取禁言记录失败");
            notifyAdmins("§7读取禁言记录失败");
        }
        return args;
    }

    public static void writeShutLogs(List<String[]> args) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(shutLogs))) {
            for (String[] arg : args) {
                w.write(String.join(",", arg));
                w.newLine();
            }
        } catch (IOException e) {
            warn("§7写入禁言记录失败");
            notifyAdmins("§7写入禁言记录失败");
        }
    }

    public static void loadRegex() {
        List<String> regexList = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(regexFile))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.isBlank())
                    regexList.add(line);
            }
        } catch (IOException e) {
            warn("§7读取正则失败");
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
            warn("§7写入正则失败");
        }
    }

    public static void writeGroupList(String[] ids) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(groupList))) {
            w.write(String.join("|", ids));
        } catch (IOException e) {
            warn("§7写入群聊失败");
        }
    }

    private static Set<String> readPipeSet(File file, String warnMsg) {
        Set<String> set = new HashSet<>();
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line = r.readLine();
            if (line == null || line.isEmpty())
                return set;
            return new HashSet<>(Arrays.asList(line.split("\\|")));
        } catch (IOException e) {
            warn(warnMsg);
            return set;
        }
    }

    private static void writePipeSet(File file, Set<String> ids, String warnMsg) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            w.write(String.join("|", ids));
        } catch (IOException e) {
            warn(warnMsg);
        }
    }

    private static void checkFile(File file) {
        if (file.exists())
            return;
        try {
            file.createNewFile();
        } catch (IOException e) {
            warn("§7创建文件失败: " + file.getName());
        }
    }

    private static void notifyAdmins(String msg) {
        users.forEach(id -> sendP(id, msg));
    }

    private static void warn(String msg) {
        Plugin plugin = get();
        if (plugin != null)
            plugin.getLogger().warning(msg);
    }
}
