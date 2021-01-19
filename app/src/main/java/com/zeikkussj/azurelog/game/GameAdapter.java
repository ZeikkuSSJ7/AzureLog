package com.zeikkussj.azurelog.game;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import com.zeikkussj.azurelog.R;
import com.zeikkussj.azurelog.util.StaticFields;

import java.io.File;
import java.util.ArrayList;

public class GameAdapter extends BaseAdapter implements Filterable {
    private Context mContext;
    private ArrayList<Game> games;
    private CustomFilter filter;
    private ArrayList<Game> filterList;
    private Point p;

    private static int[] lines = {3, 2, 1, 1};
    private static int[] sizes = {16, 13, 14, 12};

    private static final int[] RECTANGLE_IDS = { // Rectángulos de diferentes categorías
            R.drawable.rectangle_plan_to_play,
            R.drawable.rectangle_playing,
            R.drawable.rectangle_on_hold,
            R.drawable.rectangle_dropped,
            R.drawable.rectangle_completed,
            R.drawable.rectangle_mastered
    };

    public GameAdapter(Context context, ArrayList<Game> games, Display display) {
        this.games = games;
        this.mContext = context;
        this.filterList = (ArrayList<Game>) games.clone();
        p = new Point();
        // Con el tamaño de la pantalla del usuario, calcularemos la altura de las celdas del GridView
        display.getSize(p);
    }

    @Override
    public int getCount() {
        return games.size();
    }

    @Override
    public Game getItem(int position) {
        return games.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0L;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Game game = games.get(position);
        if (game == null)
            return null;
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.game_grid_card, parent, false);
        LinearLayout wrapper = (LinearLayout) convertView;
        ImageView iv = convertView.findViewById(R.id.gridGameCover);

        // Obtenemos la carátula del juego y la cargamos con Glide
        File image = new File(mContext.getFilesDir().getAbsolutePath() + "/" +  game.getId() + StaticFields.DEFAULT_COVER_EXTENSION);
        Glide.with(mContext)
                .load(image)
                .asBitmap()
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .dontAnimate()
                .into(iv);

        TextView tv = wrapper.findViewById(R.id.gridGameTitle);
        tv.setText(game.getName());
        int numColumns = ((GridView)parent).getNumColumns();

        // Ajustamos el texto al número de columnas
        tv.setTextSize(maxSize(numColumns));
        tv.setMaxLines(maxLines(numColumns));

        wrapper.setBackgroundResource(checkStatus(game)); // Rectángulo según tipo
        wrapper.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, p.y/numColumns - 56));
        return wrapper;
    }

    private int maxLines(int numColumns){
        return lines[numColumns - 2];
    }

    private int maxSize(int numColumns){
        return sizes[numColumns - 2];
    }

    /**
     * Comprueba el estado de un juego y devuelve el ID del rectángulo correspondiente
     * @param game el juego
     * @return el ID del rectángulo
     */
    private int checkStatus(Game game) {
        for (int i = 0; i < StaticFields.STATUSES_IDS.length; i++) {
            if (game.getStatus() == StaticFields.STATUSES_IDS[i])
                return RECTANGLE_IDS[i];
        }
        return RECTANGLE_IDS[0];
    }

    @Override
    public Filter getFilter() {
        if(filter == null)
        {
            filter = new CustomFilter();
        }
        return filter;
    }

    /**
     * La clase CustomFilter se usa como interfaz para crear un filtro por
     * letras para la lista creada por la clase GameAdapter
     */
    class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if(constraint != null && constraint.length() > 0) {
                constraint = constraint.toString().toLowerCase();
                ArrayList<Game> filters =new ArrayList<>();
                for(int i = 0; i < filterList.size(); i++) {
                    if(filterList.get(i).getName().toLowerCase().contains(constraint)) {
                        filters.add(filterList.get(i));
                    }
                }
                results.count = filters.size();
                results.values = filters;
            } else {
                results.count = filterList.size();
                results.values = filterList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            games.clear();
            games.addAll((ArrayList<Game>) results.values);
            notifyDataSetChanged();
        }
    }

    /**
     * Borra el juego de la lista en la posición indicada
     * @param position la posición a borrar
     */
    public void remove(int position){
        games.remove(position);
        filterList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged(){
        super.notifyDataSetChanged();
    }
}