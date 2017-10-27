package com.androiddesdecero.rateus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

/**
 * Created by albertopalomarrobledo on 27/10/17.
 */

public class RateUs {
    //Variables Estaticas con el Nombre de la App y la dirección del Paquete de la App
    private static String PACKAGE_NAME;
    //Variables Estaticas con los dias hasta los que va a aparecer el RATE_US y el número minimo de Lanzamientos
    private static int DAYS_UNTIL_PROMPT;//Min number of days
    private static int LAUNCHES_UNTIL_PROMPT;//Min number of launches
    //Variables para SHARED PREFERENCES
    private static final String SPREFERENCE = "spreference";
    private static final String SP_DONT_SHOW_AGAIN = "sp_donshowagain";
    private static final String SP_APP_RATE = "sp_app_rate";
    private static final String SP_LAUNCH_COUNT = "sp_launch_count";
    private static final String SP_DATE_FIRST_LAUNCH = "sp_date_fisrt_launch";

    public static void appLaunched(Context context, int daysUntilPront, int launchesUntilPrompt) {
        PACKAGE_NAME = context.getPackageName();
        DAYS_UNTIL_PROMPT = daysUntilPront;
        LAUNCHES_UNTIL_PROMPT = launchesUntilPrompt;
        SharedPreferences mPrefs;
        mPrefs = context.getSharedPreferences(SPREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        /*********PARA PRUEBAS***********************
         * ESTE CODIGO PONE TODO A CER0
         * editor.putBoolean(SP_DONT_SHOW_AGAIN, false);
         *editor.commit();
         *********************************************/
        if (mPrefs.getBoolean(SP_DONT_SHOW_AGAIN, false)) {
            return;
        }
        // Increment launch counter
        long launch_count = mPrefs.getLong(SP_LAUNCH_COUNT, 0) + 1;
        Log.v("paco", Long.toString(launch_count));
        editor.putLong(SP_LAUNCH_COUNT, launch_count);

        // Get date of first launch
        Long date_firstLaunch = mPrefs.getLong(SP_DATE_FIRST_LAUNCH, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(SP_DATE_FIRST_LAUNCH, date_firstLaunch);
        }
        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(context, editor).show();
            }
        }
        editor.commit();
    }
    public static AlertDialog showRateDialog(final Context context, final SharedPreferences.Editor editor){
        final AlertDialog dialog = new AlertDialog.Builder(context).
                setPositiveButton(context.getString(R.string.dialog_app_rate), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openAppInPlayStore(context);
                        editor.putBoolean(SP_DONT_SHOW_AGAIN, true);
                        editor.commit();
                    }
                }).setNeutralButton(context.getString(R.string.dialog_rate_later), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //RESET LAUNCH COUNT
                editor.putLong(SP_LAUNCH_COUNT, 0);
                editor.commit();
            }
        }).setNegativeButton(context.getString(R.string.dialog_no_rate), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putBoolean(SP_DONT_SHOW_AGAIN, true);
                editor.commit();
            }
        }).setMessage(context.getString(R.string.rate_app_message)).setTitle(context.getString(R.string.rate_app_title)).create();
        return dialog;
    }

    public static void openAppInPlayStore(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PACKAGE_NAME)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + PACKAGE_NAME)));
        }
    }
}
