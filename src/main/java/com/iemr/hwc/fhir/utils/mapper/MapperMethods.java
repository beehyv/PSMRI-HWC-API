package com.iemr.hwc.fhir.utils.mapper;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.iemr.hwc.fhir.dto.beneficiary.benIdentities.GovtIdentitiesDTO;
import com.iemr.hwc.fhir.dto.historyDetails.comorbidConditions.ConcurrentConditionsDTO;
import com.iemr.hwc.fhir.dto.historyDetails.familyHistory.FamilyDiseaseListDTO;
import com.iemr.hwc.fhir.dto.historyDetails.pastHistory.PastIllnessDTO;
import com.iemr.hwc.fhir.dto.historyDetails.pastHistory.PastSurgeryDTO;
import com.iemr.hwc.fhir.dto.vitalDetails.VitalDetailsDTO;
import com.iemr.hwc.fhir.model.encounter.EncounterExt;
import com.iemr.hwc.fhir.model.familyMemberHistory.FamilyMemberHistoryExt;
import com.iemr.hwc.fhir.model.observation.ObservationExt;
import com.iemr.hwc.fhir.model.patient.PatientExt;
import com.iemr.hwc.repo.masterrepo.anc.DiseaseTypeRepo;
import com.iemr.hwc.utils.exception.IEMRException;
import com.iemr.hwc.utils.mapper.InputMapper;
import org.hl7.fhir.r4.model.FamilyMemberHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MapperMethods {

    @Autowired
    private DiseaseTypeRepo diseaseTypeRepo;

    public static String getFirstName(PatientExt patientExt) {
            return patientExt.getNameFirstRep().getGiven().get(0).toString();
    }

    public static String getLastName(PatientExt patientExt){
        if (patientExt.hasName() && patientExt.getNameFirstRep().hasFamily()) {
            return patientExt.getNameFirstRep().getFamily();
        }
        else return null;
    }

    public static Integer getGenderID(PatientExt patientExt){
            String gender = patientExt.getGender().toString();
            if (gender.equalsIgnoreCase("male")) {
                return 1;
            } else if (gender.equalsIgnoreCase("female")) {
                return 2;
            } else {
                return 3;
            }
    }

    public static String getMaritalStatusName(PatientExt patientExt){
        if (patientExt.hasMaritalStatus() && patientExt.getMaritalStatus().hasCoding()
                && patientExt.getMaritalStatus().getCodingFirstRep().hasDisplay())
        {
            return patientExt.getMaritalStatus().getCodingFirstRep().getDisplay();
        }
        else return null;
    }


    public static Integer getMaritalStatusID(PatientExt patientExt){
        if (patientExt.hasMaritalStatus() && patientExt.getMaritalStatus().hasCoding()
                && patientExt.getMaritalStatus().getCodingFirstRep().hasCode())
        {
            return Integer.valueOf(patientExt.getMaritalStatus().getCodingFirstRep().getCode());
        }
        else return null;
    }

    public static String getTimeStampFromDate(Date date){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return dateFormat.format(date);
    }

    public static String getTelecomNo(PatientExt patientExt){
        return patientExt.getTelecomFirstRep().getValue();
    }

    public static List<GovtIdentitiesDTO> IdentityMapping(PatientExt patientExt){
        List<GovtIdentitiesDTO> govtIds = new ArrayList<>();
        if (!patientExt.getGovtIdentityType().isEmpty() && !patientExt.getGovtIdentityNo().isEmpty()){
            GovtIdentitiesDTO id1 = new GovtIdentitiesDTO();
            id1.setGovtIdentityTypeID(Integer.parseInt(patientExt.getGovtIdentityType().getCode()));
            id1.setGovtIdentityTypeName(patientExt.getGovtIdentityType().getDisplay());
            id1.setGovtIdentityNo(patientExt.getGovtIdentityNo().asStringValue());
            id1.setCreatedBy(patientExt.getCreatedBy().asStringValue());
            govtIds.add(id1);
        }
        if (!patientExt.getGovtHealthProgramType().isEmpty() && !patientExt.getGovtHealthProgramId().isEmpty()){
            GovtIdentitiesDTO id2 = new GovtIdentitiesDTO();
            id2.setGovtIdentityTypeID(Integer.parseInt(patientExt.getGovtHealthProgramType().getCode()));
            id2.setGovtIdentityTypeName(patientExt.getGovtHealthProgramType().getDisplay());
            id2.setGovtIdentityNo(patientExt.getGovtHealthProgramId().asStringValue());
            id2.setCreatedBy(patientExt.getCreatedBy().asStringValue());
            govtIds.add(id2);
        }
        return govtIds;
    }

    public static String getSubVisitCategoryFromEncounter(EncounterExt encounterExt){
        if(encounterExt.hasServiceType() && encounterExt.getServiceType().hasCoding() && encounterExt.getServiceType().getCodingFirstRep().hasDisplay()){
            return encounterExt.getServiceType().getCodingFirstRep().getDisplay();
        }
        else {
            return null;
        }
    }

    public static VitalDetailsDTO observationVitalsToDTO(ObservationExt observationExt) throws IEMRException {
        JsonObject temp_object = new JsonObject();
        VitalDetailsDTO vitalDetailsDTO = new VitalDetailsDTO();
        if (observationExt.hasComponent()){
            for (int i=0 ; i< observationExt.getComponent().size() ; i++){
                JsonElement ele = new JsonPrimitive(observationExt.getComponent().get(i).getValueQuantity().getValue());
                temp_object.add(observationExt.getComponent().get(i).getCode().getText(),ele);
            }
                vitalDetailsDTO = InputMapper.gson().fromJson(temp_object, VitalDetailsDTO.class);
        }
        return vitalDetailsDTO;
    }

    public static List<PastIllnessDTO> observationPastHistoryToPastIllnessDTO(ObservationExt observationExt) {
        List<PastIllnessDTO> pastIllnessDTOList = new ArrayList<>();
        for (int i=0 ; i < observationExt.getComponent().size() ; i++){
            if (!observationExt.getComponent().get(i).getCode().hasText()){
                throw new UnprocessableEntityException("Unable to extract value of 'text' in 'code' from 'component[" + i + "]'. It might be MISSING");
            }
            if (observationExt.getComponent().get(i).getCode().getText().equalsIgnoreCase("pastIllness")){
                PastIllnessDTO illnessDTO = new PastIllnessDTO();
                illnessDTO.setIllnessTypeID(observationExt.getComponent().get(i).getCode().getCodingFirstRep().getCode());
                illnessDTO.setIllnessType(observationExt.getComponent().get(i).getCode().getCodingFirstRep().getDisplay());
                illnessDTO.setTimePeriodAgo(observationExt.getComponent().get(i).getValueQuantity().hasValue() ?
                        observationExt.getComponent().get(i).getValueQuantity().getValue().toString() : null);
                illnessDTO.setTimePeriodUnit(observationExt.getComponent().get(i).getValueQuantity().getUnit());
                pastIllnessDTOList.add(illnessDTO);
            }
        }
        return pastIllnessDTOList;
    }

    public static List<PastSurgeryDTO> observationPastHistoryToPastSurgeryDTO(ObservationExt observationExt){
        List<PastSurgeryDTO> pastSurgeryDTOList = new ArrayList<>();
        for (int i=0 ; i < observationExt.getComponent().size() ; i++){
            if (observationExt.getComponent().get(i).getCode().hasText() &&
                    observationExt.getComponent().get(i).getCode().getText().equalsIgnoreCase("pastSurgery")){
                PastSurgeryDTO pastSurgeryDTO = new PastSurgeryDTO();
                pastSurgeryDTO.setSurgeryID(observationExt.getComponent().get(i).getCode().getCodingFirstRep().getCode());
                pastSurgeryDTO.setSurgeryType(observationExt.getComponent().get(i).getCode().getCodingFirstRep().getDisplay());
                pastSurgeryDTO.setTimePeriodAgo(observationExt.getComponent().get(i).getValueQuantity().hasValue() ?
                        observationExt.getComponent().get(i).getValueQuantity().getValue().toString() : null);
                pastSurgeryDTO.setTimePeriodUnit(observationExt.getComponent().get(i).getValueQuantity().getUnit());
                pastSurgeryDTOList.add(pastSurgeryDTO);
            }
        }
        return pastSurgeryDTOList;
    }

    public Map<String,Object> fMHistoryResourceToCoMorbidAndFMList(FamilyMemberHistoryExt fMHistoryExt){
        List<ConcurrentConditionsDTO> conditionsDTOList = new ArrayList<>();
        List<FamilyDiseaseListDTO> familyDiseaseList = new ArrayList<>();
        for (FamilyMemberHistory.FamilyMemberHistoryConditionComponent obj : fMHistoryExt.getCondition()) {

            //Mapping the coMorbid Concurrent condition list
            ConcurrentConditionsDTO conditionsDTO = new ConcurrentConditionsDTO();

            List<String> comorbiditySplitList = new ArrayList<>();

            if (obj.getCode().hasCoding()){
                conditionsDTO.setComorbidConditionID(obj.getCode().getCodingFirstRep().getCode());
                conditionsDTO.setComorbidCondition(obj.getCode().getCodingFirstRep().getDisplay());
                conditionsDTO.setIsForHistory(!obj.getCode().getCodingFirstRep().hasUserSelected() || !obj.getCode().getCodingFirstRep().getUserSelected());

                comorbiditySplitList = Arrays.stream(obj.getCode().getCodingFirstRep().getDisplay().split(" ")).collect(Collectors.toList());

            }

            if (obj.hasOnsetAge()) {
                conditionsDTO.setTimePeriodAgo(obj.getOnsetAge().hasValue() ? obj.getOnsetAge().getValue().toString() : null);
                conditionsDTO.setTimePeriodUnit(obj.getOnsetAge().getUnit());
            }
            conditionsDTO.setOtherComorbidCondition(obj.getCode().getText());

            conditionsDTOList.add(conditionsDTO);

            //Mapping the family disease list
            FamilyDiseaseListDTO familyDiseaseListDTO = new FamilyDiseaseListDTO();

            ArrayList<Object[]> diseaseTypeObj = diseaseTypeRepo.getDiseaseTypesFromSearchString(!comorbiditySplitList.isEmpty() ? comorbiditySplitList.get(0) : "Other");

            if (!diseaseTypeObj.isEmpty()) {

                familyDiseaseListDTO.setDiseaseTypeID(String.valueOf(diseaseTypeObj.get(0)[0]));
                familyDiseaseListDTO.setDiseaseType((String) diseaseTypeObj.get(0)[1]);
                familyDiseaseListDTO.setSnomedCode((String) diseaseTypeObj.get(0)[2]);
                familyDiseaseListDTO.setSnomedTerm((String) diseaseTypeObj.get(0)[3]);

                if (((String) diseaseTypeObj.get(0)[1]).equalsIgnoreCase("other")) {
                    familyDiseaseListDTO.setOtherDiseaseType(conditionsDTO.getOtherComorbidCondition());
                }

            } else {
                familyDiseaseListDTO.setDiseaseTypeID(String.valueOf(13));
                familyDiseaseListDTO.setDiseaseType("Other");
                familyDiseaseListDTO.setOtherDiseaseType(conditionsDTO.getComorbidCondition());
            }

            if (obj.hasNote() && obj.getNoteFirstRep().hasText()) {

                String[] memberList = obj.getNoteFirstRep().getText().split(",");
                familyDiseaseListDTO.setFamilyMembers(memberList);
            }

            familyDiseaseList.add(familyDiseaseListDTO);
        }

        Map<String, Object> mappedLists = new HashMap<>();
        mappedLists.put("conditionList",conditionsDTOList);
        mappedLists.put("fMHistoryList",familyDiseaseList);

        return mappedLists;
    }
}
