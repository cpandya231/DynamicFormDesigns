package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.model.DataQuery;
import ai.smartfac.logever.model.MultiSelectResponse;
import ai.smartfac.logever.model.Table;
import ai.smartfac.logever.model.TextResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserService userService;

    public List<String> getAllAccessTypes(String employeeID, String application, String idType) {
        ArrayList<String> output = new ArrayList<String>();
        Table table = new Table();
        table.setName("E_UAM");
        Table metaTable = new Table();
        String selectCols = "required_roles,application_request_type";
        String selectStmt = "SELECT required_roles, application_request_type from " + table.getName() + " l where l.application_name = '"+application+
                "' and (l.employee_id = '"+employeeID+"' or l.other_employee_id = '"+employeeID+"' or l.service_engineer_id = '"+employeeID+"') and" +
                " l.state = 'Acknowledge and Close' order by log_update_dt desc, log_create_dt desc limit 1";
        System.out.println(selectStmt);
        User user;
        Boolean userDisabled = false;
        try {
            user = userService.getUserByEmployeeCode(employeeID);
            userDisabled = !user.getIsActive();

        } catch (Exception ex) {
            System.out.println("No user found for "+employeeID);
        }

        List<DataQuery> result =  jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",")));
        if(result.size() > 0 && !result.get(0).getData().get("application_request_type").equalsIgnoreCase("De-activation")) {
            if(userDisabled) {
                output.add("De-Activation");
                return output;
            }
            else {
                output.add("Role Change");
                output.add("Password Reset/Unlock");
                output.add("De-Activation");
                return output;
            }
        } else if(!userDisabled) {
            output.add("Activation");
            return output;
        } else {
            return output;
        }
    }

    public MultiSelectResponse getAccessRoles(String employeeID, String application, String idType) {
        String selectColumns = "required_roles,application_request_type";
        String selectStmt = "SELECT required_roles, application_request_type from f_e_uam_lgs l where l.application_name = '"+application+"' " +
                "and (l.employee_id = '"+employeeID+"' or l.other_employee_id = '"+employeeID+"' or l.service_engineer_id = '"+employeeID+"') and l.state = 'Acknowledge and Close' order by log_update_dt desc, log_create_dt desc limit 1";
        System.out.println(selectStmt);

        List<DataQuery> resultOne =  jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectColumns.split(",")).collect(Collectors.joining(",")).split(",")));

        ArrayList<String> allRoles = new ArrayList<>();
        String selectCols = "roles";
        selectStmt = "SELECT distinct system_role as roles from f_mstr_system_inventory_lgs l where l.system_name = '"+application+"'";
        System.out.println(selectStmt);

        List<DataQuery> result =  jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",")));
        allRoles = new ArrayList<>(result.stream().map(d->d.getData().get("roles")).collect(Collectors.toList()));
        if(resultOne.size() > 0 && !resultOne.get(0).getData().get("application_request_type").equalsIgnoreCase("De-activation")) {
            String roles = resultOne.get(0).getData().get("required_roles");
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
                " l.state = 'Acknowledge and Close' and l.application_request_type = 'Activation' order by log_update_dt desc, log_create_dt desc limit 1";
        System.out.println(selectStmt);

        List<DataQuery> result =  jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",")));
        if(result.size() > 0) {
            return new TextResponseModel(result.get(0).getData().get("allocated_user_id"));
        } else {
            return new TextResponseModel("User ID not found");
        }
    }

    public List<DataQuery> fetchAllPendingEntries(String startDt) {
        String selectCols = "create_dt,assigned_department,assigned_user,entry_id,form_id,pending_hod";
        String selectStmt = "SELECT create_dt,assigned_department,assigned_user,entry_id,form_id,pending_hod from pending_entry where create_dt < '"+startDt+"'";
        System.out.println(selectStmt);
        List<DataQuery> result =  jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",")));
        return  result;
    }
}
