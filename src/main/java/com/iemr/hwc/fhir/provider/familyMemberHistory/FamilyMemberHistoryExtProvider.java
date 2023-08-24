package com.iemr.hwc.fhir.provider.familyMemberHistory;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.iemr.hwc.fhir.model.familyMemberHistory.FamilyMemberHistoryExt;
import com.iemr.hwc.fhir.service.familyMemberHistory.CoMorbidAndFMHistoryService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

@Component
public class FamilyMemberHistoryExtProvider implements IResourceProvider {

    @Autowired
    private CoMorbidAndFMHistoryService service;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return FamilyMemberHistoryExt.class;
    }

    @Create()
    public MethodOutcome createCoMorbidAndFamilyMemberHistory(HttpServletRequest theRequest, @ResourceParam FamilyMemberHistoryExt familyMemberHistoryExt) throws Exception{

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);
        method.setResource(service.createCoMorbidAndFMHistory(theRequest,familyMemberHistoryExt));
        return method;
    }
}
