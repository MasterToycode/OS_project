import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class UI extends JFrame {
    public List<String> jobRequests; // 定义作业请求列表
    private JLabel clockLabel;
    //时钟显示区
    private JTextArea jobRequestArea, readyQueueArea, runningArea, blockArea;
    //作业请求区、内存区、进程就绪区
    //进程运行区、进程阻塞区
    private JPanel memoryArea;

    public UI() {
        jobRequests = new ArrayList<>(); // 初始化列表


        // 设置窗口
        setTitle("操作系统仿真程序");
        setSize(1250, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//关闭窗口时，程序会终止并退出
        setLocationRelativeTo(null);//将窗口居中显示在屏幕上

        // 主面板布局
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 上部按钮区（执行、暂停、保存、实时）
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
        JButton executeButton = new JButton("执行");
        JButton pauseButton = new JButton("暂停");
        JButton saveButton = new JButton("保存");
        JButton realTimeButton = new JButton("实时");
        buttonPanel.add(executeButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(realTimeButton);

        // 中部显示区
        JPanel displayPanel = new JPanel(new GridLayout(2, 3));

        // 时钟显示区
        JPanel clockPanel = new JPanel(new BorderLayout());
        clockLabel = new JLabel("时钟: 0", SwingConstants.CENTER);
        clockLabel.setFont(new Font("Serif", Font.BOLD, 24));//设置 clockLabel 的字体为 Serif 字体，样式为 粗体（Font.BOLD），大小为 24 号字体。
        clockPanel.setBorder(BorderFactory.createTitledBorder("时钟显示区"));
        clockPanel.add(clockLabel, BorderLayout.CENTER);//并将其放置在 BorderLayout.CENTER 区域（面板的中间部分），
        // 确保时钟显示标签位于面板的中央。


        // 作业请求区
        JPanel jobRequestPanel = new JPanel(new BorderLayout());
        jobRequestArea = new JTextArea();
        jobRequestArea.setEditable(false);//不可编辑（false），这意味着用户无法直接在这个区域输入或修改文本内容。
        jobRequestPanel.setBorder(BorderFactory.createTitledBorder("作业请求区"));
        jobRequestPanel.add(new JScrollPane(jobRequestArea), BorderLayout.CENTER);//为文本区域 jobRequestArea 添加了滚动条功能。
        // JScrollPane 是一个滚动面板，当文本内容超过显示区域时，用户可以使用滚动条查看所有内容。


        // 内存区
        // 内存区
        JPanel memoryPanel = new JPanel(new BorderLayout());
        memoryArea = new JPanel(new GridLayout(4, 4)); // 设置为 4 行 4 列的 GridLayout
        memoryPanel.setBorder(BorderFactory.createTitledBorder("内存区"));
        memoryPanel.add(new JScrollPane(memoryArea), BorderLayout.CENTER);



        // 进程就绪区
        JPanel readyQueuePanel = new JPanel(new BorderLayout());
        readyQueueArea = new JTextArea();
        readyQueueArea.setEditable(false);
        readyQueuePanel.setBorder(BorderFactory.createTitledBorder("进程就绪区"));
        readyQueuePanel.add(new JScrollPane(readyQueueArea), BorderLayout.CENTER);


        // 进程运行区
        JPanel runningPanel = new JPanel(new BorderLayout());
        runningArea = new JTextArea();
        runningArea.setEditable(false);
        runningPanel.setBorder(BorderFactory.createTitledBorder("进程运行区"));
        runningPanel.add(new JScrollPane(runningArea), BorderLayout.CENTER);


        // 进程阻塞区，显示IO操作的区域
        JPanel blockPanel = new JPanel(new BorderLayout());
        blockArea = new JTextArea();
        blockArea.setEditable(false);
        blockPanel.setBorder(BorderFactory.createTitledBorder("进程阻塞区"));
        blockPanel.add(new JScrollPane(blockArea), BorderLayout.CENTER);


        // 添加各区域到显示面板
        displayPanel.add(clockPanel);
        displayPanel.add(jobRequestPanel);
        displayPanel.add(memoryPanel);
        displayPanel.add(readyQueuePanel);
        displayPanel.add(runningPanel);
        displayPanel.add(blockPanel);


        // 将按钮区和显示区添加到主面板
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(displayPanel, BorderLayout.CENTER);

        // 添加主面板到窗口
        setContentPane(mainPanel);

        // 添加窗口监听器
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 检查是否已经保存调度信息
                if (!OSKernel.fileUtils.issaved||!OSKernel.check_all_the_job()) {
                    int response = JOptionPane.showConfirmDialog(null,
                            "你有未保存的调度信息，是否保存？",
                            "保存提示",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    // 根据用户选择的选项执行相应操作
                    switch (response) {
                        case JOptionPane.YES_OPTION:
                            // 用户选择保存并退出
                            OSKernel.fileUtils.save_to_file(ClockInterruptHandlerThread.getCurrentTime());
                            System.exit(0); // 退出程序
                            break;
                        case JOptionPane.NO_OPTION:
                            // 用户选择不保存并退出
                            System.exit(0); // 直接退出程序
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            // 用户选择取消，继续运行程序，阻止窗口关闭
                            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                            break;
                        default:
                            break;
                    }
                } else {
                    System.exit(0); // 如果已经保存，直接退出程序
                }
            }
        });




        // 按钮事件监听
        executeButton.addActionListener(e -> {
            if (SyncManager.isPaused) {
                resumeExecution();  // 恢复执行
            } else if (!SyncManager.isStarted) {
                startExecution();   // 第一次启动执行
                SyncManager.isStarted = true;   // 标记已经启动
            }
        });


        pauseButton.addActionListener(e -> pauseExecution());
        saveButton.addActionListener(e -> saveToFile());
        realTimeButton.addActionListener(e -> handleRealTimeJob());
    }//初始化UI的所有组件和一些必要的存储显示信息的数据结构


    // 更新时钟显示
    public void updateClock(int time) {
        SwingUtilities.invokeLater(() -> clockLabel.setText("时钟: " + time));
    }

    // 更新作业请求显示
    public void updateJobRequests() {
        SwingUtilities.invokeLater(() -> {
            for (String job : jobRequests) {
                jobRequestArea.append(job + "\n"); // 逐行追加新的作业请求
            }
        });
    }

    public void say_to_job_are(String message){
        jobRequestArea.append(message+"\n");
    }

    // 添加作业请求
    public void addJobRequest(String job) {
        jobRequests.add(job); // 将新作业请求添加到列表中
        updateJobRequests(); // 更新显示
    }



    public void updateMemoryStatus(ArrayList<Memory_Block> memoryBlocks) {
        // 遍历当前的内存块，更新显示
        for (int i = 0; i < memoryBlocks.size(); i++) {
            Memory_Block block = memoryBlocks.get(i);
            JPanel blockPanel;

            // 检查该块是否已存在
            if (memoryArea.getComponentCount() > i) {
                blockPanel = (JPanel) memoryArea.getComponent(i);
            } else {
                blockPanel = new JPanel();
                blockPanel.setPreferredSize(new Dimension(100, 30)); // 设置每个块的尺寸
                memoryArea.add(blockPanel); // 添加到内存区域
            }

            // 根据块的占用状态更新颜色
            if (block.isOccupied()) {
                blockPanel.setBackground(Color.RED); // 被占用，设置为红色
            } else {
                blockPanel.setBackground(Color.GREEN); // 空闲，设置为绿色
            }

            blockPanel.removeAll(); // 清空之前的标签
            blockPanel.add(new JLabel("块 " + (block.getBlock_ID() + 1))); // 显示块编号
        }




        memoryArea.revalidate(); // 重新验证面板
        memoryArea.repaint(); // 刷新面板
    }


    // 更新就绪队列显示
    public void updateReadyQueue(String message) {
        readyQueueArea.append(message+"\n"); // 添加新信息到就绪队列显示区域
    }


    //更新进程运行区的显示
    public void updateRunningProcess(String message) {
        runningArea.append(message+"\n");
    }


    //更新进程阻塞区的显示
    public void updateBlockedProcesses(String message) {
        blockArea.append(message+"\n");
    }



    // 按钮功能（具体逻辑已经实现好）
    private void startExecution() {
        // 启动操作系统调度逻辑
        try {
            ProcessSchedulingHandlerThread handlerThread = new ProcessSchedulingHandlerThread(OSKernel.ui);
            InputBlockThread blockThread = new InputBlockThread();
            ClockInterruptHandlerThread thread1 = new ClockInterruptHandlerThread(OSKernel.ui);
            handlerThread.start();
            blockThread.start();
            Thread.sleep(1000); // 确保前两个线程已经启动
            thread1.start(); // 启动时钟线程和仿真程序
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private void pauseExecution() {
        SyncManager.pauseLock.lock();
        try {
            SyncManager.isPaused = true;  // 设置为暂停状态
            System.out.println("系统已暂停");
        } finally {
            SyncManager.pauseLock.unlock();
        }
    }

    private void resumeExecution() {
        SyncManager.pauseLock.lock();
        try {
            SyncManager.isPaused = false;  // 恢复执行状态
            SyncManager.pauseCondition.signalAll();  // 通知所有线程恢复执行
            System.out.println("系统已恢复");
        } finally {
            SyncManager.pauseLock.unlock();
        }
    }


    private void saveToFile() {
        if(OSKernel.check_all_the_job()) OSKernel.fileUtils.save_to_file(ClockInterruptHandlerThread.getCurrentTime());
        // 保存调度信息到文件
        else if(!OSKernel.check_all_the_job()) {
            System.out.println(ClockInterruptHandlerThread.getCurrentTime()+" [系统中还有作业没有做完]");
            say_to_job_are(ClockInterruptHandlerThread.getCurrentTime()+" [系统中还有作业没有做完]");
        }
    }


    //处理随机添加作业的逻辑
    private void handleRealTimeJob() {
        try {
            OSKernel.fileUtils.AddOnePro();
        } catch (IOException e) {
            System.out.println(ClockInterruptHandlerThread.getCurrentTime()+" [随机添加作业失败]");
            say_to_job_are(ClockInterruptHandlerThread.getCurrentTime()+" [随机添加作业失败]");
        }
    }

    public void createAndShowGUI() {
        setVisible(true); // 显示界面
    }

}
