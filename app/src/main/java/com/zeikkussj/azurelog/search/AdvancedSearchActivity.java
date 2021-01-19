package com.zeikkussj.azurelog.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.util.ListenerHandler;
import com.zeikkussj.azurelog.util.QueryBuilder;
import com.zeikkussj.azurelog.util.StaticFields;
import com.zeikkussj.azurelog.util.Util;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.zeikkussj.azurelog.game.GameConstants.GameEntry.DEVELOPER;
import static com.zeikkussj.azurelog.game.GameConstants.GameEntry.FAVOURITE;
import static com.zeikkussj.azurelog.game.GameConstants.GameEntry.FINISH_DATE;
import static com.zeikkussj.azurelog.game.GameConstants.GameEntry.NAME;
import static com.zeikkussj.azurelog.game.GameConstants.GameEntry.PLATFORM;
import static com.zeikkussj.azurelog.game.GameConstants.GameEntry.PLAYTIME;
import static com.zeikkussj.azurelog.game.GameConstants.GameEntry.PUBLISHER;
import static com.zeikkussj.azurelog.game.GameConstants.GameEntry.RELEASEDATE;
import static com.zeikkussj.azurelog.game.GameConstants.GameEntry.START_DATE;

public class AdvancedSearchActivity extends AppCompatActivity {
    private static final int[] ids = {
            R.id.etAdvancedSearchGameName,
            R.id.etAdvancedSearchDeveloper,
            R.id.etAdvancedSearchPublisher,
            R.id.etAdvancedSearchReleaseDate,
            R.id.cbAdvancedSearchFavourite,
            R.id.etAdvancedSearchPlatform,
            R.id.etAdvancedSearchStartDate1,
            R.id.etAdvancedSearchStartDate2,
            R.id.etAdvancedSearchFinishDate1,
            R.id.etAdvancedSearchFinishDate2,
            R.id.etAdvancedSearchPlayTime1,
            R.id.etAdvancedSearchPlayTime2
    };
    private EditText etGameName;
    private EditText etDeveloper;
    private EditText etPublisher;
    private EditText etReleaseDate;
    private CheckBox cbFavourite;
    private EditText etPlatform;
    private EditText etStartDate1;
    private EditText etStartDate2;
    private EditText etFinishDate1;
    private EditText etFinishDate2;
    private EditText etPlaytime1;
    private EditText etPlaytime2;
    private QueryBuilder queryBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_search);
        etGameName = findViewById(ids[0]);
        etDeveloper = findViewById(ids[1]);
        etPublisher = findViewById(ids[2]);
        etReleaseDate = findViewById(ids[3]);
        etReleaseDate.setOnClickListener(ListenerHandler.onClickDatePicker(etReleaseDate, this));
        cbFavourite = findViewById(ids[4]);
        etPlatform = findViewById(ids[5]);
        etStartDate1 = findViewById(ids[6]);
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        etStartDate1.setOnClickListener(ListenerHandler.onClickDatePicker(etStartDate1, this));
        etStartDate2 = findViewById(ids[7]);
        etStartDate2.setOnClickListener(ListenerHandler.onClickDatePicker(etStartDate2, this));
        etStartDate2.setText(date);
        etFinishDate1 = findViewById(ids[8]);
        etFinishDate1.setOnClickListener(ListenerHandler.onClickDatePicker(etFinishDate1, this));
        etFinishDate2 = findViewById(ids[9]);
        etFinishDate2.setOnClickListener(ListenerHandler.onClickDatePicker(etFinishDate2, this));
        etFinishDate2.setText(date);
        etPlaytime1 = findViewById(ids[10]);
        etPlaytime2 = findViewById(ids[11]);
        queryBuilder = new QueryBuilder();
        Util.openToolbar(this, R.id.my_toolbar);
    }

    /**
     * Busca un juego recopilando todos los datos de la búsqueda que hayas indicado y lanza
     * un Activity con los resultados
     * @param view la vista usada como enlace del evento
     */
    public void searchGameWithArgs(View view) {
        String name = etGameName.getText().toString();
        String developer = etDeveloper.getText().toString();
        String publisher = etPublisher.getText().toString();
        String releaseDate = etReleaseDate.getText().toString();
        boolean favourite = cbFavourite.isChecked();
        String platform = etPlatform.getText().toString();
        String startDate1 = etStartDate1.getText().toString();
        String startDate2 = etStartDate2.getText().toString();
        String finishDate1 = etFinishDate1.getText().toString();
        String finishDate2 = etFinishDate2.getText().toString();
        String playtime1 = etPlaytime1.getText().toString();
        String playtime2 = etPlaytime2.getText().toString();
        queryBuilder.select("*");
        queryBuilder.from("game");
        addToQuery(name, NAME, null);
        addToQuery(developer, DEVELOPER, null);
        addToQuery(publisher, PUBLISHER, null);
        addToQuery(releaseDate.equals(getString(R.string.newGameDefaultDate)) ? "" : releaseDate, RELEASEDATE, null);
        addToQuery(favourite ? "1" : "0", FAVOURITE, "=");
        addToQuery(platform, PLATFORM, null);
        addToQuery(checkDatePattern(startDate1), START_DATE, ">=");
        addToQuery(checkDatePattern(startDate2), START_DATE, "<=");
        addToQuery(checkDatePattern(finishDate1), FINISH_DATE, ">=");
        addToQuery(checkDatePattern(finishDate2), FINISH_DATE, "<=");
        addToQuery(playtime1, PLAYTIME, ">=");
        addToQuery(playtime2, PLAYTIME, "<=");
        queryBuilder.orderBy(FINISH_DATE, null);
        Intent intent = new Intent(this, AdvancedSearchResultsActivity.class);
        intent.putExtra(StaticFields.KEY_INFO_DATA, queryBuilder.getCompiledQuery());
        Log.d("ZZZ", queryBuilder.getCompiledQuery());
        queryBuilder.flush();
        startActivity(intent);

    }

    /**
     * Usado en conjunto con {@link QueryBuilder} ¡, construye la consulta realizada a la base
     * de datos
     * @param field el campo a buscar
     * @param column la columna a filtrar
     * @param condition la condición del operador (like, >=, ==...)
     */
    private void addToQuery(String field, String column, @Nullable String condition){
        String logicalPath = "AND";
        if (condition == null) condition = "like";
        if (Util.isSet(field))
            queryBuilder.where(column, condition, field, logicalPath);
    }

    /**
     * Comprueba que el patrón de la fecha sea correcto
     * @param date la fecha
     * @return la fecha bien posicionada en el caso de que no lo estuviera
     */
    private String checkDatePattern(String date){
        if (Util.isSet(date)){
            String[] dateSplit = date.split("-");
            return dateSplit[0] + "-" + Util.correctPattern(dateSplit[1]) + "-" + Util.correctPattern(dateSplit[2]);
        }
        return date;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Util.onOptionItemSelected(item, this, null);
        return super.onOptionsItemSelected(item);
    }
}
