package com.iemr.hwc.service.CHOApp;

import com.google.gson.JsonObject;
import com.iemr.hwc.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;
import javax.ws.rs.core.MediaType;

@Service
@PropertySource("classpath:application.properties")
public class CHOAppSyncServiceImpl implements CHOAppSyncService {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Value("${registrationUrl}")
    private String registrationUrl;

    private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;

    @Autowired
    public void setCommonBenStatusFlowServiceImpl(CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl) {
        this.commonBenStatusFlowServiceImpl = commonBenStatusFlowServiceImpl;
    }

    public ResponseEntity<String> registerCHOAPPBeneficiary(String comingRequest, String Authorization){

        JsonObject responseObj = new JsonObject();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Long beneficiaryRegID = null;
        Long beneficiaryID = null;
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", MediaType.APPLICATION_JSON + ";charset=utf-8");
        headers.add("AUTHORIZATION", Authorization);

        HttpEntity<Object> registrationRequest = new HttpEntity<Object>(comingRequest, headers);

        try {
            ResponseEntity<String> registrationResponse = restTemplate.exchange(registrationUrl, HttpMethod.POST, registrationRequest,
                    String.class);

                String registrationResponseStr = registrationResponse.getBody();
                JSONObject registrationResponseObj = new JSONObject(registrationResponseStr);

                if (registrationResponseObj.getInt("statusCode") == 200) {

                    beneficiaryRegID = registrationResponseObj.getJSONObject("data").getLong("beneficiaryRegID");
                    beneficiaryID = registrationResponseObj.getJSONObject("data").getLong("beneficiaryID");

                    int i = commonBenStatusFlowServiceImpl.createBenFlowRecord(comingRequest, beneficiaryRegID, beneficiaryID);

                    if (i > 0) {
                        if (i == 1) {
                            responseObj.addProperty("beneficiaryID", beneficiaryID);
                            responseObj.addProperty("beneficiaryRegID", beneficiaryRegID);
                            status = HttpStatus.OK;
                        }
                    } else {
                        logger.error("Couldn't create a new benFlowStatus record for the registered beneficiary");
                        responseObj.addProperty("error", "Beneficiary creation successful but couldn't create new flow status for it.");
                        status = HttpStatus.INTERNAL_SERVER_ERROR;
                    }

                } else {
                    logger.error("Error encountered in Common-API service while registering beneficiary. "
                            + registrationResponseObj.getString("status"));
                    responseObj.addProperty("error", "Error encountered in Common-API service while registering beneficiary. "
                            + registrationResponseObj.getString("status"));
                }

        } catch(ResourceAccessException e){
            logger.error("Error establishing connection with Common-API service. " + e);
            responseObj.addProperty("error", "Error establishing connection with Common-API service. ");
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } catch(RestClientResponseException e){
            logger.error("Error encountered in Common-API service while registering beneficiary. " + e);
            responseObj.addProperty("error", "Error encountered in Common-API service while registering beneficiary. " + e);
            status = HttpStatus.valueOf(e.getRawStatusCode());
        } catch (JSONException e){
            logger.error("Encountered JSON exception " + e);
            responseObj.addProperty("error", "Error registering the beneficiary.Encountered JSON exception " + e);
            status = HttpStatus.BAD_GATEWAY;
        } catch (Exception e){
            logger.error("Encountered exception " + e);
            responseObj.addProperty("error", "Error registering the beneficiary.Encountered exception " + e);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        headers.remove("AUTHORIZATION");

        return new ResponseEntity<> (responseObj.toString(),headers,status);
    }
}
