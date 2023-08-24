package com.iemr.hwc.fhir.service.familyMemberHistory;

import com.iemr.hwc.fhir.model.familyMemberHistory.FamilyMemberHistoryExt;
import javax.servlet.http.HttpServletRequest;

public interface CoMorbidAndFMHistoryService {
    FamilyMemberHistoryExt createCoMorbidAndFMHistory(HttpServletRequest theRequest, FamilyMemberHistoryExt familyMemberHistoryExt) throws Exception;
}
