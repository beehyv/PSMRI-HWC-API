package com.iemr.hwc.fhir.dto.historyDetails.pastHistory;

import lombok.Data;
import java.util.List;

@Data
public class PastHistoryDTO {
    private Long id;
    private String beneficiaryRegID;
    private Integer providerServiceMapID;
    private String benVisitID;
    private String visitCode;
    private String createdBy;
    private Integer vanID;
    private Integer parkingPlaceID;
    private Long benFlowID;
    private Long beneficiaryID;
    private List<PastIllnessDTO> pastIllness;
    private List<PastSurgeryDTO> pastSurgery;
}
