package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.models.Movie;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/trending")
public class TrendingStreamController {

    // Thread-safe list to keep track of all active client connections
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTrending() {
        // Create an emitter with a 0 (infinite) timeout to keep the connection alive
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        // Clean up when the client disconnects
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    // Call this method whenever a booking happens
    public void broadcastTrendingUpdate(List<Movie> updatedTrendingList) {
        System.out.println("broadcast called");
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("trending-update")
                        .data(updatedTrendingList));
            } catch (IOException e) {
                // If a connection died unexpectedly, remove it
                emitters.remove(emitter);
            }
        }
    }
}
