package com.smg.jiefang.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipFile;

/**
 * Created by Mikiller on 2016/11/1.
 */
public class ChannelUtil {

    public static String getChannel(Context context, String defaultChannel){
        String line = null;
        try
        {
            ZipFile localZipFile = new ZipFile(context.getApplicationInfo().sourceDir);
            line = new BufferedReader(new InputStreamReader(localZipFile.getInputStream(localZipFile.getEntry("META-INF/td_channel.inf")))).readLine();
        }
        catch (IOException localIOException)
        {
            Toast.makeText(context, localIOException.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally
        {
            if (TextUtils.isEmpty(line)) {
                line = defaultChannel;
            }
            Log.d(ChannelUtil.class.getSimpleName(), "current Channel: " + line);
            return line.trim();
        }
    }
}
