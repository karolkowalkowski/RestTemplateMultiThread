package junitMultithread;

import static junitMultithread.MessagesProvider.EMPTY_ACCOUNT_NAME;
import static junitMultithread.TestAccountProvider.ACCOUNT_JACEK_BALANCE_1000;
import static junitMultithread.MessagesProvider.EMPTY_ACCOUNT_BALANCE;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.management.InstanceNotFoundException;

@RunWith(ConcurrentTestRunner.class)
public class ControllerServiceTest {

    private static final String INVOICES_SERVICE_PATH = "http://localhost:8088/accounts";
    private final static int THREAD_COUNT = 8; //set how much threads you want to start

    private ObjectMapper objectMapper = new ObjectMapper();
    private HttpHeaders headers = new HttpHeaders();

    private RestTemplate restTemplate = new RestTemplate();


    @Test
    @ThreadCount(THREAD_COUNT)
    public void shouldGetAllAccounts() throws Exception {
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>("", headers);
        ResponseEntity<String> answer = restTemplate.exchange(INVOICES_SERVICE_PATH, HttpMethod.GET, entity, String.class);

        System.out.println("Status code: "+ answer.getStatusCode() + ", body: "+answer.getBody());
    }


    @Test
    @ThreadCount(THREAD_COUNT)
    public void shoulAddAccount() throws  Exception{
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>((json(ACCOUNT_JACEK_BALANCE_1000)), headers);
        ResponseEntity<String> answer = restTemplate.exchange(INVOICES_SERVICE_PATH, HttpMethod.POST, entity, String.class);

        System.out.println("Status code: "+ answer.getStatusCode() + ", body: "+answer.getBody());

    }




    @Test
   @ThreadCount(THREAD_COUNT)
    public void shouldReturnErrorCausedByEmptyNameAndEmptyBalanceFields() throws Exception {
        Account accountWithoutName = new Account(null, null,
                null); // TODO should be using AccoutRequest not Account

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>((json(accountWithoutName)), headers);
        ResponseEntity<String> answer = restTemplate.exchange(INVOICES_SERVICE_PATH, HttpMethod.POST, entity, String.class);

        System.out.println("Status code: "+ answer.getStatusCode() + ", body: "+answer.getBody());
    }

  /*  @Test
    public void shouldGetAccountById() throws Exception {
        long accountId = callRestServiceToAddAccountAndReturnId(ACCOUNT_LUKASZ_BALANCE_1124);

        mockMvc
                .perform(get(INVOICES_SERVICE_PATH + "/" + accountId))
                .andExpect(content().contentType(JSON_CONTENT_TYPE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

  @Test
    public void shouldReturnErrorCausedByNotExistingId() throws Exception {
        mockMvc
                .perform(get(INVOICES_SERVICE_PATH + "/" + NOT_EXISTING_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateAccount() throws Exception {
        long accountId = callRestServiceToAddAccountAndReturnId(ACCOUNT_ADAM_BALANCE_0);

        mockMvc.perform(put(INVOICES_SERVICE_PATH + "/" + accountId)
                .contentType(JSON_CONTENT_TYPE)
                .content(json(ACCOUNT_MATEUSZ_BALANCE_200)))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get(INVOICES_SERVICE_PATH + "/" + accountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_CONTENT_TYPE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Mateusz mBank saving account")))
                .andExpect(jsonPath("$.balance", is(equalTo("200.00"))));
    }

    @Test
    public void shouldUpdateAccountWithUpdatedAccountSameNameAsBefore() throws Exception {
        long accountId = callRestServiceToAddAccountAndReturnId(ACCOUNT_ADAM_BALANCE_0);
        Account updatedAccount = Account.builder().name(ACCOUNT_ADAM_BALANCE_0.getName())
                .balance(ACCOUNT_ADAM_BALANCE_0.getBalance().add(BigDecimal.TEN)).build();

        mockMvc.perform(put(INVOICES_SERVICE_PATH + "/" + accountId)
                .contentType(JSON_CONTENT_TYPE)
                .content(json(updatedAccount)))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get(INVOICES_SERVICE_PATH + "/" + accountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_CONTENT_TYPE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(ACCOUNT_ADAM_BALANCE_0.getName())))
                .andExpect(jsonPath("$.balance", is(equalTo("10.00"))));
    }

    @Test
    public void shouldReturnErrorCauseByDuplicatedNameWhileUpdatingAccount() throws Exception {
        callRestServiceToAddAccountAndReturnId(ACCOUNT_ADAM_BALANCE_0);
        long accountJacekId = callRestServiceToAddAccountAndReturnId(ACCOUNT_JACEK_BALANCE_1000);
        Account updatedAccount = Account.builder().name(ACCOUNT_ADAM_BALANCE_0.getName())
                .balance(ACCOUNT_JACEK_BALANCE_1000.getBalance()).build();

        mockMvc.perform(put(INVOICES_SERVICE_PATH + "/" + accountJacekId)
                .contentType(JSON_CONTENT_TYPE)
                .content(json(updatedAccount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]",
                        is(getMessage(ACCOUNT_WITH_PROVIDED_NAME_ALREADY_EXISTS))));

    }

    @Test
    public void shouldReturnErrorCauseByNotExistingIdInUpdateMethod() throws Exception {

        mockMvc
                .perform(put(INVOICES_SERVICE_PATH + "/" + NOT_EXISTING_ID)
                        .contentType(JSON_CONTENT_TYPE)
                        .content(json(ACCOUNT_ADAM_BALANCE_0)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnErrorCauseByNotValidAccountUpdateMethod() throws Exception {
        long accountId = callRestServiceToAddAccountAndReturnId(ACCOUNT_ADAM_BALANCE_0);
        Account accountToUpdate = Account.builder()
                .name("")
                .balance(ACCOUNT_ADAM_BALANCE_0.getBalance())
                .build();

        mockMvc
                .perform(put(INVOICES_SERVICE_PATH + "/" + accountId)
                        .contentType(JSON_CONTENT_TYPE)
                        .content(json(accountToUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldDeleteAccount() throws Exception {
        long accountId = callRestServiceToAddAccountAndReturnId(ACCOUNT_JUREK_BALANCE_10_99);

        mockMvc
                .perform(delete(INVOICES_SERVICE_PATH + "/" + accountId))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnErrorCauseByNotExistingIdInDeleteMethod() throws Exception {
        callRestServiceToAddAccountAndReturnId(ACCOUNT_JUREK_BALANCE_10_99);

        mockMvc
                .perform(delete(INVOICES_SERVICE_PATH + "/" + NOT_EXISTING_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnErrorCausedByExistingAccountName() throws Exception {
        callRestServiceToAddAccountAndReturnId(ACCOUNT_LUKASZ_BALANCE_1124);

        mockMvc.perform(post(INVOICES_SERVICE_PATH)
                .contentType(JSON_CONTENT_TYPE)
                .content(json(ACCOUNT_LUKASZ_BALANCE_1124)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]", is(getMessage(ACCOUNT_WITH_PROVIDED_NAME_ALREADY_EXISTS))));
    }
*/
    private String json(Account account) throws Exception {
        return objectMapper.writeValueAsString(account);
    }
//
//    private long callRestServiceToAddAccountAndReturnId(Account account) throws Exception {
//        String response =
//                mockMvc
//                        .perform(post(INVOICES_SERVICE_PATH)
//                                .content(json(account))
//                                .contentType(JSON_CONTENT_TYPE))
//                        .andExpect(status().isOk())
//                        .andReturn().getResponse().getContentAsString();
//        return Long.parseLong(response);
//    }

}