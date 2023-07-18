package com.iemr.hwc.Fhir.service.patient;

public interface PatientService {

    String create(String patientData, String authorization) throws Exception;
}
