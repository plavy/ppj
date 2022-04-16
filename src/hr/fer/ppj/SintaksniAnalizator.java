package hr.fer.ppj;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.text.ParseException;

public class SintaksniAnalizator {

	static List<List<String>> uniformni_znakovi = new ArrayList<List<String>>();
	static int i;
	static SintaksniAnalizator sa;

	public static void main(String[] args) throws IOException {

//		Scanner file = new Scanner(System.in);
		Scanner file = new Scanner(Paths.get("ulaz.txt"));

		String line;
		while (file.hasNextLine()) {
			line = file.nextLine();
			ArrayList<String> uniformni_znak = new ArrayList<String>();
			uniformni_znak.add(line.split(" ")[0]);
			uniformni_znak.add(line.split(" ")[1]);
			uniformni_znak.add(line.split(" ")[2]);
			uniformni_znakovi.add(uniformni_znak);
		}
		ArrayList<String> uniformni_znak = new ArrayList<String>();
		uniformni_znak.add("EOF");
		uniformni_znak.add("-");
		uniformni_znak.add("-");
		uniformni_znakovi.add(uniformni_znak);

		sa = new SintaksniAnalizator();
		Cvor korijen = sa.new Cvor("<program>");
		i = 0;
		try {
			korijen.evaluiraj();
			korijen.ispisi(0);
		} catch (ParseException e) {
			System.out.println("err " + e.getMessage());
		}

	}

	public static List<Cvor> odredi_dijete(String znak, String uniformni_znak) throws ParseException {
		List<Cvor> djeca = new ArrayList<Cvor>();
		if (znak.equals("<program>")) {
			if (uniformni_znak.equals("IDN") || uniformni_znak.equals("KR_ZA") || uniformni_znak.equals("EOF")) {
				djeca.add(sa.new Cvor("<lista_naredbi>"));
			}
		} else if (znak.equals("<lista_naredbi>")) {
			if (uniformni_znak.equals("IDN") || uniformni_znak.equals("KR_ZA")) {
				djeca.add(sa.new Cvor("<naredba>"));
				djeca.add(sa.new Cvor("<lista_naredbi>"));
			} else if (uniformni_znak.equals("KR_AZ") || uniformni_znak.equals("EOF")) {
				djeca.add(sa.new Cvor("$"));
			}
		} else if (znak.equals("<naredba>")) {
			if (uniformni_znak.equals("IDN")) {
				djeca.add(sa.new Cvor("<naredba_pridruzivanja>"));
			} else if (uniformni_znak.equals("KR_ZA")) {
				djeca.add(sa.new Cvor("<za_petlja>"));
			}
		} else if (znak.equals("<naredba_pridruzivanja>")) {
			if (uniformni_znak.equals("IDN")) {
				djeca.add(sa.new Cvor("IDN"));
				djeca.add(sa.new Cvor("OP_PRIDRUZI"));
				djeca.add(sa.new Cvor("<E>"));
			}

		} else if (znak.equals("<za_petlja>")) {
			if (uniformni_znak.equals("KR_ZA")) {
				djeca.add(sa.new Cvor("KR_ZA"));
				djeca.add(sa.new Cvor("IDN"));
				djeca.add(sa.new Cvor("KR_OD"));
				djeca.add(sa.new Cvor("<E>"));
				djeca.add(sa.new Cvor("KR_DO"));
				djeca.add(sa.new Cvor("<E>"));
				djeca.add(sa.new Cvor("<lista_naredbi>"));
				djeca.add(sa.new Cvor("KR_AZ"));
			}
		} else if (znak.equals("<E>")) {
			if (uniformni_znak.equals("IDN") || uniformni_znak.equals("BROJ") || uniformni_znak.equals("OP_PLUS")
					|| uniformni_znak.equals("OP_MINUS") || uniformni_znak.equals("L_ZAGRADA")) {
				djeca.add(sa.new Cvor("<T>"));
				djeca.add(sa.new Cvor("<E_lista>"));
			}
		} else if (znak.equals("<E_lista>")) {
			if (uniformni_znak.equals("OP_PLUS")) {
				djeca.add(sa.new Cvor("OP_PLUS"));
				djeca.add(sa.new Cvor("<E>"));
			} else if (uniformni_znak.equals("OP_MINUS")) {
				djeca.add(sa.new Cvor("OP_MINUS"));
				djeca.add(sa.new Cvor("<E>"));
			} else if (uniformni_znak.equals("IDN") || uniformni_znak.equals("KR_ZA") || uniformni_znak.equals("KR_DO")
					|| uniformni_znak.equals("KR_AZ") || uniformni_znak.equals("D_ZAGRADA")
					|| uniformni_znak.equals("EOF")) {
				djeca.add(sa.new Cvor("$"));
			}
		} else if (znak.equals("<T>")) {
			if (uniformni_znak.equals("IDN") || uniformni_znak.equals("BROJ") || uniformni_znak.equals("OP_PLUS")
					|| uniformni_znak.equals("OP_MINUS") || uniformni_znak.equals("L_ZAGRADA")) {
				djeca.add(sa.new Cvor("<P>"));
				djeca.add(sa.new Cvor("<T_lista>"));
			}
		} else if (znak.equals("<T_lista>")) {
			if (uniformni_znak.equals("OP_PUTA")) {
				djeca.add(sa.new Cvor("OP_PUTA"));
				djeca.add(sa.new Cvor("<T>"));
			} else if (uniformni_znak.equals("OP_DIJELI")) {
				djeca.add(sa.new Cvor("OP_DIJELI"));
				djeca.add(sa.new Cvor("<T>"));
			} else if (uniformni_znak.equals("IDN") || uniformni_znak.equals("KR_ZA") || uniformni_znak.equals("KR_DO")
					|| uniformni_znak.equals("KR_AZ") || uniformni_znak.equals("OP_PLUS")
					|| uniformni_znak.equals("OP_MINUS") || uniformni_znak.equals("D_ZAGRADA")
					|| uniformni_znak.equals("EOF")) {
				djeca.add(sa.new Cvor("$"));
			}
		} else if (znak.equals("<P>")) {
			if (uniformni_znak.equals("OP_PLUS")) {
				djeca.add(sa.new Cvor("OP_PLUS"));
				djeca.add(sa.new Cvor("<P>"));
			} else if (uniformni_znak.equals("OP_MINUS")) {
				djeca.add(sa.new Cvor("OP_MINUS"));
				djeca.add(sa.new Cvor("<P>"));
			} else if (uniformni_znak.equals("L_ZAGRADA")) {
				djeca.add(sa.new Cvor("L_ZAGRADA"));
				djeca.add(sa.new Cvor("<E>"));
				djeca.add(sa.new Cvor("D_ZAGRADA"));
			} else if (uniformni_znak.equals("IDN")) {
				djeca.add(sa.new Cvor("IDN"));
			} else if (uniformni_znak.equals("BROJ")) {
				djeca.add(sa.new Cvor("BROJ"));
			}
		}
		return djeca;
	}

