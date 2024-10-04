import javax.swing.*;
import java.io.*;
import java.util.*;


public class FileUtils {
    public static   final String dir = "../input3";
    public static final String save_dir = "../output";
    private  ArrayList<Instruction> LoadIRs(String filepath) throws IOException {
        ArrayList<Instruction> instructions = new ArrayList<>();
        try (BufferedReader bfr = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] instructionData = line.split(",");
                    if (instructionData.length == 2) {
                        try {
                            Integer Instruc_ID = Integer.parseInt(instructionData[0].trim());
                            Integer Instruc_State = Integer.parseInt(instructionData[1].trim());

                            // 打印读入的指令 ID 和状态

                            instructions.add(new Instruction(Instruc_ID, Instruc_State));
                        } catch (NumberFormatException e) {
                            System.err.println(".txt 文件中的数据格式不合理！: " + filepath + ", line: " + line);
                        }
                    } else {
                        System.err.println(".txt文件格式不对: " + filepath + ", line: " + line);
                    }
                }
            }
        }
        return instructions; // 从单个.txt文件中读取所有的指令
    }




    public  void LoadJobs() throws IOException {
        File folder = new File(dir);
        File[] files = folder.listFiles((d, name) -> name.endsWith(".txt"));
        if (files != null) {
            for (File file : files)
            {
                String filename = file.getName();
                String[] parts = filename.split("-");

                if (parts.length == 3) {
                    int jobId = Integer.parseInt(parts[0]);//作业ID
                    int requestTime = Integer.parseInt(parts[1]);//理论作业到达的时间
                    int priority = Integer.parseInt(parts[2].replace(".txt", ""));//作用的优先级
                    ArrayList<Instruction> instructions = LoadIRs(file.getPath());
                    Job job = new Job(jobId, requestTime, instructions.size(), instructions);

                    OSKernel.backupQueue.add(job);//作业加入后备队列
                }

            }
        }
    }//从input的文件夹中读取所有的指令，并加入后被队列！





    // 随机生成指令
    // 每一行代表一个指令，第一列代表指令ID，下标从1开始到InstrucNum，
    // 第二列代表指令状态，有0,1,2三种取值，其中0取值概率为0.7，1取值概率为0.2，2取值概率为0.1
    private  ArrayList<Instruction> CreateIRs(int InstrucNum) {
        //InstrucNum代表生成指令的数目
        double[] probabilities = {0.7, 0.2, 1.0}; // 各状态的概率
        ArrayList<Instruction> instructions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < InstrucNum; i++) {
            int instructionID = i + 1;
            double randomValue = random.nextDouble();
            int instructionStatus = 0; // 默认为0（用户态计算操作指令）

            for (int j = 0; j < probabilities.length; j++) {
                if (randomValue < probabilities[j]) {
                    instructionStatus = j;
                    break;
                }
            }

            Instruction instruction = new Instruction(instructionID, instructionStatus);
            instructions.add(instruction);
        }
        return instructions;
    }





    private  int TempIndex = new Random().nextInt(8 + 1) + 24; // 随机生成一个作业索引
    public  void AddOnePro() throws IOException//添加一个作业直接到后备队列中
    {
        //随机生成一个作业，然后添加到后备队列中
        TempIndex++;
        int arriveTime=ClockInterruptHandlerThread.simulationTime;//作业的到达时间为当前的时钟时间
        //随机生成指令数目在20-30之间
        int InstrucNum = (int)(Math.random()*10)+20;
        // 创建作业对象并传递优先级
        Job job = new Job(TempIndex, arriveTime, InstrucNum, CreateIRs(InstrucNum));
        OSKernel.backupQueue.add(job);
    }




    public void logInputBlock(int pid,String s) {
        // 检查该进程 ID 是否已经存在于输入阻塞队列中
        OSKernel.log_I_block.putIfAbsent(pid, new ArrayList<>()); // 如果不存在，创建一个新的列表
        OSKernel.log_I_block.get(pid).add(s); // 添加时间
    }




    public void logOutputBlock(int pid,String s) {
        // 检查该进程 ID 是否已经存在于输出阻塞队列中
        OSKernel.log_O_block.putIfAbsent(pid, new ArrayList<>()); // 如果不存在，创建一个新的列表
            OSKernel.log_O_block.get(pid).add(s); // 添加唤醒时间
    }





    public void printBlockQueueLogs() {
        // 打印 BB1 (输入阻塞队列) 信息
        System.out.println("进程状态统计信息：");
        for (Map.Entry<Integer, List<String>> entry : OSKernel.log_I_block.entrySet()) {
            StringBuilder sb = new StringBuilder();
            sb.append("进程 ID: ").append(entry.getKey()).append("/");
            for (String timeInfo : entry.getValue()) {
                sb.append(timeInfo).append(", ");
            }
            // 移除最后的逗号和空格
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 2); // 去掉最后的", "
            }
            String logMessage = "BB1:[阻塞队列 1,键盘输入:" + sb + "]";
            System.out.println(logMessage);
            collectLog(logMessage);

            // 更新 UI 界面
            OSKernel.ui.updateBlockedProcesses(logMessage);
        }


        // 打印 BB2 (输出阻塞队列) 信息
        for (Map.Entry<Integer, List<String>> entry : OSKernel.log_O_block.entrySet()) {
            StringBuilder sb = new StringBuilder();
            sb.append("进程 ID: ").append(entry.getKey()).append("/");
            for (String timeInfo : entry.getValue()) {
                sb.append(timeInfo).append(", ");
            }
            // 移除最后的逗号和空格
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 2); // 去掉最后的", "
            }
            String logMessage = "BB2:[阻塞队列 2,屏幕显示:" + sb + "]";
            System.out.println(logMessage);
            collectLog(logMessage);

            // 更新 UI 界面
            OSKernel.ui.updateBlockedProcesses(logMessage);
        }
        System.out.flush(); // 刷新缓冲区，确保所有内容被打印
    }






    // 保存所有的输出信息
    public  List<String> logBuffer = new ArrayList<>();
    public boolean issaved=false;//是否保存
    public  void collectLog(String message) {
        logBuffer.add(message); // 保存信息到集合中
    }
    public void save_to_file(int totalMinutes){
        String fileName = save_dir + "/ProcessResults-" + totalMinutes + "-DJFK.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String log : logBuffer) {
                writer.write(log);
                writer.newLine(); // 写入换行
            }
            System.out.println("日志保存到文件: " + fileName);
            issaved=true;
            SwingUtilities.invokeLater(()->OSKernel.ui.say_to_job_are("日志保存到文件: " + fileName));
        } catch (IOException e) {
            System.err.println("保存日志时出错: " + e.getMessage());
        }

    }//保存所有进程调度和状态信息的函数




    public void print_back_to_queue(int time) {
        System.out.printf("%d [重新进入就绪队列:]\n", time);
        SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue(time+" [重新进入就绪队列:]"));
        OSKernel.fileUtils.collectLog(time+" [重新进入就绪队列:]");
        // 一级就绪队列
        System.out.println("     [一级就绪队列进程ID列表         剩余指令数]");
        SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue("       [一级就绪队列进程ID列表         剩余指令数]"));
        OSKernel.fileUtils.collectLog("       [一级就绪队列进程ID列表         剩余指令数]");
        if (!OSKernel.readyQueue1.isEmpty()) {
            for (PCB pcb : OSKernel.readyQueue1) {
                System.out.printf("     [%-30d %-17d]\n", pcb.getPid(), (pcb.getInstructionCount() - pcb.getPc()));
                SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue("       ["+pcb.getPid()+"                                                  "+(pcb.getInstructionCount() - pcb.getPc())+"]"));
                OSKernel.fileUtils.collectLog("       ["+pcb.getPid()+"                                                  "+(pcb.getInstructionCount() - pcb.getPc())+"]");


            }
        } else {
            System.out.printf("     [无进程                          无指令]\n");
            SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue("       [无进程                                     无指令]"));
            OSKernel.fileUtils.collectLog("       [无进程                                           无指令]");
        }





        // 二级就绪队列
        System.out.printf("     [二级就绪队列进程ID列表         剩余指令数]\n");
        SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue("       [二级就绪队列进程ID列表         剩余指令数]"));
        OSKernel.fileUtils.collectLog("       [二级就绪队列进程ID列表         剩余指令数]");
        if (!OSKernel.readyQueue2.isEmpty()) {
            for (PCB pcb : OSKernel.readyQueue2) {
                System.out.printf("     [%-30d %-17d]\n", pcb.getPid(), (pcb.getInstructionCount() - pcb.getPc()));
                SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue("       ["+pcb.getPid()+"                                                  "+(pcb.getInstructionCount() - pcb.getPc())+"]"));
                OSKernel.fileUtils.collectLog("       ["+pcb.getPid()+"                                                  "+(pcb.getInstructionCount() - pcb.getPc())+"]");
            }
        } else {
            System.out.printf("     [无进程                          无指令]\n");
            SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue("       [无进程                                     无指令]"));
            OSKernel.fileUtils.collectLog("       [无进程                                           无指令]");
        }




        // 三级就绪队列
        System.out.printf("     [三级就绪队列进程ID列表         剩余指令数]\n");
        SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue("       [三级就绪队列进程ID列表         剩余指令数]"));
        OSKernel.fileUtils.collectLog("      [三级就绪队列进程ID列表         剩余指令数]");
        if (!OSKernel.readyQueue3.isEmpty()) {
            for (PCB pcb : OSKernel.readyQueue3) {
                System.out.printf("     [%-30d %-17d]\n", pcb.getPid(), (pcb.getInstructionCount() - pcb.getPc()));
                SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue("       ["+pcb.getPid()+"                                                 "+(pcb.getInstructionCount() - pcb.getPc())+"]"));
                OSKernel.fileUtils.collectLog("       ["+pcb.getPid()+"                                                  "+(pcb.getInstructionCount() - pcb.getPc())+"]");
            }
        } else {
            System.out.printf("     [无进程                          无指令]\n");
            SwingUtilities.invokeLater(()->OSKernel.ui.updateReadyQueue("       [无进程                                     无指令]"));
            OSKernel.fileUtils.collectLog("       [无进程                                           无指令]");
        }
        System.out.flush(); // 刷新缓冲区，确保所有内容被打印
    }//打印重新进入就绪队列是的信息





    public void print_into_blockqueue(int time){
        System.out.printf("%d [阻塞进程:输入阻塞队列:]\n", time);
        SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses(time+" [阻塞进程:输入阻塞队列:]"));
        OSKernel.fileUtils.collectLog(time+" [阻塞进程:输入阻塞队列:]");
        System.out.println("     [进程ID列表]");
        SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses("       [进程ID列表]"));
        OSKernel.fileUtils.collectLog("       [进程ID列表]");
        if (!OSKernel.I_block_queue.isEmpty()){
        for (PCB pcb : OSKernel.I_block_queue) {
            System.out.println("        [" + pcb.getPid()+"]");
            SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses("        ["+pcb.getPid()+"]"));
            OSKernel.fileUtils.collectLog("          ["+pcb.getPid()+"]");
        }
        }
        else{ System.out.println("     [无进程]");
            SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses("       [无进程]"));
            OSKernel.fileUtils.collectLog("       [无进程]");
        }


        System.out.printf("%d [阻塞进程:输出阻塞队列:]\n", time);
        SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses(time+" [阻塞进程:输出阻塞队列:]"));
        OSKernel.fileUtils.collectLog(time+" [阻塞进程:输出阻塞队列:]");
        System.out.println("     [进程ID列表]");
        SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses("       [进程ID列表]"));
        OSKernel.fileUtils.collectLog("       [进程ID列表]");
        if (!OSKernel.O_block_queue.isEmpty()){
            for (PCB pcb : OSKernel.O_block_queue) {
                System.out.println("        [" + pcb.getPid()+"]");
                SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses("          ["+pcb.getPid()+"]"));
                OSKernel.fileUtils.collectLog("          ["+pcb.getPid()+"]");
            }

        }
        else{ System.out.println("     [无进程]");
            SwingUtilities.invokeLater(()->OSKernel.ui.updateBlockedProcesses("       [无进程]"));
            OSKernel.fileUtils.collectLog("       [无进程]");
        }
        System.out.flush(); // 刷新缓冲区，确保所有内容被打印
    }//打印阻塞时候的信息
}

