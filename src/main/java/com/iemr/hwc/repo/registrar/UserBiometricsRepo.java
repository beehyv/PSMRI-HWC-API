package com.iemr.hwc.repo.registrar;

import com.iemr.hwc.data.registrar.UserBiometricsMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;


@Repository
@RestResource(exported = false)
public interface UserBiometricsRepo extends CrudRepository<UserBiometricsMapping, Long> {

}