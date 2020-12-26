package ru.otus.jdbc.mapper;

import java.util.List;

public interface SQLEntity<V> {

    V createFromRow(List<Object> row);

    List<Object> getRow(V javaEntity);

    List<Object> getArgumentsForUpdate(V javaEntity);

    int numColumns();

    String getIdColumnName();

    String getSelectByIdTemplate();

    String getUpdateByIdTemplate();

    String getInsertTemplate();

    String getInsertOrUpdate();
}
