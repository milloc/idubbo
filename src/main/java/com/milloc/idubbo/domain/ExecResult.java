package com.milloc.idubbo.domain;

import lombok.Data;

/**
 * ExecResult
 *
 * @author gongdeming
 * @date 2021-07-15
 */
@Data
public class ExecResult {
    private String result;
    private boolean ok;
    private String msg;
}
