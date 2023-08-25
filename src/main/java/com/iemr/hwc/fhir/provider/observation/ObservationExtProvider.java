package com.iemr.hwc.fhir.provider.observation;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.iemr.hwc.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.hwc.fhir.dto.visitDetailsMain.visitDetails.BenVisitsDTO;
import com.iemr.hwc.fhir.dto.vitalDetails.VitalDetailsDTO;
import com.iemr.hwc.fhir.model.encounter.EncounterExt;
import com.iemr.hwc.fhir.model.observation.ObservationExt;
import com.iemr.hwc.fhir.service.observation.ObservationService;
import com.iemr.hwc.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class ObservationExtProvider implements IResourceProvider {

    @Autowired
    private ObservationService observationService;

    @Autowired
    private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ObservationExt.class;
    }

    @Create()
    public MethodOutcome createObservation(HttpServletRequest theRequest, @ResourceParam ObservationExt observationExt) throws Exception{

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);
        method.setResource(observationService.createObservation(theRequest,observationExt));
        return method;
    }

    @Search()
    public List<ObservationExt> findVisitsByVillageAndLastModifDate(@RequiredParam(name = "providerServiceMapId") StringParam providerServiceMapId, @RequiredParam(name = "vanID") StringParam vanID,
                                                                    @RequiredParam(name = "lastModif") DateParam lastModifyDate) {
        List<ObservationExt> listRes = new ArrayList<>();
        List<VitalDetailsDTO> ListVitalDetailsDTO = observationService.getVitalsObservationByLocationAndLastModifDate(Integer.parseInt(providerServiceMapId.getValue()), Integer.parseInt(vanID.getValue()),
                new Timestamp(lastModifyDate.getValue().getTime()));
        try {
            for (int i = 0; i < ListVitalDetailsDTO.size(); i++) {
                VitalDetailsDTO benDetails = ListVitalDetailsDTO.get(i);
                BeneficiaryFlowStatus beneficiaryFlowStatus = beneficiaryFlowStatusRepo.getBenFlowByVisitIDAndVisitCode(Long.parseLong(benDetails.getBenVisitID()), Long.parseLong(benDetails.getVisitCode()));
                ObservationExt vitals = new ObservationExt();
                vitals.setId(1L+"");
                vitals.setModifiedBy(new StringType(benDetails.getModifiedBy()));
                vitals.setBeneficiaryRegID(new StringType(benDetails.getBeneficiaryRegID()+""));
                if (beneficiaryFlowStatus != null){
                    vitals.setBeneficiaryID(new StringType(beneficiaryFlowStatus.getBeneficiaryID()+""));
                    vitals.setBenFlowID(new StringType(beneficiaryFlowStatus.getBenFlowID()+""));
                }
                else {
                    throw new ResourceNotFoundException("No record found for given benVisitID and BenVisitCode");
                }

                List<CodeableConcept> listConcept1 = new ArrayList<>();
                CodeableConcept concept1 = new CodeableConcept();
                List<Coding> listCoding1 = new ArrayList<>();
                Coding coding1 = new Coding();
                coding1.setSystem("http://terminology.hl7.org/CodeSystem/observation-category");
                coding1.setCode("vital-signs");
                coding1.setDisplay("Vital Signs");
                listCoding1.add(coding1);
                concept1.setCoding(listCoding1);
                concept1.setText("Vital Signs");
                listConcept1.add(concept1);
                vitals.setCategory(listConcept1);

                CodeableConcept concept2 = new CodeableConcept();
                List<Coding> listCoding2 = new ArrayList<>();
                Coding coding2 = new Coding();
                coding2.setSystem("http://loinc.org/");
                coding2.setCode("85353-1");
                coding2.setDisplay("Vital signs panel");
                listCoding2.add(coding2);
                concept2.setCoding(listCoding2);
                concept2.setText("Vital signs Panel");
                vitals.setCode(concept2);

                List<Observation.ObservationComponentComponent> listComponent = new ArrayList<>();
                Observation.ObservationComponentComponent observation1 = new Observation.ObservationComponentComponent();
                CodeableConcept concept3 = new CodeableConcept();
                concept3.setText("weight_Kg");
                observation1.setCode(concept3);
                Type type = new Quantity(Long.parseLong(benDetails.getWeight_Kg()));
                observation1.setValue(type);
                listComponent.add(observation1);
                vitals.setComponent(listComponent);

                listRes.add(vitals);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return listRes;
    }
}
