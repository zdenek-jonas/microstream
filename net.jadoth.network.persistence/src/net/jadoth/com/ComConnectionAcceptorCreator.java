package net.jadoth.com;

public interface ComConnectionAcceptorCreator<C>
{
	public ComConnectionAcceptor<C> createConnectionAcceptor(
		ComProtocolProvider        protocolProvider       ,
		ComConnectionHandler<C>    connectionHandler      ,
		ComProtocolStringConverter protocolStringConverter,
		ComHostChannelCreator<C>   channelCreator         ,
		ComChannelAcceptor         channelAcceptor
	);
	
	
	public static <C> ComConnectionAcceptorCreator<C> New()
	{
		return new ComConnectionAcceptorCreator.Implementation<>();
	}
	
	public final class Implementation<C> implements ComConnectionAcceptorCreator<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComConnectionAcceptor<C> createConnectionAcceptor(
			final ComProtocolProvider        protocolProvider       ,
			final ComConnectionHandler<C>    connectionHandler      ,
			final ComProtocolStringConverter protocolStringConverter,
			final ComHostChannelCreator<C>   channelCreator         ,
			final ComChannelAcceptor         channelAcceptor
		)
		{
			return ComConnectionAcceptor.New(
				protocolProvider       ,
				connectionHandler      ,
				protocolStringConverter,
				channelCreator         ,
				channelAcceptor
			);
		}
		
	}
	
}
