package com.iemr.hwc.controller.wo;

import com.google.gson.Gson;
import com.iemr.hwc.controller.common.master.CommonMasterController;
import com.iemr.hwc.data.login.UsersMasterVillage;
import com.iemr.hwc.service.user.UserServiceImpl;
import com.iemr.hwc.utils.response.OutputResponse;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(value = "wo/user")
public class UserMasterVillage {
    private Logger logger = LoggerFactory.getLogger(CommonMasterController.class);

    @Autowired
    private UserServiceImpl userService;

    @CrossOrigin()
    @ApiOperation(value = "set master village to a user", consumes = "application/json", produces = "application/json")
    @RequestMapping(value = "/set/mastervillage/{userID}/{villageID}/wo", method = { RequestMethod.GET }, produces = {
            "application/json" })
    public String setMasterVillage(@PathVariable("userID") Long userID, @PathVariable("villageID") Integer villageID) {
        OutputResponse response = new OutputResponse();
        try {
            if (userID != null && villageID != null) {
                String resp = userService.setMasterVillage(userID, villageID);
                if(resp !=null && resp.equals("ok")){
                    response.setResponse(resp);
                }
                else if(resp !=null && resp.equals("ko")){
                    response.setError(500, "Error setting master village");
                }
                else if(resp !=null && resp.equals("villageID_not_exist")){
                    response.setError(404, "Master village ID do not exist");
                }
                else if(resp !=null && resp.equals("userID_not_exist")){
                    response.setError(404, "User ID do not exist");
                }
                else if(resp !=null && resp.equals("already_have_master_village")){
                    response.setError(210, "User already have master village");
                }
            } else {
                response.setError(400, "Invalid request");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            response.setError(500, "Error while setting master village");
        }
        return response.toString();
    }

    @ApiOperation(value = "Get master village for a user", consumes = "application/json", produces = "application/json")
    @RequestMapping(value = "/get/mastervillage/{userID}/wo", method = RequestMethod.GET)
    public String getMasterVillage(@PathVariable("userID") Long userID) {
        logger.info("Get master village by userID ..." + userID);
        OutputResponse response = new OutputResponse();
        UsersMasterVillage user = userService.getMasterVillage(userID);
        if (user != null){
            if(user.getMasterVillage()!=null){
                Gson gson = new Gson();
                response.setResponse(gson.toJson(user.getMasterVillage()));
            }
            else{
                response.setError(404, "User with userID: "+userID+" do not have master village");
            }
        }
        else{
            response.setError(404, "User with userID: "+userID+" not found");
        }
        logger.info("Get master village " + response.toString());
        return response.toString();
    }
}