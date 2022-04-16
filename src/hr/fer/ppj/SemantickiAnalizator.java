package hr.fer.ppj;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class SemantickiAnalizator {
	static SemantickiAnalizator sa;

	public static void main(String[] args) throws IOException {

//		Scanner file = new Scanner(System.in);
		Scanner file = new Scanner(Paths.get("ulaz.txt"));

		String line;
		line = file.nextLine();

		sa = new SemantickiAnalizator();
		int prethodna_dubina = 0;
		Cvor program = sa.new Cvor(null, line);
		Cvor prethodni_cvor = program;

		// generiraj objektno stablo
		while (file.hasNextLine()) {
			line = file.nextLine();
			int dubina = line.length() - line.replaceAll("^\\s+", "").length();
			Cvor cvor;
			Cvor roditelj;
			if (dubina == prethodna_dubina + 1) {
				roditelj = prethodni_cvor;
			} else if (dubina <= prethodna_dubina) {
				roditelj = prethodni_cvor.dohvatiRoditelja();
				for (int i = 0; i < prethodna_dubina - dubina; i++) {
					roditelj = roditelj.dohvatiRoditelja();
				}
			} else {
				throw new RuntimeException("Neocekivani format");
			}
			String stripped = line.strip();
			if ((stripped.startsWith("<") && stripped.endsWith(">")) || stripped.equals("$")) {
				cvor = sa.new Cvor(roditelj, stripped);
			} else {
				cvor = sa.new Cvor(roditelj, stripped.split(" ")[0], stripped.split(" ")[1], stripped.split(" ")[2]);
			}
			roditelj.dodajDijete(cvor);

			prethodna_dubina = dubina;
			prethodni_cvor = cvor;
		}

		try {
			program.postaviEnv(sa.new Env(null));
			program.evaluiraj(null);
		} catch (ParseException e) {
			System.out.println("err " + e.getMessage());
		}

	}

	class Env {
		private Map<String, String> varijable;

		public Env(Env nad) {
			this.varijable = new TreeMap<String, String>();
			if (nad != null) {
				for (Map.Entry<String, String> entry : nad.varijable.entrySet()) {
					this.varijable.put(entry.getKey(), entry.getValue());
				}
			}
		}

		public void dodajVarijablu(String ime, String redak) throws ParseException {
			if (!varijable.containsKey(ime))
				varijable.put(ime, redak);
		}

		public void dodajPrepisiVarijablu(String ime, String redak) throws ParseException {
			varijable.put(ime, redak);
		}

		public Map<String, String> dohvatiVarijable() {
			return varijable;
		}
	}

	class Cvor {
		private Cvor roditelj;
		private String znak;
		private String redak;
		private String data;
		private List<Cvor> djeca;
		private Env env;
		private int definicija_varijable = 0;

		public Cvor(Cvor roditelj, String znak) {
			this.roditelj = roditelj;
			this.znak = znak;
			this.djeca = new ArrayList<Cvor>();
		}

		public Cvor(Cvor roditelj, String znak, String redak, String data) {
			this.roditelj = roditelj;
			this.znak = znak;
			this.redak = redak;
			this.data = data;
			this.djeca = new ArrayList<Cvor>();
		}

		public void dodajDijete(Cvor cvor) {
			djeca.add(cvor);
		}

		public Cvor dohvatiRoditelja() {
			return roditelj;
		}

		public void postaviEnv(Env env) {
			this.env = env;
		}

		public void postaviDefiniciju() {
			definicija_varijable = 1;
		}

		public void postaviPrioritetnuDefiniciju() {
			definicija_varijable = 2;
		}

		public void evaluiraj(String trazeni_znak) throws ParseException {
			if (trazeni_znak != null && data != null) {
				if (trazeni_znak.equals(data))
					throw new ParseException(redak + " " + data, 0);
			}

			if (znak.equals("IDN")) {
				if (definicija_varijable == 2) {
					env.dodajPrepisiVarijablu(data, redak);
				} else if (definicija_varijable == 1) {
					env.dodajVarijablu(data, redak);
				} else {
					Map<String, String> varijable = env.dohvatiVarijable();
					if (!varijable.containsKey(data))
						throw new ParseException(redak + " " + data, 0);
					System.out.println(redak + " " + varijable.get(data) + " " + data);
				}
				return;
			} else if (znak.equals("<za_petlja>")) {
				env = new Env(env);
				Cvor idn = djeca.get(1);
				djeca.get(3).postaviEnv(env);
				djeca.get(3).evaluiraj(idn.data);
				djeca.get(5).postaviEnv(env);
				djeca.get(5).evaluiraj(idn.data);
				idn.postaviEnv(env);
				idn.postaviPrioritetnuDefiniciju();
				idn.evaluiraj(trazeni_znak);
				djeca.get(6).postaviEnv(env);
				djeca.get(6).evaluiraj(trazeni_znak);
				return;
			} else if (znak.equals("<naredba_pridruzivanja>")) {
				Cvor idn = djeca.get(0);
				idn.postaviDefiniciju();
				djeca.set(0, djeca.get(2));
				djeca.set(2, idn);
			}
			for (Cvor dijete : djeca) {
				dijete.postaviEnv(env);
				dijete.evaluiraj(trazeni_znak);
			}
			return;
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
