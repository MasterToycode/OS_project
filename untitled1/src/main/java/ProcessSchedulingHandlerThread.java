import javax.swing.*;
public class ProcessSchedulingHandlerThread extends Thread
{
    public UI ui;
    public static final int DEFAULT_TIME_SLICE = 1; // 默认的轮转调度时间片
    public  int timeSlice; // 当前时间片
    public ProcessSchedulingHandlerThread(UI ui) {
        timeSlice = DEFAULT_TIME_SLICE;
        this.is_time_used=true;// 默认当前时间片已经使用完
        this.ui=ui;
    }



    public   boolean is_time_used;//判断当前时间片是否用完
    //默认当前时间片用完了


    @Override
    public void run() {
        while (true) { // 无限循环

            SyncManager.lock1.lock(); // 获取锁，确保线程安全
            try {
                SyncManager.pst_clk=true;
                SyncManager.pst_clk_Condition.signal();
                // 等待线程调度信号
                SyncManager.pstCondition.await();
                runPCB();//检查能否创建进程并开始调度进程
            } catch (Exception e) {
                e.printStackTrace(); // 处理异常
            } finally {
                SyncManager.lock1.unlock(); // 释放锁
            }
        }
    }




    public void runPCB(){
        if(!OSKernel.back_pcbQueue.isEmpty()){
        for (int i=0;i<OSKernel.back_pcbQueue.size();i++) {
            if (create_pcb(OSKernel.back_pcbQueue.peek())) {
                PCB pcb = OSKernel.back_pcbQueue.poll();
                String s=ClockInterruptHandlerThread.getCurrentTime() +" [创建进程：进程"+pcb.getPid()+"分配内存成功，内存块起始地址为："+pcb.getPysical_address()+"内存大小为："+pcb.cal_size()+"B,进入就绪队列1,待执行指令条数为："+(pcb.getInstructionCount()-pcb.getPc())+"]";
                System.out.println(s);
                SwingUtilities.invokeLater(()->ui.updateReadyQueue(s));
                OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime() +" [创建进程：进程"+pcb.getPid()+"分配内存成功，内存块起始地址为："+pcb.getPysical_address()+"内存大小为："+pcb.cal_size()+"B,进入就绪队列1,待执行指令条数为："+(pcb.getInstructionCount()-pcb.getPc())+"]");
                }
        }
        }
        MFQ();
        if(OSKernel.readyQueue1.isEmpty() && OSKernel.readyQueue2.isEmpty() && OSKernel.readyQueue3.isEmpty())//如果所有的就绪队列都为空，CPU空闲
        {System.out.println(ClockInterruptHandlerThread.getCurrentTime() +" [CPU空闲]");
            SwingUtilities.invokeLater(()->ui.updateRunningProcess(ClockInterruptHandlerThread.getCurrentTime() +" [CPU空闲]"));
        OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime() +" [CPU空闲]");
        }
    }




    public  void MFQ()
    {
        //多级反馈队列的处理逻辑
        //三个时间片，三个就绪队列，三种指令类型
        //第一级就绪队列，时间片1秒，第二集2秒，第三级4秒，三种指令，0，1，2.
        //0为计算类，不能中断，1代表输入，2代表输出，io操作
        //第一级就绪队列的级别最高，以此类推

        //每一级就绪队列现来先服务
        //如果是中断1 ，2 类型的指令，进入阻塞队列，在另一个并发的线程中处理,这个具体逻辑不需要在这里实现，另外一个线程中已经有了
        //处理完之后，返回对应的就绪队列继续做。这个逻辑已经有了，在另外的一个线程中
        //当存在进程被中断的时候，进程调度算法可以就绪队列中的进程
        //虽然时钟的1秒，就是进程调度线程只能做1秒的事情，但是如果某个当前的进程是在多余1秒的时间片下，必须把该时间片走完才能处理之后的逻辑
        //进程一级一级的下降，直到做完
        //0型的指令做1秒。1，2类型的指令做2秒做完
        //总是先做时间片短的队列，比如第二级队列中的一个进程刚做完吧2秒，但是，下一秒来了一个新的进程进入第一级，就要先做第一级的
        //新的进程，就是新创建的进程必须进入第一级的就绪队列，创建进程的逻辑不需要在这里实现
        //进程在对应时间片的队列中没有做完作业，就一直降级，知道降到最低级做完，
        //这个函数只对在就绪队列中的进程操作，不需要考虑就绪队列为空的情况
        while(!OSKernel.readyQueue1.isEmpty() || !OSKernel.readyQueue2.isEmpty() || !OSKernel.readyQueue3.isEmpty()) {
                switch (select_ready_queue())
                {
                    case 1:
                    {OSKernel.cpu.setCurrentProcess(OSKernel.readyQueue1.peek());
                        break;}
                    case 2:
                    {OSKernel.cpu.setCurrentProcess(OSKernel.readyQueue2.peek());
                        break;}
                    case 4:
                    {OSKernel.cpu.setCurrentProcess(OSKernel.readyQueue3.peek());
                        break;}
                    default: {OSKernel.cpu.setPsw(-1);break;}
                }
                OSKernel.cpu.runProcess();
                if(OSKernel.cpu.getPsw()==0) { pcb_is_finised(OSKernel.cpu.getCurrentProcess()); break;}//调度线程只能工作1秒一次，因为一条计算指令用时一秒，所以需要跳出循环
                if(OSKernel.cpu.getPsw()==1) is_time_used=true;//因为发生了IO阻塞，重新选择新的进程执行非计算类指令
        }
    }





    private int select_ready_queue()
    {
        if(is_time_used){
            if(!OSKernel.readyQueue1.isEmpty()) timeSlice=OSKernel.TIME_SLICE_1;
            else if (!OSKernel.readyQueue2.isEmpty()) timeSlice=OSKernel.TIME_SLICE_2;
            else timeSlice=OSKernel.TIME_SLICE_3;
            return timeSlice;
        } else if (!is_time_used)
        {
            return timeSlice;
        }
        return DEFAULT_TIME_SLICE;
    }//根据当前时间片是否用完，获取进程的对应的时间片。
    //因为时钟是1秒停止一次检查，但是如果时间片比1秒长，该进程必须把时间片用完才行





    private void isfinised(){
        if(OSKernel.cpu.getCurrentProcess().getCal_pc()==1){
            is_time_used=true;//进程在第一级队列中已经执行了一秒，时间片用完了。
            downgradeProcess(OSKernel.cpu.getCurrentProcess());
        } else if (OSKernel.cpu.getCurrentProcess().getCal_pc()==3) {
            is_time_used=true;//进程在第二级队列中已经执行了二秒，时间片用完了。
            downgradeProcess(OSKernel.cpu.getCurrentProcess());
        } else if (OSKernel.cpu.getCurrentProcess().getCal_pc()>3) {
                int count=OSKernel.cpu.getCurrentProcess().getCal_pc()-3;
                is_time_used= count % OSKernel.TIME_SLICE_3 == 0;
                if(is_time_used) downgradeProcess(OSKernel.cpu.getCurrentProcess());
        }//当进程已经在第三级队列中，使用完时间片但是进程还没结束，说明还要继续做
        else is_time_used=false;
    }//判断当前时间片是否用完





    private boolean pcb_is_finised(PCB pcb){
        if(pcb.getPc()>=pcb.getInstructionCount())
        {//如果进程执行结束
            pcb.setTatalruntimes(ClockInterruptHandlerThread.getCurrentTime());
            pcb.setState(-1);
            OSKernel.memory.freemem(pcb);
            String s=ClockInterruptHandlerThread.getCurrentTime()+ " [终止进程：进程"+pcb.getPid()+" 执行结束,"+"一共耗时："+(pcb.getTatalruntimes()-pcb.getInTime())+"。内存释放成功]";
            System.out.println(s);
            SwingUtilities.invokeLater(()->OSKernel.ui.updateRunningProcess(s));//更新进程结束的信息至UI界面
            OSKernel.fileUtils.collectLog(s);
            switch (pcb.getTimesilve()){
                case 1:{OSKernel.readyQueue1.remove(pcb);break;}
                case 2:{OSKernel.readyQueue2.remove(pcb);break;}
                case 4:{OSKernel.readyQueue3.remove(pcb);break;}
            }
            OSKernel.fileUtils.printBlockQueueLogs();//进程运行结束，打印进程的状态信息
            return true;
        }
        isfinised();
        return false;
    }//判断进程是否执行完毕





    // 将进程降级到下一优先级队列
    private void downgradeProcess(PCB process) {
        if (OSKernel.readyQueue1.contains(process)) {
            OSKernel.readyQueue1.remove(process);
            // 从一级队列降到二级队列
            OSKernel.readyQueue2.add(process);
            System.out.println(ClockInterruptHandlerThread.getCurrentTime()+ " [进程" + process.getPid()+ " 降级到二级队列]");
            SwingUtilities.invokeLater(()->ui.updateReadyQueue(ClockInterruptHandlerThread.getCurrentTime()+ " [进程" + process.getPid()+ " 降级到二级队列]"));
            process.setTimesilve(OSKernel.TIME_SLICE_2);//重新设置进程的时间片
            OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime()+ " [进程" + process.getPid()+ " 降级到二级队列]");
        }
        else if (OSKernel.readyQueue2.contains(process)) {
            // 从二级队列降到三级队列
            OSKernel.readyQueue2.remove(process);
            OSKernel.readyQueue3.add(process);
            System.out.println(ClockInterruptHandlerThread.getCurrentTime()+" [进程" + process.getPid()+ " 降级到三级队列]");
            SwingUtilities.invokeLater(()->ui.updateReadyQueue(ClockInterruptHandlerThread.getCurrentTime()+ " [进程" + process.getPid()+ " 降级到三级队列]"));
            process.setTimesilve(OSKernel.TIME_SLICE_3);//重新设置进程的时间片
            OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime()+ " [进程" + process.getPid()+ " 降级到三级队列]");
        }
        else {
            // 在三级队列中继续执行
            OSKernel.readyQueue3.remove(process);
            OSKernel.readyQueue3.add(process);//重新加入到队尾
            System.out.println(ClockInterruptHandlerThread.getCurrentTime()+" [进程" + process.getPid()+ " 保持在三级队列中,但是重新加入队列尾部]");
            SwingUtilities.invokeLater(()->ui.updateReadyQueue(ClockInterruptHandlerThread.getCurrentTime()+ " [进程" + process.getPid()+ " 保持在三级队列中,但是重新加入队列尾部]"));
            process.setTimesilve(OSKernel.TIME_SLICE_3);//重新设置进程的时间片
            OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime()+" [进程" + process.getPid()+ " 保持在三级队列中,但是重新加入队列尾部]");
        }
    }




    public  boolean create_pcb(PCB pcb){
        if(OSKernel.memory.check_number_pcb()>12){
            String s=ClockInterruptHandlerThread.getCurrentTime()+ " [无法为作业 "+pcb.getPid() +"创建进程 "+"系统最大并发进程数已经达到12!]";
            System.out.println(s);
            SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue(s));//更新UI界面中的信息
            OSKernel.fileUtils.collectLog(s);
            return false;
        }
        else {
             if(OSKernel.memory.AllocateMem(pcb)){
                OSKernel.readyQueue1.add(pcb);//加入就绪队列
                pcb.setState(0);//设置为就绪态
                pcb.setTimesilve(OSKernel.TIME_SLICE_1);//时间片为1
                pcb.read_in_queue.add(ClockInterruptHandlerThread.getCurrentTime());//设置第一次进入就绪队列的时间
                pcb.setStart_time(ClockInterruptHandlerThread.getCurrentTime());//设置进程创建时间
                return true;
            }
             else {
                 System.out.println(ClockInterruptHandlerThread.getCurrentTime()+" [该时刻，内存已满，无法找到合适的连续来为内存块装配作业" + pcb.getPid() + "分配]");
                 SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue(ClockInterruptHandlerThread.getCurrentTime()+" [该时刻，内存已满，无法找到合适的连续来为内存块装配作业" + pcb.getPid() + "分配]"));
                 OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime()+" [该时刻，内存已满，无法找到合适的连续来为内存块装配作业" + pcb.getPid() + "分配]");
                 return false;
             }
        }
    }//创建进程函数

}
