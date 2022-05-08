package net.servokio.openbg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Editable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.TextInput;
import com.discord.stores.StoreStream;
import com.discord.views.CheckedSetting;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import kotlin.jvm.internal.Intrinsics;

public class Settings extends SettingsPage {
    private final SettingsAPI settings;
    private ImageView bannerPreview;

    public Settings(SettingsAPI settings) {
        this.settings = settings;
    }

    public void onViewBound(View view) {
        Settings.super.onViewBound(view);
        setActionBarTitle("openBG");

        bannerPreview = new ImageView(view.getContext());
        bannerPreview.setAdjustViewBounds(true);
        bannerPreview.setPadding(0,0,0,28);

        if(Main.database.getMapCache().containsKey(StoreStream.getUsers().getMe().getId())){
            GetXMLTask task = new GetXMLTask();
            task.execute(Main.database.getMapCache().get(StoreStream.getUsers().getMe().getId()));
        }

        //Change interval
        TextInput textInput = new TextInput(view.getContext());
        textInput.setHint("Caching time (in seconds)");
        EditText editText = textInput.getEditText();
        Intrinsics.checkNotNull(editText);
        editText.setText(String.valueOf(this.settings.getLong("cache_time", 60)));

        EditText editText1 = textInput.getEditText();
        Intrinsics.checkNotNull(editText1);
        editText1.addTextChangedListener(new com.discord.utilities.view.text.TextWatcher() {
            public void afterTextChanged(Editable editable) {
                SettingsAPI settingsAPI;
                try {
                    long valueOf = Long.parseLong(editable.toString());
                    if (valueOf == 0) return;
                    settingsAPI = Settings.this.settings;
                    settingsAPI.setLong("cache_time", Long.parseLong(editable.toString()));
                } catch (Exception e) {
                    settingsAPI = Settings.this.settings;
                    settingsAPI.setLong("cache_time", 60);
                }
            }
        });

        Button unload = new Button(view.getContext());
        unload.setText("Update your banner");
        unload.setOnClickListener(v -> Utils.launchUrl("https://servokio.ru/openbg"));

        Button downloadUpdate = new Button(view.getContext());
        downloadUpdate.setText("Redownload database");
        downloadUpdate.setOnClickListener(v -> Settings.redownloadThread(Settings.this));

        addView(bannerPreview);
        addView(textInput);
        addView(createCheckedSetting(view.getContext(), "Prioritize Nitro banner", "Banners of nitro users will be shown in any case", "nitro_prioritet", true));
        addView(unload);
        addView(downloadUpdate);
    }

    private class GetXMLTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap map = null;
            for (String url : urls) map = downloadImage(url);
            return map;
        }

        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            bannerPreview.setImageBitmap(result);
            bannerPreview.setMinimumWidth(bannerPreview.getWidth());
            bannerPreview.setMaxHeight(bannerPreview.getWidth());
        }

        // Creates Bitmap from InputStream and returns it
        private Bitmap downloadImage(String url) {
            Bitmap bitmap = null;
            InputStream stream = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;

            try {
                stream = getHttpConnection(url);
                bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
                stream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bitmap;
        }

        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }
    }

    public static void redownloadThread(final Settings settings) {
        Utils.threadPool.execute(() -> Settings.download(settings));
    }

    public static void download(Settings set) {
        Utils.showToast("Updating database....", false);
        Context c = set.getContext();
        if(c != null){
            Main.database.downloadDB(Main.database.getCacheFile(c));
            Utils.showToast("Database has been successfully updated", false);
        }
    }

    private CheckedSetting createCheckedSetting(Context ctx, String title, String subtitle, final String setting, boolean checked) {
        CheckedSetting checkedSetting = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, subtitle);
        checkedSetting.setChecked(this.settings.getBool(setting, checked));
        checkedSetting.setOnCheckedListener(obj -> Settings.onChange(Settings.this, setting, obj));
        return checkedSetting;
    }

    public static void onChange(Settings set, String key, Boolean value) {
        Intrinsics.checkNotNull(value);
        set.settings.setBool(key, value);
    }
}
