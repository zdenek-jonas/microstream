package one.microstream.persistence.binary.internal;

/*-
 * #%L
 * microstream-persistence-binary
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.afs.types.AFS;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AReadableFile;
import one.microstream.collections.BulkList;
import one.microstream.collections.Constant;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.ChunksWrapper;
import one.microstream.persistence.binary.types.ChunksWrapperByteReversing;
import one.microstream.persistence.binary.types.MessageWaiter;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.persistence.types.PersistenceSource;


public class BinaryFileSource implements PersistenceSource<Binary>, MessageWaiter
{
	public static final BinaryFileSource New(final AFile file, final boolean switchByteOrder)
	{
		return new BinaryFileSource(
			notNull(file),
			switchByteOrder
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

//	private static final int INITIAL_BUFFER_SIZE = 1_048_576; // or "1 << 20" or 2^20. 1 MB should be a good init size



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final AFile   file           ;
	private final boolean switchByteOrder;
	
	// (11.07.2019 TM)NOTE: removed, see comments at occurance for reason.
//	private final ByteBuffer chunkDataBuffer = ByteBuffer.allocateDirect(INITIAL_BUFFER_SIZE);



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryFileSource(final AFile file, final boolean switchByteOrder)
	{
		super();
		this.switchByteOrder = switchByteOrder;
		this.file            = file           ;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private ByteBuffer readChunk(final AReadableFile channel, final long chunkTotalLength)
	{
		final ByteBuffer byteBuffer = XMemory.allocateDirectNative(X.checkArrayRange(chunkTotalLength));
//		BinaryPersistence.setChunkTotalLength(byteBuffer);
//		byteBuffer.position(8);
		
		// only one buffer per chunk in simple implementation
		channel.readBytes(byteBuffer);
		
		return byteBuffer;
	}

	private Constant<Binary> read(final AReadableFile file)
	{
		final BulkList<ByteBuffer> chunks = new BulkList<>();

		// (10.07.2019 TM)NOTE: there is no more chunk length, just read all at once. Or re-add in FileTarget?
		chunks.add(this.readChunk(file, file.size()));
//		for(long readCount = 0, chunkTotalLength = 0; readCount < fileLength; readCount += chunkTotalLength)
//		{
//			chunkTotalLength = readChunkLength(this.chunkDataBuffer, channel, this);
//			chunks.add(this.readChunk(channel, fileLength));
//		}
		return X.<Binary>Constant(this.createChunksWrapper(chunks.toArray(ByteBuffer.class)));
	}
	
	private ChunksWrapper createChunksWrapper(final ByteBuffer[] byteBuffers)
	{
		return this.switchByteOrder
			? ChunksWrapperByteReversing.New(byteBuffers)
			: ChunksWrapper.New(byteBuffers)
		;
	}
	
//	private static final long readChunkLength(
//		final ByteBuffer          lengthBuffer ,
//		final ReadableByteChannel channel      ,
//		final MessageWaiter       messageWaiter
//	)
//		throws IOException
//	{
//		// not complicated to read a long from a channel. Not complicated at all. Just crap.
//		lengthBuffer.clear().limit(Binary.lengthLength());
//		fillBuffer(lengthBuffer, channel, messageWaiter);
////		return lengthBuffer.getLong();
//		/* They convert every single primitive to big endian, even if it's just from the same machine
//		 * to the same machine.
//		 * Giant runtime effort ruining everything just to avoid caring about / communicating local endianess.
//		 * Which is especially stupid as something like 90% of all machines are little endian anyway and
//		 * wouldn't requiere any endianess transformation at all.
//		 * Who cares about negligible overpriced SUN hardware and other exotics or some naive "network endianess".
//		 * They simply have to synchronize endianess in network communication via communication protocol.
//		 * Messing up the overwhelming normal case with RUNTIME effort just for those is so stupid I can't tell.
//		 */
//
//		// good thing is: doing it manually gets rid of the clumsy flipping in this case
//		return XMemory.get_long(XMemory.getDirectByteBufferAddress(lengthBuffer));
//	}

	

	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XGettingCollection<? extends Binary> read() throws PersistenceExceptionTransfer
	{
		try
		{
			return AFS.apply(this.file, rf -> this.read(rf));
		}
		catch(final Exception t)
		{
			throw new PersistenceExceptionTransfer(t);
		}
	}

	@Override
	public XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
		throws PersistenceExceptionTransfer
	{
		// simple input file reading implementation can't do complex queries
				
		// (10.07.2019 TM)NOTE: loader calls this here in loadOnce, so exception is not viable. Sufficient to return X.empty()?
		return X.empty();
//		throw new UnsupportedOperationException();
	}

	@Override
	public void waitForBytes(final int readCount)
	{
		// do nothing in simple local file reading implementation
	}

}
