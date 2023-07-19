package com.iemr.hwc.Fhir.service.patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import com.iemr.hwc.Fhir.utils.Mappers;
import com.iemr.hwc.service.registrar.RegistrarServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Patient;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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
    public ResponseEntity<String> create(String patientResource, String authorization){
        Patient patient;
        try{
            patient = parseStringToResource(patientResource);

            String mappedPatient = mappers.patientResourceToDbMapper(patientResource);

            String response = registrarServiceImpl.registerBeneficiary(mappedPatient, authorization);
            JSONObject responseJSON = new JSONObject(response);
            String benID = responseJSON.getJSONObject("data").getString("response");
            String[] arrOfResponse = benID.split(":");
            patient.setId(arrOfResponse[arrOfResponse.length - 1].trim());


            return new ResponseEntity<>(encodeResourceToString(patient), HttpStatus.CREATED);
        }
        catch (DataFormatException e){
            log.error("Error while parsing or encoding patient resource" + e);
            return new ResponseEntity<>("Parse Error : "+ e , HttpStatus.BAD_REQUEST);
        }
        catch (JSONException e){
            log.error("Error while mapping or retrieving JSON objects" + e);
            return new ResponseEntity<>("JSON exception : " + e, HttpStatus.BAD_REQUEST);
        }
        catch (Exception e){
            log.error("Encountered exception while registering new beneficiary" + e);
            return new ResponseEntity<>("Encountered exception while registering new beneficiary : "+ e,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
