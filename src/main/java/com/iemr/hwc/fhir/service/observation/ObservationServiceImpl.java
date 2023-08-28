package com.iemr.hwc.fhir.service.observation;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.hwc.data.anc.BenMedHistory;
import com.iemr.hwc.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.hwc.data.nurse.BenAnthropometryDetail;
import com.iemr.hwc.data.nurse.BenPhysicalVitalDetail;
import com.iemr.hwc.fhir.dto.historyDetails.pastHistory.PastHistoryDTO;
import com.iemr.hwc.fhir.dto.mandatoryFieldsDTO.MandatoryFieldsDTO;
import com.iemr.hwc.fhir.dto.vitalDetails.VitalDetailsDTO;
import com.iemr.hwc.fhir.model.observation.ObservationExt;
import com.iemr.hwc.fhir.utils.mapper.MapperMethods;
import com.iemr.hwc.fhir.utils.mapper.MapperUtils;
import com.iemr.hwc.fhir.utils.validation.ObservationValidation;
import com.iemr.hwc.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.hwc.repo.nurse.BenAnthropometryRepo;
import com.iemr.hwc.repo.nurse.BenPhysicalVitalRepo;
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
import java.util.HashMap;
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
    private BenPhysicalVitalRepo benPhysicalVitalRepo;

    @Autowired
    private BenAnthropometryRepo benAnthropometryRepo;

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
    public List<VitalDetailsDTO> getVitalsObservationByLocationAndLastModifDate(Integer providerServiceMapId, Integer vanID, Timestamp lastModifDate) {
        Map<String, VitalDetailsDTO> tmpMap = new HashMap<>();
        List<VitalDetailsDTO> listVitalsDTO = new ArrayList<>();
        List<BenPhysicalVitalDetail> listBenPhysicalVitalDetails = benPhysicalVitalRepo.getBenPhysicalVitalDetailByLocationAndLastModDate(providerServiceMapId, vanID, lastModifDate);
        List<BenAnthropometryDetail> listBenAnthropometryDetails = benAnthropometryRepo.getBenAnthropometryDetailByLocationAndLastModDate(providerServiceMapId, vanID, lastModifDate);

        if(listBenPhysicalVitalDetails !=null){
            for (int i = 0; i < listBenPhysicalVitalDetails.size() ; i++) {
                BenPhysicalVitalDetail benPhysicalVitalDetail = listBenPhysicalVitalDetails.get(i);
                BeneficiaryFlowStatus beneficiaryFlowStatus = beneficiaryFlowStatusRepo.getBenFlowByVisitIDAndVisitCode(benPhysicalVitalDetail.getBenVisitID(), benPhysicalVitalDetail.getVisitCode());
                List<BenAnthropometryDetail> listBenAnthropometryDetail = benAnthropometryRepo.getBenAnthropometryDetailByVisitCodeAndVisitID(benPhysicalVitalDetail.getBenVisitID(), benPhysicalVitalDetail.getVisitCode());
                VitalDetailsDTO vitalDetails = new VitalDetailsDTO();
                vitalDetails.setBeneficiaryRegID(benPhysicalVitalDetail.getBeneficiaryRegID()+"");
                vitalDetails.setProviderServiceMapID(benPhysicalVitalDetail.getProviderServiceMapID());
                vitalDetails.setBenVisitID(benPhysicalVitalDetail.getBenVisitID()+"");
                vitalDetails.setTemperature(benPhysicalVitalDetail.getTemperature()+"");
                vitalDetails.setPulseRate(benPhysicalVitalDetail.getPulseRate()+"");
                vitalDetails.setSPO2(benPhysicalVitalDetail.getsPO2());
                vitalDetails.setSystolicBP_1stReading(benPhysicalVitalDetail.getSystolicBP_1stReading()+"");
                vitalDetails.setDiastolicBP_1stReading(benPhysicalVitalDetail.getDiastolicBP_1stReading()+"");
                vitalDetails.setRespiratoryRate(benPhysicalVitalDetail.getRespiratoryRate()+"");
                vitalDetails.setRbsTestResult(benPhysicalVitalDetail.getRbsTestResult());
                vitalDetails.setCreatedBy(benPhysicalVitalDetail.getCreatedBy());
                vitalDetails.setModifiedBy(benPhysicalVitalDetail.getModifiedBy());
                vitalDetails.setVanID(benPhysicalVitalDetail.getVanID());
                vitalDetails.setParkingPlaceID(benPhysicalVitalDetail.getParkingPlaceID());

                if (listBenAnthropometryDetail!=null && listBenAnthropometryDetail.size()>0){
                    BenAnthropometryDetail benAnthropometryDetail = listBenAnthropometryDetail.get(0);
                    vitalDetails.setWeight_Kg(benAnthropometryDetail.getWeight_Kg()+"");
                    vitalDetails.setHeight_cm(benAnthropometryDetail.getHeight_cm()+"");
                    vitalDetails.setWaistCircumference_cm(benAnthropometryDetail.getWaistCircumference_cm()+"");
                    vitalDetails.setBMI(benAnthropometryDetail.getbMI()+"");

                }
                if (beneficiaryFlowStatus != null){
                    vitalDetails.setBeneficiaryID(beneficiaryFlowStatus.getBeneficiaryID());
                    vitalDetails.setBenFlowID(beneficiaryFlowStatus.getBenFlowID());
                }

                tmpMap.put(benPhysicalVitalDetail.getBenVisitID()+"_"+benPhysicalVitalDetail.getVisitCode(), vitalDetails);

                listVitalsDTO.add(vitalDetails);
            }
        }

        if(listBenPhysicalVitalDetails !=null){
            for (int i = 0; i < listBenAnthropometryDetails.size() ; i++) {
                BenAnthropometryDetail benAnthropometryDetail = listBenAnthropometryDetails.get(i);
                VitalDetailsDTO tmpVitalDetailsDTO = tmpMap.get(benAnthropometryDetail.getBenVisitID()+"_"+benAnthropometryDetail.getVisitCode());
                if(tmpVitalDetailsDTO == null){
                    BeneficiaryFlowStatus beneficiaryFlowStatus = beneficiaryFlowStatusRepo.getBenFlowByVisitIDAndVisitCode(benAnthropometryDetail.getBenVisitID(), benAnthropometryDetail.getVisitCode());
                    List<BenPhysicalVitalDetail> listBenPhysicalVitalDetail = benPhysicalVitalRepo.getBenPhysicalVitalDetailByVisitCodeAndVisitID(benAnthropometryDetail.getBenVisitID(), benAnthropometryDetail.getVisitCode());
                    VitalDetailsDTO vitalDetails = new VitalDetailsDTO();
                    vitalDetails.setBeneficiaryRegID(benAnthropometryDetail.getBeneficiaryRegID()+"");
                    vitalDetails.setProviderServiceMapID(benAnthropometryDetail.getProviderServiceMapID());
                    vitalDetails.setBenVisitID(benAnthropometryDetail.getBenVisitID()+"");
                    vitalDetails.setWeight_Kg(benAnthropometryDetail.getWeight_Kg()+"");
                    vitalDetails.setHeight_cm(benAnthropometryDetail.getHeight_cm()+"");
                    vitalDetails.setWaistCircumference_cm(benAnthropometryDetail.getWaistCircumference_cm()+"");
                    vitalDetails.setBMI(benAnthropometryDetail.getbMI()+"");
                    vitalDetails.setCreatedBy(benAnthropometryDetail.getCreatedBy());
                    vitalDetails.setModifiedBy(benAnthropometryDetail.getModifiedBy());
                    vitalDetails.setVanID(benAnthropometryDetail.getVanID());
                    vitalDetails.setParkingPlaceID(benAnthropometryDetail.getParkingPlaceID());

                    if (listBenPhysicalVitalDetail!=null && listBenPhysicalVitalDetail.size() > 0){
                        BenPhysicalVitalDetail benPhysicalVitalDetail = listBenPhysicalVitalDetail.get(0);
                        vitalDetails.setTemperature(benPhysicalVitalDetail.getTemperature()+"");
                        vitalDetails.setPulseRate(benPhysicalVitalDetail.getPulseRate()+"");
                        vitalDetails.setSPO2(benPhysicalVitalDetail.getsPO2());
                        vitalDetails.setSystolicBP_1stReading(benPhysicalVitalDetail.getSystolicBP_1stReading()+"");
                        vitalDetails.setDiastolicBP_1stReading(benPhysicalVitalDetail.getDiastolicBP_1stReading()+"");
                        vitalDetails.setRespiratoryRate(benPhysicalVitalDetail.getRespiratoryRate()+"");
                        vitalDetails.setRbsTestResult(benPhysicalVitalDetail.getRbsTestResult());
                    }
                    if (beneficiaryFlowStatus != null){
                        vitalDetails.setBeneficiaryID(beneficiaryFlowStatus.getBeneficiaryID());
                        vitalDetails.setBenFlowID(beneficiaryFlowStatus.getBenFlowID());
                    }

                    listVitalsDTO.add(vitalDetails);
                }
            }
        }
        return listVitalsDTO;
    }
}
