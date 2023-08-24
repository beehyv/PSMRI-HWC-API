package com.iemr.hwc.fhir.provider.condition;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.iemr.hwc.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.hwc.data.quickConsultation.BenChiefComplaint;
import com.iemr.hwc.fhir.model.condition.ConditionExt;
import com.iemr.hwc.fhir.service.condition.ConditionService;
import com.iemr.hwc.fhir.service.condition.ConditionServiceImpl;
import com.iemr.hwc.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConditionExtProvider implements IResourceProvider {

    @Autowired
    private ConditionService conditionService;

    @Autowired
    private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

    @Autowired
    private ConditionServiceImpl conditionServiceImpl;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ConditionExt.class;
    }

    public ConditionExt getMockCondition(){
        ConditionExt mockCondition = new ConditionExt();
        List<CodeableConcept> category = new ArrayList<>();

        Coding code = new Coding();
        code.setSystem("http://snomed.info/sct");
        code.setCode("55607006");
        code.setDisplay("Problem");
        CodeableConcept snomedCat = new CodeableConcept(code);
        category.add(snomedCat);

        Coding code2 = new Coding();
        code2.setSystem("http://terminology.hl7.org/CodeSystem/condition-category");
        code2.setCode("problem-list-item");
        CodeableConcept snomedSubCat = new CodeableConcept(code2);
        category.add(snomedSubCat);

        mockCondition.setCategory(category);
        return mockCondition;
    }


    @Create()
    public MethodOutcome createCondition(HttpServletRequest theRequest, @ResourceParam ConditionExt conditionExt) throws Exception {

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);
        method.setResource(conditionService.addNewChiefComplaint(theRequest,conditionExt));
        return method;
    }

    @Search()
    public List<ConditionExt> findChiefComplaintByDistrictAndLastModifDate(HttpServletRequest theRequest, @RequiredParam(name = "providerServiceMapId") StringParam providerServiceMapId, @RequiredParam(name = "vanID") StringParam vanID, @RequiredParam(name = "lastModif") DateParam lastModifyDate) {

        List<ConditionExt> listRes = new ArrayList<>();
        try {
            String authorization = theRequest.getHeader("Authorization");
            List<BenChiefComplaint> listChiefComplaint = conditionServiceImpl.getChiefComplaintByLocationAndLastModifDate(Integer.parseInt(providerServiceMapId.getValue()), Integer.parseInt(vanID.getValue()),
                    new Timestamp(lastModifyDate.getValue().getTime()));

            for (BenChiefComplaint benef:listChiefComplaint) {
                BeneficiaryFlowStatus beneficiaryFlowStatus = beneficiaryFlowStatusRepo.getBenFlowByVisitIDAndVisitCode(benef.getBenVisitID(), benef.getVisitCode());
                ConditionExt condition = new ConditionExt();
                condition.setId(benef.getBenChiefComplaintID()+"");
                condition.setProviderServiceMapId(new StringType(benef.getProviderServiceMapID()+""));
                condition.setVanID(new StringType(benef.getVanID()+""));
                condition.setParkingPlaceID(new StringType(benef.getParkingPlaceID()+""));
                condition.setCreatedBy(new StringType(benef.getCreatedBy()));
                condition.setBeneficiaryRegID(new StringType(benef.getBeneficiaryRegID()+""));
                if (beneficiaryFlowStatus != null){
                    condition.setBeneficiaryID(new StringType(beneficiaryFlowStatus.getBeneficiaryID()+""));
                    condition.setBenFlowID(new StringType(beneficiaryFlowStatus.getBenFlowID()+""));
                }
                else {
                    throw new ResourceNotFoundException("No record found for given benVisitID and BenVisitCode");
                }

                Coding coding = new Coding();
                coding.setCode(benef.getUnitOfDuration());
                coding.setDisplay(benef.getDuration()+"");
                condition.setDuration(coding);

                Reference ref= new Reference();
                ref.setReference(benef.getBenChiefComplaintID()+"");
                condition.setSubject(ref);

                CodeableConcept concept = new CodeableConcept();
                List<Coding> listCoding = new ArrayList<>();
                Coding coding1 = new Coding();
                coding1.setSystem("http://snomed.info/sct");
                coding1.setCode(benef.getChiefComplaintID()+"");
                coding1.setDisplay(benef.getChiefComplaint());
                listCoding.add(coding1);
                concept.setCoding(listCoding);
                condition.setCode(concept);

                List<Annotation> listAnnot = new ArrayList<>();
                Annotation annot = new Annotation();
                annot.setText(benef.getDescription());
                listAnnot.add(annot);
                condition.setNote(listAnnot);

                listRes.add(condition);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return listRes;
    }
}
