package com.integra.tierra.tools;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.integra.tierra.db.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Created by ULISES on 07/02/2017.
 */

public class Tools {

    static ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

    public static void imprime(String title, Context context) {
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(title);
            dialog.setCancelable(false);
            dialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void encenderGPS(final Activity context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage("El GPS esta desactivado, ¿ Desea activarlo ?");
        dialog.setCancelable(false);
        dialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                context.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 899);
            }
        });
        dialog.show();
    }

    public static void imprimeFinish(String title, final Context context, final Activity activity) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(title);
        dialog.setCancelable(false);
        dialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                activity.finish();
            }
        });
        dialog.show();
    }

    public static ProgressDialog esperaDialog(Context c, String mensaje) {
        ProgressDialog pDialog = new ProgressDialog(c);
        try {
            pDialog.setMessage(mensaje);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pDialog;
    }

    public static void cancelarDialog(ProgressDialog pd) {
        if (pd != null)
            pd.cancel();
    }

    public static boolean validarVacios(String texto) {
        if (!texto.trim().equals("")) {
            return true;
        } else {
            return false;
        }
    }

    public static Boolean checkNow(Context con) {
        ConnectivityManager connectivityManager;
        NetworkInfo wifiInfo;
        NetworkInfo mobileInfo;
        try {
            connectivityManager = (ConnectivityManager) con
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            wifiInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            mobileInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifiInfo.isConnected() || mobileInfo.isConnected()) {
                return true;
            }

        } catch (Exception e) {
            Log.e("ERROR_checkNow", e.getMessage());
        }

        return false;
    }

    public static void notiLocal(String nameIntent, String nameExtra, boolean flag, Context c) {
        Intent intent = new Intent(nameIntent);
        intent.putExtra(nameExtra, flag);
        LocalBroadcastManager.getInstance(c)
                .sendBroadcast(intent);
    }

    public static void deleteImage(String manage, Context c) {
        String[] aux = manage.split("/");
        String file_dj_path = Environment.getExternalStorageDirectory() + "/" + aux[aux.length - 2] + "/" + aux[aux.length - 1];
        File fdelete = new File(file_dj_path);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.e("-->", "file Deleted :" + file_dj_path);
                callBroadCast(c);
            } else {
                Log.e("-->", "file not Deleted :" + file_dj_path);
            }
        }
    }

    public static void callBroadCast(Context c) {
        if (Build.VERSION.SDK_INT >= 14) {
            Log.e("-->", " >= 14");
            MediaScannerConnection.scanFile(c, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                /*
                 *   (non-Javadoc)
                 * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
                 */
                public void onScanCompleted(String path, Uri uri) {
                    Log.e("ExternalStorage", "Scanned " + path + ":");
                    Log.e("ExternalStorage", "-> uri=" + uri);
                }
            });
        } else {
            Log.e("-->", " < 14");
            c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    public static void deleteAllImage(Context context) {

        Cursor c = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{BaseColumns._ID}, null, null, null);
        if (c != null) {
            int id = c.getColumnIndexOrThrow(BaseColumns._ID);
            while (c.moveToNext()) {
                Long mediaStoreId = c.getLong(id);
                context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, BaseColumns._ID + "=?", new String[]{Long.toString(mediaStoreId)});
            }
            c.close();
        }
    }

    public static void closeSession(final Context c, final Activity a) {
        Tools.notiLocal("close", "close", true, c);
        DataSource data = new DataSource(c);
        data.deleteEvent();
        data.deleteImg();
        data.closeDataBase();
        a.finish();
    }

    public void closeApp(final Context c, final Activity a) {
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(c);
            dialog.setMessage("¿Seguro que Desea Cerrar la Aplicación?");
            dialog.setCancelable(false);
            dialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Tools.notiLocal("close", "close", true, c);
                    dialog.cancel();
                    a.finish();
                }
            });
            dialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getImg64(Context c, String path) {
        Log.e("PATH", "++++++" + path);
        Bitmap bm = null;
        String img64 = null;
        ByteArrayOutputStream bao = null;
        try {
            bao = new ByteArrayOutputStream();
            bm = Glide.with(c).load(path)
                    .asBitmap().into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
            Log.e("HEIGHT", "" + bm.getHeight());
            Log.e("WIDTH", "" + bm.getWidth());

            if (bm.getHeight() > bm.getWidth()) {
                bm = null;
                bm = Glide.with(c).load(path)
                        .asBitmap().into(720, 1024).get();
            } else {
                bm = null;
                bm = Glide.with(c).load(path)
                        .asBitmap().into(1024, 720).get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (bao != null && bm != null) {
            bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);

            byte[] byteArray = bao.toByteArray();
            img64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        return img64;
    }

    public static Uri photo(Activity c, int code) {
        Uri imageUri;
        if (!memoryInfo.lowMemory) {
            System.gc();
            ContentValues values = new ContentValues();
            imageUri = c.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            c.startActivityForResult(intent, code);
            return imageUri;
        } else {
            Tools.imprime("Su dispositivo cuenta con muy poca meoria para tomar la foto", c.getApplicationContext());
            return null;
        }
    }

    public static Boolean isOnlineNet() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
