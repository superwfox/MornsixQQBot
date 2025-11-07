package sudark2.Sudark.mornsixQQBot;

import java.io.File;
import java.io.IOException;

import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.get;

public class FileManager {

    File FileFolder = get().getDataFolder();
    File superUsers = new File(FileFolder, "superUsers.txt");
    File shutLogs = new File(FileFolder, "shutLogs.csv");

    public static void  initFiles (){
        FileManager fileManager = new FileManager();
        fileManager.checkFile(fileManager.superUsers.getAbsolutePath());
        fileManager.checkFile(fileManager.shutLogs.getAbsolutePath());
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
