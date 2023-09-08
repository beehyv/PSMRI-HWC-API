package com.iemr.hwc.repo.registrar;

import com.iemr.hwc.data.registrar.FetchBeneficiaryDetails;
import com.iemr.hwc.data.registrar.UserBiometricsMapping;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RestResource(exported = false)
public interface UserBiometricsRepo extends CrudRepository<UserBiometricsMapping, Long> {
    @Query(" SELECT u FROM UserBiometricsMapping u WHERE u.userID = :userID AND u.deleted = false ")
    public UserBiometricsMapping getFingerprintsByUserID(@Param("userID") Long userID);
}