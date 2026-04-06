package com.badminton.bmp.modules.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 经纬度逆地理编码结果（供小程序填地址）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReverseGeocodeVO {
    private String address;
    /** 附近地标/商圈标题，可选 */
    private String title;
    private double latitude;
    private double longitude;
}
