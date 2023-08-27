package com.iemr.hwc.fhir.provider.encounter;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.iemr.hwc.fhir.dto.visitDetailsMain.visitDetails.BenVisitsDTO;
import com.iemr.hwc.fhir.model.encounter.EncounterExt;
import com.iemr.hwc.fhir.service.encounter.EncounterService;
import com.iemr.hwc.service.nurse.NurseServiceImpl;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class EncounterExtProvider implements IResourceProvider {

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private NurseServiceImpl nurseServiceImpl;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return EncounterExt.class;
    }

    @Create()
    public MethodOutcome createEncounter(HttpServletRequest theRequest, @ResourceParam EncounterExt encounterExt) {

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);
        method.setResource(encounterService.createNewEncounter(theRequest,encounterExt));
        return method;
    }

    @Search()
    public List<EncounterExt> findVisitsByVillageAndLastModifDate(@RequiredParam(name = Patient.SP_ADDRESS) StringParam blockID, @RequiredParam(name = "lastModif") DateParam lastModifyDate) {
        List<EncounterExt> listRes = new ArrayList<>();
        List<BenVisitsDTO> listVisits = nurseServiceImpl.getVisitByLocationAndLastModifDate(Integer.parseInt(blockID.getValue()), new Timestamp(lastModifyDate.getValue().getTime()));
        try {
            for (int i = 0; i < listVisits.size(); i++) {
                BenVisitsDTO benDetails = listVisits.get(i);
                EncounterExt encounter = new EncounterExt();
                encounter.setId(1L+"");
                encounter.setProviderServiceMapId(new StringType(benDetails.getBenVisitDetails().getProviderServiceMapID()+""));
                encounter.setVanID(new StringType(benDetails.getBenVisitDetails().getVanID()+""));
                encounter.setParkingPlaceID(new StringType(benDetails.getBenVisitDetails().getParkingPlaceID()+""));
                encounter.setCreatedBy(new StringType(benDetails.getBenVisitDetails().getCreatedBy()));
                encounter.setSessionID(new StringType(benDetails.getSessionID()+""));
                encounter.setBeneficiaryID(new StringType(benDetails.getBeneficiaryID()+""));
                encounter.setBenFlowID(new StringType(benDetails.getBenFlowID()+""));
                encounter.setBeneficiaryRegID(new StringType(benDetails.getBenVisitDetails().getBeneficiaryRegID()+""));

                String status = benDetails.getBenVisitDetails().getVisitFlowStatusFlag();
                if(status !=null && status.trim().equals("N")){
                    encounter.setStatus(Encounter.EncounterStatus.NULL);
                }

                List<CodeableConcept> listConcept = new ArrayList<>();
                CodeableConcept concept2 = new CodeableConcept();
                List<Coding> listCoding2 = new ArrayList<>();
                Coding coding2 = new Coding();
                coding2.setSystem("http://snomed.info/sct");
                coding2.setDisplay(benDetails.getBenVisitDetails().getVisitCategory());
                listCoding2.add(coding2);
                concept2.setCoding(listCoding2);
                listConcept.add(concept2);
                encounter.setType(listConcept);

                CodeableConcept concept = new CodeableConcept();
                List<Coding> listCoding = new ArrayList<>();
                Coding coding = new Coding();
                coding.setSystem("http://snomed.info/sct");
                coding.setDisplay("Management of Common Communicable Diseases and Outpatient Care for Acute Simple Illnesses & Minor Ailments");
                listCoding.add(coding);
                concept.setCoding(listCoding);
                encounter.setServiceType(concept);

                Coding coding3 = new Coding();
                coding3.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode");
                coding3.setCode("AMB");
                coding3.setDisplay("ambulatory");
                encounter.setClass_(coding3);

                List<CodeableConcept> listConcept2 = new ArrayList<>();
                CodeableConcept concept3 = new CodeableConcept();
                concept3.setText(benDetails.getBenVisitDetails().getVisitReason());
                listConcept2.add(concept3);
                encounter.setReasonCode(listConcept2);

                listRes.add(encounter);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return listRes;
    }
}
