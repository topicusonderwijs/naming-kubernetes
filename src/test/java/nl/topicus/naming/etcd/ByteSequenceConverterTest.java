package nl.topicus.naming.etcd;

import com.coreos.jetcd.data.ByteSequence;
import org.junit.Assert;
import org.junit.Test;

public class ByteSequenceConverterTest
{
	@Test
	public void testFallback()
	{
		Assert.assertEquals("hello world",
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("hello world")));
	}

	@Test
	public void testString()
	{
		Assert.assertEquals("hello world", ByteSequenceConverter
			.coerceToType(ByteSequence.fromString("java.lang.String::hello world")));
	}

	@Test
	public void testEmptyString()
	{
		Assert.assertEquals("",
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("java.lang.String::")));
	}

	@Test
	public void testCharacter()
	{
		Assert.assertEquals('z',
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("char::z")));
	}

	@Test
	public void testCharacterNoValue()
	{
		Assert.assertEquals(null,
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("char::")));
	}

	@Test
	public void testCharacterTooLong()
	{
		Assert.assertEquals('a',
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("char::abcxyz")));
	}

	@Test
	public void testCharacterFullPrefix()
	{
		Assert.assertEquals('z',
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("java.lang.Character::z")));
	}

	@Test
	public void testInteger()
	{
		Assert.assertEquals(3,
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("int::3")));
	}

	@Test
	public void testIntegerFullPrefix()
	{
		Assert.assertEquals(7,
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("java.lang.Integer::7")));
	}

	@Test
	public void testIntegerNoValue()
	{
		Assert.assertEquals(null,
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("int::")));
	}

	@Test(expected = NumberFormatException.class)
	public void testIntegerNotParseable()
	{
		Assert.assertEquals(null,
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("int::a")));
	}

	@Test
	public void testBooleanTrue()
	{
		Assert.assertEquals(true,
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("boolean::TRUE")));
	}

	@Test
	public void testBooleanFullPrefixTrue()
	{
		Assert.assertEquals(true,
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("java.lang.Boolean::true")));
	}

	@Test
	public void testBooleanFalse()
	{
		Assert.assertEquals(false,
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("boolean::false")));
	}

	@Test
	public void testBooleanNoValue()
	{
		Assert.assertEquals(false,
			ByteSequenceConverter.coerceToType(ByteSequence.fromString("boolean::")));
	}
}
