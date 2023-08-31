package com.iemr.hwc.fhir.provider.observation;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.iemr.hwc.fhir.dto.historyDetails.pastHistory.PastHistoryDTO;
import com.iemr.hwc.fhir.dto.historyDetails.pastHistory.PastIllnessDTO;
import com.iemr.hwc.fhir.dto.historyDetails.pastHistory.PastSurgeryDTO;
import com.iemr.hwc.fhir.model.observation.ObservationExt;
import com.iemr.hwc.fhir.service.observation.ObservationService;
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
    public List<ObservationExt> findHistoryByLocationAndLastModifDate(@RequiredParam(name = "providerServiceMapId") StringParam providerServiceMapId, @RequiredParam(name = "vanID") StringParam vanID,
                                                                      @RequiredParam(name = "lastModif") DateParam lastModifyDate) {
        List<ObservationExt> listRes = new ArrayList<>();
        List<PastHistoryDTO> ListHistoryDTO = observationService.getBenMedHistoryByLocationAndLastModifDate(Integer.parseInt(providerServiceMapId.getValue()), Integer.parseInt(vanID.getValue()),
                new Timestamp(lastModifyDate.getValue().getTime()));
        try {
            for (int i = 0; i < ListHistoryDTO.size(); i++) {
                PastHistoryDTO historyDTO = ListHistoryDTO.get(i);
                ObservationExt historyObservation = new ObservationExt();
                historyObservation.setId(historyDTO.getId()+"");
                historyObservation.setVanID(new StringType(historyDTO.getVanID()+""));
                historyObservation.setParkingPlaceID(new StringType(historyDTO.getParkingPlaceID()+""));
                historyObservation.setCreatedBy(new StringType(historyDTO.getCreatedBy()));
                historyObservation.setProviderServiceMapId(new StringType(historyDTO.getProviderServiceMapID()+""));
                historyObservation.setBeneficiaryRegID(new StringType(historyDTO.getBeneficiaryRegID()+""));
                historyObservation.setBeneficiaryID(new StringType(historyDTO.getBeneficiaryID()+""));
                historyObservation.setBenFlowID(new StringType(historyDTO.getBenFlowID()+""));

                List<CodeableConcept> codeableConceptList1 = new ArrayList<>();
                CodeableConcept codeableConcept1 = new CodeableConcept();
                codeableConcept1.setText("History");
                List<Coding> codingList1 = new ArrayList<>();
                Coding coding1 = new Coding();
                coding1.setSystem("http://terminology.hl7.org/CodeSystem/observation-category");
                coding1.setCode("social-history");
                coding1.setDisplay("Social History");
                codingList1.add(coding1);
                codeableConcept1.setCoding(codingList1);
                codeableConceptList1.add(codeableConcept1);
                historyObservation.setCategory(codeableConceptList1);

                CodeableConcept codeableConcept2 = new CodeableConcept();
                codeableConcept2.setText("Past medical history");
                List<Coding> codingList2 = new ArrayList<>();
                Coding coding2 = new Coding();
                coding2.setSystem("http://loinc.org/");
                coding2.setCode("11348-0");
                coding2.setDisplay("Past medical history");
                codingList2.add(coding2);
                codeableConcept2.setCoding(codingList2);
                historyObservation.setCode(codeableConcept2);


                List<Observation.ObservationComponentComponent> listComponent = new ArrayList<>();
                List<PastIllnessDTO> pastIllnessDTOList = historyDTO.getPastIllness();
                List<PastSurgeryDTO> pastSurgeryDTOList = historyDTO.getPastSurgery();
                if(pastIllnessDTOList!=null && !pastIllnessDTOList.isEmpty()){
                    for (int j = 0; j < pastIllnessDTOList.size(); j++) {
                        Observation.ObservationComponentComponent observation1 = new Observation.ObservationComponentComponent();
                        CodeableConcept codeableConcept3 = new CodeableConcept();
                        List<Coding> codingList3 = new ArrayList<>();
                        Coding coding3 = new Coding();
                        coding3.setCode(pastIllnessDTOList.get(j).getIllnessTypeID());
                        coding3.setDisplay(pastIllnessDTOList.get(j).getIllnessType());
                        codingList3.add(coding3);
                        codeableConcept3.setCoding(codingList3);
                        codeableConcept3.setText("pastIllness");
                        observation1.setCode(codeableConcept3);
                        Type type = new Quantity();
                        if(pastIllnessDTOList.get(j).getTimePeriodAgo() !=null && !pastIllnessDTOList.get(j).getTimePeriodAgo().equals("null")){
                            type = new Quantity(Quantity.QuantityComparator.NULL, 0L, "", pastIllnessDTOList.get(j).getTimePeriodAgo(), pastIllnessDTOList.get(j).getTimePeriodUnit());
                        }
                        observation1.setValue(type);
                        listComponent.add(observation1);
                    }
                }

                if(pastSurgeryDTOList!=null && !pastSurgeryDTOList.isEmpty()){
                    for (int j = 0; j < pastSurgeryDTOList.size(); j++) {
                        Observation.ObservationComponentComponent observation2 = new Observation.ObservationComponentComponent();
                        CodeableConcept codeableConcept3 = new CodeableConcept();
                        List<Coding> codingList3 = new ArrayList<>();
                        Coding coding3 = new Coding();
                        coding3.setCode(pastSurgeryDTOList.get(j).getSurgeryID());
                        coding3.setDisplay(pastSurgeryDTOList.get(j).getSurgeryType());
                        codingList3.add(coding3);
                        codeableConcept3.setCoding(codingList3);
                        codeableConcept3.setText("pastSurgery");
                        observation2.setCode(codeableConcept3);
                        Type type = new Quantity();
                        if(pastSurgeryDTOList.get(j).getTimePeriodAgo() !=null && !pastSurgeryDTOList.get(j).getTimePeriodAgo().equals("null")){
                            type = new Quantity(Quantity.QuantityComparator.NULL, 0L, "", pastSurgeryDTOList.get(j).getTimePeriodAgo(), pastSurgeryDTOList.get(j).getTimePeriodUnit());
                        }
                        observation2.setValue(type);
                        listComponent.add(observation2);
                    }
                }

                historyObservation.setComponent(listComponent);

                listRes.add(historyObservation);
            }
        }
        catch (Exception e){

        }

        return listRes;
    }
}
