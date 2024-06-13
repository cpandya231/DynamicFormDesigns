package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.entity.State;
import ai.smartfac.logever.service.*;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
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


    @GetMapping("/{formName}")
    public ResponseEntity<?> getLogEntries(@PathVariable(name = "formName") String formName,
                                           @RequestParam(name = "column", required = false) String column,
                                           @RequestParam(name = "columnValue", required = false) String columnValue) {
        Optional<Form> existingForm = formService.getFormByName(formName);

        if (existingForm.isEmpty()) {
            throw new RuntimeException("No form found for " + formName);
        }

        var dataQueried = masterFormDataService.getAllFor(existingForm.get(), column, columnValue, "entry_state,metadata");

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
    public ResponseEntity<?> downloadReport(@PathVariable Integer formId) throws IOException {
        Date now = new Date();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT);
        Optional<Form> form = formService.getFormById(formId);
        if(form.isPresent()) {
            String visibleColumns = form.get().getWorkflow().getStates().stream().filter(st->st.isFirstState()).findFirst().get().getVisibleColumns();
            List<String> data = Arrays.stream(visibleColumns.split(",")).map(col->
                    form.get().getFormLabels().stream()
                            .filter(lbl->lbl.toLowerCase().replaceAll(" ","_").equalsIgnoreCase(col)).findFirst().get()).collect(Collectors.toList());
            csvPrinter.printRecord(data);
            csvPrinter.flush();
            InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+form.get().getName().replaceAll(" ","_")+"_bulk_upload.csv")
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

        try {
            System.out.println(file.getOriginalFilename());
            CSVFormat csvFormat = CSVFormat.DEFAULT;
            Iterable<CSVRecord> records = csvFormat.withHeader().parse(new BufferedReader(new InputStreamReader(file.getInputStream(),"UTF-8"))).getRecords();
            String visibleColumns = form.getWorkflow().getStates().stream().filter(st->st.isFirstState()).findFirst().get().getVisibleColumns();
            List<String> columns = Arrays.stream(visibleColumns.split(",")).map(col->
                    form.getFormLabels().stream()
                            .filter(lbl->lbl.toLowerCase().replaceAll(" ","_").equalsIgnoreCase(col)).findFirst().get()).collect(Collectors.toList());
            ArrayList<Map<String,String>> vRecords = new ArrayList<>();

            for(CSVRecord record:records) {
                Map<String,String> value = new HashMap<>();
                for(String column:columns) {
                    value.put(column.toLowerCase().replaceAll(" ","_"),record.get(column));
                }
                value.put("state", toState.getName());
                value.put("created_by", SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
                value.put("endState", toState.isEndState() ? "true" : "false");
                vRecords.add(value);
            }
//            for(Map<String,String> val:vRecords) {
//                val.entrySet().forEach(e->System.out.println(e.getKey()+" -- "+e.getValue()));
//            }
            formDataService.bulkInsert(userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()).get(),form,vRecords);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
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
