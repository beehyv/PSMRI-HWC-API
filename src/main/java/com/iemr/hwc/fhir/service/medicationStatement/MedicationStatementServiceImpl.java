package com.iemr.hwc.fhir.service.medicationStatement;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.hwc.data.anc.WrapperMedicationHistory;
import com.iemr.hwc.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.hwc.fhir.dto.historyDetails.medicationHistory.MedicationHistoryDTO;
import com.iemr.hwc.fhir.dto.historyDetails.medicationHistory.MedicationListDTO;
import com.iemr.hwc.fhir.dto.mandatoryFieldsDTO.MandatoryFieldsDTO;
import com.iemr.hwc.fhir.model.medicationStatement.MedicationStatementExt;
import com.iemr.hwc.fhir.utils.mapper.MapperMethods;
import com.iemr.hwc.fhir.utils.mapper.MapperUtils;
import com.iemr.hwc.fhir.utils.validation.MedicationStatementValidation;
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

@Service
public class MedicationStatementServiceImpl implements MedicationService{

    public MapperUtils mapper = Mappers.getMapper(MapperUtils.class);

    Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private MedicationStatementValidation validation;

    @Autowired
    private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

    @Autowired
    private CommonNurseServiceImpl commonNurseService;

    @Override
    public MedicationStatementExt createMedicationStatement(HttpServletRequest theRequest, MedicationStatementExt medicationStatementExt) throws Exception {

        validation.medicationStatementValidator(medicationStatementExt);

        //Todo - Currently implemented considering all relevant IDs(benRegID, benFlowID) are coming in payload.
        //Todo - If not, might need to write new APIs to fetch necessary IDs through some sort of logic. And then use those further.
        MandatoryFieldsDTO mandatoryFieldsDTO = mapper.medicationStatementResourceToMandatoryFieldsDTO(medicationStatementExt);

        BeneficiaryFlowStatus beneficiaryFlowStatus = beneficiaryFlowStatusRepo.getBenDetailsForLeftSidePanel(Long.parseLong(mandatoryFieldsDTO.getBenFlowID()));

        if (beneficiaryFlowStatus !=null ) {
            mandatoryFieldsDTO.setBenVisitID(beneficiaryFlowStatus.getBenVisitID().toString());
            mandatoryFieldsDTO.setVisitCode(beneficiaryFlowStatus.getBenVisitCode().toString());
        }
        else {
            logger.error("No beneficiary flow status record found for the provided benFlowID");
            throw new ResourceNotFoundException("No record found for given benFlowID");
        }

        MedicationHistoryDTO medicationHistoryDTO = mapper.mandatoryFieldsDTOToMedicationHistoryDTO(mandatoryFieldsDTO);

        if (medicationStatementExt.getMedicationCodeableConcept().hasCoding()) {
            medicationHistoryDTO.setMedicationHistoryList(MapperMethods.medicationToMedicationListDTO(medicationStatementExt));
        }
        else {
            medicationHistoryDTO.setMedicationHistoryList(new ArrayList<MedicationListDTO>());
        }

        String medicationHistoryDTOGson = new GsonBuilder().create().toJson(medicationHistoryDTO);
        JsonObject medicationHistoryDTOJson = new JsonParser().parse(medicationHistoryDTOGson).getAsJsonObject();

        try{
            WrapperMedicationHistory wrapperMedicationHistory = InputMapper.gson()
                    .fromJson(medicationHistoryDTOJson, WrapperMedicationHistory.class);

            commonNurseService.updateBenMedicationHistory(wrapperMedicationHistory);
        }catch (IEMRException e){
            logger.error("Encountered custom exception - IEMRException while trying to map Json with WrapperMedicationHistory class using Input Mapper " + e);
            throw new InternalErrorException("Error mapping json to WrapperMedicationHistory class " + e);
        }

        return medicationStatementExt;
    }
}
