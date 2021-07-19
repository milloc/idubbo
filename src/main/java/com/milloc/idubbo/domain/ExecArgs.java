package com.milloc.idubbo.domain;

import lombok.Data;

/**
 * Exec
 *
 * @author gongdeming
 * @date 2021-07-15
 */
@Data
public class ExecArgs {
    private String serviceName;
    private String methodName;
    private String[] args;
}
