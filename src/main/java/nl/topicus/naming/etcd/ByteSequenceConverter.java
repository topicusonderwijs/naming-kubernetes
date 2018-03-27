package nl.topicus.naming.etcd;

import java.util.Optional;

import com.coreos.jetcd.data.ByteSequence;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;

public class ByteSequenceConverter
{
	private static final ByteString PREFIX_STRING = ByteString.copyFromUtf8("java.lang.String::");

	private static final ByteString PREFIX_CHARACTER = ByteString.copyFromUtf8("char::");

	private static final ByteString PREFIX_CHARACTER_FULL =
		ByteString.copyFromUtf8("java.lang.Character::");

	private static final ByteString PREFIX_INTEGER = ByteString.copyFromUtf8("int::");

	private static final ByteString PREFIX_INTEGER_FULL =
		ByteString.copyFromUtf8("java.lang.Integer::");

	private static final ByteString PREFIX_BOOLEAN = ByteString.copyFromUtf8("boolean::");

	private static final ByteString PREFIX_BOOLEAN_FULL =
		ByteString.copyFromUtf8("java.lang.Boolean::");

	private ByteSequenceConverter()
	{
	}

	/**
	 * Based on org.jboss.as.naming.subsystem.NamingBindingAdd:coerceToType
	 */
	public static Object coerceToType(final ByteSequence rawValue)
	{
		ByteString value = rawValue.getByteString();

		Optional< ? > result = coerceToString(value);
		if (result != null)
			return result.orElse(null);

		result = coerceToCharacter(value);
		if (result != null)
			return result.orElse(null);

		// TODO: add short

		result = coerceToInteger(value);
		if (result != null)
			return result.orElse(null);

		// TODO: add long

		// TODO: add float

		// TODO: add double

		result = coerceToBoolean(value);
		if (result != null)
			return result.orElse(null);

		// TODO: add URL

		return value.toStringUtf8();
	}

	private static Optional<String> coerceToString(ByteString rawValue)
	{
		if (rawValue.startsWith(PREFIX_STRING))
		{
			return Optional.of(rawValue.substring(PREFIX_STRING.size()).toStringUtf8());
		}
		return null;
	}

	private static Optional<Character> coerceToCharacter(ByteString rawValue)
	{
		ByteString value = getValue(PREFIX_CHARACTER, rawValue);
		if (value == null)
		{
			value = getValue(PREFIX_CHARACTER_FULL, rawValue);
		}
		if (value != null)
		{
			return Strings.isNullOrEmpty(value.toStringUtf8()) ? Optional.empty()
				: Optional.of(value.toStringUtf8().charAt(0));
		}
		return null;
	}

	private static Optional<Integer> coerceToInteger(ByteString rawValue)
	{
		ByteString value = getValue(PREFIX_INTEGER, rawValue);
		if (value == null)
		{
			value = getValue(PREFIX_INTEGER_FULL, rawValue);
		}
		if (value != null)
		{
			return Strings.isNullOrEmpty(value.toStringUtf8()) ? Optional.empty()
				: Optional.of(Integer.parseInt(value.toStringUtf8()));
		}
		return null;
	}

	private static Optional<Boolean> coerceToBoolean(ByteString rawValue)
	{
		ByteString value = getValue(PREFIX_BOOLEAN, rawValue);
		if (value == null)
		{
			value = getValue(PREFIX_BOOLEAN_FULL, rawValue);
		}
		return value != null ? Optional.of(Boolean.parseBoolean(value.toStringUtf8())) : null;
	}

	private static ByteString getValue(ByteString prefix, ByteString rawValue)
	{
		if (rawValue.startsWith(prefix))
		{
			return rawValue.substring(prefix.size());
		}
		return null;
	}
}
