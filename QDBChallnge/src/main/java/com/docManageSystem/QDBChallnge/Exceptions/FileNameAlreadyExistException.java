package com.docManageSystem.QDBChallnge.Exceptions;

import org.springframework.dao.DataIntegrityViolationException;

public class FileNameAlreadyExistException extends DataIntegrityViolationException{

	public FileNameAlreadyExistException(String msg, Throwable cause) {
		super("File already exist", cause);
		// TODO Auto-generated constructor stub
	}

}
