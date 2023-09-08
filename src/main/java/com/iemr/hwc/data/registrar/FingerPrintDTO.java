package com.iemr.hwc.data.registrar;

import lombok.Data;

import java.util.List;

@Data
public class FingerPrintDTO {
    private Integer id;
    private String userName;
    private List<FingerPrint> fp;
}