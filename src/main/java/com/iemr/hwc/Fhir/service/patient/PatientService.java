package com.iemr.hwc.Fhir.service.patient;

import org.springframework.http.ResponseEntity;

public interface PatientService {

    ResponseEntity<String> create(String patientData, String authorization);
}
