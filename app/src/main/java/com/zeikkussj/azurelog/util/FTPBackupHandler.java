package com.zeikkussj.azurelog.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.settings.BackupActivity;
import com.zeikkussj.azurelog.settings.RestoreActivity;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class FTPBackupHandler {
    private static FTPClient ftpClient;

    /**
     * Conecta al servidor FTP
     * @param ip la dirección del servidor
     * @throws IOException si hay algún problema con la conexión
     */
    private static void connectToFTP(String ip) throws IOException {
        ftpClient = new FTPClient();
        ftpClient.connect(ip);
        ftpClient.login(StaticFields.FTP_USERNAME, ""); // no password
        ftpClient.changeWorkingDirectory("/");
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        Log.d("FTP", "connectToFTP: connected");
    }

    /**
     * Crea una copia de seguridad en un servidor FTP concreto
     * @param context el contexto de la aplicación
     * @param ip la IP del servidor FTP
     * @param imageZipFile el ZIP de imágenes
     * @param database el archivo de la base de datos
     * @param activity el {@link BackupActivity} de origen, si proviene
     * @param message el <code>TextView</code> de información del proceso de respaldo
     * @return el mensaje de éxito o de error
     */
    public static String backup(Context context, String ip, File imageZipFile, File database, @Nullable BackupActivity activity, @Nullable TextView message){
        // Formateo del nombre de la carpeta de la copia de seguridad
        Calendar c = Calendar.getInstance();
        String databaseBackupFolderName = String.format(Locale.ENGLISH, "%d%s%s_%s%s%s",
                c.get(Calendar.YEAR),
                Util.correctPattern(c.get(Calendar.MONTH) + 1),
                Util.correctPattern(c.get(Calendar.DAY_OF_MONTH)),
                Util.correctPattern(c.get(Calendar.HOUR_OF_DAY)),
                Util.correctPattern(c.get(Calendar.MINUTE)),
                Util.correctPattern(c.get(Calendar.SECOND)));

        String ftpBackupDir = StaticFields.FTP_BACKUP_FOLDER;
        try {
            if (activity != null && message != null)
                activity.runOnUiThread(() -> message.setText(R.string.connectingToFTP));
            connectToFTP(ip);
            if (activity != null && message != null)
                activity.runOnUiThread(() -> message.setText(R.string.creatingFolders));
            ftpClient.mkd(ftpBackupDir);
            ftpClient.cwd(ftpBackupDir); // Crea directorio de las copias de seguridad si no existe y cambia a la carpeta
            ftpClient.mkd(databaseBackupFolderName); // Crea directorio de la copia de seguridad

            // Guarda la base de datos
            FileInputStream databaseInputStream = new FileInputStream(database);
            if (activity != null && message != null)
                activity.runOnUiThread(() -> message.setText(R.string.transferringDatabase));
            ftpClient.storeFile(databaseBackupFolderName + StaticFields.BACKUP_DATABASE_FILENAME, databaseInputStream);
            Log.d("FTP", "connectToFTP: database stored");
            databaseInputStream.close();

            // Guarda el ZIP de los archivos
            FileInputStream imagesInputStream = new FileInputStream(imageZipFile);
            if (activity != null && message != null)
                activity.runOnUiThread(() -> message.setText(R.string.transferringImages));
            ftpClient.storeFile(databaseBackupFolderName + StaticFields.BACKUP_IMAGES_ZIP_FILENAME, imagesInputStream);
            Log.d("FTP", "connectToFTP: images stored");
            imagesInputStream.close();

            ftpClient.logout();
            ftpClient.disconnect();
            Log.d("FTP", "connectToFTP: disconnect");
            return context.getString(R.string.databaseBackedUp);
        } catch (IOException e) {
            if (activity != null && message != null)
                activity.runOnUiThread(() -> message.setText(R.string.errorBackup));
            e.printStackTrace();
            return context.getString(R.string.databaseBackedUpError, e.getMessage());
        }
    }

    /**
     * Muestra una lista de las últimas 10 copias de seguridad del FTP para poder recuperar la que
     * elijas
     * @param ip la IP del servidor FTP
     * @param activity la {@link RestoreActivity} que inició este método
     */
    public static void getBackupsFromFTP(String ip, RestoreActivity activity){
        LinearLayout llBackups = activity.findViewById(R.id.llBackups);

        // Comprueba que la IP no esté vacía al conectarse
        if (!Util.isSet(ip)){
            llBackups.addView(Util.errorText(activity, activity.getString(R.string.theIpIsNotSet)));
            return;
        }

        //Prepara el diálogo de restauración de la copia de seguridad
        AlertDialog.Builder adb = new AlertDialog.Builder(activity, R.style.AlertDialogCustom);
        adb.setTitle(R.string.restoringBackupPleaseWait);
        adb.setView(R.layout.progress_bar_base);
        AlertDialog ad = adb.create();

        new Thread(() -> {
            try {
                connectToFTP(ip);
                ftpClient.cwd(StaticFields.FTP_BACKUP_FOLDER);
                FTPFile[] ftpFiles = ftpClient.listDirectories();
                for (int i = ftpFiles.length - 1; i >= ftpFiles.length - 11 && i >= 0; i--) {
                    // Por cada copia de seguridad, se crea un TextView a medida
                    TextView tv = new TextView(activity);
                    tv.setText(ftpFiles[i].getName());
                    tv.setTextSize(20);
                    tv.setPadding(20,20,20,20);
                    tv.setBackgroundResource(R.drawable.rectangle_red);
                    tv.setOnClickListener(v -> {
                        // El TextView elegido recupera la copia de seguridad
                        activity.runOnUiThread(ad::show);
                        new Thread(() -> {
                            try {
                                retrieve(ip, tv.getText().toString(), activity.getDatabasePath(GameDbHelper.DATABASE_NAME).getAbsolutePath(), activity);
                            } catch (IOException e) {
                                Toast.makeText(activity, R.string.failedToRetrieveFTP, Toast.LENGTH_SHORT).show();
                            }
                            activity.runOnUiThread(() -> {
                                ad.dismiss();
                                Toast.makeText(activity, R.string.databaseRestored, Toast.LENGTH_SHORT).show();
                            });
                            activity.finish();
                        }).start();
                    });
                    activity.runOnUiThread(() -> llBackups.addView(tv));
                }
                ftpClient.logout();
                ftpClient.disconnect();

            } catch (IOException e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, R.string.failedToConnectFTP, Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * Recupera un backup concreto del servidor FTP
     * @param ip la IP del servidor FTP
     * @param dir el directorio del servidor que contiene los archivos
     * @param databasePath la localización de la base de datos
     * @param context el contexto de la aplicación
     * @throws IOException si hay algún problema con la transferencia de archivos
     */
    private static void retrieve(String ip, String dir, String databasePath, Context context) throws IOException {
        connectToFTP(ip);
        ftpClient.cwd(StaticFields.FTP_BACKUP_FOLDER);
        ftpClient.cwd(dir);
        File dbFile = new File(databasePath);
        FileOutputStream databaseOutputStream = new FileOutputStream(dbFile);
        ftpClient.retrieveFile("games.db", databaseOutputStream);
        databaseOutputStream.close();
        File imagesZip = new File(context.getFilesDir() + StaticFields.BACKUP_IMAGES_ZIP_FILENAME);
        FileOutputStream imagesOutputStream = new FileOutputStream(imagesZip);
        ftpClient.retrieveFile("images.zip", imagesOutputStream);
        imagesOutputStream.close();
        ftpClient.logout();
        ftpClient.disconnect();
        unZip(new ZipFile(context.getFilesDir() + StaticFields.BACKUP_IMAGES_ZIP_FILENAME), context);
    }

    /**
     * Comprime las imágenes del directorio de usuario
     * @param context el contexto de la aplicación
     * @return el archivo comprimido
     */
    public static File zip(Context context) {
        File[] filesDirList = context.getFilesDir().listFiles();
        if (filesDirList == null)
            return null;
        File f = new File(context.getFilesDir().getAbsolutePath() + StaticFields.BACKUP_IMAGES_ZIP_FILENAME);
        try {
            if (f.createNewFile()) Log.d("FTPBackup", "zip: create image zip file");
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
                 ZipOutputStream out = new ZipOutputStream(bos)) {
                for (File file : filesDirList) {
                    if (file.isDirectory()) // check for extra folders
                        continue;
                    if (!file.getName().endsWith(StaticFields.DEFAULT_COVER_EXTENSION)) // ensure no dummy is accepted, only jpgs
                        continue;
                    byte[] bytes = new byte[1024]; // buffer of bytes
                    FileInputStream fis = context.openFileInput(file.getName());
                    out.putNextEntry(new ZipEntry(file.getName()));
                    int length;
                    while((length = fis.read(bytes, 0, 1024)) > 0) { // while current file input has bytes, buffer them to the byte[]
                        out.write(bytes, 0, length); // write the buffered bytes to the zip entry
                    }
                    out.closeEntry();
                }
            }
        } catch (IOException e){
            ((Activity)context).runOnUiThread(() -> Toast.makeText(context, R.string.failedToCreateZip, Toast.LENGTH_SHORT).show());
        }
        return f;
    }

    /**
     * Descomprime el archivo ZIP sacado de un servidor FTP en la carpeta de usuario
     * @param zip el ZIP con las imágenes
     * @param context el contexto de la aplicación
     */
    private static void unZip(ZipFile zip, Context context){
        byte[] buffer = new byte[1024]; // buffer size
        try (ZipInputStream stream = new ZipInputStream(new FileInputStream(zip.getName()))) {
            ZipEntry image;
            while ((image = stream.getNextEntry()) != null) { // while the entry is not null, write files
                File file = new File(context.getFilesDir().getAbsoluteFile() + "/" + image.getName());
                try (FileOutputStream fos = context.openFileOutput(file.getName(), Context.MODE_PRIVATE)) {
                    int len;
                    while ((len = stream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len); // writes bytes to the file until no more are left, then moves onto the next image
                    }
                }
            }
        } catch (IOException e) {
            ((Activity)context).runOnUiThread(() -> Toast.makeText(context, R.string.failedToUnzip, Toast.LENGTH_SHORT).show());
        }
    }

}
