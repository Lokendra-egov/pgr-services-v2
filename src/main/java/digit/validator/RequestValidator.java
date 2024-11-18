package digit.validator;

import digit.config.Configuration;
import digit.web.models.RequestSearchCriteria;
import digit.web.models.ServiceRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RequestValidator {

    private Configuration config;

    public void validateOnCreate(ServiceRequest request, Object mdmsResponse) {

    }

    public void validateOnSearch(RequestInfo requestInfo, RequestSearchCriteria criteria){

        /*
         * Checks if tenatId is provided with the search params
         * */
        if( (criteria.getMobileNumber()!=null
                || criteria.getServiceRequestId()!=null || criteria.getIds()!=null
                || criteria.getServiceCode()!=null )
                && criteria.getTenantId()==null)
            throw new CustomException("INVALID_SEARCH","TenantId is mandatory search param");

        validateSearchParam(requestInfo, criteria);
    }

    private void validateSearchParam(RequestInfo requestInfo, RequestSearchCriteria criteria){

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase("EMPLOYEE" ) && criteria.getIds().isEmpty())
            throw new CustomException("INVALID_SEARCH","Search without params is not allowed");

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase("EMPLOYEE") && criteria.getTenantId().split("\\.").length == 1){
            throw new CustomException("INVALID_SEARCH", "Employees cannot perform state level searches.");
        }

        String allowedParamStr = null;

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN" ))
            allowedParamStr = config.getAllowedCitizenSearchParameters();
        else if(requestInfo.getUserInfo().getType().equalsIgnoreCase("EMPLOYEE" ) || requestInfo.getUserInfo().getType().equalsIgnoreCase("SYSTEM") )
            allowedParamStr = config.getAllowedEmployeeSearchParameters();
        else throw new CustomException("INVALID SEARCH","The userType: "+requestInfo.getUserInfo().getType()+
                    " does not have any search config");

        List<String> allowedParams = Arrays.asList(allowedParamStr.split(","));

        if(criteria.getServiceCode()!=null && !allowedParams.contains("serviceCode"))
            throw new CustomException("INVALID SEARCH","Search on serviceCode is not allowed");

        if(criteria.getServiceRequestId()!=null && !allowedParams.contains("serviceRequestId"))
            throw new CustomException("INVALID SEARCH","Search on serviceRequestId is not allowed");

        if(criteria.getApplicationStatus()!=null && !allowedParams.contains("applicationStatus"))
            throw new CustomException("INVALID SEARCH","Search on applicationStatus is not allowed");

        if(criteria.getMobileNumber()!=null && !allowedParams.contains("mobileNumber"))
            throw new CustomException("INVALID SEARCH","Search on mobileNumber is not allowed");

        if(criteria.getIds()!=null && !allowedParams.contains("ids"))
            throw new CustomException("INVALID SEARCH","Search on ids is not allowed");

    }
}
