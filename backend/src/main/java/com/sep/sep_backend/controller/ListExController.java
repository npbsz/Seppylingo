package com.sep.sep_backend.controller;

import com.sep.sep_backend.dto.ListExDTO;
import com.sep.sep_backend.service.ListExService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listEx")
@CrossOrigin(origins = "http://localhost:3000")
public class ListExController {
    @Autowired
    private ListExService listExService;
    @GetMapping("/new")
    public ResponseEntity<ListExDTO> getNewListExItem(){
        ListExDTO item= listExService.createNewListEx();
        if(item.getId()==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(item);
        }
        return ResponseEntity.ok(item);
    }
}

