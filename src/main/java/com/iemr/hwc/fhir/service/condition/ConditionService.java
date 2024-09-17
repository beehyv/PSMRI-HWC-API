package com.iemr.hwc.fhir.service.condition;

import com.iemr.hwc.data.quickConsultation.BenChiefComplaint;
import com.iemr.hwc.fhir.model.condition.ConditionExt;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.List;

public interface ConditionService {
    ConditionExt addNewChiefComplaint(HttpServletRequest theRequest, ConditionExt conditionExt) throws Exception;
    public List<BenChiefComplaint> getChiefComplaintByLocationAndLastModifDate(Integer providerServiceMapId, Integer vanID, Timestamp lastModifDate);
}
