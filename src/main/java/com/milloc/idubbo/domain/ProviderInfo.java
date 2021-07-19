package com.milloc.idubbo.domain;

import lombok.Data;

import java.util.List;

/**
 * ProviderInfo
 *
 * @author gongdeming
 * @date 2021-07-15
 */
@Data
public class ProviderInfo {
    private ProviderId providerId;
    private List<String> services;
}
