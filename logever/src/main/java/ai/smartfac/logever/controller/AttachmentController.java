package ai.smartfac.logever.controller;

import ai.smartfac.logever.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/attachment")
@CrossOrigin("*")
public class AttachmentController {

    @Autowired
    FileStorageService fileStorageService;

    @GetMapping("/{form_id}/{log_entry_id}/{file_name}")
    public ResponseEntity<?> downloadLatestAttachment(@PathVariable(name = "form_id") int formId,
                                                      @PathVariable(name = "log_entry_id") int logEntryId,
                                                      @PathVariable(name = "file_name") String fileName) throws IOException {

        List<Path> folders = fileStorageService.loadAll(formId+"")
                .sorted(Comparator.comparing(p -> p.toString(), Comparator.reverseOrder()))
                .filter(path-> {
            return path.toString().startsWith(formId + "/" + logEntryId + "_");
        }).collect(Collectors.toList());

        for(Path folder: folders) {
            Optional<Path> file = fileStorageService.loadAll(folder.toString()).filter(path-> {
                return path.toString().endsWith("/"+fileName);
            }).findFirst();
            try {
                if(file.isPresent()) {
                    ByteArrayResource resource = new ByteArrayResource(fileStorageService.load(file.get().toString()).getInputStream().readAllBytes());
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                    headers.add("Pragma", "no-cache");
                    headers.add("Expires", "0");

                    return ResponseEntity.ok()
                            .headers(headers)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(resource);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{form_id}/{log_entry_id}/{log_entry_history_id}/{file_name}")
    public ResponseEntity<?> downloadAttachment(@PathVariable(name = "form_id") int formId,
                                                @PathVariable(name = "log_entry_id") int logEntryId,
                                                @PathVariable(name = "log_entry_history_id") int logEntryHistoryId,
                                                @PathVariable(name = "file_name") String fileName) throws IOException {

        List<Path> folders = fileStorageService.loadAll(formId+"")
                .sorted(Comparator.comparing(p -> p.toString(), Comparator.reverseOrder()))
                .filter(path-> {

            return Integer.parseInt(path.toString().split("_")[1]) <= logEntryHistoryId;
        }).collect(Collectors.toList());

        for(Path folder: folders) {
            Optional<Path> file = fileStorageService.loadAll(folder.toString()).filter(path-> {
                return path.toString().endsWith("/"+fileName);
            }).findFirst();
            try {
                if(file.isPresent()) {
                    ByteArrayResource resource = new ByteArrayResource(fileStorageService.load(file.get().toString()).getInputStream().readAllBytes());
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                    headers.add("Pragma", "no-cache");
                    headers.add("Expires", "0");

                    return ResponseEntity.ok()
                            .headers(headers)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(resource);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
