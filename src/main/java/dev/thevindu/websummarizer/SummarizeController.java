package dev.thevindu.websummarizer;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summarize")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class SummarizeController {
    private final SummarizeService summarizeService;

}
