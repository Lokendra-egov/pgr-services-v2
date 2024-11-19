package digit.enrichment;

import digit.config.Configuration;
import digit.util.PGRUtil;
import digit.validator.RequestValidator;
import digit.web.models.*;
import jakarta.validation.Valid;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import digit.service.UserService;
import digit.repository.IdGenRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static digit.config.ServiceConstants.USERTYPE_CITIZEN;

@Component
public class EnrichmentService {

    private UserService userService;

    private RequestValidator validator;

    private PGRUtil utils;

    private Configuration configs;

    private IdGenRepository idGenRepository;

    public EnrichmentService(UserService userService, RequestValidator validator, PGRUtil utils, Configuration configs, IdGenRepository idGenRepository) {
        this.configs = configs;
        this.idGenRepository = idGenRepository;
        this.userService = userService;
        this.validator = validator;
        this.utils = utils;
    }

    public void enrichOnCreate(ServiceRequest serviceRequest) {
        RequestInfo requestInfo = serviceRequest.getRequestInfo();
        Service service = serviceRequest.getPgrEntity().getService();
        Workflow workflow = serviceRequest.getPgrEntity().getWorkflow();
        String tenantId = service.getTenantId();

        // Enrich accountId of the logged in citizen
        serviceRequest.getPgrEntity().getService().setAccountId(requestInfo.getUserInfo().getUuid());

//        userService.callUserService(serviceRequest);


        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(), service, true);

        service.setAuditDetails(auditDetails);
        service.setId(UUID.randomUUID().toString());
        service.getAddress().setId(UUID.randomUUID().toString());
        service.getAddress().setTenantId(tenantId);
//        service.setActive(true);

//        if (workflow.getVerificationDocuments() != null) {
//            workflow.getVerificationDocuments().forEach(document -> {
//                document.setId(UUID.randomUUID().toString());
//            });
//        }

        if (StringUtils.isEmpty(service.getAccountId()))
            service.setAccountId(service.getCitizen().getUuid());

        List<String> customIds = getIds(requestInfo, tenantId, configs.getServiceRequestIdGenName(), configs.getServiceRequestIdGenFormat(), 1);

        service.setServiceRequestId(customIds.get(0));
    }

    public void enrichOnSearch(RequestInfo requestInfo, RequestSearchCriteria criteria) {

        if(criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN)){
            String citizenMobileNumber = requestInfo.getUserInfo().getUserName();
            criteria.setMobileNumber(citizenMobileNumber);
        }

        criteria.setAccountId(requestInfo.getUserInfo().getUuid());

        String tenantId = (criteria.getTenantId()!=null) ? criteria.getTenantId() : requestInfo.getUserInfo().getTenantId();

        if(criteria.getMobileNumber()!=null){
            userService.enrichUserIds(tenantId, criteria);
        }

        if(criteria.getLimit()==null)
            criteria.setLimit(configs.getDefaultLimit());

        if(criteria.getOffset()==null)
            criteria.setOffset(configs.getDefaultOffset());

        if(criteria.getLimit()!=null && criteria.getLimit() > configs.getMaxLimit())
            criteria.setLimit(configs.getMaxLimit());

    }

    private List<String> getIds(@Valid RequestInfo requestHeader, String tenantId, String idKey,
                                String idformat, int count) {
        List<digit.web.models.Idgen.IdResponse> idResponses = idGenRepository.getId(RequestInfo.builder().build(), tenantId, idKey, idformat, count).getIdResponses();

        if (CollectionUtils.isEmpty(idResponses))
            throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

        return idResponses.stream().map(idResponse -> idResponse.getId()).collect(Collectors.toList());

    }

    public void enrichOnUpdate(ServiceRequest serviceRequest) {

        RequestInfo requestInfo = serviceRequest.getRequestInfo();
        Service service = serviceRequest.getPgrEntity().getService();
        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(), service,false);

        service.setAuditDetails(auditDetails);

//        userService.callUserService(serviceRequest);
    }
}
