import java.io.FileInputStream;

public class Main {

	public static void main(String[] args) {
		System.out.println("Test Start");

		try {
			FileInputStream fis = new FileInputStream("test3.txt");
			VSJsonParser parser = new VSJsonParser(fis);
			while (parser.hasNext()) {
				String name = parser.getName();
				System.out.println("Name : " + name);
				String value = parser.getValue();
				System.out.println("Value : " + value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
