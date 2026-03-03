package com.parking.checkin.controller;

import com.parking.checkin.dto.CheckInResponse;
import com.parking.checkin.service.CheckInService;
import com.parking.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    /**
     * Authenticated check-in by spot ID — called from the user's dashboard.
     */
    @PostMapping("/spot/{spotId}")
    public ResponseEntity<CheckInResponse> checkIn(@PathVariable Long spotId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(checkInService.checkIn(spotId, user.getEmail()));
    }

    /**
     * QR code check-in by spot code — the QR code printed on each spot links to
     * this endpoint.
     * Requires user email as a query parameter since QR codes are scanned in
     * unauthenticated context.
     * In production, this should redirect to a login page first.
     */
    @GetMapping("/spot/{spotCode}/qr")
    public ResponseEntity<CheckInResponse> checkInByQr(@PathVariable String spotCode,
            @RequestParam String userEmail) {
        return ResponseEntity.ok(checkInService.checkInBySpotCode(spotCode, userEmail));
    }
}
