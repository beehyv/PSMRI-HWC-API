package com.iemr.hwc.fhir.service.medicationStatement;

import com.iemr.hwc.fhir.model.medicationStatement.MedicationStatementExt;
import javax.servlet.http.HttpServletRequest;

public interface MedicationService {
    MedicationStatementExt createMedicationStatement(HttpServletRequest theRequest, MedicationStatementExt medicationStatementExt) throws Exception;
}
