package com.swiftiq.logever.controller;

import com.swiftiq.logever.builder.entity.Form;
import com.swiftiq.logever.builder.entity.State;
import com.swiftiq.logever.main.entity.UploadHistory;
import com.swiftiq.logever.model.ColumnDef;
import com.swiftiq.logever.service.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/master/entry")
public class MasterFormDataController {

    @Autowired
    FormService formService;

    @Autowired
    UserService userService;

    @Autowired
    FormDataService formDataService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    RoleService roleService;

    @Autowired
    TransitionService transitionService;

    @Autowired
    MasterFormDataService masterFormDataService;

    @Autowired
    UploadHistoryService uploadHistoryService;


    @PutMapping("/{formName}/{col}")
    public ResponseEntity<?> updateMasterEntryStateOnly(@PathVariable(name = "formName") String formName,
                                                        @PathVariable(name = "col") String column,
                                                        @RequestParam(name = "idCol") String idCol,
                                                        @RequestParam(name = "idVal") String idVal,
                                                        @RequestParam(name = "value") String value) {
        Optional<Form> existingForm = formService.getFormByName(formName);

        if (existingForm.isEmpty()) {
            throw new RuntimeException("No form found for " + formName);
        }

        masterFormDataService.updateEntryState(existingForm.get(), idCol, idVal, column, value);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{formName}")
    public ResponseEntity<?> getLogEntries(@PathVariable(name = "formName") String formName,
                                           @RequestParam(name = "column", required = false) String column,
                                           @RequestParam(name = "columnValue", required = false) String columnValue) {
        Optional<Form> existingForm = formService.getFormByName(formName);

        if (existingForm.isEmpty()) {
            throw new RuntimeException("No form found for " + formName);
        }

        var dataQueried = masterFormDataService.getAllFor(existingForm.get(), column, columnValue, "state");

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @GetMapping("/{formName}/new/")
    public ResponseEntity<?> getFilteredLogEntries(@PathVariable(name = "formName") String formName,
                                           @RequestParam(name = "filters") String filters) {
        Optional<Form> existingForm = formService.getFormByName(formName);

        if (existingForm.isEmpty()) {
            throw new RuntimeException("No form found for " + formName);
        }

        var dataQueried = masterFormDataService.getAllFor(existingForm.get(), Arrays.stream(filters.split(";")).collect(Collectors.toMap(cond->cond.split(":")[0],cond->cond.split(":")[1])), "entry_state,metadata");

        return new ResponseEntity<>(dataQueried, HttpStatus.OK);
    }

    @PutMapping("/{formName}")
    public ResponseEntity<?> updateMasterEntryState(@PathVariable(name = "formName") String formName,
                                                    @RequestParam(name = "id") String masterTableEntryId,
                                                    @RequestParam(name="stateColumn") String stateColumn,
                                                    @RequestParam(name = "stateValue") String stateValue,
                                                    @RequestBody Map<String, String> value ) {
        Optional<Form> existingForm = formService.getFormByName(formName);

        if (existingForm.isEmpty()) {
            throw new RuntimeException("No form found for " + formName);
        }

        masterFormDataService.updateEntryState(existingForm.get(), masterTableEntryId, stateColumn, stateValue,value);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path = "/bulk/template/{formId}")
    public ResponseEntity<?> downloadTemplate(@PathVariable Integer formId) throws IOException {
        Date now = new Date();
        String pattern = "yyyyMMddHHmm";
        String dateInString = new SimpleDateFormat(pattern).format(now);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT);
        Optional<Form> form = formService.getFormById(formId);
        if(form.isPresent()) {
            List<String> visibleColumns = form.get().parseFormTemplate().stream().map(c->{
                return c.getColumnLabel();
            }).collect(Collectors.toList());
            visibleColumns.add("State");
//            String visibleColumns = form.get().getWorkflow().getStates().stream().filter(st->st.isFirstState()).findFirst().get().getVisibleColumns();
//            List<String> data = Arrays.stream(visibleColumns.split(",")).map(col->
//                    form.get().getFormKeys().stream()
//                            .filter(key->key.equalsIgnoreCase(col)).findFirst().get()).collect(Collectors.toList());
            csvPrinter.printRecord(visibleColumns);
            csvPrinter.flush();
            InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+form.get().getName().replaceAll(" ","_")+"_bulk_upload_"+dateInString+".csv")
                    .contentLength(out.toByteArray().length).contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(inputStreamResource);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/bulk/upload/{formId}")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @PathVariable Integer formId) {
        Form form = formService.getFormById(formId).get();
        State toState = form.getWorkflow().getTransitions().stream().filter(transition -> transition.getFromState().isFirstState()).findFirst().get().getToState();
        String user = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        try {
            System.out.println(file.getOriginalFilename());
            CSVFormat csvFormat = CSVFormat.DEFAULT;
            Iterable<CSVRecord> records = csvFormat.withHeader().parse(new BufferedReader(new InputStreamReader(file.getInputStream(),"UTF-8"))).getRecords();
            List <ColumnDef> template = form.parseFormTemplate();
            List<String> columns = template.stream().map(c->{
                return c.getColumnLabel();
            }).collect(Collectors.toList());
            columns.add("State");

            ArrayList<Map<String,String>> vRecords = new ArrayList<>();
            int row = 1;
            for(CSVRecord record:records) {
                Map<String,String> value = new HashMap<>();
                for(String column:columns) {
                    if(column.equalsIgnoreCase("state")) {
                        long isStateValid = form.getWorkflow().getStates().stream()
                                .filter(s->s.isEndState() && s.getName().equalsIgnoreCase(record.get("State").trim()))
                                .count();
                        if(isStateValid <= 0)
                            return new ResponseEntity<>("Wrong state name for row number "+row+" in the uploaded file", HttpStatus.EXPECTATION_FAILED);

                        value.put("state",record.get(column).trim().isEmpty()?null:record.get(column).trim());
                    } else {
                        String tableColumn = template.stream().filter(c->c.getColumnLabel().equalsIgnoreCase(column)).findFirst()
                                .get().getColumnName();
                        value.put(tableColumn,record.get(column).trim().isEmpty() ? null : record.get(column).trim());
                    }
                }
                value.put("created_by", user);
                value.put("endState", "true");
                vRecords.add(value);
                row++;
            }

            formDataService.bulkInsert(userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get(),form,vRecords);
            UploadHistory uh = new UploadHistory(file.getOriginalFilename(), form.getId(), user);
            uploadHistoryService.save(uh);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UncategorizedSQLException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getCause().toString(),HttpStatus.EXPECTATION_FAILED);
        }
        catch (SQLException e) {
//            e.printStackTrace();
            System.out.println("SQL Exception");
            return new ResponseEntity<>(e.getCause().toString().split(";")[2],HttpStatus.EXPECTATION_FAILED);
        }
        catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            String message = e.getCause().getMessage().startsWith("Data truncation") ? e.getCause().getMessage()
                    :e.getCause().getMessage().split("for ")[0];
            return new ResponseEntity<>(message,HttpStatus.EXPECTATION_FAILED);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.EXPECTATION_FAILED);
        }
    }


    @GetMapping("/reference/{formName}/{colName}")
    public ResponseEntity<?> getReferenceData(@PathVariable(name="formName") String formName
            , @PathVariable(name="colName") String colName, @RequestParam(name = "where") String where) {
        Form master = formService.getFormByName(formName).get();
        return new ResponseEntity<>(masterFormDataService.getReferenceData(master,colName,where),HttpStatus.OK);
    }
}
