import javax.swing.*;

public class InputBlockThread extends Thread
{
    public  int count_i=0;
    public  int count_o=0;
    public static final int op_IO_time=2;//处理一个IO的时间
        @Override
        public void run() {
            while (true) { // 无限循环

                SyncManager.lock.lock(); // 获取锁，确保线程安全
                try {
                    SyncManager.io_clk=true;
                    SyncManager.io_clk_Condition.signal();
                    SyncManager.ioCondition.await();
                    ALL_IO();//处理所有类型IO的逻辑
                } catch (InterruptedException e)
                {
                    e.printStackTrace(); // 处理异常
                } finally {
                    SyncManager.lock.unlock(); // 释放锁
                }
            }
        }




    public  void ALL_IO()
        {

            if (!OSKernel.I_block_queue.isEmpty())
            {
                PCB pcb=OSKernel.I_block_queue.peek();
                count_i++;
                if (count_i==op_IO_time){
                    OSKernel.I_block_queue.remove(pcb);//如果IO操作执行完成，从IO阻塞队列中弹出
                    OSKernel.fileUtils.logInputBlock(pcb.getPid(),("唤醒时间："+ClockInterruptHandlerThread.getCurrentTime()));//记录离开输入阻塞队列的时间和对应的进程ID
                    int pc = pcb.getPc();
                    System.out.println(ClockInterruptHandlerThread.getCurrentTime()+" [IO操作：进程"+pcb.getPid()+"的第"+(pcb.getPc()+1)+"条指令"+pcb.get_ir_state()+"执行完成]");
                    SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses(ClockInterruptHandlerThread.getCurrentTime()+" [IO操作：进程"+pcb.getPid()+"的第"+(pcb.getPc()+1)+"条指令"+pcb.get_ir_state()+"执行完成]"));
                    OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime()+" [IO操作：进程"+pcb.getPid()+"的第"+(pcb.getPc()+1)+"条指令"+pcb.get_ir_state()+"执行完成]");
                    pc++;



                    if(pc>=pcb.getInstructionCount())//如果进程执行结束
                    {
                        pcb.setTatalruntimes(ClockInterruptHandlerThread.getCurrentTime());
                        pcb.setState(-1);
                        OSKernel.memory.freemem(pcb);//释放内存
                        System.out.println(ClockInterruptHandlerThread.getCurrentTime()+ " [终止进程：进程"+pcb.getPid()+"执行结束"+"一共耗时："+(pcb.getTatalruntimes()-pcb.getInTime())+"。内存释放成功]");
                        SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses(ClockInterruptHandlerThread.getCurrentTime()+ " [终止进程：进程"+pcb.getPid()+"执行结束"+"一共耗时："+(pcb.getTatalruntimes()-pcb.getInTime())+"内存释放成功]"));
                        OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime()+ " [终止进程：进程"+pcb.getPid()+"执行结束"+"一共耗时："+(pcb.getTatalruntimes()-pcb.getInTime())+"。内存释放成功]");
                        OSKernel.fileUtils.printBlockQueueLogs();//进程结束打印进程的状态信息
                    }


                    else if(pc<pcb.getInstructionCount()) //如果进程还没有结束
                    {
                        pcb.setPc(pc);
                        back_to_readyqueue(pcb);//重回就绪队列
                    }
                    count_i=0;
                }


            }

            if (!OSKernel.O_block_queue.isEmpty())
            {

                PCB pcb=OSKernel.O_block_queue.peek();
                count_o++;
                if (count_o==op_IO_time){
                    OSKernel.O_block_queue.remove(pcb);//如果IO操作执行完成，从IO阻塞队列中弹出
                    OSKernel.fileUtils.logOutputBlock(pcb.getPid(),("唤醒时间："+ClockInterruptHandlerThread.getCurrentTime()));//记录离开输出阻塞队列的时间和对应进程ID
                    int pc = pcb.getPc();
                    System.out.println(ClockInterruptHandlerThread.getCurrentTime()+" [IO操作：进程"+pcb.getPid()+"的第"+(pcb.getPc()+1)+"条指令"+pcb.get_ir_state()+"执行完成]");
                    SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses(ClockInterruptHandlerThread.getCurrentTime()+" [IO操作：进程"+pcb.getPid()+"的第"+(pcb.getPc()+1)+"条指令"+pcb.get_ir_state()+"执行完成]"));
                    OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime()+" [IO操作：进程"+pcb.getPid()+"的第"+(pcb.getPc()+1)+"条指令"+pcb.get_ir_state()+"执行完成]");
                    pc++;



                    if(pc>=pcb.getInstructionCount())//如果进程执行结束
                    {
                        pcb.setTatalruntimes(ClockInterruptHandlerThread.getCurrentTime());
                        pcb.setState(-1);
                        OSKernel.memory.freemem(pcb);
                        System.out.println(ClockInterruptHandlerThread.getCurrentTime()+ " [终止进程：进程"+pcb.getPid()+"执行结束"+"一共耗时："+(pcb.getTatalruntimes()-pcb.getInTime())+"。内存释放成功]");
                        SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses(ClockInterruptHandlerThread.getCurrentTime()+ " [终止进程：进程"+pcb.getPid()+"执行结束"+"一共耗时："+(pcb.getTatalruntimes()-pcb.getInTime())+"内存释放成功]"));
                        OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime()+ " [终止进程：进程"+pcb.getPid()+"执行结束"+"一共耗时："+(pcb.getTatalruntimes()-pcb.getInTime())+"。内存释放成功]");
                        OSKernel.fileUtils.printBlockQueueLogs();//进程结束打印进程的状态信息
                    }


                    else if(pc<pcb.getInstructionCount()) //如果进程还没有结束
                    {
                        pcb.setPc(pc);
                        back_to_readyqueue(pcb);//重回就绪队列
                    }
                    count_o=0;
                }

            }
        }





        public   void back_to_readyqueue(PCB pcb){
            switch (pcb.getTimesilve()){
                case 1:
                    OSKernel.readyQueue1.add(pcb);
                    pcb.read_in_queue.add(ClockInterruptHandlerThread.getCurrentTime());//设置再次进入就绪队列的时间
                    System.out.println(ClockInterruptHandlerThread.getCurrentTime() + " [唤醒进程：进程" + pcb.getPid() + " 再次进入就绪队列1,待执行指令条数：" + (pcb.getInstructionCount() - pcb.getPc()) + "]");
                    SwingUtilities.invokeLater(()->OSKernel.ui
                            .updateBlockedProcesses(ClockInterruptHandlerThread.getCurrentTime() + " [唤醒进程：进程" + pcb.getPid() + " 再次进入就绪队列1,待执行指令条数：" + (pcb.getInstructionCount() - pcb.getPc()) + "]"));
                    OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime() + " [唤醒进程：进程" + pcb.getPid() + " 再次进入就绪队列1,待执行指令条数：" + (pcb.getInstructionCount() - pcb.getPc()) + "]");
                    OSKernel.fileUtils.print_back_to_queue(ClockInterruptHandlerThread.getCurrentTime());//打印重新回到就绪队列的PCB信息
                    break;
                case 2:
                    OSKernel.readyQueue2.add(pcb);
                    pcb.read_in_queue.add(ClockInterruptHandlerThread.getCurrentTime());//设置再次进入就绪队列的时间
                    System.out.println(ClockInterruptHandlerThread.getCurrentTime() + " [唤醒进程：进程" + pcb.getPid() + " 再次进入就绪队列2,待执行指令条数：" + (pcb.getInstructionCount() - pcb.getPc()) + "]");
                    SwingUtilities.invokeLater(()->OSKernel.ui
                            .updateBlockedProcesses(ClockInterruptHandlerThread.getCurrentTime() + " [唤醒进程：进程" + pcb.getPid() + " 再次进入就绪队列2,待执行指令条数：" + (pcb.getInstructionCount() - pcb.getPc()) + "]"));
                    OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime() + " [唤醒进程：进程" + pcb.getPid() + " 再次进入就绪队列2,待执行指令条数：" + (pcb.getInstructionCount() - pcb.getPc()) + "]");
                    OSKernel.fileUtils.print_back_to_queue(ClockInterruptHandlerThread.getCurrentTime());
                    break;
                case 4:
                    OSKernel.readyQueue3.add(pcb);
                    pcb.read_in_queue.add(ClockInterruptHandlerThread.getCurrentTime());//设置再次进入就绪队列的时间
                    System.out.println(ClockInterruptHandlerThread.getCurrentTime() + " [唤醒进程：进程" + pcb.getPid() + " 再次进入就绪队列3,待执行指令条数：" + (pcb.getInstructionCount() - pcb.getPc()) + "]");
                    SwingUtilities.invokeLater(()->OSKernel.ui
                            .updateBlockedProcesses(ClockInterruptHandlerThread.getCurrentTime() + " [唤醒进程：进程" + pcb.getPid() + " 再次进入就绪队列3,待执行指令条数：" + (pcb.getInstructionCount() - pcb.getPc()) + "]"));
                    OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime() + " [唤醒进程：进程" + pcb.getPid() + " 再次进入就绪队列3,待执行指令条数：" + (pcb.getInstructionCount() - pcb.getPc()) + "]");
                    OSKernel.fileUtils.print_back_to_queue(ClockInterruptHandlerThread.getCurrentTime());
                    break;
            }
        }


}

