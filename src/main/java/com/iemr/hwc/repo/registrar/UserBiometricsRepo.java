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

//	@Query(" SELECT d.beneficiaryRegID, d.beneficiaryID, d.firstName, d.lastName, d.gender, "
//			+ "Date(d.dob), d.maritalStatus, d.husbandName, d.income, d.educationQualification, d.occupation,  "
//			+ " d.blockID, d.blockName, d.stateID, d.stateName,"
//			+ " d.community, d.religion, d.fatherName, d.aadharNo, d.districtID, d.districtName, d.villageID,  "
//			+ " d.villageName, d.phoneNo, "
//			+ " d.govtIdentityTypeID, d.govtIdentityNo, d.isGovtID, Date(d.marrigeDate), d.literacyStatus, d.motherName, d.emailID, "
//			+ " d.bankName, d.branchName, "
//			+ " d.IFSCCode, d.accountNumber, d.benGovMapID from FetchBeneficiaryDetails d where d.beneficiaryRegID=:beneficiaryRegID")
//	public List<Object[]> getBeneficiaryDetails(@Param("beneficiaryRegID") Long beneficiaryRegID);
}