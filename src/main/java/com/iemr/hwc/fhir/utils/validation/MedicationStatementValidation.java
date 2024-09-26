package com.iemr.hwc.fhir.utils.validation;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.iemr.hwc.fhir.model.medicationStatement.MedicationStatementExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
public class MedicationStatementValidation {

    Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public void medicationStatementValidator(MedicationStatementExt medicationStatementExt) throws UnprocessableEntityException {
        ArrayList<String> errMessages = new ArrayList<>();

        if (medicationStatementExt.getBenFlowID().isEmpty()) {
            logger.error("Error while validating MedicationStatement resource. BenFlowID is a mandatory field and is MISSING");
            errMessages.add("Mandatory extension 'benFlowID' missing");
        }

        if (medicationStatementExt.getCreatedBy().isEmpty()) {
            logger.error("Error while validating MedicationStatement resource. CreatedBy is a mandatory field and is MISSING");
            errMessages.add("Mandatory extension 'createdBy' missing");
        }

        if (medicationStatementExt.getProviderServiceMapId().isEmpty()) {
            logger.error("Error while validating MedicationStatement resource. providerServiceMapId is a mandatory field and is MISSING");
            errMessages.add("Mandatory extension 'providerServiceMapId' missing");
        }

        if (medicationStatementExt.getParkingPlaceID().isEmpty()) {
            logger.error("Error while validating MedicationStatement resource. parkingPlaceId is a mandatory field and is MISSING");
            errMessages.add("Mandatory extension 'parkingPlaceId' missing");
        }

        if (medicationStatementExt.getVanID().isEmpty()) {
            logger.error("Error while validating MedicationStatement resource. vanId is a mandatory field and is MISSING");
            errMessages.add("Mandatory extension 'vanId' missing");
        }

        if (!medicationStatementExt.getSubject().hasDisplay()) {
            logger.error("Error while validating MedicationStatement resource. benRegID is a mandatory field and is MISSING");
            errMessages.add("Mandatory field 'display'(benRegID) in 'subject' missing");
        }

        if(!errMessages.isEmpty()){
            throw new UnprocessableEntityException(errMessages.toArray(new String[0]));
        }
    }
}
