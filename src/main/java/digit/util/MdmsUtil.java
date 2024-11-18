package digit.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.web.models.RequestHeader;
import digit.web.models.ServiceRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static digit.config.ServiceConstants.*;

@Slf4j
@Component
public class MdmsUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Configuration configs;



    /**
     * Calls the MDMS service to fetch data based on the provided service request.
     *
     * @param  serviceRequest  the service request containing the necessary information
     * @return                 the response from the MDMS service
     */
    public Object mdmsCall(ServiceRequest serviceRequest) {
        RequestHeader requestInfo = serviceRequest.getRequestInfo();
        String tenantId = serviceRequest.getPgrEntity().getService().getTenantId();
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);
        Object response = new HashMap<>();
        Integer rate = 0;
        MdmsResponse mdmsResponse = new MdmsResponse();
        try {
            response = restTemplate.postForObject(configs.getMdmsHost() + configs.getMdmsEndPoint(), mdmsCriteriaReq, Map.class);
            mdmsResponse = mapper.convertValue(response, MdmsResponse.class);
        }catch(Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_MDMS,e);
            return response;
        }
        return mdmsResponse.getMdmsRes();
    }

    public MdmsCriteriaReq getMDMSRequest(RequestHeader requestInfo,String tenantId){
        List<ModuleDetail> pgrModuleRequest = getPGRModuleRequest();

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.addAll(pgrModuleRequest);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo.getRequestInfo()).build();
        return mdmsCriteriaReq;
    }

    private List<ModuleDetail> getPGRModuleRequest() {

        // master details for TL module
        List<MasterDetail> pgrMasterDetails = new ArrayList<>();

        // filter to only get code field from master data
        final String filterCode = "$.[?(@.active==true)]";

        pgrMasterDetails.add(MasterDetail.builder().name(MDMS_SERVICEDEF).filter(filterCode).build());

        ModuleDetail pgrModuleDtls = ModuleDetail.builder().masterDetails(pgrMasterDetails)
                .moduleName(MDMS_MODULE_NAME).build();


        return Collections.singletonList(pgrModuleDtls);

    }

    public StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(configs.getMdmsHost()).append(configs.getMdmsEndPoint());
    }
}