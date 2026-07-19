package com.jobbed.ai;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiStatusController {
    private final AiClient client;
    public AiStatusController(AiClient client) { this.client = client; }

    @GetMapping("/status")
    public AiStatus status() { return new AiStatus(client.available(), client.provider(), client.model()); }

    public record AiStatus(boolean available, String provider, String model) {}
}
