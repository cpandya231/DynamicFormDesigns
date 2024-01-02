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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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

        Optional<Path> folder = fileStorageService.loadAll(formId+"").filter(path-> {
            return path.toString().startsWith(formId + "/" + logEntryId + "_");
        }).findFirst();

        if(folder.isPresent()) {
            Optional<Path> file = fileStorageService.loadAll(folder.get().toString()).filter(path-> {
                return path.toString().endsWith("/"+fileName);
            }).findFirst();
            if(file.isPresent()) {
                System.out.println(file.get().toFile().length());
                ByteArrayResource resource = new ByteArrayResource(fileStorageService.load(file.get().toString()).getInputStream().readAllBytes());
                HttpHeaders headers = new HttpHeaders();
                headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                headers.add("Pragma", "no-cache");
                headers.add("Expires", "0");

                return ResponseEntity.ok()
                        .headers(headers)
//                        .contentLength(file.get().toFile().length())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