	class Cvor {
		private String znak;
		private String redak;
		private String data;
		private List<Cvor> djeca;

		public Cvor(String znak) {
			this.znak = znak;
			this.djeca = new ArrayList<Cvor>();
		}

		public void evaluiraj() throws ParseException {
			if (znak.equals("$")) {
				return;
			}
			if (znak.startsWith("<") && znak.endsWith(">")) {
				djeca = odredi_dijete(znak, uniformni_znakovi.get(i).get(0));
				for (Cvor dijete : djeca) {
					dijete.evaluiraj();
				}
				if (djeca.size() != 0)
					return;
			}
			String uniformni_znak = uniformni_znakovi.get(i).get(0);
			redak = uniformni_znakovi.get(i).get(1);
			data = uniformni_znakovi.get(i).get(2);
			if (znak.equals(uniformni_znak)) {
				i++;
				return;
			}
			if (uniformni_znak.equals("EOF"))
				throw new ParseException("kraj", 0);
			throw new ParseException(uniformni_znak + " " + redak + " " + data, 0);

		}

		public void ispisi(int dubina) {
			String ispis = "";
			for (int i = 0; i < dubina; i++)
				ispis += " ";
			ispis += znak;
			if (!(znak.startsWith("<") && znak.endsWith(">")) && !znak.equals("$")) {
				ispis += " " + redak + " " + data;
			}
			System.out.println(ispis);
			for (Cvor dijete : djeca) {
				dijete.ispisi(dubina + 1);
			}
		}

	}

}
