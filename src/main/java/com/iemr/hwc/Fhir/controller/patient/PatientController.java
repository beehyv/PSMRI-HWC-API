package com.iemr.hwc.Fhir.controller.patient;

import com.iemr.hwc.Fhir.service.patient.PatientService;
import com.iemr.hwc.utils.mapper.InputMapper;
import com.iemr.hwc.utils.response.OutputResponse;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/Patient", headers = "Authorization")
/**
 * Objective: Performs QuickSearch, AdvancedSearch and fetching Beneficiary
 * Details
 */
public class PatientController {
    private InputMapper inputMapper = new InputMapper();
    private PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }


    // beneficiary registration(fhir Patient resource) with common and identity new for mobile app
    @CrossOrigin()
    @ApiOperation(value = "Register a new FHIR Patient resource", consumes = "application/json", produces = "application/json")
    @RequestMapping(value = { "" }, method = { RequestMethod.POST })
    public String createPatient(@RequestBody String patientResource,
                                @RequestHeader(value = "Authorization") String authorization) {
        String s;
        OutputResponse response = new OutputResponse();
        try {
            s = patientService.create(patientResource, authorization);
            return s;
        } catch (Exception e) {
            log.error("Error in registration" + e);
            response.setError(5000, "Error in registration; please contact administrator");
            return response.toString();
        }

    }
}