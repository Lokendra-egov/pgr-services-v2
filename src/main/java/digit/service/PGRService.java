package digit.service;

import digit.config.Configuration;
import digit.web.models.RequestHeader;
import digit.web.models.RequestSearchCriteria;
import digit.web.models.ServiceRequest;
import digit.validator.RequestValidator;
import digit.enrichment.EnrichmentService;
import digit.util.MdmsUtil;
import digit.kafka.*;
import digit.web.models.ServiceWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import digit.repository.ServiceRequestRepository;

import java.util.*;

@Component
public class PGRService {

    private RequestValidator validator;

    private EnrichmentService enrichment;

    private MdmsUtil mdmsUtils;

    private WorkflowService workflowService;

    private Producer producer;

    private Configuration config;

    private ServiceRequestRepository repository;

    public PGRService(RequestValidator validator, EnrichmentService enrichment, MdmsUtil mdmsUtils, WorkflowService workflowService, Producer producer, Configuration config) {
        this.validator = validator;
        this.enrichment = enrichment;
        this.mdmsUtils = mdmsUtils;
        this.workflowService = workflowService;
        this.producer = producer;
        this.config = config;

    }

    public ServiceRequest create(ServiceRequest serviceRequest) {
//        Object mdmsResponse = mdmsUtils.mdmsCall(serviceRequest);
        Object mdmsResponse = null;
        validator.validateOnCreate(serviceRequest, mdmsResponse);
        enrichment.enrichOnCreate(serviceRequest);
//        workflowService.updateWorkflowStatus(serviceRequest);
        producer.push(config.getCreateTopic(),serviceRequest.getPgrEntity());
        return serviceRequest;
    }

    public List<ServiceWrapper> search(RequestInfo requestInfo, RequestSearchCriteria criteria){
        validator.validateOnSearch(requestInfo, criteria);

        enrichment.enrichOnSearch(requestInfo, criteria);

        if(criteria.getIds().isEmpty() && criteria.getMobileNumber()==null && criteria.getServiceRequestId()==null)
            return new ArrayList<>();

//        criteria.setIsPlainSearch(false);

        List<ServiceWrapper> serviceWrappers = repository.getServiceWrappers(criteria);

        if(CollectionUtils.isEmpty(serviceWrappers))
            return new ArrayList<>();;

//        userService.enrichUsers(serviceWrappers);
        List<ServiceWrapper> enrichedServiceWrappers = workflowService.enrichWorkflow(requestInfo,serviceWrappers);
        Map<Long, List<ServiceWrapper>> sortedWrappers = new TreeMap<>(Collections.reverseOrder());
        for(ServiceWrapper svc : enrichedServiceWrappers){
            if(sortedWrappers.containsKey(svc.getService().getAuditDetails().getCreatedTime())){
                sortedWrappers.get(svc.getService().getAuditDetails().getCreatedTime()).add(svc);
            }else{
                List<ServiceWrapper> serviceWrapperList = new ArrayList<>();
                serviceWrapperList.add(svc);
                sortedWrappers.put(svc.getService().getAuditDetails().getCreatedTime(), serviceWrapperList);
            }
        }
        List<ServiceWrapper> sortedServiceWrappers = new ArrayList<>();
        for(Long createdTimeDesc : sortedWrappers.keySet()){
            sortedServiceWrappers.addAll(sortedWrappers.get(createdTimeDesc));
        }
        return sortedServiceWrappers;
    }

    public Map<String, Integer> getDynamicData(String tenantId) {

        Map<String,Integer> dynamicData = repository.fetchDynamicData(tenantId);

        return dynamicData;
    }
}