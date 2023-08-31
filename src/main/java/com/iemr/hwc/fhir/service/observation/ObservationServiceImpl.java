package com.iemr.hwc.fhir.service.observation;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.hwc.data.anc.BenMedHistory;
import com.iemr.hwc.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.hwc.fhir.dto.historyDetails.pastHistory.PastHistoryDTO;
import com.iemr.hwc.fhir.dto.historyDetails.pastHistory.PastIllnessDTO;
import com.iemr.hwc.fhir.dto.historyDetails.pastHistory.PastSurgeryDTO;
import com.iemr.hwc.fhir.dto.mandatoryFieldsDTO.MandatoryFieldsDTO;
import com.iemr.hwc.fhir.dto.vitalDetails.VitalDetailsDTO;
import com.iemr.hwc.fhir.model.observation.ObservationExt;
import com.iemr.hwc.fhir.utils.mapper.MapperMethods;
import com.iemr.hwc.fhir.utils.mapper.MapperUtils;
import com.iemr.hwc.fhir.utils.validation.ObservationValidation;
import com.iemr.hwc.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.hwc.repo.nurse.anc.BenMedHistoryRepo;
import com.iemr.hwc.service.anc.Utility;
import com.iemr.hwc.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.hwc.service.generalOPD.GeneralOPDServiceImpl;
import com.iemr.hwc.utils.exception.IEMRException;
import com.iemr.hwc.utils.mapper.InputMapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ObservationServiceImpl implements ObservationService{

    public MapperUtils mapper = Mappers.getMapper(MapperUtils.class);

    Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private ObservationValidation validation;

    @Autowired
    private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

    @Autowired
    private GeneralOPDServiceImpl generalOPDService;

    @Autowired
    private CommonNurseServiceImpl commonNurseService;

    @Autowired
    private BenMedHistoryRepo benMedHistoryRepo;

    @Override
    public ObservationExt createObservation(HttpServletRequest theRequest, ObservationExt observationExt) throws Exception{

        String typeOfObservation = "";
        if(observationExt.hasCategory() && observationExt.getCategoryFirstRep().hasText()){
            typeOfObservation = observationExt.getCategoryFirstRep().getText();
        }else {
            logger.error("'category' or its sub-field 'text' missing. Unable to get the type of data contained in the given Observation resource");
            throw new UnprocessableEntityException("Unable to extract category of Observation resource. Field 'category' or its sub-field 'text' MISSING ");
        }

        switch (typeOfObservation){
            case "Vital Signs":
                return updateVitalsFromObservation(theRequest,observationExt);
            case "History":
                return createOrUpdateHistoryFromObservation(theRequest, observationExt);
            default:
                throw new UnprocessableEntityException("Cannot determine 'category' of given resource. Unknown value for field 'text'.");
        }
    }

    private ObservationExt updateVitalsFromObservation(HttpServletRequest theRequest, ObservationExt observationExt) throws Exception {
        validation.vitalsObservationValidator(observationExt);

        //Todo - Currently implemented considering all relevant IDs(beneficiaryID, benRegID, benFlowID) are coming in payload.
        //Todo - If not, might need to write new APIs to fetch necessary IDs through some sort of logic. And then use those further.
        MandatoryFieldsDTO mandatoryFieldsDTO = mapper.observationResourceToMandatoryFieldsDTO(observationExt);

        BeneficiaryFlowStatus beneficiaryFlowStatus = beneficiaryFlowStatusRepo.getBenDetailsForLeftSidePanel(Long.parseLong(mandatoryFieldsDTO.getBenFlowID()));

        if (beneficiaryFlowStatus !=null ) {
            mandatoryFieldsDTO.setBenVisitID(beneficiaryFlowStatus.getBenVisitID().toString());
            mandatoryFieldsDTO.setVisitCode(beneficiaryFlowStatus.getBenVisitCode().toString());
        }
        else {
            logger.error("No beneficiary flow status record found for the provided benFlowID");
            throw new ResourceNotFoundException("No record found for given benFlowID");
        }

        VitalDetailsDTO vitalDetailsDTO = new VitalDetailsDTO();
        try {
            vitalDetailsDTO = MapperMethods.observationVitalsToDTO(observationExt);
        }catch (IEMRException e){
            logger.error("Encountered custom exception - IEMRException while trying to map Json with VitalDetails class using Input Mapper " + e);
            throw new InternalErrorException("Error mapping json to VitalDetails class " + e);
        }
        vitalDetailsDTO.setBeneficiaryRegID(mandatoryFieldsDTO.getBeneficiaryRegID());
        vitalDetailsDTO.setModifiedBy(mandatoryFieldsDTO.getModifiedBy());
        vitalDetailsDTO.setBenVisitID(mandatoryFieldsDTO.getBenVisitID());
        vitalDetailsDTO.setVisitCode(mandatoryFieldsDTO.getVisitCode());

        String vitalDetailsDTOGson = new GsonBuilder().create().toJson(vitalDetailsDTO);
        JsonObject vitalDetailsOBJ = new JsonParser().parse(vitalDetailsDTOGson).getAsJsonObject();

        generalOPDService.updateBenVitalDetails(vitalDetailsOBJ);

        return observationExt;
    }

    private ObservationExt createOrUpdateHistoryFromObservation(HttpServletRequest theRequest, ObservationExt observationExt) throws Exception{
        validation.historyObservationValidator(observationExt);

        //Todo - Currently implemented considering all relevant IDs(beneficiaryID, benRegID, benFlowID) are coming in payload.
        //Todo - If not, might need to write new APIs to fetch necessary IDs through some sort of logic. And then use those further.
        MandatoryFieldsDTO mandatoryFieldsDTO = mapper.observationResourceToMandatoryFieldsDTO(observationExt);

        BeneficiaryFlowStatus beneficiaryFlowStatus = beneficiaryFlowStatusRepo.getBenDetailsForLeftSidePanel(Long.parseLong(mandatoryFieldsDTO.getBenFlowID()));

        if (beneficiaryFlowStatus !=null ) {
            mandatoryFieldsDTO.setBenVisitID(beneficiaryFlowStatus.getBenVisitID().toString());
            mandatoryFieldsDTO.setVisitCode(beneficiaryFlowStatus.getBenVisitCode().toString());
        }
        else {
            logger.error("No beneficiary flow status record found for the provided benFlowID");
            throw new ResourceNotFoundException("No record found for given benFlowID");
        }

        if (observationExt.getCode().hasText() &&
                observationExt.getCode().getText().equalsIgnoreCase("Past medical history")){
            PastHistoryDTO pastHistoryDTO = mapper.mandatoryFieldsDTOToPastHistoryDTO(mandatoryFieldsDTO);

            pastHistoryDTO.setPastIllness(MapperMethods.observationPastHistoryToPastIllnessDTO(observationExt));
            pastHistoryDTO.setPastSurgery(MapperMethods.observationPastHistoryToPastSurgeryDTO(observationExt));


            String pastHistoryDTOGson = new GsonBuilder().create().toJson(pastHistoryDTO);
            JsonObject pastHistoryJson = new JsonParser().parse(pastHistoryDTOGson).getAsJsonObject();

            try{
                BenMedHistory benMedHistory = InputMapper.gson().fromJson(pastHistoryJson,
                        BenMedHistory.class);

                commonNurseService.updateBenPastHistoryDetails(benMedHistory);
            }catch (IEMRException e){
                logger.error("Encountered custom exception - IEMRException while trying to map Json with BenMedHistory class using Input Mapper " + e);
                throw new InternalErrorException("Error mapping json to BenMedHistory class " + e);
            }
        }else {
            logger.error("sub-field 'text' of 'code' missing or is unrecognizable. Unable to get the type of history contained in the given History Observation resource");
            throw new UnprocessableEntityException("Cannot determine type of history from given History Observation resource. Sub-field 'text' of 'code' MISSING or UNRECOGNIZABLE ");
        }

        return observationExt;
    }

    @Override
    public List<PastHistoryDTO> getBenMedHistoryByLocationAndLastModifDate(Integer providerServiceMapId, Integer vanID, Timestamp lastModifDate){
        List<PastHistoryDTO> pastHistoryDTOList = new ArrayList<>();
        List<BenMedHistory> listHistory = benMedHistoryRepo.getBenMedHistoryByLocationAndLastModDate(providerServiceMapId, vanID, lastModifDate);
        System.out.println("listHistory size "+listHistory.size());
        if(listHistory != null && !listHistory.isEmpty()){
            for (int i = 0; i < listHistory.size(); i++) {
                PastHistoryDTO pastHistoryDTO = new PastHistoryDTO();
                BenMedHistory benMedHistory = listHistory.get(i);
                BeneficiaryFlowStatus beneficiaryFlowStatus = beneficiaryFlowStatusRepo.getBenFlowByVisitIDAndVisitCode(benMedHistory.getBenVisitID(), benMedHistory.getVisitCode());
                if(beneficiaryFlowStatus !=null){
                    pastHistoryDTO.setBenFlowID(beneficiaryFlowStatus.getBenFlowID());
                    pastHistoryDTO.setBeneficiaryID(beneficiaryFlowStatus.getBeneficiaryID());
                }
                pastHistoryDTO.setId(benMedHistory.getBenMedHistoryID());
                pastHistoryDTO.setVanID(benMedHistory.getVanID());
                pastHistoryDTO.setParkingPlaceID(benMedHistory.getParkingPlaceID());
                pastHistoryDTO.setProviderServiceMapID(benMedHistory.getProviderServiceMapID());
                pastHistoryDTO.setCreatedBy(benMedHistory.getCreatedBy());
                pastHistoryDTO.setBeneficiaryRegID(benMedHistory.getBeneficiaryRegID()+"");

                List<PastIllnessDTO> pastIllnessDTOList = new ArrayList<>();
                PastIllnessDTO illnessDTO = new PastIllnessDTO();
                illnessDTO.setIllnessTypeID(benMedHistory.getIllnessTypeID()+"");
                illnessDTO.setIllnessType(benMedHistory.getIllnessType());
                illnessDTO.setOtherIllnessType(benMedHistory.getOtherIllnessType());
                Map<String, Object> timePeriodIllness = Utility.convertTimeToWords(benMedHistory.getYearofIllness(), benMedHistory.getCreatedDate());
                if(timePeriodIllness !=null){
                    illnessDTO.setTimePeriodAgo(timePeriodIllness.get("timePeriodAgo") +"");
                    illnessDTO.setTimePeriodUnit(timePeriodIllness.get("timePeriodUnit")+"");
                }
                pastIllnessDTOList.add(illnessDTO);
                pastHistoryDTO.setPastIllness(pastIllnessDTOList);

                List<PastSurgeryDTO> pastSurgeryDTOList = new ArrayList<>();
                PastSurgeryDTO surgeryDTO = new PastSurgeryDTO();
                surgeryDTO.setSurgeryID(benMedHistory.getSurgeryID()+"");
                surgeryDTO.setSurgeryType(benMedHistory.getSurgeryType());
                surgeryDTO.setOtherSurgeryType(benMedHistory.getOtherSurgeryType());
                Map<String, Object> timePeriodSurgery = Utility.convertTimeToWords(benMedHistory.getYearofSurgery(), benMedHistory.getCreatedDate());
                if(timePeriodSurgery!=null){
                    surgeryDTO.setTimePeriodAgo(timePeriodSurgery.get("timePeriodAgo")+"");
                    surgeryDTO.setTimePeriodUnit(timePeriodSurgery.get("timePeriodUnit")+"");
                }
                pastSurgeryDTOList.add(surgeryDTO);
                pastHistoryDTO.setPastSurgery(pastSurgeryDTOList);

                pastHistoryDTOList.add(pastHistoryDTO);
            }
        }

        return pastHistoryDTOList;
    }
}
