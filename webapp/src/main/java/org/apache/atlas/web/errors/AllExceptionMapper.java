/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.web.errors;

import org.apache.atlas.type.AtlasType;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.atlas.AtlasErrorCode.URL_NOT_FOUND;

/**
 * Exception mapper for Jersey.
 *
 * @param <E>
 */
@Provider
@Component
public class AllExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        final long id = ThreadLocalRandom.current().nextLong();

        // Log the response and use the error codes from the Exception
        ExceptionMapperUtil.logException(id, exception);
        return buildExceptionResponse(exception);
    }

    protected Response buildExceptionResponse(Exception exception) {

        Map<String, String> errorJsonMap = new LinkedHashMap<>();
        errorJsonMap.put("errorCode", URL_NOT_FOUND.getErrorCode());
        String errorMessage = exception.getMessage();
        if (StringUtils.isNotEmpty(errorMessage) && errorMessage.contains("null for")) {
            errorMessage = "not found" + errorMessage.substring(8);
        }
        errorJsonMap.put("errorMessage", errorMessage);

        if (exception.getCause() != null) {
            errorJsonMap.put("errorCause", exception.getCause().getMessage());
        }
        Response.ResponseBuilder responseBuilder = Response.status(400);
        responseBuilder.entity(AtlasType.toJson(errorJsonMap)).type(Servlets.JSON_MEDIA_TYPE);
        return responseBuilder.build();
    }

}
