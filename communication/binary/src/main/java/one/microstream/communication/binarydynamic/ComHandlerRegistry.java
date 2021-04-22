package one.microstream.communication.binarydynamic;

import one.microstream.collections.MiniMap;

public interface ComHandlerRegistry
{
	public <T> boolean registerSendHandler    (final Class<T> type, final ComHandlerSend<?> handler);
	public <T> boolean registerReceiveHandler (final Class<T> type, final ComHandlerReceive<?> handler);
	
	public <T extends ComMessage> ComHandlerSend<ComMessage> 	lookupSend(final Class<?> type);
	public <T extends ComMessage> ComHandlerReceive<ComMessage> lookupReceive(final Class<?> type);

	
	
	public final class Default implements ComHandlerRegistry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final MiniMap<Class<?>, ComHandlerSend<?>>    sendHandlers    = new MiniMap<>();
		private final MiniMap<Class<?>, ComHandlerReceive<?>> receiveHandlers = new MiniMap<>();
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default()
		{
			super();
		}
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final <T> boolean registerSendHandler(final Class<T> type, final ComHandlerSend<?> handler)
		{
			return this.sendHandlers.put(type, handler) != null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final <T extends ComMessage> ComHandlerSend<ComMessage> lookupSend(final Class<?> type)
		{
			return (ComHandlerSend<ComMessage>) this.sendHandlers.get(type);
		}
		
		@Override
		public final <T> boolean registerReceiveHandler(final Class<T> type, final ComHandlerReceive<?> handler)
		{
			return this.receiveHandlers.put(type, handler) != null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final <T extends ComMessage> ComHandlerReceive<ComMessage> lookupReceive(final Class<?> type)
		{
			return (ComHandlerReceive<ComMessage>) this.receiveHandlers.get(type);
		}
	}
	
	
}
