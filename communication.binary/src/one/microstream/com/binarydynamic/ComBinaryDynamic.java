package one.microstream.com.binarydynamic;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import one.microstream.com.Com;
import one.microstream.com.ComClient;
import one.microstream.com.ComClientChannel;
import one.microstream.com.ComFoundation;
import one.microstream.com.ComHost;
import one.microstream.com.ComHostChannelAcceptor;
import one.microstream.com.ComPersistenceAdaptorCreator;

public class ComBinaryDynamic
{

	public static ComFoundation.Default<?> Foundation()
	{
		return ComFoundation.New()
			.setPersistenceAdaptorCreator(DefaultPersistenceAdaptorCreator())
			.setHostIdStrategy(ComDynamicIdStrategy.New(1_000_000_000_000_000_000L))
			.setClientIdStrategy(ComDynamicIdStrategy.New(4_100_000_000_000_000_000L))
			.registerEntityTypes(ComMessageNewType.class, ComMessageClientError.class, ComMessageStatus.class, ComMessageData.class)
		;
	}

	private static ComPersistenceAdaptorCreator<SocketChannel> DefaultPersistenceAdaptorCreator()
	{
		return ComPersistenceAdaptorBinaryDynamic.Creator();
	}

	///////////////////////////////////////////////////////////////////////////
	// convenience methods //
	////////////////////////
		
	
	/////
	// host convenience methods
	////
	
	public static final ComHost<SocketChannel> Host()
	{
		return Host(DefaultPersistenceAdaptorCreator(), null);
	}
	
	public static final ComHost<SocketChannel> Host(
		final int localHostPort
	)
	{
		return Host(localHostPort, DefaultPersistenceAdaptorCreator(), null);
	}
	
	public static final ComHost<SocketChannel> Host(
		final InetSocketAddress  targetAddress
	)
	{
		return Host(targetAddress, DefaultPersistenceAdaptorCreator(), null);
	}
	
	public static final ComHost<SocketChannel> Host(
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		return Host(
			DefaultPersistenceAdaptorCreator(),
			channelAcceptor
		);
	}
	
	public static final ComHost<SocketChannel> Host(
		final int                                   localHostPort  ,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		return Host(
			DefaultPersistenceAdaptorCreator(),
			channelAcceptor
		);
	}
	
	public static final ComHost<SocketChannel> Host(
		final InetSocketAddress                     targetAddress  ,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		return Host(targetAddress, DefaultPersistenceAdaptorCreator(), channelAcceptor);
	}
	
	public static final ComHost<SocketChannel> Host(
		final ComPersistenceAdaptorCreator<SocketChannel> persistenceAdaptorCreator,
		final ComHostChannelAcceptor<SocketChannel>       channelAcceptor
	)
	{
		return Host(
			Com.localHostSocketAddress(),
			persistenceAdaptorCreator   ,
			channelAcceptor
		);
	}
	
	public static final ComHost<SocketChannel> Host(
		final InetSocketAddress                           targetAddress            ,
		final ComPersistenceAdaptorCreator<SocketChannel> persistenceAdaptorCreator,
		final ComHostChannelAcceptor<SocketChannel>       channelAcceptor
	)
	{
		final ComHost<SocketChannel> host =
			Foundation()
			.setHostBindingAddress       (targetAddress)
			.setPersistenceAdaptorCreator(persistenceAdaptorCreator)
			.setHostChannelAcceptor      (channelAcceptor)
			.createHost()
		;
		
		return host;
	}
	
	public static final ComHost<SocketChannel> Host(
			final int                                         localHostPort            ,
			final ComPersistenceAdaptorCreator<SocketChannel> persistenceAdaptorCreator,
			final ComHostChannelAcceptor<SocketChannel>       channelAcceptor
		)
	{
		return Host(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptorCreator                ,
			channelAcceptor
		);
	}
		
	public static final void runHost()
	{
		runHost(null, null);
	}
	
	public static final void runHost(
		final int localHostPort
	)
	{
		runHost(localHostPort, null);
	}
	
	public static final void runHost(
		final InetSocketAddress targetAddress
	)
	{
		runHost(targetAddress, null);
	}
	
	public static final void runHost(
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(),
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final int                                   localHostPort  ,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(localHostPort),
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final InetSocketAddress                     targetAddress  ,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		final ComHost<SocketChannel> host = Host(targetAddress, channelAcceptor);
		host.run();
	}
	
	/////
	// client convenience methods
	////
	
	public static final ComClient<SocketChannel> Client()
	{
		return Client(
			DefaultPersistenceAdaptorCreator()
		);
	}
	
	public static final ComClient<SocketChannel> Client(final int localHostPort)
	{
		return Client(
			localHostPort                     ,
			DefaultPersistenceAdaptorCreator()
		);
	}
		
	public static final ComClient<SocketChannel> Client(
		final InetSocketAddress targetAddress
	)
	{
		return Client(
			targetAddress,
			DefaultPersistenceAdaptorCreator()
		);
	}
	
	public static final ComClient<SocketChannel> Client(
		final ComPersistenceAdaptorCreator<SocketChannel> persistenceAdaptorCreator
	)
	{
		return Client(
			Com.localHostSocketAddress(),
			persistenceAdaptorCreator
		);
	}
	
	public static final ComClient<SocketChannel> Client(
			final int                                         localHostPort     ,
			final ComPersistenceAdaptorCreator<SocketChannel> persistenceAdaptorCreator
	)
	{
		return Client(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptorCreator
		);
	}
	
	public static final ComClient<SocketChannel> Client(
		final InetSocketAddress                           targetAddress     ,
		final ComPersistenceAdaptorCreator<SocketChannel> persistenceAdaptorCreator
	)
	{
		final ComClient<SocketChannel> client = Foundation()
			.setClientTargetAddress(targetAddress)
			.setPersistenceAdaptorCreator(persistenceAdaptorCreator)
			.createClient()
		;
		
		return client;
	}
	
	
	public static final ComClientChannel<SocketChannel> connect()
	{
		return Client()
			.connect()
		;
	}
	
	public static final ComClientChannel<SocketChannel> connect(
		final int localHostPort
	)
	{
		return Client(localHostPort)
			.connect()
		;
	}
		
	public static final ComClientChannel<SocketChannel> connect(
		final InetSocketAddress targetAddress
	)
	{
		return Client(targetAddress)
			.connect()
		;
	}

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private ComBinaryDynamic()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

