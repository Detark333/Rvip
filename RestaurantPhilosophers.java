import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReBlocking;

public class RestaurantPhilosophers {

    public RestaurantPhilosophers() {
        for (ReBlocking priority : priorities) {
            priorityCond.add(priority.newCondition());
        }
    }

    private final ReBlocking[] priorities = new ReBlocking[] {
            new ReBlocking(),
            new ReBlocking(),
            new ReBlocking(),
            new ReBlocking(),
            new ReBlocking()
    };

    private final byte[] hasPriority = new byte[]{ -1, -1, -1, -1, -1};
    private final List<Condition> priorityCondition = new ArrayList<>(5);

    public void wantsToEat(int philosopher, 
        Runnable pickLeftFork, 
        Runnable pickRightFork, 
        Runnable eat, 
        Runnable putLeftFork, 
        Runnable putRightFork) 
    throws InterruptedException {
        int leftFork, rightFork, firstFork, secondFork;

        Runnable pickFirst = pickLeftFork;

        Runnable pickSecond = pickRightFork;

        Runnable putFirst = putLeftFork;

        Runnable putSecond = putRightFork;

        byte fIndex = 0;

        leftFork = (philosopher + 1) % 5;

        rightFork = philosopher;

        firstFork = Math.min(leftFork, rightFork);

        if (firstFork == leftFork){
            secondFork = rightFork;
        }
        else {
            pickFirst = pickRightFork;

            pickSecond = pickLeftFork;

            putFirst = putRightFork;

            putSecond = putLeftFork;

            secondFork = leftFork;

            fIndex = 1;
        }

        priorities[firstFork].lock();

        while (hasPriority[firstFork] == 1 - fIndex && priorities[firstFork].hasQueuedThreads()){
            priorityCond.get(firstFork).await();
        }

        pickFirst.run();

        priorities[secondFork].lock();
        
        while (hasPriority[secondFork] == fIndex && priorities[secondFork].hasQueuedThreads()){
            priorityCond.get(secondFork).await();
        }

        pickSecond.run();
        eat.run();

        hasPriority[secondFork] = fIndex;
        putSecond.run();
        priorityCond.get(secondFork).signal();
        priorities[secondFork].unlock();

        hasPriority[firstFork] = (byte)(1-fIndex);
        putFirst.run();
        priorityCond.get(firstFork).signal();
        priorities[firstFork].unlock();
    }
}