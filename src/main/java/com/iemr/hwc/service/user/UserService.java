package com.iemr.hwc.service.user;

import com.iemr.hwc.data.login.Users;
import com.iemr.hwc.data.login.UsersMasterVillage;

public interface UserService {
    public String setMasterVillage(Long userID, Integer villageID);
    public UsersMasterVillage getMasterVillage(Long userID);
}