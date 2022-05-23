package com.bootcamp.account.withdrawal.service.impl;

import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.bootcamp.account.withdrawal.dto.AccountByNumAccountRequest;
import com.bootcamp.account.withdrawal.dto.AccountByNumAccountResponse;
import com.bootcamp.account.withdrawal.dto.AccountUpdateForTrxRequest;
import com.bootcamp.account.withdrawal.dto.AccountUpdateForTrxResponse;
import com.bootcamp.account.withdrawal.dto.AccountWithdrawalRequest;
import com.bootcamp.account.withdrawal.entity.AccountWithdrawal;
import com.bootcamp.account.withdrawal.repository.AccountWithdrawalRepository;
import com.bootcamp.account.withdrawal.service.AccountWithdrawalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountWithdrawalServiceImpl implements AccountWithdrawalService{
	private final AccountWithdrawalRepository accountWithdrawalRepository;
    
	private WebClient creditServiceClient = WebClient.builder()
		      .baseUrl("http://localhost:8092")
		      .build();
   
	private Function<AccountByNumAccountRequest, Mono<AccountByNumAccountResponse>> msAccountbynumAccount = (objeto) -> creditServiceClient.post()
			.uri("/Account/numAccount/")
			.body(Mono.just(objeto), AccountByNumAccountResponse.class)
			.retrieve()
			.bodyToMono(AccountByNumAccountResponse.class);
	
	private Function<AccountUpdateForTrxRequest, Mono<AccountUpdateForTrxResponse>> msAccountTrx = (objeto) -> creditServiceClient.put()
			.uri("/Account/updateAccountTrx/")
			.body(Mono.just(objeto), AccountUpdateForTrxResponse.class)
			.retrieve()
			.bodyToMono(AccountUpdateForTrxResponse.class);

	    
    @Override
    public Flux<AccountWithdrawal> getAllAccountWithdrawal() {
        return accountWithdrawalRepository.findAll();
    }

    @Override
    public Mono<AccountWithdrawal> getAccountWithdrawalById(String id) {
        return accountWithdrawalRepository.findById(id);
    }
    
    @Override
	public Flux<AccountWithdrawal> getAccountWithdrawalByIdAccount(String idAccount) {
		log.info("INICIO_ACCOUNT_Withdrawal");
		log.info("idAccount: "+idAccount);
		return accountWithdrawalRepository.findByIdAccount(idAccount);
	}
    
    @Override
    public Mono<AccountWithdrawal> createAccountWithdrawal(AccountWithdrawalRequest Withdrawal) {
    	AccountByNumAccountRequest numAccount = AccountByNumAccountRequest.builder()
    			.numAccount(Withdrawal.getIdAccount())
    			.build();
    	 
    	 //consultar  saldo 
    	 Mono<AccountByNumAccountResponse> consultMsAccount = msAccountbynumAccount.apply(numAccount);

    	
		 return consultMsAccount.flatMap(result -> {
			 double commssion;
			 if(result.getMovementTrxMax() > result.getMovement()) {
				 log.error("Sin comision");
				 commssion = 0.0;
			 }else{
				 log.error("Con comision");
				 commssion = result.getCommission();	 
			 }
			 AccountWithdrawal t = AccountWithdrawal.builder()
					    .idAccount(Withdrawal.getIdAccount())     
					    .tyTrxAccount(Withdrawal.getTyTrxAccount())   
					    .tyAccount(Withdrawal.getTyAccount())       
					    .currency(Withdrawal.getCurrency())       
					    .amountWithdrawal(Withdrawal.getAmountWithdrawal())    
					    .commission(commssion)       
					    .amountTrx(Withdrawal.getAmountWithdrawal()+commssion)        
					    .dateStar(Withdrawal.getDateStar())
					    .build();
			    
			 AccountUpdateForTrxRequest accountUpdateForTrx = AccountUpdateForTrxRequest.builder()
		    			.numAccount(Withdrawal.getIdAccount())
		    			.type(Withdrawal.getTyTrxAccount())
		    			.amount(Withdrawal.getAmountWithdrawal()+commssion)
		    			.build();
			 
			 log.error("Entro al servicio Actualizar cuenta");
			 Mono<AccountUpdateForTrxResponse> f = msAccountTrx.apply(accountUpdateForTrx);
			 log.error("Salio del servicio Actualizar cuenta");
			 log.error("Crea el nuevo Withdrawalo");
			 return f.flatMap(ra ->accountWithdrawalRepository.save(t));		  

		 });  	
       
    }

    @Override
    public Mono<AccountWithdrawal> updateAccountWithdrawal(AccountWithdrawal accountWithdrawal) {
    	 
        return accountWithdrawalRepository.findById(accountWithdrawal.getId())
                .flatMap( object ->{
                	object.setIdAccount(accountWithdrawal.getIdAccount());
                	object.setTyTrxAccount(accountWithdrawal.getTyTrxAccount());
                	object.setTyAccount(accountWithdrawal.getTyAccount());
                	object.setCurrency(accountWithdrawal.getCurrency());
                	object.setAmountWithdrawal(accountWithdrawal.getAmountWithdrawal());
                	object.setCommission(accountWithdrawal.getCommission());
                	object.setAmountTrx(accountWithdrawal.getAmountTrx());
                	
                    return accountWithdrawalRepository.save(object);
                 });
    }

    @Override
    public Mono<AccountWithdrawal> deleteAccountWithdrawal(String id) {
        return accountWithdrawalRepository.findById(id)
                .flatMap(existsAccountWithdrawalRepository -> accountWithdrawalRepository.delete(existsAccountWithdrawalRepository)
                        .then(Mono.just(existsAccountWithdrawalRepository)));
    }

	



	
}
