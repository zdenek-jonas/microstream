package one.microstream.afs.aws.s3;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

import one.microstream.collections.XArrays;

public interface S3Path
{
	final static String SEPARATOR      = "/";
	final static char   SEPARATOR_CAHR = '/';

	public String[] pathElements();

	public String bucket();

	public String identifier();

	public String fullQualifiedName();

	public S3Path parentPath();


	public static S3Path New(
		final String... pathElements
	)
	{
		if(pathElements.length == 0)
		{
			throw new IllegalArgumentException("empty path");
		}

		return new Default(pathElements);
	}


	public final static class Default implements S3Path
	{
		private final String[] pathElements     ;
		private       String   fullQualifiedName;

		Default(
			final String[] pathElements
		)
		{
			super();
			this.pathElements = pathElements;
		}

		@Override
		public String[] pathElements()
		{
			return this.pathElements;
		}

		@Override
		public String bucket()
		{
			return this.pathElements[0];
		}

		@Override
		public String identifier()
		{
			return this.pathElements[this.pathElements.length - 1];
		}

		@Override
		public String fullQualifiedName()
		{
			if(this.fullQualifiedName == null)
			{
				this.fullQualifiedName = Arrays
					.stream(this.pathElements)
					.collect(joining(SEPARATOR))
				;
			}

			return this.fullQualifiedName;
		}

		@Override
		public S3Path parentPath()
		{
			return this.pathElements.length > 1
				? new S3Path.Default(
					XArrays.copyRange(this.pathElements, 0, this.pathElements.length - 1)
				)
				: null;
		}

	}

}
