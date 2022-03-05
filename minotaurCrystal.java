import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

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

class QNode {
    volatile boolean locked = false;
    volatile QNode next = null;
}

class CLHLock implements Lock {
    AtomicReference<QNode> tail;
    ThreadLocal<QNode> myPred;
    ThreadLocal<QNode> myNode;

    public CLHLock() {
        tail = new AtomicReference<>(null);
        myNode = new ThreadLocal<QNode>() { 
            protected QNode initialValue() {
                return new QNode(); }
        };
        myPred = new ThreadLocal<QNode>() {
            protected QNode initialValue() { 
                return null;
            } };
    }
    
    public void lock() {
        QNode qnode = myNode.get();
        qnode.locked = true;
        QNode pred = tail.getAndSet(qnode);
        myPred.set(pred);
        while (pred.locked) {
            try {
                minotaurCrystal.goSeeVase();
            } catch (InterruptedException ignored) {
            }
        }
         }

    @Override
    public void lockInterruptibly() throws InterruptedException {}

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    public void unlock() {
        QNode qnode = myNode.get();
        qnode.locked = false;
        myNode.set(myPred.get());
    }

    @Override
    public Condition newCondition() {
        Condition o = null;
        return o;
    }
}

class MCSLock implements Lock {
    AtomicReference<QNode> queue;
    ThreadLocal<QNode> node;
    // queue: points to the tail
    // node:  is unique for each thread

    public MCSLock() {
        queue = new AtomicReference<>(null);
        node = new ThreadLocal<QNode>() {
            protected QNode initialValue() {
                return new QNode();
            }
        };
    }
    @Override
    public void lock() {
        QNode n = node.get();        
        QNode m = queue.getAndSet(n); 
        if (m != null) {   
            n.locked = true; 
            m.next = n;      
            while(n.locked) Thread.yield(); 
        } 
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        QNode n = node.get(); // 1
        if (n.next == null) {               // 2b
            if (queue.compareAndSet(n, null)) // 2b
                return;                         // 2b
            while(n.next == null) Thread.yield(); // 2c
        }                      // 2a
        n.next.locked = false; // 2a
        n.next = null;         // 2a
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}

public class minotaurCrystal {
    static Lock lock;
    private static int N;
    private static final int arb = 10;
    private static final int timeToLookAtVase = 100;
    static double[] sharedData;
    private static int curr;
    private static int count;
    private static boolean[] whoSawVase;


    public static class guestAtParty implements Runnable {
        @Override
        public void run() {
            if(!whoSawVase[curr]) {
                whoSawVase[curr] = true;
                count++;
            }

            try {
                if (lock.tryLock() ) {
                    lock.lock();
                }
            }
            finally {
                lock.unlock();
            }

            System.out.println("Guest " + curr + ": leaving");
        }
    }


    public static void goSeeVase() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1000);
        System.out.println("Guest " + curr + " has entered the showroom.");
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.print("Enter the number of guests that the Minotaur's is hosting (must be greater than 0): ");
        Scanner sc = new Scanner(System.in);
        N = sc.nextInt();

        boolean valid = false;
        while(!valid) {
            if(N > 0) {
                valid = true;
                sc.close();
            }
            else {
                System.out.println("N value must be greater than zero. Try again.");
                N = sc.nextInt();
            } }

        whoSawVase = new boolean[N];
        Arrays.fill(whoSawVase, false);
        count = 0;

        lock = new CLHLock();
        sharedData = new double[100];

        guestAtParty guestAtParty = new guestAtParty();

        System.out.println("Starting " + N + " threads ...");
        Thread[] guests = new Thread[N];


        while(count < N) {
            curr = ThreadLocalRandom.current().nextInt(0, (int)N); // simulating a random guest getting in line
            
            guests[curr] = new Thread(guestAtParty);
            System.out.println("Guest " + curr + " has joined the queue.");
            guests[curr].start();
        }

        System.out.println("All guest have seen the vase, so the Minotaur is closing the queue.");

        try {
            for(int i=0; i<N; i++)
                guests[i].join();
        }
        catch(NullPointerException | InterruptedException ignored) {}

    }
}
