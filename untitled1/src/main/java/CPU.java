import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CPU {
    public UI ui;
    private  int pc; // 程序计数器
    private  int ir; // 指令寄存器,指明当前是那种指令
    private  int psw; // 程序状态字
    private Map<String, Integer> registerBackup; // 寄存器备份，用于进程切换时保存寄存器状态
    public  PCB currentProcess; // 当前正在执行的进程
    public  int instructionPointer; // 当前进程的指令指针，就是存放当前运行到哪个指令
    public int getPc() {
        return pc;
    }
    public void setPc(int pc) {
        this.pc = pc;
    }
    public  int getPsw() {
        return this.psw;
    }
    public  void setPsw(int psw) {
        this.psw = psw;
    }
    public  int getIr() {
        return ir;
    }
    public void setIr(int ir) {
        this.ir = ir;
    }
    public  PCB getCurrentProcess() {
        return this.currentProcess;
    }
    public  void setCurrentProcess(PCB currentProcess) {
        this.currentProcess = currentProcess;
    }

    public CPU(UI ui) {
        this.pc = 0;
        this.ir = 0;//0代表CPU用户态
        //1代表核心态
        //-1代表CPU空闲
        this.psw = -1;
        this.registerBackup = new HashMap<>();
        currentProcess = null;
        instructionPointer = 0;
        this.ui=ui;
    }


    public  void runProcess()
    {
        int current_pc=this.getCurrentProcess().getPc();
        int current_ir_state=this.getCurrentProcess().getIr();
        pc=current_pc;
        //获取指令
        if (current_ir_state==0)
        {
            this.setPsw(0);
            this.getCurrentProcess().note_cal();
            // 计算类指令，不能中断
            String s=ClockInterruptHandlerThread.getCurrentTime()+ " [运行进程：进程" + this.getCurrentProcess().getPid()+ " 的第" +(current_pc+1)+"条指令运行完成，该指令为计算指令,指令编号为0，数据大小为100B，指令的物理地址为："+OSKernel.memory.mmu.Address_transfomer(this.getCurrentProcess())+"]";
            System.out.println(s);
            SwingUtilities.invokeLater(()->ui.updateRunningProcess(s));//更新UI界面中的信息
            OSKernel.fileUtils.collectLog(s);
            current_pc++;
            this.getCurrentProcess().setPc(current_pc);
            psw=0;//CPU处于用户态
        }//运行处理计算类指令的逻辑


        else if(current_ir_state==1)
        {
            this.setPsw(1);
            PCB pcb=this.getCurrentProcess();
            //IO类型的指令
            switch (pcb.getTimesilve()){
                case 1:OSKernel.readyQueue1.remove(pcb);break;
                case 2:OSKernel.readyQueue2.remove(pcb);break;
                case 4:OSKernel.readyQueue3.remove(pcb);break;
            }
            OSKernel.I_block_queue.add(pcb);//进程进入输入阻塞队列
            OSKernel.fileUtils.logInputBlock(pcb.getPid(),("进入时间："+ClockInterruptHandlerThread.getCurrentTime()));//记录进入输入阻塞队列的时间和对应的进程ID
            System.out.println(ClockInterruptHandlerThread.getCurrentTime()+" [阻塞进程：进程" + pcb.getPid()+ "进入输入阻塞队列，开始调度下一个进程]");
            SwingUtilities.invokeLater(()->ui.updateRunningProcess(ClockInterruptHandlerThread.getCurrentTime()+" [阻塞进程：进程" + pcb.getPid()+ "进入输入阻塞队列，开始调度下一个进程]"));//更新UI界面中的信息
            OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime()+" [阻塞进程：进程" + pcb.getPid()+ "进入输入阻塞队列，开始调度下一个进程]");//保存到文件
            OSKernel.fileUtils.print_into_blockqueue(ClockInterruptHandlerThread.getCurrentTime());
            psw=1;//CPU处于内核态
        }//运行处理IO




        else {
            this.setPsw(1);
            PCB pcb=this.getCurrentProcess();
            //IO类型的指令
            switch (pcb.getTimesilve()){
                case 1:OSKernel.readyQueue1.remove(pcb);break;
                case 2:OSKernel.readyQueue2.remove(pcb);break;
                case 4:OSKernel.readyQueue3.remove(pcb);break;
            }
            OSKernel.O_block_queue.add(pcb);//进程进入输出阻塞队列
            OSKernel.fileUtils.logOutputBlock(pcb.getPid(),("进入时间："+ClockInterruptHandlerThread.getCurrentTime()));//记录进入输出阻塞队列的时间和对应进程ID
            System.out.println(ClockInterruptHandlerThread.getCurrentTime()+" [阻塞进程：进程" + pcb.getPid()+ "进入输出阻塞队列,开始调度下一个进程]");
            SwingUtilities.invokeLater(()->ui.updateRunningProcess(ClockInterruptHandlerThread.getCurrentTime()+" [阻塞进程：进程" + pcb.getPid()+ "进入输入阻塞队列，开始调度下一个进程]"));//更新UI界面中的信息
            OSKernel.fileUtils.collectLog(ClockInterruptHandlerThread.getCurrentTime()+" [阻塞进程：进程" + pcb.getPid()+ "进入输出阻塞队列，开始调度下一个进程]");//保存到文件
            OSKernel.fileUtils.print_into_blockqueue(ClockInterruptHandlerThread.getCurrentTime());
            psw=1;//CPU处于内核态
        }//运行处理IO

    }


   public void CPU_PRO()
   {
        pc=0;
        setPsw(1);
   }//CPU现场保护
    public void CPU_REC()
    {

    }//CPU现场恢复
}
