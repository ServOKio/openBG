//Original: Copyright (c) 2021 Discord Custom Covers

package net.servokio.openbg;

import android.content.Context;

import com.aliucord.Logger;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.discord.utilities.icon.IconUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    public static Main instance;
    public Main(){
        instance = this;
        this.settingsTab = new Plugin.SettingsTab(Settings.class).withArgs(this.settings);
    }

    public static final Logger log = new Logger("openBG");
    private static final Pattern bannerMatch = Pattern.compile("^https://cdn.discordapp.com/banners/\\d+/[a-z0-9_]+\\.\\w{3,5}\\?size=\\d+$");

    public static DB database = DB.Instance;

    @Override
    public void start(Context context) throws Throwable {
        patcher.patch(IconUtils.class.getDeclaredMethod("getForUserBanner", Long.TYPE, String.class, Integer.class, Boolean.TYPE), new Hook(obj -> getBanner(settings, obj)));
        database.loadDB(com.aliucord.Utils.getAppContext(), this.settings);
    }

    public static void getBanner(SettingsAPI settings, XC_MethodHook.MethodHookParam param) {
        String bannerURL;
        if(
                param.getResult() == null ||
                !settings.getBool("nitro_prioritet", true) ||
                !bannerMatch.matcher(param.getResult().toString()).find()
        ) {
            Object obj = param.args[0];
            if (obj != null) {
                long id = (Long) obj;
                if (database.getMapCache().containsKey(id)) {
                    param.setResult(database.getMapCache().get(id));
                    return;
                }
                //If user has nitro banner
                String regex = database.getRegex();
                Matcher matcher = Pattern.compile(id + regex, 32).matcher(database.getData());
                if (matcher.find() && (bannerURL = matcher.group(1)) != null) {
                    database.getMapCache().put(id, bannerURL);
                    param.setResult(bannerURL);
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
