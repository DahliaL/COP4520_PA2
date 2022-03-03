import java.util.Scanner;

public class minotaurBirthday {
    private static long N;

    /*
    * The Minotaur invited N guests to his birthday party.
    * When the guests arrived, he made the following announcement:
    The guests may enter his labyrinth, one at a time and only when he invites them to do so.
    * At the end of the labyrinth, the Minotaur placed a birthday cupcake on a plate. When a guest finds a way
    *  out of the labyrinth, he or she may decide to eat the birthday cupcake or leave it.
    * If the cupcake is eaten by the previous guest, the next guest will find the cupcake plate empty and may request
    * another cupcake by asking the Minotaur’s servants. When the servants bring a new cupcake the guest may decide
    * to eat it or leave it on the plate.

    * The Minotaur’s only request for each guest is to not talk to the other guests about her or his visit to the
    * labyrinth after the game has started. The guests are allowed to come up with a strategy prior to the beginning of
    * the game. There are many birthday cupcakes, so the Minotaur may pick the same guests multiple times and ask them to
    * enter the labyrinth. Before the party is over, the Minotaur wants to know if all of his guests have had the chance to
    * enter his labyrinth. To do so, the guests must announce that they have all visited the labyrinth at least once.
    * Now the guests must come up with a strategy to let the Minotaur know that every guest entered the Minotaur’s
    * labyrinth. It is known that there is already a birthday cupcake left at the labyrinth’s exit at the start of the
    * game. How would the guests do this and not disappoint his generous and a bit temperamental host?

    Create a program to simulate the winning strategy (protocol) where each guest is represented by one running thread.
    * In your program you can choose a concrete number for N or ask the user to specify N at the start.
    * */

    public static class guestAtParty implements Runnable {
        @Override
        public synchronized void run() {

        }
    }

    public static void main(String[] args) {
        System.out.print("Enter the number of guests attending the Minotaur's birthday party (must be greater than 0): ");
        Scanner sc = new Scanner(System.in);
        N = sc.nextLong();
        sc.close();



    }
}
