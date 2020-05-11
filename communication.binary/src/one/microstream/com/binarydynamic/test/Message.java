package one.microstream.com.binarydynamic.test;

public class Message
{
	private final String text;

	public Message(final String text)
	{
		super();
		this.text = text;
	}

	public String getText()
	{
		return this.text;
	}

	@Override
	public String toString()
	{
		return this.text;
	}
}
