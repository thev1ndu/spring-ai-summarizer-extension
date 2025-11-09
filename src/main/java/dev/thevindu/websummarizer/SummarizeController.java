package dev.thevindu.websummarizer;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summarize")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class SummarizeController {
    private final SummarizeService summarizeService;

    @PostMapping("/process")
    public ResponseEntity<String> processContent(@RequestBody SummarizeRequest request) {
        String result = summarizeService.processContent(request);
        return ResponseEntity.ok(result);
    }
}
