package one.microstream.com.binarydynamic;

public interface ComHandlerSend<T extends ComMessage>
{
	public Object sendMessage(T message);

	public Object sendMessage(Object graphRoot);
}
