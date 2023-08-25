package com.iemr.hwc.fhir.service.observation;

import com.iemr.hwc.fhir.dto.vitalDetails.VitalDetailsDTO;
import com.iemr.hwc.fhir.model.observation.ObservationExt;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.List;

public interface ObservationService {
    ObservationExt createObservation(HttpServletRequest theRequest, ObservationExt observationExt) throws Exception;
    public List<VitalDetailsDTO> getVitalsObservationByLocationAndLastModifDate(Integer providerServiceMapId, Integer vanID, Timestamp lastModifDate);
}
