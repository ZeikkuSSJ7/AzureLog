package com.zeikkussj.azurelog.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.zeikkussj.azurelog.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class ImageHandler {

    /**
     * Abre una imagen local y la visualiza en el <code>ImageView</code> indicado
     * @param pathToFile la ruta de la imagen a mostrar
     * @param ivCover el <code>ImageView</code> a modificar
     * @param activity el <code>Activity</code> de origen
     * @throws IOException si hay algún problema a la hora de decodificar la imagen
     */
    public static void openImage(String pathToFile, ImageView ivCover, Activity activity) throws IOException {
        Log.i("ImageOpen", "Opening image locally...");
        FileInputStream isImage = new FileInputStream(pathToFile);
        Drawable dImageLocal = Drawable.createFromStream(isImage, "cover");
        if (activity != null)
            activity.runOnUiThread(() -> ivCover.setImageDrawable(dImageLocal));
        else
            ivCover.setImageDrawable(dImageLocal);
        isImage.close();
    }

    /**
     * Descarga una imagen de un enlace externo y la guarda en el lugar deseado, normalmente la
     * carpeta de usuario
     * @param url la URL de la imagen
     * @param pathToFile la ruta del archivo a guardar
     * @param ivCover el <code>ImageView</code> a modificar con la imagen descargada
     * @param activity el <code>Activity</code> de origen
     * @throws IOException si hay algún problema a la hora de descargar o decodificar la imagen
     */
    public static void saveImage(String url, String pathToFile, ImageView ivCover, Activity activity) throws IOException{
        Log.i("ImageOpen", "Opening image from server...");
        Bitmap bitmap = ((BitmapDrawable)getImage(url)).getBitmap();
        FileOutputStream out = new FileOutputStream(pathToFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 65, out);
        out.close();
        openImage(pathToFile, ivCover, activity);
    }

    /**
     * Obtiene y convierte una imagen de un enlace
     * @param url la URL de la imagen
     * @return la imagen en forma de <code>Drawable</code> decodificada
     * @throws IOException si hay algún problema a la hora de decodificar la imagen
     */
    public static Drawable getImage(String url) throws IOException{
        URL urlImage = new URL(url);
        InputStream is = urlImage.openStream();
        return Drawable.createFromStream(is, "cover");
    }

    /**
     * Guarda en la carpeta de usuario una imagen local nueva o que reemplaze a la carátula de
     * un videojuego
     * @param drawable la imagen a guardar
     * @param id el ID del juego
     * @param context el contexto de la aplicación
     * @throws IOException si hay algún problema al abrir la imagen local
     */
    public static void saveImageFromLocalSource(Drawable drawable, String id, Activity context) throws IOException {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        FileOutputStream out = context.openFileOutput(id + StaticFields.DEFAULT_COVER_EXTENSION, Context.MODE_PRIVATE);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 65, out);
        out.close();
    }

    /**
     * Guarda la imagen de usuario en la carpeta de usuario
     * @param pic la imagen de usuario en forma de <code>Drawable</code>
     * @param path la ruta al archivo de la imagen
     * @throws IOException si hay algún problema al abrir la imagen local
     */
    public static void saveUserPic(Drawable pic, String path) throws IOException {
        Bitmap bitmap = ((BitmapDrawable) pic).getBitmap();
        FileOutputStream out = new FileOutputStream(path);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 65, out);
        out.close();
    }

    /**
     * Inicia un <code>Intent</code> de acceso al almacenamiento interno para elegir una imagen
     * @param activity el <code>Activity</code> que ha realizado la llamada
     */
    public static void startImagePickIntent(AppCompatActivity activity){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, 1);
    }

    /**
     * Guarda una carátula de juego al almacenamiento externo, teniendo en cuenta las diferencias
     * entre versiones de Android
     * @param context el contexto de la aplicación
     * @param cover la carátula del juego
     * @param gameName el nombre del juego
     * @param gameId el ID del juego
     *
     * @see #saveImageToExternalStorage(Context, Bitmap, String) para Android API <= 28
     * @see #saveImageToExternalStorageAndroidQ(Context, String, String) para Android API >= 29
     */
    public static void saveImageToExternalStorage(Context context, ImageView cover, String gameName, String gameId){
        Drawable coverToSave = cover.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) coverToSave).getBitmap();
        gameName = gameName.replaceAll("[^a-zA-Z0-9]", "");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageToExternalStorageAndroidQ(context, gameName, gameId);
        } else{
            saveImageToExternalStorage(context, bitmap, gameName);
        }
    }

    /**
     * Guarda una carátula de un juego en el almacenamiento externo siguiendo el nuevo procedimiento
     * de Android Q (API 29)
     * @param context el contexto de la aplicación
     * @param gameName el nombre del juego
     * @param gameId el ID del juego
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private static void saveImageToExternalStorageAndroidQ(Context context, String gameName, String gameId){
        Log.d("Saving image", "getting content resolver...");
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.TITLE, gameName);
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, gameName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + StaticFields.IMAGE_FOLDER);
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (uri != null) {
            try {
                OutputStream oStream = resolver.openOutputStream(uri);
                if (oStream != null) {
                    Log.d("Saving image", "saving image to external source...");
                    InputStream iStream = context.openFileInput(gameId + StaticFields.DEFAULT_COVER_EXTENSION);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = iStream.read(buffer)) > 0) {
                        oStream.write(buffer, 0, len);
                    }
                    oStream.close();
                    iStream.close();
                    Log.d("Saving image", "image saved");
                    Toast.makeText(context, context.getString(R.string.coverSaved), Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(context, R.string.failedToSaveCover, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Guarda una carátula de un juego al almacenamiento externo siguiendo la rutina antigua
     * @param context el contexto de la aplicación
     * @param bitmap el <code>Bitmap</code> de la carátula
     * @param gameName el nombre del juego
     */
    private static void saveImageToExternalStorage( Context context, Bitmap bitmap, String gameName){
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File directoryPath = new File(path + StaticFields.IMAGE_FOLDER);
        directoryPath.mkdirs();
        File imagePath = new File(directoryPath.getAbsolutePath() + "/" + gameName + StaticFields.DEFAULT_COVER_EXTENSION);
        try {
            if (imagePath.exists())
                imagePath.delete();
            else
                imagePath.createNewFile();
            MediaScannerConnection.scanFile(context, new String[]{imagePath.getAbsolutePath()}, new String[]{"image/*"}, null);
            FileOutputStream fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            Toast.makeText(context, context.getString(R.string.coverSaved), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, R.string.failedToSaveCover, Toast.LENGTH_SHORT).show();
        }
        MediaScannerConnection.scanFile(context, new String[]{imagePath.getAbsolutePath()}, new String[]{"image/*"}, null);
    }
}
