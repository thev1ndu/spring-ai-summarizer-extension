package dev.thevindu.readless;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/readless")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ReadlessController {
    private final ReadlessService readlessService;

    @PostMapping("/process")
    public ResponseEntity<String> processContent(@RequestBody ReadlessRequest request) {
        String result = readlessService.processContent(request);
        return ResponseEntity.ok(result);
    }
}
