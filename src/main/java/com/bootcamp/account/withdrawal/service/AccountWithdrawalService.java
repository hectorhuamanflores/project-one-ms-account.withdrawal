package com.bootcamp.account.withdrawal.service;

import com.bootcamp.account.withdrawal.dto.AccountWithdrawalRequest;
import com.bootcamp.account.withdrawal.entity.AccountWithdrawal;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountWithdrawalService {

    public Flux<AccountWithdrawal> getAllAccountWithdrawal();
    public Mono<AccountWithdrawal> getAccountWithdrawalById(String id);
    public Flux<AccountWithdrawal> getAccountWithdrawalByIdAccount(String idAccount);
    public Mono<AccountWithdrawal> createAccountWithdrawal(AccountWithdrawalRequest accountWithdrawal);
    public Mono<AccountWithdrawal> updateAccountWithdrawal(AccountWithdrawal accountWithdrawal);
    public Mono<AccountWithdrawal> deleteAccountWithdrawal(String id);
    
    
}
