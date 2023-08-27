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
    public List<ObservationExt> findVisitsByLocationAndLastModifDate(@RequiredParam(name = "providerServiceMapId") StringParam providerServiceMapId, @RequiredParam(name = "vanID") StringParam vanID,
                                                                    @RequiredParam(name = "lastModif") DateParam lastModifyDate) {
        List<ObservationExt> listRes = new ArrayList<>();
        List<VitalDetailsDTO> ListVitalDetailsDTO = observationService.getVitalsObservationByLocationAndLastModifDate(Integer.parseInt(providerServiceMapId.getValue()), Integer.parseInt(vanID.getValue()),
                new Timestamp(lastModifyDate.getValue().getTime()));
//        System.out.println("List received "+ListVitalDetailsDTO.size());
        try {
            for (int i = 0; i < ListVitalDetailsDTO.size(); i++) {
                VitalDetailsDTO benDetails = ListVitalDetailsDTO.get(i);
                ObservationExt vitals = new ObservationExt();
                vitals.setId(1L+"");
                vitals.setModifiedBy(new StringType(benDetails.getModifiedBy()));
                vitals.setBeneficiaryRegID(new StringType(benDetails.getBeneficiaryRegID()+""));
                vitals.setBeneficiaryID(new StringType(benDetails.getBeneficiaryID()+""));
                vitals.setBenFlowID(new StringType(benDetails.getBenFlowID()+""));

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
                Type type = new Quantity();
                if(benDetails.getWeight_Kg()!=null && !benDetails.getWeight_Kg().equals("null")){
                    type = new Quantity(Double.parseDouble(benDetails.getWeight_Kg()));
                }
                observation1.setValue(type);
                listComponent.add(observation1);

                Observation.ObservationComponentComponent observation2 = new Observation.ObservationComponentComponent();
                CodeableConcept concept4 = new CodeableConcept();
                concept4.setText("height_cm");
                observation2.setCode(concept4);
                Type type2 = new Quantity();
                if(benDetails.getHeight_cm()!=null && !benDetails.getHeight_cm().equals("null")){
                    type2 = new Quantity(Double.parseDouble(benDetails.getHeight_cm()));
                }
                observation2.setValue(type2);
                listComponent.add(observation2);

                Observation.ObservationComponentComponent observation4 = new Observation.ObservationComponentComponent();
                CodeableConcept concept6 = new CodeableConcept();
                concept6.setText("waistCircumference_cm");
                observation4.setCode(concept6);
                Type type4 = new Quantity();
                if(benDetails.getWaistCircumference_cm()!=null && !benDetails.getWaistCircumference_cm().equals("null")){
                    type4 = new Quantity(Double.parseDouble(benDetails.getWaistCircumference_cm()));
                }
                observation4.setValue(type4);
                listComponent.add(observation4);

                Observation.ObservationComponentComponent observation5 = new Observation.ObservationComponentComponent();
                CodeableConcept concept7 = new CodeableConcept();
                concept7.setText("bMI");
                observation5.setCode(concept7);
                Type type5 = new Quantity();
                if(benDetails.getBMI()!=null && !benDetails.getBMI().equals("null")){
                    type5 = new Quantity(Double.parseDouble(benDetails.getBMI()));
                }
                observation5.setValue(type5);
                listComponent.add(observation5);

                Observation.ObservationComponentComponent observation6 = new Observation.ObservationComponentComponent();
                CodeableConcept concept8 = new CodeableConcept();
                concept8.setText("temperature");
                observation6.setCode(concept8);
                Type type6 = new Quantity();
                if(benDetails.getTemperature()!=null && !benDetails.getTemperature().equals("null")){
                    type6 = new Quantity(Double.parseDouble(benDetails.getTemperature()));
                }
                observation6.setValue(type6);
                listComponent.add(observation6);

                Observation.ObservationComponentComponent observation7 = new Observation.ObservationComponentComponent();
                CodeableConcept concept9 = new CodeableConcept();
                concept9.setText("pulseRate");
                observation7.setCode(concept9);
                Type type7 = new Quantity();
                if(benDetails.getPulseRate()!=null && !benDetails.getPulseRate().equals("null")){
                    type7 = new Quantity(Double.parseDouble(benDetails.getPulseRate()));
                }
                observation7.setValue(type7);
                listComponent.add(observation7);

                Observation.ObservationComponentComponent observation8 = new Observation.ObservationComponentComponent();
                CodeableConcept concept10 = new CodeableConcept();
                concept10.setText("sPO2");
                observation8.setCode(concept10);
                Type type8 = new Quantity();
                if(benDetails.getSPO2()!=null && !benDetails.getSPO2().equals("null")){
                    type8 = new Quantity(Double.parseDouble(benDetails.getSPO2()));
                }
                observation8.setValue(type8);
                listComponent.add(observation8);

                Observation.ObservationComponentComponent observation9 = new Observation.ObservationComponentComponent();
                CodeableConcept concept11 = new CodeableConcept();
                concept11.setText("systolicBP_1stReading");
                observation9.setCode(concept11);
                Type type9 = new Quantity();
                if(benDetails.getSystolicBP_1stReading()!=null && !benDetails.getSystolicBP_1stReading().equals("null")){
                    type9 = new Quantity(Double.parseDouble(benDetails.getSystolicBP_1stReading()));
                }
                observation9.setValue(type9);
                listComponent.add(observation9);

                Observation.ObservationComponentComponent observation10 = new Observation.ObservationComponentComponent();
                CodeableConcept concept12 = new CodeableConcept();
                concept12.setText("diastolicBP_1stReading");
                observation10.setCode(concept12);
                Type type10 = new Quantity();
                if(benDetails.getDiastolicBP_1stReading()!=null && !benDetails.getDiastolicBP_1stReading().equals("null")){
                    type10 = new Quantity(Double.parseDouble(benDetails.getDiastolicBP_1stReading()));
                }
                observation10.setValue(type10);
                listComponent.add(observation10);

                Observation.ObservationComponentComponent observation11 = new Observation.ObservationComponentComponent();
                CodeableConcept concept13 = new CodeableConcept();
                concept13.setText("respiratoryRate");
                observation11.setCode(concept13);
                Type type11 = new Quantity();
                if(benDetails.getRespiratoryRate()!=null && !benDetails.getRespiratoryRate().equals("null")){
                    type11 = new Quantity(Double.parseDouble(benDetails.getRespiratoryRate()));
                }
                observation11.setValue(type11);
                listComponent.add(observation11);

                Observation.ObservationComponentComponent observation12 = new Observation.ObservationComponentComponent();
                CodeableConcept concept14 = new CodeableConcept();
                concept14.setText("rbsTestResult");
                observation12.setCode(concept14);
                Type type12 = new Quantity();
                if(benDetails.getRbsTestResult()!=null && !benDetails.getRbsTestResult().equals("null")){
                    type12 = new Quantity(Double.parseDouble(benDetails.getRbsTestResult()));
                }
                observation12.setValue(type12);
                listComponent.add(observation12);

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
