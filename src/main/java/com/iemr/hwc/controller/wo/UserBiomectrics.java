package com.iemr.hwc.controller.wo;

import com.iemr.hwc.controller.common.master.CommonMasterController;
import com.iemr.hwc.data.registrar.FingerPrintDTO;
import com.iemr.hwc.service.location.LocationServiceImpl;
import com.iemr.hwc.utils.response.OutputResponse;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(value = "wo/user_biometrics")
public class UserBiomectrics {
    //    private OutputResponse response;
    private Logger logger = LoggerFactory.getLogger(CommonMasterController.class);

    private LocationServiceImpl locationServiceImpl;

    @Autowired
    public void setLocationServiceImpl(LocationServiceImpl locationServiceImpl) {
        this.locationServiceImpl = locationServiceImpl;
    }


    @CrossOrigin()
    @ApiOperation(value = "Get location details from service point id and provider service map id", consumes = "application/json", produces = "application/json")
    @RequestMapping(value = "add/fingerprint/wo", method = { RequestMethod.POST }, produces = {
            "application/json" })
    public String addFingerPrints(@RequestBody FingerPrintDTO comingRequest) {
        OutputResponse response = new OutputResponse();
        try {
//            System.out.println("Comming request "+comingRequest);
            if (comingRequest != null && comingRequest.getId()!=null && comingRequest.getUserName()!=null && comingRequest.getFpVal() != null
                    && comingRequest.getFingerType() != null) {
                String s = locationServiceImpl.saveFingerprints(comingRequest);
//                System.out.println("Response is "+s);
                response.setResponse(s);
            } else {
                response.setError(5000, "Invalid request");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            response.setError(5000, "Error while getting location data");
        }
        return response.toString();
    }
}