package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.request.CreateConservationRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.entities.Conservation;
import vn.edu.iuh.fit.services.ConservationService;

@RestController
@RequestMapping("/api/v1/conservations")
@RequiredArgsConstructor
public class ConservationController {

    private final ConservationService conservationService;

    @PostMapping("/private")
    public ResponseEntity<ApiResponse<?>> createPrivateConservation(@RequestBody CreateConservationRequest request) {
        try {
            Conservation conservation = conservationService.createPrivateConservation(
                    request.getSenderId(), request.getReceiverId()
            );
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Create/Get 1-1 conservation successfully")
                    .response(conservation)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }
}