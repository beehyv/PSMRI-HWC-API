package com.iemr.hwc.fhir.provider.patient;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.iemr.hwc.data.nurse.BeneficiaryVisitDetail;
import com.iemr.hwc.fhir.dto.BenVisitsDTO;
import com.iemr.hwc.fhir.dto.beneficiary.BeneficiariesDTOSearch;
import com.iemr.hwc.fhir.dto.beneficiary.BeneficiaryDTO;
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
import java.util.Date;
import java.util.List;

@Component
public class PatientExtProvider implements IResourceProvider {

    public MapperUtils mapper = Mappers.getMapper(MapperUtils.class);
    Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private RegistrarServiceImpl registrarServiceImpl;

    @Autowired
    private NurseServiceImpl nurseServiceImpl;

    @Autowired
    private Validations validations;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return PatientExt.class;
    }

    @Create()
    public MethodOutcome createPatient(HttpServletRequest theRequest, @ResourceParam PatientExt patientExt) throws Exception {

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);

        //Validating the resource for mandatory fields
        validations.patientResourceValidator(patientExt);

        BeneficiaryDTO benDTO = mapper.patientResourceToBeneficiaryDTOMapping(patientExt);

        String benDTOGson = new GsonBuilder().create().toJson(benDTO);

        String authorization = theRequest.getHeader("Authorization");

        String registeredBeneficiary = registrarServiceImpl.registerBeneficiary(benDTOGson, authorization);

        JsonObject registeredBenJson = new JsonParser().parse(registeredBeneficiary).getAsJsonObject();
        String benID = registeredBenJson.getAsJsonObject("data").get("response").getAsString();
        String[] arrOfResponse = benID.split(":");
        patientExt.setId(arrOfResponse[arrOfResponse.length - 1].trim());

        method.setResource(patientExt);
        return method;
    }

    @Search()
    public List<PatientExt> findPatientsByDistrictAndLastModifDate(HttpServletRequest theRequest, @RequiredParam(name = Patient.SP_ADDRESS) StringParam blockID, @RequiredParam(name = "lastModif") DateParam lastModifyDate) {
//        MethodOutcome method = new MethodOutcome();
//        method.setCreated(true);
//        OperationOutcome opOutcome = new OperationOutcome();
//        method.setOperationOutcome(opOutcome);

        List<PatientExt> listRes = new ArrayList<>();
        List<Patient> retVal = new ArrayList<>();
        try {
            String authorization = theRequest.getHeader("Authorization");
//        System.out.println("Last modif date "+lastModifyDate);
//        String jsonStringBody = "{'blockID':"+blockID.getValue()+",'lastModifyDate':"+lastModifyDate.getValue()+"}";
//        System.out.println("Last modif date value "+lastModifyDate.getValue());
            String response = registrarServiceImpl.getBeneficiaryByBlockIDAndLastModDate(blockID.getValue(), lastModifyDate.getValue(), authorization);
//
//            JsonObject registeredBenJson = new JsonParser().parse(registeredBeneficiary).getAsJsonObject();
            JSONObject jsonObject = new JSONObject(response);
            System.out.println("response Babs "+jsonObject.getJSONObject("response"));
//            JSONArray jsonArray = registeredBenJson.getAsJsonObject("response").getAsJsonArray("data");
            JSONArray jsonArray = new JSONArray(jsonObject.getJSONObject("response").getString("data"));
            System.out.println("JSON ARRAY "+jsonArray);
//            String jsonObject = registeredBenJson.getAsJsonObject("response").get("data").toString();
            Type typeOfSrc = new TypeToken<List<BeneficiariesDTOSearch>>() {}.getType();
            Gson gson = new Gson();
            List<BeneficiariesDTOSearch> listBeneficiaries = gson.fromJson(jsonArray.toString(), typeOfSrc);
            System.out.println("List DTO "+listBeneficiaries.size());
            for (BeneficiariesDTOSearch benef:listBeneficiaries) {
                HumanName human = new HumanName();
                PatientExt patient = new PatientExt();
                patient.setId(1L+"");
                patient.setFatherName(new StringType(benef.getBeneficiaryDetails().getFatherName()));
                patient.setSpouseName(new StringType(benef.getBeneficiaryDetails().getSpouseName()));
                patient.setAgeAtMarriage(new StringType());
                if(benef.getAbhaDetails() != null && benef.getAbhaDetails().size()>0 && benef.getAbhaDetails().get(0) != null){
                    Coding coding = new Coding();
                    coding.setCode(benef.getAbhaDetails().get(0).getHealthID());
                    coding.setDisplay(benef.getAbhaDetails().get(0).getHealthIDNumber());
                    patient.setAbhaGenerationMode(coding);
                    patient.setProviderServiceMapId(new StringType(benef.getBeneficiaryServiceMap().get(0).getBenServiceMapID()+""));
                }

                patient.setVanID(new StringType(benef.getBeneficiaryDetails().getVanID()+""));
                patient.setParkingPlaceID(new StringType(benef.getBeneficiaryDetails().getParkingPlaceID()+""));
                patient.setCreatedBy(new StringType(benef.getCreatedBy()));
                Coding codingState = new Coding();
                codingState.setCode(benef.getCurrentAddress().getStateId()+"");
                codingState.setDisplay(benef.getCurrentAddress().getState());
                patient.setState(codingState);
                Coding codingDistrict = new Coding();
                codingDistrict.setCode(benef.getCurrentAddress().getDistrictId()+"");
                codingDistrict.setDisplay(benef.getCurrentAddress().getDistrict());
                patient.setDistrict(codingDistrict);
                Coding codingBlock = new Coding();
                codingBlock.setCode(benef.getCurrentAddress().getVillageId()+"");
                codingBlock.setDisplay(benef.getCurrentAddress().getVillage());
                patient.setBlock(codingBlock);
                Coding codingDistrictBranch = new Coding();
                codingDistrictBranch.setCode(benef.getCurrentAddress().getSubDistrictId()+"");
                codingDistrictBranch.setDisplay(benef.getCurrentAddress().getSubDistrict());
                patient.setDistrictBranch(codingDistrictBranch);
                Coding codingReligion = new Coding();
                codingReligion.setCode(benef.getBeneficiaryDetails().getReligionId()+"");
                codingReligion.setDisplay(benef.getBeneficiaryDetails().getReligion());
                patient.setReligion(codingReligion);
                Coding codingCommunity = new Coding();
                codingCommunity.setCode(benef.getBeneficiaryDetails().getCommunityId()+"");
                codingCommunity.setDisplay(benef.getBeneficiaryDetails().getCommunity());
                patient.setCommunity(codingCommunity);

                if(benef.getBeneficiaryIdentites()!=null && benef.getBeneficiaryIdentites().size()>0 && benef.getBeneficiaryIdentites().get(0) !=null ){
                    Coding codingGovIdentityType = new Coding();
                    codingGovIdentityType.setCode(benef.getBeneficiaryIdentites().get(0).getIdentityTypeId()+"");
                    codingGovIdentityType.setDisplay(benef.getBeneficiaryIdentites().get(0).getIdentityType());
                    patient.setGovtIdentityType(codingGovIdentityType);
                    patient.setGovtIdentityNo(new StringType(benef.getBeneficiaryIdentites().get(0).getIdentityNo()));
                    Coding codingGovtHealthProgramType = new Coding();
                    codingGovtHealthProgramType.setCode(benef.getBeneficiaryIdentites().get(0).getIdentityTypeId()+"");
                    codingGovtHealthProgramType.setDisplay(benef.getBeneficiaryIdentites().get(0).getIdentityType());
//                patient.setGovtHealthProgramType(codingGovtHealthProgramType);
                }
//
//                patient.setGovtHealthProgramId(benef.);

                List<HumanName> listName = new ArrayList<>();
                HumanName humanName = new HumanName();
                humanName.setFamily(benef.getBeneficiaryDetails().getLastName());
                List<StringType> listGiven = new ArrayList<>();
                listGiven.add(new StringType(benef.getBeneficiaryDetails().getFirstName()));
                listGiven.add(new StringType(benef.getBeneficiaryDetails().getMiddleName()));
                humanName.setGiven(listGiven);
                listName.add(humanName);
                patient.setName(listName);

                List<ContactPoint> listContactPoint = new ArrayList<>();
                ContactPoint contact = new ContactPoint();
//                contact.setSystem(benef.getEmergencyContactTyp());
                contact.setValue(benef.getEmergencyContactNum());
                listContactPoint.add(contact);
                patient.setTelecom(listContactPoint);

//                Enumerations.AdministrativeGender.
//                patient.setGender(Enumerations.AdministrativeGender.MALE);
//                patient.setBirthDate(new Date())
                CodeableConcept concept = new CodeableConcept();
                List<Coding> listForConcept = new ArrayList<>();
                Coding coding1 = new Coding();
                coding1.setCode(benef.getBeneficiaryDetails().getMaritalStatusId()+"");
                coding1.setDisplay(benef.getBeneficiaryDetails().getMaritalStatus());
                listForConcept.add(coding1);
                concept.setCoding(listForConcept);
                concept.setText(benef.getBeneficiaryDetails().getMaritalStatus());
                patient.setMaritalStatus(concept);

                listRes.add(patient);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return listRes;
    }

    @Search()
    public List<EncounterExt> findVisitsByVillageAndLastModifDate(@RequiredParam(name = "type") StringParam type, @RequiredParam(name = Patient.SP_ADDRESS) StringParam blockID, @RequiredParam(name = "lastModif") DateParam lastModifyDate) {
        List<EncounterExt> listRes = new ArrayList<>();
        Gson gson = new Gson();
        Type typeOfSrc = new TypeToken<List<BeneficiaryVisitDetail>>() {
            private static final long serialVersionUID = 1L;
        }.getType();
        String jsonStringBody = "{'blockID':"+blockID.getValue()+",'lastModifyDate':"+lastModifyDate.getValue()+"} "+ type.getValue();
        System.out.println("Received "+ jsonStringBody);
        List<BenVisitsDTO> listVisits = nurseServiceImpl.getVisitByLocationAndLastModifDate(Integer.parseInt(blockID.getValue()), new Timestamp(lastModifyDate.getValue().getTime()));
        System.out.println("Response "+listVisits.get(0).toString());
        try {
            for (int i = 0; i < listVisits.size(); i++) {
                BenVisitsDTO benDetails = listVisits.get(i);
                EncounterExt encounter = new EncounterExt();
                encounter.setId(1L+"");
                encounter.setProviderServiceMapId(new StringType(benDetails.getBenVisitDetails().getProviderServiceMapID()+""));
                encounter.setVanID(new StringType(benDetails.getBenVisitDetails().getVanID()+""));
                encounter.setParkingPlaceID(new StringType(benDetails.getBenVisitDetails().getParkingPlaceID()+""));
                encounter.setCreatedBy(new StringType(benDetails.getBenVisitDetails().getCreatedBy()));
                encounter.setSessionID(new StringType(benDetails.getSessionID()+""));
                encounter.setBeneficiaryID(new StringType(benDetails.getBeneficiaryID()+""));
                encounter.setBenFlowID(new StringType(benDetails.getBenFlowID()+""));
                encounter.setBeneficiaryRegID(new StringType(benDetails.getBenVisitDetails().getBeneficiaryRegID()+""));

                String status = benDetails.getBenVisitDetails().getVisitFlowStatusFlag();
                if(status !=null && status.trim().equals("N")){
                    encounter.setStatus(Encounter.EncounterStatus.NULL);
                }

                List<CodeableConcept> listConcept = new ArrayList<>();
                CodeableConcept concept2 = new CodeableConcept();
                List<Coding> listCoding2 = new ArrayList<>();
                Coding coding2 = new Coding();
                coding2.setSystem("http://snomed.info/sct");
                coding2.setDisplay(benDetails.getBenVisitDetails().getVisitCategory());
                listCoding2.add(coding2);
                concept2.setCoding(listCoding2);
                listConcept.add(concept2);
                encounter.setType(listConcept);

                CodeableConcept concept = new CodeableConcept();
                List<Coding> listCoding = new ArrayList<>();
                Coding coding = new Coding();
                coding.setSystem("http://snomed.info/sct");
//            coding.setDisplay(benDetails.getVisitCategory());
                listCoding.add(coding);
                concept.setCoding(listCoding);
                encounter.setServiceType(concept);

                Coding coding3 = new Coding();
                coding3.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode");
                coding3.setCode("");
                coding3.setDisplay("");
                encounter.setClass_(coding3);

                List<CodeableConcept> listConcept2 = new ArrayList<>();
                CodeableConcept concept3 = new CodeableConcept();
                concept3.setText(benDetails.getBenVisitDetails().getVisitReason());
                listConcept2.add(concept3);
                encounter.setReasonCode(listConcept2);

                listRes.add(encounter);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return listRes;
    }
}