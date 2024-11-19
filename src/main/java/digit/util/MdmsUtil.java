package digit.util;

import digit.config.Configuration;
import digit.repository.ServiceRequestRepository;
import digit.web.models.ServiceRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static digit.config.ServiceConstants.*;

@Slf4j
@Component
public class MdmsUtil {

    @Autowired
    private Configuration configs;

    @Autowired
    private ServiceRequestRepository repository;


    @Autowired
    public MdmsUtil(Configuration configs, ServiceRequestRepository repository) {
        this.configs = configs;
        this.repository = repository;
    }



    /**
     * Calls the MDMS service to fetch data based on the provided service request.
     *
     * @param  serviceRequest  the service request containing the necessary information
     * @return                 the response from the MDMS service
     */
    public Object mdmsCall(ServiceRequest serviceRequest) {

        // get request info
        RequestInfo requestInfo = serviceRequest.getRequestInfo();

        // get tenant id
        String tenantId = serviceRequest.getPgrEntity().getService().getTenantId();

        // get mdms search criteria
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);


        Object mdmsResponse = repository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);

        return mdmsResponse;
    }

    /**
     * Retrieves the MDMS request based on the provided request info and tenant ID.
     *
     * @param  requestInfo  the request header containing necessary information
     * @param  tenantId     the ID of the tenant
     * @return              the MDMS criteria request generated from the module details and tenant ID
     */
    public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId){
        List<ModuleDetail> pgrModuleRequest = getPGRModuleRequest();

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.addAll(pgrModuleRequest);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        return mdmsCriteriaReq;
    }

    /**
     * Retrieves the PGR module request details.
     *
     * @return         a list containing the PGR module details
     */
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

    /**
     * Retrieves the MDMS search URL by appending the MDMS host and endpoint.
     *
     * @return         the MDMS search URL as a StringBuilder
     */
    public StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(configs.getMdmsHost()).append(configs.getMdmsEndPoint());
    }
}