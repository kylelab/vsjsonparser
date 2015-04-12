import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VSJsonParser {

	private static final char CHAR_NULL = '\u0000';
	private static final int NAME_TOKEN = 1;
	private static final int VALUE_TOKEN = NAME_TOKEN + 1;

	private char[] jsonCharArray;
	private int column = 0;
	private StringBuilder jsonName;
	private StringBuilder jsonValue;

	private int tokenType = NAME_TOKEN;
	private boolean isArrayAndValue = false;
	private StringBuilder returnJson = null;

	public VSJsonParser(String jsonStr) {
		if (jsonStr == null || jsonStr.length() == 0) {
			new Exception("Empty JsonString");
		}
		this.jsonCharArray = jsonStr.toCharArray();
	}

	public VSJsonParser(InputStream jsonStream) {
		if (jsonStream == null) {
			new Exception("Empty JsonStream");
		}

		InputStreamReader is = new InputStreamReader(jsonStream);
		CharArrayWriter caw = new CharArrayWriter();

		char[] buffer = new char[1024];
		int charInt = 0;
		try {
			while (-1 != (charInt = is.read(buffer))) {
				caw.write(buffer, 0, charInt);
			}
		} catch (IOException e) {
		}

		if (caw.size() <= 0) {
			return;
		}

		this.jsonCharArray = caw.toCharArray();
	}

	private char getCharToken() {
		if (jsonCharArray.length < 0) {
			new Exception("Empty Char Array");
		}

		if (jsonCharArray.length <= column) {
			return CHAR_NULL;
		}

		return jsonCharArray[column++];
	}

	public boolean hasNext() {
		if (jsonCharArray.length < 0) {
			new Exception("Empty Char Array");
		}
		return jsonCharArray.length > column;
	}

	/**
	 * Start
	 */
	public void get() {
		removeWhitespace();

		boolean isGet = false;
		while (!isGet) {
			char oneToken = getCharToken();
			if (oneToken == CHAR_NULL) {
				return;
			}
			switch (oneToken) {
				case ',':
					if (isArrayAndValue == true) {
						processTokenType(VALUE_TOKEN);
						isGet = true;
						break;
					} else {
						processTokenType(NAME_TOKEN);
						isGet = true;
						break;
					}
				case '{':
					isArrayAndValue = false;
				case '"':
					column--;
					processTokenType(NAME_TOKEN);
					isGet = true;
					break;
				case ':':
					processTokenType(VALUE_TOKEN);
					isGet = true;
					break;
				case ']':
					isArrayAndValue = false;
					break;
			}
		}

	}

	private void processTokenType(int type) {
		if (this.tokenType != type) {
			column--;
			return;
		}

		if (this.tokenType == NAME_TOKEN) {
			jsonName = new StringBuilder();
			returnJson = jsonName;
			processName();
		} else if (this.tokenType == VALUE_TOKEN) {
			jsonValue = new StringBuilder();
			returnJson = jsonValue;
			processValue();
		}
	}

	private void processName() {
		removeWhitespace();

		boolean isGet = false;
		while (!isGet) {
			char oneToken = getCharToken();
			if (oneToken == CHAR_NULL) {
				return;
			}
			switch (oneToken) {
				case '"':
					peekNameToken();
					isGet = true;
					break;
			}
		}
	}

	private void peekNameToken() {
		boolean isGet = false;
		while (!isGet) {
			char oneToken = getCharToken();
			if (oneToken == CHAR_NULL) {
				return;
			}
			switch (oneToken) {
				case '"':
					isGet = true;
					continue;
			}
			returnJson.append(oneToken);
		}
	}

	private void processValue() {
		removeWhitespace();

		boolean isGet = false;

		while (!isGet) {
			char oneToken = getCharToken();
			if (oneToken == CHAR_NULL) {
				return;
			}
			switch (oneToken) {
				case '"':
					peekValueToken();
					isGet = true;
					continue;
				case '[':
					isArrayAndValue = true;
					continue;
				case ',':
					column--;
					isGet = true;
					continue;
				case '}':
				case ']':
				case ' ':
				case '\r':
				case '\n':
				case '\t':
					isArrayAndValue = false;
					isGet = true;
					continue;
				case '{':
					processTokenType(NAME_TOKEN);
					isGet = true;
					continue;
			}
			returnJson.append(oneToken);
		}
	}

	private void peekValueToken() {
		boolean isGet = false;
		while (!isGet) {
			char oneToken = getCharToken();
			if (oneToken == CHAR_NULL) {
				return;
			}
			switch (oneToken) {
				case '"':
					isGet = true;
					continue;
			}
			returnJson.append(oneToken);
		}
	}

	private void removeWhitespace() {
		boolean isGet = false;

		while (!isGet) {
			char oneToken = getCharToken();
			if (oneToken == CHAR_NULL) {
				return;
			}
			switch (oneToken) {
				case ' ':
				case '\r':
				case '\n':
				case '\t':
					continue;
				default:
					column--;
					isGet = true;
					break;
			}
		}
	}

	public String getName() {
		this.tokenType = NAME_TOKEN;
		get();

		String name = null;
		if (jsonName != null && jsonName.length() > 0) {
			name = jsonName.toString();
		}
		jsonName = null;
		return name;
	}

	public String getValue() {
		this.tokenType = VALUE_TOKEN;
		get();

		String value = null;
		if (jsonValue != null && jsonValue.length() > 0) {
			value = jsonValue.toString();
		}
		jsonValue = null;
		return value;
	}

	public boolean isArray() {
		return isArrayAndValue;
	}
}
