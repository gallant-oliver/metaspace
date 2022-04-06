package org.apache.atlas.exception;

/**
 * @author huangrongwen
 * @Description: excel读取异常
 * @date 2022/4/610:48
 */
public class ReadExcelException extends RuntimeException{
    public ReadExcelException(String message) {
        super(message);
    }
}
