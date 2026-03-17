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
    private static final String customCookie = "buvid3=E0CA3EC4-9643-C968-3E8A-7B1F7F4E490F83323infoc; b_nut=1763003083; _uuid=7D221F6F-10D39-374F-2922-18CB12EC24BF84583infoc; buvid4=0C2B8E64-4B81-D7A5-16F2-E2BB62372E8D84208-025111311-sJxQhxrIacy0SHBZOWTifA%3D%3D; buvid_fp=662edab4cca349bd6540dd983e9b3634; rpdid=|(J|~YR~Yll|0J'u~YJ||J|)R; DedeUserID=177826289; DedeUserID__ckMd5=2ee7ec67e67ec401; theme-tip-show=SHOWED; theme-avatar-tip-show=SHOWED; LIVE_BUVID=AUTO2517649985153664; theme-switch-show=SHOWED; theme_style=dark; CURRENT_LANGUAGE=; PVID=1; CURRENT_BLACKGAP=0; ogv_device_support_hdr=1; CURRENT_QUALITY=125; hit-dyn-v2=1; dy_spec_agreed=1; SESSDATA=da862df5%2C1787473719%2Ca6684%2A22CjAkPx2GcVPrQPjNrIKJ-gWg1hsFwbTw5Loj8lIqniErOfA2jGR7_jZaIuZdXCQzu-0SVmh1SmFkQmRTdW0xWnR0SloxU1dJZE5uanJUTkdKeGNjQ0RtelVkbFBRdTFtY0ZlNVZlcFBMaTMwX2x1QW82OVBRTkpQZGZjY1FJRVBFblRLUzN0ZU9RIIEC; bili_jct=d92aa009e4b1a53f7f32e67da7a83162; bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NzIyMDIzNzgsImlhdCI6MTc3MTk0MzExOCwicGx0IjotMX0.t_7q3Qvsk6lY7oJlxdHfi59SyONfaO3SMOQ9RVhUBkM; bili_ticket_expires=1772202318; home_feed_column=5; browser_resolution=1542-1015; CURRENT_FNVAL=4048; bp_t_offset_177826289=1173195055914024960";

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
