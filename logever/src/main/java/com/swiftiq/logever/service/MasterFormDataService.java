package com.swiftiq.logever.service;

import com.swiftiq.logever.builder.entity.Form;
import com.swiftiq.logever.model.DataQuery;
import com.swiftiq.logever.model.Table;
import com.swiftiq.logever.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MasterFormDataService {

    @Autowired
    JdbcTemplate jdbcTemplate;


    public DataQuery getAllFor(Form form, String column, String columnValue, String additionalColumns) {
        Table table = new Table();
        table.setName(form.getMasterTableName());
        String selectCols = "id," + form.getColumns();
        String[] sCArray = Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",");
        if (ApplicationUtil.isNotEmpty(additionalColumns)) {
            selectCols += "," + additionalColumns;
        }
        var finalColumns = selectCols;
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + "";

        if (null != column && column.length() > 0) {
            selectStmt += " WHERE " + column + " = '" + columnValue + "'";
        }
        return new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        return jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet, finalColumns.split(",")));
    }

    public DataQuery getAllFor(Form form, Map<String,String> filter, String additionalColumns) {
        Table table = new Table();
        table.setName(form.getMasterTableName());
        String selectCols = "id," + form.getColumns();
        if (ApplicationUtil.isNotEmpty(additionalColumns)) {
            selectCols += "," + additionalColumns;
        }
        var finalColumns = selectCols;
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + "";

        ArrayList<String> wheres = new ArrayList<>();

        filter.forEach((col,val)-> {
            wheres.add(col +"="+"'"+val+"'");
        });

        selectStmt += " WHERE 1=1 "+wheres.stream().reduce("",(con1,con2)->con1+" AND "+con2);
        String[] sCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        return new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        return jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet, finalColumns.split(",")));
    }

    public void updateEntryState(Form masterForm, String masterTableEntryId, String stateColumn, String stateValue, Map<String, String> value) {
        var result = getAllFor(masterForm, "id", masterTableEntryId, stateColumn);
        if (result.getData().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("No data found for id %s in master table %s", masterTableEntryId, masterForm.getName()));
        }
        var entryInProgress =
                result.getData().stream().filter(dataQuery -> stateValue.equalsIgnoreCase(dataQuery.get(stateColumn).toString())).findFirst();

        if (entryInProgress.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The entryId=%s is already %s in master table=%s, it can not be %s again", masterTableEntryId, stateValue, masterForm.getName(), stateValue));
        }
        Map<String, String> masterValues = new HashMap<>();
        masterValues.put("id", masterTableEntryId);
        masterValues.put(stateColumn, stateValue);
        masterValues.put("metadata", value.get("metadata"));
        jdbcTemplate.execute(masterForm.makeUpdateMasterEntryStateStmt(masterValues));
    }

    public void updateEntryState(Form masterForm, String masterTableEntryIdColumn, String idColumnValue, String stateColumn, String stateValue) {
        var result = getAllFor(masterForm, masterTableEntryIdColumn, idColumnValue, stateColumn);
        if (result.getData().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("No data found for Column %s value %s in master table %s", masterTableEntryIdColumn, idColumnValue, masterForm.getName()));
        }
        var entryInProgress =
                result.getData().stream().filter(dataQuery -> stateValue.equalsIgnoreCase(dataQuery.get(stateColumn).toString())).findFirst();

        if (entryInProgress.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Same value present for Master Data entry."));
        }
        Map<String, String> masterValues = new HashMap<>();
        masterValues.put(masterTableEntryIdColumn, idColumnValue);
        masterValues.put(stateColumn, stateValue);
        jdbcTemplate.execute(masterForm.makeUpdateMasterEntryStateStmtWithId(masterValues, masterTableEntryIdColumn));
    }

    public DataQuery getReferenceData(Form form,String colName,String where) {
        Table table = new Table();
        table.setName(form.getMasterTableName());
        String selectStmt = "SELECT "+colName+" from "+table.getName();
        if(!where.isEmpty()) {
            selectStmt = selectStmt + " WHERE "+where;
        }
        return new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        return jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet, colName.split(",")));
    }

}
