package com.iemr.hwc.data.registrar;

import lombok.Data;

@Data
public class FingerPrintDTO {
    private Integer id;
    private String userName;
    private String fpVal;
    private String fingerType;
}
