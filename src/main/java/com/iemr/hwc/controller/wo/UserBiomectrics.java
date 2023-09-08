package com.iemr.hwc.controller.wo;

import com.iemr.hwc.controller.common.master.CommonMasterController;
import com.iemr.hwc.data.registrar.FingerPrintDTO;
import com.iemr.hwc.service.location.LocationServiceImpl;
import com.iemr.hwc.service.registrar.RegistrarServiceImpl;
import com.iemr.hwc.utils.response.OutputResponse;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Autowired
    private RegistrarServiceImpl registrarService;
    @CrossOrigin()
    @ApiOperation(value = "add fingerprint for a given username", consumes = "application/json", produces = "application/json")
    @RequestMapping(value = "add/fingerprint/wo", method = { RequestMethod.POST }, produces = {
            "application/json" })
    public String addFingerPrints(@RequestBody FingerPrintDTO comingRequest) {
        OutputResponse response = new OutputResponse();
        try {
            if (comingRequest != null && comingRequest.getUserName() != null && !comingRequest.getFp().isEmpty()) {
                String resp = registrarService.saveFingerprints(comingRequest);
                if(resp !=null && resp.equals("ok")){
                    response.setResponse(resp);
                }
                else if(resp !=null && resp.equals("ko")){
                    response.setError(500, "Error adding fingerprints");
                }
                else if(resp !=null && resp.equals("bad_request")){
                    response.setError(500, "Invalid request");
                }
            } else {
                response.setError(5000, "Invalid request");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            response.setError(5000, "Error while adding fingerprints data");
        }
        return response.toString();
    }
}