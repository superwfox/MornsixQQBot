package sudark2.Sudark.mornsixQQBot.BiliDataSniffer;

import net.sf.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class HttpsHandler {
    private static final String customCookie = "buvid3=E0CA3EC4-9643-C968-3E8A-7B1F7F4E490F83323infoc; b_nut=1763003083; _uuid=7D221F6F-10D39-374F-2922-18CB12EC24BF84583infoc; buvid4=0C2B8E64-4B81-D7A5-16F2-E2BB62372E8D84208-025111311-sJxQhxrIacy0SHBZOWTifA%3D%3D; buvid_fp=662edab4cca349bd6540dd983e9b3634; rpdid=|(J|~YR~Yll|0J'u~YJ||J|)R; DedeUserID=177826289; DedeUserID__ckMd5=2ee7ec67e67ec401; SESSDATA=88b85e5f%2C1787142579%2Cab4f1%2A22CjA5zLVcWiPgbUImiS_xtuGJb_9gdpLX_J_QYJNXCFbdghHAbqmtwhswIqy6U6QaBX4SVkRxM25VVmhlTWhtSFp4Q2NsSEZCLXI1dHR0Njd5TWt6czYtSmE1Y2FibE80NWJra3Y2aFhUcFdLX1J1eGtDVFd4aTFiZDNkY2hDUnl3SG9XV1lrNWd3IIEC; bili_jct=d02b30fb93f4f6f940478e20dc8285b1; sid=4hm0apgu; hit-dyn-v2=1; bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NzE5NDQwNTcsImlhdCI6MTc3MTY4NDc5NywicGx0IjotMX0.waq_oQng43UMxvfRpH-fO5qXiWXgkvnOazZ2g7ifSQw; bili_ticket_expires=1771943997";

    public static JSONObject fetchSpace(long hostMid) throws Exception {
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/space?host_mid=" + hostMid;
        JSONObject current = fetchOnce(url);
        int bizCode = current.optInt("code", 0);
        if (bizCode != -412)
            return current;
        return fetchOnce(url);
    }

    public static JSONObject fetchDetail(String idStr) {
        try {
            String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/detail?id=" + idStr;
            JSONObject root = fetchOnce(url);
            if (root.optInt("code", -1) != 0)
                return null;
            JSONObject data = root.optJSONObject("data");
            return data == null ? null : data.optJSONObject("item");
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject fetchOnce(String requestUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(requestUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(15_000);
        conn.setInstanceFollowRedirects(true);

        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");

        conn.setRequestProperty("Cookie", customCookie);

        int httpCode = conn.getResponseCode();
        InputStream is = (httpCode >= 200 && httpCode < 300) ? conn.getInputStream() : conn.getErrorStream();
        String body = readAll(is, conn.getContentEncoding());
        JSONObject obj = parseAsJson(body, httpCode);

        if (obj.optInt("code", 0) == -412) {
            obj.element("message", "风控(-412): 需要有效Cookie，请调用 HttpsHandler.setCookie(...) 或设置环境变量 BILI_COOKIE");
        }
        return obj;
    }

    private static String readAll(InputStream is, String contentEncoding) throws Exception {
        if (is == null)
            return "";
        InputStream wrapped = is;
        if (contentEncoding != null) {
            String enc = contentEncoding.toLowerCase();
            if (enc.contains("gzip"))
                wrapped = new GZIPInputStream(is);
            else if (enc.contains("deflate"))
                wrapped = new InflaterInputStream(is);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = wrapped.read(buf)) != -1)
            baos.write(buf, 0, n);
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static JSONObject parseAsJson(String body, int httpCode) {
        try {
            return JSONObject.fromObject(body);
        } catch (Exception ignored) {
            JSONObject fallback = new JSONObject();
            fallback.element("code", httpCode);
            fallback.element("message", "HTTP请求失败，且返回内容不是JSON");
            fallback.element("raw", body == null ? "" : body);
            return fallback;
        }
    }
}
