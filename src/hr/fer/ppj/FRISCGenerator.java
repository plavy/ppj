package hr.fer.ppj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class FRISCGenerator {
	static FRISCGenerator sa;
	static FRISC frisc = (new FRISCGenerator()).new FRISC();

	public static void main(String[] args) throws IOException {

//		Scanner file = new Scanner(System.in);
		Scanner file = new Scanner(Paths.get("ulaz.txt"));

		String line;
		line = file.nextLine();

		sa = new FRISCGenerator();
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

		program.postaviEnv(sa.new Env(null));
		program.evaluiraj();

		frisc.finaliziraj();
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

		public void dodajVarijablu(String ime, String redak) {
			if (!varijable.containsKey(ime))
				varijable.put(ime, redak);
		}

		public void dodajPrepisiVarijablu(String ime, String redak) {
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

		public void evaluiraj() {
			if (znak.equals("IDN")) {
				if (definicija_varijable == 2) {
					env.dodajPrepisiVarijablu(data, redak);
					frisc.uzmiSaStoga(znak, data);
				} else if (definicija_varijable == 1) {
					env.dodajVarijablu(data, redak);
					frisc.uzmiSaStoga(znak, data);
				} else {
					Map<String, String> varijable = env.dohvatiVarijable();
					frisc.staviNaStog(znak, data);
					// System.out.println(redak + " " + varijable.get(data) + " " + data);
				}
				return;
			} else if (znak.equals("<za_petlja>")) {
				env = new Env(env);
				Cvor idn = djeca.get(1);
				djeca.get(3).postaviEnv(env);
				djeca.get(3).evaluiraj();
				idn.postaviEnv(env);
				idn.postaviPrioritetnuDefiniciju();
				idn.evaluiraj();
				djeca.get(5).postaviEnv(env);
				frisc.dodajLiniju("FOR");
				djeca.get(6).postaviEnv(env);
				djeca.get(6).evaluiraj();
				frisc.finalizirajZa(idn.data, djeca.get(5));
				return;
			} else if (znak.equals("<naredba_pridruzivanja>")) {
				Cvor idn = djeca.get(0);
				idn.postaviDefiniciju();
				djeca.set(0, djeca.get(2));
				djeca.set(2, idn);
			} else if (znak.equals("BROJ")) {
				frisc.staviNaStog(znak, data);
			} else if (znak.equals("<E_lista>")) {
				if (djeca.size() == 2) {
					Cvor op = djeca.get(0);
					djeca.set(0, djeca.get(1));
					djeca.set(1, op);
				}
			} else if (znak.equals("OP_PLUS")) {
				frisc.zbrojiOduzmi(znak);
			} else if (znak.equals("OP_MINUS")) {
				frisc.zbrojiOduzmi(znak);
			} else if (znak.equals("<P>")) {
				if (djeca.size() == 2) {
					Cvor op = djeca.get(0);
					djeca.set(0, djeca.get(1));
					djeca.set(1, op);
					frisc.staviNaStog("BROJ", "0");
				}
			} else if (znak.equals("<T_lista>")) {
				if (djeca.size() == 2) {
					Cvor op = djeca.get(0);
					djeca.set(0, djeca.get(1));
					djeca.set(1, op);
				}
			} else if (znak.equals("OP_PUTA")) {
				frisc.mnoziDijeli(znak);
			} else if (znak.equals("OP_DIJELI")) {
				frisc.mnoziDijeli(znak);
			}
			for (Cvor dijete : djeca) {
				dijete.postaviEnv(env);
				dijete.evaluiraj();
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

	class FRISC {
		private StringBuilder kod;
		private Set<String> varijable;

		public FRISC() {
			this.kod = new StringBuilder();
			this.varijable = new TreeSet<String>();
			dodajLiniju(" MOVE 40000, R7");
		}

		private void dodajVarijablu(String data) {
			varijable.add(data);
		}

		public void dodajLiniju(String linija) {
			kod.append(linija);
			kod.append("\n");
		}

		public void staviNaStog(String znak, String data) {
			if (znak.equals("BROJ")) {
				dodajLiniju(" MOVE %D " + data + ", R0");
			} else if (znak.equals("IDN")) {
				dodajLiniju(" LOAD R0, (" + data + ")");
			} else {
				throw new RuntimeException("Invalid arg");
			}
			dodajLiniju(" PUSH R0");
		}

		public void uzmiSaStoga(String znak, String data) {
			dodajLiniju(" POP R0");
			if (znak.equals("IDN")) {
				dodajLiniju(" STORE R0, (" + data + ")");
				dodajVarijablu(data);
			} else {
				throw new RuntimeException("Invalid arg");
			}
		}

		public void zbrojiOduzmi(String znak) {
			dodajLiniju(" POP R1");
			dodajLiniju(" POP R0");
			if (znak.equals("OP_PLUS")) {
				dodajLiniju(" ADD R0, R1, R2");
			} else if (znak.equals("OP_MINUS")) {
				dodajLiniju(" SUB R0, R1, R2");
			} else {
				throw new RuntimeException("Invalid arg");
			}
			dodajLiniju(" PUSH R2");
		}

		public void mnoziDijeli(String znak) {
			if (znak.equals("OP_PUTA")) {
				dodajLiniju(" CALL MUL");
			} else if (znak.equals("OP_DIJELI")) {
				dodajLiniju(" CALL DIV");
			} else {
				throw new RuntimeException("Invalid arg");
			}
		}

		public void finalizirajZa(String iterator_data, Cvor izraz_do) {
			dodajLiniju(" LOAD R0, (" + iterator_data + ")");
			dodajLiniju(" ADD R0, 1, R0");
			dodajLiniju(" STORE R0, (" + iterator_data + ")");
			izraz_do.evaluiraj();
			dodajLiniju(" LOAD R0, (" + iterator_data + ")");
			dodajLiniju(" POP R1");
			dodajLiniju(" CMP R0, R1");
			dodajLiniju(" JP_SLE FOR");
		}

		public void finaliziraj() throws IOException {

			// ucitavanje rezultata
			dodajLiniju(" LOAD R6, (rez)");
			
			// kraj izvodenja
			dodajLiniju(" HALT");

			// mnozenje i dijelenje
			dodajLiniju("MD_SGN MOVE 0, R6");
			dodajLiniju(" XOR R0, 0, R0");
			dodajLiniju(" JP_P MD_TST1");
			dodajLiniju(" XOR R0, -1, R0");
			dodajLiniju(" ADD R0, 1, R0");
			dodajLiniju(" MOVE 1, R6");
			dodajLiniju("MD_TST1 XOR R1, 0, R1");
			dodajLiniju(" JP_P MD_SGNR");
			dodajLiniju(" XOR R1, -1, R1");
			dodajLiniju(" ADD R1, 1, R1");
			dodajLiniju(" XOR R6, 1, R6");
			dodajLiniju("MD_SGNR RET");
			dodajLiniju("MD_INIT POP R4");
			dodajLiniju(" POP R3");
			dodajLiniju(" POP R1");
			dodajLiniju(" POP R0");
			dodajLiniju(" CALL MD_SGN");
			dodajLiniju(" MOVE 0, R2");
			dodajLiniju(" PUSH R4");
			dodajLiniju(" RET");
			dodajLiniju("MD_RET XOR R6, 0, R6");
			dodajLiniju(" JP_Z MD_RET1");
			dodajLiniju(" XOR R2, -1, R2");
			dodajLiniju(" ADD R2, 1, R2");
			dodajLiniju("MD_RET1 POP R4");
			dodajLiniju(" PUSH R2");
			dodajLiniju(" PUSH R3");
			dodajLiniju(" PUSH R4");
			dodajLiniju(" RET");
			dodajLiniju("MUL CALL MD_INIT");
			dodajLiniju(" XOR R1, 0, R1");
			dodajLiniju(" JP_Z MUL_RET");
			dodajLiniju(" SUB R1, 1, R1");
			dodajLiniju("MUL_1 ADD R2, R0, R2");
			dodajLiniju(" SUB R1, 1, R1");
			dodajLiniju(" JP_NN MUL_1");
			dodajLiniju("MUL_RET CALL MD_RET");
			dodajLiniju(" RET");
			dodajLiniju("DIV CALL MD_INIT");
			dodajLiniju(" XOR R1, 0, R1");
			dodajLiniju(" JP_Z DIV_RET");
			dodajLiniju("DIV_1 ADD R2, 1, R2");
			dodajLiniju(" SUB R0, R1, R0");
			dodajLiniju(" JP_NN DIV_1");
			dodajLiniju(" SUB R2, 1, R2");
			dodajLiniju("DIV_RET CALL MD_RET");
			dodajLiniju(" RET");

			// inicijalizacija varijabli
			for (String varijabla : varijable) {
				dodajLiniju(varijabla + " DW 0 ");
			}

			Files.write(Paths.get("a.frisc"), kod.toString().getBytes());
			System.out.println(kod.toString());
		}
	}
}
