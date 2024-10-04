import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 同步管理类，包含锁、条件变量
 */
public class SyncManager
{
    // 共享锁
    public static final Lock lock1 = new ReentrantLock();//进程调度的锁
    public static final Lock lock=new ReentrantLock();//IO的锁
    // 条件变量
    public static final Condition pstCondition = lock1.newCondition();//进程调度的条件变量
    public static final Condition pst_clk_Condition = lock1.newCondition();//进程调度的条件变量
    public static final Condition ioCondition = lock.newCondition(); // IO操作的条件变量
    public static final Condition io_clk_Condition = lock.newCondition(); // IO操作的条件变量
    public static  boolean io_clk=false;//确保IO线程一定在时钟之前拿到锁
    public static  boolean pst_clk=false;//确保进程调度线程一定在时钟之前拿到锁

    // 暂停控制锁和条件变量
    public static final Lock pauseLock = new ReentrantLock();
    public static final Condition pauseCondition = pauseLock.newCondition();
    public static boolean isPaused = false; // 是否暂停
    public static boolean isStarted = false; // 仿真程序是否已经启动

}