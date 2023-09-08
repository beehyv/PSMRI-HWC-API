package com.iemr.hwc.controller.wo;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.iemr.hwc.controller.common.master.CommonMasterController;
import com.iemr.hwc.data.registrar.FingerPrintDTO;
import com.iemr.hwc.fhir.dto.beneficiary.identityDTO.BeneficiariesDTOSearch;
import com.iemr.hwc.service.location.LocationServiceImpl;
import com.iemr.hwc.service.registrar.RegistrarServiceImpl;
import com.iemr.hwc.utils.response.OutputResponse;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
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
    @ApiOperation(value = "Get location details from service point id and provider service map id", consumes = "application/json", produces = "application/json")
    @RequestMapping(value = "add/fingerprint/wo", method = { RequestMethod.POST }, produces = {
            "application/json" })
    public String addFingerPrints(@RequestBody List<FingerPrintDTO> listFingerPrints) {
        OutputResponse response = new OutputResponse();
        try {
//            System.out.println("Comming request "+listFingerPrints);
//            Type typeOfSrc = new TypeToken<List<FingerPrintDTO>>() {}.getType();
//            Gson gson = new Gson();
//            List<FingerPrintDTO> listFingerPrints = gson.fromJson(comingRequest.toString(), typeOfSrc);
            if (listFingerPrints != null && !listFingerPrints.isEmpty()) {
                String resp = registrarService.saveFingerprints(listFingerPrints);
                if(resp !=null && resp.equals("ok")){
                    response.setResponse(resp);
                }
                else if(resp !=null && resp.equals("ko")){
                    response.setError(500, "Error adding fingerprints");
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