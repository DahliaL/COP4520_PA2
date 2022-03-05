import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
* The Minotaur decided to show his favorite crystal vase to his guests in a dedicated
* showroom with a single door. He did not want many guests to gather around the vase and
* accidentally break it. For this reason, he would allow only one guest at a time into the
* showroom. He asked his guests to choose from one of three possible strategies for viewing
* the Minotaur’s favorite crystal vase:
*
* 1) Any guest could stop by and check whether the showroom’s door is open at any time and
* try to enter the room. While this would allow the guests to roam around the castle and enjoy the party,
* this strategy may also cause large crowds of eager guests to gather around the door. A particular guest
* wanting to see the vase would also have no guarantee that she or he will be able to do so and when.

2) The Minotaur’s second strategy allowed the guests to place a sign on the door indicating when the showroom
* is available. The sign would read “AVAILABLE” or “BUSY.” Every guest is responsible to set the sign to “BUSY”
* when entering the showroom and back to “AVAILABLE” upon exit. That way guests would not bother trying to go to
* the showroom if it is not available.

3) The third strategy would allow the quests to line in a queue. Every guest exiting the room was responsible
* to notify the guest standing in front of the queue that the showroom is available. Guests were allowed to
* queue multiple times.

* */

class ShowroomQueue {
    private final Lock queueLock = new ReentrantLock();

    public void letGuestIn() {
        queueLock.lock();
        try {
            long duration = (long) (Math.random() * 100); // giving each guest time to admire the vase :) 
            //System.out.println(Thread.currentThread().getName() + " has entered the showroom.");
            Thread.sleep(duration);
        } catch (InterruptedException ignored) {}
        finally {
            //System.out.println(Thread.currentThread().getName() + " is leaving the showroom");
            queueLock.unlock();
        }
    }

    public int getCurrentGuest() {
        String temp = Thread.currentThread().getName();

        if(temp.equals("main")) // we dont need main
            return -1;

        temp = temp.substring(temp.indexOf(' ')+1); // getting thread #

        return Integer.parseInt(temp);
    }
}

class guestInLine implements Runnable {
    private final ShowroomQueue showroomqueue;

    public guestInLine(ShowroomQueue showroom)
    {
        this.showroomqueue = showroom;
    }

    @Override
    public void run() {
        //System.out.println(Thread.currentThread().getName() + " has joined the queue.");
        showroomqueue.letGuestIn();
    }
}

public class minotaurVase {
    public static void main(String[] args) throws InterruptedException {
        System.out.print("Enter the number of guests that the Minotaur is hosting (must be greater than 1): ");
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();

        boolean valid = false;
        while(!valid) {
            if(n > 1) {
                valid = true;
                sc.close();
            }
            else {
                System.out.println("N value must be greater than one. Try again.");
                n = sc.nextInt();
            } }

        int[] whoSawVase = new int[n];
        Arrays.fill(whoSawVase, 0);
        int count = 0;
        int prev = 0;

        System.out.println("\nThe crystal vase viewing is starting!\n");
        Thread[] guests = new Thread[n];
        ShowroomQueue line = new ShowroomQueue();

        int entries = 0;
        int reduced = n-1;
        while(count < n) {
            int curr = ThreadLocalRandom.current().nextInt(0, n);

            while(line.getCurrentGuest() == curr || curr == prev || whoSawVase[curr] >= reduced) {
                if(reduced-1 > 1 )
                    reduced -= 1;
                curr = ThreadLocalRandom.current().nextInt(0, n); // simulating a random guest getting in line
            }

            if(whoSawVase[curr] == 0) {
                count++;
                whoSawVase[curr]++;
            }

            guests[curr] = new Thread(new guestInLine(line), "Guest " + curr);
            guests[curr].start();
            entries++;

            prev = curr;
        }

        System.out.println("All guest have seen the vase, so the Minotaur is closing the queue.");
        System.out.println("Waiting for the remaining guests in line...");

        // just making sure all the threads finish..
        try {
            for(int i = 0; i < n; i++)
                guests[i].join();
        }
        catch(NullPointerException ignored) {}

        System.out.println(n + " guests were able to see the vase a total of " + entries + " times.");

    }
}
