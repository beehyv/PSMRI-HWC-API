package com.iemr.hwc.fhir.service.familyMemberHistory;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.hwc.data.anc.BenFamilyHistory;
import com.iemr.hwc.data.anc.WrapperComorbidCondDetails;
import com.iemr.hwc.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.hwc.fhir.dto.historyDetails.comorbidConditions.ComorbidConditionsDTO;
import com.iemr.hwc.fhir.dto.historyDetails.comorbidConditions.ConcurrentConditionsDTO;
import com.iemr.hwc.fhir.dto.historyDetails.familyHistory.FamilyDiseaseListDTO;
import com.iemr.hwc.fhir.dto.historyDetails.familyHistory.FamilyHistoryDTO;
import com.iemr.hwc.fhir.dto.mandatoryFieldsDTO.MandatoryFieldsDTO;
import com.iemr.hwc.fhir.model.familyMemberHistory.FamilyMemberHistoryExt;
import com.iemr.hwc.fhir.utils.mapper.MapperMethods;
import com.iemr.hwc.fhir.utils.mapper.MapperUtils;
import com.iemr.hwc.fhir.utils.validation.FMHistoryValidation;
import com.iemr.hwc.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.hwc.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.hwc.utils.exception.IEMRException;
import com.iemr.hwc.utils.mapper.InputMapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CoMorbidAndFMHistoryServiceImpl implements CoMorbidAndFMHistoryService{

    public MapperUtils mapper = Mappers.getMapper(MapperUtils.class);

    Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private FMHistoryValidation validation;

    @Autowired
    private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

    @Autowired
    private CommonNurseServiceImpl commonNurseService;

    @Autowired
    private MapperMethods mapperMethods;

    @Override
    public FamilyMemberHistoryExt createCoMorbidAndFMHistory(HttpServletRequest theRequest, FamilyMemberHistoryExt familyMemberHistoryExt) throws Exception{
        validation.CoMorbidAndFMHistoryValidator(familyMemberHistoryExt);

        //Todo - Currently implemented considering benRegID coming in payload in 'patient'.
        MandatoryFieldsDTO mandatoryFieldsDTO = mapper.fMHistoryResourceToMandatoryFieldsDTO(familyMemberHistoryExt);

        BeneficiaryFlowStatus beneficiaryFlowStatus = beneficiaryFlowStatusRepo.getBenDetailsForLeftSidePanel(Long.parseLong(mandatoryFieldsDTO.getBenFlowID()));

        if (beneficiaryFlowStatus !=null ) {
            mandatoryFieldsDTO.setBenVisitID(beneficiaryFlowStatus.getBenVisitID().toString());
            mandatoryFieldsDTO.setVisitCode(beneficiaryFlowStatus.getBenVisitCode().toString());
        }
        else {
            logger.error("No beneficiary flow status record found for the provided benFlowID");
            throw new ResourceNotFoundException("No record found for given benFlowID");
        }

        ComorbidConditionsDTO comorbidConditionsDTO = mapper.mandatoryFieldsDTOToComorbidConditionsDTO(mandatoryFieldsDTO);
        FamilyHistoryDTO familyHistoryDTO = mapper.mandatoryFieldsDTOToFamilyHistoryDTO(mandatoryFieldsDTO);

        List<ConcurrentConditionsDTO> conditionsDTOList = new ArrayList<>();
        List<FamilyDiseaseListDTO> familyDiseaseList = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();

        if (familyMemberHistoryExt.hasCondition()){
            Map<String,Object> mappedLists = mapperMethods.fMHistoryResourceToCoMorbidAndFMList(familyMemberHistoryExt);

            conditionsDTOList = objectMapper.convertValue(mappedLists.get("conditionList"),new TypeReference<List<ConcurrentConditionsDTO>>() {});
            familyDiseaseList = objectMapper.convertValue(mappedLists.get("fMHistoryList"),new TypeReference<List<FamilyDiseaseListDTO>>() {});
        }

        familyHistoryDTO.setFamilyDiseaseList(familyDiseaseList);
        comorbidConditionsDTO.setComorbidityConcurrentConditionsList(conditionsDTOList);

        String coMorbidDTOGson = new GsonBuilder().serializeNulls().create().toJson(comorbidConditionsDTO);
        JsonObject coMorbidOBJ = new JsonParser().parse(coMorbidDTOGson).getAsJsonObject();

        String familyHistoryDTOGson = new GsonBuilder().serializeNulls().create().toJson(familyHistoryDTO);
        JsonObject familyHistoryOBJ = new JsonParser().parse(familyHistoryDTOGson).getAsJsonObject();

        try {
            WrapperComorbidCondDetails wrapperComorbidCondDetails = InputMapper.gson()
                    .fromJson(coMorbidOBJ, WrapperComorbidCondDetails.class);
            commonNurseService.updateBenComorbidConditions(wrapperComorbidCondDetails);

            BenFamilyHistory benFamilyHistory = InputMapper.gson().fromJson(familyHistoryOBJ,
                    BenFamilyHistory.class);
            commonNurseService.updateBenFamilyHistory(benFamilyHistory);

        }catch (IEMRException e){
            logger.error("Encountered custom exception - IEMRException while trying to map Json with class using Input Mapper " + e);
            throw new InternalErrorException("Error mapping json to class " + e);
        }

        return familyMemberHistoryExt;
    }
}
