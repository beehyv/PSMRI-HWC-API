package com.iemr.hwc.fhir.utils.validation;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.iemr.hwc.fhir.model.familyMemberHistory.FamilyMemberHistoryExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
public class FMHistoryValidation {

    Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());


    public void CoMorbidAndFMHistoryValidator(FamilyMemberHistoryExt familyMemberHistoryExt) throws UnprocessableEntityException {
        ArrayList<String> errMessages = new ArrayList<>();

        if (familyMemberHistoryExt.getCreatedBy().isEmpty()) {
            logger.error("Error while validating FamilyMemberHistory resource. CreatedBy is a mandatory field and is MISSING");
            errMessages.add("Mandatory extension 'createdBy' missing");
        }

        if (familyMemberHistoryExt.getProviderServiceMapId().isEmpty()) {
            logger.error("Error while validating FamilyMemberHistory resource. providerServiceMapId is a mandatory field and is MISSING");
            errMessages.add("Mandatory extension 'providerServiceMapId' missing");
        }

        if (familyMemberHistoryExt.getParkingPlaceID().isEmpty()) {
            logger.error("Error while validating FamilyMemberHistory resource. parkingPlaceId is a mandatory field and is MISSING");
            errMessages.add("Mandatory extension 'parkingPlaceId' missing");
        }

        if (familyMemberHistoryExt.getVanID().isEmpty()) {
            logger.error("Error while validating FamilyMemberHistory resource. vanId is a mandatory field and is MISSING");
            errMessages.add("Mandatory extension 'vanId' missing");
        }

        if (familyMemberHistoryExt.getBenFlowID().isEmpty()) {
            logger.error("Error while validating FamilyMemberHistory resource. benFlowID is a mandatory field and is MISSING");
            errMessages.add("Mandatory extension 'benFlowID' missing");
        }

        if (!familyMemberHistoryExt.getPatient().hasDisplay()) {
            logger.error("Error while validating FamilyMemberHistory resource. benRegID is a mandatory field and is MISSING");
            errMessages.add("Mandatory field 'display'(benRegID) in 'patient' missing");
        }

        if(!errMessages.isEmpty()){
            throw new UnprocessableEntityException(errMessages.toArray(new String[0]));
        }
    }
}
