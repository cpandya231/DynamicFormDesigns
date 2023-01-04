package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.service.EquipmentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/equipment/data")
public class EquipmentDataController {

    @Autowired
    EquipmentDataService equipmentDataService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getFormByName(@PathVariable(name = "id") int equipmentId) {
        return new ResponseEntity<>(equipmentDataService.fetchDataFor(equipmentId), HttpStatus.OK);
    }
}
