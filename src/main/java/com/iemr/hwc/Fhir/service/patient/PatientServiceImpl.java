package com.iemr.hwc.Fhir.service.patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import com.iemr.hwc.Fhir.utils.Mappers;
import com.iemr.hwc.service.registrar.RegistrarServiceImpl;
import org.hl7.fhir.r4.model.Patient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientServiceImpl implements PatientService{

    private RegistrarServiceImpl registrarServiceImpl;

    private FhirContext fhirContext;

    private Mappers mappers;

    @Autowired
    public PatientServiceImpl(Mappers mappers, FhirContext fhirContext, RegistrarServiceImpl registrarServiceImpl) {
        this.registrarServiceImpl = registrarServiceImpl;
        this.fhirContext = fhirContext;
        this.mappers = mappers;
    }

    public Patient parseStringToResource(String resourceString){
        IParser parser =fhirContext.newJsonParser();
        parser.setParserErrorHandler(new StrictErrorHandler());
        return parser.parseResource(Patient.class,resourceString);
    }

    public String encodeResourceToString(Patient patient){
        IParser jsonParser = fhirContext.newJsonParser();
        return jsonParser.encodeResourceToString(patient);
    }

    @Override
    public String create(String patientResource, String authorization) throws Exception {
        Patient patient;
        try{
            patient = parseStringToResource(patientResource);
        }
        catch (Exception e){
            return "Payload Parse error : " + e ;
        }

        String mappedPatient = mappers.patientResourceToDbMapper(patientResource);

//        return mappedPatient;
        String response =  registrarServiceImpl.registerBeneficiary(mappedPatient,authorization);
        JSONObject responseJSON = new JSONObject(response);
        String benID = responseJSON.getJSONObject("data").getString("response");
        String[] arrOfResponse = benID.split(":");
        patient.setId(arrOfResponse[arrOfResponse.length - 1].trim());

        try{
            return encodeResourceToString(patient);
        }
        catch (Exception e){
            return "Response Parse error : " + e;
        }
    }
}
