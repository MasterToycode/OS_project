import javax.swing.*;

public class ClockInterruptHandlerThread extends Thread
{
    private  UI ui;  // UI的引用
    public static int simulationTime = 0;  // 共享时间变量
    public static int milliseconds = 1000;

    // 构造函数接受UI对象
    public ClockInterruptHandlerThread(UI ui) {
        this.ui = ui;
    }

    @Override
    public void run()
    {
        while (true)
        {
            // 检查是否暂停
            SyncManager.pauseLock.lock();
            try {
                while (SyncManager.isPaused) {
                    SyncManager.pauseCondition.await();  // 暂停时进入等待
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                SyncManager.pauseLock.unlock();
            }


            SyncManager.lock.lock();
            SyncManager.lock1.lock();
            try
            {
                if (!SyncManager.pst_clk) SyncManager.pst_clk_Condition.await();
                SyncManager.pst_clk=false;
                if (!SyncManager.io_clk) SyncManager.io_clk_Condition.await();
                SyncManager.io_clk=false;
                //在这里，必须要保证每一次都是进程调度线程和IO中断线程先启动，之后才是时钟线程启动驱动仿真程序
                //时钟走完一秒，线程只能在1秒时间内做1秒钟的事情
                simulateTimePassing(milliseconds);//时钟走1秒
                // 更新UI中的时钟显示
                SwingUtilities.invokeLater(()->ui.updateClock(simulationTime));
                JobRequest();
                SyncManager.pstCondition.signal(); // 通知进程调度线程
                SyncManager.ioCondition.signal();//通知IO线程
            }

            catch (Exception e) {
                e.printStackTrace();
            }

            finally {
                SyncManager.lock1.unlock();
                SyncManager.lock.unlock();

            }
        }
    }



    public static int getCurrentTime() {
        return simulationTime;
    }


    public static void simulateTimePassing(int milliseconds)//时钟计时器
    {
        try {
            Thread.sleep(milliseconds);
            simulationTime++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//用于仿真模拟时钟走一秒




        //查询判断是否有作业请求
        private void JobRequest()
        {
            if (ClockInterruptHandlerThread.getCurrentTime()%2==0)//每2秒查询是否有作业
            {
                if (!OSKernel.backupQueue.isEmpty())//如果作业缓冲区不为空
                {
                    for (int i=0;i<OSKernel.backupQueue.size();i++)
                    {
                        if (OSKernel.backupQueue.peek().getInTime() <= ClockInterruptHandlerThread.getCurrentTime())//作业加入进程缓冲区队列
                        {
                            Job poll = OSKernel.backupQueue.poll();
                            PCB pcb = new PCB(poll.getJobId(), poll.getInTime(), poll.getInstructionCount(), poll.getJobId(),poll.getInstructions());
                            OSKernel.back_pcbQueue.add(pcb);
                            String logEntry = poll.getInTime() + " [新增作业: 作业" + poll.getJobId() + "]";
                            System.out.println(logEntry);
                            SwingUtilities.invokeLater(()->ui.say_to_job_are(logEntry));// 调用UI的方法添加作业请求
                            OSKernel.fileUtils.collectLog(logEntry);//保存到文件中
                        }
                    }
                }
            }
        }

}