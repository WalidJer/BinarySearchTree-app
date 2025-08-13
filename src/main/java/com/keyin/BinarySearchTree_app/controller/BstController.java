package com.keyin.BinarySearchTree_app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.keyin.BinarySearchTree_app.dto.TreeNodeDTO;
import com.keyin.BinarySearchTree_app.service.BstService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BstController {

    private final BstService service;

    public BstController(BstService service) {
        this.service = service;
    }

    @PostMapping("/process-numbers")
    public TreeNodeDTO processNumbersForm(
            @RequestParam("numbers") String numbers,
            @RequestParam(value = "balanced", required = false, defaultValue = "false") boolean balanced
    ) throws JsonProcessingException {
        return service.buildAndSave(numbers, balanced);
    }

    static class NumbersPayload {
        public String numbers;
        public Boolean balanced;
    }

    @PostMapping("/process-numbers-json")
    public TreeNodeDTO processNumbersJson(@RequestBody NumbersPayload body) throws JsonProcessingException {
        boolean balanced = body.balanced != null && body.balanced;
        return service.buildAndSave(body.numbers, balanced);
    }

    @GetMapping("/api/previous")
    public List<BstService.TreeRecordView> previous() {
        return service.listPrevious();
    }

}