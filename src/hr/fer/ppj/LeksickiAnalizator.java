package hr.fer.ppj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LeksickiAnalizator {
	static String unit = "";
	static StringBuilder output = new StringBuilder();
	static int line_number = 1;
	static List<String> one_char_lexems = List.of("=", "+", "-", "*", "/", "(", ")");
	static List<String> one_char_classes = List.of("OP_PRIDRUZI", "OP_PLUS", "OP_MINUS", "OP_PUTA", "OP_DIJELI",
			"L_ZAGRADA", "D_ZAGRADA");
	static List<String> two_char_lexems = List.of("za", "od", "do", "az");
	static List<String> two_char_classes = List.of("KR_ZA", "KR_OD", "KR_DO", "KR_AZ");

	public static void main(String[] args) throws IOException {

		List<Character> chars = new ArrayList<Character>();
		//InputStreamReader file = new InputStreamReader(System.in);
		BufferedReader file = new BufferedReader(new FileReader(new File("ulaz.txt")));
		int read;
		while ((read = file.read()) != -1) {
			chars.add((char) read);
		}
		chars.add('\n');

		boolean COMMENT = false;
		for (int i = 0; i < chars.size(); i++) {
			char c = chars.get(i);
			if (COMMENT) {
				if (c == '\n') {
					COMMENT = false;
					line_number += 1;
				}
			} else if (c == '\n') {
				determine();
				line_number += 1;
			} else if (c == '\t' || c == ' ') {
				determine();
			} else if (c == '/' && chars.get(i+1) == '/') {
				determine();
				COMMENT = true;
			} else if (one_char_lexems.contains(String.valueOf(c))) {
				determine();
				unit = String.valueOf(c);
				determine();
			} else {
				unit += c;
			}
		}

		file.close();
		System.out.print(output.toString());
	}

	public static void determine() {
		if (unit == "")
			return;
		String klasa = "IDN";
		if (one_char_lexems.contains(unit)) {
			klasa = one_char_classes.get(one_char_lexems.indexOf(unit));
		} else if (two_char_lexems.contains(unit)) {
			klasa = two_char_classes.get(two_char_lexems.indexOf(unit));
		} else if (isInteger(unit)) {
			klasa = "BROJ";
		} else if (isInteger(unit.substring(0, 1))) {
			int i;
			for (i = 1; i < unit.length(); i++) {
				if (!isInteger(unit.substring(i, i + 1))) {
					break;
				}
			}
			String helper_unit = unit.substring(i);
			unit = unit.substring(0, i);
			determine();
			unit = helper_unit;
			determine();
			return;
		}

		output.append(klasa + " " + line_number + " " + unit + '\n');
		unit = "";
		return;
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}