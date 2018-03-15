package com.konradbochnia;

import java.io.IOException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/gcall")
@CrossOrigin
public class TimetableController {
    
    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }
    
    @GetMapping("/metadata")
    public String versionAndClasses() throws IOException {
        return timetableService.getClassesAndVersion();
    }
    
    @GetMapping("/substitutions/{date}")
    public String substitutions(@PathVariable String date) throws IOException {
        return timetableService.getSubstitutions(date);
    }
    
    @GetMapping("/lessons/{num}/{id}")
    public String lessons(@PathVariable String num, 
                                @PathVariable String id) 
            throws IOException {
        return timetableService.getLessons(num, id);
    }
}