package com.zeikkussj.azurelog.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.game.Game;
import com.zeikkussj.azurelog.game.GameAdapter;
import com.zeikkussj.azurelog.game.GameConstants;
import com.zeikkussj.azurelog.game.GameDbHelper;
import com.zeikkussj.azurelog.game.GameInfoActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;

public abstract class ListenerHandler {

    /**
     * Maneja el clic sobre la puntuación o el estado de un juego
     * @param tv el <code>TextView</code> de origen
     * @param arrayID el ID de recurso de los items del tipo indicado
     * @param type el tipo, usado para el título del <code>AlertDialog</code>
     * @param tableColumn la columna a actualizar los datos
     * @param context el contexto de la aplicación
     * @param name el nombre del juego
     * @param platform la plataforma del juego
     * @return el evento en forma de <code>View.OnClickListener</code>
     */
    public static View.OnClickListener onClickChangeScoreStatusPlaytime(TextView tv, int arrayID, String type, String tableColumn, Context context, String name, String platform) {
        return v -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
            adb.setTitle(context.getString(R.string.selectScoreStatus, type)).setItems(arrayID, (dialog, which) -> {
                String[] values = context.getResources().getStringArray(arrayID);
                if (arrayID == R.array.validScoreOptions){
                    values[which] = values[which].split(" ")[0];
                    tv.setText(context.getString(R.string.score, values[which]));
                } else if (arrayID == R.array.validStatusOptions){
                    tv.setText(context.getString(R.string.status, values[which]));
                }
                tv.setTag(which);
                ContentValues cv = new ContentValues();
                cv.put(tableColumn, which);
                GameDbHelper.getInstance(context).updateGame(cv, name, platform);

            }).show();
        };
    }

    /**
     * Maneja el click en la celda del tiempo de juego.
     * @param context el contexto de la aplicación
     * @param tvPlaytime el <code>TextView</code> del tiempo de juego
     * @param name el nombre del juego
     * @param platform la plataforma del juego
     * @return el evento en forma de <code>View.OnClickListener</code>
     */
    public static View.OnClickListener onClickPlaytime(Context context, TextView tvPlaytime, String name, String platform){
        return v -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.dialog_edit, null);
            EditText editText = relativeLayout.findViewById(R.id.editText);
            editText.setText((String)tvPlaytime.getTag());
            adb.setTitle(context.getString(R.string.playtime))
                    .setView(relativeLayout)
                    .setPositiveButton(context.getString(R.string.setPlaytime), (dialog, which) -> {
                        String newData = context.getString(R.string.defaultPlaytime, editText.getText().toString());
                        tvPlaytime.setTag(newData);
                        tvPlaytime.setText(newData);
                        ContentValues cv = new ContentValues();
                        cv.put(GameConstants.GameEntry.PLAYTIME, Double.parseDouble(editText.getText().toString()));
                        GameDbHelper.getInstance(context).updateGame(cv, name, platform);
                    }).setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> Toast.makeText(context, R.string.playtimeNotUpdated, Toast.LENGTH_SHORT).show()).show();
        };
    }

    /**
     * Maneja el click en un <code>TextView</code> con fecha
     * @param tv el <code>TextView</code> de origen
     * @param stringID el ID de recurso de una cadena formateada (puede ser 0)
     * @param tableColumn la columna a actualizar los datos
     * @param activity el <code>GameInfoActivity</code> de origen
     * @param name el nombre del juego
     * @param platform la plataforma del juego
     * @return el evento en forma de <code>View.OnClickListener</code>
     */
    public static View.OnClickListener onClickAlertDate(TextView tv, int stringID, String tableColumn, GameInfoActivity activity, String name, String platform){
        return v -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.AlertDialogCustom));
            View view = LayoutInflater.from(activity).inflate(R.layout.dialog_date_setter, null);
            DatePicker date = view.findViewById(R.id.dateSetter);
            String curDateString = tv.getTag().toString();
            adb.setView(view).setPositiveButton(activity.getString(R.string.setDate), (dialog, which) -> {
                GameDbHelper db = GameDbHelper.getInstance(activity);
                String dateString = String.format(Locale.ENGLISH, "%d-%s-%s", date.getYear(), Util.correctPattern(String.valueOf(date.getMonth() + 1)), Util.correctPattern(String.valueOf(date.getDayOfMonth())));
                tv.setText(stringID != 0 ? activity.getString(stringID, dateString) : dateString);
                ContentValues cv = new ContentValues();
                cv.put(tableColumn, dateString);
                db.updateGame(cv, name, platform);
                if(tableColumn.equals(GameConstants.GameEntry.FINISH_DATE)) {
                    TextView tvStatus = activity.findViewById(R.id.tvStatus);
                    int oldStatus = (int) tvStatus.getTag();
                    if (oldStatus != StaticFields.STATUSES_IDS[4] && oldStatus != StaticFields.STATUSES_IDS[5]){
                        AlertDialog.Builder adb2 = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.AlertDialogCustom));
                        adb2.setTitle(R.string.statusChange).setMessage(R.string.setAsCompleted).setPositiveButton(R.string.yes, (dialog1, which1) -> {
                            ContentValues cv2 = new ContentValues();
                            cv2.put(GameConstants.GameEntry.STATUS, 4);
                            tvStatus.setText(activity.getString(R.string.status, activity.getResources().getStringArray(R.array.validStatusOptions)[4]));
                            db.updateGame(cv2, name, platform);
                        }).setNegativeButton(R.string.no, null).show();
                    }
                }
            }).setNegativeButton(R.string.cancel, (dialog, which) -> Toast.makeText(activity, R.string.dateNotSet, Toast.LENGTH_SHORT).show());
            if (!curDateString.equalsIgnoreCase(activity.getString(R.string.notDefinedYet))){
                String[] dateSplit = curDateString.split("-");
                date.updateDate(Integer.parseInt(dateSplit[0]), Integer.parseInt(dateSplit[1]) - 1, Integer.parseInt(dateSplit[2]));
            }
            adb.show();
        };
    }

    /**
     * Maneja el click en un <code>EditText</code> con fecha
     * @param et el <code>EditText</code> de origen
     * @param context el contexto de la aplicación
     * @return el evento en forma de <code>View.OnClickListener</code>
     */
    public static View.OnClickListener onClickDatePicker(EditText et, Context context){
        return v -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_date_setter, null);
            DatePicker date = view.findViewById(R.id.dateSetter);
            adb.setView(view);
            String textDate = et.getText().toString();
            adb.setPositiveButton(context.getString(R.string.setReleaseDate), (dialog, which) -> {
                String dateString = String.format(Locale.ENGLISH, "%d-%s-%s", date.getYear(), Util.correctPattern(String.valueOf(date.getMonth() + 1)), Util.correctPattern(String.valueOf(date.getDayOfMonth())));
                et.setText(dateString);
            }).setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> Toast.makeText(context, R.string.releaseDateNotSent, Toast.LENGTH_SHORT).show());
            String[] array = textDate.split("-");
            date.updateDate(Integer.parseInt(array[0]), Integer.parseInt(array[1]) - 1, Integer.parseInt(array[2]));
            adb.show();
        };
    }

    /**
     * Maneja el click sobre una celda del <code>GameInfoActivity</code>
     * @param context el contexto de la aplicación
     * @param tv el TextView que utiliza el evento
     * @param titleID el ID de recurso para el título del <code>AlertDialog</code>
     * @param positiveButtonID el ID de recurso del botón positivo del <code>AlertDialog</code>
     * @param toastMessageID el ID de recurso del <code>Toast</code> resultante si cancelas la operación
     * @param columnName el nombre de la columna a actualizar
     * @param updateView si deseas actualizar la <code>TextView</code> modificada
     * @param name el nombre del juego
     * @param platform la plataforma del juego
     * @return el evento en forma de <code>View.OnClickListener</code>
     */
    public static View.OnClickListener onClickUpdateText(Context context, TextView tv, int titleID, int positiveButtonID, int toastMessageID, String columnName, boolean updateView, String name, String platform){
        return v -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.dialog_edit, null);
            EditText editText = relativeLayout.findViewById(R.id.editText);
            editText.setText((String)tv.getTag());
            adb.setTitle(context.getString(titleID)).setView(relativeLayout).setPositiveButton(context.getString(positiveButtonID), (dialog, which) -> {
                String newData = editText.getText().toString();
                tv.setTag(newData);
                if (updateView)
                    tv.setText(newData);
                ContentValues cv = new ContentValues();
                cv.put(columnName, newData);
                GameDbHelper.getInstance(context).updateGame(cv, name, platform);
            }).setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> Toast.makeText(context, toastMessageID, Toast.LENGTH_SHORT).show());
            adb.show();
        };
    }

    /**
     * Maneja el clic largo sobre un juego en la lista, usado para borrar el juego
     * @param context el contexto de la aplicación
     * @param gameAdapter el <code>GameAdapter</code> del <code>Activity</code> original
     * @param games la lista de juegos
     * @return el evento en forma de <code>AdapterView.OnItemClickListener</code>
     */
    public static AdapterView.OnItemLongClickListener onLongClickGame(Context context, GameAdapter gameAdapter, ArrayList<Game> games) {
        return (parent, view, position, id) -> {
            AlertDialog.Builder alertOptions = new AlertDialog.Builder(context);
            alertOptions.setTitle(context.getString(R.string.gameOptions));
            alertOptions.setItems(R.array.gameOptions, (dialog, which) -> {
                Game selectedGame = games.get(position);
                AlertDialog.Builder alertDelete = new AlertDialog.Builder(context);
                alertDelete.setTitle(context.getString(R.string.deleteGameFromDatabase)).setMessage(context.getString(R.string.areYouSureToDelete, selectedGame.getName()));
                alertDelete.setPositiveButton(context.getString(R.string.yes), (dialog1, which1) -> {
                    String gameID = selectedGame.getId();
                    GameDbHelper.getInstance(context).deleteGame(gameID);
                    File f = new File(context.getFilesDir().getAbsolutePath(), gameID + StaticFields.DEFAULT_COVER_EXTENSION);
                    if (f.delete())
                        Log.i("Delete Game", "Succesfully removed " + games.get(position).getName() + " from database and " + gameID + StaticFields.DEFAULT_COVER_EXTENSION + " from file dir");
                    gameAdapter.remove(position);
                    JSONHandler.updateJSON(context);
                }).setNegativeButton(context.getString(R.string.no), null);
                alertDelete.show();
            });
            alertOptions.show();
            gameAdapter.notifyDataSetChanged();
            return true;
        };
    }

    /**
     * Maneja el clic sobre un juego, que lanza el <code>GameInfoActivity</code> de ese juego
     * @param context el contexto de la aplicación
     * @param games la lista de juegos
     * @return el evento en forma de <code>AdapterView.OnItemClickListener</code>
     *
     * @see GameInfoActivity
     */
    public static AdapterView.OnItemClickListener onClickGame(AppCompatActivity context, ArrayList<Game> games) {
        return (parent, view, position, id) -> {
            Intent i = new Intent(context, GameInfoActivity.class);
            i.putExtra(StaticFields.KEY_INFO_DATA, games.get(position).toArray());
            i.putExtra(StaticFields.KEY_GAME_POS, position);
            context.startActivityForResult(i, Activity.RESULT_CANCELED);
        };
    }

    /**
     * Maneja el clic largo sobre la carátula de un juego
     * @param activity el activity de origen
     * @param cover la carátula seleccionada
     * @param gameName el nombre del juego
     * @param gameId el ID del juego
     */
    public static void onCoverLongClick(GameInfoActivity activity, ImageView cover, String gameName, String gameId) {
        cover.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.gameCoverOptions)
                    .setItems(R.array.gameCoverOptionsItems, (dialog, which) -> {
                        if (which == 0) {
                            builder.setTitle(R.string.changeGameCover)
                                    .setMessage(R.string.doYouWishToChangeCover)
                                    .setNegativeButton(R.string.no, null)
                                    .setPositiveButton(R.string.yes, (dialog2, which2) -> ImageHandler.startImagePickIntent(activity)).show();
                        } else if (which == 1) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, StaticFields.PERMISSION_OK);
                                    return;
                                }
                            }
                            ImageHandler.saveImageToExternalStorage(activity, cover, gameName, gameId);

                        }
                    }).show();
            return true;
        });
    }

    /**
     * Activa los eventos en los tres FABs de cada <code>Activity</code> de categorías
     * @param context el contexto de la aplicación
     * @param gameAdapter el <code>GameAdapter</code> del <code>Activity</code> original
     * @param games la lista de juegos
     * @param fab1 el FAB de reordenar
     * @param fab2 el FAB de un juego nuevo que jugar
     * @param fab3 el FAB de un juego aleatorio de tu lista
     */
    public static void setGridFABListeners(AppCompatActivity context, GameAdapter gameAdapter, ArrayList<Game> games, FloatingActionButton fab1, FloatingActionButton fab2, FloatingActionButton fab3) {
        fab3.setOnClickListener(v -> {
            Collections.reverse(games);
            gameAdapter.notifyDataSetChanged();
        });
        fab2.setOnClickListener(v -> {
            Random ran = new Random();
            ArrayList<Game> newGames = Util.getDataFromDB(context, Util.FLAG_RANDOM_PLAN_TO_PLAY_GAMES);
            if (newGames.size() >= 1) {
                String[] data = newGames.get(ran.nextInt(newGames.size())).toArray();
                if (Util.isSet(data[0]) && Util.isSet(data[1]))
                    context.startActivity(new Intent(context, GameInfoActivity.class).putExtra(StaticFields.KEY_INFO_DATA, data));
            }
        });
        fab1.setOnClickListener(v -> {
            if (games.size() >= 1) {
                Random ran = new Random();
                String[] data = games.get(ran.nextInt(games.size())).toArray();
                if (Util.isSet(data[0]) && Util.isSet(data[1]))
                    context.startActivity(new Intent(context, GameInfoActivity.class).putExtra(StaticFields.KEY_INFO_DATA, data));
            }
        });
    }
}
