package com.lb.telnet.telnetjob;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.net.telnet.TelnetClient;

public class LBTestTelnetClient {

	private TelnetClient telnet = new TelnetClient();
	private BufferedInputStream input;
	private PrintStream output;
	private static int waitTime;
	public static String servers[] = { "cmh-nexus-001.lbidts.com", "cmh-db-001.lbidts.com", "ket-db-001.lbidts.com",
			"cmh-svn-001.lbidts.com", "cmh-cohnwspd-001.lbidts.com", "cmh-vsdpcohint-001.lbidts.com",
			"cmh-vsdpjetpd01-001.lbidts.com", "cmh-db2connect-001.lbidts.com", "ket-vssdbd-001.lbidts.com",
			"ket-cohnwsdev05-001.lbidts.com", "ket-vsfdbqa-001.lbidts.com", "ket-delpd-001.lbidts.com",
			"cmh-cdbviewpd-001.lbidts.com", "ket-vsdpjetpd01-001.lbidts.com", "cmh-cohbbwetpd-001.lbidts.com",
			"cmh-tc-001.lbidts.com" };

	public LBTestTelnetClient() {
		super();
	}

	private boolean connect(String IPReader, String userName, String password) {
		try {

			if (telnet != null && telnet.isConnected()) {
				telnet.disconnect();
			}
			if (input != null) {
				input.close();
			}
			if (output != null) {
				output.flush();
				output.close();
			}

			telnet.setConnectTimeout(waitTime);
			telnet.connect(IPReader, 23);

			input = new BufferedInputStream(telnet.getInputStream());
			output = new PrintStream(telnet.getOutputStream());

			Thread.sleep(waitTime);

			if (readUntil("Username: ") == null)
				return false;
			write(userName);
			Thread.sleep(waitTime);
			if (readUntil("Password: ") == null)
				return false;
			write(password);
			Thread.sleep(waitTime);

			String final_telnet_response = readUntil("Authentication: Accepted");
			Thread.sleep(waitTime);
			if (final_telnet_response == null) {
				System.out.println("Firewall User Authentication: " + "Failed");
				return false;
			} else
				System.out.println("Final Response" + final_telnet_response);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// Firewall User Authentication: Accepted

	private String readUntil(String pattern) {
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			StringBuffer sb = new StringBuffer();
			int numRead = 0;

			if (input.available() <= 5) {
				return null;
			}
			char ch = (char) input.read();

			while (true) {
				
				numRead++;
				sb.append(ch);
				if (ch == lastChar) {
					//System.out.println("The telnet Reponse before final check :"+sb.toString());
					if (sb.toString().endsWith(pattern)) {
						System.out.println("TelnetResponse :");
						System.out.println(sb.toString());
						return sb.toString();
					}
				}

				if (input.available() == 0) {
					break;
				}
				ch = (char) input.read();

				if (numRead > 2000) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void write(String value) {
		try {
			output.println(value);
			output.flush();
			// System.out.println(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void disconnect() {
		try {
			telnet.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		try {
			waitTime = Integer.parseInt(args[2]);
			// for(String server: servers){
			// LLRPmonitor llrpMonitor = new LLRPmonitor();
			// System.out.println("Telnet : Connecting to "+ server);
			// boolean flag = llrpMonitor.connect(server,"sbarik","******");
			// System.out.println("The IP : "+server+ ": Connected :"+flag);
			// llrpMonitor.disconnect();
			// System.out.println("** Disconnected **"+ server);
			// }
			for (String server : servers) {
				LBTestTelnetClient llrpMonitor = new LBTestTelnetClient();
				System.out.println("Telnet : Connecting to " + server);
				boolean flag = llrpMonitor.connect(server, args[0]+"\r", args[1]+"\r");
				Thread.sleep(llrpMonitor.waitTime);
				System.out.println("The IP : " + server + ": Connected :" + flag);
				llrpMonitor.disconnect();
				Thread.sleep(llrpMonitor.waitTime);
				System.out.println("** Disconnected **" + server);
				System.out.println("***********************************************");
				Thread.sleep(llrpMonitor.waitTime);
			}

		} catch (Exception e) {
			System.out.println("Exception Occurred while connecting :");
			e.printStackTrace();
		}

	}
}