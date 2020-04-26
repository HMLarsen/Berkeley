package client;

import static common.AppConstants.formatter;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalTime;

import common.AppConstants;
import server.ServerTime;
import server.ServerTimeImpl;

/**
 * Client-side da aplica��o.
 */
public class MainBerkeley {

	public static void main(String[] args) {
		try {
			LocalTime localTime = LocalTime.parse(AppConstants.LOCAL_HOUR, formatter);
			System.out.println("Hor�rio local: " + formatter.format(localTime));

			// cria��o dos servidores (m�quinas)
			ServerTime machine1Server = createMachineServer(1);
			ServerTime machine2Server = createMachineServer(2);
			ServerTime machine3Server = createMachineServer(3);

			// calcular a m�dia das horas
			var avgDiff = generateAverageTime(localTime,
					machine1Server.getLocalTime(),
					machine2Server.getLocalTime(),
					machine3Server.getLocalTime());

			// ajustar o tempo dos servidores
			machine1Server.adjustTime(localTime, avgDiff);
			machine2Server.adjustTime(localTime, avgDiff);
			machine3Server.adjustTime(localTime, avgDiff);
			localTime = localTime.plusNanos(avgDiff);

			System.out.println("\nHor�rios atualizados!");
			System.out.println("Hor�rio local: " + formatter.format(localTime));
			System.out.println("Hor�rio servidor 1: " + formatter.format(machine1Server.getLocalTime()));
			System.out.println("Hor�rio servidor 2: " + formatter.format(machine2Server.getLocalTime()));
			System.out.println("Hor�rio servidor 3: " + formatter.format(machine3Server.getLocalTime()));
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	/**
	 * Cria um {@link ServerTime} que est� associado a uma m�quina para ter sua hora
	 * ajustada.
	 * 
	 * @param machineNumber n�mero da m�quina
	 * @return servidor da m�quina com sua hora
	 * @throws Exception ao tentar criar o servidor ou registro
	 */
	private static ServerTime createMachineServer(int machineNumber) throws Exception {
		String serverName = AppConstants.SERVER_NAME;
		int serverPort = switch (machineNumber) {
			case 1 -> AppConstants.SERVER_PORT_1;
			case 2 -> AppConstants.SERVER_PORT_2;
			case 3 -> AppConstants.SERVER_PORT_3;
			default -> -1;
		};
		Registry machineRegistry = LocateRegistry.getRegistry(serverName, serverPort);
		ServerTime machineServerTime = (ServerTime) machineRegistry.lookup(ServerTimeImpl.class.getSimpleName());
		LocalTime machineTime = machineServerTime.getLocalTime();
		System.out.println("Conex�o com a m�quina " + machineNumber + " estabelecida com sucesso. Hora: "
				+ formatter.format(machineTime));
		return machineServerTime;
	}

	/**
	 * Calcula a m�dia da hora que deve ser ajustada.<br>
	 * Hora somada das m�quinas (cada uma subtra�da pela hora local) dividida pelo
	 * total de m�quinas.
	 * 
	 * @param localTime hora local
	 * @param times     hora das m�quinas
	 * @return hora m�dia calculada
	 */
	private static long generateAverageTime(LocalTime localTime, LocalTime... times) {
		long nanoLocal = localTime.toNanoOfDay();
		long difServer = 0;
		for (LocalTime t : times) {
			difServer += t.toNanoOfDay() - nanoLocal;
		}
		return difServer / times.length;
	}

}