package digit.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import digit.config.ServiceConstants;
import digit.web.models.RequestSearchCriteria;
import digit.web.models.Service;
import digit.web.models.ServiceWrapper;
import digit.web.models.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import digit.repository.rowmapper.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.swing.tree.RowMapper;
import java.util.*;
import java.util.stream.Collectors;

import static digit.config.ServiceConstants.*;

@Repository
@Slf4j
public class ServiceRequestRepository {

    private ObjectMapper mapper;

    private RestTemplate restTemplate;

    private PGRRowMapper rowMapper;

    private PGRQueryBuilder queryBuilder;

    private JdbcTemplate jdbcTemplate;


    @Autowired
    public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }


    public Object fetchResult(StringBuilder uri, Object request) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), request, Map.class);
        }catch(HttpClientErrorException e) {
            log.error(EXTERNAL_SERVICE_EXCEPTION,e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        }catch(Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION,e);
        }

        return response;
    }

    public List<ServiceWrapper> getServiceWrappers(RequestSearchCriteria criteria){
        List<Service> services = getServices(criteria);
        List<String> serviceRequestids = services.stream().map(Service::getServiceRequestId).collect(Collectors.toList());
        Map<String, Workflow> idToWorkflowMap = new HashMap<>();
        List<ServiceWrapper> serviceWrappers = new ArrayList<>();

        for(Service service : services){
            ServiceWrapper serviceWrapper = ServiceWrapper.builder().service(service).workflow(idToWorkflowMap.get(service.getServiceRequestId())).build();
            serviceWrappers.add(serviceWrapper);
        }
        return serviceWrappers;
    }

    public List<Service> getServices(RequestSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = null;
        List<Service> services = Collections.emptyList();
        query = queryBuilder.getPGRSearchQuery(criteria, preparedStmtList);

        System.out.println("Generated Query: " + query);
        System.out.println("Parameters: " + preparedStmtList);

        try {
            services = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        }catch(Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION,e);
        }
        return services;
    }

    public Map<String, Integer> fetchDynamicData(String tenantId) {
        List<Object> preparedStmtListCompalintsResolved = new ArrayList<>();
        String query = queryBuilder.getResolvedComplaints(tenantId,preparedStmtListCompalintsResolved );

        int complaintsResolved = jdbcTemplate.queryForObject(query,preparedStmtListCompalintsResolved.toArray(),Integer.class);

        List<Object> preparedStmtListAverageResolutionTime = new ArrayList<>();
        query = queryBuilder.getAverageResolutionTime(tenantId, preparedStmtListAverageResolutionTime);

        int averageResolutionTime = jdbcTemplate.queryForObject(query, preparedStmtListAverageResolutionTime.toArray(),Integer.class);

        Map<String, Integer> dynamicData = new HashMap<String,Integer>();
        dynamicData.put(ServiceConstants.COMPLAINTS_RESOLVED, complaintsResolved);
        dynamicData.put(ServiceConstants.AVERAGE_RESOLUTION_TIME, averageResolutionTime);

        return dynamicData;
    }
}