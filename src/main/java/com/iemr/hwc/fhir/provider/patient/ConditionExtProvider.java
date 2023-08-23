package com.iemr.hwc.fhir.provider.patient;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.hwc.data.nurse.BeneficiaryChiefComplaint;
import com.iemr.hwc.data.nurse.BeneficiaryVisitDetail;
import com.iemr.hwc.fhir.dto.BenVisitsDTO;
import com.iemr.hwc.fhir.dto.beneficiary.BeneficiariesDTOSearch;
import com.iemr.hwc.fhir.dto.beneficiary.BeneficiaryDTO;
import com.iemr.hwc.fhir.model.patient.ConditionExt;
import com.iemr.hwc.fhir.model.patient.EncounterExt;
import com.iemr.hwc.fhir.model.patient.PatientExt;
import com.iemr.hwc.fhir.utils.mapper.MapperUtils;
import com.iemr.hwc.fhir.utils.validation.Validations;
import com.iemr.hwc.service.nurse.NurseServiceImpl;
import com.iemr.hwc.service.registrar.RegistrarServiceImpl;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConditionExtProvider implements IResourceProvider {

    public MapperUtils mapper = Mappers.getMapper(MapperUtils.class);
    Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private RegistrarServiceImpl registrarServiceImpl;

    @Autowired
    private NurseServiceImpl nurseServiceImpl;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return PatientExt.class;
    }

    @Search()
    public List<ConditionExt> findChiefComplaintByDistrictAndLastModifDate(HttpServletRequest theRequest, @RequiredParam(name = "providerServiceMapId") StringParam providerServiceMapId, @RequiredParam(name = "vanID") StringParam vanID, @RequiredParam(name = "lastModif") DateParam lastModifyDate) {

        List<ConditionExt> listRes = new ArrayList<>();
        try {
            String authorization = theRequest.getHeader("Authorization");
            System.out.println("Last modif date babs **********************************************"+lastModifyDate);
            String jsonStringBody = "{'providerServiceMapId':"+providerServiceMapId.getValue()+", 'vanID':"+vanID.getValue()+",'lastModifyDate':"+lastModifyDate.getValue()+"}";
            System.out.println("Last modif date value "+lastModifyDate.getValue());
            List<BeneficiaryChiefComplaint> listChiefComplaint = nurseServiceImpl.getChiefComplaintByLocationAndLastModifDate(Integer.parseInt(providerServiceMapId.getValue()), Integer.parseInt(vanID.getValue()),
                    new Timestamp(lastModifyDate.getValue().getTime()));
//
            System.out.println("List chief compliant "+listChiefComplaint.size());
            for (BeneficiaryChiefComplaint benef:listChiefComplaint) {
                ConditionExt condition = new ConditionExt();
                condition.setId(benef.getID()+"");
                condition.setBeneficiaryID(new StringType(benef.getBeneficiaryRegID()+""));
                condition.setProviderServiceMapId(new StringType(benef.getProviderServiceMapID()+""));
                condition.setVanID(new StringType(benef.getVanID()+""));
                condition.setParkingPlaceID(new StringType(benef.getParkingPlaceID()+""));
                condition.setCreatedBy(new StringType(benef.getCreatedBy()));
                condition.setBeneficiaryRegID(new StringType(benef.getBeneficiaryRegID()+""));
                condition.setBenFlowID(new StringType(""));

                Coding coding = new Coding();
                coding.setCode(benef.getUnitOfDuration());
                coding.setDisplay(benef.getDuration()+"");
                condition.setDuration(coding);

                Reference ref= new Reference();
                ref.setReference(benef.getID()+"");
                condition.setSubject(ref);

                CodeableConcept concept = new CodeableConcept();
                List<Coding> listCoding = new ArrayList<>();
                Coding coding1 = new Coding();
                coding1.setSystem("http://snomed.info/sct");
                coding1.setCode("1");
                coding1.setDisplay(benef.getT_benVisitDetail().getVisitCategory());
                listCoding.add(coding1);
                concept.setCoding(listCoding);
                condition.setCode(concept);

                List<Annotation> listAnnot = new ArrayList<>();
                Annotation annot = new Annotation();
                annot.setText(benef.getT_benVisitDetail().getVisitReason());
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