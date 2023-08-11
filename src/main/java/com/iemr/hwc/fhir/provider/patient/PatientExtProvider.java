package com.iemr.hwc.fhir.provider.patient;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.iemr.hwc.fhir.dto.beneficiary.BeneficiariesDTOSearch;
import com.iemr.hwc.fhir.dto.beneficiary.BeneficiaryDTO;
import com.iemr.hwc.fhir.model.patient.PatientExt;
import com.iemr.hwc.fhir.utils.mapper.MapperUtils;
import com.iemr.hwc.fhir.utils.validation.Validations;
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
}