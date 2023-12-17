package com.corona.cowinslotnotifier;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		// System.out.println( "Hello World!" );
		try {
			SlotFinder slotFinder = new SlotFinder();
			while (true) {
				System.out.println("Running...");
				slotFinder.findSlotsInTheDistrict();
				Thread.sleep(60000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
