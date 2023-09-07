package com.iemr.hwc.service.CHOApp;

import org.springframework.http.ResponseEntity;

public interface CHOAppSyncService {
    ResponseEntity<String> registerCHOAPPBeneficiary(String comingReq, String authorization);
}
