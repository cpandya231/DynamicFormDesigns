package ai.smartfac.logever.controller;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

import ai.smartfac.logever.entity.Department;
import ai.smartfac.logever.entity.EmailDetails;
import ai.smartfac.logever.model.DataQuery;
import ai.smartfac.logever.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EscalationMatrixController {
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Autowired
    CustomService customService;

    @Autowired
    EmailService emailService;

    @Autowired
    UserService userService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    FormService formService;

    @Value("${app.url}") private String appUrl;

    @Scheduled(cron = "0 0 9 * * *")
//    @Scheduled(fixedRate = 5000)
    public void sendEscalations() {
        LocalDate currentDate = LocalDate.now();
        LocalDateTime currentDateTime = LocalDateTime.now();
        currentDateTime = currentDateTime.withHour(9);
        currentDateTime = currentDateTime.withMinute(0);
        currentDateTime = currentDateTime.withSecond(0);
        currentDateTime = currentDateTime.withNano(0);
        LocalDateTime finalCurrTime = currentDateTime;
        HashMap<String,String> depts = new HashMap<>();
        departmentService.getDepartments().forEach(dept -> {
            depts.put(dept.getId().toString(),dept.getName());
        });

        String startDate = currentDate.minusDays(1).format(dateTimeFormat) + " 9:00:00";
        List<DataQuery> results = customService.fetchAllPendingEntries(startDate);

        HashMap<String,List<Map>> groupedResults = new HashMap<>();
        results.stream().forEach(result-> {
            if(result.getData().get("pending_hod") != null) {
                if (groupedResults.containsKey(result.getData().get("pending_hod"))) {
                    List<Map> value = groupedResults.get(result.getData().get("pending_hod"));
                    value.add(result.getData());
                } else {
                    List<Map> newList = new ArrayList<>();
                    newList.add(result.getData());
                    groupedResults.put(result.getData().get("pending_hod"), newList);
                }
            }
        });
        groupedResults.keySet().forEach(x-> {
            String text=
                    "<div\n" +
                            "      style=\"\n" +
                            "        font-size: 16px;\n" +
                            "        font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                            "          'Lucida Sans', Arial, sans-serif;\n" +
                            "      \"\n" +
                            "    >\n" +
                            "      Dear "+x+",<br />\n" +
                            "      Below mentioned user access management requests are pending for action and\n" +
                            "      approval\n" +
                            "    </div>\n" +
                            "    <br />" +
                            "<table style=\"border: 1px solid white; border-collapse: collapse\"\n" +
                            "      width='100%' border='1' align='center'>"
                            + "<tr style=\"\n" +
                            "          border: 1px solid white;\n" +
                            "          border-collapse: collapse;\n" +
                            "          background-color: #666;\n" +
                            "          color: #48c6c5;\n" +
                            "          font-size: 24px;\n" +
                            "          line-height: 48px;\n" +
                            "          font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                            "            'Lucida Sans', Arial, sans-serif;\n" +
                            "        \" align='center'>"
                            + "<td colspan='6'><b>Pending from 24 hrs</b></td>"
                            + "</tr>"
                            + "<tr style=\"\n" +
                            "          border: 1px solid white;\n" +
                            "          border-collapse: collapse;\n" +
                            "          background-color: #000000;\n" +
                            "          color: white;\n" +
                            "          font-size: 18px;\n" +
                            "          line-height: 28px;\n" +
                            "          font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                            "            'Lucida Sans', Arial, sans-serif;\n" +
                            "        \" align='center'>"
                            + "<td><b>Request ID</b></td>"
                            + "<td><b>Initiated By</b></td>"
                            + "<td><b>Initiated On</b></td>"
                            + "<td><b>Assigned Department</b></td>"
                            + "<td><b>Assigned User</b></td>"
                            + "<td><b>System Type</b></td>"
                            + "</tr>";
            text = text + groupedResults.get(x).stream().filter(y-> {
                long diffHrs = Duration.between(LocalDateTime.parse(y.get("create_dt").toString().split("\\.")[0], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),finalCurrTime).toHours();
                if(diffHrs > 24 && diffHrs <= 36)
                    return true;
                else
                    return false;
            }).map(y -> {
                String assignedUser = y.get("assigned_user") == null ? "" : y.get("assigned_user").toString();
                String assignedDept = y.get("assigned_department") == null ? "" : depts.get(y.get("assigned_department").toString());
                return "<tr style=\"\n" +
                        "          border-bottom: 1px solid #6666665a;\n" +
                        "          border-collapse: collapse;\n" +
                        "          font-size: 18px;\n" +
                        "          line-height: 28px;\n" +
                        "          font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                        "            'Lucida Sans', Arial, sans-serif;\n" +
                        "        \" align='center'>"
                        + "<td>" + y.get("entry_id") + "</td>"
                        + "<td>" + y.get("entry_created_by") + "</td>"
                        + "<td>" + y.get("log_create_dt") + "</td>"
                        + "<td>" + assignedDept + "</td>"
                        + "<td>" + assignedUser + "</td>"
                        + "<td>" + y.get("request_type") + "</td>"
                        + "</tr>";
            }).collect(Collectors.joining()) + "</table><br>";

            text = text + "<table style=\"border: 1px solid white; border-collapse: collapse\"\n" +
                    "      width='100%' border='1' align='center'>"
                    + "<tr style=\"\n" +
                    "          border: 1px solid white;\n" +
                    "          border-collapse: collapse;\n" +
                    "          background-color: #666;\n" +
                    "          color: #48c6c5;\n" +
                    "          font-size: 24px;\n" +
                    "          line-height: 48px;\n" +
                    "          font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                    "            'Lucida Sans', Arial, sans-serif;\n" +
                    "        \" align='center'>"
                    + "<td colspan='6'><b>Pending from 36 hrs</b></td>"
                    + "</tr>"
                    + "<tr style=\"\n" +
                    "          border: 1px solid white;\n" +
                    "          border-collapse: collapse;\n" +
                    "          background-color: #000000;\n" +
                    "          color: white;\n" +
                    "          font-size: 18px;\n" +
                    "          line-height: 28px;\n" +
                    "          font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                    "            'Lucida Sans', Arial, sans-serif;\n" +
                    "        \" align='center'>"
                    + "<td><b>Request ID</b></td>"
                    + "<td><b>Initiated By</b></td>"
                    + "<td><b>Initiated On</b></td>"
                    + "<td><b>Assigned Department</b></td>"
                    + "<td><b>Assigned User</b></td>"
                    + "<td><b>System Type</b></td>"
                    + "</tr>";

            text = text + groupedResults.get(x).stream().filter(y-> {
                long diffHrs = Duration.between(LocalDateTime.parse(y.get("create_dt").toString().split("\\.")[0], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),finalCurrTime).toHours();
                if(diffHrs >= 36 && diffHrs <= 72)
                    return true;
                else
                    return false;
            }).map(y -> {
                String assignedUser = y.get("assigned_user") == null ? "" : y.get("assigned_user").toString();
                String assignedDept = y.get("assigned_department") == null ? "" : depts.get(y.get("assigned_department").toString());
                return "<tr style=\"\n" +
                        "          border-bottom: 1px solid #6666665a;\n" +
                        "          border-collapse: collapse;\n" +
                        "          font-size: 18px;\n" +
                        "          line-height: 28px;\n" +
                        "          font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                        "            'Lucida Sans', Arial, sans-serif;\n" +
                        "        \" align='center'>"
                        + "<td>" + y.get("entry_id") + "</td>"
                        + "<td>" + y.get("entry_created_by") + "</td>"
                        + "<td>" + y.get("log_create_dt") + "</td>"
                        + "<td>" + assignedDept + "</td>"
                        + "<td>" + assignedUser + "</td>"
                        + "<td>" + y.get("request_type") + "</td>"
                        + "</tr>";
            }).collect(Collectors.joining()) + "</table><br>";

            text = text + "<table style=\"border: 1px solid white; border-collapse: collapse\"\n" +
                    "      width='100%' border='1' align='center'>"
                    + "<tr style=\"\n" +
                    "          border: 1px solid white;\n" +
                    "          border-collapse: collapse;\n" +
                    "          background-color: #666;\n" +
                    "          color: #48c6c5;\n" +
                    "          font-size: 24px;\n" +
                    "          line-height: 48px;\n" +
                    "          font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                    "            'Lucida Sans', Arial, sans-serif;\n" +
                    "        \" align='center'>"
                    + "<td colspan='6'><b>Pending from 72 hrs</b></td>"
                    + "</tr>"
                    + "<tr style=\"\n" +
                    "          border: 1px solid white;\n" +
                    "          border-collapse: collapse;\n" +
                    "          background-color: #000000;\n" +
                    "          color: white;\n" +
                    "          font-size: 18px;\n" +
                    "          line-height: 28px;\n" +
                    "          font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                    "            'Lucida Sans', Arial, sans-serif;\n" +
                    "        \" align='center'>"
                    + "<td><b>Request ID</b></td>"
                    + "<td><b>Initiated By</b></td>"
                    + "<td><b>Initiated On</b></td>"
                    + "<td><b>Assigned Department</b></td>"
                    + "<td><b>Assigned User</b></td>"
                    + "<td><b>System Type</b></td>"
                    + "</tr>";

            text = text + groupedResults.get(x).stream().filter(y-> {
                long diffHrs = Duration.between(LocalDateTime.parse(y.get("create_dt").toString().split("\\.")[0], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),finalCurrTime).toHours();
                if(diffHrs > 72)
                    return true;
                else
                    return false;
            }).map(y -> {
                String assignedUser = y.get("assigned_user") == null ? "" : y.get("assigned_user").toString();
                String assignedDept = y.get("assigned_department") == null ? "" : depts.get(y.get("assigned_department").toString());
                return "<tr style=\"\n" +
                        "          border-bottom: 1px solid #6666665a;\n" +
                        "          border-collapse: collapse;\n" +
                        "          font-size: 18px;\n" +
                        "          line-height: 28px;\n" +
                        "          font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                        "            'Lucida Sans', Arial, sans-serif;\n" +
                        "        \" align='center'>"
                        + "<td>" + y.get("entry_id") + "</td>"
                        + "<td>" + y.get("entry_created_by") + "</td>"
                        + "<td>" + y.get("log_create_dt") + "</td>"
                        + "<td>" + assignedDept + "</td>"
                        + "<td>" + assignedUser + "</td>"
                        + "<td>" + y.get("request_type") + "</td>"
                        + "</tr>";
            }).collect(Collectors.joining()) + "</table><br><br><br>";

            text = text + "<br />\n" +
                    "    <div\n" +
                    "      style=\"\n" +
                    "        font-size: 16px;\n" +
                    "        font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                    "          'Lucida Sans', Arial, sans-serif;\n" +
                    "      \"\n" +
                    "    >\n" +
                    "      To view details, kindly navigate through\n" +
                    "      <a target=\"_blank\" href=\""+appUrl+"\">Flux-Intelligent</a\n" +
                    "      >!<br />\n" +
                    "      <br />\n" +
                    "      Regards,<br />\n" +
                    "    </div>\n" +
                    "    <div\n" +
                    "      style=\"\n" +
                    "        width: 100%;\n" +
                    "        display: flex;\n" +
                    "        justify-content: flex-start;\n" +
                    "        font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande',\n" +
                    "          'Lucida Sans', Arial, sans-serif;\n" +
                    "        color: #48c6c5;\n" +
                    "        font-size: 20px;\n" +
                    "      \"\n" +
                    "    >\n" +
                    "      Flux-Intelligent | DigitEdgy\n" +
                    "    </div>";

            String status = emailService.sendHtmlMail(new EmailDetails(userService.getUserByUsername(x).get().getEmail(),text,"Escalation Report "+currentDate+" Flux-Intelligent | DigitEdgy",null));
            System.out.println("Email sent to "+userService.getUserByUsername(x).get().getEmail() + " Status : "+status);
        });


    }
}
