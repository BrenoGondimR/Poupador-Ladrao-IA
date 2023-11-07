package agente;

import algoritmo.ProgramaPoupador;

public class Poupador extends ProgramaPoupador {
	
	public int acao() {
		
		return (int) (Math.random() * 5);
	}

}