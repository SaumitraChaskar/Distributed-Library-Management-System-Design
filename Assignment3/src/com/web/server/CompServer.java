package com.web.server;

import javax.xml.ws.Endpoint;

import com.web.service.impl.Implementation;

import java.net.DatagramSocket;
import java.util.Scanner;

public class CompServer {

	public static Implementation implementation;

	public static void main(String[] args) {
		System.out.println("Comp Server Started...");
		implementation = new Implementation();


		int udpPortNum = 0;
		int addr = 0;
		String campus;
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter Campus");
		campus = sc.nextLine().toUpperCase();

		try{
			switch (campus) {
				case "CON":
					udpPortNum = 2234;
					addr = 8080;
					break;
				case "MCG":
					udpPortNum = 2235;
					addr = 8081;
					break;
				case "MON":
					udpPortNum = 2236;
					addr = 8082;
					break;
				default:
					System.err.println("Server started failed");
					System.exit(42);
			}

			System.out.println("DLMS ready and waiting ...");
			DatagramSocket serversocket = new DatagramSocket(udpPortNum);
			startListening(campus, implementation, serversocket);
			implementation.StartServer(campus);
			Endpoint endpoint = Endpoint.publish("http://localhost:"+addr+"/comp", implementation);

		}
		catch (Exception re) {
			System.out.println("Exception in Server.main: " + re);
		}
	}

	private static void startListening(String campusName, Implementation campusSever, DatagramSocket SeverSocket) {

		String threadName = campusName + "listen";
		Listening listen = new Listening(threadName, SeverSocket, campusSever);
		listen.start();
	}
}
