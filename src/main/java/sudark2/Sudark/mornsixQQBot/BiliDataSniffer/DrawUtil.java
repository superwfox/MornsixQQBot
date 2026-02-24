package sudark2.Sudark.mornsixQQBot.BiliDataSniffer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DrawUtil {
    private static final Color MILD_YELLOW = new Color(234, 217, 178);
    private static final Color TIME_TEXT_COLOR = new Color(20, 20, 20);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static volatile Font customFont;

    static void configureGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    static Font font(float size) {
        Font base = customFont;
        if (base == null)
            base = loadFont();
        return base.deriveFont(Font.PLAIN, size);
    }

    private static Font loadFont() {
        if (customFont != null)
            return customFont;
        String[] paths = { "libs/jiangxizhuokai.ttf", "src/main/resources/jiangxizhuokai.ttf" };
        for (String path : paths) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    customFont = Font.createFont(Font.TRUETYPE_FONT, file);
                    return customFont;
                }
            } catch (Exception ignored) {
            }
        }
        try (InputStream stream = DrawUtil.class.getClassLoader().getResourceAsStream("jiangxizhuokai.ttf")) {
            if (stream != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, stream);
                return customFont;
            }
        } catch (Exception ignored) {
        }
        customFont = new Font("Serif", Font.PLAIN, 36);
        return customFont;
    }

    static void drawCoverImage(Graphics2D g, BufferedImage image, int x, int y, int w, int h, int arc,
            boolean drawBorder) {
        int iw = image.getWidth();
        int ih = image.getHeight();
        int sx, sy, sw, sh;
        double targetRatio = w / (double) h;
        double srcRatio = iw / (double) ih;

        if (srcRatio > targetRatio) {
            sh = ih;
            sw = (int) Math.round(ih * targetRatio);
            sx = (iw - sw) / 2;
            sy = 0;
        } else {
            sw = iw;
            sh = (int) Math.round(iw / targetRatio);
            sx = 0;
            sy = (ih - sh) / 2;
        }

        Shape old = g.getClip();
        if (arc > 0) {
            g.setClip(new RoundRectangle2D.Double(x, y, w, h, arc, arc));
        } else {
            g.setClip(new Rectangle(x, y, w, h));
        }
        g.drawImage(image, x, y, x + w, y + h, sx, sy, sx + sw, sy + sh, null);
        g.setClip(old);

        if (drawBorder) {
            g.setColor(new Color(222, 226, 232));
            if (arc > 0)
                g.draw(new RoundRectangle2D.Double(x, y, w, h, arc, arc));
        }
    }

    static BufferedImage loadImage(String url, int width, int height) {
        try {
            if (url != null && !url.isBlank()) {
                BufferedImage image = ImageIO.read(new URL(url));
                if (image != null)
                    return image;
            }
        } catch (Exception ignored) {
        }

        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();
        configureGraphics(g);
        g.setPaint(new GradientPaint(0, 0, new Color(232, 236, 241), width, height, new Color(216, 221, 228)));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(123, 131, 142));
        g.setFont(font(Math.max(24f, width / 10f)));
        g.drawString("No Image", Math.max(20, width / 5), Math.max(52, height / 2));
        g.dispose();
        return placeholder;
    }

    static void drawTimeBadge(Graphics2D g, Long publishTs, int canvasHeight, int padding, int totalWidth) {
        String ts = formatPublishTime(publishTs);
        g.setFont(font(30f));
        FontMetrics fm = g.getFontMetrics();
        int padX = 18, padY = 10;
        int badgeW = fm.stringWidth(ts) + padX * 2;
        int badgeH = fm.getHeight() + padY * 2;
        int x = totalWidth - padding - badgeW;
        int y = canvasHeight - padding - badgeH;
        g.setColor(MILD_YELLOW);
        g.fillRoundRect(x, y, badgeW, badgeH, 28, 28);
        g.setColor(TIME_TEXT_COLOR);
        g.drawString(ts, x + padX, y + padY + fm.getAscent());
    }

    static String formatPublishTime(Long publishTs) {
        long seconds = publishTs == null ? 0L : publishTs;
        if (seconds <= 0)
            seconds = System.currentTimeMillis() / 1000;
        LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault());
        return dt.format(TIME_FORMAT);
    }

    static String normalizeUrl(String url) {
        if (url == null || url.isBlank())
            return "";
        if (url.startsWith("//"))
            return "https:" + url;
        return url;
    }

    static int parsePositiveInt(String value) {
        try {
            int v = Integer.parseInt(value);
            return Math.max(v, 0);
        } catch (Exception ignored) {
            return 0;
        }
    }

    static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (ch == '\n') {
                lines.add(current.toString());
                current.setLength(0);
                continue;
            }
            String test = current + String.valueOf(ch);
            if (!current.isEmpty() && fm.stringWidth(test) > maxWidth) {
                lines.add(current.toString());
                current.setLength(0);
            }
            current.append(ch);
        }
        if (!current.isEmpty())
            lines.add(current.toString());
        if (lines.isEmpty())
            lines.add("");
        return lines;
    }

    static List<String> fitToMaxLines(List<String> lines, FontMetrics fm, int maxLines, int maxWidth) {
        if (lines.size() <= maxLines)
            return lines;
        List<String> fitted = new ArrayList<>(lines.subList(0, maxLines));
        String last = fitted.get(maxLines - 1);
        while (!last.isEmpty() && fm.stringWidth(last + "...") > maxWidth) {
            last = last.substring(0, last.length() - 1);
        }
        fitted.set(maxLines - 1, last + "...");
        return fitted;
    }
}
