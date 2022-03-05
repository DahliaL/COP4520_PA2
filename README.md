# Dahlia's Minotaur Party PA2

[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)
My submission for COP4520 Programming Assignment 2.

# How To Run
In the command prompt/terminal, make sure you are in the correct directory, where ```minotaurBirthday.java``` and ```minotaurVase.java``` are located. Then write the following commands depending on which program you wish to run:
##### Problem 1 Execution
```
  javac minotaurBirthday.java
  java minotaurBirthday
```
##### Problem 2 Execution
```
  javac minotaurVase.java
  java minotaurVase
```
Both programs will prompt you to input a value n, the number of guests that the Minotaur will be hosting.

# Summary of Approach and Efficiency of Design
#### Problem 1: Minotaur Birthday Party
At the Minotaur's Birthday Party, every guest eating exactly one cupcake is the key to my approach.

The guests deliberate and decide to designate a leader. This leader has two responsibilities:
1. resetting the state (in this case, asking for a replacement cupcake and leaving it)
2. keeping count of how many of the guests have entered the maze

This means that the leader is only person who will ever ask the servants to replace an empty plate with a cupcake. The rest of the guests will never ask the servants to replace an empty plate. This is extremely important.

Since the guests know the initial state of the plate in the labryrinth, they decide that eating the cupcake will symbolize/note to the leader that somebody has entered the labryinth that wasn't counted before. Each guest should eat just one cupcake because of this, as they cannot ruin the count for the leader.

If a guest enters the maze, and finds a cupcake available, they will:
1. eat the cupcake if they haven't before
2. do nothing and leave if they already enjoyed a cupcake

If a guest enters the maze and finds no cupcake, they have only one option (if they are not the leader):
- do nothing and leave, despite if they ate a cupcake or not

If the guest that enters the maze is the leader, and finds no cupcake, they must:
1. add another person to their count
2. ask to have the cupcake replaced

The leader knows that there are N guests at the party and once their count reaches N (if they want to count themselves eating the cupcake), they know to notify the Minotaur that everyone has entered the maze!

Because the leader is just one person, and they themself are randomly chosen to go into the maze as well, this algorithm needs to run about O(N^2) times. This is to ensure that every person actually got to visit the maze at least once, and that they are accounted for in the leaders count.

My program follows the explained algorithm. It simulates a random guest being chosen with ThreadLocal.Random. Then a while loop commences, iteratively checking for if a count variable that only the leader thread controls is < N. Inside of the while loop, a random guest is chosen and we gain access to the unique guest thread by an array of threads. The thread runs, simulating the maze visit exactly as described in my approach. The loop continues like so until the leaders count is equivalent to N. From there, the program verifies that ever guest has visited the maze, with verifyEnd() (explained below) and returns to the user how many times the guests altogether entered the maze, essentially telling the user the runtime of the algorithm.

#### Problem 2: Minotaur Crystal Vase
All of the provided algorithms are correct and get the job done. It's just a matter of what we are willing to sacrifice here...
- Algorithm 1 is similar to a TASLock. It would yield high contention (large crowds of people trying to get in at once). If there was a time limit on how long the guests had to view the vase, algorithm #1 would not be starvation-free. Since every guest would be trying to acquire the showroom all at once, and the quickest guest would win the showroom spot, we cannot guarantee that every guest will be given the opportunity to see the crystal vase. That's a bummer...
- Algorthm 2 is an improved step from Algorithm #1, similar to the TTAS lock. It reduces contention because guests will not attempt to acquire the showroom if the sign says 'BUSY.' This means the large crowds won't be a problem, but it falls short when a guest leaves and turns the sign to 'FREE.'' The guests see that the guest who had the showroom before left, and thus, they all must check to see the updated sign (this would be our cache misses, and now they have invalid accounts of the showroom's state) and thus, the crowds happen regardless since they all see the lock is free and attempt to obtain it. This means the performance isn't too different from algo 1.
- Algorithm 3 is, again, an improvement from the last. It is essential a queue lock. Every guest attempting to get in is guaranteed to acquire the showroom eventually, contention will be at an all-time low since there is a queue for who gets the showroom next and the previous guest will notify the next guest that it's their turn, and fairness is promised. This algorithm seems like it would scale well. It's only seeable disadvantage would be if a guest never leaves the showroom (a thread is suspended or sleeps for a veryyyy longgg time), delaying the queue and increasing latency of those in line.

Of the given algorthms, I chose to implement #3.

I did this by creating a ShowroomQueue object that essentially controls the locking mechanism. I made use of Java's ReentrantLock, which only allows one thread to have hold of the lock at once, and once a thread is done, it sends the lock access down to the next thread that wanted the lock in FIFO fashion.

The most important thing is that this algorithm is deadlock-free and starvation free, guaranteeing that every guest that wants to see the vase will.

A while loop goes through until every guest has seen the vase. The loop chooses a random guests and adds them to the queue. It checks for if the guest has been in before, for loop exit condition purposes, and carries on. At the where the while loop ends, any guests attempting to rejoin the queue is not allowed to, and the queue is allowed to finish with the guests that are still in line.

# Optimization
I noticed that for problem 2, guests that were currently in the showroom, would join the queue simultaneously, and so, in an attempt to eradicate that whole unrealistic scenario (because truly, how would this happen in real life...), I made it a point to check the guest that was in the showroom before, and made sure that the next random guest was not the same one. This seemed to help eliminate the impossibility of the scenario.

I also noticed that some guests were entering the queue a ton of times, upwards of above N. I instilled a cap of N-1 times that each guest could see the vase, which directly increases the chances of the guests who haven't seen the vase to get randomly pushed into the queue.


# Experimental Evaluation
I created a couple of methods to verify the correctness of both of my approaches.
###### Problem 1
```minotaurBirthday.java``` has a verifyEnd() method that verifies the correctness of the algorithm by running through a boolean array to check if every value is true. Each guest has their own index in the array and their respective value is only made true if they have not eaten a cupcake before.

Alongside verifyyEnd(), I make it a point to compare the "runtime" (# of times the guests entered the maze as a collective) to N^2, to check if the value is relatively around N^2. The results I received are below, but keep in mind this is subject to check on every run that occurs, since there is a randomizer.

The results were as follows:
| n     | # of Guest Entries | O(N^2)      |
| :---        |    :----:   |         :----: |
| 2      | 5       |  4  |
| 10  | 142        | 100      |
| 25     | 613       | 625   |
| 50      | 2666       | 2500   |
| 100     | 10203       | 10000   |
| 150     | 22312       | 25000   |

###### Problem 2
This program was a bit different to evaluate correctness, but I have commented out print statements that listed out the guest who was entering the queue, who was entering the showroom, and who was leaving. On a small scale, with about 10 guests, this was easy to verify. Because of my prints, I was able to notice some issues that I discussed under optimizations and make some tweaks to this algorithm. It's not as evident as problem 1's evaluation was, but it works.

This was how I checked for efficiency and correctness!

#### Citations & Credits
###### Random Number Generation:
https://www.javatpoint.com/how-to-generate-random-number-in-java

