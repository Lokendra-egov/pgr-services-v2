package digit.enrichment;

import digit.config.Configuration;
import digit.util.MdmsUtil;
import digit.util.PGRUtil;
import digit.validator.RequestValidator;
import digit.web.models.*;
import org.egov.common.contract.idgen.IdResponse;
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
        RequestHeader requestInfo = serviceRequest.getRequestInfo();
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

        List<String> customIds = getIds(requestInfo.getRequestInfo(), tenantId, configs.getServiceRequestIdGenName(), configs.getServiceRequestIdGenFormat(), 1);

        service.setServiceRequestId(customIds.get(0));
    }

    public void enrichOnSearch(RequestInfo requestInfo, RequestSearchCriteria criteria) {

        // TODO: implement
    }

    private List<String> getIds(RequestInfo requestInfo, String tenantId, String idKey,
                                String idformat, int count) {
//        List<digit.web.models.Idgen.IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count).getIdResponses();

//        if (CollectionUtils.isEmpty(idResponses))
//            throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");
//
//        return idResponses.stream().map(idResponse -> idResponse.getId()).collect(Collectors.toList());

        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ids.add(UUID.randomUUID().toString());
        }
        return ids;
    }
}
