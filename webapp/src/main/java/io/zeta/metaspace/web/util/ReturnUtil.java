// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.web.util;

import com.sun.org.apache.regexp.internal.RE;
import io.zeta.metaspace.model.Result;
import org.apache.atlas.exception.AtlasBaseException;

/**
 * @author lixiang03
 * @Data 2020/2/24 11:24
 */
public class ReturnUtil {
    /**
     * success
     *
     * @return
     */
    public static Result success() {
        Result result = new Result();
        result.setCode(Status.SUCCESS.getCode());
        result.setMessage(Status.SUCCESS.getMessage());

        return result;
    }

    public static Boolean isSuccess(Result result) {
       return Status.SUCCESS.getCode().equals(result.getCode());
    }

    /**
     * success does not need to return data
     *
     * @param message
     * @return
     */
    public static Result success(String message) {
        Result result = new Result();
        result.setCode(Status.SUCCESS.getCode());
        result.setMessage(message);

        return result;
    }

    /**
     * return data no paging
     *
     * @param message
     * @param data
     * @return
     */
    public static Result success(String message, Object data) {
        Result result = getResult(message, data);
        return result;
    }

    /**
     *
     * @param object
     * @return
     */
    public static Result success(Object object) {
        Result result = getResult(Status.SUCCESS.getMessage(), object);
        return result;
    }


    /**
     * error handle
     *
     * @param code
     * @param message
     * @return
     */
    public static Result error(String code, String message) {
        return new Result(code, message);
    }

    public static Result error(String code, String message, String detail) {
        return new Result(code, message, detail, null);
    }

    public static Result error(String code, String message, Exception e) {
        return error(code, message, e.getMessage());
    }

    public static Result error(AtlasBaseException e) {
        return error(e.getAtlasErrorCode().getErrorCode(), e.getAtlasErrorCode().getFormattedErrorMessage(), e.getMessage());
    }


    /**
     * get result
     *
     * @param message
     * @param data
     * @return
     */
    private static Result getResult(String message, Object data) {
        Result result = new Result();
        result.setCode(Status.SUCCESS.getCode());
        result.setMessage(message);

        result.setData(data);
        return result;
    }

    enum Status {

        SUCCESS("200", "success");

        private String code;
        private String message;

        private Status(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return this.code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
