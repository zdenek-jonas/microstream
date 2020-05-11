package one.microstream.com.binarydynamic;

import one.microstream.exceptions.BaseException;

public class ComMessageClientError extends ComMessageStatus
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final String errorMessage;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComMessageClientError(final BaseException e)
	{
		super(false);
		this.errorMessage = e.getMessage();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public String getErrorMessage()
	{
		return this.errorMessage;
	}

}
