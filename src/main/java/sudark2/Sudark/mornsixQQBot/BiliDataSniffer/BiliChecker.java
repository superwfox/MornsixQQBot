package sudark2.Sudark.mornsixQQBot.BiliDataSniffer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Set;

import static org.bukkit.map.MapPalette.imageToBytes;
import static sudark2.Sudark.mornsixQQBot.BiliDataSniffer.DrawUtil.normalizeUrl;
import static sudark2.Sudark.mornsixQQBot.FileManager.QQGroup;
import static sudark2.Sudark.mornsixQQBot.FileManager.biliUids;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.*;

public class BiliChecker {
    private static final long INTERVAL_SECONDS = 60;

    public static void check() {
        Set<String> uids = Set.copyOf(biliUids);
        long now = System.currentTimeMillis() / 1000;

        for (String uid : uids) {
            try {
                BiliData data = BiliData.getBiliData(uid);
                if (data.getPublishTs() == null || now - data.getPublishTs() >= INTERVAL_SECONDS)
                    continue;

                BufferedImage image = PictureGen.generateImage(data);
                String base64 = imageToBase64(image);
                sendPicture(base64);
                sendCard(data.getJumpUrl(), data.getUserId(), normalizeUrl(data.getUserFace()));
                sendG(data.getJumpUrl(), QQGroup);
            } catch (Exception ignored) {
            }
        }
    }

    public static void testFirst(String askId) {
        if (biliUids.isEmpty()) {
            sendP(askId, "监控列表为空");
            return;
        }
        String uid = biliUids.iterator().next();
        try {
            BiliData data = BiliData.getBiliData(uid);
            BufferedImage image = PictureGen.generateImage(data);
            String base64 = imageToBase64(image);
            sendPicture(base64);
            sendCard(data.getJumpUrl(), data.getUserId(), normalizeUrl(data.getUserFace()));
            sendG(data.getJumpUrl(), QQGroup);
        } catch (Exception e) {
            sendP(askId, "§7测试失败: " + e.getMessage());
        }
    }

    private static String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("§7图片编码失败", e);
        }
    }
}
