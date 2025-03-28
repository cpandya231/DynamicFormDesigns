package com.swiftiq.logever.service;

import com.swiftiq.logever.kraftos.entity.User;
import com.swiftiq.logever.model.DataQuery;
import com.swiftiq.logever.model.MultiSelectResponse;
import com.swiftiq.logever.model.Table;
import com.swiftiq.logever.model.TextResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserService userService;

    public List<String> getComputerIDs(String userAccountType) {
        ArrayList<String> output = new ArrayList<String>();
        Table table = new Table();
        table.setName("E_UAM");
        Table metaTable = new Table();
        String selectCols = "computer_id";
        String selectStmt = "SELECT computer_id from " + table.getName() + " l where l.user_account_type = '"+userAccountType+
                "' and l.state not in ('Acknowledge and Close','Withdraw Request')";
        System.out.println(selectStmt);
        String[] sCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        DataQuery result = new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        List<DataQuery> result =  jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",")));

        String systemInventorySelectCols = "system_name";
        String systemInventorySelectStmt = "SELECT system_name from f_mstr_system_inventory_lgs where system_category='COMPUTER'";
        String[] sMCArray = Arrays.stream(systemInventorySelectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        DataQuery masterResult = new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        List<DataQuery> masterResult =  jdbcTemplate.query(systemInventorySelectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(systemInventorySelectCols.split(",")).collect(Collectors.joining(",")).split(",")));

        List<String> inProgressComputerIds = result.getData().stream().map(row->row.get("computer_id").toString()).collect(Collectors.toList());
        return masterResult.getData().stream().map(row->row.get("system_name").toString()).filter(name->!inProgressComputerIds.contains(name)).collect(Collectors.toList());
    }

    public List<String> getAllAccessTypes(String employeeID, String application) {
        ArrayList<String> output = new ArrayList<String>();
        Table table = new Table();
        table.setName("E_UAM");
        Table metaTable = new Table();
        String selectCols = "id,required_roles,application_request_type,state";
        String selectStmt = "SELECT id, required_roles, application_request_type, state from " + table.getName() + " l where l.application_name = '"+application+
                "' and (l.employee_id = '"+employeeID+"' or l.other_employee_id = '"+employeeID+"' or l.service_engineer_id = '"+employeeID+"') order by log_update_dt is null desc, log_update_dt desc, log_create_dt desc limit 1";
        System.out.println(selectStmt);
        User user;
        Boolean userDisabled = false;
        try {
            user = userService.getUserByEmployeeCode(employeeID);
            userDisabled = !user.getIsActive();

        } catch (Exception ex) {
            System.out.println("No user found for "+employeeID);
        }
        String[] sMCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        DataQuery result = new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        List<DataQuery> result =  jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",")));
        if(result.getData().size() > 0) {
            Map<String,Object> firstRow = result.getData().get(0);
            if(!firstRow.get("state").toString().equalsIgnoreCase("Acknowledge and Close") && !firstRow.get("state").toString().equalsIgnoreCase("Withdraw Request")
                && !firstRow.get("state").toString().equalsIgnoreCase("Deactivate and Close") && !firstRow.get("state").toString().equalsIgnoreCase("Deactivated")) {
                output.add("ERR: Request already in progress : Request ID - "+firstRow.get("id"));
                return output;
            } else if(firstRow.get("state").toString().equalsIgnoreCase("Deactivate and Close") || firstRow.get("state").toString().equalsIgnoreCase("Withdraw Request")
             || firstRow.get("state").toString().equalsIgnoreCase("Deactivated")) {
                output.add("Activation");
                return output;
            }
            else {
                if (!firstRow.get("application_request_type").toString().equalsIgnoreCase("De-activation")) {
                    if (userDisabled) {
                        output.add("De-Activation");
                        return output;
                    } else {
                        output.add("Role Change");
                        output.add("Password Reset/Unlock");
                        output.add("De-Activation");
                        return output;
                    }
                }
            }
        } else if (!userDisabled) {
            output.add("Activation");
            return output;
        } else {
            return output;
        }
        return output;
    }

    public MultiSelectResponse getAccessRoles(String employeeID, String application, String category) {
        String selectColumns = "required_roles,application_request_type,state";
        String selectStmt = "SELECT required_roles, application_request_type, state from f_e_uam_lgs l where l.application_name = '"+application+"' " +
                "and (l.employee_id = '"+employeeID+"' or l.other_employee_id = '"+employeeID+"' or l.service_engineer_id = '"+employeeID+"') and l.state in ('Acknowledge and Close','Deactivated','Deactivate and Close') and l.application_request_type <> 'Password Reset/Unlock' order by log_update_dt is null desc, log_update_dt desc, log_create_dt desc limit 1";
        System.out.println(selectStmt);
        String[] sMCArray = Arrays.stream(selectColumns.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        DataQuery resultOne = new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        List<DataQuery> resultOne =  jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectColumns.split(",")).collect(Collectors.joining(",")).split(",")));

        ArrayList<String> allRoles = new ArrayList<>();
        String selectCols = "roles";
        if(category.equalsIgnoreCase("Application")) {
            selectStmt = "SELECT distinct system_role as roles from f_mstr_system_inventory_lgs l where l.system_name = '"+application+"'";
        } else {
            selectStmt = "SELECT distinct system_role as roles from f_mstr_system_inventory_lgs l where concat(l.system_name,' - ',l.system_id) = '"+application+"'";
        }
        System.out.println(selectStmt);
        String[] sCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        DataQuery result = new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        List<DataQuery> result =  jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",")));
        allRoles = new ArrayList<>(result.getData().stream().map(d->d.get("roles").toString()).collect(Collectors.toList()));
        if(resultOne.getData().size() > 0 && !resultOne.getData().get(0).get("application_request_type").toString().equalsIgnoreCase("De-activation")
                && !resultOne.getData().get(0).get("state").toString().equalsIgnoreCase("Deactivated")
                && !resultOne.getData().get(0).get("state").toString().equalsIgnoreCase("Deactivate and Close")) {
            String roles = resultOne.getData().get(0).get("required_roles").toString();
            System.out.println(roles);
            ArrayList<String> userRoles = new ArrayList<>(Arrays.asList(roles.split(",")));
            return new MultiSelectResponse(allRoles,userRoles);
        } else {
            return new MultiSelectResponse(allRoles,new ArrayList<>());
        }
    }

    public TextResponseModel getExistingID(String employeeID, String application) {
        String selectCols = "allocated_user_id";
        String selectStmt = "SELECT allocated_user_id from f_e_uam_lgs l where l.application_name = '"+application+
                "' and (l.employee_id = '"+employeeID+"' or l.other_employee_id = '"+employeeID+"' or l.service_engineer_id = '"+employeeID+"') and" +
                " l.state = 'Acknowledge and Close' and l.application_request_type = 'Activation' order by log_update_dt is null desc, log_update_dt desc, log_create_dt desc limit 1";
        System.out.println(selectStmt);
        String[] sMCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        DataQuery result = new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        List<DataQuery> result =  jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",")));
        if(result.getData().size() > 0) {
            return new TextResponseModel(result.getData().get(0).get("allocated_user_id").toString());
        } else {
            return new TextResponseModel("User ID not found");
        }
    }

    public DataQuery fetchAllPendingEntries(String startDt) {
        String selectCols = "request_type,log_create_dt,entry_created_by,create_dt,assigned_department,assigned_user,entry_id,form_id,pending_hod";
        String selectStmt = "SELECT request_type,log_create_dt,entry_created_by,create_dt,assigned_department,assigned_user,entry_id,form_id,pending_hod from pending_entry p inner join f_e_uam_lgs f on p.entry_id = f.id where create_dt < '"+startDt+"'";
        System.out.println(selectStmt);
        String[] sMCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        DataQuery result = new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        List<DataQuery> result =  jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",")));
        return  result;
    }

    public DataQuery fetchAllAccountsToBeDisabled() {
        String selectCols = "id,service_engineer_name,request_type,application_name,required_roles,user_disabling_date";
        String selectStmt = "SELECT id,service_engineer_name,request_type,application_name,required_roles,user_disabling_date from f_e_uam_lgs" +
                " WHERE datediff(user_disabling_date,current_date) = 1";
        System.out.println(selectStmt);
        String[] sMCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        DataQuery result = new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        List<DataQuery> result =  jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",")));
        return  result;
    }

    public DataQuery fetchSystemNamesWithID(String category) {
        String selectCols = "systems";
        String selectStmt = "select case when system_type in ('Laboratory System','Manufacturing System') then concat(system_name,' - ',system_id) else system_name end systems from f_mstr_inventory_master_lgs" +
                " WHERE system_type='"+category+"' order by systems";
        System.out.println(selectStmt);
        String[] sMCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        DataQuery result = new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        List<DataQuery> result =  jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",")));
        return  result;
    }

    public void updateAvlQuantity(String batchNo, Integer quantity) {
        String updateDoFetchQuery = "UPDATE f_mstr_material_received_lgs SET available_quantity = '"+quantity+"', entry_state = 'Issued' where batch_no = '"+batchNo+"'";
        jdbcTemplate.update(updateDoFetchQuery);
    }
}
