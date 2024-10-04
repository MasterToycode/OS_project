
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;



public class OSKernel
{
    public static UI ui = new UI(); // 全局UI实例
    public static CPU cpu = new CPU(ui);  // 全局CPU实例
    public static Memory memory = new Memory(new MMU(),ui);  // 全局内存实例
    public static FileUtils fileUtils=new FileUtils();//全局文件处理,信息保存,输入输出的对象
    public static Queue<Job> backupQueue = new LinkedList<>(); //缓冲区，硬盘，存放作业
    public static Queue<PCB> back_pcbQueue=new LinkedList<>();//进程缓冲区队列
    public static Queue<PCB> readyQueue1=new LinkedList<>();//一级就绪队列
    public static Queue<PCB> readyQueue2=new LinkedList<>();//二级就绪队列
    public static Queue<PCB> readyQueue3=new LinkedList<>();//三级就绪队列
    public static final int TIME_SLICE_1 = 1;  // 一级队列时间片 1 秒
    public static final int TIME_SLICE_2 = 2;  // 二级队列时间片 2 秒
    public static final int TIME_SLICE_3 = 4;  // 三级队列时间片 4 秒
    public static Queue<PCB> I_block_queue=new LinkedList<>();//进程的输入阻塞队列
    public static Queue<PCB> O_block_queue=new LinkedList<>();//进程的输出阻塞队列
    public static HashMap<Integer, List<String>> log_I_block = new HashMap<>(); // 输入阻塞队列
    public static HashMap<Integer, List<String>> log_O_block = new HashMap<>(); // 输出阻塞队列

    public static boolean check_all_the_job(){
        return (OSKernel.backupQueue.isEmpty()&&ClockInterruptHandlerThread.getCurrentTime()!=0&&back_pcbQueue.isEmpty()&&readyQueue3.isEmpty()&&readyQueue2.isEmpty()&&readyQueue1.isEmpty()&&I_block_queue.isEmpty()&&O_block_queue.isEmpty());
    }//判断是否所有的作业都已经完成
}
