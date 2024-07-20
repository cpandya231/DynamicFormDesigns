package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.model.DataQuery;
import ai.smartfac.logever.model.Table;
import ai.smartfac.logever.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MasterFormDataService {

    @Autowired
    JdbcTemplate jdbcTemplate;


    public List<DataQuery> getAllFor(Form form, String column, String columnValue, String additionalColumns) {
        Table table = new Table();
        table.setName(form.getMasterTableName());
        String selectCols = "id," + form.getColumns();
        if (ApplicationUtil.isNotEmpty(additionalColumns)) {
            selectCols += "," + additionalColumns;
        }
        var finalColumns = selectCols;
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + "";

        if (null != column && column.length() > 0) {
            selectStmt += " WHERE " + column + " = '" + columnValue + "'";
        }

        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, finalColumns.split(",")));
    }

    public List<DataQuery> getAllFor(Form form, Map<String,String> filter, String additionalColumns) {
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

        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, finalColumns.split(",")));
    }

    public void updateEntryState(Form masterForm, String masterTableEntryId, String stateColumn, String stateValue, Map<String, String> value) {
        var result = getAllFor(masterForm, "id", masterTableEntryId, stateColumn);
        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("No data found for id %s in master table %s", masterTableEntryId, masterForm.getName()));
        }
        var entryInProgress =
                result.stream().filter(dataQuery -> stateValue.equalsIgnoreCase(dataQuery.getData().get(stateColumn))).findFirst();

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
        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("No data found for Column %s value %s in master table %s", masterTableEntryIdColumn, idColumnValue, masterForm.getName()));
        }
        var entryInProgress =
                result.stream().filter(dataQuery -> stateValue.equalsIgnoreCase(dataQuery.getData().get(stateColumn))).findFirst();

        if (entryInProgress.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Same value present for Master Data entry."));
        }
        Map<String, String> masterValues = new HashMap<>();
        masterValues.put(masterTableEntryIdColumn, idColumnValue);
        masterValues.put(stateColumn, stateValue);
        jdbcTemplate.execute(masterForm.makeUpdateMasterEntryStateStmtWithId(masterValues, masterTableEntryIdColumn));
    }

    public List<DataQuery> getReferenceData(Form form,String colName,String where) {
        Table table = new Table();
        table.setName(form.getMasterTableName());
        String selectStmt = "SELECT "+colName+" from "+table.getName();
        if(!where.isEmpty()) {
            selectStmt = selectStmt + " WHERE "+where;
        }
        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, colName.split(",")));
    }

}
