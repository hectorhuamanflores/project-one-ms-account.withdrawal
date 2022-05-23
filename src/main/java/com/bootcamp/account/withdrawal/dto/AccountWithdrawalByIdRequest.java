package com.bootcamp.account.withdrawal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountWithdrawalByIdRequest {
	
   private String idAccountWithdrawal;
   private Integer currency;
}
