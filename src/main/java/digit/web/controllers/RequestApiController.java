package digit.web.controllers;


import digit.service.PGRService;
import digit.util.ResponseInfoFactory;
import digit.web.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-11-18T11:13:37.148087853+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("/request/v2")
public class RequestApiController {

    @Autowired
    private PGRService pgrService;

    private ResponseInfoFactory responseInfoFactory = new ResponseInfoFactory();

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<ServiceWrapper> requestCreatePost(@Valid @RequestBody ServiceRequest serviceRequest) {
        try {
            ServiceRequest enrichedBody = pgrService.create(serviceRequest);
            ServiceWrapper serviceWrapper = ServiceWrapper.builder().service(enrichedBody.getPgrEntity().getService()).workflow(enrichedBody.getPgrEntity().getWorkflow()).build();
            return new ResponseEntity<ServiceWrapper>(serviceWrapper, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<ServiceWrapper>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value="/_search", method = RequestMethod.POST)
    public ResponseEntity<ServiceResponse> requestsSearchPost(@javax.validation.Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                              @javax.validation.Valid @ModelAttribute RequestSearchCriteria criteria) {

        List<ServiceWrapper> serviceWrappers = pgrService.search(requestInfoWrapper.getRequestInfo(), criteria);

        // Build ServiceResponse
        ServiceResponse serviceResponse = ServiceResponse.builder()
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .pgREntities(serviceWrappers)
                .build();
        // Return ResponseEntity
        return ResponseEntity.ok(serviceResponse);
    }

    @RequestMapping(value="/_update", method = RequestMethod.POST)
    public ResponseEntity<ServiceWrapper> requestsUpdatePost(@Valid @RequestBody ServiceRequest serviceRequest) {
        ServiceRequest enrichedReq = pgrService.update(serviceRequest);
//        ServiceWrapper serviceWrapper = ServiceWrapper.builder().service(enrichedReq.getService()).workflow(enrichedReq.getWorkflow()).build();
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(serviceRequest.getRequestInfo(), true);

        ServiceWrapper response = ServiceWrapper.builder().service(enrichedReq.getPgrEntity().getService()).workflow(enrichedReq.getPgrEntity().getWorkflow()).build();
        response.setResponseInfo(responseInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
