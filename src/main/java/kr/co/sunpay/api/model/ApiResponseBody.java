package kr.co.sunpay.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiResponseBody {

	private boolean success;

	private String message;
}
