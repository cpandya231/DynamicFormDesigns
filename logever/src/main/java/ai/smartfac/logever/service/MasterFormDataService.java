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


    public void updateEntryState(Form masterForm, String masterTableEntryId, String stateValue) {
        var result = getAllFor(masterForm, "id", masterTableEntryId, "entry_state");
        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("No data found for id %s in master table %s", masterTableEntryId, masterForm.getName()));
        }
        var entryInProgress =
                result.stream().filter(dataQuery -> stateValue.equalsIgnoreCase(dataQuery.getData().get("entry_state"))).findFirst();

        if (entryInProgress.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The entryId=%s is already %s in master table=%s, it can not be %s again", masterTableEntryId, stateValue, masterForm.getName(), stateValue));
        }
        Map<String, String> masterValues = new HashMap<>();
        masterValues.put("id", masterTableEntryId);
        masterValues.put("entry_state", stateValue);
        jdbcTemplate.execute(masterForm.makeUpdateMasterEntryStateStmt(masterValues));
    }


}
