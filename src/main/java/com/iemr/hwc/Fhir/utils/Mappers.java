package com.iemr.hwc.Fhir.utils;

import ca.uhn.fhir.context.FhirContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class Mappers {

    private FhirContext fhirContext;

    @Autowired
    public Mappers(FhirContext fhirContext){
        this.fhirContext=fhirContext;
    }

    public String patientResourceToDbMapper(String patientData) throws JSONException {

        JSONObject resourceJson = new JSONObject(patientData);
        JSONObject mappedJson = new JSONObject();

        JSONArray phoneArr = new JSONArray();
        JSONObject phoneMaps=new JSONObject();

        JSONObject demographics = new JSONObject();

        JSONObject ids =new JSONObject();
        JSONArray identities = new JSONArray();


        mappedJson.put("firstName",resourceJson.getJSONArray("name").getJSONObject(0).getJSONArray("given").getString(0));
        mappedJson.put("lastName",resourceJson.getJSONArray("name").getJSONObject(0).getString("family"));

        mappedJson.put("genderName",resourceJson.getString("gender"));
        if (resourceJson.getString("gender").equalsIgnoreCase("male")){
            mappedJson.put("genderID",1);
        } else if (resourceJson.getString("gender").equalsIgnoreCase("female")) {
            mappedJson.put("genderID",2);
        }else {
            mappedJson.put("genderID",3);
        }

        String birth = resourceJson.getString("birthDate");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime dt = LocalDateTime.of(LocalDate.parse(birth), LocalTime.of(0,0,0));
        String formatted = formatter.format(dt);
        mappedJson.put("dOB",formatted);

        if(resourceJson.has("maritalStatus")) {
            mappedJson.put("maritalStatusName", resourceJson.getJSONObject("maritalStatus").getJSONArray("coding").getJSONObject(0).getString("display"));
            mappedJson.put("maritalStatusID", resourceJson.getJSONObject("maritalStatus").getJSONArray("coding").getJSONObject(0).getString("code"));
        }


        for(int i=0; i<resourceJson.getJSONArray("extension").length(); i++){
            JSONObject j = resourceJson.getJSONArray("extension").getJSONObject(i);
            String url = j.getString("url");
            String extDetails = url.substring(url.indexOf("#")+1);
            String[] arr = extDetails.split("\\.");
            String mapToObj = arr[1];
            String fieldKey = arr[2];

            if(mapToObj.equalsIgnoreCase("main"))
            {
                if (j.has("valueCoding")){
                    mappedJson.put(fieldKey,j.getJSONObject("valueCoding").getString("code"));
                }else{
                    mappedJson.put(fieldKey,j.getString("valueString"));
                }
            }
            else if (mapToObj.equalsIgnoreCase("demographics")) {
                if (j.has("valueCoding")){
                    demographics.put(fieldKey + "ID",j.getJSONObject("valueCoding").getString("code"));
                    demographics.put(fieldKey + "Name",j.getJSONObject("valueCoding").getString("display"));
                }
            }
            //@todo - Multiple Govt identities to be mapped into respective objects based on structure of extension
            else if (mapToObj.equalsIgnoreCase("identity")) {
                if (j.has("valueCoding")){
                    ids.put("govtIdentityTypeID",j.getJSONObject("valueCoding").getString("code"));
                    ids.put("govtIdentityTypeName",j.getJSONObject("valueCoding").getString("display"));
                }else{
                    ids.put("govtIdentityNo",j.getString("valueString"));
                }
            }
        }

        demographics.put("countryName","India");
        demographics.put("countryID",1);
        demographics.put("parkingPlaceID",mappedJson.getString("parkingPlaceID"));
        mappedJson.put("i_bendemographics",demographics);

        ids.put("createdBy",mappedJson.getString("createdBy"));
        identities.put(ids);
        mappedJson.put("beneficiaryIdentities",identities);

        String number = resourceJson.getJSONArray("telecom").getJSONObject(0).getString("value");
        phoneMaps.put("phoneNo",number);
        phoneMaps.put("parkingPlaceID",mappedJson.getString("parkingPlaceID"));
        phoneMaps.put("vanID",mappedJson.getString("vanID"));
        phoneMaps.put("createdBy",mappedJson.getString("createdBy"));
        phoneArr.put(phoneMaps);
        mappedJson.put("benPhoneMaps",phoneArr);
        mappedJson.put("providerServiceMapID",mappedJson.getString("providerServiceMapId"));



        return mappedJson.toString();
    }

}
