import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class minotaurBirthday {
    private static long N;
    private static boolean[] visited;
    private static boolean plateWithCupcake = true;
    private static int leaderNum;
    private static int count = 0;
    private static int curr;

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
    public static boolean verifyEnd() {
        boolean valid = true;
        for (boolean b : visited) {
            if (!b) {
                System.out.println("The Minotaur is not satisfied!");
                valid = false;
                break;
            }
        }

        return valid;
    }

    public static void eatOrReplace(int guestID) {
        if(!visited[guestID] && plateWithCupcake) { // if this guest has never been in and the plate has a cupcake..
            visited[guestID] = true;
            plateWithCupcake = false;
        }
        // in every other case, the guest should not do anything else... unless...
        if(guestID == leaderNum) { // if this guest is the designated leader, they need to keep track!
            if(!plateWithCupcake) { // someone ate the cupcake lol.. new guests came in, we need to increment
                count++;
                plateWithCupcake = true; // replacing the cupcake
            }
        }
    }

    public static class guestAtParty implements Runnable {
        @Override
        public synchronized void run() {
            eatOrReplace(curr);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.print("Enter the number of guests attending the Minotaur's birthday party (must be greater than 1): ");
        Scanner sc = new Scanner(System.in);
        N = sc.nextLong();
        sc.close();

        visited = new boolean[(int)N];
        Arrays.fill(visited, false);

        // after some deliberating, the guests agree that they should delegate a leader
        leaderNum = ThreadLocalRandom.current().nextInt(0, (int)N-1); // choosing a random guest to be the leader

        guestAtParty guestAtParty = new guestAtParty();
        Thread[] guests = new Thread[(int)N];

        // the guest are ready to play!
        int entries = 0; // this is for my own reason to verify that around n^2 entries happen

        while(count < N) {

            try {
                guests[curr].join();}
            catch(NullPointerException | InterruptedException ignored) {
            }

            curr = ThreadLocalRandom.current().nextInt(0, (int)N); // simulating a random guest getting chosen to enter

            guests[curr] = new Thread(guestAtParty);
           // System.out.println(guests[curr].getName() + "entering...");
            guests[curr].start();

            entries++;
        }

        System.out.println("\nThe guests have decided to tell the Minotaur that all guests have entered the labyrinth. Is he satisfied?...");

        if(verifyEnd())
            System.out.println("\nThe Minotaur is satisfied!\n\nThis took " + entries + " entries from the guests.");

        if(Math.pow(N, 2) > entries)
            System.out.println("The time complexity matches the expected O(N^2). Whoo!");
        else
            System.out.println("The time complexity is a bit above the expected O(N^2). It is " + (entries-Math.pow(N, 2)) + " more than the N^2.");

    }
}
