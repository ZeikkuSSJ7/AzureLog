package com.zeikkussj.azurelog.util;

import androidx.annotation.Nullable;

public class QueryBuilder {
    private String query;
    private String select;
    private String from;
    private String where;
    private String groupBy;
    private String orderBy;

    /**
     * Construye una cadena 'SELECT' con las columnas indicadas
     * @param columns las columnas a seleccionar
     * @return el objeto para concatenar métodos
     */
    public QueryBuilder select(String ... columns){
        if (select == null){
            select = "select";
        }
        select += putAll(columns);
        return this;
    }

    /**
     * Construye una cadena 'FROM' con las tablas a buscar
     * @param tables las tablas a seleccionar
     * @return el objeto para concatenar métodos
     */
    public QueryBuilder from(String ... tables){
        if (from == null){
            from = " from";
        }
        from += putAll(tables);
        return this;
    }

    /**
     * Construye una cadena 'WHERE' con la condición, valor y operador correspondientes
     * @param column la columna a filtrar
     * @param condition la condición a realizar (=, <=, etc)
     * @param value el valor a buscar
     * @param logicPath el operador AND, OR, XOR, etc
     * @return el objeto para concatenar métodos
     */
    public QueryBuilder where(String column, @Nullable String condition, String value, @Nullable String logicPath){
        if (condition == null){
            condition = "=";
        }
        if (condition.equals("like"))
            value = "%" + value + "%";
        if (logicPath == null){
            logicPath = "AND";
        }
        if (where == null){
            where = " where " + column + " " + condition + " '" + value + "' ";
        } else {
            where += logicPath + " " + column + " " + condition + " '" + value + "' ";
        }
        return this;
    }

    /**
     * Construye una cadena 'GROUP BY' con la columna indicada
     * @param column la columna a incluir
     * @return el objeto para concatenar métodos
     */
    public QueryBuilder groupBy(String column){
        if (groupBy == null){
            groupBy = " group by " + column;
        } else {
            groupBy += ", " + column;
        }
        return this;
    }

    /**
     * Construye una cadena 'ORDER BY' con la columna y orden indicados
     * @param column la columna a ordenar
     * @param order asc o desc
     * @return el objeto para concatenar métodos
     */
    public QueryBuilder orderBy(String column, @Nullable String order){
        if (order == null)
            order = "asc";
        if (orderBy == null)
            orderBy = " order by " + column + " " + order;
        // TODO: fix order by
        else
            orderBy += ", " + column;

        return this;
    }

    /**
     * Vacia el contenido de los parámetros
     */
    public void flush(){
        query = null;
        select = null;
        from = null;
        where = null;
        groupBy = null;
        orderBy = null;
    }

    /**
     * @return la consulta SQL construida
     */
    public String getCompiledQuery(){
        if (query == null){
            query = create(select) + create(from) + create(where) + create(groupBy) + create(orderBy) ;
        }
        return query;
    }

    /**
     * Añade todos los argumentos a una cadenas
     * @param args los argumentos de un SELECT O WHERE
     * @return la cadena completa
     */
    private String putAll(String[] args){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            builder.append(" ").append(args[i]);
            if (i != args.length - 1)
                builder.append(",");
        }
        return builder.toString();
    }

    /**
     * Devuelve la cadena de entrada sin nulos
     * @param input la cadena que posiblemente sea nula
     * @return el resultado de la comprobación
     */
    private String create(String input){
        return input != null ? input : "";
    }
}
