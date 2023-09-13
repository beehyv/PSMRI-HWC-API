/*
 * AMRIT – Accessible Medical Records via Integrated Technology
 * Integrated EHR (Electronic Health Records) Solution
 *
 * Copyright (C) "Piramal Swasthya Management and Research Institute"
 *
 * This file is part of AMRIT.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package com.iemr.hwc.service.user;

import com.iemr.hwc.data.location.*;
import com.iemr.hwc.data.login.Users;
import com.iemr.hwc.repo.location.*;
import com.iemr.hwc.repo.login.UserLoginRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserLoginRepo userLoginRepo;

    @Autowired
    private DistrictBranchMasterRepo districtBranchMasterRepo;

    public String setMasterVillage(Long userID, Integer villageID){
        String response = "";
        Users user = userLoginRepo.getUserByUserID(userID);
        if(user!=null){
            if(user.getActive()==false) {
                DistrictBranchMapping districtBranchMapping = districtBranchMasterRepo.findByDistrictBranchID(villageID);
                if(districtBranchMapping!=null){
                    user.setMasterVillage(districtBranchMapping);
                    user.setActive(true);
                    user = userLoginRepo.save(user);
                    if(user!=null){
                        response = "ok";
                    }
                    else{
                        response = "ko";
                    }
                }
                else{
                    response = "villageID_not_exist";
                }
            }
            else{
                response = "already_have_master_village";
            }
        }
        else{
            response = "userID_not_exist";
        }

        return response;
    }

    public Users getMasterVillage(Long userID){
        Users response = null;
        Users user = userLoginRepo.getUserByUserID(userID);
        if(user!=null){
            return user;
        }

        return response;
    }
}