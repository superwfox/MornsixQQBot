package sudark2.Sudark.mornsixQQBot.BiliDataSniffer;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static sudark2.Sudark.mornsixQQBot.BiliDataSniffer.DrawUtil.*;

public class PictureGen {
    private static final int WIDTH = 1080;
    private static final int MIN_AV_HEIGHT = 960;
    private static final int MAX_HEIGHT = 4096;
    private static final int DEFAULT_PADDING = 48;
    private static final int VERTICAL_IMAGE_GAP = 12;
    private static final int MAX_VERTICAL_IMAGE_HEIGHT = 1600;
    private static final int IMAGE_CORNER_ARC = 32;

    public static BufferedImage generateImage(BiliData data) {
        if (data == null)
            throw new IllegalArgumentException("§7Image generation failed: data is null");
        JSONObject item = data.getLatestItem();
        String dynamicType = item == null ? "" : item.optString("type", "");
        if ("DYNAMIC_TYPE_AV".equals(dynamicType))
            return generateAvLayout(data, item);
        return generatePictureOnlyLayout(data, item);
    }

    public static File generateFile(BiliData data, File output) {
        if (output == null)
            throw new IllegalArgumentException("§7output file is null");
        File parent = output.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs())
            throw new IllegalStateException("§7Create folder failed: " + parent.getAbsolutePath());
        try {
            BufferedImage image = generateImage(data);
            if (!ImageIO.write(image, "png", output))
                throw new IllegalStateException("§7PNG writer not available");
            if (!output.exists() || output.length() <= 0)
                throw new IllegalStateException("§7output file is empty");
            return output;
        } catch (IOException e) {
            throw new RuntimeException("§7Image write failed: " + e.getMessage(), e);
        }
    }

    private static BufferedImage generateAvLayout(BiliData data, JSONObject item) {
        int padding = DEFAULT_PADDING;
        int innerWidth = WIDTH - padding * 2;
        int avatarSize = 128, avatarX = padding, avatarY = 44;
        int nameX = avatarX + avatarSize + 24, nameBaseY = avatarY + 82;

        int mediaTop = avatarY + avatarSize + 48;
        List<ImageEntry> imageEntries = extractImageEntries(item, true);
        boolean useGrid = imageEntries.size() > 5;

        Font descFont = font(42f);
        Font sectionFont = font(50f);
        BufferedImage tmp = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tmpG = tmp.createGraphics();
        tmpG.setFont(descFont);
        FontMetrics descMetrics = tmpG.getFontMetrics();
        int lineHeight = descMetrics.getHeight() + 8;

        GridLayout layout = useGrid ? buildGridLayout(imageEntries.size(), innerWidth) : null;
        int mediaHeight;
        if (imageEntries.size() == 1)
            mediaHeight = 540;
        else if (useGrid)
            mediaHeight = layout.contentHeight();
        else
            mediaHeight = buildVerticalLayout(imageEntries, innerWidth, VERTICAL_IMAGE_GAP).contentHeight();
        int mediaBottom = mediaTop + mediaHeight;

        String title = extractTitle(item);
        int descTitleY = mediaBottom + 68;
        int descStartY = descTitleY + 54;
        String description = extractDesc(item);

        FontMetrics sectionMetrics = tmpG.getFontMetrics(sectionFont);
        int reservedBottom = 28 + Math.max(54, sectionMetrics.getHeight()) + padding;
        int maxLines = Math.max(1, (MAX_HEIGHT - descStartY - reservedBottom) / lineHeight);
        List<String> lines = fitToMaxLines(wrapText(description, descMetrics, innerWidth), descMetrics, maxLines,
                innerWidth);
        int expectedHeight = descStartY + lines.size() * lineHeight + reservedBottom;
        int canvasHeight = Math.min(MAX_HEIGHT, Math.max(MIN_AV_HEIGHT, expectedHeight));
        tmpG.dispose();

        BufferedImage image = new BufferedImage(WIDTH, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        configureGraphics(g);
        g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0), 0, canvasHeight, new Color(0, 0, 0)));
        g.fillRect(0, 0, WIDTH, canvasHeight);

        BufferedImage avatar = loadImage(normalizeUrl(data.getUserFace()), avatarSize, avatarSize);
        drawCoverImage(g, avatar, avatarX, avatarY, avatarSize, avatarSize, 36, true);
        g.setColor(new Color(234, 217, 178));
        g.setFont(font(58f));
        g.drawString(data.getUserId(), nameX, nameBaseY);

        if (imageEntries.size() == 1) {
            BufferedImage cover = loadImage(imageEntries.get(0).url, innerWidth, mediaHeight);
            drawCoverImage(g, cover, padding, mediaTop, innerWidth, mediaHeight, 42, true);
            g.setColor(new Color(20, 20, 20, 128));
            g.setStroke(new BasicStroke(6f));
            g.drawRoundRect(padding, mediaTop, innerWidth, mediaHeight, 84, 84);
        } else if (useGrid) {
            drawImageGrid(g, imageEntries, padding, mediaTop, layout);
        } else {
            drawVerticalImages(g, imageEntries, padding, mediaTop, innerWidth, VERTICAL_IMAGE_GAP);
        }

        g.setColor(new Color(245, 245, 245));
        g.setFont(sectionFont);
        g.drawString(title, padding, descTitleY);

        g.setColor(new Color(186, 186, 186));
        g.setFont(descFont);
        int textY = descStartY;
        for (String line : lines) {
            g.drawString(line, padding, textY);
            textY += lineHeight;
        }

        drawTimeBadge(g, data.getPublishTs(), canvasHeight, padding, WIDTH);
        g.dispose();
        return image;
    }

    private static BufferedImage generatePictureOnlyLayout(BiliData data, JSONObject item) {
        int padding = DEFAULT_PADDING;
        int innerWidth = WIDTH - padding * 2;
        int avatarSize = 128, avatarX = padding, avatarY = 44;
        int nameX = avatarX + avatarSize + 24, nameBaseY = avatarY + 82;
        int mediaTop = avatarY + avatarSize + 48;

        List<ImageEntry> imageEntries = extractImageEntries(item, false);
        boolean hasImages = imageEntries.stream().anyMatch(e -> !e.url.isBlank());

        int canvasHeight;
        if (hasImages) {
            boolean useGrid = imageEntries.size() > 5;
            GridLayout layout = useGrid ? buildGridLayout(imageEntries.size(), innerWidth) : null;
            VerticalLayout vLayout = useGrid ? null : buildVerticalLayout(imageEntries, innerWidth, VERTICAL_IMAGE_GAP);
            int mediaHeight = useGrid ? layout.contentHeight() : vLayout.contentHeight();
            canvasHeight = Math.min(Math.max(420, mediaTop + mediaHeight + 92 + padding), MAX_HEIGHT);
        } else {
            canvasHeight = mediaTop + 200 + padding;
        }

        BufferedImage image = new BufferedImage(WIDTH, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        configureGraphics(g);
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, WIDTH, canvasHeight);

        BufferedImage avatar = loadImage(normalizeUrl(data.getUserFace()), avatarSize, avatarSize);
        drawCoverImage(g, avatar, avatarX, avatarY, avatarSize, avatarSize, 36, true);
        g.setColor(new Color(234, 217, 178));
        g.setFont(font(58f));
        g.drawString(data.getUserId(), nameX, nameBaseY);

        if (hasImages) {
            boolean useGrid = imageEntries.size() > 5;
            if (useGrid)
                drawImageGrid(g, imageEntries, padding, mediaTop, buildGridLayout(imageEntries.size(), innerWidth));
            else
                drawVerticalImages(g, imageEntries, padding, mediaTop, innerWidth, VERTICAL_IMAGE_GAP);
        } else {
            g.setColor(new Color(186, 186, 186));
            g.setFont(font(38f));
            g.drawString("暂不支持解析该类型动态", padding, mediaTop + 60);
        }

        drawTimeBadge(g, data.getPublishTs(), canvasHeight, padding, WIDTH);
        g.dispose();
        return image;
    }

    // --- 图片网格/竖排布局 ---

    private static void drawImageGrid(Graphics2D g, List<ImageEntry> entries, int startX, int startY,
            GridLayout layout) {
        Shape oldClip = g.getClip();
        if (layout.gap == 0) {
            int totalW = layout.cols * layout.tileSize;
            int totalH = layout.rows * layout.tileSize;
            g.setClip(new RoundRectangle2D.Double(startX, startY, totalW, totalH, IMAGE_CORNER_ARC, IMAGE_CORNER_ARC));
        }
        for (int i = 0; i < entries.size(); i++) {
            int row = i / layout.cols, col = i % layout.cols;
            int x = startX + col * (layout.tileSize + layout.gap);
            int y = startY + row * (layout.tileSize + layout.gap);
            BufferedImage cur = loadImage(entries.get(i).url, layout.tileSize, layout.tileSize);
            int arc = layout.gap == 0 ? 0 : 24;
            drawCoverImage(g, cur, x, y, layout.tileSize, layout.tileSize, arc, layout.gap != 0);
        }
        if (layout.gap == 0)
            g.setClip(oldClip);
    }

    private static void drawVerticalImages(Graphics2D g, List<ImageEntry> entries, int startX, int startY,
            int targetWidth, int gap) {
        VerticalLayout layout = buildVerticalLayout(entries, targetWidth, gap);
        int y = startY;
        for (int i = 0; i < entries.size(); i++) {
            int h = layout.heights.get(i);
            BufferedImage cur = loadImage(entries.get(i).url, targetWidth, h);
            drawCoverImage(g, cur, startX, y, targetWidth, h, IMAGE_CORNER_ARC, true);
            y += h + gap;
        }
    }

    private static GridLayout buildGridLayout(int count, int innerWidth) {
        int cols = 3, gap = 0;
        int tile = Math.max((innerWidth - gap * (cols - 1)) / cols, 1);
        int rows = (Math.max(1, count) + cols - 1) / cols;
        return new GridLayout(cols, rows, gap, tile);
    }

    private static VerticalLayout buildVerticalLayout(List<ImageEntry> entries, int targetWidth, int gap) {
        List<Integer> heights = new ArrayList<>();
        int sum = 0;
        for (ImageEntry e : entries) {
            int h = resolveDisplayHeight(e, targetWidth);
            heights.add(h);
            sum += h;
        }
        if (!heights.isEmpty())
            sum += gap * (heights.size() - 1);
        return new VerticalLayout(heights, sum);
    }

    private static int resolveDisplayHeight(ImageEntry entry, int targetWidth) {
        if (entry.width > 0 && entry.height > 0) {
            int resolved = (int) Math.round(targetWidth * (entry.height / (double) entry.width));
            return Math.max(1, Math.min(resolved, MAX_VERTICAL_IMAGE_HEIGHT));
        }
        return Math.max(1, Math.min((int) Math.round(targetWidth * 9d / 16d), MAX_VERTICAL_IMAGE_HEIGHT));
    }

    // --- JSON数据提取 ---

    static String extractMajorType(JSONObject item) {
        JSONObject modules = item == null ? null : item.optJSONObject("modules");
        JSONObject dynamic = modules == null ? null : modules.optJSONObject("module_dynamic");
        JSONObject major = dynamic == null ? null : dynamic.optJSONObject("major");
        return major == null ? "" : major.optString("type", "");
    }

    private static String extractTitle(JSONObject item) {
        JSONObject modules = item == null ? null : item.optJSONObject("modules");
        JSONObject dynamic = modules == null ? null : modules.optJSONObject("module_dynamic");
        JSONObject major = dynamic == null ? null : dynamic.optJSONObject("major");
        JSONObject archive = major == null ? null : major.optJSONObject("archive");
        if (archive != null) {
            String title = archive.optString("title", "").trim();
            if (!title.isBlank())
                return title;
        }
        return "动态详情";
    }

    private static String extractDesc(JSONObject item) {
        JSONObject modules = item == null ? null : item.optJSONObject("modules");
        JSONObject dynamic = modules == null ? null : modules.optJSONObject("module_dynamic");
        JSONObject major = dynamic == null ? null : dynamic.optJSONObject("major");
        JSONObject archive = major == null ? null : major.optJSONObject("archive");
        if (archive != null) {
            String desc = archive.optString("desc", "").trim();
            if (!"-".equals(desc) && !desc.isBlank())
                return desc;
        }
        JSONObject descObj = dynamic == null ? null : dynamic.optJSONObject("desc");
        String descText = descObj == null ? "" : descObj.optString("text", "").trim();
        return descText.isBlank() ? "这个b很懒..." : descText;
    }

    private static List<ImageEntry> extractImageEntries(JSONObject item, boolean preferArchiveCover) {
        List<ImageEntry> entries = new ArrayList<>();
        JSONObject modules = item == null ? null : item.optJSONObject("modules");
        JSONObject dynamic = modules == null ? null : modules.optJSONObject("module_dynamic");
        JSONObject major = dynamic == null ? null : dynamic.optJSONObject("major");

        if (preferArchiveCover) {
            ImageEntry cover = extractArchiveCover(major);
            if (cover != null && !cover.url.isBlank())
                entries.add(cover);
        }

        JSONObject draw = major == null ? null : major.optJSONObject("draw");
        JSONArray drawItems = draw == null ? null : draw.optJSONArray("items");
        if (drawItems != null) {
            for (int i = 0; i < drawItems.size(); i++) {
                JSONObject obj = drawItems.optJSONObject(i);
                if (obj == null)
                    continue;
                String src = normalizeUrl(obj.optString("src", ""));
                if (src.isBlank() || containsUrl(entries, src))
                    continue;
                int w = parsePositiveInt(obj.optString("width", "0"));
                int h = parsePositiveInt(obj.optString("height", "0"));
                entries.add(new ImageEntry(src, w, h));
            }
        }

        if (!preferArchiveCover && entries.isEmpty()) {
            ImageEntry cover = extractArchiveCover(major);
            if (cover != null && !cover.url.isBlank())
                entries.add(cover);
        }
        if (entries.isEmpty())
            entries.add(new ImageEntry("", 0, 0));
        return entries;
    }

    private static ImageEntry extractArchiveCover(JSONObject major) {
        JSONObject archive = major == null ? null : major.optJSONObject("archive");
        String url = normalizeUrl(archive == null ? "" : archive.optString("cover", ""));
        return url.isBlank() ? null : new ImageEntry(url, 0, 0);
    }

    private static boolean containsUrl(List<ImageEntry> entries, String url) {
        for (ImageEntry e : entries)
            if (e.url.equals(url))
                return true;
        return false;
    }

    record GridLayout(int cols, int rows, int gap, int tileSize) {
        int contentHeight() {
            return rows * tileSize + Math.max(0, rows - 1) * gap;
        }
    }

    record VerticalLayout(List<Integer> heights, int contentHeight) {
    }

    record ImageEntry(String url, int width, int height) {
    }
}
