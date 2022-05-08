package net.servokio.openbg;

import android.content.Context;

import com.aliucord.Http;
import com.aliucord.api.SettingsAPI;
import com.aliucord.utils.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import kotlin.text.Charsets;

public class DB {
    public static final DB Instance = new DB();
    private static String data = "{}";
    private static final String regex = ".*?\\(\"(.*?)\"";
    private static final Map<Long, String> mapCache = new HashMap();
    private static final Pattern bannerMatch = Pattern.compile("^https://cdn.discordapp.com/banners/\\d+/[a-z0-9_]+\\.\\w{3,5}\\?size=\\d+$");

    public String getRegex() {
        return regex;
    }

    public String getData() {
        return data;
    }

    public Map<Long, String> getMapCache() {
        return mapCache;
    }

    public final void loadDB(Context ctx, SettingsAPI settings) {
        File cacheFile = getCacheFile(ctx);
        setData(loadFromCache(cacheFile));
        Main.log.debug("Loaded database.");
        if (!ifRecache(cacheFile.lastModified(), settings) && (!(getData().equals("{}")) && getData() != null)) return;
        downloadDB(cacheFile);
    }

    public final void downloadDB(File cachedFile) {
        try {
            String[] hello = Utils.httpGet("https://servokio.ru/hello");
            if(hello[0].equals("200")){
                JSONObject stats = new JSONObject(hello[1]);
                int apiV = stats.getJSONObject("api").getInt("useVersion");
                String dbUrl = stats.getJSONObject("api").getString("proto")+"://"+stats.getJSONObject("api").getString("host")+"/api"+apiV+"/get-openbg";
                String[] dbInfo = Utils.httpGet(dbUrl);
                if(dbInfo[0].equals("200")) {
                    Main.log.debug("Downloading database...");
                    Http.simpleDownload(dbUrl, cachedFile);
                    setData(loadFromCache(cachedFile));
                    Main.log.debug("Updated database.");
                } else com.aliucord.Utils.showToast("Code1 "+dbInfo[0], false);
            } else {
                com.aliucord.Utils.showToast("Code "+hello[0], false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String loadFromCache(File it) {
        try {
            return new String(IOUtils.readBytes(new FileInputStream(it)), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "{}";
    }

    private void setData(String data){
        //Types
        //1 - https://cdn.discordapp.com/attachments/123456789/123456789/Untitled.png
        //    123456789:123456789:Untitled.png
        try {
            DB.data = data;
            JSONArray parsed = new JSONObject(data).getJSONArray("banners");
            for(int i = 0; i < parsed.length(); i++) {
                JSONObject g = (JSONObject) parsed.get(i);
                int type = g.getInt("t");
                Long u = Long.parseLong(g.getString("u"));
                String[] d = g.getString("b").split(":");
                String b = null;
                switch (type) {
                    case 1:
                        b = "https://cdn.discordapp.com/attachments/"+d[0]+"/"+d[1]+"/"+d[2];
                        break;
                    default:
                        break;
                }
                if(b != null) mapCache.put(u, b);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public final File getCacheFile(Context ctx) {
        return new File(ctx.getCacheDir(),  "openbg_db.json");
    }

    private boolean ifRecache(long lastModified, SettingsAPI settings) {
        return System.currentTimeMillis() - lastModified > (settings.getLong("cache_time", 60) * 60 * 1000);
    }
}
