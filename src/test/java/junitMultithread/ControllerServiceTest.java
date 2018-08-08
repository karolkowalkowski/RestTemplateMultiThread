package junitMultithread;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

@RunWith(ConcurrentTestRunner.class)
public class ControllerServiceTest {

    private static final String INVOICES_SERVICE_PATH = "http://localhost:8088/accounts";
    private final static int THREAD_COUNT = 8; //set how much threads you want to start

    private ObjectMapper objectMapper = new ObjectMapper();
    private HttpHeaders headers = new HttpHeaders();
    private RestTemplate restTemplate = new RestTemplate();


    @Before
    public void initialization(){
        headers.setContentType(MediaType.APPLICATION_JSON);
    }


    @Test
    @ThreadCount(THREAD_COUNT)
    public void shoulAddCheckAndDeleteAccountTest() throws Exception {
        Account tempAccount = Account.builder()
                .id(666666L)
                .name(UUID.randomUUID().toString())
                .balance(BigDecimal.valueOf(1000))
                .build();
        HttpEntity<String> entity = new HttpEntity<String>((json(tempAccount)), headers);
        ResponseEntity<String> answer = restTemplate.exchange(INVOICES_SERVICE_PATH, HttpMethod.POST, entity, String.class);
        Assert.assertEquals(200, answer.getStatusCodeValue());
        String accountId = answer.getBody();
        entity = new HttpEntity<String>("",headers);
        answer = restTemplate.exchange(INVOICES_SERVICE_PATH + "/" + accountId, HttpMethod.GET, entity, String.class);
        Assert.assertEquals(200, answer.getStatusCodeValue());
        answer = restTemplate.exchange(INVOICES_SERVICE_PATH + "/" + accountId, HttpMethod.DELETE, entity, String.class);
        Assert.assertEquals(200, answer.getStatusCodeValue());
        Assert.assertEquals(null, answer.getBody());
    }

    @Test
    @ThreadCount(THREAD_COUNT)
    public void shoulAddUpdateAndDeleteAccountTest() throws Exception {
        Account tempAccount = Account.builder()
                .id(666666L)
                .name(UUID.randomUUID().toString())
                .balance(BigDecimal.valueOf(1000))
                .build();
        HttpEntity<String> entity = new HttpEntity<String>((json(tempAccount)), headers);
        ResponseEntity<String> answer = restTemplate.exchange(INVOICES_SERVICE_PATH, HttpMethod.POST, entity, String.class);
        assertEquals(200, answer.getStatusCodeValue());
        String accountId = answer.getBody();

        entity = new HttpEntity<String>("",headers);
        answer = restTemplate.exchange(INVOICES_SERVICE_PATH + "/" + accountId, HttpMethod.GET, entity, String.class);
        assertEquals(200, answer.getStatusCodeValue());

        tempAccount.setId(Long.parseLong(accountId));
        tempAccount.setBalance(BigDecimal.valueOf(2000));

        entity = new HttpEntity<String>(json(tempAccount),headers);
        answer = restTemplate.exchange(INVOICES_SERVICE_PATH + "/" + accountId, HttpMethod.PUT, entity, String.class);
        assertEquals(200, answer.getStatusCodeValue());

        entity = new HttpEntity<String>("",headers);
        answer = restTemplate.exchange(INVOICES_SERVICE_PATH + "/" + accountId, HttpMethod.GET, entity, String.class);
        Assert.assertEquals(200, answer.getStatusCodeValue());
        tempAccount = toObject(answer.getBody());
        assertEquals(2000,tempAccount.getBalance().intValue());

        answer = restTemplate.exchange(INVOICES_SERVICE_PATH + "/" + accountId, HttpMethod.DELETE, entity, String.class);
        Assert.assertEquals(200, answer.getStatusCodeValue());
        Assert.assertEquals(null, answer.getBody());
    }

    @After
    public void afterCheck(){
        HttpEntity<String> entity = new HttpEntity<String>("",headers);
        ResponseEntity<String> answer = restTemplate.exchange(INVOICES_SERVICE_PATH, HttpMethod.GET, entity, String.class);
        Assert.assertEquals(200, answer.getStatusCodeValue());
        Assert.assertEquals("[]",answer.getBody());
    }

    private Account toObject(String json) throws Exception{
        return objectMapper.readValue(json, Account.class);
    }

    private String json(Account account) throws Exception {
        return objectMapper.writeValueAsString(account);
    }

}