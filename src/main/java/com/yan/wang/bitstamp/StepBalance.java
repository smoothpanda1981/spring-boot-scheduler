package com.yan.wang.bitstamp;

import javax.persistence.*;
import java.io.Serializable;

public class StepBalance {

	private String name;

	private String value;

	private Integer pagination;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getPagination() {
		return pagination;
	}

	public void setPagination(Integer pagination) {
		this.pagination = pagination;
	}
}
