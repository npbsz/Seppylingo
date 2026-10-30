package com.sep.sep_backend.controller;

import com.sep.sep_backend.dto.LtItemDTO;
import com.sep.sep_backend.service.LtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lueckentext")
@CrossOrigin(origins = "http://localhost:3000")
public class LtController {
    @Autowired
    private LtService ltService;

    /**
     * Api endpoint for creating a new game session
     * @return LtItemDTO {UUID id, String textEN, String textDE}
     */
    @GetMapping("/new")
    public ResponseEntity<LtItemDTO> getNewLtItem(){
        LtItemDTO item= ltService.createNewLueckentext();
        if(item.getGameId()==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(item);
        }
        return ResponseEntity.ok(item);
    }
}

