package com.iemr.hwc.fhir.model.familyMemberHistory;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.r4.model.FamilyMemberHistory;
import org.hl7.fhir.r4.model.StringType;

@ResourceDef(name = "FamilyMemberHistory")
public class FamilyMemberHistoryExt extends FamilyMemberHistory {

    @Description(shortDefinition = "Contains providerServiceMapId ")
    @Extension(url = "http://hl7.org/fhir/StructureDefinition/FamilyMemberHistory#FamilyMemberHistory.providerServiceMapId", isModifier = false, definedLocally = true)
    @Child(name = "providerServiceMapId")
    private StringType providerServiceMapId;

    @Description(shortDefinition = "Contains vanID ")
    @Extension(url = "http://hl7.org/fhir/StructureDefinition/FamilyMemberHistory#FamilyMemberHistory.vanID", isModifier = false, definedLocally = true)
    @Child(name = "vanID")
    private StringType vanID;

    @Description(shortDefinition = "Contains parkingPlaceID ")
    @Extension(url = "http://hl7.org/fhir/StructureDefinition/FamilyMemberHistory#FamilyMemberHistory.parkingPlaceID", isModifier = false, definedLocally = true)
    @Child(name = "parkingPlaceID")
    private StringType parkingPlaceID;

    @Description(shortDefinition = "Contains createdBy ")
    @Extension(url = "http://hl7.org/fhir/StructureDefinition/FamilyMemberHistory#FamilyMemberHistory.createdBy", isModifier = false, definedLocally = true)
    @Child(name = "createdBy")
    private StringType createdBy;

    @Description(shortDefinition = "Contains benFlowID ")
    @Extension(url = "http://hl7.org/fhir/StructureDefinition/FamilyMemberHistory#FamilyMemberHistory.benFlowID", isModifier = false, definedLocally = true)
    @Child(name = "benFlowID")
    private StringType benFlowID;

    public StringType getProviderServiceMapId() {
        if (providerServiceMapId == null) {
            providerServiceMapId = new StringType();
        }
        return providerServiceMapId;
    }

    public void setProviderServiceMapId(StringType providerService_MapId) {
        providerServiceMapId =  providerService_MapId;
    }

    public StringType getVanID() {
        if (vanID == null) {
            vanID = new StringType();
        }
        return vanID;
    }

    public void setVanID(StringType van_ID) {
        vanID =  van_ID;
    }

    public StringType getParkingPlaceID() {
        if (parkingPlaceID == null) {
            parkingPlaceID = new StringType();
        }
        return parkingPlaceID;
    }

    public void setParkingPlaceID(StringType parking_PlaceID) {
        parkingPlaceID =  parking_PlaceID;
    }

    public StringType getCreatedBy() {
        if (createdBy == null) {
            createdBy = new StringType();
        }
        return createdBy;
    }

    public void setCreatedBy(StringType created_By) {
        createdBy =  created_By;
    }

    public StringType getBenFlowID() {
        if (benFlowID == null) {
            benFlowID = new StringType();
        }
        return benFlowID;
    }

    public void setBenFlowID(StringType benFlow_ID) {
        benFlowID =  benFlow_ID;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && ElementUtil.isEmpty(benFlowID , providerServiceMapId ,
                vanID , parkingPlaceID , createdBy);
    }

}
