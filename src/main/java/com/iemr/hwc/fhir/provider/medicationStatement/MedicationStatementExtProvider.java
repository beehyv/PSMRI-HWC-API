package com.iemr.hwc.fhir.provider.medicationStatement;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.iemr.hwc.fhir.model.medicationStatement.MedicationStatementExt;
import com.iemr.hwc.fhir.service.medicationStatement.MedicationService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

@Component
public class MedicationStatementExtProvider implements IResourceProvider {

    @Autowired
    private MedicationService medicationService;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return MedicationStatementExt.class;
    }

    @Create()
    public MethodOutcome createMedicationStatement(HttpServletRequest theRequest, @ResourceParam MedicationStatementExt medicationStatementExt) throws Exception{

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);
        method.setResource(medicationService.createMedicationStatement(theRequest,medicationStatementExt));
        return method;
    }
}
